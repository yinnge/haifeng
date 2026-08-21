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

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdmissionMajorScoreServiceImpl implements AdmissionMajorScoreService {

    private final AdmissionMajorScoreMapper admissionMajorScoreMapper;
    private final AdmissionGroupMapper admissionGroupMapper;

    @Override
    public IPage<AdmissionMajorScoreListVO> page(AdmissionMajorScoreQueryDTO dto) {
        Page<AdmissionMajorScore> page = new Page<>(dto.getPage(), dto.getSize());

        Map<String, Object> params = new HashMap<>();
        params.put("isDeleted", dto.getIsDeleted());
        params.put("groupId", dto.getGroupId());
        if (StringUtils.hasText(dto.getMajorCode())) {
            params.put("majorCode", dto.getMajorCode());
        }
        if (StringUtils.hasText(dto.getMajorName())) {
            params.put("majorName", dto.getMajorName());
        }
        if (StringUtils.hasText(dto.getEducationLevel())) {
            params.put("educationLevel", dto.getEducationLevel());
        }

        IPage<AdmissionMajorScore> result = admissionMajorScoreMapper.selectPageCustom(page, params);
        return result.convert(this::convertToListVO);
    }

    @Override
    public AdmissionMajorScoreDetailVO detail(Integer id) {
        AdmissionMajorScore entity = admissionMajorScoreMapper.selectByIdCustom(id);
        if (entity == null) {
            throw new BusinessException(404, "专业录取明细不存在");
        }
        return convertToDetailVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer add(AdmissionMajorScoreAddDTO dto) {
        validateGroupExists(dto.getGroupId());

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
        entity.setConstraints(dto.getConstraints());
        entity.setIsDeleted(false);

        // 从 DTO 构建 history jsonb：优先使用多年度数组，否则用平铺分数字段构建单条
        List<Map<String, Object>> historyArray = null;
        if (dto.getHistory() != null && !dto.getHistory().isEmpty()) {
            historyArray = dto.getHistory();
        } else if (dto.getYear() != null) {
            historyArray = new ArrayList<>();
            historyArray.add(buildHistoryEntry(dto.getYear(), dto));
        }
        if (historyArray != null) {
            entity.setHistory(historyArray);
        }

        admissionMajorScoreMapper.insert(entity);
        log.info("新增专业录取明细成功，id={}, groupId={}, majorCode={}",
                entity.getId(), entity.getGroupId(), entity.getMajorCode());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Integer id, AdmissionMajorScoreAddDTO dto) {
        AdmissionMajorScore existing = admissionMajorScoreMapper.selectByIdCustom(id);
        if (existing == null) {
            throw new BusinessException(404, "专业录取明细不存在");
        }

        validateGroupExists(dto.getGroupId());

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
        if (dto.getConstraints() != null) existing.setConstraints(dto.getConstraints());

        // 更新 history：优先整体替换（前端提交全量多年度分数），否则用平铺字段按年份覆盖/追加
        if (dto.getHistory() != null) {
            existing.setHistory(dto.getHistory());
        } else if (dto.getYear() != null && dto.getMinScore() != null) {
            List<Map<String, Object>> history = parseHistory(existing.getHistory());
            Map<String, Object> entry = buildHistoryEntry(dto.getYear(), dto);
            // 替换或追加对应年份
            boolean replaced = false;
            for (int i = 0; i < history.size(); i++) {
                if (Objects.equals(history.get(i).get("year"), dto.getYear())) {
                    history.set(i, entry);
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                history.add(entry);
            }
            existing.setHistory(history);
        }

        int rows = admissionMajorScoreMapper.updateByIdCustom(existing);
        if (rows == 0) {
            throw new BusinessException(500, "更新专业录取明细失败，记录可能已被禁用或不存在，请刷新后重试");
        }
        log.info("更新专业录取明细成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Integer id, Boolean isDeleted) {
        AdmissionMajorScore entity = admissionMajorScoreMapper.selectByIdCustom(id);
        if (entity == null) {
            throw new BusinessException(404, "专业录取明细不存在");
        }
        admissionMajorScoreMapper.updateIsDeletedById(id, isDeleted);
        log.info("更新专业明细状态成功，id={}，isDeleted={}", id, isDeleted);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id) {
        AdmissionMajorScore entity = admissionMajorScoreMapper.selectByIdCustom(id);
        if (entity == null) {
            throw new BusinessException(404, "专业录取明细不存在");
        }
        int affected = admissionMajorScoreMapper.physicalDeleteById(id);
        if (affected == 0) {
            throw new BusinessException(404, "专业录取明细不存在");
        }
        log.info("物理删除专业录取明细成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return;
        int affected = admissionMajorScoreMapper.update(null,
                Wrappers.lambdaUpdate(AdmissionMajorScore.class)
                        .set(AdmissionMajorScore::getIsDeleted, true)
                        .in(AdmissionMajorScore::getId, ids)
                        .eq(AdmissionMajorScore::getIsDeleted, false));
        log.info("批量软删除专业录取明细成功，请求{}条，实际删除{}条", ids.size(), affected);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchHardDelete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return;
        int affected = admissionMajorScoreMapper.physicalDeleteBatchIds(ids);
        log.info("批量物理删除专业录取明细成功，请求{}条，实际删除{}条", ids.size(), affected);
    }

    // ==================== 辅助方法 ====================

    private Map<String, Object> buildHistoryEntry(Integer year, AdmissionMajorScoreAddDTO dto) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("year", year);
        entry.put("admissionCount", dto.getAdmissionCount());
        entry.put("minScore", dto.getMinScore());
        entry.put("minRank", dto.getMinRank());
        entry.put("avgScore", dto.getAvgScore());
        entry.put("avgRank", dto.getAvgRank());
        entry.put("maxScore", dto.getMaxScore());
        entry.put("maxRank", dto.getMaxRank());
        return entry;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseHistory(Object history) {
        if (history == null) return new ArrayList<>();
        if (history instanceof List) {
            return new ArrayList<>((List<Map<String, Object>>) history);
        }
        return new ArrayList<>();
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
        vo.setHistory(entity.getHistory() instanceof List ? (List<Object>) entity.getHistory() : Collections.emptyList());
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
        vo.setHistory(entity.getHistory() instanceof List ? (List<Object>) entity.getHistory() : Collections.emptyList());
        vo.setConstraints(entity.getConstraints());
        vo.setIsDeleted(entity.getIsDeleted());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
