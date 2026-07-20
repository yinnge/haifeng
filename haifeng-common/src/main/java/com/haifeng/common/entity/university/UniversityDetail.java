package com.haifeng.common.entity.university;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.haifeng.common.config.StringListTypeHandler;
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
@TableName(value = "t_universities_detail", autoResultMap = true)
public class UniversityDetail {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long universityId;

    private String address;

    private String admissionPhone;

    private String website;

    private Integer historyGroupScore;

    private Integer scienceGroupScore;

    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> carouselImages;

    private String introduction;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Integer> rankings;

    private String abroadRate;

    private String genderRatio;

    private Integer sortOrder;

    private Short status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
