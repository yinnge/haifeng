package com.haifeng.common.entity.algorithm.wish;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 志愿方案-专业明细快照表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_wish_major_snapshot", autoResultMap = true)
public class WishMajorSnapshot {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer planId;

    private Integer groupSnapshotId;

    private Long majorId;

    private Integer majorSortOrder;

    private Boolean isExported;

    private String majorCode;

    private String majorName;

    private String duration;

    private BigDecimal tuition;

    private String description;

    private Integer admissionCount;

    private BigDecimal safetyLevel;

    private String levelShort;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<HistoryScore> historyScores;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    /**
     * 历史录取分快照 (JSONB 反序列化目标)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryScore {
        private Integer year;
        private Integer minScore;
        private Integer minRank;
        private BigDecimal avgScore;
        private Integer avgRank;
        private Integer maxScore;
        private Integer maxRank;
        private Integer admissionCount;
    }
}
