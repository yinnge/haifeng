package com.haifeng.app.vo.algorithm.admission;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdmissionGroupPageVO {
    private Integer id;
    private Boolean masked;

    private String universityName;
    private String cityName;
    private String enrollmentCode;
    private String groupCode;
    private String groupName;
    private List<String> subjects;
    private String requirementType;
    private String description;
    private Integer majorCount;
    private Integer categoryCount;
    private List<String> constraints;

    private Boolean subjectMatch;
    private String subjectMatchReason;

    private List<YearScoreVO> historyScores;
}
