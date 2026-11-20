package com.haifeng.admin.util;

import com.haifeng.common.entity.university.University;

import java.util.function.Function;

/**
 * 院校名称匹配工具：兼容 Excel 里全角（）与半角()混用导致的精确匹配失败。
 *
 * <p>只负责「名称候选」的解析（原名 → 括号互换名），不负责缓存与落库。
 * 缓存仍由各调用方按其自身 key 维护，查询条件（status 过滤、select 列、
 * selectOne/selectList 语义）由调用方通过 {@code exactLookup} 回调决定。</p>
 */
public final class UniversityNameResolver {

    private UniversityNameResolver() {
    }

    /**
     * 先用原名称精确查询；查不到时把全角（）与半角()互换后再查一次。
     *
     * @param name        调用方已 trim / normalize 的院校名
     * @param exactLookup 精确查询回调，由调用方决定 status 过滤、select 列、返回单条或多条
     * @return 命中的院校；仍未命中返回 null
     */
    public static University resolve(String name, Function<String, University> exactLookup) {
        if (name == null) {
            return null;
        }
        University university = exactLookup.apply(name);
        if (university != null) {
            return university;
        }
        String swapped = swapParentheses(name);
        if (!swapped.equals(name)) {
            return exactLookup.apply(swapped);
        }
        return null;
    }

    /** 全角（）与半角()互相转换 */
    public static String swapParentheses(String name) {
        StringBuilder sb = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            switch (c) {
                case '(':
                    sb.append('（');
                    break;
                case ')':
                    sb.append('）');
                    break;
                case '（':
                    sb.append('(');
                    break;
                case '）':
                    sb.append(')');
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
