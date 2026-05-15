package com.haifeng.app.vo.university;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class UniversityBriefVO {
    private String name;
    private String provinceName;
    private String cityName;
    private String region;
    private String category;
    private String educationLevel;
    private String nature;
    private BigDecimal recommendationRate;
    private String department;
    private List<String> tags;
    private String imageUrl;
}
