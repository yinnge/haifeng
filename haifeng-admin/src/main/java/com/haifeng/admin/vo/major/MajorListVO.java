package com.haifeng.admin.vo.major;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class MajorListVO {

    private Long id;

    private String majorCode;

    private String majorName;

    private String disciplineName;

    private String majorType;

    private String majorCategory;

    private Integer status;

    private OffsetDateTime createdAt;
}
