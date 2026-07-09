package com.haifeng.admin.service.algorithm.config;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.config.ProvinceConfigQueryDTO;
import com.haifeng.admin.dto.algorithm.config.ProvinceConfigUpdateDTO;
import com.haifeng.admin.vo.algorithm.config.ProvinceConfigDetailVO;
import com.haifeng.admin.vo.algorithm.config.ProvinceConfigListVO;

public interface ProvinceConfigService {

    IPage<ProvinceConfigListVO> page(ProvinceConfigQueryDTO dto);

    ProvinceConfigDetailVO detail(String province);

    void update(String province, ProvinceConfigUpdateDTO dto);
}
