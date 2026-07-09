package com.haifeng.admin.service.algorithm.config;

import com.haifeng.admin.dto.algorithm.config.GaokaoConfigUpdateDTO;
import com.haifeng.admin.vo.algorithm.config.GaokaoConfigDetailVO;

public interface GaokaoConfigService {

    GaokaoConfigDetailVO getCurrent();

    void update(GaokaoConfigUpdateDTO dto);
}
