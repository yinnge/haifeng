package com.haifeng.admin.vo.university;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class DepartmentListVO {
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
