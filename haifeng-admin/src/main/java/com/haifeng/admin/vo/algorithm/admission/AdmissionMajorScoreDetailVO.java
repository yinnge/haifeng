package com.haifeng.admin.vo.algorithm.admission;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class AdmissionMajorScoreDetailVO {
    private Integer id;
    private Integer groupId;
    private Long majorId;
    private String majorCode;
    private String majorName;
    private String educationLevel;
    private String duration;
    private String tuition;
    private String description;
    private Integer admissionCount;
    private Integer minScore;
    private Integer minRank;
    private BigDecimal avgScore;
    private Integer avgRank;
    private Integer maxScore;
    private Integer maxRank;
    private List<String> constraints;
    private Boolean isDeleted;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
