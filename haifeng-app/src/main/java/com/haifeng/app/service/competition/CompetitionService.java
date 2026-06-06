package com.haifeng.app.service.competition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.vo.competition.CompetitionDetailVO;
import com.haifeng.app.vo.competition.CompetitionListVO;
import com.haifeng.app.vo.competition.CompetitionMajorBriefVO;
import com.haifeng.common.dto.common.BasePageQueryDTO;

public interface CompetitionService {

    /** 任务2接口1：竞赛分页列表（公开） */
    IPage<CompetitionListVO> page(BasePageQueryDTO dto);

    /** 任务2接口2：竞赛详情（登录） */
    CompetitionDetailVO detail(Long compId);

    /** 任务2接口3：分页查询某竞赛关联的专业（Pro） */
    IPage<CompetitionMajorBriefVO> majors(Long compId, BasePageQueryDTO dto);
}
