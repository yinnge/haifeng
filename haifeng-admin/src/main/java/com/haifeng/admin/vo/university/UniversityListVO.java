package com.haifeng.admin.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityListVO implements Serializable {

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
