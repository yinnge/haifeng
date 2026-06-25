package com.haifeng.common.entity.algorithm.wish;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 志愿方案主表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_wish_plan")
public class WishPlan {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Long memberId;

    private String planName;

    private Short planYear;

    private String planProvince;

    private String reformModel;

    private String planBatch;

    private Integer userScore;

    private Integer userRank;

    private Integer boLimit;

    private Integer chongLimit;

    private Integer wenLimit;

    private Integer baoLimit;

    private Integer dieLimit;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
