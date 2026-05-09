package com.haifeng.admin.vo.algorithm.admission;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class AdmissionGroupDetailVO {
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
    private String description;
    private String subjectRequirements;
    private Short requirementLevel;
    private List<String> constraints;
    private Integer majorCount;
    private Integer categoryCount;
    private Integer admissionCount;
    private Integer minScore;
    private Integer minRank;
    private BigDecimal avgScore;
    private Integer avgRank;
    private Integer maxScore;
    private Integer maxRank;
    private Boolean isDeleted;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
