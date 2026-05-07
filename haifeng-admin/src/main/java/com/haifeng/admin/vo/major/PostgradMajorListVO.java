package com.haifeng.admin.vo.major;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class PostgradMajorListVO {

    private Long id;

    private String majorName;

    private String majorCode;

    private String degreeType;

    private String disciplineCategory;

    private Integer popularity;

    private Integer difficulty;

    private Integer status;

    private OffsetDateTime createdAt;
}
