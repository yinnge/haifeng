package com.haifeng.app.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.UniversityQueryDTO;
import com.haifeng.app.vo.university.UniversityListVO;

public interface UniversityService {

    /**
     * 分页查询院校列表（仅 status=1）
     * 多筛选条件 AND 组合；name 走 LIKE %name%
     * 排序：sort_order ASC, id DESC
     */
    IPage<UniversityListVO> page(UniversityQueryDTO dto);
}
