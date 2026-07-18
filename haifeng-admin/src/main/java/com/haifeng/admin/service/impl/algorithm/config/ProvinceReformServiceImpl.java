package com.haifeng.admin.service.impl.algorithm.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.algorithm.config.ProvinceReformAddDTO;
import com.haifeng.admin.dto.algorithm.config.ProvinceReformQueryDTO;
import com.haifeng.admin.service.algorithm.config.ProvinceReformService;
import com.haifeng.admin.vo.algorithm.config.ProvinceReformDetailVO;
import com.haifeng.admin.vo.algorithm.config.ProvinceReformListVO;
import com.haifeng.common.entity.algorithm.ProvinceReform;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.ProvinceReformMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProvinceReformServiceImpl implements ProvinceReformService {

    private final ProvinceReformMapper provinceReformMapper;

    @Override
    public IPage<ProvinceReformListVO> page(ProvinceReformQueryDTO dto) {
        Page<ProvinceReform> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<ProvinceReform> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ProvinceReform::getProvince);

        IPage<ProvinceReform> resultPage = provinceReformMapper.selectPage(page, wrapper);
        return resultPage.convert(this::convertToListVO);
    }

    @Override
    public ProvinceReformDetailVO detail(Long id) {
        ProvinceReform entity = provinceReformMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "省份改革配置不存在");
        }
        return convertToDetailVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(ProvinceReformAddDTO dto) {
        Long deletedId = provinceReformMapper.selectDeletedIdByProvince(dto.getProvince());
        if (deletedId != null) {
            ProvinceReform deleted = provinceReformMapper.selectByIdIgnoreDeleted(deletedId);
            deleted.setIsDeleted(false);
            deleted.setReformYear(dto.getReformYear());
            deleted.setReformModel(dto.getReformModel());
            provinceReformMapper.updateById(deleted);
            log.info("恢复省份改革配置，id={}, province={}", deletedId, dto.getProvince());
            return deletedId;
        }

        Long existingId = provinceReformMapper.selectIdByProvince(dto.getProvince());
        if (existingId != null) {
            throw new BusinessException(400, "该省份配置已存在");
        }

        ProvinceReform entity = ProvinceReform.builder()
                .id(SnowflakeIdGenerator.nextId())
                .province(dto.getProvince())
                .reformYear(dto.getReformYear())
                .reformModel(dto.getReformModel())
                .isDeleted(false)
                .build();

        provinceReformMapper.insert(entity);
        log.info("新增省份改革配置，province={}", dto.getProvince());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ProvinceReformAddDTO dto) {
        ProvinceReform existing = provinceReformMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "省份改革配置不存在");
        }

        Long existingId = provinceReformMapper.selectIdByProvince(dto.getProvince());
        if (existingId != null && !existingId.equals(id)) {
            throw new BusinessException(400, "该省份配置已存在");
        }

        existing.setProvince(dto.getProvince());
        existing.setReformYear(dto.getReformYear());
        existing.setReformModel(dto.getReformModel());

        provinceReformMapper.updateById(existing);
        log.info("修改省份改革配置，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ProvinceReform entity = provinceReformMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "省份改革配置不存在");
        }
        provinceReformMapper.deleteById(id);
        log.info("删除省份改革配置，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的记录");
        }
        provinceReformMapper.batchSoftDelete(ids);
        log.info("批量删除省份改革配置，count={}", ids.size());
    }

    private ProvinceReformListVO convertToListVO(ProvinceReform entity) {
        ProvinceReformListVO vo = new ProvinceReformListVO();
        vo.setId(entity.getId());
        vo.setProvince(entity.getProvince());
        vo.setReformYear(entity.getReformYear());
        vo.setReformModel(entity.getReformModel());
        return vo;
    }

    private ProvinceReformDetailVO convertToDetailVO(ProvinceReform entity) {
        ProvinceReformDetailVO vo = new ProvinceReformDetailVO();
        vo.setId(entity.getId());
        vo.setProvince(entity.getProvince());
        vo.setReformYear(entity.getReformYear());
        vo.setReformModel(entity.getReformModel());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        vo.setVersion(entity.getVersion());
        return vo;
    }
}
