package com.haifeng.app.service.major;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.MybatisMapperBuilderAssistant;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.service.impl.major.MajorServiceImpl;
import com.haifeng.app.vo.major.PostgradMajorDirectionBriefVO;
import com.haifeng.app.vo.major.CompetitionBriefVO;
import com.haifeng.common.dto.common.BasePageQueryDTO;
import com.haifeng.common.entity.major.Major;
import com.haifeng.common.mapper.major.MajorDetailMapper;
import com.haifeng.common.mapper.major.MajorMapper;
import com.haifeng.common.mapper.major.MajorPostgradDirectionMapper;
import com.haifeng.common.mapper.certificate.CompetitionMajorMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MajorServiceImplTest {

    @Mock private MajorMapper majorMapper;
    @Mock private MajorDetailMapper majorDetailMapper;
    @Mock private MajorPostgradDirectionMapper majorPostgradDirectionMapper;
    @Mock private CompetitionMajorMapper competitionMajorMapper;

    @InjectMocks private MajorServiceImpl service;

    @Captor private ArgumentCaptor<Page<?>> pageCaptor;

    @BeforeAll
    static void initTableInfoCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MybatisMapperBuilderAssistant assistant = new MybatisMapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, Major.class);
    }

    @Test
    void postgradDirections_callsMapperAndConvertsResult() {
        BasePageQueryDTO dto = new BasePageQueryDTO();
        dto.setPage(1);
        dto.setSize(20);

        Map<String, Object> row1 = new HashMap<>();
        row1.put("id", 1001L);
        row1.put("postgradMajorName", "计算机科学与技术");

        Map<String, Object> row2 = new HashMap<>();
        row2.put("id", 1002L);
        row2.put("postgradMajorName", "软件工程");

        Page<Map<String, Object>> page = new Page<>(1, 20);
        page.setRecords(List.of(row1, row2));
        page.setTotal(2);
        when(majorPostgradDirectionMapper.selectPostgradMajorsByMajorId(pageCaptor.capture(), org.mockito.ArgumentMatchers.eq(42L)))
                .thenReturn(page);

        IPage<PostgradMajorDirectionBriefVO> result = service.postgradDirections(42L, dto);

        assertThat(pageCaptor.getValue().getCurrent()).isEqualTo(1);
        assertThat(pageCaptor.getValue().getSize()).isEqualTo(20);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getRecords()).hasSize(2);
        assertThat(result.getRecords().get(0).getId()).isEqualTo(1001L);
        assertThat(result.getRecords().get(0).getPostgradMajorName()).isEqualTo("计算机科学与技术");
        assertThat(result.getRecords().get(1).getId()).isEqualTo(1002L);
        assertThat(result.getRecords().get(1).getPostgradMajorName()).isEqualTo("软件工程");
        verify(majorPostgradDirectionMapper).selectPostgradMajorsByMajorId(pageCaptor.capture(), org.mockito.ArgumentMatchers.eq(42L));
    }

    @Test
    void postgradDirections_emptyMapperResult_returnsEmptyPage() {
        BasePageQueryDTO dto = new BasePageQueryDTO();
        Page<Map<String, Object>> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);
        when(majorPostgradDirectionMapper.selectPostgradMajorsByMajorId(pageCaptor.capture(), org.mockito.ArgumentMatchers.eq(99L)))
                .thenReturn(page);

        IPage<PostgradMajorDirectionBriefVO> result = service.postgradDirections(99L, dto);

        assertThat(result.getTotal()).isZero();
        assertThat(result.getRecords()).isEmpty();
    }

    @Test
    void postgradDirections_nullFieldsInRowMap_yieldsNullVoFields() {
        BasePageQueryDTO dto = new BasePageQueryDTO();
        Map<String, Object> row = new HashMap<>();
        row.put("id", null);
        row.put("postgradMajorName", null);
        Page<Map<String, Object>> page = new Page<>(1, 10);
        page.setRecords(List.of(row));
        page.setTotal(1);
        when(majorPostgradDirectionMapper.selectPostgradMajorsByMajorId(pageCaptor.capture(), org.mockito.ArgumentMatchers.eq(1L)))
                .thenReturn(page);

        IPage<PostgradMajorDirectionBriefVO> result = service.postgradDirections(1L, dto);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getId()).isNull();
        assertThat(result.getRecords().get(0).getPostgradMajorName()).isNull();
    }

    @Test
    void competitions_MajorNotFound_ShouldThrow() {
        BasePageQueryDTO dto = new BasePageQueryDTO();
        dto.setPage(1);
        dto.setSize(10);
        when(majorMapper.selectOne(org.mockito.ArgumentMatchers.any(LambdaQueryWrapper.class))).thenReturn(null);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.competitions(999L, dto))
                .isInstanceOf(com.haifeng.common.exception.BusinessException.class)
                .hasMessageContaining("专业");
    }

    @Test
    void competitions_Found_ShouldReturnPagedVO() {
        BasePageQueryDTO dto = new BasePageQueryDTO();
        dto.setPage(1);
        dto.setSize(10);

        Major major = new Major();
        major.setId(1L);
        major.setStatus((short) 1);
        when(majorMapper.selectOne(org.mockito.ArgumentMatchers.any(LambdaQueryWrapper.class))).thenReturn(major);

        Map<String, Object> row = new HashMap<>();
        row.put("competitionId", 200L);
        row.put("competitionName", "蓝桥杯");

        Page<Map<String, Object>> page = new Page<>(1, 10);
        page.setRecords(List.of(row));
        page.setTotal(1);
        when(competitionMajorMapper.selectCompetitionsByMajorId(pageCaptor.capture(), org.mockito.ArgumentMatchers.eq(1L)))
                .thenReturn(page);

        IPage<CompetitionBriefVO> result = service.competitions(1L, dto);

        assertThat(pageCaptor.getValue().getCurrent()).isEqualTo(1);
        assertThat(pageCaptor.getValue().getSize()).isEqualTo(10);
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getCompetitionId()).isEqualTo(200L);
        assertThat(result.getRecords().get(0).getCompetitionName()).isEqualTo("蓝桥杯");
    }
}
