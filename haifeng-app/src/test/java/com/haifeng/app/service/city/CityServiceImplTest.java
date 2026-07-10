package com.haifeng.app.service.city;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisMapperBuilderAssistant;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.haifeng.app.service.impl.city.CityServiceImpl;
import com.haifeng.app.vo.city.CityBriefVO;
import com.haifeng.common.entity.city.City;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.city.CityDetailMapper;
import com.haifeng.common.mapper.city.CityMapper;
import com.haifeng.common.response.ResultCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CityServiceImplTest {

    @Mock private CityMapper cityMapper;
    @Mock private CityDetailMapper cityDetailMapper;

    @InjectMocks private CityServiceImpl service;

    @Captor private ArgumentCaptor<LambdaQueryWrapper<City>> cityWrapperCaptor;

    @BeforeAll
    static void initTableInfoCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MybatisMapperBuilderAssistant assistant = new MybatisMapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, City.class);
    }

    @Test
    void getBriefByName_returnsBriefByExactNameAndNotDeleted() {
        when(cityMapper.selectOne(cityWrapperCaptor.capture()))
                .thenReturn(City.builder().id(1001L).cityName("北京").province("北京").region("华北")
                        .cityIntro("首都").collegeCount(100).isDeleted(false).build());

        CityBriefVO result = service.getBriefByName("北京");

        assertThat(result.getCityName()).isEqualTo("北京");
        assertThat(result.getProvince()).isEqualTo("北京");
        assertThat(cityWrapperCaptor.getValue().getCustomSqlSegment()).contains(
                "city_name",
                "is_deleted");
        assertThat(cityWrapperCaptor.getValue().getParamNameValuePairs().values())
                .contains("北京", false);
    }

    @Test
    void getBriefByName_cityNotFound_throwsNotFound() {
        when(cityMapper.selectOne(any())).thenReturn(null);

        BusinessException exception = catchThrowableOfType(
                () -> service.getBriefByName("不存在城市"), BusinessException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(exception).hasMessageContaining("城市不存在");
    }
}
