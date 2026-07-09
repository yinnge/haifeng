package com.haifeng.admin.service.impl.algorithm.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.algorithm.config.ProvinceConfigQueryDTO;
import com.haifeng.admin.dto.algorithm.config.ProvinceConfigUpdateDTO;
import com.haifeng.admin.service.algorithm.config.ProvinceConfigService;
import com.haifeng.admin.vo.algorithm.config.ProvinceConfigDetailVO;
import com.haifeng.admin.vo.algorithm.config.ProvinceConfigListVO;
import com.haifeng.common.entity.algorithm.ProvinceConfig;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.ProvinceConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProvinceConfigServiceImpl implements ProvinceConfigService {

    private final ProvinceConfigMapper provinceConfigMapper;

    @Override
    public IPage<ProvinceConfigListVO> page(ProvinceConfigQueryDTO dto) {
        Page<ProvinceConfig> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<ProvinceConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ProvinceConfig::getProvince);

        IPage<ProvinceConfig> resultPage = provinceConfigMapper.selectPage(page, wrapper);
        return resultPage.convert(this::convertToListVO);
    }

    @Override
    public ProvinceConfigDetailVO detail(String province) {
        ProvinceConfig entity = provinceConfigMapper.selectByProvince(province);
        if (entity == null) {
            throw new BusinessException(404, "省份配置不存在");
        }
        return convertToDetailVO(entity);
    }

    @Override
    public void update(String province, ProvinceConfigUpdateDTO dto) {
        ProvinceConfig entity = provinceConfigMapper.selectByProvince(province);
        if (entity == null) {
            throw new BusinessException(404, "省份配置不存在");
        }

        entity.setDensityK(dto.getDensityK());
        entity.setLineSteepness(dto.getLineSteepness());
        entity.setRankSteepness(dto.getRankSteepness());

        provinceConfigMapper.updateById(entity);
        log.info("修改省份算法配置，province={}", province);
    }

    private ProvinceConfigListVO convertToListVO(ProvinceConfig entity) {
        ProvinceConfigListVO vo = new ProvinceConfigListVO();
        vo.setProvince(entity.getProvince());
        vo.setDensityK(entity.getDensityK());
        vo.setLineSteepness(entity.getLineSteepness());
        vo.setRankSteepness(entity.getRankSteepness());
        return vo;
    }

    private ProvinceConfigDetailVO convertToDetailVO(ProvinceConfig entity) {
        ProvinceConfigDetailVO vo = new ProvinceConfigDetailVO();
        vo.setProvince(entity.getProvince());
        vo.setDensityK(entity.getDensityK());
        vo.setLineSteepness(entity.getLineSteepness());
        vo.setRankSteepness(entity.getRankSteepness());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}
