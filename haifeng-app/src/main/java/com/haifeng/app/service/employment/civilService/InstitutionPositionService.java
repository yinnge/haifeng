package com.haifeng.app.service.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.civilService.InstitutionPositionSearchDTO;
import com.haifeng.app.vo.employment.civilService.InstitutionPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.InstitutionPositionListVO;

public interface InstitutionPositionService {

    IPage<InstitutionPositionListVO> page(InstitutionPositionSearchDTO dto);

    InstitutionPositionDetailVO detail(Long id);
}
