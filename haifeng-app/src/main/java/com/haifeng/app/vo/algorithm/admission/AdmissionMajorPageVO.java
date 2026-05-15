package com.haifeng.app.vo.algorithm.admission;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdmissionMajorPageVO {
    private Integer id;
    private String majorCode;
    private String majorName;
    private String educationLevel;
    private String duration;
    private String tuition;
    private String description;
    private List<String> constraints;

    private List<YearScoreVO> historyScores;
}
