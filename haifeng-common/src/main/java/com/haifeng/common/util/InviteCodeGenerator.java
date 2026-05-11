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
     *
     * @param snowflakeId 雪花ID
     * @return 8位邀请码
     */
    public static String generate(long snowflakeId) {
        return HASHIDS.encode(snowflakeId);
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
        return ids.length > 0 ? ids[0] : -1;
    }
}
