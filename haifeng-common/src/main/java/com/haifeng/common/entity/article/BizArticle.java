package com.haifeng.common.entity.article;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 热点资讯（高考/考公/考研/中考/英语四六级五模块共用，见 V71）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_biz_article")
public class BizArticle {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属模块：高考/考公/考研/中考/英语四六级（直接存中文，DB CHECK 约束对齐） */
    private String moduleName;

    /** 省份裸值（无 省/市 后缀，如 山西）；全国=全国；NULL=不限省份 */
    private String province;

    /** 年份（如 2026）；NULL=不分年份 */
    private Integer year;

    /** 专栏名 */
    private String columnName;

    /** 标题 */
    private String title;

    /** 列表页摘要 */
    private String summary;

    /** 富文本 HTML 正文 */
    private String content;

    /** 状态：0已发布 1下架 */
    private Integer status;

    /** 排序权重（越大越靠前） */
    private Integer sortOrder;

    /** 浏览量 */
    private Long viewCount;

    /** 发布时间 */
    private OffsetDateTime publishTime;

    /** 创建人（sys_admin.id） */
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updateTime;
}
