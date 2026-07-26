package com.haifeng.common.entity.university;

import com.baomidou.mybatisplus.annotation.*;
import com.haifeng.common.config.JsonbTypeHandler;
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

    @Version
    private Integer version;

    private Long departmentId;

    private String subtitle;

    @TableField(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> overview;

    @TableField(typeHandler = JsonbTypeHandler.class)
    private List<Map<String, Object>> subjectsDetail;

    @TableField(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> postgraduate;

    @TableField(typeHandler = JsonbTypeHandler.class)
    private List<Map<String, Object>> citySalary;

    @TableField(typeHandler = JsonbTypeHandler.class)
    private List<Map<String, Object>> salary;

    @TableField(typeHandler = JsonbTypeHandler.class)
    private List<Map<String, Object>> career;

    @TableField(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> trends;

    @TableField(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> prospects;

    @TableField(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> disclaimer;

    @TableField(typeHandler = JsonbTypeHandler.class)
    private List<Map<String, Object>> majorCompose;

    private Integer sortOrder;

    private Short status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
