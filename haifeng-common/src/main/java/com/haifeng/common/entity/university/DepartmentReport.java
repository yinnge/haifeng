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
@TableName(value = "department_reports", autoResultMap = true)
public class DepartmentReport {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long departmentId;

    private String subtitle;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> overview;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> subjectsDetail;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> postgraduate;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> citySalary;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> salary;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> career;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> trends;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> prospects;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> disclaimer;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> majorCompose;

    private Integer sortOrder;

    private Short status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
