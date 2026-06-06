package com.haifeng.app.vo.city;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * C 端城市详情 VO（任务 1 接口 2，来自 t_city_detail）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ===== 来自 t_city_detail =====
    private String cityName;
    private BigDecimal area;
    private String subtitle;
    private String cityLevel;
    private String adminCode;
    private BigDecimal perCapitaGdp;
    private BigDecimal urbanizationRate;
    private BigDecimal ruralPopRatio;
    private BigDecimal agingRate;
    private BigDecimal migrantPopRatio;
    private BigDecimal gdpGrowthRate;
    private Integer fortune500Count;
    private Map<String, Object> industryStructure;
    private String industryDescription;
    private List<String> mainIndustries;
    private List<String> emergingIndustries;
    private Map<String, Object> futurePlan;
    private Map<String, Object> highEducation;
    private Map<String, Object> basicEducation;
    private Map<String, Object> enterpriseStats;
    private Map<String, Object> housingPriceLevel;
    private Map<String, Object> rentalCost;
    private Map<String, Object> housingPolicy;
    private Map<String, Object> consumption;
    private Map<String, Object> employment;
    private Map<String, Object> transportation;
    private Map<String, Object> medical;
    private Map<String, Object> culture;
}
