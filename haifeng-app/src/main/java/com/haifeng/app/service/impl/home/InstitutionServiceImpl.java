package com.haifeng.app.service.impl.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.home.InstitutionQueryDTO;
import com.haifeng.app.service.home.InstitutionService;
import com.haifeng.app.vo.home.InstitutionAppointmentVO;
import com.haifeng.app.vo.home.InstitutionDetailVO;
import com.haifeng.app.vo.home.InstitutionListVO;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.dto.common.PageCacheDTO;
import com.haifeng.common.entity.home.Institution;
import com.haifeng.common.entity.home.InstitutionClassType;
import com.haifeng.common.enums.InstitutionCategoryEnum;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.home.InstitutionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
        String name = trimToNull(dto.getName());
        String category = trimToNull(dto.getCategory());
        String province = trimToNull(dto.getProvince());

        validateFilter(category, province);

        String cacheKey = RedisKeyConstant.getInstitutionListKey(pageNo, size, name, category, province);

        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof PageCacheDTO) {
            log.debug("培训机构列表缓存命中, key={}", cacheKey);
            return toPage((PageCacheDTO<InstitutionListVO>) cached);
        }
        log.debug("培训机构列表缓存未命中, key={}", cacheKey);

        Page<Institution> page = new Page<>(pageNo, size);
        // 列表只取卡片需要的列 + 轮播图（取首图做封面）；介绍/班型等大字段留给详情
        LambdaQueryWrapper<Institution> wrapper = new LambdaQueryWrapper<Institution>()
                .select(Institution::getId, Institution::getName, Institution::getCategory,
                        Institution::getProvince, Institution::getTags, Institution::getCarouselImages)
                .eq(Institution::getStatus, STATUS_PUBLISHED);

        if (name != null) {
            wrapper.like(Institution::getName, name);
        }
        if (category != null) {
            wrapper.eq(Institution::getCategory, category);
        }
        if (province != null) {
            wrapper.eq(Institution::getProvince, province);
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

        Institution entity = getPublished(id);

        InstitutionDetailVO vo = toDetailVO(entity);

        redisTemplate.opsForValue().set(cacheKey, vo,
                RedisKeyConstant.HOME_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        return vo;
    }

    @Override
    public InstitutionAppointmentVO appointment(Long id) {
        // 联系方式数据敏感且量小，直连 DB 不缓存，保证编辑后实时生效
        Institution entity = getPublished(id);

        InstitutionAppointmentVO vo = new InstitutionAppointmentVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setAddress(entity.getAddress());
        vo.setWechatQrcodeUrl(entity.getWechatQrcodeUrl());
        vo.setEmail(entity.getEmail());
        vo.setPhone(entity.getPhone());
        return vo;
    }

    /**
     * 只允许查询已上架（status=1）的机构，不存在或已下架统一 404
     */
    private Institution getPublished(Long id) {
        Institution entity = institutionMapper.selectById(id);
        if (entity == null || entity.getStatus() == null || entity.getStatus() != STATUS_PUBLISHED) {
            throw new BusinessException(ResultCode.NOT_FOUND, "培训机构不存在");
        }
        return entity;
    }

    /**
     * 筛选项校验：不传不校验，传了必须合法
     */
    private static void validateFilter(String category, String province) {
        if (category != null && !InstitutionCategoryEnum.isValid(category)) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "机构分类不合法，只允许：高中冲刺小班/考公考编机构/央国企培训/职业培训机构");
        }
        if (province != null && !ProvinceEnum.isValid(province)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "省份不合法");
        }
    }

    private InstitutionListVO toListVO(Institution entity) {
        InstitutionListVO vo = new InstitutionListVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setCategory(entity.getCategory());
        vo.setProvince(entity.getProvince());
        vo.setTags(copyList(entity.getTags()));
        List<String> images = entity.getCarouselImages();
        vo.setCoverImage(images == null || images.isEmpty() ? null : images.get(0));
        return vo;
    }

    private InstitutionDetailVO toDetailVO(Institution entity) {
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
        return vo;
    }

    private static String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private static List<String> copyList(List<String> source) {
        return source == null ? new ArrayList<>() : new ArrayList<>(source);
    }

    private static List<InstitutionClassType> copyClassTypes(List<InstitutionClassType> source) {
        return source == null ? new ArrayList<>() : new ArrayList<>(source);
    }

    private IPage<InstitutionListVO> toPage(PageCacheDTO<InstitutionListVO> cached) {
        Page<InstitutionListVO> page = new Page<>(cached.getCurrent(), cached.getSize(), cached.getTotal());
        page.setRecords(cached.getRecords() == null
                ? Collections.emptyList()
                : cached.getRecords().stream().collect(Collectors.toList()));
        return page;
    }
}
