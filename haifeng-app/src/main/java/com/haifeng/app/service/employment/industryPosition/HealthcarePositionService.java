package com.haifeng.app.service.employment.industryPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.industryPosition.HealthcarePositionSearchDTO;
import com.haifeng.app.vo.employment.industryPosition.HealthcarePositionDetailVO;
import com.haifeng.app.vo.employment.industryPosition.HealthcarePositionListVO;

public interface HealthcarePositionService {

    IPage<HealthcarePositionListVO> page(HealthcarePositionSearchDTO dto);

    HealthcarePositionDetailVO detail(Long id);
}
