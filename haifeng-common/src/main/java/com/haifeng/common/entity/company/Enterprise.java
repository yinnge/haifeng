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
@TableName(value = "t_enterprise", autoResultMap = true)
public class Enterprise {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String cityName;

    private String enterpriseName;

    private String enterpriseNature;

    private String enterpriseType;

    private String logoUrl;

    private String officialWebsite;

    private String region;

    private String enterpriseScale;

    private String mainBusiness;

    private String enterpriseIntro;

    private String recruitmentStatus;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
