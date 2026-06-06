package com.haifeng.app.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.UniversityPostgradMajorQueryDTO;
import com.haifeng.app.vo.major.PostgradMajorBriefVO;

public interface UniversityPostgradMajorService {

    /** 任务3接口1：大学 → 考研专业列表（Pro） */
    IPage<PostgradMajorBriefVO> page(Long universityId, UniversityPostgradMajorQueryDTO dto);
}
