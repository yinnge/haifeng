package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * C 端院校详情 VO（任务 2，联表 t_universities + t_universities_detail）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ===== 来自 t_universities_detail =====
    private String address;
    private String admissionPhone;
    private String website;
    private Integer historyGroupScore;
    private Integer scienceGroupScore;
    private List<String> carouselImages;
    /** 详情表的 introduction（更完整） */
    private String introduction;
    private Map<String, Integer> rankings;
    private String abroadRate;
    private String genderRatio;

    // ===== 来自 t_universities =====
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
}
