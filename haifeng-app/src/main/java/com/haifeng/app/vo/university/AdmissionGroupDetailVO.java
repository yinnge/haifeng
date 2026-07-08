package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdmissionGroupDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Long universityId;
    private String universityName;
    private String cityName;
    private Short year;
    private String province;
    private String batch;
    private String enrollmentCode;
    private String groupCode;
    private String groupName;
    private List<String> subjects;
    private String requirementType;
    private String description;
    private List<String> constraints;
    private Integer majorCount;
    private Integer categoryCount;
    private Integer admissionCount;
    private Integer minScore;
    private Integer minRank;
    private Integer maxScore;
    private Integer maxRank;
    private BigDecimal avgScore;
    private Integer avgRank;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
