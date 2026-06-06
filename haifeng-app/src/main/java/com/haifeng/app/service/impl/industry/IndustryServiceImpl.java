package com.haifeng.app.service.impl.industry;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.industry.IndustryQueryDTO;
import com.haifeng.app.service.industry.IndustryService;
import com.haifeng.app.vo.company.EnterpriseJumpVO;
import com.haifeng.app.vo.company.IndustryEnterpriseGroupVO;
import com.haifeng.app.vo.industry.IndustryDetailVO;
import com.haifeng.app.vo.industry.IndustryListVO;
import com.haifeng.common.entity.company.EnterpriseIndustry;
import com.haifeng.common.entity.industry.Industry;
import com.haifeng.common.entity.industry.IndustryDetail;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.company.EnterpriseIndustryMapper;
import com.haifeng.common.mapper.industry.IndustryDetailMapper;
import com.haifeng.common.mapper.industry.IndustryMapper;
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
public class IndustryServiceImpl implements IndustryService {

    private final IndustryMapper industryMapper;
    private final IndustryDetailMapper industryDetailMapper;
    private final EnterpriseIndustryMapper enterpriseIndustryMapper;

    @Override
    public IPage<IndustryListVO> page(IndustryQueryDTO dto) {
        Page<Industry> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Industry> wrapper = new LambdaQueryWrapper<Industry>()
                .eq(Industry::getIsDeleted, false)
                .eq(StringUtils.hasText(dto.getCategory()), Industry::getCategory, dto.getCategory())
                .orderByAsc(Industry::getId);

        IPage<Industry> entityPage = industryMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    @Override
    public IndustryDetailVO detail(Long industryId) {
        IndustryDetail detail = industryDetailMapper.findByIndustryId(industryId);
        if (detail == null) {
            log.debug("行业详情不存在, industryId={}", industryId);
            throw new BusinessException(ResultCode.NOT_FOUND, "行业详情不存在");
        }

        return IndustryDetailVO.builder()
                .industryName(detail.getIndustryName())
                .shortDescription(detail.getShortDescription())
                .detailedDescription(detail.getDetailedDescription())
                .industryScale(detail.getIndustryScale())
                .industryTalentDemand(detail.getIndustryTalentDemand())
                .industrySalary(detail.getIndustrySalary())
                .policyInfo(detail.getPolicyInfo())
                .developmentSupportInfo(detail.getDevelopmentSupportInfo())
                .talentAnalysis(detail.getTalentAnalysis())
                .talentPolicy(detail.getTalentPolicy())
                .salaryData(detail.getSalaryData())
                .build();
    }

    @Override
    public List<IndustryEnterpriseGroupVO> enterprisesByIndustryIds(List<Long> industryIds) {
        List<Long> ids = normalizeIds(industryIds, "行业ID列表不能为空");

        LambdaQueryWrapper<EnterpriseIndustry> wrapper = new LambdaQueryWrapper<EnterpriseIndustry>()
                .in(EnterpriseIndustry::getIndustryId, ids)
                .orderByAsc(EnterpriseIndustry::getIndustryId)
                .orderByAsc(EnterpriseIndustry::getSortOrder)
                .orderByAsc(EnterpriseIndustry::getId);

        List<EnterpriseIndustry> relations = enterpriseIndustryMapper.selectList(wrapper);

        Map<Long, List<EnterpriseJumpVO>> grouped = new LinkedHashMap<>();
        ids.forEach(id -> grouped.put(id, new ArrayList<>()));

        for (EnterpriseIndustry relation : relations) {
            List<EnterpriseJumpVO> enterprises = grouped.get(relation.getIndustryId());
            if (enterprises != null) {
                enterprises.add(EnterpriseJumpVO.builder()
                        .enterpriseId(relation.getEnterpriseId())
                        .enterpriseName(relation.getEnterpriseName())
                        .build());
            }
        }

        return grouped.entrySet().stream()
                .map(entry -> IndustryEnterpriseGroupVO.builder()
                        .industryId(entry.getKey())
                        .enterprises(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    private IndustryListVO toListVO(Industry e) {
        return IndustryListVO.builder()
                .id(e.getId())
                .industryName(e.getIndustryName())
                .category(e.getCategory())
                .description(e.getDescription())
                .annualGrowthRate(e.getAnnualGrowthRate())
                .marketScale(e.getMarketScale())
                .talentGap(e.getTalentGap())
                .investmentHeat(e.getInvestmentHeat())
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
