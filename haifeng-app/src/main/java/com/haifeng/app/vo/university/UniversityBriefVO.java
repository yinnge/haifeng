package com.haifeng.app.vo.university;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class UniversityBriefVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
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
