package com.haifeng.app.service.impl.company;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.company.EnterpriseQueryDTO;
import com.haifeng.app.service.company.EnterpriseService;
import com.haifeng.app.vo.company.EnterpriseIndustryGroupVO;
import com.haifeng.app.vo.company.EnterpriseListVO;
import com.haifeng.app.vo.company.EnterprisePositionVO;
import com.haifeng.app.vo.company.IndustryJumpVO;
import com.haifeng.common.entity.company.Enterprise;
import com.haifeng.common.entity.company.EnterpriseIndustry;
import com.haifeng.common.entity.company.EnterprisePosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.company.EnterpriseIndustryMapper;
import com.haifeng.common.mapper.company.EnterpriseMapper;
import com.haifeng.common.mapper.company.EnterprisePositionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnterpriseServiceImpl implements EnterpriseService {

    private final EnterpriseMapper enterpriseMapper;
    private final EnterprisePositionMapper enterprisePositionMapper;
    private final EnterpriseIndustryMapper enterpriseIndustryMapper;

    @Override
    public IPage<EnterpriseListVO> page(EnterpriseQueryDTO dto) {
        Page<Enterprise> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<Enterprise>()
                .eq(Enterprise::getIsDeleted, false)
                .like(StringUtils.hasText(dto.getEnterpriseName()), Enterprise::getEnterpriseName, dto.getEnterpriseName())
                .eq(StringUtils.hasText(dto.getEnterpriseNature()), Enterprise::getEnterpriseNature, dto.getEnterpriseNature())
                .eq(StringUtils.hasText(dto.getEnterpriseType()), Enterprise::getEnterpriseType, dto.getEnterpriseType())
                .eq(StringUtils.hasText(dto.getCityName()), Enterprise::getCityName, dto.getCityName())
                .eq(StringUtils.hasText(dto.getRecruitmentStatus()), Enterprise::getRecruitmentStatus, dto.getRecruitmentStatus())
                .orderByAsc(Enterprise::getId);

        IPage<Enterprise> entityPage = enterpriseMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    @Override
    public List<EnterprisePositionVO> positions(Long enterpriseId) {
        Enterprise enterprise = enterpriseMapper.selectById(enterpriseId);
        if (enterprise == null || Boolean.TRUE.equals(enterprise.getIsDeleted())) {
            log.debug("企业不存在或已删除, enterpriseId={}", enterpriseId);
            throw new BusinessException(ResultCode.NOT_FOUND, "企业不存在");
        }

        LambdaQueryWrapper<EnterprisePosition> wrapper = new LambdaQueryWrapper<EnterprisePosition>()
                .eq(EnterprisePosition::getEnterpriseId, enterpriseId)
                .eq(EnterprisePosition::getIsDeleted, false)
                .orderByAsc(EnterprisePosition::getId);

        return enterprisePositionMapper.selectList(wrapper).stream()
                .map(this::toPositionVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EnterpriseIndustryGroupVO> industriesByEnterpriseIds(List<Long> enterpriseIds) {
        List<Long> ids = normalizeIds(enterpriseIds, "企业ID列表不能为空");

        LambdaQueryWrapper<EnterpriseIndustry> wrapper = new LambdaQueryWrapper<EnterpriseIndustry>()
                .in(EnterpriseIndustry::getEnterpriseId, ids)
                .orderByAsc(EnterpriseIndustry::getEnterpriseId)
                .orderByAsc(EnterpriseIndustry::getSortOrder)
                .orderByAsc(EnterpriseIndustry::getId);

        List<EnterpriseIndustry> relations = enterpriseIndustryMapper.selectList(wrapper);

        Map<Long, List<IndustryJumpVO>> grouped = new LinkedHashMap<>();
        ids.forEach(id -> grouped.put(id, new ArrayList<>()));

        for (EnterpriseIndustry relation : relations) {
            List<IndustryJumpVO> industries = grouped.get(relation.getEnterpriseId());
            if (industries != null) {
                industries.add(IndustryJumpVO.builder()
                        .industryId(relation.getIndustryId())
                        .industryName(relation.getIndustryName())
                        .build());
            }
        }

        return grouped.entrySet().stream()
                .map(entry -> EnterpriseIndustryGroupVO.builder()
                        .enterpriseId(entry.getKey())
                        .industries(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    private EnterpriseListVO toListVO(Enterprise e) {
        return EnterpriseListVO.builder()
                .id(e.getId())
                .cityName(e.getCityName())
                .enterpriseName(e.getEnterpriseName())
                .enterpriseNature(e.getEnterpriseNature())
                .enterpriseType(e.getEnterpriseType())
                .logoUrl(e.getLogoUrl())
                .officialWebsite(e.getOfficialWebsite())
                .region(e.getRegion())
                .enterpriseScale(e.getEnterpriseScale())
                .mainBusiness(e.getMainBusiness())
                .enterpriseIntro(e.getEnterpriseIntro())
                .build();
    }

    private EnterprisePositionVO toPositionVO(EnterprisePosition e) {
        return EnterprisePositionVO.builder()
                .positionName(e.getPositionName())
                .recruitmentType(e.getRecruitmentType())
                .positionRequirement(e.getPositionRequirement())
                .positionTags(e.getPositionTags())
                .province(e.getProvince())
                .city(e.getCity())
                .workLocation(e.getWorkLocation())
                .educationRequirement(e.getEducationRequirement())
                .majorRequirement(e.getMajorRequirement())
                .workExperience(e.getWorkExperience())
                .salaryMin(e.getSalaryMin())
                .salaryMax(e.getSalaryMax())
                .applyLink(e.getApplyLink())
                .deadline(e.getDeadline())
                .positionStatus(e.getPositionStatus())
                .build();
    }

    private List<Long> normalizeIds(List<Long> ids, String emptyMessage) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, emptyMessage);
        }

        List<Long> normalized = ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (normalized.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, emptyMessage);
        }
        return normalized;
    }
}
