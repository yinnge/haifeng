package com.haifeng.admin.dto.permission;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RoleModuleBindDTO {

    @NotEmpty(message = "模块ID列表不能为空")
    @Size(max = 200, message = "模块ID列表最多200个")
    private List<Long> moduleIds;
}
