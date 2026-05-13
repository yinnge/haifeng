package com.haifeng.app.vo.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批次分数线 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchLineVO {

    /**
     * 批次名称
     */
    private String batch;

    /**
     * 分数线
     */
    private Integer scoreLine;

    /**
     * 位次线
     */
    private Integer rankLine;
}
