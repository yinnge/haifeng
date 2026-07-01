package com.haifeng.app.vo.algorithm.pdf;

import com.haifeng.app.vo.algorithm.wish.WishExportMajorVO;
import com.haifeng.app.vo.university.UniversityDetailVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 大学维度 PDF 聚合结果
 * <p>每个非跳过专业组对应一条；detail 由 {@link com.haifeng.app.service.university.UniversityService#detail} 提供。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfUniversityVO {

    /** 专业组快照ID */
    private Integer groupSnapshotId;

    /** 院校ID */
    private Long universityId;

    /** 城市名 */
    private String cityName;

    /** 专业组名称 */
    private String groupName;

    /** 院校详情（TODO：aggregateUniversities 中调用 UniversityService.detail 填充） */
    private UniversityDetailVO detail;

    /** 该组下可导出专业 */
    private List<WishExportMajorVO> majors;
}
