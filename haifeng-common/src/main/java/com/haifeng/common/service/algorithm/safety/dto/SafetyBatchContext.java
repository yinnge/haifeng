package com.haifeng.common.service.algorithm.safety.dto;

import com.haifeng.common.entity.algorithm.GaokaoConfig;
import com.haifeng.common.entity.algorithm.ProvinceConfig;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 安全系数批量计算上下文
 * 封装一次批量计算（如专业组列表页）所需的全部预取数据，
 * 由 service 层每次请求查一次后复用，避免逐专业重复查询数据库
 */
@Data
@Builder
public class SafetyBatchContext {

    /** 用户同分密度（一次请求查一次） */
    private BigDecimal density;

    /** 省配置（一次） */
    private ProvinceConfig provinceConfig;

    /** 约束权重配置（GaokaoConfig 单例，一次） */
    private GaokaoConfig gaokaoConfig;

    /** 用户约束 code -> HARD/SOFT（一次） */
    private Map<String, String> severityMap;
}
