package com.haifeng.admin.dto.user;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberUpgradeDTO {

    @NotBlank(message = "目标会员类型不能为空")
    @Pattern(regexp = "^(pro|vip)$", message = "目标会员类型只能是pro或vip")
    private String targetType;

    @NotNull(message = "时长不能为空")
    @Min(value = 1, message = "时长最少1个月")
    @Max(value = 120, message = "时长最多120个月")
    private Integer durationMonths;

    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;  // 可选，不传则自动根据系统设置计算

    private String remark;
}
