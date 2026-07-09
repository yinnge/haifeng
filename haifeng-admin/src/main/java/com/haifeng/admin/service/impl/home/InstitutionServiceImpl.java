package com.haifeng.admin.service.impl.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.home.InstitutionAddDTO;
import com.haifeng.admin.dto.home.InstitutionQueryDTO;
import com.haifeng.admin.dto.home.InstitutionUpdateDTO;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.service.home.InstitutionService;
import com.haifeng.admin.vo.home.InstitutionDetailVO;
import com.haifeng.admin.vo.home.InstitutionListVO;
import com.haifeng.common.entity.home.Institution;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.home.InstitutionMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstitutionServiceImpl implements InstitutionService {

    private final InstitutionMapper institutionMapper;

    @Override
    public IPage<InstitutionListVO> page(InstitutionQueryDTO dto) {
        Page<Institution> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Institution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Institution::getDeleted, false);

        // 名称模糊查询
        if (StringUtils.hasText(dto.getName())) {
            wrapper.like(Institution::getName, dto.getName());
        }
        // 类型精确筛选
        if (StringUtils.hasText(dto.getType())) {
            wrapper.eq(Institution::getType, dto.getType());
        }
        // 状态筛选
        if (dto.getStatus() != null) {
            wrapper.eq(Institution::getStatus, dto.getStatus());
        }

        // 按sortOrder升序 + createdAt降序排列
        wrapper.orderByAsc(Institution::getSortOrder)
               .orderByDesc(Institution::getCreatedAt);

        IPage<Institution> institutionPage = institutionMapper.selectPage(page, wrapper);

        return institutionPage.convert(institution -> {
            InstitutionListVO vo = new InstitutionListVO();
            BeanUtils.copyProperties(institution, vo);
            return vo;
        });
    }

    @Override
    public InstitutionDetailVO detail(Long id) {
        Institution institution = institutionMapper.selectById(id);
        if (institution == null || institution.getDeleted()) {
            throw new BusinessException(404, "培训机构不存在");
        }

        InstitutionDetailVO vo = new InstitutionDetailVO();
        BeanUtils.copyProperties(institution, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(InstitutionAddDTO dto) {
        OffsetDateTime now = OffsetDateTime.now();
        Long id = SnowflakeIdGenerator.nextId();

        Institution institution = Institution.builder()
                .id(id)
                .name(dto.getName())
                .type(dto.getType())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .description(dto.getDescription())
                .courses(dto.getCourses())
                .images(dto.getImages())
                .logo(dto.getLogo())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .status((short) 1)  // 默认展示状态
                .deleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        institutionMapper.insert(institution);

        log.info("新增培训机构成功: id={}, name={}", id, dto.getName());
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, InstitutionUpdateDTO dto) {
        Institution institution = institutionMapper.selectById(id);
        if (institution == null || institution.getDeleted()) {
            throw new BusinessException(404, "培训机构不存在");
        }

        institution.setName(dto.getName());
        institution.setType(dto.getType());
        institution.setPhone(dto.getPhone());
        institution.setAddress(dto.getAddress());
        institution.setDescription(dto.getDescription());
        institution.setCourses(dto.getCourses());
        institution.setImages(dto.getImages());
        institution.setLogo(dto.getLogo());
        institution.setSortOrder(dto.getSortOrder());
        institution.setUpdatedAt(OffsetDateTime.now());

        institutionMapper.updateById(institution);

        log.info("更新培训机构成功: id={}, name={}", id, dto.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, StatusDTO dto) {
        Institution institution = institutionMapper.selectById(id);
        if (institution == null || institution.getDeleted()) {
            throw new BusinessException(404, "培训机构不存在");
        }

        institution.setStatus(dto.getStatus());
        institution.setUpdatedAt(OffsetDateTime.now());

        institutionMapper.updateById(institution);

        log.info("更新培训机构状态成功: id={}, status={}", id, dto.getStatus());
    }

    @Override
    public void delete(Long id) {
        Institution institution = institutionMapper.selectById(id);
        if (institution == null) {
            throw new BusinessException(404, "培训机构不存在");
        }

        institutionMapper.hardDeleteById(id);

        log.info("硬删除培训机构成功: id={}", id);
    }
}
