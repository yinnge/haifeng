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
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndustryServiceImpl implements IndustryService {

    private final IndustryMapper industryMapper;
    private final IndustryDetailMapper industryDetailMapper;

    private static final int MAX_IMPORT_ROWS = 500;
    private static final Set<String> VALID_TRENDS = Set.of("上升", "稳定", "下降");
    private static final BigDecimal NEGATIVE_HUNDRED = new BigDecimal("-100");
    private static final BigDecimal THOUSAND = new BigDecimal("1000");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

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
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
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
        detail.setUpdatedAt(OffsetDateTime.now());

        industryDetailMapper.updateById(detail);

        log.info("更新行业详情成功: industryId={}, detailId={}", id, detail.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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

        // 删除详情表（不关心 is_deleted 状态，全部硬删）
        industryDetailMapper.deleteByIndustryIds(List.of(id));

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

        // 批量删除详情表
        industryDetailMapper.deleteByIndustryIds(ids);

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
                    .sheet("行业主表导入")
                    .doReadSync();

            if (mainData == null || mainData.isEmpty()) {
                throw new BusinessException(400, "导入失败：Excel文件为空");
            }

            if (mainData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：单次导入数量不能超过" + MAX_IMPORT_ROWS + "行");
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

                // 校验字段长度
                if (data.getIndustryName().length() > 100) {
                    errorMsgs.add("第" + rowNum + "行：行业名称长度不能超过100个字符");
                    continue;
                }
                if (StringUtils.hasText(data.getCategory()) && data.getCategory().length() > 50) {
                    errorMsgs.add("第" + rowNum + "行：行业分类长度不能超过50个字符");
                    continue;
                }
                if (StringUtils.hasText(data.getIconClass()) && data.getIconClass().length() > 100) {
                    errorMsgs.add("第" + rowNum + "行：图标类名长度不能超过100个字符");
                    continue;
                }
                if (StringUtils.hasText(data.getMarketScale()) && data.getMarketScale().length() > 50) {
                    errorMsgs.add("第" + rowNum + "行：市场规模长度不能超过50个字符");
                    continue;
                }
                if (StringUtils.hasText(data.getTalentGap()) && data.getTalentGap().length() > 50) {
                    errorMsgs.add("第" + rowNum + "行：人才缺口长度不能超过50个字符");
                    continue;
                }

                // 校验枚举值
                if (StringUtils.hasText(data.getGrowthTrend()) && !VALID_TRENDS.contains(data.getGrowthTrend())) {
                    errorMsgs.add("第" + rowNum + "行：增长趋势'" + data.getGrowthTrend() + "'不合法，必须是：上升、稳定、下降");
                    continue;
                }
                if (StringUtils.hasText(data.getMarketTrend()) && !VALID_TRENDS.contains(data.getMarketTrend())) {
                    errorMsgs.add("第" + rowNum + "行：市场趋势'" + data.getMarketTrend() + "'不合法，必须是：上升、稳定、下降");
                    continue;
                }
                if (StringUtils.hasText(data.getTalentTrend()) && !VALID_TRENDS.contains(data.getTalentTrend())) {
                    errorMsgs.add("第" + rowNum + "行：人才趋势'" + data.getTalentTrend() + "'不合法，必须是：上升、稳定、下降");
                    continue;
                }
                if (StringUtils.hasText(data.getInvestmentTrend()) && !VALID_TRENDS.contains(data.getInvestmentTrend())) {
                    errorMsgs.add("第" + rowNum + "行：投资趋势'" + data.getInvestmentTrend() + "'不合法，必须是：上升、稳定、下降");
                    continue;
                }

                // 校验数值范围
                if (data.getAnnualGrowthRate() != null) {
                    BigDecimal rate = data.getAnnualGrowthRate();
                    if (rate.compareTo(NEGATIVE_HUNDRED) < 0 || rate.compareTo(THOUSAND) > 0) {
                        errorMsgs.add("第" + rowNum + "行：年增长率必须在-100到1000之间");
                        continue;
                    }
                }
                if (data.getInvestmentHeat() != null) {
                    BigDecimal heat = data.getInvestmentHeat();
                    if (heat.compareTo(BigDecimal.ZERO) < 0 || heat.compareTo(HUNDRED) > 0) {
                        errorMsgs.add("第" + rowNum + "行：投资热度必须在0-100之间");
                        continue;
                    }
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
                industryMapper.insertBatch(industries);
                industryDetailMapper.insertBatch(industryDetails);
                log.info("导入行业主表成功，数量={}", industries.size());
            }

        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(500, "读取Excel文件失败");
        } catch (Exception e) {
            log.error("导入行业数据失败", e);
            throw new BusinessException(400, "解析Excel数据失败，请检查Excel格式和数据类型是否正确");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importIndustryDetails(MultipartFile file) {
        List<String> errorMsgs = new ArrayList<>();

        try {
            // Sheet: 详情基础字段
            List<IndustryDetailExcelDTO> detailData = EasyExcel.read(file.getInputStream())
                    .head(IndustryDetailExcelDTO.class)
                    .sheet("详情基础字段")
                    .doReadSync();

            // Sheet: 发展规模
            List<IndustryScaleExcelDTO> scaleData = EasyExcel.read(file.getInputStream())
                    .head(IndustryScaleExcelDTO.class)
                    .sheet("发展规模")
                    .doReadSync();

            // Sheet: 人才需求
            List<TalentDemandExcelDTO> talentDemandData = EasyExcel.read(file.getInputStream())
                    .head(TalentDemandExcelDTO.class)
                    .sheet("人才需求")
                    .doReadSync();

            // Sheet: 行业薪资
            List<IndustrySalaryExcelDTO> salaryData = EasyExcel.read(file.getInputStream())
                    .head(IndustrySalaryExcelDTO.class)
                    .sheet("行业薪资")
                    .doReadSync();

            // Sheet: 政策信息
            List<PolicyInfoExcelDTO> policyInfoData = EasyExcel.read(file.getInputStream())
                    .head(PolicyInfoExcelDTO.class)
                    .sheet("政策信息")
                    .doReadSync();

            // Sheet: 发展支持
            List<DevelopmentSupportExcelDTO> developmentSupportData = EasyExcel.read(file.getInputStream())
                    .head(DevelopmentSupportExcelDTO.class)
                    .sheet("发展支持")
                    .doReadSync();

            // Sheet: 人才分析
            List<TalentAnalysisExcelDTO> talentAnalysisData = EasyExcel.read(file.getInputStream())
                    .head(TalentAnalysisExcelDTO.class)
                    .sheet("人才分析")
                    .doReadSync();

            // Sheet: 人才政策
            List<TalentPolicyExcelDTO> talentPolicyData = EasyExcel.read(file.getInputStream())
                    .head(TalentPolicyExcelDTO.class)
                    .sheet("人才政策")
                    .doReadSync();

            // Sheet: 薪资数据
            List<SalaryDataExcelDTO> salaryDataExcelList = EasyExcel.read(file.getInputStream())
                    .head(SalaryDataExcelDTO.class)
                    .sheet("薪资数据")
                    .doReadSync();

            // 行数检查
            if (detailData != null && detailData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：详情基础字段Sheet数据不能超过" + MAX_IMPORT_ROWS + "行");
            }
            if (scaleData != null && scaleData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：发展规模Sheet数据不能超过" + MAX_IMPORT_ROWS + "行");
            }
            if (talentDemandData != null && talentDemandData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：人才需求Sheet数据不能超过" + MAX_IMPORT_ROWS + "行");
            }
            if (salaryData != null && salaryData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：行业薪资Sheet数据不能超过" + MAX_IMPORT_ROWS + "行");
            }
            if (policyInfoData != null && policyInfoData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：政策信息Sheet数据不能超过" + MAX_IMPORT_ROWS + "行");
            }
            if (developmentSupportData != null && developmentSupportData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：发展支持Sheet数据不能超过" + MAX_IMPORT_ROWS + "行");
            }
            if (talentAnalysisData != null && talentAnalysisData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：人才分析Sheet数据不能超过" + MAX_IMPORT_ROWS + "行");
            }
            if (talentPolicyData != null && talentPolicyData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：人才政策Sheet数据不能超过" + MAX_IMPORT_ROWS + "行");
            }
            if (salaryDataExcelList != null && salaryDataExcelList.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：薪资数据Sheet数据不能超过" + MAX_IMPORT_ROWS + "行");
            }

            // Sheet1~Sheet8 文件内去重检查
            validateSheetDedup(scaleData, "发展规模", errorMsgs, IndustryScaleExcelDTO::getIndustryName);
            validateSheetDedup(talentDemandData, "人才需求", errorMsgs, TalentDemandExcelDTO::getIndustryName);
            validateSheetDedup(salaryData, "行业薪资", errorMsgs, IndustrySalaryExcelDTO::getIndustryName);
            validateSheetDedup(policyInfoData, "政策信息", errorMsgs, PolicyInfoExcelDTO::getIndustryName);
            validateSheetDedup(developmentSupportData, "发展支持", errorMsgs, DevelopmentSupportExcelDTO::getIndustryName);
            validateSheetDedup(talentAnalysisData, "人才分析", errorMsgs, TalentAnalysisExcelDTO::getIndustryName);
            validateSheetDedup(talentPolicyData, "人才政策", errorMsgs, TalentPolicyExcelDTO::getIndustryName);
            validateSheetDedup(salaryDataExcelList, "薪资数据", errorMsgs, SalaryDataExcelDTO::getIndustryName);

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
            // 文件内去重检查
            Set<String> detailNamesInFile = new HashSet<>();

            // 处理详情基础字段
            int updatedCount = 0;
            for (int i = 0; i < detailData.size(); i++) {
                int rowNum = i + 2;
                IndustryDetailExcelDTO data = detailData.get(i);

                if (!StringUtils.hasText(data.getIndustryName())) {
                    errorMsgs.add("Sheet0第" + rowNum + "行：行业名称不能为空");
                    continue;
                }

                // 检查文件内重复
                if (detailNamesInFile.contains(data.getIndustryName())) {
                    errorMsgs.add("Sheet0第" + rowNum + "行：行业名称'" + data.getIndustryName() + "'在文件中重复");
                    continue;
                }
                detailNamesInFile.add(data.getIndustryName());

                // 校验字段长度
                if (StringUtils.hasText(data.getShortDescription()) && data.getShortDescription().length() > 500) {
                    errorMsgs.add("Sheet0第" + rowNum + "行：简短描述长度不能超过500个字符");
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

            // Sheet1~Sheet8 孤儿数据校验
            validateSheetOrphan(scaleData, "发展规模", errorMsgs, IndustryScaleExcelDTO::getIndustryName, detailNamesInFile);
            validateSheetOrphan(talentDemandData, "人才需求", errorMsgs, TalentDemandExcelDTO::getIndustryName, detailNamesInFile);
            validateSheetOrphan(salaryData, "行业薪资", errorMsgs, IndustrySalaryExcelDTO::getIndustryName, detailNamesInFile);
            validateSheetOrphan(policyInfoData, "政策信息", errorMsgs, PolicyInfoExcelDTO::getIndustryName, detailNamesInFile);
            validateSheetOrphan(developmentSupportData, "发展支持", errorMsgs, DevelopmentSupportExcelDTO::getIndustryName, detailNamesInFile);
            validateSheetOrphan(talentAnalysisData, "人才分析", errorMsgs, TalentAnalysisExcelDTO::getIndustryName, detailNamesInFile);
            validateSheetOrphan(talentPolicyData, "人才政策", errorMsgs, TalentPolicyExcelDTO::getIndustryName, detailNamesInFile);
            validateSheetOrphan(salaryDataExcelList, "薪资数据", errorMsgs, SalaryDataExcelDTO::getIndustryName, detailNamesInFile);

            if (!errorMsgs.isEmpty()) {
                throw new BusinessException(400, "导入失败：" + String.join("；", errorMsgs));
            }

            log.info("导入行业详情成功，更新数量={}", updatedCount);

        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(500, "读取Excel文件失败");
        } catch (Exception e) {
            log.error("导入行业详情数据失败", e);
            throw new BusinessException(400, "解析Excel数据失败，请检查Excel格式和数据类型是否正确");
        }
    }

    /**
     * 校验Sheet内行业名称是否重复
     */
    private <T> void validateSheetDedup(List<T> dataList, String sheetName,
                                         List<String> errorMsgs,
                                         java.util.function.Function<T, String> nameExtractor) {
        if (dataList == null) return;
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < dataList.size(); i++) {
            String name = nameExtractor.apply(dataList.get(i));
            if (StringUtils.hasText(name) && !seen.add(name)) {
                errorMsgs.add(sheetName + "第" + (i + 2) + "行：行业名称'" + name + "'在文件中重复");
            }
        }
    }

    /**
     * 校验Sheet中行业名称是否都在Sheet0中存在
     */
    private <T> void validateSheetOrphan(List<T> dataList, String sheetName,
                                          List<String> errorMsgs,
                                          java.util.function.Function<T, String> nameExtractor,
                                          Set<String> validNames) {
        if (dataList == null) return;
        for (int i = 0; i < dataList.size(); i++) {
            String name = nameExtractor.apply(dataList.get(i));
            if (StringUtils.hasText(name) && !validNames.contains(name)) {
                errorMsgs.add(sheetName + "第" + (i + 2) + "行：行业名称'" + name + "'在Sheet0中不存在");
            }
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
