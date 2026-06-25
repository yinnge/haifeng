package com.haifeng.app.vo.algorithm.wish;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 志愿方案默认数量限制 VO
 *
 * <p>对应 system_settings 表的 5 个默认推荐志愿数字段，
 * 用户进入志愿填报页时由后端返回作为每档可选数量上限。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishPlanLimitVO {

    /** 搏(大胆冲刺)档默认推荐志愿数 */
    private Integer reachHighCount;

    /** 冲(可以冲击)档默认推荐志愿数 */
    private Integer reachCount;

    /** 稳(较为稳妥)档默认推荐志愿数 */
    private Integer matchCount;

    /** 保(比较安全)档默认推荐志愿数 */
    private Integer safeCount;

    /** 垫(高度保底)档默认推荐志愿数 */
    private Integer floorCount;
}
