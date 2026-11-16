package com.haifeng.app.service.impl.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.home.PlannerQueryDTO;
import com.haifeng.app.service.home.PlannerService;
import com.haifeng.app.vo.home.PlannerAppointmentVO;
import com.haifeng.app.vo.home.PlannerDetailVO;
import com.haifeng.app.vo.home.PlannerListVO;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.dto.common.PageCacheDTO;
import com.haifeng.common.entity.home.Planner;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.enums.ServiceTargetEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.home.PlannerMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlannerServiceImpl implements PlannerService {

    private final PlannerMapper plannerMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public IPage<PlannerListVO> page(PlannerQueryDTO dto) {
        // 1) 参数校验：serviceTarget 必填且合法；province 可选，传了须合法
        if (!ServiceTargetEnum.isValid(dto.getServiceTarget())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "服务对象须为高考或考研");
        }
        String province = dto.getProvince();
        if (province != null && !ProvinceEnum.isValid(province)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的省份");
        }

        int pageNo = dto.getPage();
        int size = dto.getSize();
        String serviceTarget = dto.getServiceTarget();

        // 2) 缓存查询（serviceTarget + province 参与维度）
        String cacheKey = RedisKeyConstant.getPlannerListKey(pageNo, size, serviceTarget, province);
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof PageCacheDTO) {
            log.debug("规划师列表缓存命中, key={}", cacheKey);
            return toPage((PageCacheDTO<PlannerListVO>) cached);
        }
        log.debug("规划师列表缓存未命中, key={}", cacheKey);

        // 3) DB 查询：软删除手动条件（禁用 @TableLogic）
        Page<Planner> page = new Page<>(pageNo, size);
        LambdaQueryWrapper<Planner> wrapper = new LambdaQueryWrapper<Planner>()
                .eq(Planner::getDeleted, false)
                .eq(Planner::getServiceTarget, serviceTarget);
        if (province != null) {
            wrapper.eq(Planner::getProvince, province);
        }
        wrapper.orderByAsc(Planner::getSortOrder)
               .orderByDesc(Planner::getId);

        IPage<Planner> entityPage = plannerMapper.selectPage(page, wrapper);
        IPage<PlannerListVO> voPage = entityPage.convert(this::toListVO);

        // 4) 写缓存
        PageCacheDTO<PlannerListVO> toCache = new PageCacheDTO<>(
                voPage.getRecords(), voPage.getTotal(), voPage.getCurrent(), voPage.getSize());
        redisTemplate.opsForValue().set(cacheKey, toCache,
                RedisKeyConstant.HOME_CACHE_TTL_MINUTES, TimeUnit.MINUTES);

        return voPage;
    }

    @Override
    public PlannerDetailVO detail(Long id) {
        String cacheKey = RedisKeyConstant.getPlannerDetailKey(id);

        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof PlannerDetailVO) {
            log.debug("规划师详情缓存命中, key={}", cacheKey);
            return (PlannerDetailVO) cached;
        }
        log.debug("规划师详情缓存未命中, key={}", cacheKey);

        Planner entity = findActivePlanner(id);

        PlannerDetailVO vo = new PlannerDetailVO();
        BeanUtils.copyProperties(entity, vo);

        redisTemplate.opsForValue().set(cacheKey, vo,
                RedisKeyConstant.HOME_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        return vo;
    }

    @Override
    public PlannerAppointmentVO appointment(Long id) {
        // 联系方式数据敏感且量小，直连 DB 不缓存，保证编辑后实时生效
        Planner entity = findActivePlanner(id);

        PlannerAppointmentVO vo = new PlannerAppointmentVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public List<String> listProvinces(String serviceTarget) {
        if (!ServiceTargetEnum.isValid(serviceTarget)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "服务对象须为高考或考研");
        }
        // DB 实际存在的省份：手动软删条件 + groupBy 去重（范式同 FileLoadServiceImpl.distinctColumn）
        LambdaQueryWrapper<Planner> w = new LambdaQueryWrapper<Planner>()
                .eq(Planner::getDeleted, false)
                .eq(Planner::getServiceTarget, serviceTarget)
                .isNotNull(Planner::getProvince)
                .ne(Planner::getProvince, "")
                .groupBy(Planner::getProvince)
                .select(Planner::getProvince);
        Set<String> existing = plannerMapper.selectObjs(w).stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.toSet());

        // 按 ProvinceEnum 行政顺序输出，保证前端展示稳定
        List<String> ordered = new ArrayList<>();
        for (ProvinceEnum p : ProvinceEnum.values()) {
            if (existing.remove(p.getDesc())) {
                ordered.add(p.getDesc());
            }
        }
        // 枚举外的脏数据兜底追加，避免遗漏（正常不会出现）
        ordered.addAll(existing);
        return ordered;
    }

    /** 查询未软删除的规划师，否则抛 404 */
    private Planner findActivePlanner(Long id) {
        Planner entity = plannerMapper.selectById(id);
        if (entity == null || Boolean.TRUE.equals(entity.getDeleted())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "规划师不存在");
        }
        return entity;
    }

    private PlannerListVO toListVO(Planner entity) {
        PlannerListVO vo = new PlannerListVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private IPage<PlannerListVO> toPage(PageCacheDTO<PlannerListVO> cached) {
        Page<PlannerListVO> page = new Page<>(cached.getCurrent(), cached.getSize(), cached.getTotal());
        page.setRecords(cached.getRecords() == null
                ? Collections.emptyList()
                : cached.getRecords().stream().collect(Collectors.toList()));
        return page;
    }
}
