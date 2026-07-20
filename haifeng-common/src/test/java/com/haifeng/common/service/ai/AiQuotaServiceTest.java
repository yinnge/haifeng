package com.haifeng.common.service.ai;

import com.haifeng.common.entity.system.SystemSettings;
import com.haifeng.common.exception.QuotaExceededException;
import com.haifeng.common.mapper.system.SystemSettingsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AiQuotaServiceTest {

    @SuppressWarnings("unchecked")
    private final RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
    private final SystemSettingsMapper settingsMapper = mock(SystemSettingsMapper.class);

    private AiQuotaService service;

    @BeforeEach
    void setup() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        service = new AiQuotaService(redisTemplate, settingsMapper);
    }

    @Test
    void underLimit_passes() {
        when(valueOps.get("sys:api_number")).thenReturn(5);
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class)))
                .thenReturn(1L);

        service.incrAndCheck(42L);
    }

    @Test
    void overLimit_throwsQuotaExceeded() {
        when(valueOps.get("sys:api_number")).thenReturn(5);
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class)))
                .thenReturn(0L);

        assertThatThrownBy(() -> service.incrAndCheck(42L))
                .isInstanceOf(QuotaExceededException.class);
    }

    @Test
    void executeReturnsNull_throwsQuotaExceeded() {
        when(valueOps.get("sys:api_number")).thenReturn(5);
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class)))
                .thenReturn(null);

        assertThatThrownBy(() -> service.incrAndCheck(42L))
                .isInstanceOf(QuotaExceededException.class);
    }

    @Test
    void decr_callsExecuteWithDecrScript() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class)))
                .thenReturn(2L);

        service.decr(42L);

        verify(redisTemplate).execute(any(RedisScript.class), anyList(), any(Object[].class));
    }

    @Test
    void decr_failure_doesNotPropagateException() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class)))
                .thenThrow(new RuntimeException("redis down"));

        // 不应抛异常，避免影响失败流程
        service.decr(42L);
    }

    @Test
    void apiNumberLimit_cachedInRedis_skipsDb() {
        when(valueOps.get("sys:api_number")).thenReturn(7);

        int limit = service.getApiNumberLimit();

        assertThat(limit).isEqualTo(7);
        verifyNoInteractions(settingsMapper);
    }

    @Test
    void apiNumberLimit_cacheMiss_loadsFromDbAndCaches() {
        when(valueOps.get("sys:api_number")).thenReturn(null);
        when(settingsMapper.selectList(any())).thenReturn(Arrays.asList(
                SystemSettings.builder().apiNumber(9).build()));

        int limit = service.getApiNumberLimit();

        assertThat(limit).isEqualTo(9);
        verify(valueOps).set(eq("sys:api_number"), eq(9), eq(5L), eq(java.util.concurrent.TimeUnit.MINUTES));
    }

    @Test
    void apiNumberLimit_dbEmpty_fallbacksToDefault() {
        when(valueOps.get("sys:api_number")).thenReturn(null);
        when(settingsMapper.selectList(any())).thenReturn(Collections.emptyList());

        int limit = service.getApiNumberLimit();

        assertThat(limit).isEqualTo(3);
    }
}
