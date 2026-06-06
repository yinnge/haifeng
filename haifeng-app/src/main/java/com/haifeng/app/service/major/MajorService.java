package com.haifeng.app.service.major;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.major.MajorListQueryDTO;
import com.haifeng.app.dto.major.MajorRankingQueryDTO;
import com.haifeng.app.vo.major.MajorCategoryStatVO;
import com.haifeng.app.vo.major.MajorDetailVO;
import com.haifeng.app.vo.major.MajorListVO;
import com.haifeng.app.vo.major.PostgradMajorDirectionBriefVO;
import com.haifeng.app.vo.major.CompetitionBriefVO;
import com.haifeng.common.dto.common.BasePageQueryDTO;

import java.util.List;

public interface MajorService {

    /** 任务1接口1：专业列表（公开） */
    IPage<MajorListVO> page(MajorListQueryDTO dto);

    /** 任务1接口2：专业详情（登录） */
    MajorDetailVO detail(Long majorId);

    /** 任务1接口3：按 major_category 分组统计（公开） */
    List<MajorCategoryStatVO> categoryStats();

    /** 任务1接口4：薪资/就业排行（Pro） */
    IPage<MajorListVO> ranking(MajorRankingQueryDTO dto);

    /** 任务1接口1（关联查询）：本科专业 → 考研方向列表（Pro） */
    IPage<PostgradMajorDirectionBriefVO> postgradDirections(Long majorId, BasePageQueryDTO dto);

    /** 任务3接口1：专业 → 关联竞赛列表（Pro 及以上） */
    IPage<CompetitionBriefVO> competitions(Long majorId, BasePageQueryDTO dto);
}
