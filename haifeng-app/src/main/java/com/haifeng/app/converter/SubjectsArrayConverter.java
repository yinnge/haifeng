package com.haifeng.app.converter;

import java.util.List;

/**
 * 将 List<String> 安全转换为 PostgreSQL 数组字面量。
 * 对含特殊字符（逗号、大括号、空格、双引号、反斜杠）的元素用双引号包裹并转义。
 */
public final class SubjectsArrayConverter {

    private SubjectsArrayConverter() {
    }

    /**
     * 转换为 PostgreSQL text[] 字面量格式，如 {物理,化学} 或 {"含,逗号",生物}
     *
     * @param subjects 科目列表
     * @return PostgreSQL 数组字面量，若列表为空则返回 null
     */
    public static String toPgArrayLiteral(List<String> subjects) {
        if (subjects == null || subjects.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < subjects.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escapeElement(subjects.get(i)));
        }
        sb.append('}');
        return sb.toString();
    }

    /**
     * 转义单个数组元素。
     * 含逗号、大括号、空格、双引号、反斜杠的元素需用双引号包裹，
     * 内部的双引号转义为 \"，反斜杠转义为 \\。
     */
    private static String escapeElement(String element) {
        if (element == null) {
            return "NULL";
        }

        boolean needsQuoting = element.isEmpty()
                || element.chars().anyMatch(c -> c == ',' || c == '{' || c == '}' || c == '"' || c == '\\' || Character.isWhitespace(c));

        if (!needsQuoting) {
            return element;
        }

        // 用双引号包裹，转义内部双引号和反斜杠
        String escaped = element.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }
}
