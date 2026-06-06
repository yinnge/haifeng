package com.haifeng.app.service.company;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.company.EnterpriseQueryDTO;
import com.haifeng.app.vo.company.EnterpriseIndustryGroupVO;
import com.haifeng.app.vo.company.EnterpriseListVO;
import com.haifeng.app.vo.company.EnterprisePositionVO;

import java.util.List;

public interface EnterpriseService {

    /** 企业分页列表（公开） */
    IPage<EnterpriseListVO> page(EnterpriseQueryDTO dto);

    /** 企业岗位列表（登录） */
    List<EnterprisePositionVO> positions(Long enterpriseId);

    /** 企业 → 行业跳转信息（Pro） */
    List<EnterpriseIndustryGroupVO> industriesByEnterpriseIds(List<Long> enterpriseIds);
}
