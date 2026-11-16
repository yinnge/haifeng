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
import com.haifeng.admin.vo.home.InstitutionOptionVO;
import com.haifeng.admin.vo.home.TeacherOptionVO;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.entity.home.Institution;
import com.haifeng.common.entity.home.InstitutionClassType;
import com.haifeng.common.entity.user.Member;
import com.haifeng.common.enums.InstitutionCategoryEnum;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.home.InstitutionMapper;
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.SecurityUtil;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 首页管理 - 培训机构
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstitutionServiceImpl implements InstitutionService {

    private static final short DEFAULT_STATUS = 1;
    private static final int DEFAULT_SORT_ORDER = 0;

    private final InstitutionMapper institutionMapper;
    private final MemberMapper memberMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public IPage<InstitutionListVO> page(InstitutionQueryDTO dto) {
        Page<Institution> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Institution> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getName())) {
            wrapper.like(Institution::getName, dto.getName().trim());
        }
        if (StringUtils.hasText(dto.getCategory())) {
            wrapper.eq(Institution::getCategory, dto.getCategory().trim());
        }
        if (StringUtils.hasText(dto.getProvince())) {
            wrapper.eq(Institution::getProvince, dto.getProvince().trim());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(Institution::getStatus, dto.getStatus());
        }
        if (dto.getUserId() != null) {
            wrapper.eq(Institution::getUserId, dto.getUserId());
        }

        // 列表只查轻量字段，介绍/轮播图/班型等大字段留给详情
        wrapper.select(Institution::getId, Institution::getName, Institution::getCategory,
                        Institution::getProvince, Institution::getFoundedYear, Institution::getAddress,
                        Institution::getPhone, Institution::getEmail, Institution::getUserId,
                        Institution::getStatus, Institution::getSortOrder, Institution::getCreatedAt)
                .orderByAsc(Institution::getSortOrder)
                .orderByDesc(Institution::getId);

        IPage<Institution> entityPage = institutionMapper.selectPage(page, wrapper);
        Map<Long, Member> managerMap = loadManagers(entityPage.getRecords().stream()
                .map(Institution::getUserId)
                .collect(Collectors.toSet()));

        return entityPage.convert(entity -> toListVO(entity, managerMap.get(entity.getUserId())));
    }

    @Override
    public InstitutionDetailVO detail(Long id) {
        Institution entity = getExisting(id);
        Member manager = entity.getUserId() == null ? null : memberMapper.selectById(entity.getUserId());
        return toDetailVO(entity, manager);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(InstitutionAddDTO dto) {
        String category = requireCategory(dto.getCategory());
        String province = requireProvince(dto.getProvince());
        validateManager(dto.getUserId(), null);

        Long id = SnowflakeIdGenerator.nextId();
        OffsetDateTime now = OffsetDateTime.now();
        Long adminId = SecurityUtil.getCurrentAdminId();

        Institution entity = Institution.builder()
                .id(id)
                .category(category)
                .userId(dto.getUserId())
                .name(dto.getName().trim())
                .province(province)
                .foundedYear(trimToNull(dto.getFoundedYear()))
                .carouselImages(copyList(dto.getCarouselImages()))
                .tags(copyList(dto.getTags()))
                .introduction(trimToNull(dto.getIntroduction()))
                .mainDirection(trimToNull(dto.getMainDirection()))
                .classSize(trimToNull(dto.getClassSize()))
                .teacherConfig(trimToNull(dto.getTeacherConfig()))
                .courseForm(trimToNull(dto.getCourseForm()))
                .classTypes(copyClassTypes(dto.getClassTypes()))
                .notice(trimToNull(dto.getNotice()))
                .advantages(copyList(dto.getAdvantages()))
                .address(trimToNull(dto.getAddress()))
                .wechatQrcodeUrl(trimToNull(dto.getWechatQrcodeUrl()))
                .email(trimToNull(dto.getEmail()))
                .phone(trimToNull(dto.getPhone()))
                .status(DEFAULT_STATUS)
                .sortOrder(dto.getSortOrder() == null ? DEFAULT_SORT_ORDER : dto.getSortOrder())
                .version(0)
                .createdBy(adminId)
                .updatedBy(adminId)
                .createdAt(now)
                .updatedAt(now)
                .build();

        try {
            institutionMapper.insert(entity);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该会员账号已绑定其他机构");
        }

        evictCacheAfterCommit();
        log.info("新增培训机构成功: id={}, name={}, category={}", id, entity.getName(), entity.getCategory());
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, InstitutionUpdateDTO dto) {
        Institution entity = getExisting(id);

        String category = requireCategory(dto.getCategory());
        String province = requireProvince(dto.getProvince());
        validateManager(dto.getUserId(), id);

        entity.setCategory(category);
        entity.setUserId(dto.getUserId());
        entity.setName(dto.getName().trim());
        entity.setProvince(province);
        entity.setFoundedYear(trimToNull(dto.getFoundedYear()));
        entity.setCarouselImages(copyList(dto.getCarouselImages()));
        entity.setTags(copyList(dto.getTags()));
        entity.setIntroduction(trimToNull(dto.getIntroduction()));
        entity.setMainDirection(trimToNull(dto.getMainDirection()));
        entity.setClassSize(trimToNull(dto.getClassSize()));
        entity.setTeacherConfig(trimToNull(dto.getTeacherConfig()));
        entity.setCourseForm(trimToNull(dto.getCourseForm()));
        entity.setClassTypes(copyClassTypes(dto.getClassTypes()));
        entity.setNotice(trimToNull(dto.getNotice()));
        entity.setAdvantages(copyList(dto.getAdvantages()));
        entity.setAddress(trimToNull(dto.getAddress()));
        entity.setWechatQrcodeUrl(trimToNull(dto.getWechatQrcodeUrl()));
        entity.setEmail(trimToNull(dto.getEmail()));
        entity.setPhone(trimToNull(dto.getPhone()));
        entity.setSortOrder(dto.getSortOrder() == null ? DEFAULT_SORT_ORDER : dto.getSortOrder());
        entity.setUpdatedBy(SecurityUtil.getCurrentAdminId());
        entity.setUpdatedAt(OffsetDateTime.now());

        int rows;
        try {
            rows = institutionMapper.updateById(entity);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该会员账号已绑定其他机构");
        }
        if (rows == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "数据已被他人修改，请刷新后重试");
        }

        evictCacheAfterCommit();
        log.info("更新培训机构成功: id={}, name={}", id, entity.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, StatusDTO dto) {
        Institution entity = getExisting(id);

        entity.setStatus(dto.getStatus());
        entity.setUpdatedBy(SecurityUtil.getCurrentAdminId());
        entity.setUpdatedAt(OffsetDateTime.now());

        int rows = institutionMapper.updateById(entity);
        if (rows == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "数据已被他人修改，请刷新后重试");
        }

        evictCacheAfterCommit();
        log.info("更新培训机构状态成功: id={}, status={}", id, dto.getStatus());
    }

    @Override
    public void delete(Long id) {
        getExisting(id);
        institutionMapper.deleteById(id);
        evictCache();
        log.info("删除培训机构成功: id={}", id);
    }

    @Override
    public List<InstitutionOptionVO> categories() {
        return Arrays.stream(InstitutionCategoryEnum.values())
                .map(item -> new InstitutionOptionVO(item.getValue(), item.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<TeacherOptionVO> listTeacherOptions() {
        // Member 带 @TableLogic，已自动过滤 is_deleted = true
        return memberMapper.selectList(new LambdaQueryWrapper<Member>()
                        .eq(Member::getTeacher, true)
                        .orderByAsc(Member::getUsername))
                .stream()
                .map(member -> new TeacherOptionVO(member.getId(), member.getUsername()))
                .collect(Collectors.toList());
    }

    private Institution getExisting(Long id) {
        Institution entity = institutionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "培训机构不存在");
        }
        return entity;
    }

    private String requireCategory(String category) {
        String value = category == null ? null : category.trim();
        if (!InstitutionCategoryEnum.isValid(value)) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "机构分类不合法，只允许：高中冲刺小班/考公考编机构/央国企培训/职业培训机构");
        }
        return value;
    }

    private String requireProvince(String province) {
        String value = province == null ? null : province.trim();
        if (!StringUtils.hasText(value) || !ProvinceEnum.isValid(value)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "省份不合法");
        }
        return value;
    }

    /**
     * 管理账号必须是未被占用的老师账号
     *
     * @param excludeId 修改时排除当前机构自身
     */
    private void validateManager(Long userId, Long excludeId) {
        Member member = memberMapper.selectById(userId);
        if (member == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "管理账号不存在");
        }
        if (!Boolean.TRUE.equals(member.getTeacher())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "管理账号必须是老师身份");
        }

        Long boundCount = institutionMapper.selectCount(new LambdaQueryWrapper<Institution>()
                .eq(Institution::getUserId, userId)
                .ne(excludeId != null, Institution::getId, excludeId));
        if (boundCount != null && boundCount > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该会员账号已绑定其他机构");
        }
    }

    private Map<Long, Member> loadManagers(Collection<Long> userIds) {
        Set<Long> distinctIds = userIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (distinctIds.isEmpty()) {
            return Map.of();
        }
        return memberMapper.selectBatchIds(distinctIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity(), (a, b) -> a));
    }

    private InstitutionListVO toListVO(Institution entity, Member manager) {
        InstitutionListVO vo = new InstitutionListVO();
        vo.setId(entity.getId());
        vo.setCategory(entity.getCategory());
        vo.setName(entity.getName());
        vo.setProvince(entity.getProvince());
        vo.setFoundedYear(entity.getFoundedYear());
        vo.setAddress(entity.getAddress());
        vo.setPhone(entity.getPhone());
        vo.setEmail(entity.getEmail());
        vo.setUserId(entity.getUserId());
        if (manager != null) {
            vo.setManagerUsername(manager.getUsername());
            vo.setManagerPhone(manager.getPhone());
        }
        vo.setStatus(entity.getStatus());
        vo.setSortOrder(entity.getSortOrder());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    private InstitutionDetailVO toDetailVO(Institution entity, Member manager) {
        InstitutionDetailVO vo = new InstitutionDetailVO();
        vo.setId(entity.getId());
        vo.setCategory(entity.getCategory());
        vo.setName(entity.getName());
        vo.setProvince(entity.getProvince());
        vo.setFoundedYear(entity.getFoundedYear());
        vo.setCarouselImages(copyList(entity.getCarouselImages()));
        vo.setTags(copyList(entity.getTags()));
        vo.setIntroduction(entity.getIntroduction());
        vo.setMainDirection(entity.getMainDirection());
        vo.setClassSize(entity.getClassSize());
        vo.setTeacherConfig(entity.getTeacherConfig());
        vo.setCourseForm(entity.getCourseForm());
        vo.setClassTypes(copyClassTypes(entity.getClassTypes()));
        vo.setNotice(entity.getNotice());
        vo.setAdvantages(copyList(entity.getAdvantages()));
        vo.setAddress(entity.getAddress());
        vo.setWechatQrcodeUrl(entity.getWechatQrcodeUrl());
        vo.setEmail(entity.getEmail());
        vo.setPhone(entity.getPhone());
        vo.setUserId(entity.getUserId());
        if (manager != null) {
            vo.setManagerUsername(manager.getUsername());
            vo.setManagerPhone(manager.getPhone());
        }
        vo.setStatus(entity.getStatus());
        vo.setSortOrder(entity.getSortOrder());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static List<String> copyList(List<String> source) {
        return source == null ? new ArrayList<>() : new ArrayList<>(source);
    }

    private static List<InstitutionClassType> copyClassTypes(List<InstitutionClassType> source) {
        if (source == null) {
            return new ArrayList<>();
        }
        List<InstitutionClassType> target = new ArrayList<>(source.size());
        for (InstitutionClassType item : source) {
            if (item == null) {
                continue;
            }
            target.add(InstitutionClassType.builder()
                    .title(trimToNull(item.getTitle()))
                    .introduction(trimToNull(item.getIntroduction()))
                    .tag(trimToNull(item.getTag()))
                    .build());
        }
        return target;
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

    private void evictCache() {
        try {
            Set<String> listKeys = scanKeys(RedisKeyConstant.HOME_INSTITUTION_LIST_PREFIX + "*");
            if (!listKeys.isEmpty()) {
                redisTemplate.delete(listKeys);
            }
            Set<String> detailKeys = scanKeys(RedisKeyConstant.HOME_INSTITUTION_DETAIL_PREFIX + "*");
            if (!detailKeys.isEmpty()) {
                redisTemplate.delete(detailKeys);
            }
        } catch (Exception e) {
            log.error("清除培训机构缓存失败", e);
        }
    }

    private Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        Cursor<String> cursor = redisTemplate.scan(
                ScanOptions.scanOptions().match(pattern).count(100).build());
        if (cursor == null) {
            return keys;
        }
        try (cursor) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        } catch (Exception e) {
            log.warn("SCAN 缓存 key 失败, pattern={}", pattern, e);
        }
        return keys;
    }
}
