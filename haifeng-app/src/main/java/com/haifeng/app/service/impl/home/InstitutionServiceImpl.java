package com.haifeng.app.service.impl.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.home.InstitutionQueryDTO;
import com.haifeng.app.service.home.InstitutionService;
import com.haifeng.app.vo.home.InstitutionDetailVO;
import com.haifeng.app.vo.home.InstitutionListVO;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.dto.common.PageCacheDTO;
import com.haifeng.common.entity.home.Institution;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.home.InstitutionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstitutionServiceImpl implements InstitutionService {

    private static final short STATUS_PUBLISHED = 1;

    private final InstitutionMapper institutionMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public IPage<InstitutionListVO> page(InstitutionQueryDTO dto) {
        int pageNo = dto.getPage();
        int size = dto.getSize();
        String name = dto.getName();

        String cacheKey = RedisKeyConstant.getInstitutionListKey(pageNo, size, name);

        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof PageCacheDTO) {
            log.debug("培训机构列表缓存命中, key={}", cacheKey);
            return toPage((PageCacheDTO<InstitutionListVO>) cached);
        }
        log.debug("培训机构列表缓存未命中, key={}", cacheKey);

        Page<Institution> page = new Page<>(pageNo, size);
        LambdaQueryWrapper<Institution> wrapper = new LambdaQueryWrapper<Institution>()
                .eq(Institution::getStatus, STATUS_PUBLISHED);

        if (StringUtils.hasText(name)) {
            wrapper.like(Institution::getName, name);
        }

        wrapper.orderByAsc(Institution::getSortOrder)
               .orderByDesc(Institution::getId);

        IPage<Institution> entityPage = institutionMapper.selectPage(page, wrapper);
        IPage<InstitutionListVO> voPage = entityPage.convert(this::toListVO);

        PageCacheDTO<InstitutionListVO> toCache = new PageCacheDTO<>(
                voPage.getRecords(), voPage.getTotal(), voPage.getCurrent(), voPage.getSize());
        redisTemplate.opsForValue().set(cacheKey, toCache,
                RedisKeyConstant.HOME_CACHE_TTL_MINUTES, TimeUnit.MINUTES);

        return voPage;
    }

    @Override
    public InstitutionDetailVO detail(Long id) {
        String cacheKey = RedisKeyConstant.getInstitutionDetailKey(id);

        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof InstitutionDetailVO) {
            log.debug("培训机构详情缓存命中, key={}", cacheKey);
            return (InstitutionDetailVO) cached;
        }
        log.debug("培训机构详情缓存未命中, key={}", cacheKey);

        Institution entity = institutionMapper.selectById(id);
        if (entity == null || entity.getStatus() == null || entity.getStatus() != STATUS_PUBLISHED) {
            throw new BusinessException(ResultCode.NOT_FOUND, "培训机构不存在");
        }

        InstitutionDetailVO vo = new InstitutionDetailVO();
        BeanUtils.copyProperties(entity, vo);
        desensitizePhone(vo);

        redisTemplate.opsForValue().set(cacheKey, vo,
                RedisKeyConstant.HOME_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        return vo;
    }

    private InstitutionListVO toListVO(Institution entity) {
        InstitutionListVO vo = new InstitutionListVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private void desensitizePhone(InstitutionDetailVO vo) {
        if (vo.getPhone() == null || vo.getPhone().isEmpty()) {
            return;
        }
        
        String phone = vo.getPhone();
        // 移除电话号码中的非数字字符（保留+号用于国际号码）
        String digitsOnly = phone.replaceAll("[^0-9+]", "");
        
        if (digitsOnly.length() < 7) {
            // 电话号码太短，无法进行标准脱敏，返回原样
            return;
        }
        
        // 保留前3位和后4位，中间用****替换
        String prefix = digitsOnly.substring(0, 3);
        String suffix = digitsOnly.substring(digitsOnly.length() - 4);
        vo.setPhone(prefix + "****" + suffix);
    }

    private IPage<InstitutionListVO> toPage(PageCacheDTO<InstitutionListVO> cached) {
        Page<InstitutionListVO> page = new Page<>(cached.getCurrent(), cached.getSize(), cached.getTotal());
        page.setRecords(cached.getRecords() == null
                ? Collections.emptyList()
                : cached.getRecords().stream().collect(Collectors.toList()));
        return page;
    }
}
