package com.haifeng.app.service.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.grassrootsPosition.PublicWelfarePositionSearchDTO;
import com.haifeng.app.vo.employment.grassrootsPosition.PublicWelfarePositionDetailVO;
import com.haifeng.app.vo.employment.grassrootsPosition.PublicWelfarePositionListVO;

public interface PublicWelfarePositionService {
    IPage<PublicWelfarePositionListVO> page(PublicWelfarePositionSearchDTO dto);
    PublicWelfarePositionDetailVO detail(Long id);
}
