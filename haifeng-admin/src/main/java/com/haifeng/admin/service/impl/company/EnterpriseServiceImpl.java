package com.haifeng.admin.service.impl.company;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.company.*;
import com.haifeng.admin.excel.company.EnterpriseExcelDTO;
import com.haifeng.admin.excel.company.EnterprisePositionExcelDTO;
import com.haifeng.admin.service.company.EnterpriseService;
import com.haifeng.admin.vo.company.EnterpriseDetailVO;
import com.haifeng.admin.vo.company.EnterpriseListVO;
import com.haifeng.admin.vo.company.EnterprisePositionVO;
import com.haifeng.common.entity.company.Enterprise;
import com.haifeng.common.entity.company.EnterprisePosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.company.EnterpriseIndustryMapper;
import com.haifeng.common.mapper.company.EnterpriseMapper;
import com.haifeng.common.mapper.company.EnterprisePositionMapper;
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
import java.time.ZoneOffset;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnterpriseServiceImpl implements EnterpriseService {

    private final EnterpriseMapper enterpriseMapper;
    private final EnterprisePositionMapper enterprisePositionMapper;
    private final EnterpriseIndustryMapper enterpriseIndustryMapper;

    private static final Set<String> VALID_ENTERPRISE_NATURES = Set.of("央企", "国企", "民企", "外企", "合资");
    private static final Set<String> VALID_RECRUITMENT_TYPES = Set.of("校招", "社招", "实习");
    private static final Set<String> VALID_EDUCATION_REQUIREMENTS = Set.of("不限", "大专", "本科", "硕士", "博士");

    @Override
    public IPage<EnterpriseListVO> page(EnterpriseQueryDTO dto) {
        Page<Enterprise> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();

        // 城市名称模糊查询
        if (StringUtils.hasText(dto.getCityName())) {
            wrapper.like(Enterprise::getCityName, dto.getCityName());
        }
        // 企业名称模糊查询
        if (StringUtils.hasText(dto.getEnterpriseName())) {
            wrapper.like(Enterprise::getEnterpriseName, dto.getEnterpriseName());
        }
        // 企业类型模糊查询
        if (StringUtils.hasText(dto.getEnterpriseType())) {
            wrapper.like(Enterprise::getEnterpriseType, dto.getEnterpriseType());
        }
        // 企业性质精准查询
        if (StringUtils.hasText(dto.getEnterpriseNature())) {
            wrapper.eq(Enterprise::getEnterpriseNature, dto.getEnterpriseNature());
        }
        // 招聘状态精准查询
        if (StringUtils.hasText(dto.getRecruitmentStatus())) {
            wrapper.eq(Enterprise::getRecruitmentStatus, dto.getRecruitmentStatus());
        }
        // 删除状态筛选
        if (dto.getIsDeleted() != null) {
            wrapper.eq(Enterprise::getIsDeleted, dto.getIsDeleted());
        }

        // 按创建时间降序
        wrapper.orderByDesc(Enterprise::getCreatedAt);

        IPage<Enterprise> enterprisePage = enterpriseMapper.selectPage(page, wrapper);

        return enterprisePage.convert(enterprise -> {
            EnterpriseListVO vo = new EnterpriseListVO();
            BeanUtils.copyProperties(enterprise, vo);
            // 处理时间类型转换
            if (enterprise.getCreatedAt() != null) {
                vo.setCreatedAt(enterprise.getCreatedAt().toLocalDateTime());
            }
            return vo;
        });
    }

    @Override
    public EnterpriseDetailVO detail(Long id) {
        // 查询企业主表
        Enterprise enterprise = enterpriseMapper.selectById(id);
        if (enterprise == null) {
            throw new BusinessException(404, "企业不存在");
        }

        EnterpriseDetailVO vo = new EnterpriseDetailVO();
        BeanUtils.copyProperties(enterprise, vo);

        // 处理时间类型转换
        if (enterprise.getCreatedAt() != null) {
            vo.setCreatedAt(enterprise.getCreatedAt().toLocalDateTime());
        }
        if (enterprise.getUpdatedAt() != null) {
            vo.setUpdatedAt(enterprise.getUpdatedAt().toLocalDateTime());
        }

        // 查询关联的岗位列表
        LambdaQueryWrapper<EnterprisePosition> positionWrapper = new LambdaQueryWrapper<>();
        positionWrapper.eq(EnterprisePosition::getEnterpriseId, id)
                       .eq(EnterprisePosition::getIsDeleted, false)
                       .orderByDesc(EnterprisePosition::getCreatedAt);
        List<EnterprisePosition> positions = enterprisePositionMapper.selectList(positionWrapper);

        List<EnterprisePositionVO> positionVOs = new ArrayList<>();
        for (EnterprisePosition position : positions) {
            EnterprisePositionVO positionVO = new EnterprisePositionVO();
            BeanUtils.copyProperties(position, positionVO);
            // 处理时间类型转换
            if (position.getCreatedAt() != null) {
                positionVO.setCreatedAt(position.getCreatedAt().toLocalDateTime());
            }
            if (position.getUpdatedAt() != null) {
                positionVO.setUpdatedAt(position.getUpdatedAt().toLocalDateTime());
            }
            if (position.getDeadline() != null) {
                positionVO.setDeadline(position.getDeadline().toLocalDateTime());
            }
            positionVOs.add(positionVO);
        }
        vo.setPositions(positionVOs);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(EnterpriseAddDTO dto) {
        // 检查企业名称是否已存在
        if (enterpriseMapper.existsByEnterpriseName(dto.getEnterpriseName())) {
            throw new BusinessException(400, "企业名称已存在");
        }

        OffsetDateTime now = OffsetDateTime.now();
        Long enterpriseId = SnowflakeIdGenerator.nextId();

        Enterprise enterprise = Enterprise.builder()
                .id(enterpriseId)
                .cityName(dto.getCityName())
                .enterpriseName(dto.getEnterpriseName())
                .enterpriseNature(dto.getEnterpriseNature())
                .enterpriseType(dto.getEnterpriseType())
                .logoUrl(dto.getLogoUrl())
                .officialWebsite(dto.getOfficialWebsite())
                .region(dto.getRegion())
                .enterpriseScale(dto.getEnterpriseScale())
                .mainBusiness(dto.getMainBusiness())
                .enterpriseIntro(dto.getEnterpriseIntro())
                .recruitmentStatus(dto.getRecruitmentStatus())
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        enterpriseMapper.insert(enterprise);

        log.info("新增企业成功: id={}, enterpriseName={}", enterpriseId, dto.getEnterpriseName());
        return enterpriseId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, EnterpriseUpdateDTO dto) {
        Enterprise enterprise = enterpriseMapper.selectById(id);
        if (enterprise == null) {
            throw new BusinessException(404, "企业不存在");
        }

        // 如果修改了企业名称，检查是否与其他企业重名
        if (!enterprise.getEnterpriseName().equals(dto.getEnterpriseName())
                && enterpriseMapper.existsByEnterpriseName(dto.getEnterpriseName())) {
            throw new BusinessException(400, "企业名称已存在");
        }

        enterprise.setCityName(dto.getCityName());
        enterprise.setEnterpriseName(dto.getEnterpriseName());
        enterprise.setEnterpriseNature(dto.getEnterpriseNature());
        enterprise.setEnterpriseType(dto.getEnterpriseType());
        enterprise.setLogoUrl(dto.getLogoUrl());
        enterprise.setOfficialWebsite(dto.getOfficialWebsite());
        enterprise.setRegion(dto.getRegion());
        enterprise.setEnterpriseScale(dto.getEnterpriseScale());
        enterprise.setMainBusiness(dto.getMainBusiness());
        enterprise.setEnterpriseIntro(dto.getEnterpriseIntro());
        enterprise.setRecruitmentStatus(dto.getRecruitmentStatus());
        enterprise.setUpdatedAt(OffsetDateTime.now());

        enterpriseMapper.updateById(enterprise);

        log.info("更新企业成功: id={}, enterpriseName={}", id, dto.getEnterpriseName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, EnterpriseStatusDTO dto) {
        Enterprise enterprise = enterpriseMapper.selectById(id);
        if (enterprise == null) {
            throw new BusinessException(404, "企业不存在");
        }

        enterprise.setIsDeleted(dto.getIsDeleted());
        enterprise.setUpdatedAt(OffsetDateTime.now());

        enterpriseMapper.updateById(enterprise);

        log.info("更新企业状态成功: id={}, isDeleted={}", id, dto.getIsDeleted());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Enterprise enterprise = enterpriseMapper.selectById(id);
        if (enterprise == null) {
            throw new BusinessException(404, "企业不存在");
        }

        // 删除关联的岗位
        enterprisePositionMapper.deleteByEnterpriseId(id);

        // 删除企业行业关联
        enterpriseIndustryMapper.deleteByEnterpriseIds(List.of(id));

        // 删除企业主表
        enterpriseMapper.deleteById(id);

        log.info("硬删除企业成功: id={}, enterpriseName={}", id, enterprise.getEnterpriseName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的企业");
        }

        // 批量删除关联的岗位
        enterprisePositionMapper.deleteByEnterpriseIds(ids);

        // 批量删除企业行业关联
        enterpriseIndustryMapper.deleteByEnterpriseIds(ids);

        // 批量删除企业主表
        int deleted = enterpriseMapper.deleteBatchIds(ids);

        log.info("批量硬删除企业成功: 删除数量={}, ids={}", deleted, ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importEnterprises(MultipartFile file) {
        List<String> errorMsgs = new ArrayList<>();

        try {
            // Sheet0: 企业主表数据
            List<EnterpriseExcelDTO> enterpriseData = EasyExcel.read(file.getInputStream())
                    .head(EnterpriseExcelDTO.class)
                    .sheet(0)
                    .doReadSync();

            // Sheet1: 岗位数据
            List<EnterprisePositionExcelDTO> positionData = EasyExcel.read(file.getInputStream())
                    .head(EnterprisePositionExcelDTO.class)
                    .sheet(1)
                    .doReadSync();

            if (enterpriseData == null || enterpriseData.isEmpty()) {
                throw new BusinessException(400, "导入失败：企业数据Sheet为空");
            }

            // 用于检查文件内企业名称重复
            Set<String> enterpriseNamesInFile = new HashSet<>();
            // 企业名称到企业ID的映射（用于岗位关联）
            Map<String, Long> enterpriseNameToIdMap = new HashMap<>();
            // 待插入的企业列表
            List<Enterprise> enterprises = new ArrayList<>();
            // 待插入的岗位列表
            List<EnterprisePosition> positions = new ArrayList<>();

            OffsetDateTime now = OffsetDateTime.now();

            // 1. 校验并处理企业数据（Sheet0）
            for (int i = 0; i < enterpriseData.size(); i++) {
                int rowNum = i + 2;
                EnterpriseExcelDTO data = enterpriseData.get(i);

                // 校验企业名称必填
                if (!StringUtils.hasText(data.getEnterpriseName())) {
                    errorMsgs.add("Sheet0第" + rowNum + "行：企业名称不能为空");
                    continue;
                }

                // 检查文件内重复
                if (enterpriseNamesInFile.contains(data.getEnterpriseName())) {
                    errorMsgs.add("Sheet0第" + rowNum + "行：企业名称'" + data.getEnterpriseName() + "'在文件中重复");
                    continue;
                }
                enterpriseNamesInFile.add(data.getEnterpriseName());

                // 检查数据库中是否已存在
                if (enterpriseMapper.existsByEnterpriseName(data.getEnterpriseName())) {
                    errorMsgs.add("Sheet0第" + rowNum + "行：企业名称'" + data.getEnterpriseName() + "'已存在于数据库中");
                    continue;
                }

                // 校验企业性质
                if (StringUtils.hasText(data.getEnterpriseNature())
                        && !VALID_ENTERPRISE_NATURES.contains(data.getEnterpriseNature())) {
                    errorMsgs.add("Sheet0第" + rowNum + "行：企业性质'" + data.getEnterpriseNature()
                            + "'不合法，必须是：央企、国企、民企、外企、合资");
                    continue;
                }

                Long enterpriseId = SnowflakeIdGenerator.nextId();
                enterpriseNameToIdMap.put(data.getEnterpriseName(), enterpriseId);

                Enterprise enterprise = Enterprise.builder()
                        .id(enterpriseId)
                        .cityName(data.getCityName())
                        .enterpriseName(data.getEnterpriseName())
                        .enterpriseNature(data.getEnterpriseNature())
                        .enterpriseType(data.getEnterpriseType())
                        .logoUrl(data.getLogoUrl())
                        .officialWebsite(data.getOfficialWebsite())
                        .region(data.getRegion())
                        .enterpriseScale(data.getEnterpriseScale())
                        .mainBusiness(data.getMainBusiness())
                        .enterpriseIntro(data.getEnterpriseIntro())
                        .recruitmentStatus(data.getRecruitmentStatus())
                        .isDeleted(false)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

                enterprises.add(enterprise);
            }

            // 2. 校验并处理岗位数据（Sheet1）
            if (positionData != null && !positionData.isEmpty()) {
                for (int i = 0; i < positionData.size(); i++) {
                    int rowNum = i + 2;
                    EnterprisePositionExcelDTO data = positionData.get(i);

                    // 校验企业名称必填
                    if (!StringUtils.hasText(data.getEnterpriseName())) {
                        errorMsgs.add("Sheet1第" + rowNum + "行：企业名称不能为空");
                        continue;
                    }

                    // 校验企业名称必须在Sheet0中存在
                    if (!enterpriseNameToIdMap.containsKey(data.getEnterpriseName())) {
                        errorMsgs.add("Sheet1第" + rowNum + "行：企业名称'" + data.getEnterpriseName()
                                + "'在企业数据Sheet中不存在");
                        continue;
                    }

                    // 校验招聘类型（如果非空）
                    if (StringUtils.hasText(data.getRecruitmentType())
                            && !VALID_RECRUITMENT_TYPES.contains(data.getRecruitmentType())) {
                        errorMsgs.add("Sheet1第" + rowNum + "行：招聘类型'" + data.getRecruitmentType()
                                + "'不合法，必须是：校招、社招、实习");
                        continue;
                    }

                    // 校验学历要求（如果非空）
                    if (StringUtils.hasText(data.getEducationRequirement())
                            && !VALID_EDUCATION_REQUIREMENTS.contains(data.getEducationRequirement())) {
                        errorMsgs.add("Sheet1第" + rowNum + "行：学历要求'" + data.getEducationRequirement()
                                + "'不合法，必须是：不限、大专、本科、硕士、博士");
                        continue;
                    }

                    Long enterpriseId = enterpriseNameToIdMap.get(data.getEnterpriseName());
                    Long positionId = SnowflakeIdGenerator.nextId();

                    EnterprisePosition position = EnterprisePosition.builder()
                            .id(positionId)
                            .enterpriseId(enterpriseId)
                            .positionName(data.getPositionName())
                            .recruitmentType(data.getRecruitmentType())
                            .positionRequirement(data.getPositionRequirement())
                            .positionTags(data.getPositionTags())
                            .province(data.getProvince())
                            .city(data.getCity())
                            .workLocation(data.getWorkLocation())
                            .educationRequirement(data.getEducationRequirement())
                            .majorRequirement(data.getMajorRequirement())
                            .workExperience(data.getWorkExperience())
                            .salaryMin(data.getSalaryMin())
                            .salaryMax(data.getSalaryMax())
                            .applyLink(data.getApplyLink())
                            .deadline(data.getDeadline() != null
                                    ? data.getDeadline().atOffset(ZoneOffset.ofHours(8))
                                    : null)
                            .positionStatus(data.getPositionStatus())
                            .isDeleted(false)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();

                    positions.add(position);
                }
            }

            // 如果有错误，抛出异常
            if (!errorMsgs.isEmpty()) {
                throw new BusinessException(400, "导入失败：" + String.join("；", errorMsgs));
            }

            // 3. 批量插入企业
            if (!enterprises.isEmpty()) {
                enterpriseMapper.insertBatch(enterprises);
            }
            log.info("导入企业成功，数量={}", enterprises.size());

            // 4. 批量插入岗位
            if (!positions.isEmpty()) {
                enterprisePositionMapper.insertBatch(positions);
            }
            log.info("导入企业岗位成功，数量={}", positions.size());

        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(500, "读取Excel文件失败");
        } catch (Exception e) {
            log.error("导入企业数据失败", e);
            throw new BusinessException(500, "导入企业数据失败：" + e.getMessage());
        }
    }
}
