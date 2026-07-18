package com.haifeng.common.entity.employment.contentManagement;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_notice", autoResultMap = true)
public class Notice implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private Long id;

    private String noticeCategory;

    private String noticeType;

    private String title;

    private String summary;

    private String content;

    private String province;

    private String city;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String[] tags;

    private Integer year;

    private String source;

    private String sourceUrl;

    private OffsetDateTime publishDate;

    private String publishUnit;

    private OffsetDateTime regStartDate;

    private OffsetDateTime regEndDate;

    private OffsetDateTime examTime;

    private Integer recruitmentCount;

    private Boolean isTop;

    private Boolean isImportant;

    private Integer sortOrder;

    private Integer viewCount;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
