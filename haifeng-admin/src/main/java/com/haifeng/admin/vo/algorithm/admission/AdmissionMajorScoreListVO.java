package com.haifeng.admin.vo.algorithm.admission;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AdmissionMajorScoreListVO {
    private Integer id;
    private Integer groupId;
    private String majorCode;
    private String majorName;
    private String educationLevel;
    private Integer admissionCount;
    private Integer minScore;
    private Integer minRank;
    private BigDecimal avgScore;
    private Boolean isDeleted;
}
