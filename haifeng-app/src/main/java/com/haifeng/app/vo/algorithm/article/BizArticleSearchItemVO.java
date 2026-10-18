package com.haifeng.app.vo.algorithm.article;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 热点资讯 - C 端标题搜索建议 VO
 * <p>highlight 为标题上的命中区间（[start,end) 半开，UTF-16 下标与 JS 一致），
 * 前端按区间切段渲染橙色，匹配算法只在后端存在一份。
 */
@Data
@Builder
public class BizArticleSearchItemVO {

    /** 资讯 id（跳详情用；前端 json-bigint 解析为 string） */
    private Long id;

    /** 专栏名（建议列表 pill） */
    private String columnName;

    /** 省份裸值；null=不限省份文章 */
    private String province;

    /** 标题（含高亮区间定位的原文） */
    private String title;

    /** 命中高亮区间，按 start 升序 */
    private List<HighlightSpan> highlight;

    /** 标题上的高亮区间 [start, end) */
    @Data
    @Builder
    public static class HighlightSpan {
        private Integer start;
        private Integer end;
    }
}
