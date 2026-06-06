package com.haifeng.app.vo.competition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端竞赛→专业简明 VO（spec 任务2接口3） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionMajorBriefVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long majorId;
    private String majorName;
}
