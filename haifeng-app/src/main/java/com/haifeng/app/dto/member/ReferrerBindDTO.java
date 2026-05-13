package com.haifeng.app.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReferrerBindDTO {

    @NotBlank(message = "邀请码不能为空")
    @Size(min = 8, max = 8, message = "邀请码长度必须为8位")
    private String inviteCode;
}
