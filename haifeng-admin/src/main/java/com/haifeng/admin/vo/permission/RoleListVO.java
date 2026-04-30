package com.haifeng.admin.vo.permission;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class RoleListVO {

    private Long id;
    private String roleName;
    private String roleCode;
    private String description;
    private Integer status;
    private OffsetDateTime createdAt;
}
