package com.haifeng.common.entity.university;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.haifeng.common.config.StringListTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_university_guides", autoResultMap = true)
public class UniversityGuide {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long universityId;

    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> customTags;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> campusFacilities;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> dormitoryServices;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> campusTransportation;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> academicGuidance;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> majorTransferGuidelines;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> majorTransferConstriction;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> academicSupportResources;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> studentOrganizations;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> campusEvents;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> classDormSocial;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> financialAid;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> campusSecurity;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> healthServices;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> lifeServices;

    private String remark;

    private Short status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
