package com.haifeng.app.vo.algorithm.wish;

import com.haifeng.app.vo.algorithm.admission.YearScoreVO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class WishPlanMajorVO {
    private Integer id;
    private Integer groupSnapshotId;
    private Long majorId;
    private Integer majorSortOrder;
    private String majorCode;
    private String majorName;
    private String duration;
    private String tuition;
    private String description;
    private String educationLevel;
    private List<String> constraints;
    private Integer admissionCount;
    private BigDecimal safetyLevel;
    private String levelShort;
    private List<YearScoreVO> historyScores;
    private Boolean isExported;
}
