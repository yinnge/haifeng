package com.haifeng.admin.dto.permission;

import lombok.Data;

@Data
public class ModuleQueryDTO {

    private String keyword;

    private Boolean exactMatch;

    private String moduleName;

    private String moduleCode;

    private String path;

    private Integer status;

    private Integer level;
}
