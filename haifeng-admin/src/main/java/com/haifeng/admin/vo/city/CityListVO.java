package com.haifeng.admin.vo.city;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class CityListVO {

    @JsonSerialize(using = ToStringSerializer.class)
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
     * 城市背景图URL
     */
    private String coverImageUrl;

    /**
     * 删除状态
     */
    private Boolean isDeleted;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 排序权重（越大越靠前，C 端热点优先展示）
     */
    private Integer sortOrder;
}
