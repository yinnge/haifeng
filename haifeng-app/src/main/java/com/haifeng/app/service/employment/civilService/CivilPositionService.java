package com.haifeng.app.service.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.civilService.CivilPositionSearchDTO;
import com.haifeng.app.vo.employment.civilService.CivilPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.CivilPositionListVO;

import java.util.Map;

public interface CivilPositionService {

    IPage<CivilPositionListVO> page(CivilPositionSearchDTO dto);

    CivilPositionDetailVO detail(Long id);

    /**
     * 获取动态筛选选项（考试类别等去重值列表）
     */
    Map<String, Object> getFilters();
}
