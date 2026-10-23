package com.haifeng.app.vo.algorithm.article;

import lombok.Builder;
import lombok.Data;

/**
 * 热点资讯 - C 端全局列表 VO（跨模块统一入口）
 * <p>在高考列表基础上补充 moduleName / province / year / sortOrder，供前端按模块、省份筛选与排序展示。
 */
@Data
@Builder
public class BizArticleGlobalListVO {

    /** 资讯 id（跳转详情用） */
    private Long id;

    /** 所属模块（高考/中考/考公/考研/英语四六级） */
    private String moduleName;

    /** 省份裸值（如 山西；全国）；null=不限省份 */
    private String province;

    /** 专栏名 */
    private String columnName;

    /** 标题 */
    private String title;

    /** 年份；NULL=不分年份 */
    private Integer year;

    /** 排序权重（越大越靠前） */
    private Integer sortOrder;
}
