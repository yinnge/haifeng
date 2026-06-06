package com.haifeng.app.vo.city;

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

    private Long id;
    private String cityName;
    private String province;
    private String region;
    private String cityIntro;
    private Integer collegeCount;
    private Integer keyCollegeCount;
    private BigDecimal residentPopulation;
    private BigDecimal gdp;
}
