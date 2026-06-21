package com.haifeng.common.entity.employment.jobIndex;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_job_index", autoResultMap = true)
public class JobIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String sourceType;

    private Long sourceId;

    private String categoryLabel;

    private String positionName;

    private String organizationName;

    private String organizationLogo;

    private String province;

    private String city;

    private String educationRequirement;

    private Integer recruitmentCount;

    private String recruitmentType;

    private Integer salaryMin;

    private Integer salaryMax;

    private String salaryText;

    private OffsetDateTime publishDate;

    private OffsetDateTime regDeadline;

    private Boolean isHot;

    private Integer viewCount;

    private Integer applyCount;

    private String positionStatus;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
