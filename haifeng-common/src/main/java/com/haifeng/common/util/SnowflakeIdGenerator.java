package com.haifeng.common.util;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

/**
 * 雪花算法ID生成器
 */
public class SnowflakeIdGenerator {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(1, 1);

    private SnowflakeIdGenerator() {
    }

    /**
     * 生成雪花ID
     */
    public static long nextId() {
        return SNOWFLAKE.nextId();
    }

    /**
     * 生成雪花ID（字符串）
     */
    public static String nextIdStr() {
        return SNOWFLAKE.nextIdStr();
    }
}
