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
import com.haifeng.admin.vo.home.TeacherOptionVO;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.entity.home.Planner;
import com.haifeng.common.entity.user.Member;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.enums.ServiceTargetEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.home.PlannerMapper;
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.service.resource.OssService;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlannerServiceImpl implements PlannerService {

    private final PlannerMapper plannerMapper;
    private final MemberMapper memberMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final OssService ossService;

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
        // status=null 查全部（含已下线）；status=1 只查在线；status=0 只查已下线
        LambdaQueryWrapper<Planner> wrapper = new LambdaQueryWrapper<>();
        if (dto.getStatus() != null) {
            wrapper.eq(Planner::getDeleted, dto.getStatus() == 0);
        }

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

        // 批量取绑定的教师账号，避免 N+1
        Map<Long, Member> teacherMap = loadTeachers(plannerPage.getRecords().stream()
                .map(Planner::getUserId)
                .collect(Collectors.toSet()));

        return plannerPage.convert(planner -> {
            PlannerListVO vo = new PlannerListVO();
            BeanUtils.copyProperties(planner, vo);
            vo.setStatus(Boolean.TRUE.equals(planner.getDeleted()) ? (short) 0 : (short) 1);
            Member teacher = teacherMap.get(planner.getUserId());
            if (teacher != null) {
                vo.setTeacherUsername(teacher.getUsername());
            }
            return vo;
        });
    }

    @Override
    public PlannerDetailVO detail(Long id) {
        Planner planner = plannerMapper.selectById(id);
        if (planner == null) {
            throw new BusinessException(404, "规划师不存在");
        }

        PlannerDetailVO vo = new PlannerDetailVO();
        BeanUtils.copyProperties(planner, vo);
        vo.setStatus(Boolean.TRUE.equals(planner.getDeleted()) ? (short) 0 : (short) 1);
        if (planner.getUserId() != null) {
            Member teacher = memberMapper.selectById(planner.getUserId());
            if (teacher != null) {
                vo.setTeacherUsername(teacher.getUsername());
            }
        }
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

        evictCacheAfterCommit();
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

        // 收集被替换掉的旧图片 URL（旧值存在且新值不同），事务提交后删 OSS，避免"删了图却回滚"
        // null 统一归一为空串：MyBatis-Plus 默认 NOT_NULL 策略会跳过 null 字段，
        // 若不归一，前端清空（或旧版前端省略字段）时 DB 残留旧 URL、OSS 却已删，出现破图。
        List<String> obsoleteImageUrls = new ArrayList<>();
        String newAvatarUrl = dto.getAvatarUrl() == null ? "" : dto.getAvatarUrl();
        String newQrcodeUrl = dto.getWechatQrcodeUrl() == null ? "" : dto.getWechatQrcodeUrl();
        collectReplacedUrl(planner.getAvatarUrl(), newAvatarUrl, obsoleteImageUrls);
        collectReplacedUrl(planner.getWechatQrcodeUrl(), newQrcodeUrl, obsoleteImageUrls);

        planner.setUserId(dto.getUserId());
        planner.setName(dto.getName());
        planner.setServiceTarget(dto.getServiceTarget());
        planner.setPosition(dto.getPosition());
        planner.setProvince(dto.getProvince());
        planner.setListDesc(dto.getListDesc());
        planner.setListTags(dto.getListTags() == null ? Collections.emptyList() : dto.getListTags());
        planner.setAvatarUrl(newAvatarUrl);
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
        planner.setWechatQrcodeUrl(newQrcodeUrl);
        planner.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        planner.setUpdatedBy(operatorId);
        planner.setUpdatedAt(OffsetDateTime.now());

        plannerMapper.updateById(planner);

        deleteOssAfterCommit(obsoleteImageUrls);
        evictCacheAfterCommit();
        log.info("更新规划师成功: id={}, name={}, operator={}", id, dto.getName(), operatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, StatusDTO dto) {
        Planner planner = plannerMapper.selectById(id);
        if (planner == null) {
            throw new BusinessException(404, "规划师不存在");
        }

        // 规划师无 status 列：状态由软删除标记承担，1=上线，0=下线
        boolean deleted = dto.getStatus() == 0;
        if (Boolean.TRUE.equals(planner.getDeleted()) == deleted) {
            // 状态未变化，直接返回（幂等）
            return;
        }

        planner.setDeleted(deleted);
        planner.setUpdatedBy(SecurityUtil.getCurrentAdminId());
        planner.setUpdatedAt(OffsetDateTime.now());
        plannerMapper.updateById(planner);

        evictCacheAfterCommit();
        log.info("修改规划师上下架状态成功: id={}, status={}", id, dto.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDelete(Long id) {
        Planner planner = plannerMapper.selectById(id);
        if (planner == null) {
            throw new BusinessException(404, "规划师不存在");
        }

        // 物理删除：无论当前是否已下线都允许彻底移除；同时清掉该规划师引用的图片
        List<String> obsoleteImageUrls = new ArrayList<>();
        addIfPresent(planner.getAvatarUrl(), obsoleteImageUrls);
        addIfPresent(planner.getWechatQrcodeUrl(), obsoleteImageUrls);
        plannerMapper.deleteById(id);

        deleteOssAfterCommit(obsoleteImageUrls);
        evictCacheAfterCommit();
        log.info("彻底删除规划师成功: id={}", id);
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

    /** 批量取教师账号，避免列表 N+1 */
    private Map<Long, Member> loadTeachers(Collection<Long> userIds) {
        Set<Long> distinctIds = userIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (distinctIds.isEmpty()) {
            return Map.of();
        }
        return memberMapper.selectBatchIds(distinctIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity(), (a, b) -> a));
    }

    /** 旧值存在且被换成新值（含被清空）时，登记旧 URL 为待删除 */
    private static void collectReplacedUrl(String oldUrl, String newUrl, List<String> obsoleteUrls) {
        if (StringUtils.hasText(oldUrl) && !oldUrl.equals(newUrl)) {
            obsoleteUrls.add(oldUrl);
        }
    }

    /** URL 非空则加入待删列表（删除整行场景用） */
    private static void addIfPresent(String url, List<String> obsoleteUrls) {
        if (StringUtils.hasText(url)) {
            obsoleteUrls.add(url);
        }
    }

    /**
     * 事务提交后再删 OSS 图片：先保证 DB 变更落地，删除失败也只留孤儿对象，不会因回滚丢图。
     * 有事务走 afterCommit，无事务（单测/非事务调用）则立即删。
     */
    private void deleteOssAfterCommit(List<String> obsoleteUrls) {
        if (obsoleteUrls == null || obsoleteUrls.isEmpty()) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    doDeleteOss(obsoleteUrls);
                }
            });
        } else {
            doDeleteOss(obsoleteUrls);
        }
    }

    private void doDeleteOss(List<String> obsoleteUrls) {
        for (String url : obsoleteUrls) {
            ossService.deletePublicFileByUrl(url);
        }
    }

    /**
     * 有事务时提交后清缓存，无事务（如单测/非事务方法）时立即清
     */
    private void evictCacheAfterCommit() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    evictCache();
                }
            });
        } else {
            evictCache();
        }
    }

    private void evictCache() {        try {
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
