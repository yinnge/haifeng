package com.haifeng.admin.service.industry;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.industry.IndustryAddDTO;
import com.haifeng.admin.dto.industry.IndustryDetailUpdateDTO;
import com.haifeng.admin.dto.industry.IndustryQueryDTO;
import com.haifeng.admin.dto.industry.IndustryStatusDTO;
import com.haifeng.admin.dto.industry.IndustryUpdateDTO;
import com.haifeng.admin.vo.industry.IndustryDetailVO;
import com.haifeng.admin.vo.industry.IndustryListVO;

import java.util.List;

public interface IndustryService {

    /**
     * 分页查询行业列表
     */
    IPage<IndustryListVO> page(IndustryQueryDTO dto);

    /**
     * 获取行业详情（主表+详情表）
     */
    IndustryDetailVO detail(Long id);

    /**
     * 新增行业（事务：主表+详情一起创建）
     */
    Long add(IndustryAddDTO dto);

    /**
     * 更新行业主表信息
     */
    void update(Long id, IndustryUpdateDTO dto);

    /**
     * 更新行业详情表信息
     */
    void updateDetail(Long id, IndustryDetailUpdateDTO dto);

    /**
     * 更新行业状态（禁用/启用）
     */
    void updateStatus(Long id, IndustryStatusDTO dto);

    /**
     * 硬删除行业（主表+详情表）
     */
    void delete(Long id);

    /**
     * 批量硬删除行业
     */
    void batchDelete(List<Long> ids);
}
