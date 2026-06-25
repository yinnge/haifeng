package com.haifeng.common.service.ai;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.haifeng.common.entity.system.SystemSettings;
import com.haifeng.common.exception.QuotaExceededException;
import com.haifeng.common.mapper.system.SystemSettingsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AI 调用配额：
 * - INCR pdf:ai:quota:{userId}:{yyyyMMdd}（首次写入时设 TTL 到当日 23:59:59）
 * - 上限来源：system_settings.api_number（缓存 Redis 5 分钟，默认 3）
 * - 超额抛 QuotaExceededException（HTTP 429）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiQuotaService {

    private static final String QUOTA_KEY_PREFIX = "pdf:ai:quota:";
    private static final String API_NUMBER_CACHE_KEY = "sys:api_number";
    private static final long API_NUMBER_CACHE_TTL_MIN = 5L;
    private static final int DEFAULT_API_NUMBER = 3;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RedisTemplate<String, Object> redisTemplate;
    private final SystemSettingsMapper settingsMapper;

    public void incrAndCheck(Long userId) {
        int limit = getApiNumberLimit();
        String key = quotaKey(userId);
        Long count = redisTemplate.opsForValue().increment(key);
        long current = count == null ? 0L : count;

        if (current == 1L) {
            redisTemplate.expireAt(key, endOfTodayDate());
        }

        if (current > limit) {
            log.warn("AI quota exceeded for userId={}, current={}, limit={}", userId, current, limit);
            throw new QuotaExceededException();
        }
    }

    public int getApiNumberLimit() {
        Object cached = redisTemplate.opsForValue().get(API_NUMBER_CACHE_KEY);
        if (cached instanceof Number) {
            return ((Number) cached).intValue();
        }
        List<SystemSettings> rows = settingsMapper.selectList(Wrappers.<SystemSettings>lambdaQuery());
        int limit = DEFAULT_API_NUMBER;
        if (rows != null && !rows.isEmpty() && rows.get(0).getApiNumber() != null) {
            limit = rows.get(0).getApiNumber();
        }
        redisTemplate.opsForValue().set(API_NUMBER_CACHE_KEY, limit,
                API_NUMBER_CACHE_TTL_MIN, TimeUnit.MINUTES);
        return limit;
    }

    private String quotaKey(Long userId) {
        return QUOTA_KEY_PREFIX + userId + ":" + LocalDate.now().format(DATE_FMT);
    }

    private Date endOfTodayDate() {
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59));
        return Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }
}
