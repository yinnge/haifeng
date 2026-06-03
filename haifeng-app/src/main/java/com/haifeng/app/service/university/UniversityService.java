package com.haifeng.app.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.UniversityQueryDTO;
import com.haifeng.app.vo.university.UniversityDetailVO;
import com.haifeng.app.vo.university.UniversityListVO;

public interface UniversityService {

    /**
     * 分页查询院校列表（仅 status=1）；多筛选 AND；name LIKE；排序 sort_order ASC, id DESC
     */
    IPage<UniversityListVO> page(UniversityQueryDTO dto);

    /**
     * 院校详情：联表查询 t_universities + t_universities_detail
     * 任一不存在或 status != 1 → BusinessException(NOT_FOUND)
     */
    UniversityDetailVO detail(Long universityId);
}
