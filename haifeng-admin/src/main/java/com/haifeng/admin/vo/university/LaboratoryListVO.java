package com.haifeng.admin.vo.university;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class LaboratoryListVO {
    private Long id;
    private Long universityId;
    private String universityName;
    private String name;
    private String labType;
    private String region;
    private String department;
    private String director;
    private Integer status;
    private OffsetDateTime createdAt;
}
