package com.haifeng.app.vo.major;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/** C 端专业简要信息 VO（专业组页点击专业名弹抽屉，模式同 CityBriefVO） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MajorBriefVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String majorCode;

    private String majorName;

    private String disciplineName;

    private String majorType;

    private String majorCategory;

    private String parentCategory;

    private String majorTags;

    private String degreeAwarded;

    private String studyDuration;

    private BigDecimal employmentRate;

    private Integer salaryMin;

    private Integer salaryMax;

    private String description;
}
