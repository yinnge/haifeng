package com.haifeng.admin.vo.university;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UniversityListVO {

    private Long id;

    private String name;

    private String provinceName;

    private String cityName;

    private String region;

    private String category;

    private Integer majorCount;

    private String educationLevel;

    private String nature;

    private Integer status;

    private LocalDateTime createdAt;
}
