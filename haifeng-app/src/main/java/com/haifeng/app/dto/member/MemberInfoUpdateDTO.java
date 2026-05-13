package com.haifeng.app.dto.member;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MemberInfoUpdateDTO {

    @Size(min = 2, max = 50, message = "用户名长度2-50个字符")
    private String username;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Size(max = 500, message = "头像URL最多500个字符")
    private String avatar;
}
