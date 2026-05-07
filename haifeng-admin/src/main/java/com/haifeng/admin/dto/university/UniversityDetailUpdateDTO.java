package com.haifeng.admin.dto.university;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UniversityDetailUpdateDTO {

    private String address;

    private String admissionPhone;

    private String website;

    private Integer historyGroupScore;

    private Integer scienceGroupScore;

    private List<String> carouselImages;

    private String introduction;

    private Map<String, Integer> rankings;

    private String abroadRate;

    private String genderRatio;

    private Integer sortOrder;

    private Integer status;
}
