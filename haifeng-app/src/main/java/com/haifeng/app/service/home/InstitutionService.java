package com.haifeng.app.service.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.home.InstitutionQueryDTO;
import com.haifeng.app.vo.home.InstitutionDetailVO;
import com.haifeng.app.vo.home.InstitutionListVO;

public interface InstitutionService {

    /** 分页查询展示中的培训机构（status=1），按 sort_order ASC, id DESC 排序 */
    IPage<InstitutionListVO> page(InstitutionQueryDTO dto);

    /** 查询培训机构详情（仅 status=1，不存在抛 404） */
    InstitutionDetailVO detail(Long id);
}
