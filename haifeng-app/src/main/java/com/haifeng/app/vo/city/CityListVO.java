package com.haifeng.app.vo.city;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * C 端城市列表 VO（任务 1 接口 1）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String cityName;
    private String province;
    private String region;
    private String cityIntro;
    private Integer collegeCount;
    private Integer keyCollegeCount;
    private BigDecimal residentPopulation;
    private BigDecimal gdp;

    /** 城市背景图URL */
    private String coverImageUrl;

    /** 城市等级（直辖市/省会城市/地级市/县级市，来自 t_city_detail） */
    private String cityLevel;
}
