package com.haifeng.admin.service.impl.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.home.PlannerAddDTO;
import com.haifeng.admin.dto.home.PlannerQueryDTO;
import com.haifeng.admin.dto.home.PlannerUpdateDTO;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.service.home.PlannerService;
import com.haifeng.admin.vo.home.PlannerDetailVO;
import com.haifeng.admin.vo.home.PlannerListVO;
import com.haifeng.common.entity.home.Planner;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.home.PlannerMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlannerServiceImpl implements PlannerService {

    private final PlannerMapper plannerMapper;

    @Override
    public IPage<PlannerListVO> page(PlannerQueryDTO dto) {
        Page<Planner> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Planner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Planner::getDeleted, false);

        // 姓名模糊查询
        if (StringUtils.hasText(dto.getName())) {
            wrapper.like(Planner::getName, dto.getName());
        }
        // 状态筛选
        if (dto.getStatus() != null) {
            wrapper.eq(Planner::getStatus, dto.getStatus());
        }

        // 按sortOrder升序 + createdAt降序排列
        wrapper.orderByAsc(Planner::getSortOrder)
               .orderByDesc(Planner::getCreatedAt);

        IPage<Planner> plannerPage = plannerMapper.selectPage(page, wrapper);

        return plannerPage.convert(planner -> {
            PlannerListVO vo = new PlannerListVO();
            BeanUtils.copyProperties(planner, vo);
            return vo;
        });
    }

    @Override
    public PlannerDetailVO detail(Long id) {
        Planner planner = plannerMapper.selectById(id);
        if (planner == null || planner.getDeleted()) {
            throw new BusinessException(404, "规划师不存在");
        }

        PlannerDetailVO vo = new PlannerDetailVO();
        BeanUtils.copyProperties(planner, vo);
        return vo;
    }

    @Override
    public Long add(PlannerAddDTO dto) {
        OffsetDateTime now = OffsetDateTime.now();
        Long id = SnowflakeIdGenerator.nextId();

        Planner planner = Planner.builder()
                .id(id)
                .name(dto.getName())
                .position(dto.getPosition())
                .region(dto.getRegion())
                .avatar(dto.getAvatar())
                .specialty(dto.getSpecialty())
                .douyinName(dto.getDouyinName())
                .douyinUrl(dto.getDouyinUrl())
                .personalDescription(dto.getPersonalDescription())
                .experienceJob(dto.getExperienceJob())
                .achievements(dto.getAchievements())
                .expertiseAreas(dto.getExpertiseAreas())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .status((short) 1)  // 默认展示状态
                .deleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        plannerMapper.insert(planner);

        log.info("新增规划师成功: id={}, name={}", id, dto.getName());
        return id;
    }

    @Override
    public void update(Long id, PlannerUpdateDTO dto) {
        Planner planner = plannerMapper.selectById(id);
        if (planner == null || planner.getDeleted()) {
            throw new BusinessException(404, "规划师不存在");
        }

        planner.setName(dto.getName());
        planner.setPosition(dto.getPosition());
        planner.setRegion(dto.getRegion());
        planner.setAvatar(dto.getAvatar());
        planner.setSpecialty(dto.getSpecialty());
        planner.setDouyinName(dto.getDouyinName());
        planner.setDouyinUrl(dto.getDouyinUrl());
        planner.setPersonalDescription(dto.getPersonalDescription());
        planner.setExperienceJob(dto.getExperienceJob());
        planner.setAchievements(dto.getAchievements());
        planner.setExpertiseAreas(dto.getExpertiseAreas());
        planner.setSortOrder(dto.getSortOrder());
        planner.setUpdatedAt(OffsetDateTime.now());

        plannerMapper.updateById(planner);

        log.info("更新规划师成功: id={}, name={}", id, dto.getName());
    }

    @Override
    public void updateStatus(Long id, StatusDTO dto) {
        Planner planner = plannerMapper.selectById(id);
        if (planner == null || planner.getDeleted()) {
            throw new BusinessException(404, "规划师不存在");
        }

        planner.setStatus(dto.getStatus());
        planner.setUpdatedAt(OffsetDateTime.now());

        plannerMapper.updateById(planner);

        log.info("更新规划师状态成功: id={}, status={}", id, dto.getStatus());
    }

    @Override
    public void delete(Long id) {
        Planner planner = plannerMapper.selectById(id);
        if (planner == null || planner.getDeleted()) {
            throw new BusinessException(404, "规划师不存在");
        }

        planner.setDeleted(true);
        planner.setUpdatedAt(OffsetDateTime.now());

        plannerMapper.updateById(planner);

        log.info("删除规划师成功: id={}", id);
    }
}
