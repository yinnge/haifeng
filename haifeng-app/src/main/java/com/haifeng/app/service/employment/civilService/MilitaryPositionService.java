package com.haifeng.app.service.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.civilService.MilitaryPositionSearchDTO;
import com.haifeng.app.vo.employment.civilService.MilitaryPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.MilitaryPositionListVO;

public interface MilitaryPositionService {

    IPage<MilitaryPositionListVO> page(MilitaryPositionSearchDTO dto);

    MilitaryPositionDetailVO detail(Long id);
}
