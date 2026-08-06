package com.haifeng.admin.dto.company;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnterpriseIndustryAddDTO {
    @NotNull(message = "请选择企业")
    private Long enterpriseId;

    @NotNull(message = "请选择行业")
    private Long industryId;

    private Boolean isPrimary;

    @Min(value = 0, message = "排序值最小为0")
    @Max(value = 999, message = "排序值最大为999")
    private Short sortOrder;
}
