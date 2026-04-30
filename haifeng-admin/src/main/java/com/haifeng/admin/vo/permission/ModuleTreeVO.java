package com.haifeng.admin.vo.permission;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class ModuleTreeVO {

    private Long id;
    private String moduleName;
    private String moduleCode;
    private Long parentId;
    private String path;
    private String icon;
    private Integer sortOrder;
    private Integer level;
    private String description;
    private Integer status;
    private OffsetDateTime createdAt;
    private List<ModuleTreeVO> children;
}
