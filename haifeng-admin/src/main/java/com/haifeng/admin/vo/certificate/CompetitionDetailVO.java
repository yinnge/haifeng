package com.haifeng.admin.vo.certificate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionDetailVO {

    private Long id;

    private String compName;

    private String compLevel;

    private String registrationTime;

    // 详情字段
    private Long detailId;

    private Map<String, Object> basicInfo;

    private List<String> awards;

    private String background;

    private List<String> purposes;

    private List<Map<String, String>> competitionRules;

    private List<String> scoringCriteria;

    private List<String> notices;

    private List<Map<String, String>> processGuide;

    private List<Map<String, String>> awardsDisplay;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
