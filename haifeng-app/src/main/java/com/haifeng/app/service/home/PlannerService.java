package com.haifeng.app.service.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.home.PlannerQueryDTO;
import com.haifeng.app.vo.home.PlannerDetailVO;
import com.haifeng.app.vo.home.PlannerListVO;

public interface PlannerService {

    /** 分页查询展示中的规划师（status=1），按 sort_order ASC, id DESC 排序 */
    IPage<PlannerListVO> page(PlannerQueryDTO dto);

    /** 查询规划师详情（仅 status=1，不存在抛 404） */
    PlannerDetailVO detail(Long id);
}
