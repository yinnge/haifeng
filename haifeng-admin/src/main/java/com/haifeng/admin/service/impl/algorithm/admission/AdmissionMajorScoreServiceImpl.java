package com.haifeng.admin.service.impl.algorithm.admission;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.algorithm.admission.AdmissionMajorScoreAddDTO;
import com.haifeng.admin.dto.algorithm.admission.AdmissionMajorScoreQueryDTO;
import com.haifeng.admin.service.algorithm.admission.AdmissionMajorScoreService;
import com.haifeng.admin.vo.algorithm.admission.AdmissionMajorScoreDetailVO;
import com.haifeng.admin.vo.algorithm.admission.AdmissionMajorScoreListVO;
import com.haifeng.common.entity.algorithm.AdmissionGroup;
import com.haifeng.common.entity.algorithm.AdmissionMajorScore;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.AdmissionGroupMapper;
import com.haifeng.common.mapper.algorithm.AdmissionMajorScoreMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdmissionMajorScoreServiceImpl implements AdmissionMajorScoreService {

    private final AdmissionMajorScoreMapper admissionMajorScoreMapper;
    private final AdmissionGroupMapper admissionGroupMapper;

    @Override
    public IPage<AdmissionMajorScoreListVO> page(AdmissionMajorScoreQueryDTO dto) {
        Page<AdmissionMajorScore> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<AdmissionMajorScore> wrapper = new LambdaQueryWrapper<>();

        if (dto.getGroupId() != null) {
            wrapper.eq(AdmissionMajorScore::getGroupId, dto.getGroupId());
        }
        if (StringUtils.hasText(dto.getMajorCode())) {
            wrapper.like(AdmissionMajorScore::getMajorCode, dto.getMajorCode());
        }
        if (StringUtils.hasText(dto.getMajorName())) {
            wrapper.like(AdmissionMajorScore::getMajorName, dto.getMajorName());
        }
        if (StringUtils.hasText(dto.getEducationLevel())) {
            wrapper.eq(AdmissionMajorScore::getEducationLevel, dto.getEducationLevel());
        }

        // 默认查询未删除的记录
        if (dto.getIsDeleted() != null) {
            wrapper.eq(AdmissionMajorScore::getIsDeleted, dto.getIsDeleted());
        } else {
            wrapper.eq(AdmissionMajorScore::getIsDeleted, false);
        }

        wrapper.orderByAsc(AdmissionMajorScore::getMajorCode)
               .orderByAsc(AdmissionMajorScore::getId);

        IPage<AdmissionMajorScore> result = admissionMajorScoreMapper.selectPage(page, wrapper);

        return result.convert(this::convertToListVO);
    }

    @Override
    public AdmissionMajorScoreDetailVO detail(Integer id) {
        AdmissionMajorScore entity = admissionMajorScoreMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "专业录取明细不存在");
        }
        return convertToDetailVO(entity);
    }

    @Override
    public Integer add(AdmissionMajorScoreAddDTO dto) {
        // 检查专业组是否存在且未删除
        validateGroupExists(dto.getGroupId());

        // 检查同一专业组内majorCode是否重复
        if (existsByGroupIdAndMajorCode(dto.getGroupId(), dto.getMajorCode(), null)) {
            throw new BusinessException(400, "该专业组内已存在相同的专业代码");
        }

        AdmissionMajorScore entity = new AdmissionMajorScore();
        BeanUtils.copyProperties(dto, entity);

        admissionMajorScoreMapper.insert(entity);
        log.info("新增专业录取明细成功，id={}, groupId={}, majorCode={}",
                entity.getId(), entity.getGroupId(), entity.getMajorCode());
        return entity.getId();
    }

    @Override
    public void update(Integer id, AdmissionMajorScoreAddDTO dto) {
        AdmissionMajorScore existing = admissionMajorScoreMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "专业录取明细不存在");
        }

        // 检查专业组是否存在且未删除
        validateGroupExists(dto.getGroupId());

        // 检查同一专业组内majorCode是否重复（排除自己）
        if (existsByGroupIdAndMajorCode(dto.getGroupId(), dto.getMajorCode(), id)) {
            throw new BusinessException(400, "该专业组内已存在相同的专业代码");
        }

        BeanUtils.copyProperties(dto, existing);
        existing.setId(id);
        admissionMajorScoreMapper.updateById(existing);
        log.info("更新专业录取明细成功，id={}", id);
    }

    @Override
    public void updateStatus(Integer id, Boolean isDeleted) {
        AdmissionMajorScore entity = admissionMajorScoreMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "专业录取明细不存在");
        }
        admissionMajorScoreMapper.updateIsDeletedById(id, isDeleted);
        log.info("更新专业明细状态成功，id={}，isDeleted={}", id, isDeleted);
    }

    @Override
    public void delete(Integer id) {
        AdmissionMajorScore entity = admissionMajorScoreMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "专业录取明细不存在");
        }

        admissionMajorScoreMapper.deleteById(id);
        log.info("删除专业录取明细成功，id={}", id);
    }

    @Override
    public void batchDelete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        admissionMajorScoreMapper.deleteBatchIds(ids);
        log.info("批量删除专业录取明细成功，ids={}", ids);
    }

    private void validateGroupExists(Integer groupId) {
        LambdaQueryWrapper<AdmissionGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdmissionGroup::getId, groupId)
               .eq(AdmissionGroup::getIsDeleted, false);
        if (admissionGroupMapper.selectCount(wrapper) == 0) {
            throw new BusinessException(400, "专业组不存在或已删除");
        }
    }

    private boolean existsByGroupIdAndMajorCode(Integer groupId, String majorCode, Integer excludeId) {
        int count = admissionMajorScoreMapper.countByGroupIdAndMajorCode(groupId, majorCode);
        if (excludeId != null) {
            // 如果是更新操作，需要检查是否是当前记录本身
            AdmissionMajorScore existing = admissionMajorScoreMapper.selectById(excludeId);
            if (existing != null && existing.getGroupId().equals(groupId)
                    && existing.getMajorCode().equals(majorCode)) {
                return count > 1;
            }
        }
        return count > 0;
    }

    private AdmissionMajorScoreListVO convertToListVO(AdmissionMajorScore entity) {
        AdmissionMajorScoreListVO vo = new AdmissionMajorScoreListVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private AdmissionMajorScoreDetailVO convertToDetailVO(AdmissionMajorScore entity) {
        AdmissionMajorScoreDetailVO vo = new AdmissionMajorScoreDetailVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
