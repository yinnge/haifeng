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
public class AdmissionMajorScoreListVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer groupId;
    private String majorCode;
    private String majorName;
    private String educationLevel;
    private String duration;
    private String tuition;
    private String description;
    private Integer admissionCount;
    private Integer minScore;
    private Integer minRank;
    private Integer maxScore;
    private Integer maxRank;
    private BigDecimal avgScore;
    private Integer avgRank;
    private List<String> constraints;
}
