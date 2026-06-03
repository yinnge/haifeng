package com.haifeng.app.vo.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 约束冲突信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConstraintConflictVO {

    /**
     * 约束代码
     */
    private String code;

    /**
     * 约束名称
     */
    private String name;

    /**
     * 约束描述/提示信息
     */
    private String description;
}
