package com.haifeng.app.vo.algorithm.article;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 热点资讯 - C 端详情 VO
 */
@Data
@Builder
public class BizArticleDetailVO {

    /** 省份裸值（如 山西；全国） */
    private String province;

    /** 年份；NULL=不分年份 */
    private Integer year;

    /** 专栏名 */
    private String columnName;

    /** 标题 */
    private String title;

    /** 列表页摘要 */
    private String summary;

    /** 富文本 HTML 正文 */
    private String content;

    /** 浏览量 */
    private Long viewCount;

    /** 发布时间 */
    private OffsetDateTime publishTime;
}
