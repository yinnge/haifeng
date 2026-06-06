package com.haifeng.app.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.DepartmentQueryDTO;
import com.haifeng.app.vo.university.DepartmentListVO;
import com.haifeng.app.vo.university.DepartmentReportVO;

public interface DepartmentService {

    /**
     * 按 universityId 分页查询院系（仅 status=1）
     * 排序 sort_order ASC, id DESC
     */
    IPage<DepartmentListVO> page(Long universityId, DepartmentQueryDTO dto);

    /**
     * 按院系 id 查询其分析报告
     * 报告不存在时抛 BusinessException(404, "院系分析报告不存在")
     */
    DepartmentReportVO report(Long departmentId);
}
