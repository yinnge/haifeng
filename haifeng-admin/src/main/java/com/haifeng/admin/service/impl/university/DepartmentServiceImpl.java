package com.haifeng.admin.service.impl.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haifeng.admin.dto.university.DepartmentAddDTO;
import com.haifeng.admin.dto.university.DepartmentQueryDTO;
import com.haifeng.admin.dto.university.DepartmentUpdateDTO;
import com.haifeng.admin.service.university.DepartmentService;
import com.haifeng.admin.vo.university.DepartmentDetailVO;
import com.haifeng.admin.vo.university.DepartmentListVO;
import com.haifeng.common.entity.university.Department;
import com.haifeng.common.entity.university.DepartmentReport;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.DepartmentMapper;
import com.haifeng.common.mapper.university.DepartmentReportMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.SnowflakeIdGenerator;
import com.alibaba.excel.EasyExcel;
import com.haifeng.admin.excel.university.DepartmentExcelDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    private final DepartmentMapper departmentMapper;

    private static final int MAX_IMPORT_ROWS = 1000;
    private final DepartmentReportMapper departmentReportMapper;
    private final UniversityMapper universityMapper;

    private static final Map<String, String> SHEET_TO_FIELD = new LinkedHashMap<>();

    static {
        SHEET_TO_FIELD.put("基础信息", "baseInfo");
        SHEET_TO_FIELD.put("城市薪资", "citySalary");
        SHEET_TO_FIELD.put("考研方向", "postgraduate");
        SHEET_TO_FIELD.put("免责声明", "disclaimer");
        SHEET_TO_FIELD.put("就业前景", "prospects");
        SHEET_TO_FIELD.put("就业趋势", "trends");
        SHEET_TO_FIELD.put("概述", "overview");
        SHEET_TO_FIELD.put("职业路径", "career");
        SHEET_TO_FIELD.put("专业详情", "subjectsDetail");
        SHEET_TO_FIELD.put("专业薪资", "salary");
        SHEET_TO_FIELD.put("学科组成", "majorCompose");
    }

    @Override
    public IPage<DepartmentListVO> page(DepartmentQueryDTO dto) {
        Page<Department> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getUniversityName())) {
            wrapper.like(Department::getUniversityName, dto.getUniversityName());
        }
        if (StringUtils.hasText(dto.getDepartmentName())) {
            wrapper.like(Department::getDepartmentName, dto.getDepartmentName());
        }
        if (StringUtils.hasText(dto.getDepartmentType())) {
            wrapper.eq(Department::getDepartmentType, dto.getDepartmentType());
        }
        // 状态筛选（管理员可查看所有状态）
        if (dto.getStatus() != null) {
            wrapper.eq(Department::getStatus, dto.getStatus());
        }

        wrapper.orderByAsc(Department::getSortOrder).orderByDesc(Department::getCreatedAt);

        IPage<Department> deptPage = departmentMapper.selectPage(page, wrapper);

        return deptPage.convert(dept -> {
            DepartmentListVO vo = new DepartmentListVO();
            vo.setId(dept.getId());
            vo.setUniversityId(dept.getUniversityId());
            vo.setUniversityName(dept.getUniversityName());
            vo.setDepartmentName(dept.getDepartmentName());
            vo.setDepartmentType(dept.getDepartmentType());
            vo.setPageTitle(dept.getPageTitle());
            vo.setSortOrder(dept.getSortOrder());
            vo.setStatus(dept.getStatus() != null ? dept.getStatus().intValue() : null);
            vo.setCreatedAt(dept.getCreatedAt());
            return vo;
        });
    }

    @Override
    public DepartmentDetailVO detail(Long id) {
        Department dept = departmentMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "院系不存在");
        }

        DepartmentDetailVO vo = new DepartmentDetailVO();
        BeanUtils.copyProperties(dept, vo);
        vo.setStatus(dept.getStatus() != null ? dept.getStatus().intValue() : null);

        // 查询关联的报告
        DepartmentReport report = departmentReportMapper.selectByDepartmentId(id);
        if (report != null) {
            vo.setReportId(report.getId());
            vo.setSubtitle(report.getSubtitle());
            vo.setOverview(report.getOverview());
            vo.setSubjectsDetail(report.getSubjectsDetail());
            vo.setPostgraduate(report.getPostgraduate());
            vo.setCitySalary(report.getCitySalary());
            vo.setSalary(report.getSalary());
            vo.setCareer(report.getCareer());
            vo.setTrends(report.getTrends());
            vo.setProspects(report.getProspects());
            vo.setDisclaimer(report.getDisclaimer());
            vo.setMajorCompose(report.getMajorCompose());
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(DepartmentAddDTO dto) {
        University university = universityMapper.selectById(dto.getUniversityId());
        if (university == null || university.getStatus() == 0) {
            throw new BusinessException(400, "院校不存在");
        }

        if (departmentMapper.existsByUniversityIdAndName(dto.getUniversityId(), dto.getDepartmentName())) {
            throw new BusinessException(400, "该院校下院系名称已存在");
        }

        OffsetDateTime now = OffsetDateTime.now();
        Long deptId = SnowflakeIdGenerator.nextId();

        // 创建院系
        Department dept = Department.builder()
                .id(deptId)
                .universityId(dto.getUniversityId())
                .universityName(university.getName())
                .departmentName(dto.getDepartmentName())
                .departmentType(dto.getDepartmentType())
                .pageTitle(dto.getPageTitle())
                .tags(dto.getTags())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .status((short) 1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        departmentMapper.insert(dept);

        // 仅当报告字段不全为空时创建关联报告
        boolean hasReportData = dto.getSubtitle() != null
                || dto.getOverview() != null
                || dto.getSubjectsDetail() != null
                || dto.getPostgraduate() != null
                || dto.getCitySalary() != null
                || dto.getSalary() != null
                || dto.getCareer() != null
                || dto.getTrends() != null
                || dto.getProspects() != null
                || dto.getDisclaimer() != null
                || dto.getMajorCompose() != null;

        if (hasReportData) {
            Long reportId = SnowflakeIdGenerator.nextId();
            DepartmentReport report = DepartmentReport.builder()
                    .id(reportId)
                    .departmentId(deptId)
                    .subtitle(dto.getSubtitle())
                    .overview(dto.getOverview())
                    .subjectsDetail(dto.getSubjectsDetail())
                    .postgraduate(dto.getPostgraduate())
                    .citySalary(dto.getCitySalary())
                    .salary(dto.getSalary())
                    .career(dto.getCareer())
                    .trends(dto.getTrends())
                    .prospects(dto.getProspects())
                    .disclaimer(dto.getDisclaimer())
                    .majorCompose(dto.getMajorCompose())
                    .sortOrder(0)
                    .status((short) 1)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            departmentReportMapper.insert(report);
        }

        log.info("新增院系成功，id={}, name={}", deptId, dto.getDepartmentName());
        return deptId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, DepartmentUpdateDTO dto) {
        Department dept = departmentMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "院系不存在");
        }

        if (StringUtils.hasText(dto.getDepartmentName()) && !dto.getDepartmentName().equals(dept.getDepartmentName())) {
            if (departmentMapper.existsByUniversityIdAndName(dept.getUniversityId(), dto.getDepartmentName())) {
                throw new BusinessException(400, "该院校下院系名称已存在");
            }
            dept.setDepartmentName(dto.getDepartmentName());
        }

        if (dto.getDepartmentType() != null) dept.setDepartmentType(dto.getDepartmentType());
        if (dto.getPageTitle() != null) dept.setPageTitle(dto.getPageTitle());
        if (dto.getTags() != null) dept.setTags(dto.getTags());
        if (dto.getSortOrder() != null) dept.setSortOrder(dto.getSortOrder());
        if (dto.getStatus() != null) dept.setStatus(dto.getStatus().shortValue());

        dept.setUpdatedAt(OffsetDateTime.now());
        int affected = departmentMapper.updateById(dept);
        if (affected == 0) {
            throw new BusinessException(400, "数据已被其他人修改，请刷新后重试");
        }

        // 更新或创建报告
        DepartmentReport report = departmentReportMapper.selectByDepartmentId(id);
        if (report == null) {
            // 报告不存在时创建一条空报告，确保 1:1 对应
            report = DepartmentReport.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .departmentId(id)
                    .subtitle(dto.getSubtitle())
                    .overview(dto.getOverview())
                    .subjectsDetail(dto.getSubjectsDetail())
                    .postgraduate(dto.getPostgraduate())
                    .citySalary(dto.getCitySalary())
                    .salary(dto.getSalary())
                    .career(dto.getCareer())
                    .trends(dto.getTrends())
                    .prospects(dto.getProspects())
                    .disclaimer(dto.getDisclaimer())
                    .majorCompose(dto.getMajorCompose())
                    .sortOrder(0)
                    .status((short) 1)
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();
            departmentReportMapper.insert(report);
        } else {
            if (dto.getSubtitle() != null) report.setSubtitle(dto.getSubtitle());
            if (dto.getOverview() != null) report.setOverview(dto.getOverview());
            if (dto.getSubjectsDetail() != null) report.setSubjectsDetail(dto.getSubjectsDetail());
            if (dto.getPostgraduate() != null) report.setPostgraduate(dto.getPostgraduate());
            if (dto.getCitySalary() != null) report.setCitySalary(dto.getCitySalary());
            if (dto.getSalary() != null) report.setSalary(dto.getSalary());
            if (dto.getCareer() != null) report.setCareer(dto.getCareer());
            if (dto.getTrends() != null) report.setTrends(dto.getTrends());
            if (dto.getProspects() != null) report.setProspects(dto.getProspects());
            if (dto.getDisclaimer() != null) report.setDisclaimer(dto.getDisclaimer());
            if (dto.getMajorCompose() != null) report.setMajorCompose(dto.getMajorCompose());

            report.setUpdatedAt(OffsetDateTime.now());
            int reportAffected = departmentReportMapper.updateById(report);
            if (reportAffected == 0) {
                throw new BusinessException(400, "报告数据已被其他人修改，请刷新后重试");
            }
        }

        log.info("更新院系成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Short status) {
        Department dept = departmentMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "院系不存在");
        }

        OffsetDateTime now = OffsetDateTime.now();

        LambdaUpdateWrapper<Department> deptWrapper = new LambdaUpdateWrapper<>();
        deptWrapper.eq(Department::getId, id)
                   .set(Department::getStatus, status)
                   .set(Department::getUpdatedAt, now);
        departmentMapper.update(null, deptWrapper);

        // 同步更新报告状态
        LambdaUpdateWrapper<DepartmentReport> reportWrapper = new LambdaUpdateWrapper<>();
        reportWrapper.eq(DepartmentReport::getDepartmentId, id)
                     .set(DepartmentReport::getStatus, status)
                     .set(DepartmentReport::getUpdatedAt, now);
        departmentReportMapper.update(null, reportWrapper);

        log.info("更新院系状态，id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        updateStatus(id, (short) 0);
        log.info("软删除院系，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDelete(Long id) {
        Department dept = departmentMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "院系不存在");
        }

        // 先删除报告
        LambdaQueryWrapper<DepartmentReport> reportWrapper = new LambdaQueryWrapper<>();
        reportWrapper.eq(DepartmentReport::getDepartmentId, id);
        departmentReportMapper.delete(reportWrapper);

        // 再删除院系
        departmentMapper.deleteById(id);
        log.info("硬删除院系，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        OffsetDateTime now = OffsetDateTime.now();

        LambdaUpdateWrapper<Department> deptWrapper = new LambdaUpdateWrapper<>();
        deptWrapper.in(Department::getId, ids)
                   .ne(Department::getStatus, (short) 0)
                   .set(Department::getStatus, (short) 0)
                   .set(Department::getUpdatedAt, now);
        departmentMapper.update(null, deptWrapper);

        LambdaUpdateWrapper<DepartmentReport> reportWrapper = new LambdaUpdateWrapper<>();
        reportWrapper.in(DepartmentReport::getDepartmentId, ids)
                     .ne(DepartmentReport::getStatus, (short) 0)
                     .set(DepartmentReport::getStatus, (short) 0)
                     .set(DepartmentReport::getUpdatedAt, now);
        departmentReportMapper.update(null, reportWrapper);

        log.info("批量软删除院系，数量={}", ids.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchHardDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;

        LambdaQueryWrapper<DepartmentReport> reportWrapper = new LambdaQueryWrapper<>();
        reportWrapper.in(DepartmentReport::getDepartmentId, ids);
        departmentReportMapper.delete(reportWrapper);

        departmentMapper.deleteBatchIds(ids);
        log.info("批量硬删除院系，数量={}", ids.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importDepartments(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }

        List<DepartmentExcelDTO> dataList;
        try {
            dataList = EasyExcel.read(file.getInputStream())
                    .head(DepartmentExcelDTO.class)
                    .sheet("院系主表")
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "读取Excel文件失败: " + e.getMessage());
        }

        if (dataList != null && dataList.size() > MAX_IMPORT_ROWS) {
            throw new BusinessException(400, "单次导入不能超过" + MAX_IMPORT_ROWS + "条记录");
        }

        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }

        List<String> errors = new ArrayList<>();
        List<Department> validList = new ArrayList<>();
        Map<String, University> universityCache = new HashMap<>();
        OffsetDateTime now = OffsetDateTime.now();

        for (int i = 0; i < dataList.size(); i++) {
            int rowNum = i + 2;
            DepartmentExcelDTO dto = dataList.get(i);

            if (!StringUtils.hasText(dto.getUniversityName())) {
                errors.add("第" + rowNum + "行: 院校名称不能为空");
                continue;
            }
            if (!StringUtils.hasText(dto.getDepartmentName())) {
                errors.add("第" + rowNum + "行: 院系名称不能为空");
                continue;
            }
            if (!StringUtils.hasText(dto.getDepartmentType())) {
                errors.add("第" + rowNum + "行: 院系类型不能为空");
                continue;
            }

            University university = universityCache.get(dto.getUniversityName());
            if (university == null) {
                LambdaQueryWrapper<University> univWrapper = new LambdaQueryWrapper<>();
                univWrapper.eq(University::getName, dto.getUniversityName())
                           .ne(University::getStatus, (short) 0);
                university = universityMapper.selectOne(univWrapper);
                if (university != null) {
                    universityCache.put(dto.getUniversityName(), university);
                }
            }
            if (university == null) {
                errors.add("第" + rowNum + "行: 院校[" + dto.getUniversityName() + "]不存在");
                continue;
            }

            if (departmentMapper.existsByUniversityIdAndName(university.getId(), dto.getDepartmentName())) {
                errors.add("第" + rowNum + "行: 该院校下院系名称[" + dto.getDepartmentName() + "]已存在");
                continue;
            }

            Department dept = Department.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .universityId(university.getId())
                    .universityName(university.getName())
                    .departmentName(dto.getDepartmentName())
                    .departmentType(dto.getDepartmentType())
                    .pageTitle(dto.getPageTitle())
                    .tags(dto.getTags())
                    .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                    .status((short) 1)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            validList.add(dept);
        }

        if (!errors.isEmpty()) {
            String errorSummary = errors.size() <= 50
                    ? String.join("; ", errors)
                    : String.join("; ", errors.subList(0, 50)) + "...等共" + errors.size() + "条错误";
            throw new BusinessException(400, "导入校验失败，共" + errors.size() + "条错误：" + errorSummary);
        }

        for (Department dept : validList) {
            departmentMapper.insert(dept);
        }

        log.info("导入院系主表数据成功: 共{}条", validList.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importDepartmentReports(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }

        try {
            byte[] fileBytes = file.getBytes();

            List<DepartmentExcelDTO> mainDataList;
            try (InputStream is = new ByteArrayInputStream(fileBytes)) {
                mainDataList = EasyExcel.read(is)
                        .head(DepartmentExcelDTO.class)
                        .sheet("院系主表")
                        .doReadSync();
            }

            if (mainDataList != null && mainDataList.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "单次导入不能超过" + MAX_IMPORT_ROWS + "条记录");
            }

            if (mainDataList == null || mainDataList.isEmpty()) {
                throw new BusinessException(400, "「院系主表」Sheet中没有任何数据");
            }

            Map<String, Map<String, Object>> reportDataMap = buildReportDataMap(fileBytes);

            List<String> errors = new ArrayList<>();
            OffsetDateTime now = OffsetDateTime.now();
            int successCount = 0;

            for (int i = 0; i < mainDataList.size(); i++) {
                int rowNum = i + 2;
                DepartmentExcelDTO dto = mainDataList.get(i);

                if (!StringUtils.hasText(dto.getUniversityName())) {
                    errors.add("第" + rowNum + "行: 院校名称不能为空");
                    continue;
                }
                if (!StringUtils.hasText(dto.getDepartmentName())) {
                    errors.add("第" + rowNum + "行: 院系名称不能为空");
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

                Department dept = departmentMapper.selectOne(
                        new LambdaQueryWrapper<Department>()
                                .eq(Department::getUniversityId, university.getId())
                                .eq(Department::getDepartmentName, dto.getDepartmentName())
                                .ne(Department::getStatus, (short) 0));
                if (dept == null) {
                    errors.add("第" + rowNum + "行: 院系[" + dto.getDepartmentName() + "]不存在");
                    continue;
                }

                DepartmentReport existingReport = departmentReportMapper.selectByDepartmentId(dept.getId());
                Map<String, Object> reportData = reportDataMap.get(dto.getDepartmentName());

                if (existingReport != null) {
                    applyReportData(existingReport, reportData);
                    existingReport.setUpdatedAt(now);
                    departmentReportMapper.updateById(existingReport);
                } else {
                    Long reportId = SnowflakeIdGenerator.nextId();
                    DepartmentReport report = DepartmentReport.builder()
                            .id(reportId)
                            .departmentId(dept.getId())
                            .sortOrder(0)
                            .status((short) 1)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    applyReportData(report, reportData);
                    departmentReportMapper.insert(report);
                }
                successCount++;
            }

            if (!errors.isEmpty()) {
                String errorMsg = String.format("导入失败，共%d行数据存在错误，已全部回滚。错误信息：%s",
                        errors.size(), String.join("; ", errors));
                throw new BusinessException(400, errorMsg);
            }

            log.info("导入院系报告数据成功: 共{}条", successCount);

        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "读取Excel文件失败: " + e.getMessage());
        }
    }

    private Map<String, Map<String, Object>> buildReportDataMap(byte[] fileBytes) {
        Map<String, Map<String, Object>> result = new HashMap<>();

        for (Map.Entry<String, String> entry : SHEET_TO_FIELD.entrySet()) {
            String sheetName = entry.getKey();
            String fieldName = entry.getValue();

            List<List<String>> rows;
            try (InputStream is = new ByteArrayInputStream(fileBytes)) {
                rows = readSheetRows(is, sheetName);
            } catch (Exception e) {
                log.warn("Sheet「{}」读取失败，已跳过: {}", sheetName, e.getMessage());
                continue;
            }

            if (rows == null || rows.size() < 2) continue;

            List<String> headers = rows.get(0);

            for (int i = 1; i < rows.size(); i++) {
                List<String> row = rows.get(i);
                if (row == null || row.isEmpty() || !StringUtils.hasText(row.get(0))) continue;

                String deptName = row.get(0).trim();
                Map<String, Object> deptReport = result.computeIfAbsent(deptName, k -> new LinkedHashMap<>());

                switch (fieldName) {
                    case "baseInfo":
                        if (headers.size() > 1 && StringUtils.hasText(row.get(1))) {
                            deptReport.put("subtitle", row.get(1));
                        }
                        break;
                    case "citySalary":
                        addToReportList(deptReport, "citySalary", headers, row, Map.of(
                                "城市名称", "cityName",
                                "最低薪资(万元/年)", "minSalary",
                                "最高薪资(万元/年)", "maxSalary"
                        ));
                        break;
                    case "postgraduate":
                        addToReportMap(deptReport, "postgraduate", headers, row, Map.of(
                                "标题", "title",
                                "考研方向内容", "directions"
                        ));
                        break;
                    case "disclaimer":
                        addToReportMap(deptReport, "disclaimer", headers, row, Map.of(
                                "免责声明文本", "text",
                                "更新时间", "updateTime",
                                "报告版本", "version",
                                "编制单位", "compileUnit"
                        ));
                        break;
                    case "prospects":
                        addToReportMap(deptReport, "prospects", headers, row, Map.of(
                                "综合就业率", "employmentRate",
                                "硕士平均起薪", "masterSalary",
                                "继续深造率", "furtherStudyRate",
                                "进入世界500强", "fortune500Rate",
                                "年薪增长率", "salaryGrowthRate",
                                "海外深造占比", "overseasRate"
                        ));
                        break;
                    case "trends":
                        addToReportMap(deptReport, "trends", headers, row, Map.of(
                                "高速增长赛道", "highGrowthTracks",
                                "核心政策导向", "policyOrientations",
                                "就业环境分析", "environmentAnalysis"
                        ));
                        break;
                    case "overview":
                        addToReportMap(deptReport, "overview", headers, row, Map.of(
                                "标题", "title",
                                "内容描述", "descriptions"
                        ));
                        break;
                    case "career":
                        addToCareerList(deptReport, headers, row);
                        break;
                    case "subjectsDetail":
                        addToReportList(deptReport, "subjectsDetail", headers, row, Map.of(
                                "专业名称", "majorName",
                                "专业标签", "tags",
                                "核心学科", "coreSubject",
                                "支撑学科", "supportSubject",
                                "专业定位", "positioning",
                                "核心课程", "coreCourses",
                                "培养能力", "abilities",
                                "推荐证书", "certificates"
                        ));
                        break;
                    case "salary":
                        addToReportList(deptReport, "salary", headers, row, Map.of(
                                "专业名称", "majorName",
                                "最低薪资(万元/年)", "minSalary",
                                "最高薪资(万元/年)", "maxSalary"
                        ));
                        break;
                    case "majorCompose":
                        addToReportList(deptReport, "majorCompose", headers, row, Map.of(
                                "学科名称", "subjectName",
                                "占比(%)", "percentage"
                        ));
                        break;
                }
            }
        }

        return result;
    }

    private void addToReportMap(Map<String, Object> report, String key, List<String> headers, List<String> row, Map<String, String> fieldMapping) {
        Map<String, Object> data = new LinkedHashMap<>();
        for (Map.Entry<String, String> mapping : fieldMapping.entrySet()) {
            int idx = headers.indexOf(mapping.getKey());
            if (idx >= 0 && idx < row.size() && StringUtils.hasText(row.get(idx))) {
                String val = row.get(idx).trim();
                if (mapping.getValue().equals("directions") || mapping.getValue().equals("descriptions")
                        || mapping.getValue().equals("highGrowthTracks") || mapping.getValue().equals("policyOrientations")
                        || mapping.getValue().equals("environmentAnalysis")) {
                    data.put(mapping.getValue(), parseList(val));
                } else {
                    data.put(mapping.getValue(), val);
                }
            }
        }
        if (!data.isEmpty()) {
            report.put(key, data);
        }
    }

    private void addToReportList(Map<String, Object> report, String key, List<String> headers, List<String> row, Map<String, String> fieldMapping) {
        Map<String, Object> data = new LinkedHashMap<>();
        for (Map.Entry<String, String> mapping : fieldMapping.entrySet()) {
            int idx = headers.indexOf(mapping.getKey());
            if (idx >= 0 && idx < row.size() && StringUtils.hasText(row.get(idx))) {
                String val = row.get(idx).trim();
                if (mapping.getValue().equals("tags") || mapping.getValue().equals("coreCourses")
                        || mapping.getValue().equals("abilities") || mapping.getValue().equals("certificates")) {
                    data.put(mapping.getValue(), parseList(val));
                } else if (mapping.getValue().equals("minSalary") || mapping.getValue().equals("maxSalary")
                        || mapping.getValue().equals("percentage")) {
                    data.put(mapping.getValue(), parseDouble(val));
                } else {
                    data.put(mapping.getValue(), val);
                }
            }
        }
        if (!data.isEmpty()) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) report.computeIfAbsent(key, k -> new ArrayList<>());
            list.add(data);
        }
    }

    private void addToCareerList(Map<String, Object> report, List<String> headers, List<String> row) {
        Map<String, Object> item = new LinkedHashMap<>();
        for (int j = 0; j < headers.size() && j < row.size(); j++) {
            String header = headers.get(j).trim();
            String value = row.get(j).trim();
            if (!header.isEmpty() && !value.isEmpty()) {
                switch (header) {
                    case "路径标题": item.put("pathTitle", value); break;
                    case "路径描述": item.put("pathDesc", value); break;
                    case "阶段小标题": item.put("stageTitle", value); break;
                    case "工作年限": item.put("workYears", value); break;
                    case "职位名称": item.put("position", value); break;
                    case "核心目标": item.put("coreGoal", value); break;
                    case "薪资范围(万元/年)": item.put("salaryRange", value); break;
                }
            }
        }
        if (!item.isEmpty()) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) report.computeIfAbsent("career", k -> new ArrayList<>());
            list.add(item);
        }
    }

    private List<String> parseList(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value.replace("%", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void applyReportData(DepartmentReport report, Map<String, Object> reportData) {
        if (reportData == null || reportData.isEmpty()) return;

        if (reportData.containsKey("subtitle")) {
            report.setSubtitle((String) reportData.get("subtitle"));
        }
        if (reportData.containsKey("citySalary")) {
            report.setCitySalary((List<Map<String, Object>>) reportData.get("citySalary"));
        }
        if (reportData.containsKey("postgraduate")) {
            report.setPostgraduate((Map<String, Object>) reportData.get("postgraduate"));
        }
        if (reportData.containsKey("disclaimer")) {
            report.setDisclaimer((Map<String, Object>) reportData.get("disclaimer"));
        }
        if (reportData.containsKey("prospects")) {
            report.setProspects((Map<String, Object>) reportData.get("prospects"));
        }
        if (reportData.containsKey("trends")) {
            report.setTrends((Map<String, Object>) reportData.get("trends"));
        }
        if (reportData.containsKey("overview")) {
            report.setOverview((Map<String, Object>) reportData.get("overview"));
        }
        if (reportData.containsKey("career")) {
            report.setCareer((List<Map<String, Object>>) reportData.get("career"));
        }
        if (reportData.containsKey("subjectsDetail")) {
            report.setSubjectsDetail((List<Map<String, Object>>) reportData.get("subjectsDetail"));
        }
        if (reportData.containsKey("salary")) {
            report.setSalary((List<Map<String, Object>>) reportData.get("salary"));
        }
        if (reportData.containsKey("majorCompose")) {
            report.setMajorCompose((List<Map<String, Object>>) reportData.get("majorCompose"));
        }
    }

    private List<List<String>> readSheetRows(InputStream is, String sheetName) {
        try {
            List<Object> rawRows = EasyExcel.read(is).sheet(sheetName).doReadSync();
            if (rawRows != null && rawRows.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "单次导入不能超过" + MAX_IMPORT_ROWS + "条记录");
            }
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
            log.warn("读取Sheet「{}」失败: {}", sheetName, e.getMessage());
            return Collections.emptyList();
        }
    }
}
