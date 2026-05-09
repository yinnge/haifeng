package com.haifeng.common.entity.certificate;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_competition_detail", autoResultMap = true)
public class CompetitionDetail {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long competitionId;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> basicInfo;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> awards;

    private String background;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> purposes;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, String>> competitionRules;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> scoringCriteria;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> notices;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, String>> processGuide;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, String>> awardsDisplay;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
