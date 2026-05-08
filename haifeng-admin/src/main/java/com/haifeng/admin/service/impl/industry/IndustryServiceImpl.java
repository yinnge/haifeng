package com.haifeng.admin.service.impl.industry;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.industry.IndustryAddDTO;
import com.haifeng.admin.dto.industry.IndustryDetailUpdateDTO;
import com.haifeng.admin.dto.industry.IndustryQueryDTO;
import com.haifeng.admin.dto.industry.IndustryStatusDTO;
import com.haifeng.admin.dto.industry.IndustryUpdateDTO;
import com.haifeng.admin.excel.industry.*;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importIndustries(MultipartFile file) {
        List<String> errorMsgs = new ArrayList<>();

        try {
            // 读取主表数据
            List<IndustryExcelDTO> mainData = EasyExcel.read(file.getInputStream())
                    .head(IndustryExcelDTO.class)
                    .sheet(0)
                    .doReadSync();

            if (mainData == null || mainData.isEmpty()) {
                throw new BusinessException(400, "导入失败：Excel文件为空");
            }

            List<Industry> industries = new ArrayList<>();
            List<IndustryDetail> industryDetails = new ArrayList<>();
            Set<String> industryNamesInFile = new HashSet<>();

            for (int i = 0; i < mainData.size(); i++) {
                int rowNum = i + 2;
                IndustryExcelDTO data = mainData.get(i);

                // 校验必填字段
                if (!StringUtils.hasText(data.getIndustryName())) {
                    errorMsgs.add("第" + rowNum + "行：行业名称不能为空");
                    continue;
                }

                // 检查文件内重复
                if (industryNamesInFile.contains(data.getIndustryName())) {
                    errorMsgs.add("第" + rowNum + "行：行业名称'" + data.getIndustryName() + "'在文件中重复");
                    continue;
                }
                industryNamesInFile.add(data.getIndustryName());

                // 检查数据库中是否已存在
                if (industryMapper.existsByIndustryName(data.getIndustryName())) {
                    errorMsgs.add("第" + rowNum + "行：行业名称'" + data.getIndustryName() + "'已存在");
                    continue;
                }

                OffsetDateTime now = OffsetDateTime.now();
                Long industryId = SnowflakeIdGenerator.nextId();
                Long detailId = SnowflakeIdGenerator.nextId();

                Industry industry = Industry.builder()
                        .id(industryId)
                        .industryName(data.getIndustryName())
                        .category(data.getCategory())
                        .iconClass(data.getIconClass())
                        .description(data.getDescription())
                        .annualGrowthRate(data.getAnnualGrowthRate())
                        .marketScale(data.getMarketScale())
                        .talentGap(data.getTalentGap())
                        .investmentHeat(data.getInvestmentHeat())
                        .growthTrend(data.getGrowthTrend())
                        .marketTrend(data.getMarketTrend())
                        .talentTrend(data.getTalentTrend())
                        .investmentTrend(data.getInvestmentTrend())
                        .isDeleted(false)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

                IndustryDetail detail = IndustryDetail.builder()
                        .id(detailId)
                        .industryId(industryId)
                        .industryName(data.getIndustryName())
                        .isDeleted(false)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

                industries.add(industry);
                industryDetails.add(detail);
            }

            if (!errorMsgs.isEmpty()) {
                throw new BusinessException(400, "导入失败：" + String.join("；", errorMsgs));
            }

            // 批量插入
            if (!industries.isEmpty()) {
                for (Industry industry : industries) {
                    industryMapper.insert(industry);
                }
                for (IndustryDetail detail : industryDetails) {
                    industryDetailMapper.insert(detail);
                }
                log.info("导入行业主表成功，数量={}", industries.size());
            }

        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(500, "读取Excel文件失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importIndustryDetails(MultipartFile file) {
        List<String> errorMsgs = new ArrayList<>();

        try {
            // Sheet0: 详情基础字段
            List<IndustryDetailExcelDTO> detailData = EasyExcel.read(file.getInputStream())
                    .head(IndustryDetailExcelDTO.class)
                    .sheet(0)
                    .doReadSync();

            // Sheet1: industry_scale
            List<IndustryScaleExcelDTO> scaleData = EasyExcel.read(file.getInputStream())
                    .head(IndustryScaleExcelDTO.class)
                    .sheet(1)
                    .doReadSync();

            // Sheet2: industry_talent_demand
            List<TalentDemandExcelDTO> talentDemandData = EasyExcel.read(file.getInputStream())
                    .head(TalentDemandExcelDTO.class)
                    .sheet(2)
                    .doReadSync();

            // Sheet3: industry_salary
            List<IndustrySalaryExcelDTO> salaryData = EasyExcel.read(file.getInputStream())
                    .head(IndustrySalaryExcelDTO.class)
                    .sheet(3)
                    .doReadSync();

            // Sheet4: policy_info
            List<PolicyInfoExcelDTO> policyInfoData = EasyExcel.read(file.getInputStream())
                    .head(PolicyInfoExcelDTO.class)
                    .sheet(4)
                    .doReadSync();

            // Sheet5: development_support_info
            List<DevelopmentSupportExcelDTO> developmentSupportData = EasyExcel.read(file.getInputStream())
                    .head(DevelopmentSupportExcelDTO.class)
                    .sheet(5)
                    .doReadSync();

            // Sheet6: talent_analysis
            List<TalentAnalysisExcelDTO> talentAnalysisData = EasyExcel.read(file.getInputStream())
                    .head(TalentAnalysisExcelDTO.class)
                    .sheet(6)
                    .doReadSync();

            // Sheet7: talent_policy
            List<TalentPolicyExcelDTO> talentPolicyData = EasyExcel.read(file.getInputStream())
                    .head(TalentPolicyExcelDTO.class)
                    .sheet(7)
                    .doReadSync();

            // Sheet8: salary_data
            List<SalaryDataExcelDTO> salaryDataExcelList = EasyExcel.read(file.getInputStream())
                    .head(SalaryDataExcelDTO.class)
                    .sheet(8)
                    .doReadSync();

            // 按行业名称分组JSONB数据
            Map<String, Map<String, Object>> scaleMap = buildJsonbMap(scaleData,
                    dto -> dto.getIndustryName(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("scaleValue", dto.getScaleValue());
                        m.put("scaleLabel", dto.getScaleLabel());
                        m.put("scaleDescriptions", dto.getScaleDescriptions());
                        return m;
                    });

            Map<String, Map<String, Object>> talentDemandMap = buildJsonbMap(talentDemandData,
                    dto -> dto.getIndustryName(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("demandValue", dto.getDemandValue());
                        m.put("demandLabel", dto.getDemandLabel());
                        m.put("demandDescriptions", dto.getDemandDescriptions());
                        return m;
                    });

            Map<String, Map<String, Object>> salaryMap = buildJsonbMap(salaryData,
                    dto -> dto.getIndustryName(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("salaryRange", dto.getSalaryRange());
                        m.put("salaryLabel", dto.getSalaryLabel());
                        m.put("salaryDescriptions", dto.getSalaryDescriptions());
                        return m;
                    });

            Map<String, Map<String, Object>> policyInfoMap = buildJsonbMap(policyInfoData,
                    dto -> dto.getIndustryName(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("policyOverview", dto.getPolicyOverview());
                        m.put("nationalPolicies", dto.getNationalPolicies());
                        m.put("policyHighlights", dto.getPolicyHighlights());
                        return m;
                    });

            Map<String, Map<String, Object>> developmentSupportMap = buildJsonbMap(developmentSupportData,
                    dto -> dto.getIndustryName(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("regionalOverview", dto.getRegionalOverview());
                        m.put("keyCities", dto.getKeyCities());
                        m.put("cityPolicies", dto.getCityPolicies());
                        return m;
                    });

            Map<String, Map<String, Object>> talentAnalysisMap = buildJsonbMap(talentAnalysisData,
                    dto -> dto.getIndustryName(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("analysisTitle", dto.getAnalysisTitle());
                        m.put("shortagePositions", dto.getShortagePositions());
                        m.put("educationRequirement", dto.getEducationRequirement());
                        m.put("majorRequirement", dto.getMajorRequirement());
                        m.put("talentTrendDescription", dto.getTalentTrendDescription());
                        return m;
                    });

            Map<String, Map<String, Object>> talentPolicyMap = buildJsonbMap(talentPolicyData,
                    dto -> dto.getIndustryName(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("policyTitle", dto.getPolicyTitle());
                        m.put("nationalPolicies", dto.getNationalPolicies());
                        m.put("localPolicies", dto.getLocalPolicies());
                        m.put("enterpriseDescription", dto.getEnterpriseDescription());
                        return m;
                    });

            Map<String, Map<String, Object>> salaryDataMap = buildJsonbMap(salaryDataExcelList,
                    dto -> dto.getIndustryName(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("salaryAnalysisTitle", dto.getSalaryAnalysisTitle());
                        m.put("salaryAnalysisDescription", dto.getSalaryAnalysisDescription());
                        m.put("regionalSalaryTitle", dto.getRegionalSalaryTitle());
                        m.put("regionalSalaryDescription", dto.getRegionalSalaryDescription());
                        m.put("salaryTrendAnalysis", dto.getSalaryTrendAnalysis());
                        return m;
                    });

            // 缓存行业ID
            Map<String, Long> industryIdCache = new HashMap<>();

            // 处理详情基础字段
            int updatedCount = 0;
            for (int i = 0; i < detailData.size(); i++) {
                int rowNum = i + 2;
                IndustryDetailExcelDTO data = detailData.get(i);

                if (!StringUtils.hasText(data.getIndustryName())) {
                    errorMsgs.add("Sheet0第" + rowNum + "行：行业名称不能为空");
                    continue;
                }

                // 查询行业ID
                Long industryId = industryIdCache.get(data.getIndustryName());
                if (industryId == null) {
                    LambdaQueryWrapper<Industry> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(Industry::getIndustryName, data.getIndustryName())
                           .eq(Industry::getIsDeleted, false);
                    Industry industry = industryMapper.selectOne(wrapper);
                    if (industry == null) {
                        errorMsgs.add("Sheet0第" + rowNum + "行：行业名称'" + data.getIndustryName() + "'不存在");
                        continue;
                    }
                    industryId = industry.getId();
                    industryIdCache.put(data.getIndustryName(), industryId);
                }

                // 查询详情记录
                IndustryDetail detail = industryDetailMapper.findByIndustryId(industryId);
                if (detail == null) {
                    errorMsgs.add("Sheet0第" + rowNum + "行：行业'" + data.getIndustryName() + "'的详情记录不存在");
                    continue;
                }

                // 更新详情基础字段
                detail.setShortDescription(data.getShortDescription());
                detail.setDetailedDescription(data.getDetailedDescription());

                // 设置JSONB字段
                detail.setIndustryScale(scaleMap.get(data.getIndustryName()));
                detail.setIndustryTalentDemand(talentDemandMap.get(data.getIndustryName()));
                detail.setIndustrySalary(salaryMap.get(data.getIndustryName()));
                detail.setPolicyInfo(policyInfoMap.get(data.getIndustryName()));
                detail.setDevelopmentSupportInfo(developmentSupportMap.get(data.getIndustryName()));
                detail.setTalentAnalysis(talentAnalysisMap.get(data.getIndustryName()));
                detail.setTalentPolicy(talentPolicyMap.get(data.getIndustryName()));
                detail.setSalaryData(salaryDataMap.get(data.getIndustryName()));

                detail.setUpdatedAt(OffsetDateTime.now());
                industryDetailMapper.updateById(detail);
                updatedCount++;
            }

            if (!errorMsgs.isEmpty()) {
                throw new BusinessException(400, "导入失败：" + String.join("；", errorMsgs));
            }

            log.info("导入行业详情成功，更新数量={}", updatedCount);

        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(500, "读取Excel文件失败");
        }
    }

    /**
     * 构建JSONB Map的辅助方法
     */
    private <T> Map<String, Map<String, Object>> buildJsonbMap(
            List<T> dataList,
            java.util.function.Function<T, String> keyExtractor,
            java.util.function.Function<T, Map<String, Object>> valueExtractor) {
        Map<String, Map<String, Object>> result = new HashMap<>();
        if (dataList != null) {
            for (T data : dataList) {
                String key = keyExtractor.apply(data);
                if (StringUtils.hasText(key)) {
                    result.put(key, valueExtractor.apply(data));
                }
            }
        }
        return result;
    }
}
