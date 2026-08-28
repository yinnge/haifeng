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
import com.haifeng.admin.vo.major.ImportResultVO;
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
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnterpriseServiceImpl implements EnterpriseService {

    private final EnterpriseMapper enterpriseMapper;
    private final EnterprisePositionMapper enterprisePositionMapper;
    private final EnterpriseIndustryMapper enterpriseIndustryMapper;

    private static final int MAX_IMPORT_ROWS = 500;
    private static final int MAX_ERROR_DISPLAY = 50;
    private static final Set<String> VALID_ENTERPRISE_NATURES = Set.of("央企", "国企", "民企", "外企", "合资");
    private static final Set<String> VALID_RECRUITMENT_TYPES = Set.of("校招", "社招", "实习");
    private static final Set<String> VALID_EDUCATION_REQUIREMENTS = Set.of("不限", "大专", "本科", "硕士", "博士");
    private static final Set<String> VALID_POSITION_STATUSES = Set.of("招聘中", "已结束");

    @Override
    public IPage<EnterpriseListVO> page(EnterpriseQueryDTO dto) {
        Page<Enterprise> page = new Page<>(dto.getPage(), dto.getSize());

        Map<String, Object> params = new HashMap<>();
        if (StringUtils.hasText(dto.getCityName())) {
            params.put("cityName", dto.getCityName());
        }
        if (StringUtils.hasText(dto.getEnterpriseName())) {
            params.put("enterpriseName", dto.getEnterpriseName());
        }
        if (StringUtils.hasText(dto.getEnterpriseType())) {
            params.put("enterpriseType", dto.getEnterpriseType());
        }
        if (StringUtils.hasText(dto.getEnterpriseNature())) {
            params.put("enterpriseNature", dto.getEnterpriseNature());
        }
        if (StringUtils.hasText(dto.getRecruitmentStatus())) {
            params.put("recruitmentStatus", dto.getRecruitmentStatus());
        }
        if (dto.getIsDeleted() != null) {
            params.put("isDeleted", dto.getIsDeleted());
        }

        IPage<Enterprise> enterprisePage = enterpriseMapper.selectPageCustom(page, params);

        return enterprisePage.convert(enterprise -> {
            EnterpriseListVO vo = new EnterpriseListVO();
            BeanUtils.copyProperties(enterprise, vo);
            if (enterprise.getCreatedAt() != null) {
                vo.setCreatedAt(enterprise.getCreatedAt().toLocalDateTime());
            }
            return vo;
        });
    }

    @Override
    public EnterpriseDetailVO detail(Long id) {
        // 查询企业主表
        Enterprise enterprise = enterpriseMapper.selectByIdCustom(id);
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
        Enterprise enterprise = enterpriseMapper.selectByIdCustom(id);
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

        enterpriseMapper.updateEntityById(enterprise);

        log.info("更新企业成功: id={}, enterpriseName={}", id, dto.getEnterpriseName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, EnterpriseStatusDTO dto) {
        Enterprise enterprise = enterpriseMapper.selectByIdCustom(id);
        if (enterprise == null) {
            throw new BusinessException(404, "企业不存在");
        }

        enterpriseMapper.updateIsDeletedById(id, dto.getIsDeleted());

        log.info("更新企业状态成功: id={}, isDeleted={}", id, dto.getIsDeleted());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Enterprise enterprise = enterpriseMapper.selectByIdCustom(id);
        if (enterprise == null) {
            throw new BusinessException(404, "企业不存在");
        }

        // 删除关联的岗位
        enterprisePositionMapper.deleteByEnterpriseId(id);

        // 删除企业行业关联
        enterpriseIndustryMapper.deleteByEnterpriseIds(List.of(id));

        // 硬删除企业主表
        enterpriseMapper.deletePhysicallyById(id);

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

        // 批量硬删除企业主表
        enterpriseMapper.deletePhysicallyBatch(ids);
        int deleted = ids.size();

        log.info("批量硬删除企业成功: 删除数量={}, ids={}", deleted, ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importEnterprises(MultipartFile file) {
        List<String> errorMsgs = new ArrayList<>();
        // 企业主表：待新增 / 待更新（补空）
        List<Enterprise> enterprisesToInsert = new ArrayList<>();
        List<Enterprise> enterprisesToUpdate = new ArrayList<>();
        // 岗位：待新增 / 待更新（补空）
        List<EnterprisePosition> positionsToInsert = new ArrayList<>();
        List<EnterprisePosition> positionsToUpdate = new ArrayList<>();
        // 企业名称 -> 企业ID（涵盖本文件新增 + 库中已有）
        Map<String, Long> enterpriseNameToIdMap = new HashMap<>();
        // 文件内企业名称重复检查
        Set<String> enterpriseNamesInFile = new HashSet<>();
        // 文件内岗位 (enterpriseId, positionName) 重复检查
        Set<String> positionKeysInFile = new HashSet<>();
        int insertCount = 0;
        int updatedCount = 0;

        try {
            // Sheet: 企业主表数据
            List<EnterpriseExcelDTO> enterpriseData = EasyExcel.read(file.getInputStream())
                    .head(EnterpriseExcelDTO.class)
                    .sheet("企业主表")
                    .doReadSync();

            // Sheet: 岗位数据
            List<EnterprisePositionExcelDTO> positionData = EasyExcel.read(file.getInputStream())
                    .head(EnterprisePositionExcelDTO.class)
                    .sheet("企业岗位")
                    .doReadSync();

            if (enterpriseData == null || enterpriseData.isEmpty()) {
                throw new BusinessException(400, "导入失败：企业数据Sheet为空");
            }

            if (enterpriseData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：企业数据单次导入数量不能超过" + MAX_IMPORT_ROWS + "行");
            }

            if (positionData != null && positionData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：岗位数据单次导入数量不能超过" + MAX_IMPORT_ROWS + "行");
            }

            OffsetDateTime now = OffsetDateTime.now();

            // 1. 校验并处理企业数据（企业主表）：已存在则补空、不存在则新增
            for (int i = 0; i < enterpriseData.size(); i++) {
                int rowNum = i + 2;
                EnterpriseExcelDTO data = enterpriseData.get(i);
                String enterpriseName = null;
                try {
                    enterpriseName = data.getEnterpriseName() == null ? null : data.getEnterpriseName().trim();

                    // 校验企业名称必填
                    if (!StringUtils.hasText(enterpriseName)) {
                        errorMsgs.add("企业主表第" + rowNum + "行：企业名称不能为空");
                        continue;
                    }

                    // 检查文件内重复
                    if (enterpriseNamesInFile.contains(enterpriseName)) {
                        errorMsgs.add("企业主表第" + rowNum + "行：企业名称'" + enterpriseName + "'在文件中重复");
                        continue;
                    }
                    enterpriseNamesInFile.add(enterpriseName);

                    // 查询库中是否已存在（用于补空合并）
                    Enterprise existing = enterpriseMapper.selectByEnterpriseName(enterpriseName);
                    if (existing != null) {
                        // 已存在：仅填补数据库中为 NULL 的列，已有数据一律不覆盖
                        boolean changed = mergeEnterpriseIfBlank(existing, data, now);
                        if (changed) {
                            enterprisesToUpdate.add(existing);
                        }
                        enterpriseNameToIdMap.put(enterpriseName, existing.getId());
                        updatedCount++;
                        continue;
                    }

                    // 不存在：新增，需校验必填与枚举
                    if (!StringUtils.hasText(data.getEnterpriseNature())) {
                        errorMsgs.add("企业主表第" + rowNum + "行：企业性质不能为空");
                        continue;
                    }
                    if (!VALID_ENTERPRISE_NATURES.contains(data.getEnterpriseNature())) {
                        errorMsgs.add("企业主表第" + rowNum + "行：企业性质'" + data.getEnterpriseNature()
                                + "'不合法，必须是：央企、国企、民企、外企、合资");
                        continue;
                    }
                    if (enterpriseName.length() > 200) {
                        errorMsgs.add("企业主表第" + rowNum + "行：企业名称长度不能超过200个字符");
                        continue;
                    }
                    if (StringUtils.hasText(data.getCityName()) && data.getCityName().length() > 50) {
                        errorMsgs.add("企业主表第" + rowNum + "行：城市名称长度不能超过50个字符");
                        continue;
                    }
                    if (StringUtils.hasText(data.getEnterpriseType()) && data.getEnterpriseType().length() > 50) {
                        errorMsgs.add("企业主表第" + rowNum + "行：企业类型长度不能超过50个字符");
                        continue;
                    }
                    if (StringUtils.hasText(data.getLogoUrl()) && data.getLogoUrl().length() > 500) {
                        errorMsgs.add("企业主表第" + rowNum + "行：LOGO地址长度不能超过500个字符");
                        continue;
                    }
                    if (StringUtils.hasText(data.getOfficialWebsite()) && data.getOfficialWebsite().length() > 500) {
                        errorMsgs.add("企业主表第" + rowNum + "行：官方网站长度不能超过500个字符");
                        continue;
                    }
                    if (StringUtils.hasText(data.getRegion()) && data.getRegion().length() > 100) {
                        errorMsgs.add("企业主表第" + rowNum + "行：所在地区长度不能超过100个字符");
                        continue;
                    }
                    if (StringUtils.hasText(data.getEnterpriseScale()) && data.getEnterpriseScale().length() > 50) {
                        errorMsgs.add("企业主表第" + rowNum + "行：企业规模长度不能超过50个字符");
                        continue;
                    }
                    if (StringUtils.hasText(data.getMainBusiness()) && data.getMainBusiness().length() > 500) {
                        errorMsgs.add("企业主表第" + rowNum + "行：主营业务长度不能超过500个字符");
                        continue;
                    }
                    if (StringUtils.hasText(data.getRecruitmentStatus()) && data.getRecruitmentStatus().length() > 20) {
                        errorMsgs.add("企业主表第" + rowNum + "行：招聘状态长度不能超过20个字符");
                        continue;
                    }

                    Long enterpriseId = SnowflakeIdGenerator.nextId();
                    enterpriseNameToIdMap.put(enterpriseName, enterpriseId);

                    Enterprise enterprise = Enterprise.builder()
                            .id(enterpriseId)
                            .cityName(data.getCityName())
                            .enterpriseName(enterpriseName)
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

                    enterprisesToInsert.add(enterprise);
                    insertCount++;
                } catch (Exception e) {
                    errorMsgs.add("企业主表第" + rowNum + "行：数据库操作失败[" + enterpriseName + "]：" + e.getMessage());
                }
            }

            // 2. 校验并处理岗位数据（企业岗位）：企业须存在于本文件或库中；按 (企业,岗位名) 补空/去重
            if (positionData != null && !positionData.isEmpty()) {
                for (int i = 0; i < positionData.size(); i++) {
                    int rowNum = i + 2;
                    EnterprisePositionExcelDTO data = positionData.get(i);
                    String enterpriseName = null;
                    String positionName = null;
                    try {
                        enterpriseName = data.getEnterpriseName() == null ? null : data.getEnterpriseName().trim();
                        positionName = data.getPositionName() == null ? null : data.getPositionName().trim();

                        // 校验企业名称必填
                        if (!StringUtils.hasText(enterpriseName)) {
                            errorMsgs.add("企业岗位第" + rowNum + "行：企业名称不能为空");
                            continue;
                        }
                        // 校验岗位名称必填
                        if (!StringUtils.hasText(positionName)) {
                            errorMsgs.add("企业岗位第" + rowNum + "行：岗位名称不能为空");
                            continue;
                        }

                        // 解析企业ID：优先本文件新增，其次库中已经存在
                        Long enterpriseId = enterpriseNameToIdMap.get(enterpriseName);
                        if (enterpriseId == null) {
                            enterpriseId = enterpriseMapper.findIdByEnterpriseName(enterpriseName);
                        }
                        if (enterpriseId == null) {
                            errorMsgs.add("企业岗位第" + rowNum + "行：企业名称'" + enterpriseName
                                    + "'在企业数据Sheet与数据库中均不存在");
                            continue;
                        }

                        // 文件内 (企业,岗位名) 重复检查
                        String positionKey = enterpriseId + "_" + positionName;
                        if (positionKeysInFile.contains(positionKey)) {
                            errorMsgs.add("企业岗位第" + rowNum + "行：企业'" + enterpriseName
                                    + "'的岗位'" + positionName + "'在文件中重复");
                            continue;
                        }
                        positionKeysInFile.add(positionKey);

                        // 校验字段长度
                        if (positionName.length() > 200) {
                            errorMsgs.add("企业岗位第" + rowNum + "行：岗位名称长度不能超过200个字符");
                            continue;
                        }
                        if (StringUtils.hasText(data.getProvince()) && data.getProvince().length() > 30) {
                            errorMsgs.add("企业岗位第" + rowNum + "行：省份长度不能超过30个字符");
                            continue;
                        }
                        if (StringUtils.hasText(data.getCity()) && data.getCity().length() > 50) {
                            errorMsgs.add("企业岗位第" + rowNum + "行：城市长度不能超过50个字符");
                            continue;
                        }
                        if (StringUtils.hasText(data.getWorkLocation()) && data.getWorkLocation().length() > 200) {
                            errorMsgs.add("企业岗位第" + rowNum + "行：工作地点长度不能超过200个字符");
                            continue;
                        }
                        if (StringUtils.hasText(data.getMajorRequirement()) && data.getMajorRequirement().length() > 500) {
                            errorMsgs.add("企业岗位第" + rowNum + "行：专业要求长度不能超过500个字符");
                            continue;
                        }
                        if (StringUtils.hasText(data.getWorkExperience()) && data.getWorkExperience().length() > 50) {
                            errorMsgs.add("企业岗位第" + rowNum + "行：工作经验长度不能超过50个字符");
                            continue;
                        }
                        if (StringUtils.hasText(data.getApplyLink()) && data.getApplyLink().length() > 500) {
                            errorMsgs.add("企业岗位第" + rowNum + "行：申请链接长度不能超过500个字符");
                            continue;
                        }

                        // 校验岗位状态枚举值
                        if (StringUtils.hasText(data.getPositionStatus())
                                && !VALID_POSITION_STATUSES.contains(data.getPositionStatus())) {
                            errorMsgs.add("企业岗位第" + rowNum + "行：岗位状态'" + data.getPositionStatus()
                                    + "'不合法，必须是：招聘中、已结束");
                            continue;
                        }
                        // 校验薪资范围
                        if (data.getSalaryMin() != null && data.getSalaryMax() != null
                                && data.getSalaryMin() > data.getSalaryMax()) {
                            errorMsgs.add("企业岗位第" + rowNum + "行：最低薪资不能大于最高薪资");
                            continue;
                        }
                        // 校验招聘类型
                        if (StringUtils.hasText(data.getRecruitmentType())
                                && !VALID_RECRUITMENT_TYPES.contains(data.getRecruitmentType())) {
                            errorMsgs.add("企业岗位第" + rowNum + "行：招聘类型'" + data.getRecruitmentType()
                                    + "'不合法，必须是：校招、社招、实习");
                            continue;
                        }
                        // 校验学历要求
                        if (StringUtils.hasText(data.getEducationRequirement())
                                && !VALID_EDUCATION_REQUIREMENTS.contains(data.getEducationRequirement())) {
                            errorMsgs.add("企业岗位第" + rowNum + "行：学历要求'" + data.getEducationRequirement()
                                    + "'不合法，必须是：不限、大专、本科、硕士、博士");
                            continue;
                        }

                        // 已存在（同企业同岗位名）→ 仅补空；否则新增
                        EnterprisePosition existing = enterprisePositionMapper
                                .selectByEnterpriseIdAndPositionName(enterpriseId, positionName);
                        if (existing != null) {
                            boolean changed = mergePositionIfBlank(existing, data, now);
                            if (changed) {
                                positionsToUpdate.add(existing);
                            }
                            updatedCount++;
                            continue;
                        }

                        Long positionId = SnowflakeIdGenerator.nextId();
                        EnterprisePosition position = EnterprisePosition.builder()
                                .id(positionId)
                                .enterpriseId(enterpriseId)
                                .positionName(positionName)
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
                                .deadline(data.getDeadline())
                                .positionStatus(data.getPositionStatus())
                                .isDeleted(false)
                                .createdAt(now)
                                .updatedAt(now)
                                .build();

                        positionsToInsert.add(position);
                        insertCount++;
                    } catch (Exception e) {
                        errorMsgs.add("企业岗位第" + rowNum + "行：数据库操作失败[" + enterpriseName + "]：" + e.getMessage());
                    }
                }
            }

            // 如果有错误，抛出异常（限制展示条数）
            if (!errorMsgs.isEmpty()) {
                throw buildImportException(errorMsgs);
            }

            // 3. 批量落库
            if (!enterprisesToInsert.isEmpty()) {
                enterpriseMapper.insertBatch(enterprisesToInsert);
            }
            if (!enterprisesToUpdate.isEmpty()) {
                for (Enterprise e : enterprisesToUpdate) {
                    enterpriseMapper.updateEntityById(e);
                }
            }
            if (!positionsToInsert.isEmpty()) {
                enterprisePositionMapper.insertBatch(positionsToInsert);
            }
            if (!positionsToUpdate.isEmpty()) {
                for (EnterprisePosition p : positionsToUpdate) {
                    enterprisePositionMapper.updateById(p);
                }
            }

            int total = enterpriseData.size() + (positionData == null ? 0 : positionData.size());
            int success = insertCount + updatedCount;
            log.info("导入企业成功：企业主表+岗位共{}行，新增{}，补齐/已存在{}", total, insertCount, updatedCount);

            return ImportResultVO.builder()
                    .total(total)
                    .success(success)
                    .failed(0)
                    .updated(updatedCount)
                    .errors(null)
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(500, "读取Excel文件失败");
        } catch (Exception e) {
            log.error("导入企业数据失败", e);
            throw new BusinessException(400, "解析Excel数据失败，请检查Excel格式和数据类型是否正确");
        }
    }

    /**
     * 企业主表合并补空：仅当库中字段为 NULL 且上传有值时填充，已有数据不覆盖。
     * 返回是否有字段被填充。
     */
    private boolean mergeEnterpriseIfBlank(Enterprise existing, EnterpriseExcelDTO dto, OffsetDateTime now) {
        boolean changed = false;
        if (existing.getCityName() == null && StringUtils.hasText(dto.getCityName())) {
            existing.setCityName(dto.getCityName().trim());
            changed = true;
        }
        if (existing.getEnterpriseNature() == null && StringUtils.hasText(dto.getEnterpriseNature())) {
            existing.setEnterpriseNature(dto.getEnterpriseNature().trim());
            changed = true;
        }
        if (existing.getEnterpriseType() == null && StringUtils.hasText(dto.getEnterpriseType())) {
            existing.setEnterpriseType(dto.getEnterpriseType().trim());
            changed = true;
        }
        if (existing.getLogoUrl() == null && StringUtils.hasText(dto.getLogoUrl())) {
            existing.setLogoUrl(dto.getLogoUrl().trim());
            changed = true;
        }
        if (existing.getOfficialWebsite() == null && StringUtils.hasText(dto.getOfficialWebsite())) {
            existing.setOfficialWebsite(dto.getOfficialWebsite().trim());
            changed = true;
        }
        if (existing.getRegion() == null && StringUtils.hasText(dto.getRegion())) {
            existing.setRegion(dto.getRegion().trim());
            changed = true;
        }
        if (existing.getEnterpriseScale() == null && StringUtils.hasText(dto.getEnterpriseScale())) {
            existing.setEnterpriseScale(dto.getEnterpriseScale().trim());
            changed = true;
        }
        if (existing.getMainBusiness() == null && StringUtils.hasText(dto.getMainBusiness())) {
            existing.setMainBusiness(dto.getMainBusiness().trim());
            changed = true;
        }
        if (existing.getEnterpriseIntro() == null && StringUtils.hasText(dto.getEnterpriseIntro())) {
            existing.setEnterpriseIntro(dto.getEnterpriseIntro().trim());
            changed = true;
        }
        if (existing.getRecruitmentStatus() == null && StringUtils.hasText(dto.getRecruitmentStatus())) {
            existing.setRecruitmentStatus(dto.getRecruitmentStatus().trim());
            changed = true;
        }
        if (changed) {
            existing.setUpdatedAt(now);
        }
        return changed;
    }

    /**
     * 岗位合并补空：仅当库中字段为 NULL/空 且上传有值时填充，已有数据不覆盖。
     * 返回是否有字段被填充。
     */
    private boolean mergePositionIfBlank(EnterprisePosition existing, EnterprisePositionExcelDTO dto, OffsetDateTime now) {
        boolean changed = false;
        if (existing.getRecruitmentType() == null && StringUtils.hasText(dto.getRecruitmentType())) {
            existing.setRecruitmentType(dto.getRecruitmentType().trim());
            changed = true;
        }
        if (existing.getPositionRequirement() == null && StringUtils.hasText(dto.getPositionRequirement())) {
            existing.setPositionRequirement(dto.getPositionRequirement().trim());
            changed = true;
        }
        if ((existing.getPositionTags() == null || existing.getPositionTags().isEmpty())
                && dto.getPositionTags() != null && !dto.getPositionTags().isEmpty()) {
            existing.setPositionTags(dto.getPositionTags());
            changed = true;
        }
        if (existing.getProvince() == null && StringUtils.hasText(dto.getProvince())) {
            existing.setProvince(dto.getProvince().trim());
            changed = true;
        }
        if (existing.getCity() == null && StringUtils.hasText(dto.getCity())) {
            existing.setCity(dto.getCity().trim());
            changed = true;
        }
        if (existing.getWorkLocation() == null && StringUtils.hasText(dto.getWorkLocation())) {
            existing.setWorkLocation(dto.getWorkLocation().trim());
            changed = true;
        }
        if (existing.getEducationRequirement() == null && StringUtils.hasText(dto.getEducationRequirement())) {
            existing.setEducationRequirement(dto.getEducationRequirement().trim());
            changed = true;
        }
        if (existing.getMajorRequirement() == null && StringUtils.hasText(dto.getMajorRequirement())) {
            existing.setMajorRequirement(dto.getMajorRequirement().trim());
            changed = true;
        }
        if (existing.getWorkExperience() == null && StringUtils.hasText(dto.getWorkExperience())) {
            existing.setWorkExperience(dto.getWorkExperience().trim());
            changed = true;
        }
        if (existing.getSalaryMin() == null && dto.getSalaryMin() != null) {
            existing.setSalaryMin(dto.getSalaryMin());
            changed = true;
        }
        if (existing.getSalaryMax() == null && dto.getSalaryMax() != null) {
            existing.setSalaryMax(dto.getSalaryMax());
            changed = true;
        }
        if (existing.getApplyLink() == null && StringUtils.hasText(dto.getApplyLink())) {
            existing.setApplyLink(dto.getApplyLink().trim());
            changed = true;
        }
        if (existing.getDeadline() == null && dto.getDeadline() != null) {
            existing.setDeadline(dto.getDeadline());
            changed = true;
        }
        if (existing.getPositionStatus() == null && StringUtils.hasText(dto.getPositionStatus())) {
            existing.setPositionStatus(dto.getPositionStatus().trim());
            changed = true;
        }
        if (changed) {
            existing.setUpdatedAt(now);
        }
        return changed;
    }

    /**
     * 将错误列表转为抛出异常，限制展示条数避免 msg 过长。
     */
    private BusinessException buildImportException(List<String> errorMsgs) {
        int display = Math.min(errorMsgs.size(), MAX_ERROR_DISPLAY);
        String joined = String.join("；", errorMsgs.subList(0, display));
        String tail = errorMsgs.size() > MAX_ERROR_DISPLAY
                ? "（仅展示前" + MAX_ERROR_DISPLAY + "条，完整错误请查看后端日志）"
                : "";
        return new BusinessException(400, "导入失败，共" + errorMsgs.size() + "行存在错误，已全部回滚。错误信息：" + joined + tail);
    }
}
