package com.haifeng.app.vo.algorithm.admission;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class AdmissionGroupPageVO {
    private Integer id;
    private Boolean masked;

    /** 安全系数 0.00~1.00 */
    private BigDecimal safetyLevel;

    /** 等级简称：搏/冲/稳/保/垫/禁 */
    private String levelShort;

    /** 说明（约束原因或数据不足提示） */
    private String safetyDescription;

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
