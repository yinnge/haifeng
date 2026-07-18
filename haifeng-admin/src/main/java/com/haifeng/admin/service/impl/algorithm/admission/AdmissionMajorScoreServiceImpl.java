package com.haifeng.admin.service.impl.algorithm.admission;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional(rollbackFor = Exception.class)
    public Integer add(AdmissionMajorScoreAddDTO dto) {
        // 检查专业组是否存在且未删除
        validateGroupExists(dto.getGroupId());

        // 检查同一专业组内majorCode是否重复
        if (existsByGroupIdAndMajorCode(dto.getGroupId(), dto.getMajorCode(), null)) {
            throw new BusinessException(400, "该专业组内已存在相同的专业代码");
        }

        AdmissionMajorScore entity = new AdmissionMajorScore();
        entity.setGroupId(dto.getGroupId());
        entity.setMajorId(dto.getMajorId());
        entity.setMajorCode(dto.getMajorCode());
        entity.setMajorName(dto.getMajorName());
        entity.setEducationLevel(dto.getEducationLevel());
        entity.setDuration(dto.getDuration());
        entity.setTuition(dto.getTuition());
        entity.setDescription(dto.getDescription());
        entity.setAdmissionCount(dto.getAdmissionCount());
        entity.setMinScore(dto.getMinScore());
        entity.setMinRank(dto.getMinRank());
        entity.setAvgScore(dto.getAvgScore());
        entity.setAvgRank(dto.getAvgRank());
        entity.setMaxScore(dto.getMaxScore());
        entity.setMaxRank(dto.getMaxRank());
        entity.setConstraints(dto.getConstraints());
        entity.setIsDeleted(false);

        admissionMajorScoreMapper.insert(entity);
        log.info("新增专业录取明细成功，id={}, groupId={}, majorCode={}",
                entity.getId(), entity.getGroupId(), entity.getMajorCode());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Integer id, AdmissionMajorScoreAddDTO dto) {
        AdmissionMajorScore existing = admissionMajorScoreMapper.selectById(id);
        if (existing == null || existing.getIsDeleted()) {
            throw new BusinessException(404, "专业录取明细不存在");
        }

        // 检查专业组是否存在且未删除
        validateGroupExists(dto.getGroupId());

        // 检查同一专业组内majorCode是否重复（排除自己）
        if (existsByGroupIdAndMajorCode(dto.getGroupId(), dto.getMajorCode(), id)) {
            throw new BusinessException(400, "该专业组内已存在相同的专业代码");
        }

        if (dto.getGroupId() != null) existing.setGroupId(dto.getGroupId());
        if (dto.getMajorId() != null) existing.setMajorId(dto.getMajorId());
        if (dto.getMajorCode() != null) existing.setMajorCode(dto.getMajorCode());
        if (dto.getMajorName() != null) existing.setMajorName(dto.getMajorName());
        if (dto.getEducationLevel() != null) existing.setEducationLevel(dto.getEducationLevel());
        if (dto.getDuration() != null) existing.setDuration(dto.getDuration());
        if (dto.getTuition() != null) existing.setTuition(dto.getTuition());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
        if (dto.getAdmissionCount() != null) existing.setAdmissionCount(dto.getAdmissionCount());
        if (dto.getMinScore() != null) existing.setMinScore(dto.getMinScore());
        if (dto.getMinRank() != null) existing.setMinRank(dto.getMinRank());
        if (dto.getAvgScore() != null) existing.setAvgScore(dto.getAvgScore());
        if (dto.getAvgRank() != null) existing.setAvgRank(dto.getAvgRank());
        if (dto.getMaxScore() != null) existing.setMaxScore(dto.getMaxScore());
        if (dto.getMaxRank() != null) existing.setMaxRank(dto.getMaxRank());
        if (dto.getConstraints() != null) existing.setConstraints(dto.getConstraints());
        admissionMajorScoreMapper.updateById(existing);
        log.info("更新专业录取明细成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Integer id, Boolean isDeleted) {
        AdmissionMajorScore entity = admissionMajorScoreMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "专业录取明细不存在");
        }
        admissionMajorScoreMapper.updateIsDeletedById(id, isDeleted);
        log.info("更新专业明细状态成功，id={}，isDeleted={}", id, isDeleted);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id) {
        AdmissionMajorScore entity = admissionMajorScoreMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "专业录取明细不存在");
        }

        entity.setIsDeleted(true);
        admissionMajorScoreMapper.updateById(entity);
        log.info("软删除专业录取明细成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        int affected = admissionMajorScoreMapper.update(null,
                Wrappers.lambdaUpdate(AdmissionMajorScore.class)
                        .set(AdmissionMajorScore::getIsDeleted, true)
                        .in(AdmissionMajorScore::getId, ids)
                        .eq(AdmissionMajorScore::getIsDeleted, false));
        log.info("批量软删除专业录取明细成功，请求{}条，实际删除{}条", ids.size(), affected);
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
        return admissionMajorScoreMapper.countByGroupIdAndMajorCode(groupId, majorCode, excludeId) > 0;
    }

    private AdmissionMajorScoreListVO convertToListVO(AdmissionMajorScore entity) {
        AdmissionMajorScoreListVO vo = new AdmissionMajorScoreListVO();
        vo.setId(entity.getId());
        vo.setGroupId(entity.getGroupId());
        vo.setMajorCode(entity.getMajorCode());
        vo.setMajorName(entity.getMajorName());
        vo.setEducationLevel(entity.getEducationLevel());
        vo.setAdmissionCount(entity.getAdmissionCount());
        vo.setMinScore(entity.getMinScore());
        vo.setMinRank(entity.getMinRank());
        vo.setAvgScore(entity.getAvgScore());
        vo.setIsDeleted(entity.getIsDeleted());
        return vo;
    }

    private AdmissionMajorScoreDetailVO convertToDetailVO(AdmissionMajorScore entity) {
        AdmissionMajorScoreDetailVO vo = new AdmissionMajorScoreDetailVO();
        vo.setId(entity.getId());
        vo.setGroupId(entity.getGroupId());
        vo.setMajorId(entity.getMajorId());
        vo.setMajorCode(entity.getMajorCode());
        vo.setMajorName(entity.getMajorName());
        vo.setEducationLevel(entity.getEducationLevel());
        vo.setDuration(entity.getDuration());
        vo.setTuition(entity.getTuition());
        vo.setDescription(entity.getDescription());
        vo.setAdmissionCount(entity.getAdmissionCount());
        vo.setMinScore(entity.getMinScore());
        vo.setMinRank(entity.getMinRank());
        vo.setAvgScore(entity.getAvgScore());
        vo.setAvgRank(entity.getAvgRank());
        vo.setMaxScore(entity.getMaxScore());
        vo.setMaxRank(entity.getMaxRank());
        vo.setConstraints(entity.getConstraints());
        vo.setIsDeleted(entity.getIsDeleted());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
