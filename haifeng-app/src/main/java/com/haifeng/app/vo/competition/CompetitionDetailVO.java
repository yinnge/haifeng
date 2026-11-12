package com.haifeng.app.vo.competition;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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

    /** 雪花 ID：序列化为字符串，避免前端 JS Number 精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 雪花 ID：序列化为字符串，避免前端 JS Number 精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long competitionId;

    private String compName;
    private String compLevel;

    private List<Map<String, String>> basicInfo;
    private List<String> awards;
    private String background;
    private List<String> purposes;
    private List<Map<String, String>> competitionRules;
    private List<String> scoringCriteria;
    private List<String> notices;
    private List<Map<String, String>> processGuide;
    private List<Map<String, String>> awardsDisplay;
}
