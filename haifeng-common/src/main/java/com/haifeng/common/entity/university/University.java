package com.haifeng.common.entity.university;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
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
@TableName(value = "t_universities", autoResultMap = true)
public class University {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String nameEn;

    private String provinceName;

    private String cityName;

    private String region;

    private String category;

    private Integer majorCount;

    private String educationLevel;

    private String nature;

    private BigDecimal recommendationRate;

    private Integer recommendationYear;

    private Boolean hasDoctorate;

    private Boolean hasMaster;

    private String department;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    private String famousUnion;

    private String imageUrl;

    private String introduction;

    private Integer sortOrder;

    private Short status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
