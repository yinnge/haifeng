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
import com.haifeng.admin.vo.major.ImportResultVO;
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
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    private final DepartmentMapper departmentMapper;

    private static final int MAX_IMPORT_ROWS = 1000;
    private static final int MAX_ERROR_DISPLAY = 50;
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

    /**
     * department_reports 各 JSONB 字段的写入器（键名与 Excel 解析出的 reportData key 一致）。
     * 值与 DepartmentReport 实体字段类型对应，类型转换在 lambda 内完成。
     */
    private static final Map<String, BiConsumer<DepartmentReport, Object>> REPORT_JSONB_SETTERERS = new LinkedHashMap<>();
    /**
     * 与 REPORT_JSONB_SETTERERS 一一对应的取值器，用于读取库中该 JSONB 字段当前值（仅补齐模式判断"已有数据"用）。
     */
    private static final Map<String, Function<DepartmentReport, Object>> REPORT_JSONB_GETTERERS = new LinkedHashMap<>();

    static {
        REPORT_JSONB_SETTERERS.put("subtitle", (r, v) -> r.setSubtitle((String) v));
        REPORT_JSONB_SETTERERS.put("citySalary", (r, v) -> r.setCitySalary((List<Map<String, Object>>) v));
        REPORT_JSONB_SETTERERS.put("postgraduate", (r, v) -> r.setPostgraduate((Map<String, Object>) v));
        REPORT_JSONB_SETTERERS.put("disclaimer", (r, v) -> r.setDisclaimer((Map<String, Object>) v));
        REPORT_JSONB_SETTERERS.put("prospects", (r, v) -> r.setProspects((Map<String, Object>) v));
        REPORT_JSONB_SETTERERS.put("trends", (r, v) -> r.setTrends((Map<String, Object>) v));
        REPORT_JSONB_SETTERERS.put("overview", (r, v) -> r.setOverview((Map<String, Object>) v));
        REPORT_JSONB_SETTERERS.put("career", (r, v) -> r.setCareer((List<Map<String, Object>>) v));
        REPORT_JSONB_SETTERERS.put("subjectsDetail", (r, v) -> r.setSubjectsDetail((List<Map<String, Object>>) v));
        REPORT_JSONB_SETTERERS.put("salary", (r, v) -> r.setSalary((List<Map<String, Object>>) v));
        REPORT_JSONB_SETTERERS.put("majorCompose", (r, v) -> r.setMajorCompose((List<Map<String, Object>>) v));

        REPORT_JSONB_GETTERERS.put("subtitle", DepartmentReport::getSubtitle);
        REPORT_JSONB_GETTERERS.put("citySalary", DepartmentReport::getCitySalary);
        REPORT_JSONB_GETTERERS.put("postgraduate", DepartmentReport::getPostgraduate);
        REPORT_JSONB_GETTERERS.put("disclaimer", DepartmentReport::getDisclaimer);
        REPORT_JSONB_GETTERERS.put("prospects", DepartmentReport::getProspects);
        REPORT_JSONB_GETTERERS.put("trends", DepartmentReport::getTrends);
        REPORT_JSONB_GETTERERS.put("overview", DepartmentReport::getOverview);
        REPORT_JSONB_GETTERERS.put("career", DepartmentReport::getCareer);
        REPORT_JSONB_GETTERERS.put("subjectsDetail", DepartmentReport::getSubjectsDetail);
        REPORT_JSONB_GETTERERS.put("salary", DepartmentReport::getSalary);
        REPORT_JSONB_GETTERERS.put("majorCompose", DepartmentReport::getMajorCompose);
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

        departmentMapper.deleteByIds(ids);
        log.info("批量硬删除院系，数量={}", ids.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importDepartments(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }

        List<DepartmentExcelDTO> dataList;
        try {
            dataList = EasyExcel.read(file.getInputStream())
                    .head(DepartmentExcelDTO.class)
                    .sheet("院系主表")
                    .doReadSync();
        } catch (Exception e) {
            log.error("读取『院系主表』Sheet失败", e);
            throw new BusinessException(400, "读取『院系主表』Sheet失败，请确认Sheet名称为『院系主表』且表头正确: " + e.getMessage());
        }

        if (dataList != null && dataList.size() > MAX_IMPORT_ROWS) {
            throw new BusinessException(400, "单次导入不能超过" + MAX_IMPORT_ROWS + "条记录");
        }

        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }

        List<String> errors = new ArrayList<>();
        List<String> mainDeptNames = new ArrayList<>();
        Map<String, University> universityCache = new HashMap<>();
        OffsetDateTime now = OffsetDateTime.now();
        int addedCount = 0;
        int updatedCount = 0;

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
            mainDeptNames.add(dto.getDepartmentName().trim());
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

            // 同院校+同院系名称：已存在则只补空，不再报"已存在"
            List<Department> existingList = departmentMapper.selectList(
                    new LambdaQueryWrapper<Department>()
                            .eq(Department::getUniversityId, university.getId())
                            .eq(Department::getDepartmentName, dto.getDepartmentName())
                            .eq(Department::getStatus, (short) 1));
            Department existing = existingList.isEmpty() ? null : existingList.get(0);

            try {
                if (existing != null) {
                    fillDeptGaps(existing, dto, now);
                    departmentMapper.updateById(existing);
                    updatedCount++;
                } else {
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
                    departmentMapper.insert(dept);
                    addedCount++;
                }
            } catch (Exception e) {
                errors.add("第" + rowNum + "行: 保存失败[院校=" + dto.getUniversityName()
                        + ", 院系=" + dto.getDepartmentName() + "]：" + e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new BusinessException(400, "导入院系主表校验失败，已全部回滚：" + joinErrors(errors));
        }

        log.info("导入院系主表数据成功: 新增{}条, 补齐{}条", addedCount, updatedCount);
        return ImportResultVO.builder()
                .total(dataList.size())
                .success(dataList.size())
                .failed(0)
                .updated(updatedCount)
                .errors(Collections.emptyList())
                .build();
    }

    /**
     * 已有院系记录：仅补齐数据库中为 NULL 的字段，已有数据一律不覆盖。
     */
    private void fillDeptGaps(Department db, DepartmentExcelDTO dto, OffsetDateTime now) {
        if (db.getPageTitle() == null) {
            db.setPageTitle(dto.getPageTitle());
        }
        if ((db.getTags() == null || db.getTags().isEmpty()) && dto.getTags() != null && !dto.getTags().isEmpty()) {
            db.setTags(dto.getTags());
        }
        if (db.getSortOrder() == null) {
            db.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        }
        db.setUpdatedAt(now);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importDepartmentReports(MultipartFile file) {
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
            int addedCount = 0;
            int updatedCount = 0;
            List<String> mainDeptNames = new ArrayList<>();

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
                mainDeptNames.add(dto.getDepartmentName().trim());

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
                Map<String, Object> reportData = reportDataMap.get(dto.getDepartmentName().trim());

                try {
                    if (existingReport != null) {
                        // 已存在：仅补齐数据库中为 NULL 的字段，已有数据一律不覆盖
                        applyReportData(existingReport, reportData, true);
                        existingReport.setUpdatedAt(now);
                        departmentReportMapper.updateById(existingReport);
                        updatedCount++;
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
                        applyReportData(report, reportData, false);
                        departmentReportMapper.insert(report);
                        addedCount++;
                    }
                } catch (Exception e) {
                    errors.add("第" + rowNum + "行: 保存失败[院系=" + dto.getDepartmentName() + "]：" + e.getMessage());
                }
            }

            for (String reportDept : reportDataMap.keySet()) {
                if (!mainDeptNames.contains(reportDept)) {
                    errors.add("报告分类Sheet中的院系名称[" + reportDept + "]在主表『院系主表』中不存在，该数据未导入");
                }
            }

            if (!errors.isEmpty()) {
                throw new BusinessException(400, "导入院系报告失败，已全部回滚：" + joinErrors(errors));
            }

            log.info("导入院系报告数据成功: 新增{}条, 补齐{}条", addedCount, updatedCount);
            return ImportResultVO.builder()
                    .total(mainDataList.size())
                    .success(mainDataList.size())
                    .failed(0)
                    .updated(updatedCount)
                    .errors(Collections.emptyList())
                    .build();

        } catch (BusinessException be) {
            // 保留清晰的分类Sheet读取失败等错误信息，不做二次包裹
            throw be;
        } catch (Exception e) {
            log.error("读取院系报告Excel失败", e);
            throw new BusinessException(400, "读取院系报告Excel失败: " + e.getMessage());
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
            } catch (BusinessException be) {
                throw be;
            } catch (Exception e) {
                throw new BusinessException(400, "分类Sheet「" + sheetName + "」读取失败: " + e.getMessage());
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

    /**
     * 错误列表统一截断：超过 MAX_ERROR_DISPLAY 条时提示总数，避免全局报错 msg 过长。
     * 与项目导入模板（AdmissionGroupServiceImpl）保持一致。
     */
    private String joinErrors(List<String> errors) {
        if (errors == null || errors.isEmpty()) return null;
        int shown = Math.min(errors.size(), MAX_ERROR_DISPLAY);
        String joined = String.join("; ", errors.subList(0, shown));
        if (errors.size() > MAX_ERROR_DISPLAY) {
            joined += "; ...仅显示前" + MAX_ERROR_DISPLAY + "条，共" + errors.size() + "行存在错误";
        }
        return joined;
    }

    /**
     * 将导入的报告数据写入报告实体。
     *
     * @param onlyFillNull true=仅补齐实体中为 NULL（或空集合）的字段，已有数据一律不覆盖；
     *                     false=导入数据中存在该字段就写入（新增记录时使用）
     */
    /**
     * 将导入的报告数据写入报告实体（映射表模式，与 UniversityGuideServiceImpl 一致）。
     *
     * @param onlyFillNull true=仅补齐实体中为 NULL（或空集合/空串）的字段，已有数据一律不覆盖；
     *                     false=导入数据中存在该字段就写入（新增记录时使用）
     */
    private void applyReportData(DepartmentReport report, Map<String, Object> reportData, boolean onlyFillNull) {
        if (reportData == null || reportData.isEmpty()) return;

        for (Map.Entry<String, Object> entry : reportData.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();
            BiConsumer<DepartmentReport, Object> setter = REPORT_JSONB_SETTERERS.get(fieldName);
            if (setter == null) continue; // reportData 中未登记的键（如 baseInfo 基础信息）忽略
            if (onlyFillNull) {
                // 仅补齐模式：库中该字段已有值（非空集合/非空串）则跳过，不覆盖
                Function<DepartmentReport, Object> getter = REPORT_JSONB_GETTERERS.get(fieldName);
                Object current = getter != null ? getter.apply(report) : null;
                if (!isBlankJsonb(current)) continue;
            }
            setter.accept(report, value);
        }
    }

    /**
     * 通用判空：null / 空 Map / 空 List / 空 String 均视为"空"，需要补齐。
     * 用于绕开 JSONB 建表 DEFAULT '{}'/'[]' → MP 插入省略 null → 库里落空集合的坑。
     */
    private boolean isBlankJsonb(Object v) {
        if (v == null) return true;
        if (v instanceof Map) return ((Map<?, ?>) v).isEmpty();
        if (v instanceof Collection) return ((Collection<?>) v).isEmpty();
        if (v instanceof String) return ((String) v).isEmpty();
        return false;
    }

    private List<List<String>> readSheetRows(InputStream is, String sheetName) {
        try {
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
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            throw new BusinessException(400, "读取Sheet「" + sheetName + "」失败: " + e.getMessage());
        }
    }
}
