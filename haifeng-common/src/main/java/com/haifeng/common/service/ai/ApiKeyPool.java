package com.haifeng.common.service.ai;

import com.haifeng.common.config.DeepSeekProperties;
import com.haifeng.common.entity.system.ModelProvider;
import com.haifeng.common.mapper.system.ModelProviderMapper;
import com.haifeng.common.mapper.system.SystemSettingsMapper;
import com.haifeng.common.service.ai.dto.ModelProviderConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * API key 池：
 * - 每次从数据库读取启用的 OpenAI 协议模型供应商配置，保证后台调整无需重启
 * - system_settings.provider_name/model_name 对应的配置优先级最高
 * - 默认模型失败后，先回退同厂商其他配置，再回退其他厂商配置
 * - 未配置默认厂商/模型时，按 userId 取模生成稳定的回退顺序
 * - markUnhealthy 按模型供应商 id 写入 Redis 冷却
 * - orderedFallback/pickProvider 跳过冷却中的供应商；全部冷却时返回空
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyPool {

    private static final Long SYSTEM_SETTINGS_ID = 1L;
    private static final String COOLDOWN_KEY_PREFIX = "ai:model-provider:cooldown:";

    private final DeepSeekProperties properties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ModelProviderMapper modelProviderMapper;
    private final SystemSettingsMapper settingsMapper;

    @PostConstruct
    public void init() {
        log.info("ApiKeyPool initialized with database-backed model provider configs");
    }

    public ModelProviderConfig pickProvider(Long userId) {
        List<ModelProviderConfig> providers = orderedFallback(userId);
        if (providers.isEmpty()) {
            return null;
        }
        return providers.get(0);
    }

    /**
     * Legacy convenience method for callers that still need only the API key.
     */
    public String pickKey(Long userId) {
        ModelProviderConfig provider = pickProvider(userId);
        return provider == null ? null : provider.getApiKey();
    }

    public void markUnhealthy(ModelProviderConfig provider) {
        if (provider == null || provider.getId() == null) {
            log.warn("Skip marking model provider unhealthy because provider/id is missing");
            return;
        }
        redisTemplate.opsForValue().set(
                cooldownKey(provider.getId()),
                "1",
                cooldownSeconds(),
                TimeUnit.SECONDS);
        log.warn("Model provider marked unhealthy: id={}, model={}, key=...{}",
                provider.getId(), provider.getModelName(), maskKey(provider.getApiKey()));
    }

    public List<ModelProviderConfig> orderedFallback(Long userId) {
        List<ModelProviderConfig> providers = loadEnabledProviders();
        if (providers.isEmpty()) {
            return Collections.emptyList();
        }

        providers = prioritizeBySystemSettings(providers, userId);
        List<ModelProviderConfig> ordered = new ArrayList<>(providers.size());
        for (ModelProviderConfig provider : providers) {
            if (!isCoolingDown(provider)) {
                ordered.add(provider);
            }
        }
        return ordered;
    }

    public boolean isCoolingDown(ModelProviderConfig provider) {
        return provider != null && isCoolingDown(provider.getId());
    }

    public boolean isCoolingDown(Long providerId) {
        if (providerId == null) {
            return false;
        }
        Boolean has = redisTemplate.hasKey(cooldownKey(providerId));
        return Boolean.TRUE.equals(has);
    }

    private List<ModelProviderConfig> loadEnabledProviders() {
        List<ModelProvider> records = modelProviderMapper.findAllEnabledByType("model");
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }

        List<ModelProviderConfig> providers = new ArrayList<>();
        for (ModelProvider record : records) {
            if (record == null
                    || !StringUtils.hasText(record.getApiKey())
                    || !StringUtils.hasText(record.getModelName())
                    || !StringUtils.hasText(record.getProviderName())) {
                continue;
            }
            providers.add(ModelProviderConfig.builder()
                    .id(record.getId())
                    .apiKey(record.getApiKey().trim())
                    .modelName(record.getModelName().trim())
                    .providerName(normalize(record.getProviderName()))
                    .build());
        }
        return providers;
    }

    private List<ModelProviderConfig> prioritizeBySystemSettings(List<ModelProviderConfig> providers, Long userId) {
        var settings = settingsMapper.selectById(SYSTEM_SETTINGS_ID);
        final String defaultProviderName = settings == null ? null : normalize(settings.getProviderName());
        final String defaultModelName = settings == null ? null : normalize(settings.getModelName());

        List<ModelProviderConfig> userOrdered = rotateByUserId(providers, userId);
        if (!StringUtils.hasText(defaultProviderName) || !StringUtils.hasText(defaultModelName)) {
            return userOrdered;
        }

        return userOrdered.stream()
                .sorted(Comparator.comparingInt(provider -> priority(provider, defaultProviderName, defaultModelName)))
                .toList();
    }

    private List<ModelProviderConfig> rotateByUserId(List<ModelProviderConfig> providers, Long userId) {
        int n = providers.size();
        int firstIdx = indexFor(userId, n);
        List<ModelProviderConfig> ordered = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            ordered.add(providers.get((firstIdx + i) % n));
        }
        return ordered;
    }

    private int priority(ModelProviderConfig provider, String defaultProviderName, String defaultModelName) {
        boolean sameProvider = defaultProviderName.equals(normalize(provider.getProviderName()));
        boolean sameModel = defaultModelName.equals(normalize(provider.getModelName()));
        if (sameProvider && sameModel) {
            return 0;
        }
        if (sameProvider) {
            return 1;
        }
        return 2;
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase() : null;
    }

    private int indexFor(Long userId, int size) {
        long uid = userId == null ? 0L : userId;
        return (int) Math.floorMod(uid, size);
    }

    private String cooldownKey(Long providerId) {
        return COOLDOWN_KEY_PREFIX + providerId;
    }

    private long cooldownSeconds() {
        Integer configured = properties.getKeyCooldownSeconds();
        return configured == null ? 300L : configured.longValue();
    }

    private String maskKey(String key) {
        if (key == null || key.length() < 6) return "***";
        return key.substring(key.length() - 4);
    }
}
