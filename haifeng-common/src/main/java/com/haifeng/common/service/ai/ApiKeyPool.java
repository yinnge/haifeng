package com.haifeng.common.service.ai;

import com.haifeng.common.config.DeepSeekProperties;
import com.haifeng.common.entity.system.ModelProvider;
import com.haifeng.common.mapper.system.ModelProviderMapper;
import com.haifeng.common.service.ai.dto.ModelProviderConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * API key 池：
 * - 每次从数据库读取启用的 DeepSeek 模型供应商配置，保证后台调整无需重启
 * - 按 userId 取模生成稳定的回退顺序
 * - markUnhealthy 按模型供应商 id 写入 Redis 冷却
 * - orderedFallback/pickProvider 跳过冷却中的供应商；全部冷却时返回空
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyPool {

    private static final String DEEPSEEK_PROVIDER = "deepseek";
    private static final String COOLDOWN_KEY_PREFIX = "ai:model-provider:cooldown:";

    private final DeepSeekProperties properties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ModelProviderMapper modelProviderMapper;

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

        int n = providers.size();
        int firstIdx = indexFor(userId, n);
        List<ModelProviderConfig> ordered = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            ModelProviderConfig provider = providers.get((firstIdx + i) % n);
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
        List<ModelProvider> records = modelProviderMapper.findEnabledByProvider(DEEPSEEK_PROVIDER);
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }

        List<ModelProviderConfig> providers = new ArrayList<>();
        for (ModelProvider record : records) {
            if (record == null
                    || !StringUtils.hasText(record.getApiKey())
                    || !StringUtils.hasText(record.getModelName())) {
                continue;
            }
            providers.add(ModelProviderConfig.builder()
                    .id(record.getId())
                    .apiKey(record.getApiKey().trim())
                    .modelName(record.getModelName().trim())
                    .providerName(StringUtils.hasText(record.getProviderName())
                            ? record.getProviderName().trim()
                            : DEEPSEEK_PROVIDER)
                    .build());
        }
        return providers;
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
