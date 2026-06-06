package com.haifeng.app.vo.competition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端竞赛列表 VO（spec 任务2接口1） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String compName;
    private String compLevel;
    private String registrationTime;
}
