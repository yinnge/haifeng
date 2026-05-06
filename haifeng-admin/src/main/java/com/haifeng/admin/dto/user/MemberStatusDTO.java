package com.haifeng.admin.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MemberStatusDTO {

    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^(active|disabled)$", message = "状态只能是 active 或 disabled")
    private String status;
}
