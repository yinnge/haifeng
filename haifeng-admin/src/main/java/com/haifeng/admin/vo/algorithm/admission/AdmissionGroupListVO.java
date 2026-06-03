package com.haifeng.admin.vo.algorithm.admission;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AdmissionGroupListVO {
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
    private Integer majorCount;
    private Integer admissionCount;
    private Integer minScore;
    private Integer minRank;
    private BigDecimal avgScore;
    private Boolean isDeleted;
}
