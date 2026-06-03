package com.haifeng.app.service.home;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.home.AnnouncementQueryDTO;
import com.haifeng.app.service.impl.home.AnnouncementServiceImpl;
import com.haifeng.app.vo.home.AnnouncementDetailVO;
import com.haifeng.app.vo.home.AnnouncementListVO;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.dto.common.PageCacheDTO;
import com.haifeng.common.entity.home.Announcement;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.home.AnnouncementMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.OffsetDateTime;
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
class AnnouncementServiceImplTest {

    @Mock private AnnouncementMapper announcementMapper;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOps;

    @InjectMocks private AnnouncementServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    // ===== 列表 =====

    @Test
    void list_cacheHit_returnsDirectlyWithoutQueryingDb() {
        AnnouncementListVO vo = new AnnouncementListVO();
        vo.setId(1L);
        vo.setTitle("cached");
        PageCacheDTO<AnnouncementListVO> cached =
                new PageCacheDTO<>(List.of(vo), 1L, 1L, 10L);

        String key = RedisKeyConstant.getAnnouncementListKey(1, 10, null);
        when(valueOps.get(key)).thenReturn(cached);

        AnnouncementQueryDTO dto = new AnnouncementQueryDTO();
        IPage<AnnouncementListVO> result = service.page(dto);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getTitle()).isEqualTo("cached");
        verify(announcementMapper, never()).selectPage(any(), any());
    }

    @Test
    void list_cacheMiss_queriesDbAndWritesCache() {
        String key = RedisKeyConstant.getAnnouncementListKey(1, 10, null);
        when(valueOps.get(key)).thenReturn(null);

        Announcement entity = Announcement.builder()
                .id(1L).title("t").tag("policy").status((short) 1)
                .updatedAt(OffsetDateTime.now()).deleted(false).build();
        Page<Announcement> mybatisPage = new Page<>(1, 10);
        mybatisPage.setRecords(List.of(entity));
        mybatisPage.setTotal(1);
        when(announcementMapper.selectPage(any(Page.class), any(Wrapper.class)))
                .thenReturn(mybatisPage);

        IPage<AnnouncementListVO> result = service.page(new AnnouncementQueryDTO());

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getTitle()).isEqualTo("t");
        verify(valueOps).set(eq(key), any(PageCacheDTO.class),
                eq(RedisKeyConstant.HOME_CACHE_TTL_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    void list_withTagFilter_usesTagInCacheKey() {
        AnnouncementQueryDTO dto = new AnnouncementQueryDTO();
        dto.setTag("policy");
        String key = RedisKeyConstant.getAnnouncementListKey(1, 10, "policy");
        when(valueOps.get(key)).thenReturn(null);

        Page<Announcement> empty = new Page<>(1, 10);
        empty.setRecords(Collections.emptyList());
        when(announcementMapper.selectPage(any(Page.class), any(Wrapper.class)))
                .thenReturn(empty);

        service.page(dto);

        verify(valueOps).get(key);
        verify(valueOps).set(eq(key), any(PageCacheDTO.class),
                eq(RedisKeyConstant.HOME_CACHE_TTL_MINUTES), eq(TimeUnit.MINUTES));
    }

    // ===== 详情 =====

    @Test
    void detail_cacheHit_returnsDirectly() {
        AnnouncementDetailVO cached = new AnnouncementDetailVO();
        cached.setId(1L);
        cached.setTitle("cached");
        when(valueOps.get(RedisKeyConstant.getAnnouncementDetailKey(1L))).thenReturn(cached);

        AnnouncementDetailVO result = service.detail(1L);

        assertThat(result.getTitle()).isEqualTo("cached");
        verify(announcementMapper, never()).selectById(anyLong());
    }

    @Test
    void detail_cacheMissAndExists_queriesDbAndCaches() {
        String key = RedisKeyConstant.getAnnouncementDetailKey(1L);
        when(valueOps.get(key)).thenReturn(null);
        Announcement entity = Announcement.builder()
                .id(1L).title("t").content("c").tag("g").status((short) 1).deleted(false).build();
        when(announcementMapper.selectById(1L)).thenReturn(entity);

        AnnouncementDetailVO result = service.detail(1L);

        assertThat(result.getTitle()).isEqualTo("t");
        verify(valueOps).set(eq(key), any(AnnouncementDetailVO.class),
                eq(RedisKeyConstant.HOME_CACHE_TTL_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    void detail_notFound_throws404() {
        when(valueOps.get(anyString())).thenReturn(null);
        when(announcementMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> service.detail(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("公告不存在");
    }

    @Test
    void detail_statusZero_throws404() {
        when(valueOps.get(anyString())).thenReturn(null);
        Announcement entity = Announcement.builder()
                .id(1L).title("t").status((short) 0).deleted(false).build();
        when(announcementMapper.selectById(1L)).thenReturn(entity);

        assertThatThrownBy(() -> service.detail(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("公告不存在");
    }
}
