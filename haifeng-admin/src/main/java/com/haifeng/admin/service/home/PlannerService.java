package com.haifeng.admin.service.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.PlannerAddDTO;
import com.haifeng.admin.dto.home.PlannerQueryDTO;
import com.haifeng.admin.dto.home.PlannerUpdateDTO;
import com.haifeng.admin.vo.home.PlannerDetailVO;
import com.haifeng.admin.vo.home.PlannerListVO;

public interface PlannerService {

    IPage<PlannerListVO> page(PlannerQueryDTO dto);

    PlannerDetailVO detail(Long id);

    Long add(PlannerAddDTO dto);

    void update(Long id, PlannerUpdateDTO dto);

    /** 软删除规划师（置 is_deleted=true，非物理删除） */
    void delete(Long id);
}
