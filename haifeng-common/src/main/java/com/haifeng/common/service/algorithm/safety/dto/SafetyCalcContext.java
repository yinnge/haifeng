package com.haifeng.common.service.algorithm.safety.dto;

import com.haifeng.common.entity.algorithm.MajorHistoryItem;
import com.haifeng.common.entity.algorithm.ProvinceConfig;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 安全系数计算上下文
 * 封装单次计算所需的全部预聚合数据，由 service 层一次性预查询后传入，
 * 避免在循环中对每条专业明细重复查询数据库
 */
@Data
@Builder
public class SafetyCalcContext {

    /** 用户同分密度（同省份同年同分类型下查一次） */
    private BigDecimal density;

    /** 用户省份配置（同省份查一次） */
    private ProvinceConfig provinceConfig;

    /** 用户省份的改革年份（null = 未改革省份） */
    private Short reformYear;

    /** 约束 severity 映射（key: 约束code, value: HARD/SOFT），service 层预聚合 */
    private Map<String, String> severityMap;

    /** 当前专业明细的历史录取数据 */
    private List<MajorHistoryItem> majorHistory;
}
