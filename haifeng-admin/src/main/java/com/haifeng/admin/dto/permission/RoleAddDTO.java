package com.haifeng.admin.dto.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleAddDTO {

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称最长50字符")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码最长50字符")
    private String roleCode;

    @Size(max = 100, message = "描述最长100字符")
    private String description;
}
