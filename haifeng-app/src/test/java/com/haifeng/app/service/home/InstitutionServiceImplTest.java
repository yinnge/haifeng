package com.haifeng.app.service.home;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.home.InstitutionQueryDTO;
import com.haifeng.app.service.impl.home.InstitutionServiceImpl;
import com.haifeng.app.vo.home.InstitutionDetailVO;
import com.haifeng.app.vo.home.InstitutionListVO;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.dto.common.PageCacheDTO;
import com.haifeng.common.entity.home.Institution;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.home.InstitutionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

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
class InstitutionServiceImplTest {

    @Mock private InstitutionMapper institutionMapper;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOps;

    @InjectMocks private InstitutionServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void list_cacheHit_returnsDirectly() {
        InstitutionListVO vo = new InstitutionListVO();
        vo.setId(1L);
        vo.setName("cached");
        PageCacheDTO<InstitutionListVO> cached =
                new PageCacheDTO<>(List.of(vo), 1L, 1L, 10L);
        when(valueOps.get(anyString())).thenReturn(cached);

        IPage<InstitutionListVO> result = service.page(new InstitutionQueryDTO());

        assertThat(result.getRecords().get(0).getName()).isEqualTo("cached");
        verify(institutionMapper, never()).selectPage(any(), any());
    }

    @Test
    void list_cacheMiss_queriesDbAndWritesCache() {
        String key = RedisKeyConstant.getInstitutionListKey(1, 10);
        when(valueOps.get(key)).thenReturn(null);

        Institution entity = Institution.builder()
                .id(1L).name("机构A").type("职业培训").status((short) 1).deleted(false).build();
        Page<Institution> page = new Page<>(1, 10);
        page.setRecords(List.of(entity));
        page.setTotal(1);
        when(institutionMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        IPage<InstitutionListVO> result = service.page(new InstitutionQueryDTO());

        assertThat(result.getRecords()).hasSize(1);
        verify(valueOps).set(eq(key), any(PageCacheDTO.class),
                eq(RedisKeyConstant.HOME_CACHE_TTL_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    void detail_cacheHit_returnsDirectly() {
        InstitutionDetailVO cached = new InstitutionDetailVO();
        cached.setId(1L);
        cached.setName("cached");
        when(valueOps.get(RedisKeyConstant.getInstitutionDetailKey(1L))).thenReturn(cached);

        InstitutionDetailVO result = service.detail(1L);

        assertThat(result.getName()).isEqualTo("cached");
        verify(institutionMapper, never()).selectById(anyLong());
    }

    @Test
    void detail_cacheMissAndExists_queriesDbAndCaches() {
        String key = RedisKeyConstant.getInstitutionDetailKey(1L);
        when(valueOps.get(key)).thenReturn(null);
        Institution entity = Institution.builder()
                .id(1L).name("机构A").status((short) 1).deleted(false).build();
        when(institutionMapper.selectById(1L)).thenReturn(entity);

        InstitutionDetailVO result = service.detail(1L);

        assertThat(result.getName()).isEqualTo("机构A");
        verify(valueOps).set(eq(key), any(InstitutionDetailVO.class),
                eq(RedisKeyConstant.HOME_CACHE_TTL_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    void detail_notFound_throws404() {
        when(valueOps.get(anyString())).thenReturn(null);
        when(institutionMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> service.detail(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("培训机构不存在");
    }

    @Test
    void detail_statusZero_throws404() {
        when(valueOps.get(anyString())).thenReturn(null);
        Institution entity = Institution.builder()
                .id(1L).name("机构A").status((short) 0).deleted(false).build();
        when(institutionMapper.selectById(1L)).thenReturn(entity);

        assertThatThrownBy(() -> service.detail(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("培训机构不存在");
    }
}
