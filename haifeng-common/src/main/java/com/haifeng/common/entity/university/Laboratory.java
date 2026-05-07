package com.haifeng.common.entity.university;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
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
@TableName(value = "laboratories", autoResultMap = true)
public class Laboratory {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long universityId;

    private String universityName;

    private String name;

    private String labType;

    private String establishedYear;

    private String region;

    private String department;

    private String director;

    private String staffCount;

    private String studentCount;

    private String email;

    private String phone;

    private String introduction;

    private String researchDescription;

    private String labSpace;

    private String openTopics;

    private String cooperation;

    private String visitingScholars;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> researchFields;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> statistics;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> majorEquipment;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> coreTeam;

    private Integer sortOrder;

    private Short status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
