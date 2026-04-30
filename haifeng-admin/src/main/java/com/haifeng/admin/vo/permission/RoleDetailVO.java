package com.haifeng.admin.vo.permission;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class RoleDetailVO {

    private Long id;
    private String roleName;
    private String roleCode;
    private String description;
    private Integer status;
    private OffsetDateTime createdAt;
    private List<Long> moduleIds;
    private List<ModuleTreeVO> modules;
}
