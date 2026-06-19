package com.haifeng.app.service.employment.jobIndex;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisMapperBuilderAssistant;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.jobIndex.JobSearchDTO;
import com.haifeng.app.service.impl.employment.jobIndex.JobIndexServiceImpl;
import com.haifeng.app.vo.employment.jobIndex.JobIndexDetailVO;
import com.haifeng.common.entity.employment.jobIndex.JobIndex;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.jobIndex.JobIndexMapper;
import com.haifeng.common.response.ResultCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobIndexServiceImplTest {

    @Mock
    private JobIndexMapper jobIndexMapper;

    @InjectMocks
    private JobIndexServiceImpl service;

    @Captor
    private ArgumentCaptor<LambdaQueryWrapper<JobIndex>> wrapperCaptor;

    @BeforeAll
    static void initTableInfoCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MybatisMapperBuilderAssistant assistant = new MybatisMapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, JobIndex.class);
    }

    @Test
    void page_withKeyword_buildsFuzzyCondition() {
        JobSearchDTO dto = new JobSearchDTO();
        dto.setKeyword("工程师");
        dto.setPage(1);
        dto.setSize(10);
        Page<JobIndex> mockPage = new Page<>(1, 10);
        when(jobIndexMapper.selectPage(any(), wrapperCaptor.capture())).thenReturn(mockPage);

        service.page(dto);

        String sql = wrapperCaptor.getValue().getCustomSqlSegment();
        assertThat(sql).containsIgnoringCase("position_name");
        assertThat(sql).containsIgnoringCase("organization_name");
        assertThat(sql).containsIgnoringCase("LIKE");
        assertThat(sql).contains("is_deleted");
        verify(jobIndexMapper).selectPage(any(), any());
    }

    @Test
    void page_withProvince_buildsExactCondition() {
        JobSearchDTO dto = new JobSearchDTO();
        dto.setProvince("广东省");
        dto.setPage(1);
        dto.setSize(10);
        Page<JobIndex> mockPage = new Page<>(1, 10);
        when(jobIndexMapper.selectPage(any(), wrapperCaptor.capture())).thenReturn(mockPage);

        service.page(dto);

        assertThat(wrapperCaptor.getValue().getCustomSqlSegment()).contains("province");
        verify(jobIndexMapper).selectPage(any(), any());
    }

    @Test
    void page_withSalaryRange_buildsOverlapCondition() {
        JobSearchDTO dto = new JobSearchDTO();
        dto.setSalaryMin(10);
        dto.setSalaryMax(30);
        dto.setPage(1);
        dto.setSize(10);
        Page<JobIndex> mockPage = new Page<>(1, 10);
        when(jobIndexMapper.selectPage(any(), wrapperCaptor.capture())).thenReturn(mockPage);

        service.page(dto);

        String sql = wrapperCaptor.getValue().getCustomSqlSegment();
        assertThat(sql).contains("salary_max");
        assertThat(sql).contains("salary_min");
        verify(jobIndexMapper).selectPage(any(), any());
    }

    @Test
    void page_withPublishDateRange_buildsDateCondition() {
        JobSearchDTO dto = new JobSearchDTO();
        dto.setPublishDateStart(java.time.LocalDate.of(2026, 1, 1));
        dto.setPublishDateEnd(java.time.LocalDate.of(2026, 6, 1));
        dto.setPage(1);
        dto.setSize(10);
        Page<JobIndex> mockPage = new Page<>(1, 10);
        when(jobIndexMapper.selectPage(any(), wrapperCaptor.capture())).thenReturn(mockPage);

        service.page(dto);

        String sql = wrapperCaptor.getValue().getCustomSqlSegment();
        assertThat(sql).contains("publish_date");
        assertThat(sql).contains(">=");
        assertThat(sql).contains("<=");
        verify(jobIndexMapper).selectPage(any(), any());
    }

    @Test
    void page_withEmptyDto_stillFiltersDeleted() {
        JobSearchDTO dto = new JobSearchDTO();
        dto.setPage(1);
        dto.setSize(10);
        Page<JobIndex> mockPage = new Page<>(1, 10);
        when(jobIndexMapper.selectPage(any(), wrapperCaptor.capture())).thenReturn(mockPage);

        service.page(dto);

        String sql = wrapperCaptor.getValue().getCustomSqlSegment();
        assertThat(sql).contains("is_deleted");
        assertThat(sql).doesNotContain("LIKE");
        verify(jobIndexMapper).selectPage(any(), any());
    }

    @Test
    void detail_notFound_throws404() {
        when(jobIndexMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = catchThrowableOfType(
                () -> service.detail(999L), BusinessException.class);

        assertThat(exception.getCode()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        verify(jobIndexMapper).selectById(999L);
    }

    @Test
    void detail_deletedEntity_throws404() {
        JobIndex deleted = JobIndex.builder()
                .id(2L)
                .positionName("已删除岗位")
                .isDeleted(true)
                .build();
        when(jobIndexMapper.selectById(2L)).thenReturn(deleted);

        BusinessException exception = catchThrowableOfType(
                () -> service.detail(2L), BusinessException.class);

        assertThat(exception.getCode()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        verify(jobIndexMapper).selectById(2L);
    }

    @Test
    void detail_found_returnsVO() {
        JobIndex entity = JobIndex.builder()
                .id(1L)
                .sourceType("civil")
                .sourceId(100L)
                .categoryLabel("公务员")
                .positionName("一级科员")
                .organizationName("某单位")
                .province("广东省")
                .city("广州市")
                .educationRequirement("本科")
                .recruitmentCount(2)
                .recruitmentType("国考")
                .salaryMin(8)
                .salaryMax(15)
                .salaryText("8k-15k")
                .positionStatus("招聘中")
                .publishDate(OffsetDateTime.now())
                .isHot(true)
                .viewCount(1000)
                .applyCount(50)
                .isDeleted(false)
                .build();
        when(jobIndexMapper.selectById(1L)).thenReturn(entity);

        JobIndexDetailVO vo = service.detail(1L);

        assertThat(vo.getId()).isEqualTo(1L);
        assertThat(vo.getSourceType()).isEqualTo("civil");
        assertThat(vo.getCategoryLabel()).isEqualTo("公务员");
        assertThat(vo.getPositionStatus()).isEqualTo("招聘中");
        assertThat(vo.getIsHot()).isTrue();
        verify(jobIndexMapper).selectById(1L);
    }
}
