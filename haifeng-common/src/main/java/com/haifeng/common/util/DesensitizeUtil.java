package com.haifeng.common.util;

public class DesensitizeUtil {

    private DesensitizeUtil() {
    }

    /**
     * 微信号脱敏：保留前4位和后2位
     * 例如：wxid_abc123 -> wxid_***23
     *
     * @param wechatId 微信号
     * @return 脱敏后的微信号
     */
    public static String desensitizeWechat(String wechatId) {
        if (wechatId == null || wechatId.length() <= 6) {
            return wechatId;
        }
        int length = wechatId.length();
        String prefix = wechatId.substring(0, 4);
        String suffix = wechatId.substring(length - 2);
        return prefix + "***" + suffix;
    }

    /**
     * 手机号脱敏：保留前3位和后4位
     * 例如：13812345678 -> 138****5678
     *
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    public static String desensitizePhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 用户名脱敏：保留第一个字符，其余用*替换
     * 例如：张三 -> 张*，王小明 -> 王**
     *
     * @param name 用户名
     * @return 脱敏后的用户名
     */
    public static String desensitizeName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() == 1) {
            return name;
        }
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }
}
