package com.haifeng.admin.service.algorithm.config;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.config.ProvinceReformAddDTO;
import com.haifeng.admin.dto.algorithm.config.ProvinceReformQueryDTO;
import com.haifeng.admin.vo.algorithm.config.ProvinceReformDetailVO;
import com.haifeng.admin.vo.algorithm.config.ProvinceReformListVO;

import java.util.List;

public interface ProvinceReformService {
    IPage<ProvinceReformListVO> page(ProvinceReformQueryDTO dto);

    ProvinceReformDetailVO detail(Long id);

    Long add(ProvinceReformAddDTO dto);

    void update(Long id, ProvinceReformAddDTO dto);

    void delete(Long id);

    void batchDelete(List<Long> ids);
}
