package com.haifeng.admin.dto.certificate;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class CompetitionDetailDTO {
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
