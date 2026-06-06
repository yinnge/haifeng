package com.haifeng.app.vo.major;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端专业→竞赛简明 VO（spec 任务3） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionBriefVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long competitionId;
    private String competitionName;
}
