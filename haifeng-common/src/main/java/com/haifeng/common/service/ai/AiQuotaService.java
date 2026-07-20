package com.haifeng.common.service.ai;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.haifeng.common.entity.system.SystemSettings;
import com.haifeng.common.exception.QuotaExceededException;
import com.haifeng.common.mapper.system.SystemSettingsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
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
 * PDF 生成配额：
 * - INCR pdf:report:quota:{userId}:{yyyyMMdd}（首次写入时设 TTL 到当日 23:59:59）
 * - 上限来源：system_settings.api_number（缓存 Redis 5 分钟，默认 3）
 * - 含义：每天可生成 PDF 报告的次数（1 次 PDF = 1 额度，内部 N+1 次 AI 调用不另计）
 * - 超额抛 QuotaExceededException（HTTP 429）
 * - incrAndCheck 与 decr 均通过 Lua 脚本保证原子性，避免 TTL 竞态与负数回退
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiQuotaService {

    private static final String QUOTA_KEY_PREFIX = "pdf:report:quota:";
    private static final String API_NUMBER_CACHE_KEY = "sys:api_number";
    private static final long API_NUMBER_CACHE_TTL_MIN = 5L;
    private static final int DEFAULT_API_NUMBER = 3;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Lua 脚本：原子化 INCR + EXPIREAT + 上限校验
     * KEYS[1] = quota key
     * ARGV[1] = end-of-day epoch seconds (for EXPIREAT)
     * ARGV[2] = limit
     * 返回 1 表示允许，0 表示超额
     */
    private static final String INCR_LUA =
            "local c = redis.call('INCR', KEYS[1]) " +
            "if c == 1 then redis.call('EXPIREAT', KEYS[1], ARGV[1]) end " +
            "if c > tonumber(ARGV[2]) then return 0 end " +
            "return 1";

    /**
     * Lua 脚本：原子化 DECR（仅在值 > 0 时执行，避免负数）
     * KEYS[1] = quota key
     * 返回扣减后的值
     */
    private static final String DECR_LUA =
            "local c = tonumber(redis.call('GET', KEYS[1]) or '0') " +
            "if c > 0 then return redis.call('DECR', KEYS[1]) end " +
            "return 0";

    private final RedisTemplate<String, Object> redisTemplate;
    private final SystemSettingsMapper settingsMapper;

    private final DefaultRedisScript<Long> incrScript = new DefaultRedisScript<>(INCR_LUA, Long.class);
    private final DefaultRedisScript<Long> decrScript = new DefaultRedisScript<>(DECR_LUA, Long.class);

    public void incrAndCheck(Long userId) {
        int limit = getApiNumberLimit();
        String key = quotaKey(userId);
        Long allowed = redisTemplate.execute(
                incrScript,
                List.of(key),
                String.valueOf(endOfTodayEpochSeconds()),
                String.valueOf(limit));

        if (allowed == null || allowed == 0L) {
            log.warn("PDF report quota exceeded for userId={}, limit={}", userId, limit);
            throw new QuotaExceededException();
        }
    }

    /**
     * 配额回退：在 doGenerate 失败分支调用，避免用户损失当日额度。
     * 仅在当前计数 > 0 时执行 DECR，避免产生负数。
     */
    public void decr(Long userId) {
        try {
            String key = quotaKey(userId);
            Long after = redisTemplate.execute(decrScript, List.of(key));
            log.info("PDF report quota decremented for userId={}, after={}", userId, after);
        } catch (Exception e) {
            log.warn("Failed to decrement PDF quota for userId={}: {}", userId, e.getMessage());
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

    private long endOfTodayEpochSeconds() {
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59));
        return endOfDay.atZone(ZoneId.systemDefault()).toEpochSecond();
    }
}
