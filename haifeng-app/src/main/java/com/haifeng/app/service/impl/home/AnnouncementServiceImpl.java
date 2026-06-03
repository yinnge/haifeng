package com.haifeng.app.service.impl.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.home.AnnouncementQueryDTO;
import com.haifeng.app.service.home.AnnouncementService;
import com.haifeng.app.vo.home.AnnouncementDetailVO;
import com.haifeng.app.vo.home.AnnouncementListVO;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.dto.common.PageCacheDTO;
import com.haifeng.common.entity.home.Announcement;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.home.AnnouncementMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private static final short STATUS_PUBLISHED = 1;

    private final AnnouncementMapper announcementMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public IPage<AnnouncementListVO> page(AnnouncementQueryDTO dto) {
        int pageNo = dto.getPage();
        int size = dto.getSize();
        String tag = dto.getTag();

        String cacheKey = RedisKeyConstant.getAnnouncementListKey(pageNo, size, tag);

        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof PageCacheDTO) {
            log.debug("公告列表缓存命中, key={}", cacheKey);
            PageCacheDTO<AnnouncementListVO> dtoCached = (PageCacheDTO<AnnouncementListVO>) cached;
            return toPage(dtoCached);
        }
        log.debug("公告列表缓存未命中, key={}", cacheKey);

        Page<Announcement> page = new Page<>(pageNo, size);
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, STATUS_PUBLISHED);
        if (StringUtils.hasText(tag)) {
            wrapper.eq(Announcement::getTag, tag);
        }
        wrapper.orderByDesc(Announcement::getUpdatedAt);

        IPage<Announcement> entityPage = announcementMapper.selectPage(page, wrapper);
        IPage<AnnouncementListVO> voPage = entityPage.convert(this::toListVO);

        PageCacheDTO<AnnouncementListVO> toCache = new PageCacheDTO<>(
                voPage.getRecords(),
                voPage.getTotal(),
                voPage.getCurrent(),
                voPage.getSize()
        );
        redisTemplate.opsForValue().set(cacheKey, toCache,
                RedisKeyConstant.HOME_CACHE_TTL_MINUTES, TimeUnit.MINUTES);

        return voPage;
    }

    @Override
    public AnnouncementDetailVO detail(Long id) {
        String cacheKey = RedisKeyConstant.getAnnouncementDetailKey(id);

        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof AnnouncementDetailVO) {
            log.debug("公告详情缓存命中, key={}", cacheKey);
            return (AnnouncementDetailVO) cached;
        }
        log.debug("公告详情缓存未命中, key={}", cacheKey);

        Announcement entity = announcementMapper.selectById(id);
        if (entity == null || entity.getStatus() == null || entity.getStatus() != STATUS_PUBLISHED) {
            throw new BusinessException(ResultCode.NOT_FOUND, "公告不存在");
        }

        AnnouncementDetailVO vo = new AnnouncementDetailVO();
        BeanUtils.copyProperties(entity, vo);

        redisTemplate.opsForValue().set(cacheKey, vo,
                RedisKeyConstant.HOME_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        return vo;
    }

    private AnnouncementListVO toListVO(Announcement entity) {
        AnnouncementListVO vo = new AnnouncementListVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private IPage<AnnouncementListVO> toPage(PageCacheDTO<AnnouncementListVO> cached) {
        Page<AnnouncementListVO> page = new Page<>(cached.getCurrent(), cached.getSize(), cached.getTotal());
        page.setRecords(cached.getRecords() == null
                ? java.util.Collections.emptyList()
                : cached.getRecords().stream().collect(Collectors.toList()));
        return page;
    }
}
