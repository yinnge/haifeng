package com.haifeng.app.service.company;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisMapperBuilderAssistant;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.company.EnterpriseQueryDTO;
import com.haifeng.app.service.impl.company.EnterpriseServiceImpl;
import com.haifeng.app.vo.company.EnterpriseIndustryGroupVO;
import com.haifeng.app.vo.company.EnterpriseListVO;
import com.haifeng.app.vo.company.EnterprisePositionVO;
import com.haifeng.common.entity.company.Enterprise;
import com.haifeng.common.entity.company.EnterpriseIndustry;
import com.haifeng.common.entity.company.EnterprisePosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.company.EnterpriseIndustryMapper;
import com.haifeng.common.mapper.company.EnterpriseMapper;
import com.haifeng.common.mapper.company.EnterprisePositionMapper;
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
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnterpriseServiceImplTest {

    @Mock private EnterpriseMapper enterpriseMapper;
    @Mock private EnterprisePositionMapper enterprisePositionMapper;
    @Mock private EnterpriseIndustryMapper enterpriseIndustryMapper;

    @InjectMocks private EnterpriseServiceImpl service;

    @Captor private ArgumentCaptor<Page<Enterprise>> pageCaptor;
    @Captor private ArgumentCaptor<Wrapper<Enterprise>> enterpriseWrapperCaptor;
    @Captor private ArgumentCaptor<LambdaQueryWrapper<EnterprisePosition>> positionWrapperCaptor;
    @Captor private ArgumentCaptor<LambdaQueryWrapper<EnterpriseIndustry>> industryWrapperCaptor;

    @BeforeAll
    static void initTableInfoCache() {
        // LambdaQueryWrapper.getCustomSqlSegment() requires MyBatis-Plus table metadata.
        // In unit tests there is no Spring context to trigger that initialization, so
        // we prime the per-entity cache manually.
        MybatisConfiguration configuration = new MybatisConfiguration();
        MybatisMapperBuilderAssistant assistant = new MybatisMapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, Enterprise.class);
        TableInfoHelper.initTableInfo(assistant, EnterprisePosition.class);
        TableInfoHelper.initTableInfo(assistant, EnterpriseIndustry.class);
    }

    @Test
    void page_returnsConvertedVOs() {
        Enterprise entity = Enterprise.builder()
                .id(1L)
                .cityName("深圳")
                .enterpriseName("华为技术有限公司")
                .enterpriseNature("民企")
                .enterpriseType("科技")
                .logoUrl("https://example.com/logo.png")
                .officialWebsite("https://www.huawei.com")
                .region("华南")
                .enterpriseScale("10万人以上")
                .mainBusiness("通信设备")
                .enterpriseIntro("企业介绍")
                .isDeleted(false)
                .build();
        Page<Enterprise> page = new Page<>(1, 10);
        page.setRecords(List.of(entity));
        page.setTotal(1);
        when(enterpriseMapper.selectPage(any(), any())).thenReturn(page);

        IPage<EnterpriseListVO> result = service.page(new EnterpriseQueryDTO());

        assertThat(result.getRecords()).hasSize(1);
        EnterpriseListVO vo = result.getRecords().get(0);
        assertThat(vo.getId()).isEqualTo(1L);
        assertThat(vo.getCityName()).isEqualTo("深圳");
        assertThat(vo.getEnterpriseName()).isEqualTo("华为技术有限公司");
        assertThat(vo.getEnterpriseNature()).isEqualTo("民企");
        assertThat(vo.getEnterpriseType()).isEqualTo("科技");
        assertThat(vo.getLogoUrl()).isEqualTo("https://example.com/logo.png");
        assertThat(vo.getOfficialWebsite()).isEqualTo("https://www.huawei.com");
        assertThat(vo.getRegion()).isEqualTo("华南");
        assertThat(vo.getEnterpriseScale()).isEqualTo("10万人以上");
        assertThat(vo.getMainBusiness()).isEqualTo("通信设备");
        assertThat(vo.getEnterpriseIntro()).isEqualTo("企业介绍");
    }

    @Test
    void page_passesPageNumberAndSizeFromDto() {
        EnterpriseQueryDTO dto = new EnterpriseQueryDTO();
        dto.setPage(2);
        dto.setSize(20);
        Page<Enterprise> page = new Page<>(2, 20);
        page.setRecords(List.of());
        page.setTotal(0);

        when(enterpriseMapper.selectPage(pageCaptor.capture(), any())).thenReturn(page);

        service.page(dto);

        assertThat(pageCaptor.getValue().getCurrent()).isEqualTo(2L);
        assertThat(pageCaptor.getValue().getSize()).isEqualTo(20L);
    }

    @Test
    void page_populatedDtoBuildsExpectedQueryWrapper() {
        EnterpriseQueryDTO dto = new EnterpriseQueryDTO();
        dto.setEnterpriseName("华为");
        dto.setEnterpriseNature("民企");
        dto.setEnterpriseType("科技");
        dto.setCityName("深圳");
        dto.setRecruitmentStatus("招聘中");
        Page<Enterprise> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);

        when(enterpriseMapper.selectPage(any(), enterpriseWrapperCaptor.capture())).thenReturn(page);

        service.page(dto);

        @SuppressWarnings("unchecked")
        LambdaQueryWrapper<Enterprise> wrapper = (LambdaQueryWrapper<Enterprise>) enterpriseWrapperCaptor.getValue();
        assertThat(wrapper.getCustomSqlSegment()).contains(
                "is_deleted =",
                "enterprise_name LIKE",
                "enterprise_nature =",
                "enterprise_type =",
                "city_name =",
                "recruitment_status =",
                "ORDER BY id ASC");
        assertThat(wrapper.getParamNameValuePairs().values())
                .contains(false, "%华为%", "民企", "科技", "深圳", "招聘中");
    }

    @Test
    void positions_enterpriseMissing_throws404() {
        when(enterpriseMapper.selectById(99L)).thenReturn(null);

        BusinessException exception = catchThrowableOfType(
                () -> service.positions(99L), BusinessException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(exception).hasMessageContaining("企业不存在");
        verify(enterprisePositionMapper, never()).selectList(any());
    }

    @Test
    void positions_enterpriseDeleted_throws404() {
        Enterprise enterprise = Enterprise.builder().id(1L).isDeleted(true).build();
        when(enterpriseMapper.selectById(1L)).thenReturn(enterprise);

        BusinessException exception = catchThrowableOfType(
                () -> service.positions(1L), BusinessException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(exception).hasMessageContaining("企业不存在");
        verify(enterprisePositionMapper, never()).selectList(any());
    }

    @Test
    void positions_returnsConvertedVOs() {
        Enterprise enterprise = Enterprise.builder().id(1L).isDeleted(false).build();
        OffsetDateTime deadline = OffsetDateTime.parse("2026-07-01T00:00:00+08:00");
        EnterprisePosition position = EnterprisePosition.builder()
                .id(11L)
                .enterpriseId(1L)
                .positionName("后端开发")
                .recruitmentType("校招")
                .positionRequirement("熟悉 Java")
                .positionTags(List.of("Java", "Spring"))
                .province("广东")
                .city("深圳")
                .workLocation("南山区")
                .educationRequirement("本科")
                .majorRequirement("计算机类")
                .workExperience("不限")
                .salaryMin(15)
                .salaryMax(25)
                .applyLink("https://example.com/apply")
                .deadline(deadline)
                .positionStatus("招聘中")
                .isDeleted(false)
                .build();
        when(enterpriseMapper.selectById(1L)).thenReturn(enterprise);
        when(enterprisePositionMapper.selectList(positionWrapperCaptor.capture())).thenReturn(List.of(position));

        List<EnterprisePositionVO> result = service.positions(1L);

        assertThat(positionWrapperCaptor.getValue().getCustomSqlSegment()).contains(
                "enterprise_id",
                "is_deleted",
                "ORDER BY id ASC");
        assertThat(positionWrapperCaptor.getValue().getParamNameValuePairs().values()).contains(1L, false);
        assertThat(result).hasSize(1);
        EnterprisePositionVO vo = result.get(0);
        assertThat(vo.getPositionName()).isEqualTo("后端开发");
        assertThat(vo.getRecruitmentType()).isEqualTo("校招");
        assertThat(vo.getPositionRequirement()).isEqualTo("熟悉 Java");
        assertThat(vo.getPositionTags()).containsExactly("Java", "Spring");
        assertThat(vo.getProvince()).isEqualTo("广东");
        assertThat(vo.getCity()).isEqualTo("深圳");
        assertThat(vo.getWorkLocation()).isEqualTo("南山区");
        assertThat(vo.getEducationRequirement()).isEqualTo("本科");
        assertThat(vo.getMajorRequirement()).isEqualTo("计算机类");
        assertThat(vo.getWorkExperience()).isEqualTo("不限");
        assertThat(vo.getSalaryMin()).isEqualTo(15);
        assertThat(vo.getSalaryMax()).isEqualTo(25);
        assertThat(vo.getApplyLink()).isEqualTo("https://example.com/apply");
        assertThat(vo.getDeadline()).isEqualTo(deadline);
        assertThat(vo.getPositionStatus()).isEqualTo("招聘中");
    }

    @Test
    void industriesByEnterpriseIds_nullIds_throwsBadRequest() {
        BusinessException exception = catchThrowableOfType(
                () -> service.industriesByEnterpriseIds(null), BusinessException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(exception).hasMessageContaining("企业ID列表不能为空");
        verify(enterpriseIndustryMapper, never()).selectList(any());
    }

    @Test
    void industriesByEnterpriseIds_emptyIds_throwsBadRequest() {
        BusinessException exception = catchThrowableOfType(
                () -> service.industriesByEnterpriseIds(List.of()), BusinessException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(exception).hasMessageContaining("企业ID列表不能为空");
        verify(enterpriseIndustryMapper, never()).selectList(any());
    }

    @Test
    void industriesByEnterpriseIds_onlyNullIds_throwsBadRequest() {
        BusinessException exception = catchThrowableOfType(
                () -> service.industriesByEnterpriseIds(Arrays.asList((Long) null)), BusinessException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(exception).hasMessageContaining("企业ID列表不能为空");
        verify(enterpriseIndustryMapper, never()).selectList(any());
    }

    @Test
    void industriesByEnterpriseIds_groupsByRequestedEnterpriseIdsAndReturnsEmptyGroups() {
        EnterpriseIndustry relation1 = EnterpriseIndustry.builder()
                .id(101L)
                .enterpriseId(1L)
                .industryId(10L)
                .industryName("人工智能")
                .sortOrder((short) 1)
                .build();
        EnterpriseIndustry relation2 = EnterpriseIndustry.builder()
                .id(102L)
                .enterpriseId(1L)
                .industryId(11L)
                .industryName("智能制造")
                .sortOrder((short) 2)
                .build();
        when(enterpriseIndustryMapper.selectList(industryWrapperCaptor.capture()))
                .thenReturn(List.of(relation1, relation2));

        List<EnterpriseIndustryGroupVO> result = service.industriesByEnterpriseIds(List.of(1L, 2L, 1L));

        assertThat(industryWrapperCaptor.getValue().getCustomSqlSegment()).contains(
                "enterprise_id",
                "IN",
                "ORDER BY enterprise_id ASC,sort_order ASC,id ASC");
        assertThat(industryWrapperCaptor.getValue().getParamNameValuePairs().values())
                .filteredOn(value -> value instanceof Long)
                .containsExactlyInAnyOrder(1L, 2L);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEnterpriseId()).isEqualTo(1L);
        assertThat(result.get(0).getIndustries()).hasSize(2);
        assertThat(result.get(0).getIndustries().get(0).getIndustryId()).isEqualTo(10L);
        assertThat(result.get(0).getIndustries().get(0).getIndustryName()).isEqualTo("人工智能");
        assertThat(result.get(0).getIndustries().get(1).getIndustryId()).isEqualTo(11L);
        assertThat(result.get(0).getIndustries().get(1).getIndustryName()).isEqualTo("智能制造");
        assertThat(result.get(1).getEnterpriseId()).isEqualTo(2L);
        assertThat(result.get(1).getIndustries()).isEmpty();
    }
}
