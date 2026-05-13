package com.haifeng.app.vo.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 改革模式响应 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReformModelVO {

    /**
     * 改革模式（3+3/3+1+2/传统文理）
     */
    private String reformModel;

    /**
     * 可选科目
     * - 3+1+2: {"first": ["物理", "历史"], "second": ["化学", "生物", "政治", "地理"]}
     * - 3+3: {"first": ["物理", "化学", "生物", "政治", "历史", "地理"]}
     * - 传统文理: {"first": ["文科", "理科"]}
     */
    private Map<String, List<String>> subjects;
}
