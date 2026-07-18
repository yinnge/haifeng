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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    private final DepartmentMapper departmentMapper;
    private final DepartmentReportMapper departmentReportMapper;
    private final UniversityMapper universityMapper;

    @Override
    public IPage<DepartmentListVO> page(DepartmentQueryDTO dto) {
        Page<Department> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(Department::getStatus, (short) 0);

        if (StringUtils.hasText(dto.getUniversityName())) {
            wrapper.like(Department::getUniversityName, dto.getUniversityName());
        }
        if (StringUtils.hasText(dto.getDepartmentName())) {
            wrapper.like(Department::getDepartmentName, dto.getDepartmentName());
        }
        if (StringUtils.hasText(dto.getDepartmentType())) {
            wrapper.eq(Department::getDepartmentType, dto.getDepartmentType());
        }
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
        if (dept == null || dept.getStatus() == 0) {
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
        if (dept == null || dept.getStatus() == 0) {
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
        // Excel导入实现 - 见Task 11
        throw new BusinessException(400, "该功能暂未开放");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importDepartmentReports(MultipartFile file) {
        // Excel导入实现 - 见Task 11
        throw new BusinessException(400, "该功能暂未开放");
    }
}
