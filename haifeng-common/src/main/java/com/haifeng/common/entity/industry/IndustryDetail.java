package com.haifeng.common.entity.industry;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_industry_detail", autoResultMap = true)
public class IndustryDetail {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long industryId;

    private String industryName;

    private String shortDescription;

    private String detailedDescription;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> industryScale;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> industryTalentDemand;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> industrySalary;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> policyInfo;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> developmentSupportInfo;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> talentAnalysis;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> talentPolicy;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> salaryData;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
