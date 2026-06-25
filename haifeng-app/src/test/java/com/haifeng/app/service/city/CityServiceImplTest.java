package com.haifeng.app.service.city;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisMapperBuilderAssistant;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.haifeng.app.service.impl.city.CityServiceImpl;
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
    void findIdByName_returnsCityIdByExactNameAndNotDeleted() {
        when(cityMapper.selectOne(cityWrapperCaptor.capture()))
                .thenReturn(City.builder().id(1001L).cityName("北京").isDeleted(false).build());

        Long result = service.findIdByName("北京");

        assertThat(result).isEqualTo(1001L);
        assertThat(cityWrapperCaptor.getValue().getCustomSqlSegment()).contains(
                "city_name",
                "is_deleted");
        assertThat(cityWrapperCaptor.getValue().getParamNameValuePairs().values())
                .contains("北京", false);
    }

    @Test
    void findIdByName_cityNotFound_throwsNotFound() {
        when(cityMapper.selectOne(any())).thenReturn(null);

        BusinessException exception = catchThrowableOfType(
                () -> service.findIdByName("不存在城市"), BusinessException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(exception).hasMessageContaining("城市不存在");
    }
}
