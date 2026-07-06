package com.haifeng.common.entity.algorithm.pdf;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * PDF 报告记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_pdf_report", autoResultMap = true)
public class PdfReport {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Long memberId;

    private Integer planId;

    /** 0=生成中, 1=成功, 2=失败 */
    private Short status;

    /** Map 阶段逐校 AI 简评 JSONB 数组 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String mapResults;

    /** Reduce 阶段全局研判 JSONB */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String reduceResult;

    /** 封面页数据快照 JSONB */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String planSnapshot;

    private String failReason;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
