package com.haifeng.app.util.algorithm.article;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 资讯标题关键字对齐匹配器（去噪查询）。
 * <p>以标题为参照对输入做贪心最长匹配切分：每段必须是出现在标题中的连续子串（长度≥2），
 * 找不到的单字视为噪声跳过；命中字符占比≥50% 即视为命中（AND 语义）。
 * 不依赖分词词典；高亮区间由命中段在标题中的首次出现位置得出。
 */
public final class TitleAlignMatcher {

    /** 命中段最小长度（低于此长度的单字匹配视为噪声，避免「的/是」等误命中） */
    private static final int MIN_SEGMENT = 2;
    /** 命中段最大长度（防御性上限） */
    private static final int MAX_SEGMENT = 20;
    /** 接受阈值：命中字符占比下限（%） */
    private static final int MIN_COVERAGE_PERCENT = 50;

    private TitleAlignMatcher() {
    }

    /** 标题上的高亮区间 [start, end)（UTF-16 下标，与 JS 字符串一致） */
    @Getter
    public static final class HighlightSpan {
        private final int start;
        private final int end;

        private HighlightSpan(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    /** 对齐结果 */
    @Getter
    public static final class AlignResult {
        private final List<HighlightSpan> spans;
        private final int matchedChars;
        private final int normalizedLength;

        private AlignResult(List<HighlightSpan> spans, int matchedChars, int normalizedLength) {
            this.spans = spans;
            this.matchedChars = matchedChars;
            this.normalizedLength = normalizedLength;
        }

        public boolean accepted() {
            return normalizedLength > 0
                    && !spans.isEmpty()
                    && matchedChars * 100 >= normalizedLength * MIN_COVERAGE_PERCENT;
        }
    }

    /**
     * 对齐匹配：返回命中段区间（按 start 升序；渲染端以游标推进兜底重叠）
     */
    public static AlignResult align(String rawKeyword, String title) {
        String keyword = normalize(rawKeyword);
        if (keyword.isEmpty() || title == null || title.isEmpty()) {
            return new AlignResult(List.of(), 0, keyword.length());
        }
        List<HighlightSpan> spans = new ArrayList<>();
        int matched = 0;
        int pos = 0;
        while (pos < keyword.length()) {
            int maxLen = Math.min(MAX_SEGMENT, keyword.length() - pos);
            int hitLen = 0;
            for (int len = maxLen; len >= MIN_SEGMENT; len--) {
                if (title.contains(keyword.substring(pos, pos + len))) {
                    hitLen = len;
                    break;
                }
            }
            if (hitLen > 0) {
                String segment = keyword.substring(pos, pos + hitLen);
                int start = title.indexOf(segment);
                spans.add(new HighlightSpan(start, start + hitLen));
                matched += hitLen;
                pos += hitLen;
            } else {
                // 最长命中 <2 字：该字符视为噪声跳过（疑问词尾部天然去噪）
                pos++;
            }
        }
        spans.sort(Comparator.comparingInt(HighlightSpan::getStart));
        return new AlignResult(spans, matched, keyword.length());
    }

    /** 去掉空白与中英文标点 */
    static String normalize(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (Character.isWhitespace(c) || isPunctuation(c)) {
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private static boolean isPunctuation(char c) {
        if (c < 128) {
            // ASCII 标点与符号：!-/ :-@ [-` {-~
            return (c >= 33 && c <= 47) || (c >= 58 && c <= 64)
                    || (c >= 91 && c <= 96) || (c >= 123 && c <= 126);
        }
        int type = Character.getType(c);
        return type == Character.CONNECTOR_PUNCTUATION
                || type == Character.DASH_PUNCTUATION
                || type == Character.START_PUNCTUATION
                || type == Character.END_PUNCTUATION
                || type == Character.INITIAL_QUOTE_PUNCTUATION
                || type == Character.FINAL_QUOTE_PUNCTUATION
                || type == Character.OTHER_PUNCTUATION;
    }
}
