package com.haifeng.admin.service.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.ConsultationQueryDTO;
import com.haifeng.admin.vo.home.ConsultationDetailVO;
import com.haifeng.admin.vo.home.ConsultationListVO;

public interface ConsultationService {

    /**
     * Admin 端分页查询咨询留言（只读）。
     */
    IPage<ConsultationListVO> page(ConsultationQueryDTO dto);

    /**
     * Admin 端留言详情（只读）。
     */
    ConsultationDetailVO detail(Long id);
}
