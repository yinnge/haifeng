package com.haifeng.app.service.employment.industryPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.industryPosition.FinancePositionSearchDTO;
import com.haifeng.app.vo.employment.industryPosition.FinancePositionDetailVO;
import com.haifeng.app.vo.employment.industryPosition.FinancePositionListVO;

import java.util.Map;

public interface FinancePositionService {

    IPage<FinancePositionListVO> page(FinancePositionSearchDTO dto);

    FinancePositionDetailVO detail(Long id);

    /**
     * 获取动态筛选选项（岗位类别等去重值列表）
     */
    Map<String, Object> getFilters();
}
