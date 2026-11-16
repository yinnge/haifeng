package com.haifeng.app.service.home;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.home.PlannerQueryDTO;
import com.haifeng.app.service.impl.home.PlannerServiceImpl;
import com.haifeng.app.vo.home.PlannerDetailVO;
import com.haifeng.app.vo.home.PlannerListVO;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.dto.common.PageCacheDTO;
import com.haifeng.common.entity.home.Planner;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.home.PlannerMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlannerServiceImplTest {

    @Mock private PlannerMapper plannerMapper;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOps;

    @InjectMocks private PlannerServiceImpl service;

    private PlannerQueryDTO dto(String target, String province) {
        PlannerQueryDTO dto = new PlannerQueryDTO();
        dto.setServiceTarget(target);
        dto.setProvince(province);
        return dto;
    }

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    // ===== serviceTarget 校验 =====

    @Test
    void list_invalidTarget_throws400_doesNotTouchCacheOrDb() {
        assertThatThrownBy(() -> service.page(dto("火星", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("服务对象");

        verify(redisTemplate, never()).opsForValue();
        verify(plannerMapper, never()).selectPage(any(), any());
    }

    @Test
    void list_nullTarget_throws400() {
        assertThatThrownBy(() -> service.page(dto(null, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("服务对象");
    }

    // ===== province 校验 =====

    @Test
    void list_invalidProvince_throws400_doesNotTouchCacheOrDb() {
        assertThatThrownBy(() -> service.page(dto("高考", "火星")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无效的省份");

        verify(redisTemplate, never()).opsForValue();
        verify(plannerMapper, never()).selectPage(any(), any());
    }

    @Test
    void list_nullProvince_proceedsToQuery() {
        when(valueOps.get(anyString())).thenReturn(null);
        Page<Planner> empty = new Page<>(1, 10);
        empty.setRecords(Collections.emptyList());
        when(plannerMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(empty);

        service.page(dto("高考", null));

        verify(plannerMapper).selectPage(any(Page.class), any(Wrapper.class));
    }

    @Test
    void list_validProvince_proceedsToQuery() {
        when(valueOps.get(anyString())).thenReturn(null);
        Page<Planner> empty = new Page<>(1, 10);
        empty.setRecords(Collections.emptyList());
        when(plannerMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(empty);

        service.page(dto("高考", "北京"));

        String key = RedisKeyConstant.getPlannerListKey(1, 10, "高考", "北京");
        verify(valueOps).get(key);
    }

    // ===== 缓存 =====

    @Test
    void list_cacheHit_returnsDirectly() {
        PlannerListVO vo = new PlannerListVO();
        vo.setId(1L);
        vo.setName("cached");
        PageCacheDTO<PlannerListVO> cached =
                new PageCacheDTO<>(List.of(vo), 1L, 1L, 10L);
        when(valueOps.get(anyString())).thenReturn(cached);

        IPage<PlannerListVO> result = service.page(dto("高考", null));

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getName()).isEqualTo("cached");
        verify(plannerMapper, never()).selectPage(any(), any());
    }

    @Test
    void list_cacheMiss_queriesDbAndWritesCache() {
        String key = RedisKeyConstant.getPlannerListKey(1, 10, "高考", null);
        when(valueOps.get(key)).thenReturn(null);

        Planner entity = Planner.builder()
                .id(1L).name("张老师").serviceTarget("高考").province("北京").deleted(false).build();
        Page<Planner> page = new Page<>(1, 10);
        page.setRecords(List.of(entity));
        page.setTotal(1);
        when(plannerMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        IPage<PlannerListVO> result = service.page(dto("高考", null));

        assertThat(result.getRecords()).hasSize(1);
        verify(valueOps).set(eq(key), any(PageCacheDTO.class),
                eq(RedisKeyConstant.HOME_CACHE_TTL_MINUTES), eq(TimeUnit.MINUTES));
    }

    // ===== 详情 =====

    @Test
    void detail_cacheHit_returnsDirectly() {
        PlannerDetailVO cached = new PlannerDetailVO();
        cached.setId(1L);
        cached.setName("cached");
        when(valueOps.get(RedisKeyConstant.getPlannerDetailKey(1L))).thenReturn(cached);

        PlannerDetailVO result = service.detail(1L);

        assertThat(result.getName()).isEqualTo("cached");
        verify(plannerMapper, never()).selectById(anyLong());
    }

    @Test
    void detail_cacheMissAndExists_queriesDbAndCaches() {
        String key = RedisKeyConstant.getPlannerDetailKey(1L);
        when(valueOps.get(key)).thenReturn(null);
        Planner entity = Planner.builder()
                .id(1L).name("张老师").serviceTarget("高考").deleted(false).build();
        when(plannerMapper.selectById(1L)).thenReturn(entity);

        PlannerDetailVO result = service.detail(1L);

        assertThat(result.getName()).isEqualTo("张老师");
        verify(valueOps).set(eq(key), any(PlannerDetailVO.class),
                eq(RedisKeyConstant.HOME_CACHE_TTL_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    void detail_notFound_throws404() {
        when(valueOps.get(anyString())).thenReturn(null);
        when(plannerMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> service.detail(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("规划师不存在");
    }

    @Test
    void detail_softDeleted_throws404() {
        when(valueOps.get(anyString())).thenReturn(null);
        Planner entity = Planner.builder()
                .id(1L).name("张老师").serviceTarget("高考").deleted(true).build();
        when(plannerMapper.selectById(1L)).thenReturn(entity);

        assertThatThrownBy(() -> service.detail(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("规划师不存在");
    }
}
