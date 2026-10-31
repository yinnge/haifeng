package com.haifeng.app.dto.major;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class MajorListQueryDTO extends BasePageQueryDTO {

    @Size(max = 100, message = "专业名称最长100个字符")
    private String name;

    @Size(max = 20, message = "专业代码最长20个字符")
    private String code;

    @Size(max = 50, message = "专业类型最长50个字符")
    private String majorType;

    @Size(max = 50, message = "专业类别最长50个字符")
    private String majorCategory;

    /**
     * 排序字段：综合排序(comprehensive, 默认) / 就业率(employmentRate) / 薪资水平(salaryMax) / 热度(sortOrder)
     */
    @Pattern(regexp = "comprehensive|employmentRate|salaryMax|sortOrder",
            message = "sortBy 只支持 comprehensive、employmentRate、salaryMax、sortOrder")
    private String sortBy = "comprehensive";

    @Pattern(regexp = "asc|desc", message = "sortOrder 只支持 asc、desc")
    private String sortOrder = "desc";

    /** 就业率下限（百分比，例如 80 表示 80%） */
    private BigDecimal employmentRateMin;

    /** 就业率上限（百分比） */
    private BigDecimal employmentRateMax;

    /** 月薪下限（元/月） */
    private Integer salaryMin;

    /** 月薪上限（元/月） */
    private Integer salaryMax;
}
