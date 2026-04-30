package com.haifeng.admin.dto.permission;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class RoleModuleBindDTO {

    @NotEmpty(message = "模块ID列表不能为空")
    private List<Long> moduleIds;
}
