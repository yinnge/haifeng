package com.haifeng.common.entity.algorithm;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 批次分数线实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_batch_score_line")
public class BatchScoreLine {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 省份名称
     */
    private String province;

    /**
     * 年份
     */
    private Short year;

    /**
     * 科目类型（物理类/历史类/理科/文科）
     */
    private String subjectType;

    /**
     * 批次（本科批/专科批/本科一批/本科二批等）
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

    /**
     * 备注
     */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;

    @TableLogic
    @TableField("is_deleted")
    private Boolean isDeleted;

    @Version
    private Integer version;
}
