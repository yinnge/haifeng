package com.haifeng.app.vo.competition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/** C 端竞赛详情 VO（spec 任务2接口2） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long competitionId;

    private Map<String, Object> basicInfo;
    private List<String> awards;
    private String background;
    private List<String> purposes;
    private List<Map<String, String>> competitionRules;
    private List<String> scoringCriteria;
    private List<String> notices;
    private List<Map<String, String>> processGuide;
    private List<Map<String, String>> awardsDisplay;
}
