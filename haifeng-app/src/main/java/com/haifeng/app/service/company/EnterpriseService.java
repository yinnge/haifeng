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

    /** 企业详情（公开，按 id 查询） */
    EnterpriseListVO detail(Long enterpriseId);

    /** 企业岗位列表（登录） */
    List<EnterprisePositionVO> positions(Long enterpriseId);

    /** 企业 → 行业跳转信息（Pro） */
    List<EnterpriseIndustryGroupVO> industriesByEnterpriseIds(List<Long> enterpriseIds);

    /** 企业类型列表（公开，去重，前端用作下拉筛选） */
    List<String> listTypes();
}
