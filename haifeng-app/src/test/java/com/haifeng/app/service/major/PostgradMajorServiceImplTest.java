package com.haifeng.app.service.major;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisMapperBuilderAssistant;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.service.impl.major.PostgradMajorServiceImpl;
import com.haifeng.app.vo.major.UndergraduateMajorDirectionBriefVO;
import com.haifeng.common.dto.common.BasePageQueryDTO;
import com.haifeng.common.entity.major.PostgradMajor;
import com.haifeng.common.mapper.major.MajorPostgradDirectionMapper;
import com.haifeng.common.mapper.major.PostgradMajorMapper;
import com.haifeng.common.mapper.major.PostgradMajorUniversityMapper;
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
class PostgradMajorServiceImplTest {

    @Mock private PostgradMajorMapper postgradMajorMapper;
    @Mock private PostgradMajorUniversityMapper postgradMajorUniversityMapper;
    @Mock private MajorPostgradDirectionMapper majorPostgradDirectionMapper;

    @InjectMocks private PostgradMajorServiceImpl service;

    @Captor private ArgumentCaptor<Page<?>> pageCaptor;

    @BeforeAll
    static void initTableInfoCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MybatisMapperBuilderAssistant assistant = new MybatisMapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, PostgradMajor.class);
    }

    @Test
    void undergraduateMajors_callsMapperAndConvertsResult() {
        BasePageQueryDTO dto = new BasePageQueryDTO();
        dto.setPage(1);
        dto.setSize(20);

        Map<String, Object> row1 = new HashMap<>();
        row1.put("id", 2001L);
        row1.put("majorName", "计算机科学与技术");

        Map<String, Object> row2 = new HashMap<>();
        row2.put("id", 2002L);
        row2.put("majorName", "软件工程");

        Page<Map<String, Object>> page = new Page<>(1, 20);
        page.setRecords(List.of(row1, row2));
        page.setTotal(2);
        when(majorPostgradDirectionMapper.selectMajorsByPostgradMajorId(pageCaptor.capture(), org.mockito.ArgumentMatchers.eq(88L)))
                .thenReturn(page);

        IPage<UndergraduateMajorDirectionBriefVO> result = service.undergraduateMajors(88L, dto);

        assertThat(pageCaptor.getValue().getCurrent()).isEqualTo(1);
        assertThat(pageCaptor.getValue().getSize()).isEqualTo(20);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getRecords()).hasSize(2);
        assertThat(result.getRecords().get(0).getId()).isEqualTo(2001L);
        assertThat(result.getRecords().get(0).getMajorName()).isEqualTo("计算机科学与技术");
        assertThat(result.getRecords().get(1).getId()).isEqualTo(2002L);
        assertThat(result.getRecords().get(1).getMajorName()).isEqualTo("软件工程");
        verify(majorPostgradDirectionMapper).selectMajorsByPostgradMajorId(pageCaptor.capture(), org.mockito.ArgumentMatchers.eq(88L));
    }

    @Test
    void undergraduateMajors_emptyMapperResult_returnsEmptyPage() {
        BasePageQueryDTO dto = new BasePageQueryDTO();
        Page<Map<String, Object>> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);
        when(majorPostgradDirectionMapper.selectMajorsByPostgradMajorId(pageCaptor.capture(), org.mockito.ArgumentMatchers.eq(7L)))
                .thenReturn(page);

        IPage<UndergraduateMajorDirectionBriefVO> result = service.undergraduateMajors(7L, dto);

        assertThat(result.getTotal()).isZero();
        assertThat(result.getRecords()).isEmpty();
    }

    @Test
    void undergraduateMajors_nullFieldsInRowMap_yieldsNullVoFields() {
        BasePageQueryDTO dto = new BasePageQueryDTO();
        Map<String, Object> row = new HashMap<>();
        row.put("id", null);
        row.put("majorName", null);
        Page<Map<String, Object>> page = new Page<>(1, 10);
        page.setRecords(List.of(row));
        page.setTotal(1);
        when(majorPostgradDirectionMapper.selectMajorsByPostgradMajorId(pageCaptor.capture(), org.mockito.ArgumentMatchers.eq(5L)))
                .thenReturn(page);

        IPage<UndergraduateMajorDirectionBriefVO> result = service.undergraduateMajors(5L, dto);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getId()).isNull();
        assertThat(result.getRecords().get(0).getMajorName()).isNull();
    }
}
