package com.haifeng.app.service.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.civilService.SelectionPositionSearchDTO;
import com.haifeng.app.vo.employment.civilService.SelectionPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.SelectionPositionListVO;

import java.util.List;

public interface SelectionPositionService {

    IPage<SelectionPositionListVO> page(SelectionPositionSearchDTO dto);

    SelectionPositionDetailVO detail(Long id);

    /** 所有不重复的年份（倒序），供前端年份筛选下拉 */
    List<String> listYears();
}
