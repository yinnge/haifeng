package com.haifeng.app.vo.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批次分数线列表响应 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchLineListVO {

    /**
     * 数据来源年份
     */
    private Integer dataYear;

    /**
     * 是否为当年数据
     */
    private Boolean isCurrentYear;

    /**
     * 批次列表
     */
    private List<BatchLineVO> batches;
}
