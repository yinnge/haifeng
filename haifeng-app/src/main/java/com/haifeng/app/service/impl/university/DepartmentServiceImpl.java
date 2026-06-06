package com.haifeng.app.service.impl.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.university.DepartmentQueryDTO;
import com.haifeng.app.service.university.DepartmentService;
import com.haifeng.app.vo.university.DepartmentListVO;
import com.haifeng.app.vo.university.DepartmentReportVO;
import com.haifeng.common.entity.university.Department;
import com.haifeng.common.entity.university.DepartmentReport;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.DepartmentMapper;
import com.haifeng.common.mapper.university.DepartmentReportMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private static final short STATUS_PUBLISHED = 1;

    private final DepartmentMapper departmentMapper;
    private final DepartmentReportMapper departmentReportMapper;

    @Override
    public IPage<DepartmentListVO> page(Long universityId, DepartmentQueryDTO dto) {
        Page<Department> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<Department>()
                .eq(Department::getUniversityId, universityId)
                .eq(Department::getStatus, STATUS_PUBLISHED)
                .orderByAsc(Department::getSortOrder)
                .orderByDesc(Department::getId);

        IPage<Department> entityPage = departmentMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    @Override
    public DepartmentReportVO report(Long departmentId) {
        // mapper 自带 status=1 过滤，见 DepartmentReportMapper.selectByDepartmentId
        DepartmentReport r = departmentReportMapper.selectByDepartmentId(departmentId);
        if (r == null) {
            log.debug("院系分析报告不存在, departmentId={}", departmentId);
            throw new BusinessException(ResultCode.NOT_FOUND, "院系分析报告不存在");
        }

        return DepartmentReportVO.builder()
                .subtitle(r.getSubtitle())
                .overview(r.getOverview())
                .subjectsDetail(r.getSubjectsDetail())
                .postgraduate(r.getPostgraduate())
                .citySalary(r.getCitySalary())
                .salary(r.getSalary())
                .career(r.getCareer())
                .trends(r.getTrends())
                .prospects(r.getProspects())
                .disclaimer(r.getDisclaimer())
                .majorCompose(r.getMajorCompose())
                .build();
    }

    private DepartmentListVO toListVO(Department e) {
        return DepartmentListVO.builder()
                .id(e.getId())
                .departmentName(e.getDepartmentName())
                .departmentType(e.getDepartmentType())
                .build();
    }
}
