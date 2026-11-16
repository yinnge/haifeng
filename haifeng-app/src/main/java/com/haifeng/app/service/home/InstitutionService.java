package com.haifeng.app.service.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.home.InstitutionQueryDTO;
import com.haifeng.app.vo.home.InstitutionAppointmentVO;
import com.haifeng.app.vo.home.InstitutionDetailVO;
import com.haifeng.app.vo.home.InstitutionListVO;

public interface InstitutionService {

    /** 分页查询展示中的培训机构（status=1），支持按名称/分类/省份筛选，排序 sort_order ASC, id DESC */
    IPage<InstitutionListVO> page(InstitutionQueryDTO dto);

    /** 查询培训机构详情（仅 status=1，不存在抛 404） */
    InstitutionDetailVO detail(Long id);

    /** 查询培训机构预约信息（机构名 + 联系方式，仅 status=1，不缓存） */
    InstitutionAppointmentVO appointment(Long id);
}
