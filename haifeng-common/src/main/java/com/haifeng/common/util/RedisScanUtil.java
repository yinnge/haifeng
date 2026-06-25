package com.haifeng.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis SCAN 工具
 * <p>
 * 避免在生产环境使用 KEYS 命令造成的阻塞,使用 SCAN 替代
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisScanUtil {

    private static final int SCAN_BATCH_SIZE = 500;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 按 pattern 扫描匹配的所有 key
     *
     * @param pattern SCAN MATCH 模式
     * @return 匹配的 key 列表
     */
    public List<String> scanKeys(String pattern) {
        List<String> keys = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(SCAN_BATCH_SIZE)
                .build();

        try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        } catch (Exception e) {
            log.warn("Redis SCAN 失败: pattern={}", pattern, e);
        }
        return keys;
    }

    /**
     * 按 pattern 扫描并删除所有匹配 key
     *
     * @param pattern SCAN MATCH 模式
     * @return 删除的 key 数量
     */
    public long scanAndDelete(String pattern) {
        List<String> keys = scanKeys(pattern);
        if (keys.isEmpty()) {
            return 0;
        }
        try {
            Long deleted = stringRedisTemplate.delete(keys);
            if (deleted != null && deleted > 0) {
                log.debug("Redis SCAN 删除: pattern={}, deleted={}", pattern, deleted);
            }
            return deleted == null ? 0 : deleted;
        } catch (Exception e) {
            log.warn("Redis 删除失败: pattern={}, keyCount={}", pattern, keys.size(), e);
            return 0;
        }
    }
}
