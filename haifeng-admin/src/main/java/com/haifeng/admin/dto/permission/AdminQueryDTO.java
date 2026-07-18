package com.haifeng.admin.dto.permission;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminQueryDTO extends BasePageQueryDTO {

    @Size(max = 50, message = "用户名长度不能超过50")
    private String username;

    @Size(max = 20, message = "手机号长度不能超过20")
    private String phone;

    @Size(max = 50, message = "真实姓名长度不能超过50")
    private String realName;

    private Integer status;
}
