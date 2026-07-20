package com.haifeng.common.entity.city;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.haifeng.common.config.StringListTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_city_detail", autoResultMap = true)
public class CityDetail {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long cityId;

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

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> industryStructure;

    private String industryDescription;

    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> mainIndustries;

    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> emergingIndustries;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> futurePlan;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> highEducation;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> basicEducation;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> enterpriseStats;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> housingPriceLevel;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> rentalCost;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> housingPolicy;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> consumption;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> employment;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> transportation;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> medical;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> culture;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
