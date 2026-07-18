package com.haifeng.admin.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityDetailVO implements Serializable {

    // 基础信息（Tab1）
    private Long id;

    private String name;

    private String nameEn;

    private String provinceName;

    private String cityName;

    private String region;

    private String category;

    private Integer majorCount;

    private String educationLevel;

    private String nature;

    private BigDecimal recommendationRate;

    private Integer recommendationYear;

    private Boolean hasDoctorate;

    private Boolean hasMaster;

    private String department;

    private List<String> tags;

    private String famousUnion;

    private String imageUrl;

    private String introduction;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 详细介绍（Tab2）
    private Long detailId;

    private String address;

    private String admissionPhone;

    private String website;

    private Integer historyGroupScore;

    private Integer scienceGroupScore;

    private List<String> carouselImages;

    private String detailIntroduction;

    private RankingsVO rankings;

    private String abroadRate;

    private String genderRatio;
}
