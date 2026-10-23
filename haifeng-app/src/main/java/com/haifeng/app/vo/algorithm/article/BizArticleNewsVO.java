package com.haifeng.app.vo.algorithm.article;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 热点资讯 - C 端主页资讯列表 VO（轻量，仅主页展示所需）
 * <p>返回 资讯 id / 模块名(标签) / 标题 / 专栏名 / 年份 / 发布时间，按年份倒序（NULL 置后）→ 发布时间倒序。
 */
@Data
@Builder
public class BizArticleNewsVO {

    /** 资讯 id（跳转详情用） */
    private Long id;

    /** 所属模块（高考/中考/考公/考研/英语四六级，作彩色标签） */
    private String moduleName;

    /** 标题 */
    private String title;

    /** 专栏名 */
    private String columnName;

    /** 年份；NULL=不分年份 */
    private Integer year;

    /** 发布时间（前端格式化为 月-日，如 08-27） */
    private OffsetDateTime publishTime;
}
