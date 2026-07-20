package com.haifeng.app.vo.algorithm.wish;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导出进度VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishPlanExportProgressVO {

    /**
     * 总专业数
     */
    private Integer totalMajors;

    /**
     * 已导出专业数
     */
    private Integer exportedMajors;

    /**
     * 进度百分比
     */
    private Integer percentage;

    /**
     * 状态：processing/completed/error
     */
    private String status;

    /**
     * 状态消息（可选）
     */
    private String message;
}
