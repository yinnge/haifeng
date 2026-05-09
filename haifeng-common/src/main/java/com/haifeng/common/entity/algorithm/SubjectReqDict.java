package com.haifeng.common.entity.algorithm;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_subject_req_dict", autoResultMap = true)
public class SubjectReqDict {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String code;

    private String displayName;

    private Short requirementLevel;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> subjects;

    private String requirementType;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
