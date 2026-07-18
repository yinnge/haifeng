package com.haifeng.admin.service.impl.algorithm.admission;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
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
    private final PlatformTransactionManager transactionManager;

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
        if (StringUtils.hasText(dto.getRequirementType())) {
            wrapper.eq(AdmissionGroup::getRequirementType, dto.getRequirementType());
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
        // 通过大学名称查询大学信息（id + cityName）
        University university = getUniversityByName(dto.getUniversityName());

        // 检查唯一约束
        Integer existingId = admissionGroupMapper.selectIdByBusinessKey(
                university.getId(),
                dto.getYear(),
                dto.getProvince(),
                dto.getBatch(),
                dto.getGroupCode()
        );
        if (existingId != null) {
            throw new BusinessException(400, "该专业组已存在（相同大学、年份、省份、批次、组代码）");
        }

        AdmissionGroup entity = new AdmissionGroup();
        entity.setYear(dto.getYear());
        entity.setProvince(dto.getProvince());
        entity.setBatch(dto.getBatch());
        entity.setEnrollmentCode(dto.getEnrollmentCode());
        entity.setGroupCode(dto.getGroupCode());
        entity.setGroupName(dto.getGroupName());
        entity.setSubjects(dto.getSubjects());
        entity.setRequirementType(dto.getRequirementType());
        entity.setDescription(dto.getDescription());
        entity.setConstraints(dto.getConstraints());
        entity.setUniversityId(university.getId());
        entity.setUniversityName(university.getName());
        entity.setCityName(university.getCityName());
        entity.setIsDeleted(false);

        admissionGroupMapper.insert(entity);
        log.info("新增专业组成功，id={}, universityName={}, cityName={}, groupCode={}",
                entity.getId(), entity.getUniversityName(), entity.getCityName(), entity.getGroupCode());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Integer id, AdmissionGroupAddDTO dto) {
        AdmissionGroup existing = admissionGroupMapper.selectById(id);
        if (existing == null || existing.getIsDeleted()) {
            throw new BusinessException(404, "专业组不存在");
        }

        // 通过大学名称查询大学信息（id + cityName）
        University university = getUniversityByName(dto.getUniversityName());

        // 检查唯一约束（排除自己）
        Integer existingId = admissionGroupMapper.selectIdByBusinessKey(
                university.getId(),
                dto.getYear(),
                dto.getProvince(),
                dto.getBatch(),
                dto.getGroupCode()
        );
        if (existingId != null && !existingId.equals(id)) {
            throw new BusinessException(400, "该专业组已存在（相同大学、年份、省份、批次、组代码）");
        }

        existing.setYear(dto.getYear());
        existing.setProvince(dto.getProvince());
        existing.setBatch(dto.getBatch());
        existing.setEnrollmentCode(dto.getEnrollmentCode());
        existing.setGroupCode(dto.getGroupCode());
        existing.setGroupName(dto.getGroupName());
        existing.setSubjects(dto.getSubjects());
        existing.setRequirementType(dto.getRequirementType());
        existing.setDescription(dto.getDescription());
        existing.setConstraints(dto.getConstraints());
        existing.setUniversityId(university.getId());
        existing.setUniversityName(university.getName());
        existing.setCityName(university.getCityName());
        admissionGroupMapper.updateById(existing);
        log.info("更新专业组成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Integer id, Boolean isDeleted) {
        AdmissionGroup entity = admissionGroupMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "专业组不存在");
        }

        admissionGroupMapper.update(null,
                Wrappers.lambdaUpdate(AdmissionGroup.class)
                        .set(AdmissionGroup::getIsDeleted, isDeleted)
                        .eq(AdmissionGroup::getId, id));
        log.info("更新专业组状态成功，id={}，isDeleted={}", id, isDeleted);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id) {
        AdmissionGroup entity = admissionGroupMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "专业组不存在");
        }

        // 先软删除关联的明细记录
        admissionMajorScoreMapper.update(null,
                Wrappers.lambdaUpdate(AdmissionMajorScore.class)
                        .set(AdmissionMajorScore::getIsDeleted, true)
                        .eq(AdmissionMajorScore::getGroupId, id)
                        .eq(AdmissionMajorScore::getIsDeleted, false));

        // 再软删除专业组
        entity.setIsDeleted(true);
        admissionGroupMapper.updateById(entity);
        log.info("软删除专业组成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 先软删除关联的明细记录
        admissionMajorScoreMapper.update(null,
                Wrappers.lambdaUpdate(AdmissionMajorScore.class)
                        .set(AdmissionMajorScore::getIsDeleted, true)
                        .in(AdmissionMajorScore::getGroupId, ids)
                        .eq(AdmissionMajorScore::getIsDeleted, false));

        // 再软删除专业组
        int affected = admissionGroupMapper.update(null,
                Wrappers.lambdaUpdate(AdmissionGroup.class)
                        .set(AdmissionGroup::getIsDeleted, true)
                        .in(AdmissionGroup::getId, ids)
                        .eq(AdmissionGroup::getIsDeleted, false));
        log.info("批量软删除专业组成功，请求{}条，实际删除{}条", ids.size(), affected);
    }

    @Override
    public void importData(MultipartFile file) {
        // ==================== 0. 文件基础校验 ====================
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new BusinessException(400, "请上传Excel文件（.xlsx或.xls）");
        }
        String lowerName = filename.toLowerCase();
        if (!lowerName.endsWith(".xlsx") && !lowerName.endsWith(".xls")) {
            throw new BusinessException(400, "请上传Excel文件（.xlsx或.xls）");
        }
        // Content-Type 二次校验，防止篡改后缀上传非法文件
        String contentType = file.getContentType();
        if (contentType == null
                || (!contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    && !contentType.equals("application/vnd.ms-excel")
                    && !contentType.equals("application/octet-stream"))) {
            throw new BusinessException(400, "文件类型不合法，只允许xlsx或xls");
        }

        // ==================== 1. 解析Excel（事务外，避免长事务持有DB连接） ====================
        List<AdmissionImportDTO> dataList;
        try {
            dataList = EasyExcel.read(file.getInputStream())
                    .head(AdmissionImportDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (Exception e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "读取Excel文件失败: " + e.getMessage());
        }
        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }
        if (dataList.size() > 1000) {
            throw new BusinessException(400, "单次导入不能超过1000条记录");
        }

        // ==================== 2. 第一轮：全量校验（事务外，绝不 insert） ====================
        List<String> errors = new ArrayList<>();
        Map<String, University> universityCache = new HashMap<>();
        // 业务键 -> DB中已存在的专业组（null 也会缓存，表示已查过但不存在）
        Map<String, AdmissionGroup> existingGroupCache = new HashMap<>();
        // groupId -> DB中已有专业代码集合
        Map<Integer, Set<String>> existingMajorCodesCache = new HashMap<>();
        // 业务键 -> Excel内已出现专业代码（Excel内去重）
        Map<String, Set<String>> groupMajorCodes = new HashMap<>();

        Set<String> validBatches = Set.of("本科批", "提前批", "专科批");
        Set<String> validReqTypes = Set.of("不限", "2选1", "3选1", "必选1", "必选2", "必选3");
        Set<String> validSubjects = Set.of("物理", "化学", "生物", "历史", "地理", "政治");
        Set<String> validEduLevels = Set.of("本科", "专科");
        Set<String> validProvinces = Set.of(
                "北京", "天津", "河北", "山西", "内蒙古", "辽宁", "吉林", "黑龙江",
                "上海", "江苏", "浙江", "安徽", "福建", "江西", "山东", "河南",
                "湖北", "湖南", "广东", "广西", "海南", "重庆", "四川", "贵州",
                "云南", "西藏", "陕西", "甘肃", "青海", "宁夏", "新疆"
        );

        for (int i = 0; i < dataList.size(); i++) {
            int rowNum = i + 2;
            AdmissionImportDTO dto = dataList.get(i);

            // ---- 必填字段 ----
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

            // ---- 字符串长度校验（对齐数据库列长度，超长会绕过校验直接打崩DB）----
            String uniName = dto.getUniversityName().trim();
            if (uniName.length() > 50) {
                errors.add("第" + rowNum + "行: 大学名长度不能超过50");
            }
            if (dto.getEnrollmentCode() != null && dto.getEnrollmentCode().length() > 30) {
                errors.add("第" + rowNum + "行: 省招代码长度不能超过30");
            }
            if (dto.getGroupCode().length() > 30) {
                errors.add("第" + rowNum + "行: 专业组代码长度不能超过30");
            }
            if (dto.getGroupName() != null && dto.getGroupName().length() > 100) {
                errors.add("第" + rowNum + "行: 专业组名称长度不能超过100");
            }
            if (dto.getMajorCode().length() > 20) {
                errors.add("第" + rowNum + "行: 专业代码长度不能超过20");
            }
            if (dto.getMajorName().length() > 100) {
                errors.add("第" + rowNum + "行: 专业名称长度不能超过100");
            }
            if (dto.getDuration() != null && dto.getDuration().length() > 20) {
                errors.add("第" + rowNum + "行: 学制长度不能超过20");
            }
            if (dto.getTuition() != null && dto.getTuition().length() > 50) {
                errors.add("第" + rowNum + "行: 学费长度不能超过50");
            }
            if (dto.getGroupDescription() != null && dto.getGroupDescription().length() > 2000) {
                errors.add("第" + rowNum + "行: 专业组简介长度不能超过2000");
            }
            if (dto.getMajorDescription() != null && dto.getMajorDescription().length() > 2000) {
                errors.add("第" + rowNum + "行: 专业简介长度不能超过2000");
            }
            if (dto.getSubjectsStr() != null && dto.getSubjectsStr().length() > 200) {
                errors.add("第" + rowNum + "行: 科目长度不能超过200");
            }
            if (dto.getConstraintsStr() != null && dto.getConstraintsStr().length() > 500) {
                errors.add("第" + rowNum + "行: 报考限制条件长度不能超过500");
            }

            // ---- 年份范围 ----
            if (dto.getYear() < 2000 || dto.getYear() > 2100) {
                errors.add("第" + rowNum + "行: 年份[" + dto.getYear() + "]不合法，只允许2000-2100");
            }

            // ---- 省份枚举 ----
            if (!validProvinces.contains(dto.getProvince())) {
                errors.add("第" + rowNum + "行: 省份[" + dto.getProvince() + "]不合法");
            }

            // ---- 批次枚举 ----
            if (!validBatches.contains(dto.getBatch())) {
                errors.add("第" + rowNum + "行: 批次[" + dto.getBatch() + "]不合法，只允许：本科批/提前批/专科批");
            }

            // ---- 选科类型枚举 ----
            if (StringUtils.hasText(dto.getRequirementType()) && !validReqTypes.contains(dto.getRequirementType())) {
                errors.add("第" + rowNum + "行: 选科类型[" + dto.getRequirementType() + "]不合法，只允许：不限/2选1/3选1/必选1/必选2/必选3");
            }

            // ---- 科目枚举与数量 ----
            List<String> subjectsList = new ArrayList<>();
            if (StringUtils.hasText(dto.getSubjectsStr())) {
                String[] subjectArr = dto.getSubjectsStr().split("[,，]");
                for (String subject : subjectArr) {
                    String s = subject.trim();
                    if (StringUtils.hasText(s)) {
                        if (!validSubjects.contains(s)) {
                            errors.add("第" + rowNum + "行: 科目[" + s + "]不合法，只允许：物理/化学/生物/历史/地理/政治");
                        } else {
                            subjectsList.add(s);
                        }
                    }
                }
                if (subjectsList.size() > 6) {
                    errors.add("第" + rowNum + "行: 科目数量不能超过6个");
                }
            }

            // ---- 层次枚举 ----
            if (StringUtils.hasText(dto.getEducationLevel()) && !validEduLevels.contains(dto.getEducationLevel())) {
                errors.add("第" + rowNum + "行: 层次[" + dto.getEducationLevel() + "]不合法，只允许：本科/专科");
            }

            // ---- 选科类型与科目数量一致性 ----
            if (StringUtils.hasText(dto.getRequirementType()) && !subjectsList.isEmpty()) {
                int count = subjectsList.size();
                switch (dto.getRequirementType()) {
                    case "不限":
                        break;
                    case "2选1":
                    case "必选2":
                        if (count != 2) {
                            errors.add("第" + rowNum + "行: 选科类型[" + dto.getRequirementType() + "]需要2个科目，实际" + count + "个");
                        }
                        break;
                    case "3选1":
                    case "必选3":
                        if (count != 3) {
                            errors.add("第" + rowNum + "行: 选科类型[" + dto.getRequirementType() + "]需要3个科目，实际" + count + "个");
                        }
                        break;
                    case "必选1":
                        if (count != 1) {
                            errors.add("第" + rowNum + "行: 选科类型[必选1]需要1个科目，实际" + count + "个");
                        }
                        break;
                }
            }

            // ---- 数值范围校验（对齐数据库类型，防止超限打崩DB）----
            if (dto.getAdmissionCount() != null && (dto.getAdmissionCount() < 0 || dto.getAdmissionCount() > 99999)) {
                errors.add("第" + rowNum + "行: 录取人数范围0-99999");
            }
            if (dto.getMinScore() != null && (dto.getMinScore() < 0 || dto.getMinScore() > 900)) {
                errors.add("第" + rowNum + "行: 最低分范围0-900");
            }
            if (dto.getMaxScore() != null && (dto.getMaxScore() < 0 || dto.getMaxScore() > 900)) {
                errors.add("第" + rowNum + "行: 最高分范围0-900");
            }
            if (dto.getMinRank() != null && (dto.getMinRank() < 0 || dto.getMinRank() > 9999999)) {
                errors.add("第" + rowNum + "行: 最低位次范围0-9999999");
            }
            if (dto.getMaxRank() != null && (dto.getMaxRank() < 0 || dto.getMaxRank() > 9999999)) {
                errors.add("第" + rowNum + "行: 最高位次范围0-9999999");
            }
            if (dto.getAvgRank() != null && (dto.getAvgRank() < 0 || dto.getAvgRank() > 9999999)) {
                errors.add("第" + rowNum + "行: 中位位次范围0-9999999");
            }
            // avgScore 数据库 NUMERIC(6,2)，最大 9999.99
            if (dto.getAvgScore() != null && (dto.getAvgScore().compareTo(BigDecimal.ZERO) < 0
                    || dto.getAvgScore().compareTo(new BigDecimal("9999.99")) > 0)) {
                errors.add("第" + rowNum + "行: 中位分范围0-9999.99");
            }

            // ---- 分数逻辑关系 ----
            if (dto.getMinScore() != null && dto.getMaxScore() != null && dto.getMinScore() > dto.getMaxScore()) {
                errors.add("第" + rowNum + "行: 最低分不能大于最高分");
            }
            if (dto.getMinScore() != null && dto.getAvgScore() != null
                    && dto.getAvgScore().compareTo(new BigDecimal(dto.getMinScore())) < 0) {
                errors.add("第" + rowNum + "行: 中位分不能小于最低分");
            }
            if (dto.getAvgScore() != null && dto.getMaxScore() != null
                    && dto.getAvgScore().compareTo(new BigDecimal(dto.getMaxScore())) > 0) {
                errors.add("第" + rowNum + "行: 中位分不能大于最高分");
            }

            // ---- 位次逻辑关系 ----
            if (dto.getMinRank() != null && dto.getMaxRank() != null && dto.getMinRank() > dto.getMaxRank()) {
                errors.add("第" + rowNum + "行: 最低位次不能大于最高位次");
            }
            if (dto.getMinRank() != null && dto.getAvgRank() != null && dto.getMinRank() > dto.getAvgRank()) {
                errors.add("第" + rowNum + "行: 最低位次不能大于中位位次");
            }
            if (dto.getAvgRank() != null && dto.getMaxRank() != null && dto.getAvgRank() > dto.getMaxRank()) {
                errors.add("第" + rowNum + "行: 中位位次不能大于最高位次");
            }

            // ---- 大学名校验 + 缓存 ----
            if (!universityCache.containsKey(uniName)) {
                University university = universityMapper.selectIdAndCityByName(uniName);
                if (university == null) {
                    errors.add("第" + rowNum + "行: 大学[" + uniName + "]不存在");
                    continue;
                }
                universityCache.put(uniName, university);
            }
            University university = universityCache.get(uniName);

            // ---- 与数据库已有专业代码冲突检查（关键：保证要么全成功要么全失败）----
            String groupKey = String.format("%s_%d_%s_%s_%s",
                    uniName, dto.getYear(), dto.getProvince(),
                    dto.getBatch(), dto.getGroupCode());

            // 查询 DB 中该业务键对应的专业组（带缓存，null 也会缓存表示查过但不存在）
            AdmissionGroup existingGroup = existingGroupCache.get(groupKey);
            if (existingGroup == null && !existingGroupCache.containsKey(groupKey)) {
                existingGroup = admissionGroupMapper.selectOne(
                        Wrappers.lambdaQuery(AdmissionGroup.class)
                                .eq(AdmissionGroup::getUniversityId, university.getId())
                                .eq(AdmissionGroup::getYear, dto.getYear())
                                .eq(AdmissionGroup::getProvince, dto.getProvince())
                                .eq(AdmissionGroup::getBatch, dto.getBatch())
                                .eq(AdmissionGroup::getGroupCode, dto.getGroupCode())
                                .eq(AdmissionGroup::getIsDeleted, false));
                existingGroupCache.put(groupKey, existingGroup);
            }

            // 若专业组已存在，查出 DB 中已有专业代码，检查冲突（不跳过，直接报错）
            if (existingGroup != null) {
                Integer groupId = existingGroup.getId();
                Set<String> dbMajorCodes = existingMajorCodesCache.get(groupId);
                if (dbMajorCodes == null) {
                    List<AdmissionMajorScore> existingMajors = admissionMajorScoreMapper.selectList(
                            Wrappers.lambdaQuery(AdmissionMajorScore.class)
                                    .eq(AdmissionMajorScore::getGroupId, groupId)
                                    .eq(AdmissionMajorScore::getIsDeleted, false)
                                    .select(AdmissionMajorScore::getMajorCode));
                    dbMajorCodes = existingMajors.stream()
                            .map(AdmissionMajorScore::getMajorCode)
                            .collect(Collectors.toSet());
                    existingMajorCodesCache.put(groupId, dbMajorCodes);
                }
                if (dbMajorCodes.contains(dto.getMajorCode())) {
                    errors.add("第" + rowNum + "行: 专业代码[" + dto.getMajorCode() + "]在数据库该专业组下已存在");
                    continue;
                }
            }

            // ---- Excel 内同组专业代码重复检查 ----
            Set<String> excelCodes = groupMajorCodes.computeIfAbsent(groupKey, k -> new HashSet<>());
            if (excelCodes.contains(dto.getMajorCode())) {
                errors.add("第" + rowNum + "行: 专业代码[" + dto.getMajorCode() + "]在同一专业组内重复");
                continue;
            }
            excelCodes.add(dto.getMajorCode());
        }

        // 错误条数限制 + 校验失败则全部不插入
        if (!errors.isEmpty()) {
            String detail = errors.size() > 20
                    ? String.join("; ", errors.subList(0, 20)) + " 等" + errors.size() + "条错误"
                    : String.join("; ", errors);
            throw new BusinessException(400, "数据校验失败：" + detail);
        }

        // ==================== 3. 第二轮：全量插入（事务内，校验全过后才执行） ====================
        // 插入阶段不再做任何跳过/校验，保证"要么全成功，要么全失败"
        final OffsetDateTime now = OffsetDateTime.now();
        new TransactionTemplate(transactionManager).execute(status -> {
            doInsert(dataList, universityCache, existingGroupCache, now);
            return null;
        });

        log.info("导入专业组数据成功，总行数={}", dataList.size());
    }

    /**
     * 执行实际插入：按专业组分组，新建或更新专业组，再全量插入专业明细。
     * 校验阶段已确认无冲突，此处不再 skip。
     */
    private void doInsert(List<AdmissionImportDTO> dataList,
                          Map<String, University> universityCache,
                          Map<String, AdmissionGroup> existingGroupCache,
                          OffsetDateTime now) {
        // 按专业组业务键分组
        Map<String, List<AdmissionImportDTO>> groupedData = new LinkedHashMap<>();
        for (AdmissionImportDTO dto : dataList) {
            String groupKey = String.format("%s_%d_%s_%s_%s",
                    dto.getUniversityName().trim(), dto.getYear(), dto.getProvince(),
                    dto.getBatch(), dto.getGroupCode());
            groupedData.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(dto);
        }

        for (Map.Entry<String, List<AdmissionImportDTO>> entry : groupedData.entrySet()) {
            List<AdmissionImportDTO> rows = entry.getValue();
            AdmissionImportDTO firstRow = rows.get(0);
            University university = universityCache.get(firstRow.getUniversityName().trim());

            // 查询或创建专业组
            AdmissionGroup existingGroup = existingGroupCache.get(entry.getKey());
            Integer groupId;
            if (existingGroup == null) {
                AdmissionGroup group = AdmissionGroup.builder()
                        .universityId(university.getId())
                        .universityName(university.getName())
                        .cityName(university.getCityName())
                        .year(firstRow.getYear())
                        .province(firstRow.getProvince())
                        .batch(firstRow.getBatch())
                        .enrollmentCode(firstRow.getEnrollmentCode())
                        .groupCode(firstRow.getGroupCode())
                        .groupName(StringUtils.hasText(firstRow.getGroupName()) ? firstRow.getGroupName() : firstRow.getGroupCode())
                        .subjects(parseList(firstRow.getSubjectsStr()))
                        .requirementType(StringUtils.hasText(firstRow.getRequirementType()) ? firstRow.getRequirementType() : "不限")
                        .description(firstRow.getGroupDescription())
                        .isDeleted(false)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                admissionGroupMapper.insert(group);
                groupId = group.getId();
            } else {
                groupId = existingGroup.getId();
                // 仅当有值时更新组字段
                if (StringUtils.hasText(firstRow.getEnrollmentCode())) {
                    existingGroup.setEnrollmentCode(firstRow.getEnrollmentCode());
                }
                if (StringUtils.hasText(firstRow.getGroupName())) {
                    existingGroup.setGroupName(firstRow.getGroupName());
                }
                if (StringUtils.hasText(firstRow.getSubjectsStr())) {
                    existingGroup.setSubjects(parseList(firstRow.getSubjectsStr()));
                }
                if (StringUtils.hasText(firstRow.getRequirementType())) {
                    existingGroup.setRequirementType(firstRow.getRequirementType());
                }
                if (StringUtils.hasText(firstRow.getGroupDescription())) {
                    existingGroup.setDescription(firstRow.getGroupDescription());
                }
                existingGroup.setUpdatedAt(now);
                admissionGroupMapper.updateById(existingGroup);
            }

            // 插入专业明细（校验阶段已确认无冲突，全量插入，不跳过）
            for (AdmissionImportDTO row : rows) {
                List<String> constraintsList = parseList(row.getConstraintsStr());
                AdmissionMajorScore majorScore = AdmissionMajorScore.builder()
                        .groupId(groupId)
                        .majorCode(row.getMajorCode())
                        .majorName(row.getMajorName())
                        .educationLevel(row.getEducationLevel())
                        .duration(row.getDuration())
                        .tuition(row.getTuition())
                        .description(row.getMajorDescription())
                        .admissionCount(row.getAdmissionCount())
                        .minScore(row.getMinScore())
                        .minRank(row.getMinRank())
                        .avgScore(row.getAvgScore())
                        .avgRank(row.getAvgRank())
                        .maxScore(row.getMaxScore())
                        .maxRank(row.getMaxRank())
                        .constraints(constraintsList.isEmpty() ? null : constraintsList)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                admissionMajorScoreMapper.insert(majorScore);
            }
        }
    }

    /**
     * 将逗号分隔字符串解析为列表
     */
    private List<String> parseList(String str) {
        List<String> result = new ArrayList<>();
        if (StringUtils.hasText(str)) {
            String[] arr = str.split("[,，]");
            for (String s : arr) {
                if (StringUtils.hasText(s.trim())) {
                    result.add(s.trim());
                }
            }
        }
        return result;
    }

    @Override
    public Integer recalcAll() {
        Integer count = admissionGroupMapper.recalcAllGroups();
        log.info("全量重算聚合数据完成，处理专业组数量={}", count);
        return count;
    }

    /**
     * 通过大学名称查询大学信息（id + cityName）
     */
    private University getUniversityByName(String universityName) {
        if (!StringUtils.hasText(universityName)) {
            throw new BusinessException(400, "大学名称不能为空");
        }
        University university = universityMapper.selectIdAndCityByName(universityName.trim());
        if (university == null) {
            throw new BusinessException(400, "大学[" + universityName + "]不存在或已禁用");
        }
        return university;
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
        vo.setId(entity.getId());
        vo.setUniversityId(entity.getUniversityId());
        vo.setUniversityName(entity.getUniversityName());
        vo.setCityName(entity.getCityName());
        vo.setYear(entity.getYear());
        vo.setProvince(entity.getProvince());
        vo.setBatch(entity.getBatch());
        vo.setEnrollmentCode(entity.getEnrollmentCode());
        vo.setGroupCode(entity.getGroupCode());
        vo.setGroupName(entity.getGroupName());
        vo.setSubjects(entity.getSubjects());
        vo.setRequirementType(entity.getRequirementType());
        vo.setMajorCount(entity.getMajorCount());
        vo.setAdmissionCount(entity.getAdmissionCount());
        vo.setMinScore(entity.getMinScore());
        vo.setMinRank(entity.getMinRank());
        vo.setAvgScore(entity.getAvgScore());
        vo.setIsDeleted(entity.getIsDeleted());
        // 优先使用冗余字段，兼容旧数据
        if (!StringUtils.hasText(vo.getUniversityName())) {
            vo.setUniversityName(universityNameMap.get(entity.getUniversityId()));
        }
        return vo;
    }

    private AdmissionGroupDetailVO convertToDetailVO(AdmissionGroup entity) {
        AdmissionGroupDetailVO vo = new AdmissionGroupDetailVO();
        vo.setId(entity.getId());
        vo.setUniversityId(entity.getUniversityId());
        vo.setUniversityName(entity.getUniversityName());
        vo.setCityName(entity.getCityName());
        vo.setYear(entity.getYear());
        vo.setProvince(entity.getProvince());
        vo.setBatch(entity.getBatch());
        vo.setEnrollmentCode(entity.getEnrollmentCode());
        vo.setGroupCode(entity.getGroupCode());
        vo.setGroupName(entity.getGroupName());
        vo.setSubjects(entity.getSubjects());
        vo.setRequirementType(entity.getRequirementType());
        vo.setDescription(entity.getDescription());
        vo.setConstraints(entity.getConstraints());
        vo.setMajorCount(entity.getMajorCount());
        vo.setCategoryCount(entity.getCategoryCount());
        vo.setAdmissionCount(entity.getAdmissionCount());
        vo.setMinScore(entity.getMinScore());
        vo.setMinRank(entity.getMinRank());
        vo.setAvgScore(entity.getAvgScore());
        vo.setAvgRank(entity.getAvgRank());
        vo.setMaxScore(entity.getMaxScore());
        vo.setMaxRank(entity.getMaxRank());
        vo.setIsDeleted(entity.getIsDeleted());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());

        // 兼容旧数据：如果冗余字段为空，从大学表查询
        if (!StringUtils.hasText(vo.getUniversityName()) || !StringUtils.hasText(vo.getCityName())) {
            University university = universityMapper.selectById(entity.getUniversityId());
            if (university != null) {
                if (!StringUtils.hasText(vo.getUniversityName())) {
                    vo.setUniversityName(university.getName());
                }
                if (!StringUtils.hasText(vo.getCityName())) {
                    vo.setCityName(university.getCityName());
                }
            }
        }

        return vo;
    }
}
