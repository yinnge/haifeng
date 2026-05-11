package com.haifeng.common.entity.company;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_enterprise_industry", autoResultMap = true)
public class EnterpriseIndustry {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long enterpriseId;

    private String enterpriseName;

    private Long industryId;

    private String industryName;

    private Boolean isPrimary;

    private Short sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
