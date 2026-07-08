package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdmissionGroupListVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String groupCode;
    private String groupName;
    private Short year;
    private String province;
    private String batch;
    private String cityName;
    private List<String> subjects;
    private String requirementType;
    private Integer majorCount;
    private Integer admissionCount;
    private Integer minScore;
    private Integer minRank;
    private Integer maxScore;
    private Integer maxRank;
    private BigDecimal avgScore;
    private Integer avgRank;
}
