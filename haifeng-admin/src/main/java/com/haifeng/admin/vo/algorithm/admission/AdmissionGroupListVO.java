package com.haifeng.admin.vo.algorithm.admission;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AdmissionGroupListVO {
    private Integer id;
    private Long universityId;
    private String universityName;
    private Short year;
    private String province;
    private String subjectType;
    private String batch;
    private String enrollmentCode;
    private String groupCode;
    private String groupName;
    private String subjectRequirements;
    private Integer majorCount;
    private Integer admissionCount;
    private Integer minScore;
    private Integer minRank;
    private BigDecimal avgScore;
    private Boolean isDeleted;
}
