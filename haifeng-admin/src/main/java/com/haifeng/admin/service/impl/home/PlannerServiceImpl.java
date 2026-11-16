package com.haifeng.admin.service.impl.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.home.PlannerAddDTO;
import com.haifeng.admin.dto.home.PlannerQueryDTO;
import com.haifeng.admin.dto.home.PlannerUpdateDTO;
import com.haifeng.admin.service.home.PlannerService;
import com.haifeng.admin.vo.home.PlannerDetailVO;
import com.haifeng.admin.vo.home.PlannerListVO;
import com.haifeng.admin.vo.home.TeacherOptionVO;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.entity.home.Planner;
import com.haifeng.common.entity.user.Member;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.enums.ServiceTargetEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.home.PlannerMapper;
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.util.SecurityUtil;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlannerServiceImpl implements PlannerService {

    private final PlannerMapper plannerMapper;
    private final MemberMapper memberMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<TeacherOptionVO> listTeacherOptions() {
        // 教师身份用户：is_teacher=true，且未软删除（显式写条件，与 MemberServiceImpl 保持一致）
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getTeacher, true)
               .eq(Member::getDeleted, false);
        return memberMapper.selectList(wrapper).stream()
                .map(m -> new TeacherOptionVO(m.getId(), m.getUsername()))
                .collect(Collectors.toList());
    }

    @Override
    public IPage<PlannerListVO> page(PlannerQueryDTO dto) {
        // 参数校验：serviceTarget / province 若传则必须合法
        validateFilter(dto.getServiceTarget(), dto.getProvince());

        Page<Planner> page = new Page<>(dto.getPage(), dto.getSize());

        // 软删除手动条件（禁用 @TableLogic）
        LambdaQueryWrapper<Planner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Planner::getDeleted, false);

        if (StringUtils.hasText(dto.getName())) {
            wrapper.like(Planner::getName, dto.getName());
        }
        if (StringUtils.hasText(dto.getServiceTarget())) {
            wrapper.eq(Planner::getServiceTarget, dto.getServiceTarget());
        }
        if (StringUtils.hasText(dto.getProvince())) {
            wrapper.eq(Planner::getProvince, dto.getProvince());
        }

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
        if (planner == null || Boolean.TRUE.equals(planner.getDeleted())) {
            throw new BusinessException(404, "规划师不存在");
        }

        PlannerDetailVO vo = new PlannerDetailVO();
        BeanUtils.copyProperties(planner, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(PlannerAddDTO dto) {
        validateTargetAndProvince(dto.getServiceTarget(), dto.getProvince());
        validateTeacher(dto.getUserId());

        OffsetDateTime now = OffsetDateTime.now();
        Long id = SnowflakeIdGenerator.nextId();
        Long operatorId = SecurityUtil.getCurrentAdminId();

        Planner planner = Planner.builder()
                .id(id)
                .userId(dto.getUserId())
                .name(dto.getName())
                .serviceTarget(dto.getServiceTarget())
                .position(dto.getPosition())
                .province(dto.getProvince())
                .listDesc(dto.getListDesc())
                .listTags(dto.getListTags() == null ? Collections.emptyList() : dto.getListTags())
                .avatarUrl(dto.getAvatarUrl())
                .workYears(dto.getWorkYears())
                .servedCount(dto.getServedCount())
                .draftSla(dto.getDraftSla())
                .certDesc(dto.getCertDesc())
                .description(dto.getDescription())
                .detailTags(dto.getDetailTags() == null ? Collections.emptyList() : dto.getDetailTags())
                .services(dto.getServices() == null ? Collections.emptyList() : dto.getServices())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .wechatQrcodeUrl(dto.getWechatQrcodeUrl())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .version(0)
                .deleted(false)
                .createdBy(operatorId)
                .updatedBy(operatorId)
                .createdAt(now)
                .updatedAt(now)
                .build();

        plannerMapper.insert(planner);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                evictCache();
            }
        });
        log.info("新增规划师成功: id={}, name={}, operator={}", id, dto.getName(), operatorId);
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, PlannerUpdateDTO dto) {
        validateTargetAndProvince(dto.getServiceTarget(), dto.getProvince());
        validateTeacher(dto.getUserId());

        Planner planner = plannerMapper.selectById(id);
        if (planner == null || Boolean.TRUE.equals(planner.getDeleted())) {
            throw new BusinessException(404, "规划师不存在");
        }

        Long operatorId = SecurityUtil.getCurrentAdminId();

        planner.setUserId(dto.getUserId());
        planner.setName(dto.getName());
        planner.setServiceTarget(dto.getServiceTarget());
        planner.setPosition(dto.getPosition());
        planner.setProvince(dto.getProvince());
        planner.setListDesc(dto.getListDesc());
        planner.setListTags(dto.getListTags() == null ? Collections.emptyList() : dto.getListTags());
        planner.setAvatarUrl(dto.getAvatarUrl());
        planner.setWorkYears(dto.getWorkYears());
        planner.setServedCount(dto.getServedCount());
        planner.setDraftSla(dto.getDraftSla());
        planner.setCertDesc(dto.getCertDesc());
        planner.setDescription(dto.getDescription());
        planner.setDetailTags(dto.getDetailTags() == null ? Collections.emptyList() : dto.getDetailTags());
        planner.setServices(dto.getServices() == null ? Collections.emptyList() : dto.getServices());
        planner.setEmail(dto.getEmail());
        planner.setPhone(dto.getPhone());
        planner.setAddress(dto.getAddress());
        planner.setWechatQrcodeUrl(dto.getWechatQrcodeUrl());
        planner.setSortOrder(dto.getSortOrder());
        planner.setUpdatedBy(operatorId);
        planner.setUpdatedAt(OffsetDateTime.now());

        plannerMapper.updateById(planner);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                evictCache();
            }
        });
        log.info("更新规划师成功: id={}, name={}, operator={}", id, dto.getName(), operatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Planner planner = plannerMapper.selectById(id);
        if (planner == null || Boolean.TRUE.equals(planner.getDeleted())) {
            throw new BusinessException(404, "规划师不存在");
        }

        // 软删除：手动置 is_deleted=true（禁用 @TableLogic）
        planner.setDeleted(true);
        planner.setUpdatedBy(SecurityUtil.getCurrentAdminId());
        planner.setUpdatedAt(OffsetDateTime.now());
        plannerMapper.updateById(planner);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                evictCache();
            }
        });
        log.info("软删除规划师成功: id={}", id);
    }

    /** 筛选参数校验（可选字段：传了才校验） */
    private void validateFilter(String serviceTarget, String province) {
        if (StringUtils.hasText(serviceTarget) && !ServiceTargetEnum.isValid(serviceTarget)) {
            throw new BusinessException(400, "服务对象须为高考或考研");
        }
        if (StringUtils.hasText(province) && !ProvinceEnum.isValid(province)) {
            throw new BusinessException(400, "无效的省份");
        }
    }

    /** 新增/修改必填枚举校验 */
    private void validateTargetAndProvince(String serviceTarget, String province) {
        if (!ServiceTargetEnum.isValid(serviceTarget)) {
            throw new BusinessException(400, "服务对象须为高考或考研");
        }
        if (StringUtils.hasText(province) && !ProvinceEnum.isValid(province)) {
            throw new BusinessException(400, "无效的省份");
        }
    }

    /**
     * 绑定用户校验：必须是存在、未软删除、且 is_teacher=true 的用户。
     *
     * <p>注意 Member 带 @TableLogic，selectById 会自动过滤 is_deleted=true，
     * 这里再显式判断 deleted 并校验 teacher，防止非教师账号被绑定为规划师。
     */
    private void validateTeacher(Long userId) {
        if (userId == null) {
            throw new BusinessException(400, "关联用户不能为空");
        }
        Member member = memberMapper.selectById(userId);
        if (member == null || Boolean.TRUE.equals(member.getDeleted())) {
            throw new BusinessException(400, "关联用户不存在");
        }
        if (!Boolean.TRUE.equals(member.getTeacher())) {
            throw new BusinessException(400, "关联用户不是教师身份，请先授予教师身份");
        }
    }

    private void evictCache() {
        try {
            Set<String> listKeys = scanKeys(RedisKeyConstant.HOME_PLANNER_LIST_PREFIX + "*");
            if (!listKeys.isEmpty()) {
                redisTemplate.delete(listKeys);
            }
            Set<String> detailKeys = scanKeys(RedisKeyConstant.HOME_PLANNER_DETAIL_PREFIX + "*");
            if (!detailKeys.isEmpty()) {
                redisTemplate.delete(detailKeys);
            }
        } catch (Exception e) {
            log.error("清除规划师缓存失败", e);
        }
    }

    private Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        try (Cursor<String> cursor = redisTemplate.scan(
                ScanOptions.scanOptions().match(pattern).count(100).build())) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        } catch (Exception e) {
            log.warn("SCAN 缓存 key 失败, pattern={}", pattern, e);
        }
        return keys;
    }
}
