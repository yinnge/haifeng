package com.haifeng.admin.service.impl.algorithm.config;

import com.haifeng.admin.dto.algorithm.config.GaokaoConfigUpdateDTO;
import com.haifeng.admin.service.algorithm.config.GaokaoConfigService;
import com.haifeng.admin.vo.algorithm.config.GaokaoConfigDetailVO;
import com.haifeng.common.entity.algorithm.GaokaoConfig;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.GaokaoConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GaokaoConfigServiceImpl implements GaokaoConfigService {

    private final GaokaoConfigMapper gaokaoConfigMapper;

    @Override
    public GaokaoConfigDetailVO getCurrent() {
        GaokaoConfig entity = gaokaoConfigMapper.selectSingleton();
        if (entity == null) {
            throw new BusinessException(404, "高考算法全局配置不存在");
        }
        return convertToDetailVO(entity);
    }

    @Override
    public void update(GaokaoConfigUpdateDTO dto) {
        GaokaoConfig entity = gaokaoConfigMapper.selectSingleton();
        if (entity == null) {
            entity = GaokaoConfig.builder()
                    .id((short) 1)
                    .build();
        }

        entity.setDefaultDensityK(dto.getDefaultDensityK());
        entity.setDefaultLineSteepness(dto.getDefaultLineSteepness());
        entity.setDefaultRankSteepness(dto.getDefaultRankSteepness());
        entity.setNewGaokaoLineWeight(dto.getNewGaokaoLineWeight());
        entity.setNewGaokaoRankWeight(dto.getNewGaokaoRankWeight());
        entity.setOldGaokaoLineWeight(dto.getOldGaokaoLineWeight());
        entity.setOldGaokaoRankWeight(dto.getOldGaokaoRankWeight());
        entity.setWeightSoftGroup(dto.getWeightSoftGroup());
        entity.setWeightSoftBoth(dto.getWeightSoftBoth());
        entity.setYearWeights(dto.getYearWeights());

        if (entity.getCreatedAt() == null) {
            gaokaoConfigMapper.insert(entity);
        } else {
            gaokaoConfigMapper.updateById(entity);
        }
        log.info("修改高考算法全局配置");
    }

    private GaokaoConfigDetailVO convertToDetailVO(GaokaoConfig entity) {
        GaokaoConfigDetailVO vo = new GaokaoConfigDetailVO();
        vo.setDefaultDensityK(entity.getDefaultDensityK());
        vo.setDefaultLineSteepness(entity.getDefaultLineSteepness());
        vo.setDefaultRankSteepness(entity.getDefaultRankSteepness());
        vo.setNewGaokaoLineWeight(entity.getNewGaokaoLineWeight());
        vo.setNewGaokaoRankWeight(entity.getNewGaokaoRankWeight());
        vo.setOldGaokaoLineWeight(entity.getOldGaokaoLineWeight());
        vo.setOldGaokaoRankWeight(entity.getOldGaokaoRankWeight());
        vo.setWeightSoftGroup(entity.getWeightSoftGroup());
        vo.setWeightSoftBoth(entity.getWeightSoftBoth());
        vo.setYearWeights(entity.getYearWeights());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}
