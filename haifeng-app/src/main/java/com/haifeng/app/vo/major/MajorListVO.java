package com.haifeng.app.vo.major;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/** C 端专业列表 VO（spec 任务1接口1 + 任务1接口4 复用） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MajorListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String majorCode;
    private String majorName;
    private String disciplineName;
    private String majorCategory;
    private String parentCategory;
    private String majorTags;
    private String degreeAwarded;
    private BigDecimal employmentRate;
    private Integer salaryMin;
    private Integer salaryMax;
    private String description;
}
