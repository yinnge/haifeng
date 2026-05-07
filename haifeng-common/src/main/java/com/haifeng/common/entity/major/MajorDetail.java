package com.haifeng.common.entity.major;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_major_detail", autoResultMap = true)
public class MajorDetail {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long majorId;

    private Integer courseCount;

    private String graduateScale;

    private BigDecimal maleRatio;

    private BigDecimal femaleRatio;

    private String majorDescription;

    private String trainingObjective;

    private String trainingRequirement;

    private String subjectRequirement;

    private String careerProspect;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String[] mainCourses;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String[] knowledgeSkills;

    private Short status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
