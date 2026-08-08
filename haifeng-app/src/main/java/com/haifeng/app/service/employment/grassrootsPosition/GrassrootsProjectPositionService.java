package com.haifeng.app.service.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.grassrootsPosition.GrassrootsProjectPositionSearchDTO;
import com.haifeng.app.vo.employment.grassrootsPosition.GrassrootsProjectPositionDetailVO;
import com.haifeng.app.vo.employment.grassrootsPosition.GrassrootsProjectPositionListVO;

import java.util.List;

public interface GrassrootsProjectPositionService {
    IPage<GrassrootsProjectPositionListVO> page(GrassrootsProjectPositionSearchDTO dto);
    GrassrootsProjectPositionDetailVO detail(Long id);

    /** 所有不重复的招募年份（倒序），供前端筛选下拉 */
    List<String> listYears();

    /** 所有不重复的毕业年份要求（倒序），供前端筛选下拉 */
    List<String> listGradYears();
}
