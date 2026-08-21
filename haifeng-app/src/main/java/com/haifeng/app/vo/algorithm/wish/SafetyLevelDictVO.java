package com.haifeng.app.vo.algorithm.wish;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 安全等级档位字典 VO（含推荐上限）
 *
 * <p>合并 t_safety_level_dict 字典字段与 system_settings 的推荐数量上限，
 * 供前端志愿填报页顶部展示「冲稳保垫」图例 + 已选/上限统计。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SafetyLevelDictVO {

    /** 档位编码（REACH_HIGH / REACH / MATCH / SAFE / FLOOR） */
    private String code;

    /** 档位名称（大胆冲刺 / 可以冲击 / 较为稳妥 / 比较安全 / 高度保底） */
    private String name;

    /** 档位简写（搏 / 冲 / 稳 / 保 / 垫） */
    private String nameShort;

    /** 安全系数下限（0.00~1.00） */
    private BigDecimal minCoefficient;

    /** 安全系数上限（0.00~1.00） */
    private BigDecimal maxCoefficient;

    /** 档位颜色（#FF4D4F 等） */
    private String color;

    /** 档位说明文案 */
    private String description;

    /** 该档推荐志愿数量上限（来自 system_settings） */
    private Integer limit;
}
