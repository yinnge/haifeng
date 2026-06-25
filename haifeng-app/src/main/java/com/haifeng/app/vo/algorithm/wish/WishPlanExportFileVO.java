package com.haifeng.app.vo.algorithm.wish;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下载文件VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishPlanExportFileVO {

    /**
     * 下载链接
     */
    private String downloadUrl;

    /**
     * 文件名
     */
    private String fileName;
}
