package com.haifeng.common.entity.algorithm;

import com.baomidou.mybatisplus.annotation.*;
import com.haifeng.common.config.JsonbTypeHandler;
import com.haifeng.common.config.StringListTypeHandler;
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
@TableName(value = "t_admission_major_score", autoResultMap = true)
public class AdmissionMajorScore {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer groupId;

    private Long majorId;

    private String majorCode;

    private String majorName;

    private String educationLevel;

    private String duration;

    private String tuition;

    private String description;

    @TableField(typeHandler = JsonbTypeHandler.class)
    private Object history;

    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> constraints;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
