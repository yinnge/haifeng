package com.haifeng.admin.vo.major;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class PostgradMajorUniversityListVO {

    private Long id;

    private String universityName;

    private String postgradMajorName;

    private Integer sortOrder;

    private Integer status;

    private OffsetDateTime createdAt;
}
