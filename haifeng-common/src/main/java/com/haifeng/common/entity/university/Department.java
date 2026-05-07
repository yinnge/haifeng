package com.haifeng.common.entity.university;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_department", autoResultMap = true)
public class Department {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long universityId;

    private String universityName;

    private String departmentName;

    private String departmentType;

    private String pageTitle;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    private Integer sortOrder;

    private Short status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
