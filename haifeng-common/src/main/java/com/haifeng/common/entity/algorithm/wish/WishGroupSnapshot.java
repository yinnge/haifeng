package com.haifeng.common.entity.algorithm.wish;

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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 志愿方案-专业组快照表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_wish_group_snapshot", autoResultMap = true)
public class WishGroupSnapshot {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer planId;

    private Integer groupId;

    private Integer groupSortOrder;

    private Long universityId;

    private String category;

    private Integer majorCount;

    private String nature;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    private String universityName;

    private String cityName;

    private Short year;

    private String province;

    private String batch;

    private String enrollmentCode;

    private String groupCode;

    private String groupName;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> subjects;

    private String description;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> constraintsDescription;

    private Integer recommendationYear;

    private BigDecimal recommendationRate;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
