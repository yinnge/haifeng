package com.haifeng.admin.service.impl.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.employment.grassrootsPosition.CommunityPositionQueryDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.CommunityPositionUpdateDTO;
import com.haifeng.admin.excel.employment.grassrootsPosition.CommunityPositionExcelDTO;
import com.haifeng.admin.service.employment.grassrootsPosition.CommunityPositionService;
import com.haifeng.admin.vo.employment.grassrootsPosition.CommunityPositionDetailVO;
import com.haifeng.admin.vo.employment.grassrootsPosition.CommunityPositionListVO;
import com.haifeng.common.entity.employment.grassrootsPosition.CommunityPosition;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.grassrootsPosition.CommunityPositionMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import com.alibaba.excel.EasyExcel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityPositionServiceImpl implements CommunityPositionService {

    private final CommunityPositionMapper communityPositionMapper;

    @Override
    public IPage<CommunityPositionListVO> page(CommunityPositionQueryDTO dto) {
        Page<CommunityPosition> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<CommunityPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityPosition::getIsDeleted, false);

        if (StringUtils.hasText(dto.getPositionName())) {
            wrapper.like(CommunityPosition::getPositionName, dto.getPositionName());
        }
        if (StringUtils.hasText(dto.getCommunityName())) {
            wrapper.like(CommunityPosition::getCommunityName, dto.getCommunityName());
        }
        if (StringUtils.hasText(dto.getSupervisingDept())) {
            wrapper.like(CommunityPosition::getSupervisingDept, dto.getSupervisingDept());
        }
        if (StringUtils.hasText(dto.getPositionType())) {
            wrapper.eq(CommunityPosition::getPositionType, dto.getPositionType());
        }
        if (StringUtils.hasText(dto.getProvince())) {
            wrapper.eq(CommunityPosition::getProvince, dto.getProvince());
        }
        if (StringUtils.hasText(dto.getCity())) {
            wrapper.eq(CommunityPosition::getCity, dto.getCity());
        }
        if (StringUtils.hasText(dto.getPositionStatus())) {
            wrapper.eq(CommunityPosition::getPositionStatus, dto.getPositionStatus());
        }

        wrapper.orderByAsc(CommunityPosition::getSortOrder).orderByDesc(CommunityPosition::getUpdatedAt);

        IPage<CommunityPosition> entityPage = communityPositionMapper.selectPage(page, wrapper);

        return entityPage.convert(entity -> {
            CommunityPositionListVO vo = new CommunityPositionListVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        });
    }

    @Override
    public CommunityPositionDetailVO detail(Long id) {
        CommunityPosition entity = communityPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "社区工作者岗位不存在");
        }
        CommunityPositionDetailVO vo = new CommunityPositionDetailVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public void update(Long id, CommunityPositionUpdateDTO dto) {
        CommunityPosition entity = communityPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "社区工作者岗位不存在");
        }
        BeanUtils.copyProperties(dto, entity);
        entity.setUpdatedAt(OffsetDateTime.now());
        communityPositionMapper.updateById(entity);
        log.info("更新社区工作者岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        CommunityPosition entity = communityPositionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "社区工作者岗位不存在");
        }
        communityPositionMapper.deleteById(id);
        log.info("硬删除社区工作者岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        CommunityPosition entity = communityPositionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "社区工作者岗位不存在");
        }
        entity.setIsDeleted(status == 0);
        entity.setUpdatedAt(OffsetDateTime.now());
        communityPositionMapper.updateById(entity);
        log.info("更新社区工作者岗位状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        int successCount = 0;
        for (Long id : ids) {
            CommunityPosition entity = communityPositionMapper.selectById(id);
            if (entity != null) {
                communityPositionMapper.deleteById(id);
                successCount++;
            }
        }
        log.info("批量删除社区工作者岗位成功: total={}, success={}", ids.size(), successCount);
    }

    @Override
    public String preValidate(MultipartFile file) {
        List<CommunityPositionExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        for (CommunityPositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getPositionName())) {
                errors.add("岗位名称不能为空");
            }
            if (!StringUtils.hasText(dto.getStreetOffice())) {
                errors.add("街道办事处/乡镇不能为空");
            }
            if (!StringUtils.hasText(dto.getProvince())) {
                errors.add("省份不能为空");
            } else if (!ProvinceEnum.isValid(dto.getProvince())) {
                errors.add("省份不合法: " + dto.getProvince());
            }
            if (!errors.isEmpty()) {
                errorMsg.append("第").append(row).append("行: ").append(String.join("; ", errors)).append("\n");
            }
        }
        return errorMsg.length() > 0 ? errorMsg.toString() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importExcel(MultipartFile file) {
        List<CommunityPositionExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        for (CommunityPositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getPositionName())) errors.add("岗位名称不能为空");
            if (!StringUtils.hasText(dto.getStreetOffice())) errors.add("街道办事处/乡镇不能为空");
            if (!StringUtils.hasText(dto.getProvince())) errors.add("省份不能为空");
            else if (!ProvinceEnum.isValid(dto.getProvince())) errors.add("省份不合法: " + dto.getProvince());
            if (!errors.isEmpty()) {
                errorMsg.append("第").append(row).append("行: ").append(String.join("; ", errors)).append("\n");
            }
        }
        if (errorMsg.length() > 0) {
            throw new BusinessException(400, errorMsg.toString());
        }

        OffsetDateTime now = OffsetDateTime.now();
        for (CommunityPositionExcelDTO dto : list) {
            CommunityPosition entity = CommunityPosition.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .streetOffice(dto.getStreetOffice())
                    .communityName(dto.getCommunityName())
                    .supervisingDept(dto.getSupervisingDept())
                    .district(dto.getDistrict())
                    .positionName(dto.getPositionName())
                    .positionType(dto.getPositionType())
                    .employmentType(dto.getEmploymentType())
                    .province(dto.getProvince())
                    .city(dto.getCity())
                    .workLocation(dto.getWorkLocation())
                    .educationRequirement(dto.getEducationRequirement())
                    .ageLimit(dto.getAgeLimit())
                    .recruitmentCount(dto.getRecruitmentCount())
                    .majorRequirement(dto.getMajorRequirement())
                    .householdRequirement(dto.getHouseholdRequirement())
                    .politicalStatus(dto.getPoliticalStatus())
                    .workExperience(dto.getWorkExperience())
                    .socialWorkCert(dto.getSocialWorkCert())
                    .communityExperience(dto.getCommunityExperience())
                    .residenceRequirement(dto.getResidenceRequirement())
                    .salaryRange(dto.getSalaryRange())
                    .salaryComposition(dto.getSalaryComposition())
                    .benefits(dto.getBenefits())
                    .examContent(dto.getExamContent())
                    .interviewForm(dto.getInterviewForm())
                    .regStartDate(dto.getRegStartDate())
                    .regEndDate(dto.getRegEndDate())
                    .examTime(dto.getExamTime())
                    .positionStatus(dto.getPositionStatus())
                    .applyLink(dto.getApplyLink())
                    .applyMethod(dto.getApplyMethod())
                    .contactPhone(dto.getContactPhone())
                    .contactAddress(dto.getContactAddress())
                    .remark(dto.getRemark())
                    .content(dto.getContent())
                    .sortOrder(dto.getSortOrder())
                    .isDeleted(false)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            communityPositionMapper.insert(entity);
        }
        log.info("导入社区工作者岗位成功: count={}", list.size());
    }

    private List<CommunityPositionExcelDTO> readExcel(MultipartFile file) {
        try {
            return EasyExcel.read(file.getInputStream())
                    .head(CommunityPositionExcelDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel失败", e);
            throw new BusinessException(400, "读取Excel文件失败");
        }
    }
}
