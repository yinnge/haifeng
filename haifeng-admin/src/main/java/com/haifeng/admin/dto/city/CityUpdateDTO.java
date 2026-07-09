package com.haifeng.admin.dto.city;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CityUpdateDTO {

    @NotBlank(message = "城市名称不能为空")
    @Size(max = 50, message = "城市名称不能超过50个字符")
    private String cityName;

    @NotBlank(message = "省份不能为空")
    @Size(max = 50, message = "省份不能超过50个字符")
    private String province;

    @NotBlank(message = "所属地区不能为空")
    @Size(max = 50, message = "所属地区不能超过50个字符")
    private String region;

    /**
     * 城市简介
     */
    private String cityIntro;

    /**
     * 高校数量
     */
    private Integer collegeCount;

    /**
     * 重点高校数量
     */
    private Integer keyCollegeCount;

    /**
     * 常住人口（万人）
     */
    private BigDecimal residentPopulation;

    /**
     * GDP（亿元）
     */
    private BigDecimal gdp;
}
