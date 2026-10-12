package com.haifeng.app.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.LaboratoryQueryDTO;
import com.haifeng.app.vo.university.LaboratoryDetailVO;
import com.haifeng.app.vo.university.LaboratoryListVO;

import java.util.List;

public interface LaboratoryService {

    /**
     * 按 universityId 分页查询实验室（仅 status=1）
     * dto.labType 非空时按类型精确筛选
     * 排序 sort_order ASC, id DESC
     * universityId 不存在时返回空分页（不报错）
     */
    IPage<LaboratoryListVO> page(Long universityId, LaboratoryQueryDTO dto);

    /**
     * 查询该院校下实验室类型清单（去重、仅 status=1），用于前端筛选按钮
     * universityId 不存在或无数据时返回空列表
     */
    List<String> types(Long universityId);

    /**
     * 按主键查询实验室详情（仅 status=1）
     * 不存在或已下架时抛 BusinessException(404, "实验室不存在")
     */
    LaboratoryDetailVO detail(Long labId);
}
