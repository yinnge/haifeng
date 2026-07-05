package com.haifeng.admin.service.impl.employment.civilService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.employment.civilService.InstitutionPositionQueryDTO;
import com.haifeng.admin.dto.employment.civilService.InstitutionPositionUpdateDTO;
import com.haifeng.admin.excel.employment.civilService.InstitutionPositionExcelDTO;
import com.haifeng.admin.service.employment.civilService.InstitutionPositionService;
import com.haifeng.admin.vo.employment.civilService.InstitutionPositionDetailVO;
import com.haifeng.admin.vo.employment.civilService.InstitutionPositionListVO;
import com.haifeng.common.entity.employment.civilService.InstitutionPosition;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.civilService.InstitutionPositionMapper;
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
public class InstitutionPositionServiceImpl implements InstitutionPositionService {

    private final InstitutionPositionMapper institutionPositionMapper;

    @Override
    public IPage<InstitutionPositionListVO> page(InstitutionPositionQueryDTO dto) {
        Page<InstitutionPosition> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<InstitutionPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InstitutionPosition::getIsDeleted, false);

        if (StringUtils.hasText(dto.getPositionName())) {
            wrapper.like(InstitutionPosition::getPositionName, dto.getPositionName());
        }
        if (StringUtils.hasText(dto.getSupervisingDept())) {
            wrapper.like(InstitutionPosition::getSupervisingDept, dto.getSupervisingDept());
        }
        if (StringUtils.hasText(dto.getInstitution())) {
            wrapper.like(InstitutionPosition::getInstitution, dto.getInstitution());
        }
        if (StringUtils.hasText(dto.getProvince())) {
            wrapper.eq(InstitutionPosition::getProvince, dto.getProvince());
        }
        if (StringUtils.hasText(dto.getExamCategory())) {
            wrapper.eq(InstitutionPosition::getExamCategory, dto.getExamCategory());
        }
        if (StringUtils.hasText(dto.getPositionType())) {
            wrapper.eq(InstitutionPosition::getPositionType, dto.getPositionType());
        }
        if (StringUtils.hasText(dto.getPositionStatus())) {
            wrapper.eq(InstitutionPosition::getPositionStatus, dto.getPositionStatus());
        }

        wrapper.orderByAsc(InstitutionPosition::getSortOrder).orderByDesc(InstitutionPosition::getUpdatedAt);

        IPage<InstitutionPosition> entityPage = institutionPositionMapper.selectPage(page, wrapper);
        return entityPage.convert(entity -> {
            InstitutionPositionListVO vo = new InstitutionPositionListVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        });
    }

    @Override
    public InstitutionPositionDetailVO detail(Long id) {
        InstitutionPosition entity = institutionPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "事业编职位不存在");
        }
        InstitutionPositionDetailVO vo = new InstitutionPositionDetailVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public void update(Long id, InstitutionPositionUpdateDTO dto) {
        InstitutionPosition entity = institutionPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "事业编职位不存在");
        }
        BeanUtils.copyProperties(dto, entity);
        entity.setUpdatedAt(OffsetDateTime.now());
        institutionPositionMapper.updateById(entity);
        log.info("更新事业编职位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        InstitutionPosition entity = institutionPositionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "事业编职位不存在");
        }
        institutionPositionMapper.deleteById(id);
        log.info("硬删除事业编职位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        InstitutionPosition entity = institutionPositionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "事业编职位不存在");
        }
        entity.setIsDeleted(status == 0);
        entity.setUpdatedAt(OffsetDateTime.now());
        institutionPositionMapper.updateById(entity);
        log.info("更新事业编职位状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        int successCount = 0;
        for (Long id : ids) {
            InstitutionPosition entity = institutionPositionMapper.selectById(id);
            if (entity != null) {
                institutionPositionMapper.deleteById(id);
                successCount++;
            }
        }
        log.info("批量删除事业编职位成功: total={}, success={}", ids.size(), successCount);
    }

    @Override
    public String preValidate(MultipartFile file) {
        List<InstitutionPositionExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        for (InstitutionPositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getPositionName())) {
                errors.add("职位名称不能为空");
            }
            if (StringUtils.hasText(dto.getProvince()) && !ProvinceEnum.isValid(dto.getProvince())) {
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
        List<InstitutionPositionExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        for (InstitutionPositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getPositionName())) errors.add("职位名称不能为空");
            if (StringUtils.hasText(dto.getProvince()) && !ProvinceEnum.isValid(dto.getProvince())) {
                errors.add("省份不合法: " + dto.getProvince());
            }
            if (!errors.isEmpty()) {
                errorMsg.append("第").append(row).append("行: ").append(String.join("; ", errors)).append("\n");
            }
        }
        if (errorMsg.length() > 0) {
            throw new BusinessException(400, errorMsg.toString());
        }

        OffsetDateTime now = OffsetDateTime.now();
        for (InstitutionPositionExcelDTO dto : list) {
            InstitutionPosition entity = InstitutionPosition.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .positionName(dto.getPositionName())
                    .supervisingDept(dto.getSupervisingDept())
                    .institution(dto.getInstitution())
                    .workLocation(dto.getWorkLocation())
                    .province(dto.getProvince())
                    .examCategory(dto.getExamCategory())
                    .positionType(dto.getPositionType())
                    .subCategory(dto.getSubCategory())
                    .educationRequirement(dto.getEducationRequirement())
                    .degreeRequirement(dto.getDegreeRequirement())
                    .ageLimit(dto.getAgeLimit())
                    .recruitmentCount(dto.getRecruitmentCount())
                    .salaryRange(dto.getSalaryRange())
                    .regDeadline(dto.getRegDeadline())
                    .majorRequirements(dto.getMajorRequirements())
                    .specialPosition(dto.getSpecialPosition())
                    .otherRequirement(dto.getOtherRequirement())
                    .otherRequirementDesc(dto.getOtherRequirementDesc())
                    .remarkType(dto.getRemarkType())
                    .remarkDesc(dto.getRemarkDesc())
                    .consultationPhone(dto.getConsultationPhone())
                    .supervisionPhone(dto.getSupervisionPhone())
                    .positionStatus(dto.getPositionStatus())
                    .positionTag(dto.getPositionTag())
                    .tagText(dto.getTagText())
                    .sortOrder(dto.getSortOrder())
                    .isDeleted(false)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            institutionPositionMapper.insert(entity);
        }
        log.info("导入事业编职位成功: count={}", list.size());
    }

    private List<InstitutionPositionExcelDTO> readExcel(MultipartFile file) {
        try {
            return EasyExcel.read(file.getInputStream())
                    .head(InstitutionPositionExcelDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel失败", e);
            throw new BusinessException(400, "读取Excel文件失败");
        }
    }
}
