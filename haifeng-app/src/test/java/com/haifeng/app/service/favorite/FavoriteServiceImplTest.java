package com.haifeng.app.service.favorite;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisMapperBuilderAssistant;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.favorite.FavoriteAddDTO;
import com.haifeng.app.dto.favorite.FavoriteQueryDTO;
import com.haifeng.app.dto.favorite.FavoriteStatusQueryDTO;
import com.haifeng.app.service.impl.favorite.FavoriteServiceImpl;
import com.haifeng.app.service.impl.favorite.FavoriteTargetValidatorRegistry;
import com.haifeng.app.vo.favorite.FavoriteCountItemVO;
import com.haifeng.app.vo.favorite.FavoriteCountsVO;
import com.haifeng.app.vo.favorite.FavoriteItemVO;
import com.haifeng.common.dto.user.FavoriteTypeCountDTO;
import com.haifeng.common.entity.user.MemberFavorite;
import com.haifeng.common.enums.FavoriteTargetType;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.user.MemberFavoriteMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.security.AuthUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplTest {

    private static final Long MEMBER_ID = 100L;

    @Mock
    private MemberFavoriteMapper memberFavoriteMapper;

    @Mock
    private FavoriteTargetValidatorRegistry validatorRegistry;

    @InjectMocks
    private FavoriteServiceImpl service;

    @BeforeAll
    static void initTableInfoCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MybatisMapperBuilderAssistant assistant = new MybatisMapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, MemberFavorite.class);
    }

    @BeforeEach
    void setUp() {
        AuthUser authUser = AuthUser.builder()
                .userId(MEMBER_ID)
                .userType("member")
                .memberType("normal")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void add_validTarget_insertsFavorite() {
        FavoriteAddDTO dto = new FavoriteAddDTO();
        dto.setTargetType("university");
        dto.setTargetId(1L);
        when(validatorRegistry.exists(FavoriteTargetType.UNIVERSITY, 1L)).thenReturn(true);

        service.add(dto);

        verify(memberFavoriteMapper).insertIgnoreConflict(
                anyLong(), eq(MEMBER_ID), eq("university"), eq(1L));
    }

    @Test
    void add_duplicateFavorite_stillSucceeds() {
        FavoriteAddDTO dto = new FavoriteAddDTO();
        dto.setTargetType("major");
        dto.setTargetId(2L);
        when(validatorRegistry.exists(FavoriteTargetType.MAJOR, 2L)).thenReturn(true);
        when(memberFavoriteMapper.insertIgnoreConflict(anyLong(), eq(MEMBER_ID), eq("major"), eq(2L)))
                .thenReturn(0);

        service.add(dto);

        verify(memberFavoriteMapper).insertIgnoreConflict(
                anyLong(), eq(MEMBER_ID), eq("major"), eq(2L));
    }

    @Test
    void add_invalidType_throwsBadRequest() {
        FavoriteAddDTO dto = new FavoriteAddDTO();
        dto.setTargetType("unknown");
        dto.setTargetId(1L);

        BusinessException ex = catchThrowableOfType(() -> service.add(dto), BusinessException.class);

        assertThat(ex.getCode()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(ex.getMsg()).contains("不支持的收藏类型");
        verifyNoInteractions(memberFavoriteMapper);
    }

    @Test
    void add_targetMissing_throwsNotFound() {
        FavoriteAddDTO dto = new FavoriteAddDTO();
        dto.setTargetType("city");
        dto.setTargetId(3L);
        when(validatorRegistry.exists(FavoriteTargetType.CITY, 3L)).thenReturn(false);

        BusinessException ex = catchThrowableOfType(() -> service.add(dto), BusinessException.class);

        assertThat(ex.getCode()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        verify(memberFavoriteMapper, never()).insertIgnoreConflict(anyLong(), anyLong(), any(), anyLong());
    }

    @Test
    void add_nullTargetId_throwsBadRequest() {
        FavoriteAddDTO dto = new FavoriteAddDTO();
        dto.setTargetType("university");

        BusinessException ex = catchThrowableOfType(() -> service.add(dto), BusinessException.class);

        assertThat(ex.getCode()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        verifyNoInteractions(memberFavoriteMapper);
    }

    @Test
    void remove_deletesFavorite() {
        service.remove("industry", 9L);

        verify(memberFavoriteMapper).delete(any(LambdaQueryWrapper.class));
    }

    @Test
    void remove_invalidTargetId_throwsBadRequest() {
        BusinessException ex = catchThrowableOfType(() -> service.remove("industry", 0L), BusinessException.class);

        assertThat(ex.getCode()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        verifyNoInteractions(memberFavoriteMapper);
    }

    @Test
    void page_mapsFavoriteRecords() {
        MemberFavorite entity = MemberFavorite.builder()
                .id(10L)
                .memberId(MEMBER_ID)
                .targetType(FavoriteTargetType.UNIVERSITY)
                .targetId(1L)
                .createdAt(OffsetDateTime.now())
                .build();
        Page<MemberFavorite> dbPage = new Page<>(1, 10);
        dbPage.setRecords(List.of(entity));
        dbPage.setTotal(1);
        when(memberFavoriteMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(dbPage);

        FavoriteQueryDTO dto = new FavoriteQueryDTO();
        dto.setTargetType("university");

        var result = service.page(dto);

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).hasSize(1);
        FavoriteItemVO item = result.getRecords().get(0);
        assertThat(item.getId()).isEqualTo(10L);
        assertThat(item.getTargetType()).isEqualTo("university");
        assertThat(item.getTargetId()).isEqualTo(1L);
    }

    @Test
    void counts_fillsAllTypesAndIgnoresUnknownRows() {
        FavoriteTypeCountDTO university = new FavoriteTypeCountDTO();
        university.setTargetType("university");
        university.setCnt(2L);

        FavoriteTypeCountDTO unknown = new FavoriteTypeCountDTO();
        unknown.setTargetType("removed_module");
        unknown.setCnt(5L);

        when(memberFavoriteMapper.countByType(MEMBER_ID)).thenReturn(List.of(university, unknown));

        FavoriteCountsVO result = service.counts();

        assertThat(result.getTotal()).isEqualTo(2L);
        assertThat(result.getItems()).hasSize(FavoriteTargetType.values().length);
        assertThat(result.getItems())
                .filteredOn(item -> "university".equals(item.getTargetType()))
                .singleElement()
                .extracting(FavoriteCountItemVO::getCount)
                .isEqualTo(2L);
        assertThat(result.getItems())
                .filteredOn(item -> "major".equals(item.getTargetType()))
                .singleElement()
                .extracting(FavoriteCountItemVO::getCount)
                .isEqualTo(0L);
    }

    @Test
    void favoritedIds_emptyList_throwsBadRequest() {
        FavoriteStatusQueryDTO dto = new FavoriteStatusQueryDTO();
        dto.setTargetType("major");
        dto.setTargetIds(List.of());

        BusinessException ex = catchThrowableOfType(() -> service.favoritedIds(dto), BusinessException.class);

        assertThat(ex.getCode()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        verifyNoInteractions(memberFavoriteMapper);
    }

    @Test
    void favoritedIds_returnsOnlyFavoritedTargetIds() {
        MemberFavorite first = MemberFavorite.builder()
                .id(11L)
                .memberId(MEMBER_ID)
                .targetType(FavoriteTargetType.MAJOR)
                .targetId(1L)
                .build();
        MemberFavorite second = MemberFavorite.builder()
                .id(12L)
                .memberId(MEMBER_ID)
                .targetType(FavoriteTargetType.MAJOR)
                .targetId(2L)
                .build();
        when(memberFavoriteMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(first, second));

        FavoriteStatusQueryDTO dto = new FavoriteStatusQueryDTO();
        dto.setTargetType("major");
        dto.setTargetIds(List.of(1L, 2L, 3L));

        List<Long> result = service.favoritedIds(dto);

        assertThat(result).containsExactly(1L, 2L);
    }
}
