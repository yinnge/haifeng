package com.haifeng.app.service.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.grassrootsPosition.GrassrootsProjectPositionSearchDTO;
import com.haifeng.app.vo.employment.grassrootsPosition.GrassrootsProjectPositionDetailVO;
import com.haifeng.app.vo.employment.grassrootsPosition.GrassrootsProjectPositionListVO;

public interface GrassrootsProjectPositionService {
    IPage<GrassrootsProjectPositionListVO> page(GrassrootsProjectPositionSearchDTO dto);
    GrassrootsProjectPositionDetailVO detail(Long id);
}
