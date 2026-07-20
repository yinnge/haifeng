package com.haifeng.app.vo.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 约束匹配结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConstraintMatchVO {

    /**
     * 触发的约束代码列表
     */
    private List<String> constraintCodes;
}
