package com.haifeng.app.service.industry;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.industry.IndustryQueryDTO;
import com.haifeng.app.vo.company.IndustryEnterpriseGroupVO;
import com.haifeng.app.vo.industry.IndustryDetailVO;
import com.haifeng.app.vo.industry.IndustryListVO;

import java.util.List;

public interface IndustryService {

    /**
     * 获取所有不重复的行业分类，用于前端下拉筛选
     */
    List<String> getCategories();

    /**
     * 分页查询行业列表（isDeleted=false）；category EQ；排序 id ASC
     */
    IPage<IndustryListVO> page(IndustryQueryDTO dto);

    /**
     * 行业详情：通过 industryId 关联 t_industry_detail 查询
     * 不存在 → BusinessException(NOT_FOUND)
     */
    IndustryDetailVO detail(Long industryId);

    /**
     * 根据行业ID列表批量查询关联企业，按请求顺序分组返回
     * 空列表 → BusinessException(BAD_REQUEST)
     */
    List<IndustryEnterpriseGroupVO> enterprisesByIndustryIds(List<Long> industryIds);
}
