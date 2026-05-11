package com.haifeng.admin.service.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.*;
import com.haifeng.admin.vo.home.*;

public interface PlannerService {
    IPage<PlannerListVO> page(PlannerQueryDTO dto);
    PlannerDetailVO detail(Long id);
    Long add(PlannerAddDTO dto);
    void update(Long id, PlannerUpdateDTO dto);
    void updateStatus(Long id, StatusDTO dto);
    /**
     * 硬删除规划师
     */
    void delete(Long id);
}
