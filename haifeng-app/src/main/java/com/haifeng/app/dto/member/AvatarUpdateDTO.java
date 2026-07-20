package com.haifeng.app.dto.member;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AvatarUpdateDTO {

    @NotBlank(message = "头像URL不能为空")
    private String avatar;
}
