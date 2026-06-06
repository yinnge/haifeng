package com.haifeng.app.service.industry;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisMapperBuilderAssistant;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.haifeng.app.service.impl.industry.IndustryServiceImpl;
import com.haifeng.app.vo.company.IndustryEnterpriseGroupVO;
import com.haifeng.common.entity.company.EnterpriseIndustry;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.company.EnterpriseIndustryMapper;
import com.haifeng.common.mapper.industry.IndustryDetailMapper;
import com.haifeng.common.mapper.industry.IndustryMapper;
import com.haifeng.common.response.ResultCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndustryServiceImplTest {

    @Mock private IndustryMapper industryMapper;
    @Mock private IndustryDetailMapper industryDetailMapper;
    @Mock private EnterpriseIndustryMapper enterpriseIndustryMapper;

    @InjectMocks private IndustryServiceImpl service;

    @Captor private ArgumentCaptor<LambdaQueryWrapper<EnterpriseIndustry>> enterpriseIndustryWrapperCaptor;

    @BeforeAll
    static void initTableInfoCache() {
        // LambdaQueryWrapper.getCustomSqlSegment() requires MyBatis-Plus table metadata.
        // In unit tests there is no Spring context to trigger that initialization, so
        // we prime the per-entity cache manually.
        MybatisConfiguration configuration = new MybatisConfiguration();
        MybatisMapperBuilderAssistant assistant = new MybatisMapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, EnterpriseIndustry.class);
    }

    @Test
    void enterprisesByIndustryIds_nullIds_throwsBadRequest() {
        BusinessException exception = catchThrowableOfType(
                () -> service.enterprisesByIndustryIds(null), BusinessException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(exception).hasMessageContaining("行业ID列表不能为空");
        verify(enterpriseIndustryMapper, never()).selectList(any());
    }

    @Test
    void enterprisesByIndustryIds_emptyIds_throwsBadRequest() {
        BusinessException exception = catchThrowableOfType(
                () -> service.enterprisesByIndustryIds(List.of()), BusinessException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(exception).hasMessageContaining("行业ID列表不能为空");
        verify(enterpriseIndustryMapper, never()).selectList(any());
    }

    @Test
    void enterprisesByIndustryIds_onlyNullIds_throwsBadRequest() {
        BusinessException exception = catchThrowableOfType(
                () -> service.enterprisesByIndustryIds(Arrays.asList((Long) null)), BusinessException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(exception).hasMessageContaining("行业ID列表不能为空");
        verify(enterpriseIndustryMapper, never()).selectList(any());
    }

    @Test
    void enterprisesByIndustryIds_groupsByRequestedIndustryIdsAndReturnsEmptyGroups() {
        EnterpriseIndustry relation1 = EnterpriseIndustry.builder()
                .id(201L)
                .enterpriseId(1L)
                .enterpriseName("华为技术有限公司")
                .industryId(10L)
                .sortOrder((short) 1)
                .build();
        EnterpriseIndustry relation2 = EnterpriseIndustry.builder()
                .id(202L)
                .enterpriseId(2L)
                .enterpriseName("腾讯科技")
                .industryId(10L)
                .sortOrder((short) 2)
                .build();
        when(enterpriseIndustryMapper.selectList(enterpriseIndustryWrapperCaptor.capture()))
                .thenReturn(List.of(relation1, relation2));

        List<IndustryEnterpriseGroupVO> result = service.enterprisesByIndustryIds(List.of(10L, 11L, 10L));

        assertThat(enterpriseIndustryWrapperCaptor.getValue().getCustomSqlSegment()).contains(
                "industry_id",
                "IN",
                "ORDER BY industry_id ASC,sort_order ASC,id ASC");
        assertThat(enterpriseIndustryWrapperCaptor.getValue().getParamNameValuePairs().values())
                .filteredOn(value -> value instanceof Long)
                .containsExactlyInAnyOrder(10L, 11L);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getIndustryId()).isEqualTo(10L);
        assertThat(result.get(0).getEnterprises()).hasSize(2);
        assertThat(result.get(0).getEnterprises().get(0).getEnterpriseId()).isEqualTo(1L);
        assertThat(result.get(0).getEnterprises().get(0).getEnterpriseName()).isEqualTo("华为技术有限公司");
        assertThat(result.get(0).getEnterprises().get(1).getEnterpriseId()).isEqualTo(2L);
        assertThat(result.get(0).getEnterprises().get(1).getEnterpriseName()).isEqualTo("腾讯科技");
        assertThat(result.get(1).getIndustryId()).isEqualTo(11L);
        assertThat(result.get(1).getEnterprises()).isEmpty();
    }
}
