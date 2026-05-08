package com.haifeng.common.entity.city;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_city", autoResultMap = true)
public class City {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String cityName;

    private String province;

    private String region;

    private String cityIntro;

    private Integer collegeCount;

    private Integer keyCollegeCount;

    private BigDecimal residentPopulation;

    private BigDecimal gdp;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
