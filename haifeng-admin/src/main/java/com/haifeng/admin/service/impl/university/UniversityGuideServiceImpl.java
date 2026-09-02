package com.haifeng.admin.service.impl.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.university.UniversityGuideAddDTO;
import com.haifeng.admin.dto.university.UniversityGuideQueryDTO;
import com.haifeng.admin.dto.university.UniversityGuideUpdateDTO;
import com.haifeng.admin.service.university.UniversityGuideService;
import com.haifeng.admin.vo.university.UniversityGuideDetailVO;
import com.haifeng.admin.vo.university.UniversityGuideListVO;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.entity.university.UniversityGuide;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.UniversityGuideMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.SnowflakeIdGenerator;
import com.alibaba.excel.EasyExcel;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import com.haifeng.admin.excel.university.UniversityGuideExcelDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 院校适应指南Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UniversityGuideServiceImpl implements UniversityGuideService {

    private final UniversityGuideMapper universityGuideMapper;

    private static final int MAX_IMPORT_ROWS = 1000;
    /** 导入报错信息最多展示条数，避免单条 msg 过长（完整错误见后端日志） */
    private static final int MAX_ERROR_DISPLAY = 50;
    /** 主表 Sheet 名称：文件缺少该 Sheet 时显式报错，避免静默 total=0 假成功 */
    private static final String MAIN_SHEET_NAME = "适应指南-主表";
    private final UniversityMapper universityMapper;

    private static final Map<String, String> SHEET_TO_FIELD = new LinkedHashMap<>();
    private static final Map<String, BiConsumer<UniversityGuide, Map<String, Object>>> JSONB_SETTERERS = new HashMap<>();

    /**
     * 与 JSONB_SETTERERS 一一对应的取值器，用于读取库中该 JSONB 字段的当前值。
     * 仅补空模式下需要先取值判断该字段是否为空，为空才写入。
     */
    private static final Map<String, Function<UniversityGuide, Map<String, Object>>> JSONB_GETTERS = new HashMap<>();

    static {
        SHEET_TO_FIELD.put("班级与宿舍社交", "classDormSocial");
        SHEET_TO_FIELD.put("奖助勤贷与权益保障", "financialAid");
        SHEET_TO_FIELD.put("生活服务", "lifeServices");
        SHEET_TO_FIELD.put("水电网与宿舍管理", "dormitoryServices");
        SHEET_TO_FIELD.put("校园安全与应急处理", "campusSecurity");
        SHEET_TO_FIELD.put("校园活动与竞赛", "campusEvents");
        SHEET_TO_FIELD.put("校园设施", "campusFacilities");
        SHEET_TO_FIELD.put("校园通勤与校外交通", "campusTransportation");
        SHEET_TO_FIELD.put("学生组织与社团", "studentOrganizations");
        SHEET_TO_FIELD.put("学习支持资源", "academicSupportResources");
        SHEET_TO_FIELD.put("医保与心理健康", "healthServices");
        SHEET_TO_FIELD.put("专业与课程核心信息", "academicGuidance");
        SHEET_TO_FIELD.put("转专业限制", "majorTransferConstriction");
        SHEET_TO_FIELD.put("转专业原则", "majorTransferGuidelines");

        JSONB_SETTERERS.put("classDormSocial", UniversityGuide::setClassDormSocial);
        JSONB_SETTERERS.put("financialAid", UniversityGuide::setFinancialAid);
        JSONB_SETTERERS.put("lifeServices", UniversityGuide::setLifeServices);
        JSONB_SETTERERS.put("dormitoryServices", UniversityGuide::setDormitoryServices);
        JSONB_SETTERERS.put("campusSecurity", UniversityGuide::setCampusSecurity);
        JSONB_SETTERERS.put("campusEvents", UniversityGuide::setCampusEvents);
        JSONB_SETTERERS.put("campusFacilities", UniversityGuide::setCampusFacilities);
        JSONB_SETTERERS.put("campusTransportation", UniversityGuide::setCampusTransportation);
        JSONB_SETTERERS.put("studentOrganizations", UniversityGuide::setStudentOrganizations);
        JSONB_SETTERERS.put("academicSupportResources", UniversityGuide::setAcademicSupportResources);
        JSONB_SETTERERS.put("healthServices", UniversityGuide::setHealthServices);
        JSONB_SETTERERS.put("academicGuidance", UniversityGuide::setAcademicGuidance);
        JSONB_SETTERERS.put("majorTransferConstriction", UniversityGuide::setMajorTransferConstriction);
        JSONB_SETTERERS.put("majorTransferGuidelines", UniversityGuide::setMajorTransferGuidelines);

        JSONB_GETTERS.put("classDormSocial", UniversityGuide::getClassDormSocial);
        JSONB_GETTERS.put("financialAid", UniversityGuide::getFinancialAid);
        JSONB_GETTERS.put("lifeServices", UniversityGuide::getLifeServices);
        JSONB_GETTERS.put("dormitoryServices", UniversityGuide::getDormitoryServices);
        JSONB_GETTERS.put("campusSecurity", UniversityGuide::getCampusSecurity);
        JSONB_GETTERS.put("campusEvents", UniversityGuide::getCampusEvents);
        JSONB_GETTERS.put("campusFacilities", UniversityGuide::getCampusFacilities);
        JSONB_GETTERS.put("campusTransportation", UniversityGuide::getCampusTransportation);
        JSONB_GETTERS.put("studentOrganizations", UniversityGuide::getStudentOrganizations);
        JSONB_GETTERS.put("academicSupportResources", UniversityGuide::getAcademicSupportResources);
        JSONB_GETTERS.put("healthServices", UniversityGuide::getHealthServices);
        JSONB_GETTERS.put("academicGuidance", UniversityGuide::getAcademicGuidance);
        JSONB_GETTERS.put("majorTransferConstriction", UniversityGuide::getMajorTransferConstriction);
        JSONB_GETTERS.put("majorTransferGuidelines", UniversityGuide::getMajorTransferGuidelines);
    }

    @Override
    public IPage<UniversityGuideListVO> page(UniversityGuideQueryDTO dto) {
        Page<UniversityGuide> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<UniversityGuide> wrapper = new LambdaQueryWrapper<>();

        // 院校名称模糊搜索
        if (StringUtils.hasText(dto.getUniversityName())) {
            List<Long> matchedUnivIds = universityMapper.selectList(
                new LambdaQueryWrapper<University>()
                    .like(University::getName, dto.getUniversityName())
                    .select(University::getId)
            ).stream().map(University::getId).collect(Collectors.toList());
            if (matchedUnivIds.isEmpty()) {
                wrapper.in(UniversityGuide::getUniversityId, List.of(-1L));
            } else {
                wrapper.in(UniversityGuide::getUniversityId, matchedUnivIds);
            }
        }

        // 状态筛选（管理员可查看所有状态）
        if (dto.getStatus() != null) {
            wrapper.eq(UniversityGuide::getStatus, dto.getStatus());
        }

        // 按createdAt降序排列
        wrapper.orderByDesc(UniversityGuide::getCreatedAt);

        IPage<UniversityGuide> guidePage = universityGuideMapper.selectPage(page, wrapper);

        // 批量查询院校名称（避免 N+1）
        List<UniversityGuide> records = guidePage.getRecords();
        Set<Long> univIds = records.stream()
                .map(UniversityGuide::getUniversityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> univNameMap = new HashMap<>();
        if (!univIds.isEmpty()) {
            List<University> universities = universityMapper.selectBatchIds(univIds);
            univNameMap = universities.stream()
                    .collect(Collectors.toMap(University::getId, University::getName, (a, b) -> a));
        }

        Map<Long, String> finalUnivNameMap = univNameMap;
        return guidePage.convert(guide -> {
            UniversityGuideListVO vo = new UniversityGuideListVO();
            vo.setId(guide.getId());
            vo.setUniversityId(guide.getUniversityId());
            vo.setCustomTags(guide.getCustomTags());
            vo.setRemark(guide.getRemark());
            vo.setStatus(guide.getStatus() != null ? guide.getStatus().intValue() : null);
            vo.setCreatedAt(guide.getCreatedAt() != null ? guide.getCreatedAt().toLocalDateTime() : null);
            vo.setUniversityName(finalUnivNameMap.get(guide.getUniversityId()));
            return vo;
        });
    }

    @Override
    public UniversityGuideDetailVO detail(Long id) {
        UniversityGuide guide = universityGuideMapper.selectById(id);
        if (guide == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "院校适应指南不存在");
        }

        UniversityGuideDetailVO vo = new UniversityGuideDetailVO();
        vo.setId(guide.getId());
        vo.setUniversityId(guide.getUniversityId());
        vo.setCustomTags(guide.getCustomTags());
        vo.setCampusFacilities(guide.getCampusFacilities());
        vo.setDormitoryServices(guide.getDormitoryServices());
        vo.setCampusTransportation(guide.getCampusTransportation());
        vo.setAcademicGuidance(guide.getAcademicGuidance());
        vo.setMajorTransferGuidelines(guide.getMajorTransferGuidelines());
        vo.setMajorTransferConstriction(guide.getMajorTransferConstriction());
        vo.setAcademicSupportResources(guide.getAcademicSupportResources());
        vo.setStudentOrganizations(guide.getStudentOrganizations());
        vo.setCampusEvents(guide.getCampusEvents());
        vo.setClassDormSocial(guide.getClassDormSocial());
        vo.setFinancialAid(guide.getFinancialAid());
        vo.setCampusSecurity(guide.getCampusSecurity());
        vo.setHealthServices(guide.getHealthServices());
        vo.setLifeServices(guide.getLifeServices());
        vo.setRemark(guide.getRemark());
        vo.setStatus(guide.getStatus() != null ? guide.getStatus().intValue() : null);
        vo.setCreatedAt(guide.getCreatedAt() != null ? guide.getCreatedAt().toLocalDateTime() : null);
        vo.setUpdatedAt(guide.getUpdatedAt() != null ? guide.getUpdatedAt().toLocalDateTime() : null);

        // 查询院校名称
        University university = universityMapper.selectById(guide.getUniversityId());
        if (university != null) {
            vo.setUniversityName(university.getName());
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(UniversityGuideAddDTO dto) {
        // 校验院校是否存在
        University university = universityMapper.selectById(dto.getUniversityId());
        if (university == null || university.getStatus() == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "关联院校不存在");
        }

        // 检查该院校是否已有指南（1:1关系）
        LambdaQueryWrapper<UniversityGuide> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UniversityGuide::getUniversityId, dto.getUniversityId())
               .ne(UniversityGuide::getStatus, (short) 0);
        if (universityGuideMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "该院校已存在适应指南，请直接修改");
        }

        OffsetDateTime now = OffsetDateTime.now();
        Long id = SnowflakeIdGenerator.nextId();

        UniversityGuide guide = UniversityGuide.builder()
                .id(id)
                .universityId(dto.getUniversityId())
                .customTags(dto.getCustomTags())
                .campusFacilities(dto.getCampusFacilities())
                .dormitoryServices(dto.getDormitoryServices())
                .campusTransportation(dto.getCampusTransportation())
                .academicGuidance(dto.getAcademicGuidance())
                .majorTransferGuidelines(dto.getMajorTransferGuidelines())
                .majorTransferConstriction(dto.getMajorTransferConstriction())
                .academicSupportResources(dto.getAcademicSupportResources())
                .studentOrganizations(dto.getStudentOrganizations())
                .campusEvents(dto.getCampusEvents())
                .classDormSocial(dto.getClassDormSocial())
                .financialAid(dto.getFinancialAid())
                .campusSecurity(dto.getCampusSecurity())
                .healthServices(dto.getHealthServices())
                .lifeServices(dto.getLifeServices())
                .remark(dto.getRemark())
                .status((short) 1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        universityGuideMapper.insert(guide);

        log.info("新增院校适应指南成功: id={}, universityId={}", id, dto.getUniversityId());
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, UniversityGuideUpdateDTO dto) {
        UniversityGuide guide = universityGuideMapper.selectById(id);
        if (guide == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "院校适应指南不存在");
        }

        if (dto.getCustomTags() != null) guide.setCustomTags(dto.getCustomTags());
        if (dto.getCampusFacilities() != null) guide.setCampusFacilities(dto.getCampusFacilities());
        if (dto.getDormitoryServices() != null) guide.setDormitoryServices(dto.getDormitoryServices());
        if (dto.getCampusTransportation() != null) guide.setCampusTransportation(dto.getCampusTransportation());
        if (dto.getAcademicGuidance() != null) guide.setAcademicGuidance(dto.getAcademicGuidance());
        if (dto.getMajorTransferGuidelines() != null) guide.setMajorTransferGuidelines(dto.getMajorTransferGuidelines());
        if (dto.getMajorTransferConstriction() != null) guide.setMajorTransferConstriction(dto.getMajorTransferConstriction());
        if (dto.getAcademicSupportResources() != null) guide.setAcademicSupportResources(dto.getAcademicSupportResources());
        if (dto.getStudentOrganizations() != null) guide.setStudentOrganizations(dto.getStudentOrganizations());
        if (dto.getCampusEvents() != null) guide.setCampusEvents(dto.getCampusEvents());
        if (dto.getClassDormSocial() != null) guide.setClassDormSocial(dto.getClassDormSocial());
        if (dto.getFinancialAid() != null) guide.setFinancialAid(dto.getFinancialAid());
        if (dto.getCampusSecurity() != null) guide.setCampusSecurity(dto.getCampusSecurity());
        if (dto.getHealthServices() != null) guide.setHealthServices(dto.getHealthServices());
        if (dto.getLifeServices() != null) guide.setLifeServices(dto.getLifeServices());
        if (dto.getRemark() != null) guide.setRemark(dto.getRemark());
        if (dto.getStatus() != null) {
            guide.setStatus(dto.getStatus().shortValue());
        }
        guide.setUpdatedAt(OffsetDateTime.now());

        universityGuideMapper.updateById(guide);

        log.info("修改院校适应指南成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Short status) {
        UniversityGuide guide = universityGuideMapper.selectById(id);
        if (guide == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "院校适应指南不存在");
        }

        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "状态值无效，仅支持0（下架）或1（展示）");
        }

        guide.setStatus(status);
        guide.setUpdatedAt(OffsetDateTime.now());
        universityGuideMapper.updateById(guide);

        log.info("修改院校适应指南状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        UniversityGuide guide = universityGuideMapper.selectById(id);
        if (guide == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "院校适应指南不存在");
        }

        // 软删除：status = 0
        guide.setStatus((short) 0);
        guide.setUpdatedAt(OffsetDateTime.now());
        universityGuideMapper.updateById(guide);

        log.info("软删除院校适应指南成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDelete(Long id) {
        UniversityGuide guide = universityGuideMapper.selectById(id);
        if (guide == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "院校适应指南不存在");
        }

        // 硬删除：物理删除
        universityGuideMapper.deleteById(id);

        log.info("硬删除院校适应指南成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "ID列表不能为空");
        }

        OffsetDateTime now = OffsetDateTime.now();
        int successCount = 0;

        for (Long id : ids) {
            UniversityGuide guide = universityGuideMapper.selectById(id);
            if (guide != null && guide.getStatus() != 0) {
                guide.setStatus((short) 0);
                guide.setUpdatedAt(now);
                universityGuideMapper.updateById(guide);
                successCount++;
            }
        }

        log.info("批量软删除院校适应指南成功: 请求数量={}, 实际删除数量={}", ids.size(), successCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchHardDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "ID列表不能为空");
        }

        int successCount = 0;

        for (Long id : ids) {
            UniversityGuide guide = universityGuideMapper.selectById(id);
            if (guide != null) {
                universityGuideMapper.deleteById(id);
                successCount++;
            }
        }

        log.info("批量硬删除院校适应指南成功: 请求数量={}, 实际删除数量={}", ids.size(), successCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importGuide(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }

        try {
            byte[] fileBytes = file.getBytes();

            // 主表 Sheet 存在性校验：文件缺少该 Sheet 时显式报错（列出可用 Sheet），
            // 避免 EasyExcel 按名字找不到 Sheet 时静默返回空列表、导致 total=0 假成功。
            List<String> sheetNames = listSheetNames(fileBytes);
            if (!sheetNames.isEmpty() && !sheetNames.contains(MAIN_SHEET_NAME)) {
                throw new BusinessException(400, "未找到名为「" + MAIN_SHEET_NAME
                        + "」的Sheet，请确认主表Sheet名称是否正确。当前文件包含的Sheet为："
                        + String.join("、", sheetNames));
            }

            // Step 1: Read "适应指南-主表" sheet - main data
            List<UniversityGuideExcelDTO> mainDataList;
            try (InputStream is = new ByteArrayInputStream(fileBytes)) {
                mainDataList = EasyExcel.read(is)
                        .head(UniversityGuideExcelDTO.class)
                        .sheet(MAIN_SHEET_NAME)
                        .doReadSync();
            } catch (RuntimeException e) {
                log.error("读取「" + MAIN_SHEET_NAME + "」Sheet失败", e);
                throw new BusinessException(400, "读取「" + MAIN_SHEET_NAME + "」Sheet失败，请确认sheet名称为「"
                        + MAIN_SHEET_NAME + "」且表头为：院校名称/自定义标签/备注/状态。详细: " + e.getMessage());
            }

            if (mainDataList != null && mainDataList.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "单次导入不能超过" + MAX_IMPORT_ROWS + "条记录");
            }

            if (mainDataList == null || mainDataList.isEmpty()) {
                log.info("「" + MAIN_SHEET_NAME + "」Sheet 为空，无需导入，直接跳过");
                return ImportResultVO.builder()
                        .total(0)
                        .success(0)
                        .failed(0)
                        .updated(0)
                        .errors(new ArrayList<>())
                        .build();
            }

            // Step 2: Read Sheet1-14 - JSONB data
            List<String> errors = new ArrayList<>();
            Map<String, Map<String, Map<String, List<String>>>> jsonbDataMap = buildJsonbDataMap(fileBytes, errors);

            // Step 3: Process each row from Sheet0
            OffsetDateTime now = OffsetDateTime.now();
            int insertCount = 0;
            int updateCount = 0;

            for (int i = 0; i < mainDataList.size(); i++) {
                int rowNum = i + 2;
                UniversityGuideExcelDTO dto = mainDataList.get(i);

                if (!StringUtils.hasText(dto.getUniversityName())) {
                    errors.add("第" + rowNum + "行: 院校名称不能为空");
                    continue;
                }

                // 与 JSONB 分类 Sheet 的第一列院校名称做精确匹配（统一 trim，避免前后空格导致匹配不上）
                String univName = dto.getUniversityName().trim();

                // 该行涉及的数据库操作整体包 try-catch：一旦失败即可带上行号转成行级错误，
                // 否则异常向上抛出后丢失行号，前端/管理员无法定位是哪一行出错。
                try {
                    University university = universityMapper.selectOne(
                            new LambdaQueryWrapper<University>()
                                    .eq(University::getName, univName)
                                    .ne(University::getStatus, (short) 0));

                    if (university == null) {
                        errors.add("第" + rowNum + "行: 院校[" + univName + "]不存在");
                        continue;
                    }

                    UniversityGuide existingGuide = universityGuideMapper.selectOne(
                            new LambdaQueryWrapper<UniversityGuide>()
                                    .eq(UniversityGuide::getUniversityId, university.getId())
                                    .ne(UniversityGuide::getStatus, (short) 0));

                    // 该院校在本批上传的分类 Sheet 中未匹配到数据：跳过 JSONB 补填，仅处理主表字段（customTags/remark）。
                    // 增量导入策略下允许分批上传，分类 Sheet 留空不报错，用户后续补数据再传。
                    Map<String, Map<String, List<String>>> univJsonb = jsonbDataMap.get(univName);

                    if (existingGuide != null) {
                        // 已存在：仅补齐数据库中为 NULL / 空 的字段，已有数据一律不覆盖
                        // （status 为控制字段且必有值，按"已有数据不覆盖"规则不再随导入变更）
                        if (isBlankList(existingGuide.getCustomTags())
                                && dto.getCustomTags() != null && !dto.getCustomTags().isEmpty()) {
                            existingGuide.setCustomTags(dto.getCustomTags());
                        }
                        if (existingGuide.getRemark() == null && StringUtils.hasText(dto.getRemark())) {
                            existingGuide.setRemark(dto.getRemark());
                        }
                        setJsonbFields(existingGuide, univJsonb, true);
                        existingGuide.setUpdatedAt(now);
                        universityGuideMapper.updateById(existingGuide);
                        updateCount++;
                    } else {
                        Long id = SnowflakeIdGenerator.nextId();
                        UniversityGuide guide = UniversityGuide.builder()
                                .id(id)
                                .universityId(university.getId())
                                .customTags(dto.getCustomTags())
                                .remark(dto.getRemark())
                                .status(dto.getStatus() != null ? dto.getStatus().shortValue() : (short) 1)
                                .createdAt(now)
                                .updatedAt(now)
                                .build();
                        setJsonbFields(guide, univJsonb, false);
                        universityGuideMapper.insert(guide);
                        insertCount++;
                    }
                } catch (Exception e) {
                    // 数据库操作异常（唯一约束冲突、字段超长、连接异常等）转为行级错误，保留行号
                    errors.add("第" + rowNum + "行: 数据库操作失败[" + univName + "]: " + e.getMessage());
                }
            }

            if (!errors.isEmpty()) {
                int shownCount = Math.min(errors.size(), MAX_ERROR_DISPLAY);
                String errorMsg = String.format(
                        "导入失败，共%d行数据存在错误（仅展示前%d条，完整错误请查看后端日志），已全部回滚。错误信息：%s",
                        errors.size(), shownCount,
                        String.join("; ", errors.subList(0, shownCount)));
                throw new BusinessException(400, errorMsg);
            }

            log.info("导入院校适应指南数据成功: 新增{}条, 补齐{}条", insertCount, updateCount);
            int total = mainDataList.size();
            int failed = 0; // 整批回滚，无部分成功
            return ImportResultVO.builder()
                    .total(total)
                    .success(total - failed)
                    .failed(failed)
                    .updated(updateCount)
                    .errors(errors)
                    .build();

        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "读取Excel文件失败: " + e.getMessage());
        }
    }

    private Map<String, Map<String, Map<String, List<String>>>> buildJsonbDataMap(byte[] fileBytes, List<String> errors) {
        Map<String, Map<String, Map<String, List<String>>>> result = new HashMap<>();

        for (Map.Entry<String, String> entry : SHEET_TO_FIELD.entrySet()) {
            String sheetName = entry.getKey();
            String fieldName = entry.getValue();

            List<List<String>> rows;
            try (InputStream is = new ByteArrayInputStream(fileBytes)) {
                rows = readSheetRows(is, sheetName);
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                // 分类 Sheet 缺失或无法读取：增量导入下允许留空，跳过不报错（仅记日志便于排查）
                log.warn("分类Sheet「{}」读取失败，跳过: {}", sheetName, e.getMessage());
                continue;
            }

            if (rows == null || rows.size() < 2) {
                // 该分类 Sheet 为空（或仅表头）：增量导入下允许留空，跳过不报错，用户后续补数据再传
                log.debug("分类Sheet「{}」无数据行，跳过", sheetName);
                continue;
            }

            List<String> headers = rows.get(0);

            for (int i = 1; i < rows.size(); i++) {
                List<String> row = rows.get(i);
                if (row == null || row.isEmpty() || !StringUtils.hasText(row.get(0))) continue;

                String univName = row.get(0).trim();
                Map<String, Map<String, List<String>>> univData = result.computeIfAbsent(univName, k -> new HashMap<>());
                Map<String, List<String>> fieldData = univData.computeIfAbsent(fieldName, k -> new LinkedHashMap<>());

                for (int j = 1; j < headers.size() && j < row.size(); j++) {
                    String header = safeStr(headers.get(j));
                    String value = safeStr(row.get(j));
                    if (!header.isEmpty() && !value.isEmpty()) {
                        List<String> items = Arrays.stream(value.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toList());
                        fieldData.put(header, items);
                    }
                }
            }
        }

        return result;
    }

    private List<List<String>> readSheetRows(InputStream is, String sheetName) {
        // headRowNumber(0)：把表头行也当数据读入（调用方用 rows.get(0) 作为表头）。
        // 不加时 EasyExcel 默认 headRowNumber=1，第一行（表头）会被当作 head 消费掉，不出现在结果里。
        // 另外：不指定 head class 时每行返回 LinkedHashMap<Integer,String>（列索引→单元格值），不是 List。
        List<Object> rawRows = EasyExcel.read(is).sheet(sheetName).headRowNumber(0).doReadSync();
        if (rawRows != null && rawRows.size() > MAX_IMPORT_ROWS) {
            throw new BusinessException(400, "单次导入不能超过" + MAX_IMPORT_ROWS + "条记录");
        }
        List<List<String>> result = new ArrayList<>();
        for (Object rawRow : rawRows) {
            if (rawRow instanceof List) {
                // 兜底分支：head(List.class) 等场景下返回 List 形态
                List<String> row = new ArrayList<>();
                for (Object cell : (List<?>) rawRow) {
                    row.add(cell != null ? cell.toString().trim() : "");
                }
                result.add(row);
            } else if (rawRow instanceof Map) {
                // 无 head class 时 EasyExcel 默认返回 Map<列索引, 单元格值>
                Map<?, ?> map = (Map<?, ?>) rawRow;
                if (map.isEmpty()) continue;
                int maxIdx = -1;
                for (Object key : map.keySet()) {
                    if (key instanceof Number) {
                        maxIdx = Math.max(maxIdx, ((Number) key).intValue());
                    }
                }
                if (maxIdx < 0) continue;
                List<String> row = new ArrayList<>(Collections.nCopies(maxIdx + 1, ""));
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (entry.getKey() instanceof Number) {
                        int idx = ((Number) entry.getKey()).intValue();
                        row.set(idx, entry.getValue() != null ? entry.getValue().toString().trim() : "");
                    }
                }
                result.add(row);
            }
        }
        return result;
    }

    /**
     * 枚举工作簿内所有 Sheet 名称，用于导入前校验主表 Sheet 是否存在。
     * 无法读取（文件损坏/不支持格式）时返回空列表，调用方据此跳过校验而非阻断导入。
     */
    private List<String> listSheetNames(byte[] fileBytes) {
        List<String> names = new ArrayList<>();
        try (InputStream is = new ByteArrayInputStream(fileBytes);
             Workbook workbook = WorkbookFactory.create(is)) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                names.add(workbook.getSheetName(i));
            }
        } catch (Exception e) {
            log.warn("枚举Sheet名称失败，跳过主表存在性校验: {}", e.getMessage());
        }
        return names;
    }

    /**
     * 将分类 Sheet 采集到的 JSONB 数据写入指南实体。
     *
     * @param onlyFillNull true=仅在该字段当前为空时才写入，已有数据一律不覆盖；
     *                     false=采集到数据就写入（新增记录时使用）
     */
    private void setJsonbFields(UniversityGuide guide, Map<String, Map<String, List<String>>> jsonbData,
                                boolean onlyFillNull) {
        if (jsonbData == null || jsonbData.isEmpty()) return;

        for (Map.Entry<String, Map<String, List<String>>> entry : jsonbData.entrySet()) {
            String fieldName = entry.getKey();
            Map<String, List<String>> fieldData = entry.getValue();
            if (fieldData == null || fieldData.isEmpty()) continue;

            BiConsumer<UniversityGuide, Map<String, Object>> setter = JSONB_SETTERERS.get(fieldName);
            if (setter == null) {
                continue;
            }
            if (onlyFillNull) {
                Function<UniversityGuide, Map<String, Object>> getter = JSONB_GETTERS.get(fieldName);
                Map<String, Object> current = getter != null ? getter.apply(guide) : null;
                // 注意：这些 JSONB 列建表带 DEFAULT '{}'，MP 插入时省略 null 列，
                // 库中实际存的是空对象而非 NULL，因此必须把 empty 也视为"空"
                if (current != null && !current.isEmpty()) {
                    continue;
                }
            }
            setter.accept(guide, new LinkedHashMap<>(fieldData));
        }
    }

    private static String safeStr(Object obj) {
        return obj != null ? obj.toString().trim() : "";
    }

    private boolean isBlankList(List<?> list) {
        return list == null || list.isEmpty();
    }
}
