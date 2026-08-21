package com.haifeng.app.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AvatarUpdateDTO {

    @NotBlank(message = "头像URL不能为空")
    @Size(max = 4096, message = "头像URL最长4096个字符")
    private String avatar;
}
