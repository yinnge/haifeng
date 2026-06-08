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
import com.haifeng.common.entity.university.University;
import com.haifeng.common.entity.university.UniversityGuide;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.UniversityGuideMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import com.alibaba.excel.EasyExcel;
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
import java.util.stream.Collectors;

/**
 * 院校适应指南Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UniversityGuideServiceImpl implements UniversityGuideService {

    private final UniversityGuideMapper universityGuideMapper;
    private final UniversityMapper universityMapper;

    private static final Map<Integer, String> SHEET_TO_FIELD = new LinkedHashMap<>();
    private static final Map<String, BiConsumer<UniversityGuide, Map<String, Object>>> JSONB_SETTERERS = new HashMap<>();

    static {
        SHEET_TO_FIELD.put(1, "classDormSocial");
        SHEET_TO_FIELD.put(2, "financialAid");
        SHEET_TO_FIELD.put(3, "lifeServices");
        SHEET_TO_FIELD.put(4, "dormitoryServices");
        SHEET_TO_FIELD.put(5, "campusSecurity");
        SHEET_TO_FIELD.put(6, "campusEvents");
        SHEET_TO_FIELD.put(7, "campusFacilities");
        SHEET_TO_FIELD.put(8, "campusTransportation");
        SHEET_TO_FIELD.put(9, "studentOrganizations");
        SHEET_TO_FIELD.put(10, "academicSupportResources");
        SHEET_TO_FIELD.put(11, "healthServices");
        SHEET_TO_FIELD.put(12, "academicGuidance");
        SHEET_TO_FIELD.put(13, "majorTransferConstriction");
        SHEET_TO_FIELD.put(14, "majorTransferGuidelines");

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
    }

    @Override
    public IPage<UniversityGuideListVO> page(UniversityGuideQueryDTO dto) {
        Page<UniversityGuide> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<UniversityGuide> wrapper = new LambdaQueryWrapper<>();
        // 只查询未删除的（status != 0）
        wrapper.ne(UniversityGuide::getStatus, (short) 0);

        // 状态筛选
        if (dto.getStatus() != null) {
            wrapper.eq(UniversityGuide::getStatus, dto.getStatus());
        }

        // 按createdAt降序排列
        wrapper.orderByDesc(UniversityGuide::getCreatedAt);

        IPage<UniversityGuide> guidePage = universityGuideMapper.selectPage(page, wrapper);

        return guidePage.convert(guide -> {
            UniversityGuideListVO vo = new UniversityGuideListVO();
            vo.setId(guide.getId());
            vo.setUniversityId(guide.getUniversityId());
            vo.setCustomTags(guide.getCustomTags());
            vo.setRemark(guide.getRemark());
            vo.setStatus(guide.getStatus() != null ? guide.getStatus().intValue() : null);
            vo.setCreatedAt(guide.getCreatedAt() != null ? guide.getCreatedAt().toLocalDateTime() : null);

            // 查询院校名称
            University university = universityMapper.selectById(guide.getUniversityId());
            if (university != null) {
                vo.setUniversityName(university.getName());
            }

            return vo;
        });
    }

    @Override
    public UniversityGuideDetailVO detail(Long id) {
        UniversityGuide guide = universityGuideMapper.selectById(id);
        if (guide == null || guide.getStatus() == 0) {
            throw new BusinessException(404, "院校适应指南不存在");
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
            throw new BusinessException(404, "关联院校不存在");
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
        if (guide == null || guide.getStatus() == 0) {
            throw new BusinessException(404, "院校适应指南不存在");
        }

        guide.setCustomTags(dto.getCustomTags());
        guide.setCampusFacilities(dto.getCampusFacilities());
        guide.setDormitoryServices(dto.getDormitoryServices());
        guide.setCampusTransportation(dto.getCampusTransportation());
        guide.setAcademicGuidance(dto.getAcademicGuidance());
        guide.setMajorTransferGuidelines(dto.getMajorTransferGuidelines());
        guide.setMajorTransferConstriction(dto.getMajorTransferConstriction());
        guide.setAcademicSupportResources(dto.getAcademicSupportResources());
        guide.setStudentOrganizations(dto.getStudentOrganizations());
        guide.setCampusEvents(dto.getCampusEvents());
        guide.setClassDormSocial(dto.getClassDormSocial());
        guide.setFinancialAid(dto.getFinancialAid());
        guide.setCampusSecurity(dto.getCampusSecurity());
        guide.setHealthServices(dto.getHealthServices());
        guide.setLifeServices(dto.getLifeServices());
        guide.setRemark(dto.getRemark());
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
            throw new BusinessException(404, "院校适应指南不存在");
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
            throw new BusinessException(404, "院校适应指南不存在");
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
            throw new BusinessException(404, "院校适应指南不存在");
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
    public void importGuide(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }

        try {
            byte[] fileBytes = file.getBytes();

            // Step 1: Read Sheet0 - main data
            List<UniversityGuideExcelDTO> mainDataList;
            try (InputStream is = new ByteArrayInputStream(fileBytes)) {
                mainDataList = EasyExcel.read(is)
                        .head(UniversityGuideExcelDTO.class)
                        .sheet(0)
                        .doReadSync();
            }

            if (mainDataList == null || mainDataList.isEmpty()) {
                throw new BusinessException(400, "Sheet0中没有任何数据");
            }

            // Step 2: Read Sheet1-14 - JSONB data
            Map<String, Map<String, Map<String, List<String>>>> jsonbDataMap = buildJsonbDataMap(fileBytes);

            // Step 3: Process each row from Sheet0
            List<String> errors = new ArrayList<>();
            OffsetDateTime now = OffsetDateTime.now();
            int successCount = 0;

            for (int i = 0; i < mainDataList.size(); i++) {
                int rowNum = i + 2;
                UniversityGuideExcelDTO dto = mainDataList.get(i);

                if (!StringUtils.hasText(dto.getUniversityName())) {
                    errors.add("第" + rowNum + "行: 院校名称不能为空");
                    continue;
                }

                University university = universityMapper.selectOne(
                        new LambdaQueryWrapper<University>()
                                .eq(University::getName, dto.getUniversityName())
                                .ne(University::getStatus, (short) 0));

                if (university == null) {
                    errors.add("第" + rowNum + "行: 院校[" + dto.getUniversityName() + "]不存在");
                    continue;
                }

                UniversityGuide existingGuide = universityGuideMapper.selectOne(
                        new LambdaQueryWrapper<UniversityGuide>()
                                .eq(UniversityGuide::getUniversityId, university.getId())
                                .ne(UniversityGuide::getStatus, (short) 0));

                Map<String, Map<String, List<String>>> univJsonb = jsonbDataMap.get(dto.getUniversityName());

                if (existingGuide != null) {
                    existingGuide.setCustomTags(dto.getCustomTags());
                    existingGuide.setRemark(dto.getRemark());
                    if (dto.getStatus() != null) {
                        existingGuide.setStatus(dto.getStatus().shortValue());
                    }
                    setJsonbFields(existingGuide, univJsonb);
                    existingGuide.setUpdatedAt(now);
                    universityGuideMapper.updateById(existingGuide);
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
                    setJsonbFields(guide, univJsonb);
                    universityGuideMapper.insert(guide);
                }
                successCount++;
            }

            if (!errors.isEmpty()) {
                String errorMsg = String.format("导入完成，成功%d条，失败%d条。错误信息：%s",
                        successCount, errors.size(), String.join("; ", errors));
                throw new BusinessException(400, errorMsg);
            }

            log.info("导入院校适应指南数据成功: 共{}条", successCount);

        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "读取Excel文件失败: " + e.getMessage());
        }
    }

    private Map<String, Map<String, Map<String, List<String>>>> buildJsonbDataMap(byte[] fileBytes) {
        Map<String, Map<String, Map<String, List<String>>>> result = new HashMap<>();

        for (Map.Entry<Integer, String> entry : SHEET_TO_FIELD.entrySet()) {
            int sheetNo = entry.getKey();
            String fieldName = entry.getValue();

            List<List<String>> rows;
            try (InputStream is = new ByteArrayInputStream(fileBytes)) {
                rows = readSheetRows(is, sheetNo);
            } catch (Exception e) {
                log.warn("Sheet{}（{}）读取失败，已跳过: {}", sheetNo, fieldName, e.getMessage());
                continue;
            }

            if (rows == null || rows.size() < 2) continue;

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

    private List<List<String>> readSheetRows(InputStream is, int sheetNo) {
        try {
            List<Object> rawRows = EasyExcel.read(is).sheet(sheetNo).doReadSync();
            List<List<String>> result = new ArrayList<>();
            for (Object rawRow : rawRows) {
                if (rawRow instanceof List) {
                    List<String> row = new ArrayList<>();
                    for (Object cell : (List<?>) rawRow) {
                        row.add(cell != null ? cell.toString().trim() : "");
                    }
                    result.add(row);
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("读取Sheet{}失败: {}", sheetNo, e.getMessage());
            return Collections.emptyList();
        }
    }

    private void setJsonbFields(UniversityGuide guide, Map<String, Map<String, List<String>>> jsonbData) {
        if (jsonbData == null || jsonbData.isEmpty()) return;

        for (Map.Entry<String, Map<String, List<String>>> entry : jsonbData.entrySet()) {
            String fieldName = entry.getKey();
            Map<String, List<String>> fieldData = entry.getValue();
            if (fieldData == null || fieldData.isEmpty()) continue;

            BiConsumer<UniversityGuide, Map<String, Object>> setter = JSONB_SETTERERS.get(fieldName);
            if (setter != null) {
                setter.accept(guide, new LinkedHashMap<>(fieldData));
            }
        }
    }

    private static String safeStr(Object obj) {
        return obj != null ? obj.toString().trim() : "";
    }
}
