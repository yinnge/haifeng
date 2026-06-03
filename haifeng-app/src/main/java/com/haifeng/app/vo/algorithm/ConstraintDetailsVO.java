package com.haifeng.app.vo.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 约束详情列表VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConstraintDetailsVO {

    /**
     * 约束详情列表
     */
    private List<ConstraintDetailVO> constraints;
}
