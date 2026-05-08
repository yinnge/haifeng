package com.haifeng.admin.service.impl.industry;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.industry.IndustryAddDTO;
import com.haifeng.admin.dto.industry.IndustryDetailUpdateDTO;
import com.haifeng.admin.dto.industry.IndustryQueryDTO;
import com.haifeng.admin.dto.industry.IndustryStatusDTO;
import com.haifeng.admin.dto.industry.IndustryUpdateDTO;
import com.haifeng.admin.service.industry.IndustryService;
import com.haifeng.admin.vo.industry.IndustryDetailVO;
import com.haifeng.admin.vo.industry.IndustryListVO;
import com.haifeng.common.entity.industry.Industry;
import com.haifeng.common.entity.industry.IndustryDetail;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.industry.IndustryDetailMapper;
import com.haifeng.common.mapper.industry.IndustryMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndustryServiceImpl implements IndustryService {

    private final IndustryMapper industryMapper;
    private final IndustryDetailMapper industryDetailMapper;

    @Override
    public IPage<IndustryListVO> page(IndustryQueryDTO dto) {
        Page<Industry> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Industry> wrapper = new LambdaQueryWrapper<>();

        // 行业名称模糊查询
        if (StringUtils.hasText(dto.getIndustryName())) {
            wrapper.like(Industry::getIndustryName, dto.getIndustryName());
        }
        // 行业分类模糊查询
        if (StringUtils.hasText(dto.getCategory())) {
            wrapper.like(Industry::getCategory, dto.getCategory());
        }
        // 人才趋势模糊查询
        if (StringUtils.hasText(dto.getTalentTrend())) {
            wrapper.like(Industry::getTalentTrend, dto.getTalentTrend());
        }
        // 删除状态筛选
        if (dto.getIsDeleted() != null) {
            wrapper.eq(Industry::getIsDeleted, dto.getIsDeleted());
        }

        // 按分类升序，行业名称升序
        wrapper.orderByAsc(Industry::getCategory)
               .orderByAsc(Industry::getIndustryName);

        IPage<Industry> industryPage = industryMapper.selectPage(page, wrapper);

        return industryPage.convert(industry -> {
            IndustryListVO vo = new IndustryListVO();
            BeanUtils.copyProperties(industry, vo);
            // 处理时间类型转换
            if (industry.getCreatedAt() != null) {
                vo.setCreatedAt(industry.getCreatedAt().toLocalDateTime());
            }
            return vo;
        });
    }

    @Override
    public IndustryDetailVO detail(Long id) {
        // 查询主表
        Industry industry = industryMapper.selectById(id);
        if (industry == null) {
            throw new BusinessException(404, "行业不存在");
        }

        IndustryDetailVO vo = new IndustryDetailVO();
        BeanUtils.copyProperties(industry, vo);

        // 处理时间类型转换
        if (industry.getCreatedAt() != null) {
            vo.setCreatedAt(industry.getCreatedAt().toLocalDateTime());
        }
        if (industry.getUpdatedAt() != null) {
            vo.setUpdatedAt(industry.getUpdatedAt().toLocalDateTime());
        }

        // 查询详情表
        IndustryDetail detail = industryDetailMapper.findByIndustryId(id);
        if (detail != null) {
            vo.setDetailId(detail.getId());
            vo.setShortDescription(detail.getShortDescription());
            vo.setDetailedDescription(detail.getDetailedDescription());
            vo.setIndustryScale(detail.getIndustryScale());
            vo.setIndustryTalentDemand(detail.getIndustryTalentDemand());
            vo.setIndustrySalary(detail.getIndustrySalary());
            vo.setPolicyInfo(detail.getPolicyInfo());
            vo.setDevelopmentSupportInfo(detail.getDevelopmentSupportInfo());
            vo.setTalentAnalysis(detail.getTalentAnalysis());
            vo.setTalentPolicy(detail.getTalentPolicy());
            vo.setSalaryData(detail.getSalaryData());
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(IndustryAddDTO dto) {
        // 检查行业名称是否已存在
        if (industryMapper.existsByIndustryName(dto.getIndustryName())) {
            throw new BusinessException(400, "行业名称已存在");
        }

        OffsetDateTime now = OffsetDateTime.now();
        Long industryId = SnowflakeIdGenerator.nextId();
        Long detailId = SnowflakeIdGenerator.nextId();

        // 创建主表记录
        Industry industry = Industry.builder()
                .id(industryId)
                .industryName(dto.getIndustryName())
                .category(dto.getCategory())
                .iconClass(dto.getIconClass())
                .description(dto.getDescription())
                .annualGrowthRate(dto.getAnnualGrowthRate())
                .marketScale(dto.getMarketScale())
                .talentGap(dto.getTalentGap())
                .investmentHeat(dto.getInvestmentHeat())
                .growthTrend(dto.getGrowthTrend())
                .marketTrend(dto.getMarketTrend())
                .talentTrend(dto.getTalentTrend())
                .investmentTrend(dto.getInvestmentTrend())
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        industryMapper.insert(industry);

        // 创建详情表记录
        IndustryDetail detail = IndustryDetail.builder()
                .id(detailId)
                .industryId(industryId)
                .industryName(dto.getIndustryName())
                .shortDescription(dto.getShortDescription())
                .detailedDescription(dto.getDetailedDescription())
                .industryScale(dto.getIndustryScale())
                .industryTalentDemand(dto.getIndustryTalentDemand())
                .industrySalary(dto.getIndustrySalary())
                .policyInfo(dto.getPolicyInfo())
                .developmentSupportInfo(dto.getDevelopmentSupportInfo())
                .talentAnalysis(dto.getTalentAnalysis())
                .talentPolicy(dto.getTalentPolicy())
                .salaryData(dto.getSalaryData())
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        industryDetailMapper.insert(detail);

        log.info("新增行业成功: id={}, industryName={}", industryId, dto.getIndustryName());
        return industryId;
    }

    @Override
    public void update(Long id, IndustryUpdateDTO dto) {
        Industry industry = industryMapper.selectById(id);
        if (industry == null) {
            throw new BusinessException(404, "行业不存在");
        }

        // 如果修改了行业名称，检查是否与其他行业重名
        if (!industry.getIndustryName().equals(dto.getIndustryName()) && industryMapper.existsByIndustryName(dto.getIndustryName())) {
            throw new BusinessException(400, "行业名称已存在");
        }

        industry.setIndustryName(dto.getIndustryName());
        industry.setCategory(dto.getCategory());
        industry.setIconClass(dto.getIconClass());
        industry.setDescription(dto.getDescription());
        industry.setAnnualGrowthRate(dto.getAnnualGrowthRate());
        industry.setMarketScale(dto.getMarketScale());
        industry.setTalentGap(dto.getTalentGap());
        industry.setInvestmentHeat(dto.getInvestmentHeat());
        industry.setGrowthTrend(dto.getGrowthTrend());
        industry.setMarketTrend(dto.getMarketTrend());
        industry.setTalentTrend(dto.getTalentTrend());
        industry.setInvestmentTrend(dto.getInvestmentTrend());
        if (dto.getIsDeleted() != null) {
            industry.setIsDeleted(dto.getIsDeleted());
        }
        industry.setUpdatedAt(OffsetDateTime.now());

        industryMapper.updateById(industry);

        // 同步更新详情表中的行业名称
        IndustryDetail detail = industryDetailMapper.findByIndustryId(id);
        if (detail != null) {
            detail.setIndustryName(dto.getIndustryName());
            detail.setUpdatedAt(OffsetDateTime.now());
            industryDetailMapper.updateById(detail);
        }

        log.info("更新行业成功: id={}, industryName={}", id, dto.getIndustryName());
    }

    @Override
    public void updateDetail(Long id, IndustryDetailUpdateDTO dto) {
        // 先检查行业是否存在
        Industry industry = industryMapper.selectById(id);
        if (industry == null) {
            throw new BusinessException(404, "行业不存在");
        }

        // 查找对应的详情记录
        IndustryDetail detail = industryDetailMapper.findByIndustryId(id);
        if (detail == null) {
            throw new BusinessException(404, "行业详情不存在");
        }

        detail.setShortDescription(dto.getShortDescription());
        detail.setDetailedDescription(dto.getDetailedDescription());
        detail.setIndustryScale(dto.getIndustryScale());
        detail.setIndustryTalentDemand(dto.getIndustryTalentDemand());
        detail.setIndustrySalary(dto.getIndustrySalary());
        detail.setPolicyInfo(dto.getPolicyInfo());
        detail.setDevelopmentSupportInfo(dto.getDevelopmentSupportInfo());
        detail.setTalentAnalysis(dto.getTalentAnalysis());
        detail.setTalentPolicy(dto.getTalentPolicy());
        detail.setSalaryData(dto.getSalaryData());
        if (dto.getIsDeleted() != null) {
            detail.setIsDeleted(dto.getIsDeleted());
        }
        detail.setUpdatedAt(OffsetDateTime.now());

        industryDetailMapper.updateById(detail);

        log.info("更新行业详情成功: industryId={}, detailId={}", id, detail.getId());
    }

    @Override
    public void updateStatus(Long id, IndustryStatusDTO dto) {
        Industry industry = industryMapper.selectById(id);
        if (industry == null) {
            throw new BusinessException(404, "行业不存在");
        }

        industry.setIsDeleted(dto.getIsDeleted());
        industry.setUpdatedAt(OffsetDateTime.now());

        industryMapper.updateById(industry);

        // 同步更新详情表状态
        IndustryDetail detail = industryDetailMapper.findByIndustryId(id);
        if (detail != null) {
            detail.setIsDeleted(dto.getIsDeleted());
            detail.setUpdatedAt(OffsetDateTime.now());
            industryDetailMapper.updateById(detail);
        }

        log.info("更新行业状态成功: id={}, isDeleted={}", id, dto.getIsDeleted());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Industry industry = industryMapper.selectById(id);
        if (industry == null) {
            throw new BusinessException(404, "行业不存在");
        }

        // 删除详情表
        IndustryDetail detail = industryDetailMapper.findByIndustryId(id);
        if (detail != null) {
            industryDetailMapper.deleteById(detail.getId());
        }

        // 删除主表
        industryMapper.deleteById(id);

        log.info("硬删除行业成功: id={}, industryName={}", id, industry.getIndustryName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的行业");
        }

        // 删除所有关联的详情记录
        for (Long industryId : ids) {
            IndustryDetail detail = industryDetailMapper.findByIndustryId(industryId);
            if (detail != null) {
                industryDetailMapper.deleteById(detail.getId());
            }
        }

        // 批量删除主表记录
        int deleted = industryMapper.deleteBatchIds(ids);

        log.info("批量硬删除行业成功: 删除数量={}, ids={}", deleted, ids);
    }
}
