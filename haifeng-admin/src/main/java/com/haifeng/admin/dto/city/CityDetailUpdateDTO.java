package com.haifeng.admin.dto.city;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class CityDetailUpdateDTO {

    /**
     * 城市面积（平方公里）
     */
    private BigDecimal area;

    /**
     * 城市副标题/描述
     */
    private String subtitle;

    /**
     * 城市等级
     */
    private String cityLevel;

    /**
     * 行政区划代码
     */
    private String adminCode;

    /**
     * 人均GDP（万元）
     */
    private BigDecimal perCapitaGdp;

    /**
     * 城镇化率（%）
     */
    private BigDecimal urbanizationRate;

    /**
     * 农村人口占比（%）
     */
    private BigDecimal ruralPopRatio;

    /**
     * 老龄化率（%）
     */
    private BigDecimal agingRate;

    /**
     * 外来人口占比（%）
     */
    private BigDecimal migrantPopRatio;

    /**
     * GDP增长率（%）
     */
    private BigDecimal gdpGrowthRate;

    /**
     * 世界500强企业数量
     */
    private Integer fortune500Count;

    /**
     * 产业结构（第一、二、三产业占比）
     */
    private Map<String, Object> industryStructure;

    /**
     * 产业描述
     */
    private String industryDescription;

    /**
     * 主导产业列表
     */
    private List<String> mainIndustries;

    /**
     * 新兴产业列表
     */
    private List<String> emergingIndustries;

    /**
     * 未来规划
     */
    private Map<String, Object> futurePlan;

    /**
     * 高等教育情况
     */
    private Map<String, Object> highEducation;

    /**
     * 基础教育情况
     */
    private Map<String, Object> basicEducation;

    /**
     * 企业统计
     */
    private Map<String, Object> enterpriseStats;

    /**
     * 房价水平
     */
    private Map<String, Object> housingPriceLevel;

    /**
     * 租房成本
     */
    private Map<String, Object> rentalCost;

    /**
     * 住房政策
     */
    private Map<String, Object> housingPolicy;

    /**
     * 消费水平
     */
    private Map<String, Object> consumption;

    /**
     * 就业情况
     */
    private Map<String, Object> employment;

    /**
     * 交通情况
     */
    private Map<String, Object> transportation;

    /**
     * 医疗资源
     */
    private Map<String, Object> medical;

    /**
     * 文化娱乐
     */
    private Map<String, Object> culture;

    /**
     * 删除状态
     */
    private Boolean isDeleted;
}
