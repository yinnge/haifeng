package com.haifeng.app.service.impl.employment.civilService;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.civilService.MilitaryPositionSearchDTO;
import com.haifeng.app.service.employment.civilService.MilitaryPositionService;
import com.haifeng.app.vo.employment.civilService.MilitaryPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.MilitaryPositionListVO;
import com.haifeng.common.entity.employment.civilService.MilitaryPosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.civilService.MilitaryPositionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MilitaryPositionServiceImpl implements MilitaryPositionService {

    private final MilitaryPositionMapper militaryPositionMapper;

    @Override
    public IPage<MilitaryPositionListVO> page(MilitaryPositionSearchDTO dto) {
        LambdaQueryWrapper<MilitaryPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MilitaryPosition::getIsDeleted, false);

        if (StrUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w
                    .like(MilitaryPosition::getPositionName, dto.getKeyword())
                    .or()
                    .like(MilitaryPosition::getEmployerUnit, dto.getKeyword())
                    .or()
                    .like(MilitaryPosition::getDepartment, dto.getKeyword())
            );
        }

        wrapper.eq(StrUtil.isNotBlank(dto.getPositionType()), MilitaryPosition::getPositionType, dto.getPositionType());
        wrapper.eq(StrUtil.isNotBlank(dto.getMajorRequirement()), MilitaryPosition::getMajorRequirement, dto.getMajorRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getEducationRequirement()), MilitaryPosition::getEducationRequirement, dto.getEducationRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getPositionStatus()), MilitaryPosition::getPositionStatus, dto.getPositionStatus());
        wrapper.eq(StrUtil.isNotBlank(dto.getWorkLocation()), MilitaryPosition::getWorkLocation, dto.getWorkLocation());

        wrapper.orderByDesc(MilitaryPosition::getCreatedAt);

        Page<MilitaryPosition> page = new Page<>(dto.getPage(), dto.getSize());
        militaryPositionMapper.selectPage(page, wrapper);

        return page.convert(item -> MilitaryPositionListVO.builder()
                .id(item.getId())
                .positionName(item.getPositionName())
                .employerUnit(item.getEmployerUnit())
                .department(item.getDepartment())
                .positionType(item.getPositionType())
                .workLocation(item.getWorkLocation())
                .salaryRange(item.getSalaryRange())
                .majorRequirement(item.getMajorRequirement())
                .educationRequirement(item.getEducationRequirement())
                .regDeadline(item.getRegDeadline())
                .positionStatus(item.getPositionStatus())
                .build());
    }

    @Override
    public MilitaryPositionDetailVO detail(Long id) {
        MilitaryPosition item = militaryPositionMapper.selectById(id);
        if (item == null || Boolean.TRUE.equals(item.getIsDeleted())) {
            log.warn("部队文职岗位不存在，id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return MilitaryPositionDetailVO.builder()
                .id(item.getId())
                .positionName(item.getPositionName())
                .employerUnit(item.getEmployerUnit())
                .department(item.getDepartment())
                .positionType(item.getPositionType())
                .workLocation(item.getWorkLocation())
                .salaryRange(item.getSalaryRange())
                .majorRequirement(item.getMajorRequirement())
                .educationRequirement(item.getEducationRequirement())
                .regDeadline(item.getRegDeadline())
                .positionStatus(item.getPositionStatus())
                .positionDescription(item.getPositionDescription())
                .responsibilities(item.getResponsibilities())
                .qualifications(item.getQualifications())
                .build();
    }
}
