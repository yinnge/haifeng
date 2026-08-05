package com.haifeng.admin.service.impl.company;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.company.EnterprisePositionAddDTO;
import com.haifeng.admin.dto.company.EnterprisePositionQueryDTO;
import com.haifeng.admin.dto.company.EnterprisePositionUpdateDTO;
import com.haifeng.admin.service.company.EnterprisePositionService;
import com.haifeng.admin.vo.company.EnterprisePositionDetailVO;
import com.haifeng.common.entity.company.Enterprise;
import com.haifeng.common.entity.company.EnterprisePosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.company.EnterpriseMapper;
import com.haifeng.common.mapper.company.EnterprisePositionMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnterprisePositionServiceImpl implements EnterprisePositionService {

    private final EnterprisePositionMapper enterprisePositionMapper;
    private final EnterpriseMapper enterpriseMapper;

    private static final Set<String> VALID_RECRUITMENT_TYPES = Set.of("校招", "社招", "实习");
    private static final Set<String> VALID_EDUCATION_REQUIREMENTS = Set.of("不限", "大专", "本科", "硕士", "博士");
    private static final Set<String> VALID_POSITION_STATUSES = Set.of("招聘中", "已结束");

    @Override
    public IPage<EnterprisePositionDetailVO> page(EnterprisePositionQueryDTO dto) {
        Page<EnterprisePosition> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<EnterprisePosition> wrapper = new LambdaQueryWrapper<>();

        if (dto.getEnterpriseId() != null) {
            wrapper.eq(EnterprisePosition::getEnterpriseId, dto.getEnterpriseId());
        }
        if (StringUtils.hasText(dto.getPositionName())) {
            wrapper.like(EnterprisePosition::getPositionName, dto.getPositionName());
        }
        if (StringUtils.hasText(dto.getRecruitmentType())) {
            wrapper.eq(EnterprisePosition::getRecruitmentType, dto.getRecruitmentType());
        }
        if (StringUtils.hasText(dto.getEducationRequirement())) {
            wrapper.eq(EnterprisePosition::getEducationRequirement, dto.getEducationRequirement());
        }
        if (StringUtils.hasText(dto.getPositionStatus())) {
            wrapper.eq(EnterprisePosition::getPositionStatus, dto.getPositionStatus());
        }
        if (StringUtils.hasText(dto.getProvince())) {
            wrapper.eq(EnterprisePosition::getProvince, dto.getProvince());
        }
        if (StringUtils.hasText(dto.getCity())) {
            wrapper.eq(EnterprisePosition::getCity, dto.getCity());
        }

        wrapper.eq(EnterprisePosition::getIsDeleted, false);
        wrapper.orderByDesc(EnterprisePosition::getCreatedAt);

        IPage<EnterprisePosition> positionPage = enterprisePositionMapper.selectPage(page, wrapper);

        return positionPage.convert(this::convertToDetailVO);
    }

    @Override
    public EnterprisePositionDetailVO detail(Long id) {
        EnterprisePosition position = enterprisePositionMapper.selectById(id);
        if (position == null) {
            throw new BusinessException(404, "岗位不存在");
        }
        return convertToDetailVO(position);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(Long enterpriseId, EnterprisePositionAddDTO dto) {
        Enterprise enterprise = enterpriseMapper.selectById(enterpriseId);
        if (enterprise == null) {
            throw new BusinessException(404, "企业不存在");
        }

        if (StringUtils.hasText(dto.getRecruitmentType()) && !VALID_RECRUITMENT_TYPES.contains(dto.getRecruitmentType())) {
            throw new BusinessException(400, "招聘类型必须是：校招、社招、实习");
        }
        if (StringUtils.hasText(dto.getEducationRequirement()) && !VALID_EDUCATION_REQUIREMENTS.contains(dto.getEducationRequirement())) {
            throw new BusinessException(400, "学历要求必须是：不限、大专、本科、硕士、博士");
        }
        if (StringUtils.hasText(dto.getPositionStatus()) && !VALID_POSITION_STATUSES.contains(dto.getPositionStatus())) {
            throw new BusinessException(400, "岗位状态必须是：招聘中、已结束");
        }
        if (dto.getSalaryMin() != null && dto.getSalaryMax() != null && dto.getSalaryMin() > dto.getSalaryMax()) {
            throw new BusinessException(400, "最低薪资不能大于最高薪资");
        }

        OffsetDateTime now = OffsetDateTime.now();
        Long positionId = SnowflakeIdGenerator.nextId();

        EnterprisePosition position = EnterprisePosition.builder()
                .id(positionId)
                .enterpriseId(enterpriseId)
                .positionName(dto.getPositionName())
                .recruitmentType(dto.getRecruitmentType())
                .positionRequirement(dto.getPositionRequirement())
                .positionTags(dto.getPositionTags())
                .province(dto.getProvince())
                .city(dto.getCity())
                .workLocation(dto.getWorkLocation())
                .educationRequirement(dto.getEducationRequirement())
                .majorRequirement(dto.getMajorRequirement())
                .workExperience(dto.getWorkExperience())
                .salaryMin(dto.getSalaryMin())
                .salaryMax(dto.getSalaryMax())
                .applyLink(dto.getApplyLink())
                .deadline(dto.getDeadline())
                .positionStatus(dto.getPositionStatus())
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        enterprisePositionMapper.insert(position);

        log.info("新增企业岗位成功: id={}, enterpriseId={}, positionName={}", positionId, enterpriseId, dto.getPositionName());
        return positionId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, EnterprisePositionUpdateDTO dto) {
        EnterprisePosition position = enterprisePositionMapper.selectById(id);
        if (position == null) {
            throw new BusinessException(404, "岗位不存在");
        }

        if (StringUtils.hasText(dto.getRecruitmentType()) && !VALID_RECRUITMENT_TYPES.contains(dto.getRecruitmentType())) {
            throw new BusinessException(400, "招聘类型必须是：校招、社招、实习");
        }
        if (StringUtils.hasText(dto.getEducationRequirement()) && !VALID_EDUCATION_REQUIREMENTS.contains(dto.getEducationRequirement())) {
            throw new BusinessException(400, "学历要求必须是：不限、大专、本科、硕士、博士");
        }
        if (StringUtils.hasText(dto.getPositionStatus()) && !VALID_POSITION_STATUSES.contains(dto.getPositionStatus())) {
            throw new BusinessException(400, "岗位状态必须是：招聘中、已结束");
        }
        if (dto.getSalaryMin() != null && dto.getSalaryMax() != null && dto.getSalaryMin() > dto.getSalaryMax()) {
            throw new BusinessException(400, "最低薪资不能大于最高薪资");
        }

        position.setPositionName(dto.getPositionName());
        position.setRecruitmentType(dto.getRecruitmentType());
        position.setPositionRequirement(dto.getPositionRequirement());
        position.setPositionTags(dto.getPositionTags());
        position.setProvince(dto.getProvince());
        position.setCity(dto.getCity());
        position.setWorkLocation(dto.getWorkLocation());
        position.setEducationRequirement(dto.getEducationRequirement());
        position.setMajorRequirement(dto.getMajorRequirement());
        position.setWorkExperience(dto.getWorkExperience());
        position.setSalaryMin(dto.getSalaryMin());
        position.setSalaryMax(dto.getSalaryMax());
        position.setApplyLink(dto.getApplyLink());
        position.setDeadline(dto.getDeadline());
        position.setPositionStatus(dto.getPositionStatus());
        position.setUpdatedAt(OffsetDateTime.now());

        enterprisePositionMapper.updateById(position);

        log.info("更新企业岗位成功: id={}, positionName={}", id, dto.getPositionName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        EnterprisePosition position = enterprisePositionMapper.selectById(id);
        if (position == null) {
            throw new BusinessException(404, "岗位不存在");
        }

        enterprisePositionMapper.deleteById(id);

        log.info("硬删除企业岗位成功: id={}, positionName={}", id, position.getPositionName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的岗位");
        }

        enterprisePositionMapper.deleteBatchIds(ids);

        log.info("批量硬删除企业岗位成功: 删除数量={}, ids={}", ids.size(), ids);
    }

    private EnterprisePositionDetailVO convertToDetailVO(EnterprisePosition position) {
        EnterprisePositionDetailVO vo = new EnterprisePositionDetailVO();
        vo.setId(position.getId());
        vo.setEnterpriseId(position.getEnterpriseId());
        vo.setPositionName(position.getPositionName());
        vo.setRecruitmentType(position.getRecruitmentType());
        vo.setPositionRequirement(position.getPositionRequirement());
        vo.setPositionTags(position.getPositionTags());
        vo.setProvince(position.getProvince());
        vo.setCity(position.getCity());
        vo.setWorkLocation(position.getWorkLocation());
        vo.setEducationRequirement(position.getEducationRequirement());
        vo.setMajorRequirement(position.getMajorRequirement());
        vo.setWorkExperience(position.getWorkExperience());
        vo.setSalaryMin(position.getSalaryMin());
        vo.setSalaryMax(position.getSalaryMax());
        vo.setApplyLink(position.getApplyLink());
        vo.setPositionStatus(position.getPositionStatus());
        vo.setIsDeleted(position.getIsDeleted());

        if (position.getCreatedAt() != null) {
            vo.setCreatedAt(position.getCreatedAt().toLocalDateTime());
        }
        if (position.getUpdatedAt() != null) {
            vo.setUpdatedAt(position.getUpdatedAt().toLocalDateTime());
        }
        if (position.getDeadline() != null) {
            vo.setDeadline(position.getDeadline().toLocalDateTime());
        }

        return vo;
    }
}
