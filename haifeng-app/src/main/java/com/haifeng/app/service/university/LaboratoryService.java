package com.haifeng.app.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.LaboratoryQueryDTO;
import com.haifeng.app.vo.university.LaboratoryDetailVO;
import com.haifeng.app.vo.university.LaboratoryListVO;

public interface LaboratoryService {

    /**
     * 按 universityId 分页查询实验室（仅 status=1）
     * 排序 sort_order ASC, id DESC
     * universityId 不存在时返回空分页（不报错）
     */
    IPage<LaboratoryListVO> page(Long universityId, LaboratoryQueryDTO dto);

    /**
     * 按主键查询实验室详情（仅 status=1）
     * 不存在或已下架时抛 BusinessException(404, "实验室不存在")
     */
    LaboratoryDetailVO detail(Long labId);
}
