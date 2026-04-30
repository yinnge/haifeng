package com.haifeng.admin.dto.permission;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AdminUpdateDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度必须在2-50之间")
    private String username;

    @Pattern(regexp = "^$|^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,16}$",
             message = "密码必须是数字+字母，长度6-16位")
    private String password;

    @Size(max = 50, message = "真实姓名最长50字符")
    private String realName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱最长100字符")
    private String email;

    @Size(max = 500, message = "头像URL最长500字符")
    private String avatar;

    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    private Integer status;
}
