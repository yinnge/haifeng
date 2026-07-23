package com.haifeng.admin.vo.university;

import lombok.Data;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class DepartmentListVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private Long universityId;
    private String universityName;
    private String departmentName;
    private String departmentType;
    private String pageTitle;
    private Integer sortOrder;
    private Integer status;
    private OffsetDateTime createdAt;
}
