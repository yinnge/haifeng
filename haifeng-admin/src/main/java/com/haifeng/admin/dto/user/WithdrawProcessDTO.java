package com.haifeng.admin.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WithdrawProcessDTO {

    @NotBlank(message = "处理动作不能为空")
    @Pattern(regexp = "^(paid|rejected)$", message = "处理动作只能是paid或rejected")
    private String action;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
