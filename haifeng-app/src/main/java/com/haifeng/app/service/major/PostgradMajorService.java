package com.haifeng.app.service.major;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.major.PostgradMajorListQueryDTO;
import com.haifeng.app.dto.major.PostgradMajorUniversityQueryDTO;
import com.haifeng.app.vo.major.PostgradMajorDetailVO;
import com.haifeng.app.vo.major.PostgradMajorListVO;
import com.haifeng.app.vo.major.UniversityBriefForPostgradVO;
import com.haifeng.app.vo.major.UndergraduateMajorDirectionBriefVO;
import com.haifeng.common.dto.common.BasePageQueryDTO;

public interface PostgradMajorService {

    /** 任务2接口1：考研专业列表（登录） */
    IPage<PostgradMajorListVO> page(PostgradMajorListQueryDTO dto);

    /** 任务2接口2：考研专业详情（登录） */
    PostgradMajorDetailVO detail(Long majorId);

    /** 任务4接口1：考研专业 → 大学列表（Pro） */
    IPage<UniversityBriefForPostgradVO> universities(Long majorId, PostgradMajorUniversityQueryDTO dto);

    /** 任务1接口2（关联查询）：考研方向 → 本科专业列表（Pro） */
    IPage<UndergraduateMajorDirectionBriefVO> undergraduateMajors(Long postgradMajorId, BasePageQueryDTO dto);
}
