package com.haifeng.admin.dto.permission;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ModuleUpdateDTO {

    @NotBlank(message = "模块名称不能为空")
    @Size(max = 50, message = "模块名称最长50字符")
    private String moduleName;

    @NotBlank(message = "模块编码不能为空")
    @Size(max = 50, message = "模块编码最长50字符")
    private String moduleCode;

    private Long parentId;

    @Size(max = 200, message = "路由路径最长200字符")
    private String path;

    @Size(max = 50, message = "图标最长50字符")
    private String icon;

    private Integer sortOrder;

    @NotNull(message = "层级不能为空")
    @Min(value = 1, message = "层级最小为1")
    @Max(value = 2, message = "层级最大为2")
    private Integer level;

    @Size(max = 255, message = "描述最长255字符")
    private String description;
}
