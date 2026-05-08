package com.haifeng.admin.vo.city;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CityListVO {

    private Long id;

    /**
     * 城市名称
     */
    private String cityName;

    /**
     * 省份
     */
    private String province;

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
     * 删除状态
     */
    private Boolean isDeleted;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
