package com.haifeng.common.service.ai;

import com.haifeng.common.config.DeepSeekProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * API key 池：
 * - 一致性哈希按 userId 选 key（命中缓存）
 * - markUnhealthy 进入 Redis 冷却
 * - pickKey 跳过冷却中的 key；全部冷却时仍返回首选
 * - orderedFallback 返回首选 + 其余顺序 key（流式调用失败重试用）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyPool {

    private static final String COOLDOWN_KEY_PREFIX = "ai:key:cooldown:";

    private final DeepSeekProperties properties;
    private final RedisTemplate<String, Object> redisTemplate;

    private List<String> keys;

    @PostConstruct
    public void init() {
        List<String> raw = properties.getApiKeys();
        if (raw == null || raw.isEmpty()) {
            throw new IllegalStateException(
                    "deepseek.api-keys is empty; please set DEEPSEEK_API_KEYS in env or application.yml");
        }
        List<String> filtered = new ArrayList<>();
        for (String k : raw) {
            if (k != null && !k.isBlank()) {
                filtered.add(k.trim());
            }
        }
        if (filtered.isEmpty()) {
            throw new IllegalStateException("deepseek.api-keys contains only blanks");
        }
        this.keys = Collections.unmodifiableList(filtered);
        log.info("ApiKeyPool initialized with {} keys", keys.size());
    }

    public String pickKey(Long userId) {
        int n = keys.size();
        int firstIdx = indexFor(userId);
        for (int i = 0; i < n; i++) {
            String candidate = keys.get((firstIdx + i) % n);
            if (!isCoolingDown(candidate)) {
                return candidate;
            }
        }
        return keys.get(firstIdx);
    }

    public void markUnhealthy(String key) {
        redisTemplate.opsForValue().set(
                COOLDOWN_KEY_PREFIX + key,
                "1",
                properties.getKeyCooldownSeconds().longValue(),
                TimeUnit.SECONDS);
        log.warn("ApiKey marked unhealthy: ...{}", maskKey(key));
    }

    public List<String> orderedFallback(Long userId) {
        int n = keys.size();
        int firstIdx = indexFor(userId);
        List<String> ordered = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            ordered.add(keys.get((firstIdx + i) % n));
        }
        return ordered;
    }

    private int indexFor(Long userId) {
        long uid = userId == null ? 0L : userId;
        return (int) Math.floorMod(uid, keys.size());
    }

    private boolean isCoolingDown(String key) {
        Boolean has = redisTemplate.hasKey(COOLDOWN_KEY_PREFIX + key);
        return Boolean.TRUE.equals(has);
    }

    private String maskKey(String key) {
        if (key == null || key.length() < 6) return "***";
        return key.substring(key.length() - 4);
    }
}
