package com.haifeng.app.service.impl.employment.jobIndex;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.jobIndex.JobSearchDTO;
import com.haifeng.app.service.employment.jobIndex.JobIndexService;
import com.haifeng.app.vo.employment.jobIndex.JobIndexDetailVO;
import com.haifeng.app.vo.employment.jobIndex.JobIndexListVO;
import com.haifeng.common.entity.employment.jobIndex.JobIndex;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.jobIndex.JobIndexMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobIndexServiceImpl implements JobIndexService {

    private final JobIndexMapper jobIndexMapper;

    @Override
    public IPage<JobIndexListVO> page(JobSearchDTO dto) {
        LambdaQueryWrapper<JobIndex> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobIndex::getIsDeleted, false);

        if (StrUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w
                    .like(JobIndex::getPositionName, dto.getKeyword())
                    .or()
                    .like(JobIndex::getOrganizationName, dto.getKeyword())
            );
        }

        wrapper.eq(StrUtil.isNotBlank(dto.getProvince()), JobIndex::getProvince, dto.getProvince());
        wrapper.eq(StrUtil.isNotBlank(dto.getCity()), JobIndex::getCity, dto.getCity());
        wrapper.eq(StrUtil.isNotBlank(dto.getEducationRequirement()), JobIndex::getEducationRequirement, dto.getEducationRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getRecruitmentType()), JobIndex::getRecruitmentType, dto.getRecruitmentType());
        wrapper.eq(StrUtil.isNotBlank(dto.getPositionStatus()), JobIndex::getPositionStatus, dto.getPositionStatus());
        wrapper.eq(StrUtil.isNotBlank(dto.getCategoryLabel()), JobIndex::getCategoryLabel, dto.getCategoryLabel());

        if (dto.getSalaryMin() != null) {
            wrapper.ge(JobIndex::getSalaryMax, dto.getSalaryMin());
        }
        if (dto.getSalaryMax() != null) {
            wrapper.le(JobIndex::getSalaryMin, dto.getSalaryMax());
        }

        if (dto.getPublishDateStart() != null) {
            wrapper.ge(JobIndex::getPublishDate, toStartOfDay(dto.getPublishDateStart()));
        }
        if (dto.getPublishDateEnd() != null) {
            wrapper.le(JobIndex::getPublishDate, toEndOfDay(dto.getPublishDateEnd()));
        }
        if (dto.getRegDeadlineStart() != null) {
            wrapper.ge(JobIndex::getRegDeadline, toStartOfDay(dto.getRegDeadlineStart()));
        }
        if (dto.getRegDeadlineEnd() != null) {
            wrapper.le(JobIndex::getRegDeadline, toEndOfDay(dto.getRegDeadlineEnd()));
        }

        wrapper.orderByDesc(JobIndex::getPublishDate).last("NULLS LAST");

        Page<JobIndex> page = new Page<>(dto.getPage(), dto.getSize());
        jobIndexMapper.selectPage(page, wrapper);

        return page.convert(job -> JobIndexListVO.builder()
                .id(job.getId())
                .categoryLabel(job.getCategoryLabel())
                .positionName(job.getPositionName())
                .organizationName(job.getOrganizationName())
                .city(job.getCity())
                .educationRequirement(job.getEducationRequirement())
                .recruitmentType(job.getRecruitmentType())
                .salaryText(job.getSalaryText())
                .positionStatus(job.getPositionStatus())
                .build());
    }

    @Override
    public JobIndexDetailVO detail(Long id) {
        JobIndex job = jobIndexMapper.selectById(id);
        if (job == null || Boolean.TRUE.equals(job.getIsDeleted())) {
            log.warn("岗位不存在，id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return JobIndexDetailVO.builder()
                .id(job.getId())
                .sourceType(job.getSourceType())
                .sourceId(job.getSourceId())
                .categoryLabel(job.getCategoryLabel())
                .positionName(job.getPositionName())
                .organizationName(job.getOrganizationName())
                .organizationLogo(job.getOrganizationLogo())
                .province(job.getProvince())
                .city(job.getCity())
                .educationRequirement(job.getEducationRequirement())
                .recruitmentCount(job.getRecruitmentCount())
                .recruitmentType(job.getRecruitmentType())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .salaryText(job.getSalaryText())
                .positionStatus(job.getPositionStatus())
                .publishDate(job.getPublishDate())
                .regDeadline(job.getRegDeadline())
                .isHot(job.getIsHot())
                .viewCount(job.getViewCount())
                .applyCount(job.getApplyCount())
                .build();
    }

    private static OffsetDateTime toStartOfDay(LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
    }

    private static OffsetDateTime toEndOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);
    }
}
