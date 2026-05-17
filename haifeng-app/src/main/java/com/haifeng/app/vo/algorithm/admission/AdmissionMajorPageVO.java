package com.haifeng.app.vo.algorithm.admission;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class AdmissionMajorPageVO {
    private Integer id;

    /** 安全系数 0.00~1.00 */
    private BigDecimal safetyLevel;

    /** 等级简称：搏/冲/稳/保/垫/禁 */
    private String levelShort;

    /** 说明（约束原因或数据不足提示） */
    private String safetyDescription;

    private String majorCode;
    private String majorName;
    private String educationLevel;
    private String duration;
    private String tuition;
    private String description;
    private List<String> constraints;

    private List<YearScoreVO> historyScores;
}
