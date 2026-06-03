package com.haifeng.app.service.impl.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.home.PlannerQueryDTO;
import com.haifeng.app.service.home.PlannerService;
import com.haifeng.app.vo.home.PlannerDetailVO;
import com.haifeng.app.vo.home.PlannerListVO;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.dto.common.PageCacheDTO;
import com.haifeng.common.entity.home.Planner;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.home.PlannerMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlannerServiceImpl implements PlannerService {

    private static final short STATUS_PUBLISHED = 1;

    private final PlannerMapper plannerMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public IPage<PlannerListVO> page(PlannerQueryDTO dto) {
        // 1) 参数校验：region 必须在 ProvinceEnum 中（null 视为合法）
        if (dto.getRegion() != null && !ProvinceEnum.isValid(dto.getRegion())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的省份");
        }

        int pageNo = dto.getPage();
        int size = dto.getSize();
        String region = dto.getRegion();

        // 2) 缓存查询
        String cacheKey = RedisKeyConstant.getPlannerListKey(pageNo, size, region);
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof PageCacheDTO) {
            log.debug("规划师列表缓存命中, key={}", cacheKey);
            return toPage((PageCacheDTO<PlannerListVO>) cached);
        }
        log.debug("规划师列表缓存未命中, key={}", cacheKey);

        // 3) DB 查询
        Page<Planner> page = new Page<>(pageNo, size);
        LambdaQueryWrapper<Planner> wrapper = new LambdaQueryWrapper<Planner>()
                .eq(Planner::getStatus, STATUS_PUBLISHED);
        if (region != null) {
            wrapper.eq(Planner::getRegion, region);
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

        Planner entity = plannerMapper.selectById(id);
        if (entity == null || entity.getStatus() == null || entity.getStatus() != STATUS_PUBLISHED) {
            throw new BusinessException(ResultCode.NOT_FOUND, "规划师不存在");
        }

        PlannerDetailVO vo = new PlannerDetailVO();
        BeanUtils.copyProperties(entity, vo);

        redisTemplate.opsForValue().set(cacheKey, vo,
                RedisKeyConstant.HOME_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        return vo;
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
