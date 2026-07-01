package com.haifeng.app.vo.algorithm.pdf;

import com.haifeng.app.vo.major.MajorDetailVO;
import com.haifeng.common.entity.algorithm.wish.WishMajorSnapshot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 专业维度 PDF 聚合结果
 * <p>每个可导出专业对应一条；detail 由 {@link com.haifeng.app.service.major.MajorService#detail} 提供。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfMajorVO {

    /** 专业ID */
    private Long majorId;

    /** 专业组快照ID */
    private Integer groupSnapshotId;

    /** 院校ID */
    private Long universityId;

    /** 专业组名称 */
    private String groupName;

    /** 安全系数 */
    private BigDecimal safetyLevel;

    /** 档位简称（搏/冲/稳/保/垫） */
    private String levelShort;

    /** 历史录取分快照（JSONB） */
    private List<WishMajorSnapshot.HistoryScore> historyScores;

    /** 专业详情（TODO：aggregateMajors 中调用 MajorService.detail 填充） */
    private MajorDetailVO detail;
}
