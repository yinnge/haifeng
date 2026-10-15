package com.haifeng.app.vo.algorithm.article;

import lombok.Builder;
import lombok.Data;

/**
 * 热点资讯 - C 端列表 VO
 * <p>返回该模块全部已发布资讯（不做专栏过滤），前端根据 columnName 自行归类排版。
 */
@Data
@Builder
public class BizArticleListVO {

    /** 资讯 id（跳转详情用） */
    private Long id;

    /** 专栏名（前端据此分组归类） */
    private String columnName;

    /** 标题 */
    private String title;

    /** 列表页摘要 */
    private String summary;
}
