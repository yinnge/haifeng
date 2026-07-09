package com.haifeng.app.vo.algorithm.wish;

import com.haifeng.app.vo.algorithm.pdf.MajorEnrichmentVO;
import com.haifeng.common.entity.algorithm.wish.WishMajorSnapshot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 可导出专业（PDF导出用）
 * <p>携带专业ID及快照中的安全系数、档位与历史录取分，供后续大模型分析与详情聚合使用。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishExportMajorVO {

    /** 专业ID（t_wish_major_snapshot.major_id） */
    private Long majorId;

    /** 专业名称 */
    private String majorName;

    /** 安全系数 */
    private BigDecimal safetyLevel;

    /** 档位简称（搏/冲/稳/保/垫） */
    private String levelShort;

    /** 历史录取分快照（JSONB） */
    private List<WishMajorSnapshot.HistoryScore> historyScores;

    /** 专业增强数据（就业率/薪资/就业前景等，供 AI 分析与 PDF 展示） */
    private MajorEnrichmentVO majorEnrichment;
}
