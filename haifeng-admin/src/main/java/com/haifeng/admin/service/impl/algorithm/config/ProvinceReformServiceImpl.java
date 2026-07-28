package com.haifeng.admin.service.impl.algorithm.config;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProvinceReformServiceImpl implements ProvinceReformService {

    private final ProvinceReformMapper provinceReformMapper;

    @Override
    public IPage<ProvinceReformListVO> page(ProvinceReformQueryDTO dto) {
        Page<ProvinceReform> page = new Page<>(dto.getPage(), dto.getSize());

        Map<String, Object> params = new HashMap<>();
        params.put("isDeleted", dto.getIsDeleted());

        IPage<ProvinceReform> resultPage = provinceReformMapper.selectPageCustom(page, params);
        return resultPage.convert(this::convertToListVO);
    }

    @Override
    public ProvinceReformDetailVO detail(Long id) {
        ProvinceReform entity = provinceReformMapper.selectByIdCustom(id);
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
            throw new BusinessException(400, "省份「" + dto.getProvince() + "」的配置已存在，请勿重复添加");
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
        ProvinceReform existing = provinceReformMapper.selectByIdCustom(id);
        if (existing == null) {
            throw new BusinessException(404, "省份改革配置不存在");
        }

        Long existingId = provinceReformMapper.selectIdByProvince(dto.getProvince());
        if (existingId != null && !existingId.equals(id)) {
            throw new BusinessException(400, "省份「" + dto.getProvince() + "」的配置已存在，请勿重复添加");
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
        ProvinceReform entity = provinceReformMapper.selectByIdCustom(id);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Boolean isDeleted) {
        ProvinceReform entity = provinceReformMapper.selectByIdCustom(id);
        if (entity == null) {
            throw new BusinessException(404, "省份改革配置不存在");
        }
        provinceReformMapper.updateIsDeletedById(id, isDeleted);
        log.info("更新省份改革配置状态，id={}, isDeleted={}", id, isDeleted);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchStatus(List<Long> ids, Boolean isDeleted) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择要操作的记录");
        }
        provinceReformMapper.batchUpdateStatus(ids, isDeleted);
        log.info("批量更新省份改革配置状态，count={}, isDeleted={}", ids.size(), isDeleted);
    }

    private ProvinceReformListVO convertToListVO(ProvinceReform entity) {
        ProvinceReformListVO vo = new ProvinceReformListVO();
        vo.setId(entity.getId());
        vo.setProvince(entity.getProvince());
        vo.setReformYear(entity.getReformYear());
        vo.setReformModel(entity.getReformModel());
        vo.setIsDeleted(entity.getIsDeleted());
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
