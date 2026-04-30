package com.haifeng.admin.vo.permission;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class AdminDetailVO {

    private Long id;

    private String username;

    private String realName;

    private String phone;

    private String email;

    private String avatar;

    private Long roleId;

    private String roleName;

    private Integer status;

    private OffsetDateTime lastLoginAt;

    private String lastLoginIp;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
