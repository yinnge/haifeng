package com.haifeng.common.service.ai;

import com.haifeng.common.config.DeepSeekProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ApiKeyPoolTest {

    private DeepSeekProperties properties;
    @SuppressWarnings("unchecked")
    private RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
    @SuppressWarnings("unchecked")
    private ValueOperations<String, Object> valueOps = mock(ValueOperations.class);

    @BeforeEach
    void setup() {
        properties = new DeepSeekProperties();
        properties.setKeyCooldownSeconds(300);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    private ApiKeyPool buildPool(List<String> keys) {
        properties.setApiKeys(keys);
        ApiKeyPool pool = new ApiKeyPool(properties, redisTemplate);
        pool.init();
        return pool;
    }

    @Test
    void emptyKeys_failsFastOnInit() {
        properties.setApiKeys(Collections.emptyList());
        ApiKeyPool pool = new ApiKeyPool(properties, redisTemplate);
        assertThatThrownBy(pool::init).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sameUserId_returnsSameKey() {
        ApiKeyPool pool = buildPool(Arrays.asList("k1", "k2", "k3"));
        when(redisTemplate.hasKey(ArgumentMatchers.anyString())).thenReturn(false);

        String first = pool.pickKey(123L);
        String second = pool.pickKey(123L);
        assertThat(first).isEqualTo(second);
    }

    @Test
    void differentUserIds_distributeAcrossKeys() {
        ApiKeyPool pool = buildPool(Arrays.asList("k1", "k2", "k3"));
        when(redisTemplate.hasKey(ArgumentMatchers.anyString())).thenReturn(false);

        Set<String> chosen = new HashSet<>();
        for (long uid = 0; uid < 100; uid++) {
            chosen.add(pool.pickKey(uid));
        }
        assertThat(chosen.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void markUnhealthy_writesCooldownToRedis() {
        ApiKeyPool pool = buildPool(Arrays.asList("k1", "k2"));
        pool.markUnhealthy("k1");
        verify(valueOps).set(eq("ai:key:cooldown:k1"), eq("1"), eq(300L), eq(TimeUnit.SECONDS));
    }

    @Test
    void pickKey_skipsCooldownKey() {
        ApiKeyPool pool = buildPool(Arrays.asList("k1", "k2"));
        when(redisTemplate.hasKey("ai:key:cooldown:k1")).thenReturn(true);
        when(redisTemplate.hasKey("ai:key:cooldown:k2")).thenReturn(false);

        for (long uid = 0; uid < 50; uid++) {
            String picked = pool.pickKey(uid);
            assertThat(picked).isEqualTo("k2");
        }
    }

    @Test
    void allKeysCooldown_stillReturnsFirstChoice() {
        ApiKeyPool pool = buildPool(Arrays.asList("k1", "k2"));
        when(redisTemplate.hasKey(ArgumentMatchers.anyString())).thenReturn(true);

        String picked = pool.pickKey(123L);
        assertThat(picked).isIn("k1", "k2");
    }

    @Test
    void orderedFallback_returnsAllKeysWithFirstChoiceFirst() {
        ApiKeyPool pool = buildPool(Arrays.asList("k1", "k2", "k3"));
        when(redisTemplate.hasKey(ArgumentMatchers.anyString())).thenReturn(false);

        List<String> ordered = pool.orderedFallback(123L);
        assertThat(ordered).hasSize(3);
        assertThat(ordered).containsExactlyInAnyOrder("k1", "k2", "k3");
        assertThat(ordered.get(0)).isEqualTo(pool.pickKey(123L));
    }
}
