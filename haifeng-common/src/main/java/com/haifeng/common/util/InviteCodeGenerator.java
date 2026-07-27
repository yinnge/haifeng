package com.haifeng.common.util;

import org.hashids.Hashids;

/**
 * 邀请码生成器
 * 基于雪花ID + Hashids，保证唯一且双向可逆
 */
public class InviteCodeGenerator {

    // 盐值 + 最小长度8位 + 自定义字符集（去除易混淆字符 0OI1）
    private static final Hashids HASHIDS = new Hashids(
            "haifeng-invite-salt",
            8,
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    );

    private InviteCodeGenerator() {
    }

    /**
     * 根据雪花ID生成邀请码
     * 雪花ID为64位，超出Hashids上限(2^53)，拆分为高11位+低53位分别编码
     *
     * @param snowflakeId 雪花ID
     * @return 8位邀请码
     */
    public static String generate(long snowflakeId) {
        long upper = snowflakeId >>> 53;
        long lower = snowflakeId & 0x1FFFFFFFFFFFFFL;
        return HASHIDS.encode(upper, lower);
    }

    /**
     * 从邀请码解析出雪花ID
     *
     * @param inviteCode 邀请码
     * @return 雪花ID，解析失败返回 -1
     */
    public static long decode(String inviteCode) {
        if (inviteCode == null || inviteCode.isEmpty()) {
            return -1;
        }
        long[] ids = HASHIDS.decode(inviteCode);
        if (ids.length < 2) {
            return -1;
        }
        return (ids[0] << 53) | ids[1];
    }
}
