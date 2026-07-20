package com.haifeng.common.entity.algorithm;

import com.baomidou.mybatisplus.annotation.*;
import com.haifeng.common.config.StringListTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_admission_group", autoResultMap = true)
public class AdmissionGroup {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Long universityId;

    private String universityName;

    private String cityName;

    private Short year;

    private String province;

    private String batch;

    private String enrollmentCode;

    private String groupCode;

    private String groupName;

    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> subjects;

    private String requirementType;

    private String description;

    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> constraints;

    private Integer majorCount;

    private Integer categoryCount;

    private Integer admissionCount;

    private Integer minScore;

    private Integer minRank;

    private BigDecimal avgScore;

    private Integer avgRank;

    private Integer maxScore;

    private Integer maxRank;

    private Boolean isDeleted;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
