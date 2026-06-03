package com.haifeng.app.vo.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 专业组约束校验结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckGroupResultVO {

    /**
     * 是否通过校验（无HARD约束冲突）
     */
    private Boolean isPass;

    /**
     * 硬限制冲突列表（不可报考）
     */
    private List<ConstraintConflictVO> hardConflicts;

    /**
     * 软提示冲突列表（建议谨慎）
     */
    private List<ConstraintConflictVO> softConflicts;
}
