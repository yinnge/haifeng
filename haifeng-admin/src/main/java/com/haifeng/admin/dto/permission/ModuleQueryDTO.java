package com.haifeng.admin.dto.permission;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ModuleQueryDTO {

    @Size(max = 50, message = "关键字长度不能超过50")
    private String keyword;

    private Boolean exactMatch;

    @Size(max = 50, message = "模块名称长度不能超过50")
    private String moduleName;

    @Size(max = 50, message = "模块编码长度不能超过50")
    private String moduleCode;

    @Size(max = 200, message = "路由路径长度不能超过200")
    private String path;

    private Integer status;

    private Integer level;
}
