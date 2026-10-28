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
 * C 端城市排行榜 VO（高校数量 / 重点院校 / GDP / 人口 共用一个结构）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityRankVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 名次，1 起始 */
    private Integer rank;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String cityName;

    private String province;

    /** 指标值：高校数/重点院校为个数，GDP 为亿元，人口为万人 */
    private BigDecimal value;
}
