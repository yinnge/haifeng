package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * C 端院校统计概览 VO（列表页右侧数据卡）
 * 985/211/双一流：按 tags 数组包含匹配独立计数（重叠不互斥，与原型一致）；
 * 本科：education_level IN ('本科', '本专兼招')；
 * 全部仅统计 status=1，不受列表筛选影响
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 985 院校数量 */
    private Long count985;

    /** 211 院校数量 */
    private Long count211;

    /** 双一流院校数量 */
    private Long countDoubleFirstClass;

    /** 本科院校数量（含本专兼招） */
    private Long countUndergraduate;
}
