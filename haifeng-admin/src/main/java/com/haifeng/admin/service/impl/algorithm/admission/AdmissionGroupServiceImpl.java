package com.haifeng.admin.service.impl.algorithm.admission;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.algorithm.admission.AdmissionGroupAddDTO;
import com.haifeng.admin.dto.algorithm.admission.AdmissionGroupQueryDTO;
import com.haifeng.admin.service.algorithm.admission.AdmissionGroupService;
import com.haifeng.admin.vo.algorithm.admission.AdmissionGroupDetailVO;
import com.haifeng.admin.vo.algorithm.admission.AdmissionGroupListVO;
import com.haifeng.common.entity.algorithm.AdmissionGroup;
import com.haifeng.common.entity.algorithm.AdmissionMajorScore;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.AdmissionGroupMapper;
import com.haifeng.common.mapper.algorithm.AdmissionMajorScoreMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdmissionGroupServiceImpl implements AdmissionGroupService {

    private final AdmissionGroupMapper admissionGroupMapper;
    private final AdmissionMajorScoreMapper admissionMajorScoreMapper;
    private final UniversityMapper universityMapper;

    @Override
    public IPage<AdmissionGroupListVO> page(AdmissionGroupQueryDTO dto) {
        Page<AdmissionGroup> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<AdmissionGroup> wrapper = new LambdaQueryWrapper<>();

        // 按大学名称模糊查询：先查询符合条件的大学ID列表
        if (StringUtils.hasText(dto.getUniversityName())) {
            LambdaQueryWrapper<University> uniWrapper = new LambdaQueryWrapper<>();
            uniWrapper.like(University::getName, dto.getUniversityName())
                      .eq(University::getStatus, 1)
                      .select(University::getId);
            List<University> universities = universityMapper.selectList(uniWrapper);
            if (universities.isEmpty()) {
                // 没有匹配的大学，直接返回空结果
                return new Page<AdmissionGroupListVO>(dto.getPage(), dto.getSize());
            }
            List<Long> universityIds = universities.stream()
                    .map(University::getId)
                    .collect(Collectors.toList());
            wrapper.in(AdmissionGroup::getUniversityId, universityIds);
        }

        if (dto.getYear() != null) {
            wrapper.eq(AdmissionGroup::getYear, dto.getYear());
        }
        if (StringUtils.hasText(dto.getProvince())) {
            wrapper.eq(AdmissionGroup::getProvince, dto.getProvince());
        }
        if (StringUtils.hasText(dto.getSubjectType())) {
            wrapper.eq(AdmissionGroup::getSubjectType, dto.getSubjectType());
        }
        if (StringUtils.hasText(dto.getEnrollmentCode())) {
            wrapper.like(AdmissionGroup::getEnrollmentCode, dto.getEnrollmentCode());
        }
        if (StringUtils.hasText(dto.getGroupCode())) {
            wrapper.like(AdmissionGroup::getGroupCode, dto.getGroupCode());
        }
        if (StringUtils.hasText(dto.getGroupName())) {
            wrapper.like(AdmissionGroup::getGroupName, dto.getGroupName());
        }

        // 默认查询未删除的记录
        if (dto.getIsDeleted() != null) {
            wrapper.eq(AdmissionGroup::getIsDeleted, dto.getIsDeleted());
        } else {
            wrapper.eq(AdmissionGroup::getIsDeleted, false);
        }

        wrapper.orderByDesc(AdmissionGroup::getYear)
               .orderByAsc(AdmissionGroup::getUniversityId)
               .orderByAsc(AdmissionGroup::getGroupCode);

        IPage<AdmissionGroup> result = admissionGroupMapper.selectPage(page, wrapper);

        // 批量获取大学名称
        Map<Long, String> universityNameMap = getUniversityNameMap(result.getRecords());

        return result.convert(entity -> convertToListVO(entity, universityNameMap));
    }

    @Override
    public AdmissionGroupDetailVO detail(Integer id) {
        AdmissionGroup entity = admissionGroupMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "专业组不存在");
        }
        return convertToDetailVO(entity);
    }

    @Override
    public Integer add(AdmissionGroupAddDTO dto) {
        // 检查大学是否存在
        validateUniversityExists(dto.getUniversityId());

        // 检查唯一约束
        Integer existingId = admissionGroupMapper.selectIdByBusinessKey(
                dto.getUniversityId(),
                dto.getYear(),
                dto.getProvince(),
                dto.getSubjectType(),
                dto.getBatch(),
                dto.getGroupCode()
        );
        if (existingId != null) {
            throw new BusinessException(400, "该专业组已存在（相同大学、年份、省份、科类、批次、组代码）");
        }

        AdmissionGroup entity = new AdmissionGroup();
        BeanUtils.copyProperties(dto, entity);
        entity.setIsDeleted(false);

        admissionGroupMapper.insert(entity);
        log.info("新增专业组成功，id={}, universityId={}, groupCode={}",
                entity.getId(), entity.getUniversityId(), entity.getGroupCode());
        return entity.getId();
    }

    @Override
    public void update(Integer id, AdmissionGroupAddDTO dto) {
        AdmissionGroup existing = admissionGroupMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "专业组不存在");
        }

        // 检查大学是否存在
        validateUniversityExists(dto.getUniversityId());

        // 检查唯一约束（排除自己）
        Integer existingId = admissionGroupMapper.selectIdByBusinessKey(
                dto.getUniversityId(),
                dto.getYear(),
                dto.getProvince(),
                dto.getSubjectType(),
                dto.getBatch(),
                dto.getGroupCode()
        );
        if (existingId != null && !existingId.equals(id)) {
            throw new BusinessException(400, "该专业组已存在（相同大学、年份、省份、科类、批次、组代码）");
        }

        BeanUtils.copyProperties(dto, existing);
        existing.setId(id);
        admissionGroupMapper.updateById(existing);
        log.info("更新专业组成功，id={}", id);
    }

    @Override
    public void updateStatus(Integer id, Boolean isDeleted) {
        AdmissionGroup entity = admissionGroupMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "专业组不存在");
        }

        entity.setIsDeleted(isDeleted);
        admissionGroupMapper.updateById(entity);
        log.info("更新专业组状态成功，id={}，isDeleted={}", id, isDeleted);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id) {
        AdmissionGroup entity = admissionGroupMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "专业组不存在");
        }

        // 先删除关联的明细记录
        LambdaQueryWrapper<AdmissionMajorScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdmissionMajorScore::getGroupId, id);
        int deletedCount = admissionMajorScoreMapper.delete(wrapper);

        // 再删除专业组
        admissionGroupMapper.deleteById(id);
        log.info("删除专业组成功，id={}，同时删除明细记录{}条", id, deletedCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 先删除关联的明细记录
        LambdaQueryWrapper<AdmissionMajorScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(AdmissionMajorScore::getGroupId, ids);
        int deletedCount = admissionMajorScoreMapper.delete(wrapper);

        // 再删除专业组
        admissionGroupMapper.deleteBatchIds(ids);
        log.info("批量删除专业组成功，ids={}，同时删除明细记录{}条", ids, deletedCount);
    }

    @Override
    public void importData(MultipartFile file) {
        throw new BusinessException(500, "导入功能待实现");
    }

    @Override
    public Integer recalcAll() {
        Integer count = admissionGroupMapper.recalcAllGroups();
        log.info("全量重算聚合数据完成，处理专业组数量={}", count);
        return count;
    }

    private void validateUniversityExists(Long universityId) {
        LambdaQueryWrapper<University> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(University::getId, universityId)
               .eq(University::getStatus, 1);
        if (universityMapper.selectCount(wrapper) == 0) {
            throw new BusinessException(400, "大学不存在或已禁用");
        }
    }

    private Map<Long, String> getUniversityNameMap(List<AdmissionGroup> records) {
        if (records == null || records.isEmpty()) {
            return new HashMap<>();
        }

        Set<Long> universityIds = records.stream()
                .map(AdmissionGroup::getUniversityId)
                .collect(Collectors.toSet());

        LambdaQueryWrapper<University> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(University::getId, universityIds)
               .select(University::getId, University::getName);
        List<University> universities = universityMapper.selectList(wrapper);

        return universities.stream()
                .collect(Collectors.toMap(University::getId, University::getName));
    }

    private AdmissionGroupListVO convertToListVO(AdmissionGroup entity, Map<Long, String> universityNameMap) {
        AdmissionGroupListVO vo = new AdmissionGroupListVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setUniversityName(universityNameMap.get(entity.getUniversityId()));
        return vo;
    }

    private AdmissionGroupDetailVO convertToDetailVO(AdmissionGroup entity) {
        AdmissionGroupDetailVO vo = new AdmissionGroupDetailVO();
        BeanUtils.copyProperties(entity, vo);

        // 获取大学名称
        University university = universityMapper.selectById(entity.getUniversityId());
        if (university != null) {
            vo.setUniversityName(university.getName());
        }

        return vo;
    }
}
