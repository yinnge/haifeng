package com.haifeng.admin.vo.fileload;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 热点资讯 - 列表 VO（不含 content 正文，详情接口返回）
 */
@Data
@Builder
public class BizArticleListVO {

    private Long id;

    /** 所属模块：高考/考公/考研/中考/英语四六级 */
    private String moduleName;

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

    /** 状态：0已发布 1下架 */
    private Integer status;

    /** 排序权重 */
    private Integer sortOrder;

    /** 浏览量 */
    private Long viewCount;

    /** 发布时间 */
    private OffsetDateTime publishTime;

    private OffsetDateTime createTime;

    private OffsetDateTime updateTime;
}
