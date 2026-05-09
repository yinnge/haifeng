package com.haifeng.admin.service.impl.algorithm.admission;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.algorithm.admission.AdmissionGroupAddDTO;
import com.haifeng.admin.dto.algorithm.admission.AdmissionGroupQueryDTO;
import com.haifeng.admin.excel.algorithm.admission.AdmissionImportDTO;
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

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;
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
    @Transactional(rollbackFor = Exception.class)
    public void importData(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }

        List<AdmissionImportDTO> dataList;
        try {
            dataList = EasyExcel.read(file.getInputStream())
                    .head(AdmissionImportDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "读取Excel文件失败: " + e.getMessage());
        }

        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }

        // ==================== 第一次遍历：校验 ====================
        List<String> errors = new ArrayList<>();
        Map<String, Long> universityIdCache = new HashMap<>();
        Set<String> validSubjectTypes = Set.of("理科", "物理类", "文科", "历史类", "不分文理");
        Set<String> validBatches = Set.of("本科批", "提前批", "专科批");
        // 用于检查同一专业组内专业代码是否重复
        Map<String, Set<String>> groupMajorCodes = new HashMap<>();

        for (int i = 0; i < dataList.size(); i++) {
            int rowNum = i + 2;
            AdmissionImportDTO dto = dataList.get(i);

            // 必填字段校验
            if (!StringUtils.hasText(dto.getUniversityName())) {
                errors.add("第" + rowNum + "行: 大学名不能为空");
                continue;
            }
            if (dto.getYear() == null) {
                errors.add("第" + rowNum + "行: 年份不能为空");
                continue;
            }
            if (!StringUtils.hasText(dto.getProvince())) {
                errors.add("第" + rowNum + "行: 省份不能为空");
                continue;
            }
            if (!StringUtils.hasText(dto.getSubjectType())) {
                errors.add("第" + rowNum + "行: 科类不能为空");
                continue;
            }
            if (!StringUtils.hasText(dto.getBatch())) {
                errors.add("第" + rowNum + "行: 批次不能为空");
                continue;
            }
            if (!StringUtils.hasText(dto.getGroupCode())) {
                errors.add("第" + rowNum + "行: 专业组代码不能为空");
                continue;
            }
            if (!StringUtils.hasText(dto.getMajorCode())) {
                errors.add("第" + rowNum + "行: 专业代码不能为空");
                continue;
            }
            if (!StringUtils.hasText(dto.getMajorName())) {
                errors.add("第" + rowNum + "行: 专业名称不能为空");
                continue;
            }

            // 枚举值校验
            if (!validSubjectTypes.contains(dto.getSubjectType())) {
                errors.add("第" + rowNum + "行: 科类[" + dto.getSubjectType() + "]不合法，只允许：理科/物理类/文科/历史类/不分文理");
                continue;
            }
            if (!validBatches.contains(dto.getBatch())) {
                errors.add("第" + rowNum + "行: 批次[" + dto.getBatch() + "]不合法，只允许：本科批/提前批/专科批");
                continue;
            }

            // 大学名校验
            String uniName = dto.getUniversityName().trim();
            if (!universityIdCache.containsKey(uniName)) {
                Long universityId = universityMapper.selectIdByName(uniName);
                if (universityId == null) {
                    errors.add("第" + rowNum + "行: 大学[" + uniName + "]不存在");
                    continue;
                }
                universityIdCache.put(uniName, universityId);
            }

            // 同组专业代码重复检查
            String groupKey = String.format("%s_%d_%s_%s_%s_%s",
                    uniName, dto.getYear(), dto.getProvince(),
                    dto.getSubjectType(), dto.getBatch(), dto.getGroupCode());
            groupMajorCodes.computeIfAbsent(groupKey, k -> new HashSet<>());
            if (groupMajorCodes.get(groupKey).contains(dto.getMajorCode())) {
                errors.add("第" + rowNum + "行: 专业代码[" + dto.getMajorCode() + "]在同一专业组内重复");
                continue;
            }
            groupMajorCodes.get(groupKey).add(dto.getMajorCode());
        }

        if (!errors.isEmpty()) {
            throw new BusinessException(400, "数据校验失败：" + String.join("; ", errors));
        }

        // ==================== 第二次遍历：按专业组分组插入 ====================
        Map<String, List<AdmissionImportDTO>> groupedData = new LinkedHashMap<>();
        for (AdmissionImportDTO dto : dataList) {
            String groupKey = String.format("%s_%d_%s_%s_%s_%s",
                    dto.getUniversityName().trim(), dto.getYear(), dto.getProvince(),
                    dto.getSubjectType(), dto.getBatch(), dto.getGroupCode());
            groupedData.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(dto);
        }

        OffsetDateTime now = OffsetDateTime.now();
        int groupCount = 0;
        int majorCount = 0;

        for (Map.Entry<String, List<AdmissionImportDTO>> entry : groupedData.entrySet()) {
            List<AdmissionImportDTO> rows = entry.getValue();
            AdmissionImportDTO firstRow = rows.get(0);

            Long universityId = universityIdCache.get(firstRow.getUniversityName().trim());

            // 查询或创建专业组
            Integer groupId = admissionGroupMapper.selectIdByBusinessKey(
                    universityId, firstRow.getYear(), firstRow.getProvince(),
                    firstRow.getSubjectType(), firstRow.getBatch(), firstRow.getGroupCode());

            if (groupId == null) {
                AdmissionGroup group = AdmissionGroup.builder()
                        .universityId(universityId)
                        .year(firstRow.getYear())
                        .province(firstRow.getProvince())
                        .subjectType(firstRow.getSubjectType())
                        .batch(firstRow.getBatch())
                        .enrollmentCode(firstRow.getEnrollmentCode())
                        .groupCode(firstRow.getGroupCode())
                        .groupName(firstRow.getGroupCode())
                        .description(firstRow.getGroupDescription())
                        .isDeleted(false)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                admissionGroupMapper.insert(group);
                groupId = group.getId();
                groupCount++;
            }

            // 插入专业明细
            for (AdmissionImportDTO row : rows) {
                AdmissionMajorScore majorScore = AdmissionMajorScore.builder()
                        .groupId(groupId)
                        .majorCode(row.getMajorCode())
                        .majorName(row.getMajorName())
                        .subjectRequirements(row.getSubjectRequirements())
                        .educationLevel(row.getEducationLevel())
                        .tuition(row.getTuition())
                        .description(row.getMajorDescription())
                        .admissionCount(row.getAdmissionCount())
                        .minScore(row.getMinScore())
                        .minRank(row.getMinRank())
                        .avgScore(row.getAvgScore())
                        .avgRank(row.getAvgRank())
                        .maxScore(row.getMaxScore())
                        .maxRank(row.getMaxRank())
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                admissionMajorScoreMapper.insert(majorScore);
                majorCount++;
            }
        }

        log.info("导入专业组数据成功: 新增专业组={}个, 新增专业明细={}条", groupCount, majorCount);
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
