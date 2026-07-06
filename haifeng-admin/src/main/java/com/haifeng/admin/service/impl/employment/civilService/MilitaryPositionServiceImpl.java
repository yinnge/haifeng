package com.haifeng.admin.service.impl.employment.civilService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.employment.civilService.MilitaryPositionQueryDTO;
import com.haifeng.admin.dto.employment.civilService.MilitaryPositionUpdateDTO;
import com.haifeng.admin.excel.employment.civilService.MilitaryPositionExcelDTO;
import com.haifeng.admin.service.employment.civilService.MilitaryPositionService;
import com.haifeng.admin.vo.employment.civilService.MilitaryPositionDetailVO;
import com.haifeng.admin.vo.employment.civilService.MilitaryPositionListVO;
import com.haifeng.common.entity.employment.civilService.MilitaryPosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.civilService.MilitaryPositionMapper;
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
public class MilitaryPositionServiceImpl implements MilitaryPositionService {

    private final MilitaryPositionMapper militaryPositionMapper;

    @Override
    public IPage<MilitaryPositionListVO> page(MilitaryPositionQueryDTO dto) {
        Page<MilitaryPosition> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<MilitaryPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MilitaryPosition::getIsDeleted, false);

        if (StringUtils.hasText(dto.getPositionName())) {
            wrapper.like(MilitaryPosition::getPositionName, dto.getPositionName());
        }
        if (StringUtils.hasText(dto.getEmployerUnit())) {
            wrapper.like(MilitaryPosition::getEmployerUnit, dto.getEmployerUnit());
        }
        if (StringUtils.hasText(dto.getDepartment())) {
            wrapper.like(MilitaryPosition::getDepartment, dto.getDepartment());
        }
        if (StringUtils.hasText(dto.getPositionType())) {
            wrapper.eq(MilitaryPosition::getPositionType, dto.getPositionType());
        }
        if (StringUtils.hasText(dto.getPositionStatus())) {
            wrapper.eq(MilitaryPosition::getPositionStatus, dto.getPositionStatus());
        }

        wrapper.orderByAsc(MilitaryPosition::getSortOrder).orderByDesc(MilitaryPosition::getUpdatedAt);

        IPage<MilitaryPosition> entityPage = militaryPositionMapper.selectPage(page, wrapper);
        return entityPage.convert(entity -> {
            MilitaryPositionListVO vo = new MilitaryPositionListVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        });
    }

    @Override
    public MilitaryPositionDetailVO detail(Long id) {
        MilitaryPosition entity = militaryPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "部队文职岗位不存在");
        }
        MilitaryPositionDetailVO vo = new MilitaryPositionDetailVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public void update(Long id, MilitaryPositionUpdateDTO dto) {
        MilitaryPosition entity = militaryPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "部队文职岗位不存在");
        }
        BeanUtils.copyProperties(dto, entity);
        entity.setUpdatedAt(OffsetDateTime.now());
        militaryPositionMapper.updateById(entity);
        log.info("更新部队文职岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        MilitaryPosition entity = militaryPositionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "部队文职岗位不存在");
        }
        militaryPositionMapper.deleteById(id);
        log.info("硬删除部队文职岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        MilitaryPosition entity = militaryPositionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "部队文职岗位不存在");
        }
        entity.setIsDeleted(status == 0);
        entity.setUpdatedAt(OffsetDateTime.now());
        militaryPositionMapper.updateById(entity);
        log.info("更新部队文职岗位状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        int successCount = 0;
        for (Long id : ids) {
            MilitaryPosition entity = militaryPositionMapper.selectById(id);
            if (entity != null) {
                militaryPositionMapper.deleteById(id);
                successCount++;
            }
        }
        log.info("批量删除部队文职岗位成功: total={}, success={}", ids.size(), successCount);
    }

    @Override
    public String preValidate(MultipartFile file) {
        List<MilitaryPositionExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        for (MilitaryPositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getPositionName())) {
                errors.add("岗位名称不能为空");
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
        List<MilitaryPositionExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        for (MilitaryPositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getPositionName())) errors.add("岗位名称不能为空");
            if (!errors.isEmpty()) {
                errorMsg.append("第").append(row).append("行: ").append(String.join("; ", errors)).append("\n");
            }
        }
        if (errorMsg.length() > 0) {
            throw new BusinessException(400, errorMsg.toString());
        }

        OffsetDateTime now = OffsetDateTime.now();
        for (MilitaryPositionExcelDTO dto : list) {
            MilitaryPosition entity = MilitaryPosition.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .positionName(dto.getPositionName())
                    .employerUnit(dto.getEmployerUnit())
                    .department(dto.getDepartment())
                    .positionType(dto.getPositionType())
                    .workLocation(dto.getWorkLocation())
                    .salaryRange(dto.getSalaryRange())
                    .majorRequirement(dto.getMajorRequirement())
                    .educationRequirement(dto.getEducationRequirement())
                    .regDeadline(dto.getRegDeadline())
                    .positionStatus(dto.getPositionStatus())
                    .positionDescription(dto.getPositionDescription())
                    .responsibilities(dto.getResponsibilities())
                    .qualifications(dto.getQualifications())
                    .sortOrder(dto.getSortOrder())
                    .isDeleted(false)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            militaryPositionMapper.insert(entity);
        }
        log.info("导入部队文职岗位成功: count={}", list.size());
    }

    private List<MilitaryPositionExcelDTO> readExcel(MultipartFile file) {
        try {
            return EasyExcel.read(file.getInputStream())
                    .head(MilitaryPositionExcelDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel失败", e);
            throw new BusinessException(400, "读取Excel文件失败");
        }
    }
}
