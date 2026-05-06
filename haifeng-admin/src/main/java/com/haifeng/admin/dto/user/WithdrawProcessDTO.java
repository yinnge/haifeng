package com.haifeng.admin.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class WithdrawProcessDTO {

    @NotBlank(message = "处理动作不能为空")
    @Pattern(regexp = "paid|rejected", message = "处理动作只能是paid或rejected")
    private String action;

    private String remark;
}
