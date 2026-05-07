package com.haifeng.admin.service.impl.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.university.UniversityGuideAddDTO;
import com.haifeng.admin.dto.university.UniversityGuideQueryDTO;
import com.haifeng.admin.dto.university.UniversityGuideUpdateDTO;
import com.haifeng.admin.service.university.UniversityGuideService;
import com.haifeng.admin.vo.university.UniversityGuideDetailVO;
import com.haifeng.admin.vo.university.UniversityGuideListVO;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.entity.university.UniversityGuide;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.UniversityGuideMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import com.alibaba.excel.EasyExcel;
import com.haifeng.admin.excel.university.UniversityGuideExcelDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 院校适应指南Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UniversityGuideServiceImpl implements UniversityGuideService {

    private final UniversityGuideMapper universityGuideMapper;
    private final UniversityMapper universityMapper;

    @Override
    public IPage<UniversityGuideListVO> page(UniversityGuideQueryDTO dto) {
        Page<UniversityGuide> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<UniversityGuide> wrapper = new LambdaQueryWrapper<>();
        // 只查询未删除的（status != 0）
        wrapper.ne(UniversityGuide::getStatus, (short) 0);

        // 状态筛选
        if (dto.getStatus() != null) {
            wrapper.eq(UniversityGuide::getStatus, dto.getStatus());
        }

        // 按createdAt降序排列
        wrapper.orderByDesc(UniversityGuide::getCreatedAt);

        IPage<UniversityGuide> guidePage = universityGuideMapper.selectPage(page, wrapper);

        return guidePage.convert(guide -> {
            UniversityGuideListVO vo = new UniversityGuideListVO();
            vo.setId(guide.getId());
            vo.setUniversityId(guide.getUniversityId());
            vo.setCustomTags(guide.getCustomTags());
            vo.setRemark(guide.getRemark());
            vo.setStatus(guide.getStatus() != null ? guide.getStatus().intValue() : null);
            vo.setCreatedAt(guide.getCreatedAt() != null ? guide.getCreatedAt().toLocalDateTime() : null);

            // 查询院校名称
            University university = universityMapper.selectById(guide.getUniversityId());
            if (university != null) {
                vo.setUniversityName(university.getName());
            }

            return vo;
        });
    }

    @Override
    public UniversityGuideDetailVO detail(Long id) {
        UniversityGuide guide = universityGuideMapper.selectById(id);
        if (guide == null || guide.getStatus() == 0) {
            throw new BusinessException(404, "院校适应指南不存在");
        }

        UniversityGuideDetailVO vo = new UniversityGuideDetailVO();
        vo.setId(guide.getId());
        vo.setUniversityId(guide.getUniversityId());
        vo.setCustomTags(guide.getCustomTags());
        vo.setCampusFacilities(guide.getCampusFacilities());
        vo.setDormitoryServices(guide.getDormitoryServices());
        vo.setCampusTransportation(guide.getCampusTransportation());
        vo.setAcademicGuidance(guide.getAcademicGuidance());
        vo.setMajorTransferGuidelines(guide.getMajorTransferGuidelines());
        vo.setMajorTransferConstriction(guide.getMajorTransferConstriction());
        vo.setAcademicSupportResources(guide.getAcademicSupportResources());
        vo.setStudentOrganizations(guide.getStudentOrganizations());
        vo.setCampusEvents(guide.getCampusEvents());
        vo.setClassDormSocial(guide.getClassDormSocial());
        vo.setFinancialAid(guide.getFinancialAid());
        vo.setCampusSecurity(guide.getCampusSecurity());
        vo.setHealthServices(guide.getHealthServices());
        vo.setLifeServices(guide.getLifeServices());
        vo.setRemark(guide.getRemark());
        vo.setStatus(guide.getStatus() != null ? guide.getStatus().intValue() : null);
        vo.setCreatedAt(guide.getCreatedAt() != null ? guide.getCreatedAt().toLocalDateTime() : null);
        vo.setUpdatedAt(guide.getUpdatedAt() != null ? guide.getUpdatedAt().toLocalDateTime() : null);

        // 查询院校名称
        University university = universityMapper.selectById(guide.getUniversityId());
        if (university != null) {
            vo.setUniversityName(university.getName());
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(UniversityGuideAddDTO dto) {
        // 校验院校是否存在
        University university = universityMapper.selectById(dto.getUniversityId());
        if (university == null || university.getStatus() == 0) {
            throw new BusinessException(404, "关联院校不存在");
        }

        // 检查该院校是否已有指南（1:1关系）
        LambdaQueryWrapper<UniversityGuide> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UniversityGuide::getUniversityId, dto.getUniversityId())
               .ne(UniversityGuide::getStatus, (short) 0);
        if (universityGuideMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "该院校已存在适应指南，请直接修改");
        }

        OffsetDateTime now = OffsetDateTime.now();
        Long id = SnowflakeIdGenerator.nextId();

        UniversityGuide guide = UniversityGuide.builder()
                .id(id)
                .universityId(dto.getUniversityId())
                .customTags(dto.getCustomTags())
                .campusFacilities(dto.getCampusFacilities())
                .dormitoryServices(dto.getDormitoryServices())
                .campusTransportation(dto.getCampusTransportation())
                .academicGuidance(dto.getAcademicGuidance())
                .majorTransferGuidelines(dto.getMajorTransferGuidelines())
                .majorTransferConstriction(dto.getMajorTransferConstriction())
                .academicSupportResources(dto.getAcademicSupportResources())
                .studentOrganizations(dto.getStudentOrganizations())
                .campusEvents(dto.getCampusEvents())
                .classDormSocial(dto.getClassDormSocial())
                .financialAid(dto.getFinancialAid())
                .campusSecurity(dto.getCampusSecurity())
                .healthServices(dto.getHealthServices())
                .lifeServices(dto.getLifeServices())
                .remark(dto.getRemark())
                .status((short) 1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        universityGuideMapper.insert(guide);

        log.info("新增院校适应指南成功: id={}, universityId={}", id, dto.getUniversityId());
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, UniversityGuideUpdateDTO dto) {
        UniversityGuide guide = universityGuideMapper.selectById(id);
        if (guide == null || guide.getStatus() == 0) {
            throw new BusinessException(404, "院校适应指南不存在");
        }

        guide.setCustomTags(dto.getCustomTags());
        guide.setCampusFacilities(dto.getCampusFacilities());
        guide.setDormitoryServices(dto.getDormitoryServices());
        guide.setCampusTransportation(dto.getCampusTransportation());
        guide.setAcademicGuidance(dto.getAcademicGuidance());
        guide.setMajorTransferGuidelines(dto.getMajorTransferGuidelines());
        guide.setMajorTransferConstriction(dto.getMajorTransferConstriction());
        guide.setAcademicSupportResources(dto.getAcademicSupportResources());
        guide.setStudentOrganizations(dto.getStudentOrganizations());
        guide.setCampusEvents(dto.getCampusEvents());
        guide.setClassDormSocial(dto.getClassDormSocial());
        guide.setFinancialAid(dto.getFinancialAid());
        guide.setCampusSecurity(dto.getCampusSecurity());
        guide.setHealthServices(dto.getHealthServices());
        guide.setLifeServices(dto.getLifeServices());
        guide.setRemark(dto.getRemark());
        if (dto.getStatus() != null) {
            guide.setStatus(dto.getStatus().shortValue());
        }
        guide.setUpdatedAt(OffsetDateTime.now());

        universityGuideMapper.updateById(guide);

        log.info("修改院校适应指南成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Short status) {
        UniversityGuide guide = universityGuideMapper.selectById(id);
        if (guide == null) {
            throw new BusinessException(404, "院校适应指南不存在");
        }

        guide.setStatus(status);
        guide.setUpdatedAt(OffsetDateTime.now());
        universityGuideMapper.updateById(guide);

        log.info("修改院校适应指南状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        UniversityGuide guide = universityGuideMapper.selectById(id);
        if (guide == null) {
            throw new BusinessException(404, "院校适应指南不存在");
        }

        // 软删除：status = 0
        guide.setStatus((short) 0);
        guide.setUpdatedAt(OffsetDateTime.now());
        universityGuideMapper.updateById(guide);

        log.info("软删除院校适应指南成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDelete(Long id) {
        UniversityGuide guide = universityGuideMapper.selectById(id);
        if (guide == null) {
            throw new BusinessException(404, "院校适应指南不存在");
        }

        // 硬删除：物理删除
        universityGuideMapper.deleteById(id);

        log.info("硬删除院校适应指南成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "ID列表不能为空");
        }

        OffsetDateTime now = OffsetDateTime.now();
        int successCount = 0;

        for (Long id : ids) {
            UniversityGuide guide = universityGuideMapper.selectById(id);
            if (guide != null && guide.getStatus() != 0) {
                guide.setStatus((short) 0);
                guide.setUpdatedAt(now);
                universityGuideMapper.updateById(guide);
                successCount++;
            }
        }

        log.info("批量软删除院校适应指南成功: 请求数量={}, 实际删除数量={}", ids.size(), successCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchHardDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "ID列表不能为空");
        }

        int successCount = 0;

        for (Long id : ids) {
            UniversityGuide guide = universityGuideMapper.selectById(id);
            if (guide != null) {
                universityGuideMapper.deleteById(id);
                successCount++;
            }
        }

        log.info("批量硬删除院校适应指南成功: 请求数量={}, 实际删除数量={}", ids.size(), successCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importGuide(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }

        List<UniversityGuideExcelDTO> dataList;
        try {
            dataList = EasyExcel.read(file.getInputStream())
                    .head(UniversityGuideExcelDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "读取Excel文件失败: " + e.getMessage());
        }

        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }

        List<String> errors = new ArrayList<>();
        OffsetDateTime now = OffsetDateTime.now();
        int successCount = 0;

        for (int i = 0; i < dataList.size(); i++) {
            int rowNum = i + 2;
            UniversityGuideExcelDTO dto = dataList.get(i);

            // 校验必填字段
            if (!StringUtils.hasText(dto.getUniversityName())) {
                errors.add("第" + rowNum + "行: 院校名称不能为空");
                continue;
            }

            // 根据院校名称查找院校
            LambdaQueryWrapper<University> univWrapper = new LambdaQueryWrapper<>();
            univWrapper.eq(University::getName, dto.getUniversityName())
                       .ne(University::getStatus, (short) 0);
            University university = universityMapper.selectOne(univWrapper);

            if (university == null) {
                errors.add("第" + rowNum + "行: 院校[" + dto.getUniversityName() + "]不存在");
                continue;
            }

            // 检查该院校是否已有指南（1:1关系）
            LambdaQueryWrapper<UniversityGuide> guideWrapper = new LambdaQueryWrapper<>();
            guideWrapper.eq(UniversityGuide::getUniversityId, university.getId())
                       .ne(UniversityGuide::getStatus, (short) 0);
            UniversityGuide existingGuide = universityGuideMapper.selectOne(guideWrapper);

            if (existingGuide != null) {
                // 更新现有记录
                existingGuide.setCustomTags(dto.getCustomTags());
                existingGuide.setRemark(dto.getRemark());
                if (dto.getStatus() != null) {
                    existingGuide.setStatus(dto.getStatus().shortValue());
                }
                existingGuide.setUpdatedAt(now);
                universityGuideMapper.updateById(existingGuide);
            } else {
                // 新建指南记录
                Long id = SnowflakeIdGenerator.nextId();
                UniversityGuide guide = UniversityGuide.builder()
                        .id(id)
                        .universityId(university.getId())
                        .customTags(dto.getCustomTags())
                        .remark(dto.getRemark())
                        .status(dto.getStatus() != null ? dto.getStatus().shortValue() : (short) 1)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                universityGuideMapper.insert(guide);
            }
            successCount++;
        }

        if (!errors.isEmpty()) {
            String errorMsg = String.format("导入完成，成功%d条，失败%d条。错误信息：%s",
                    successCount, errors.size(), String.join("; ", errors));
            throw new BusinessException(400, errorMsg);
        }

        log.info("导入院校适应指南数据成功: 共{}条", successCount);
    }
}
