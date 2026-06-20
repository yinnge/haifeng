package com.haifeng.app.service.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.civilService.CivilPositionSearchDTO;
import com.haifeng.app.vo.employment.civilService.CivilPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.CivilPositionListVO;

public interface CivilPositionService {

    IPage<CivilPositionListVO> page(CivilPositionSearchDTO dto);

    CivilPositionDetailVO detail(Long id);
}
