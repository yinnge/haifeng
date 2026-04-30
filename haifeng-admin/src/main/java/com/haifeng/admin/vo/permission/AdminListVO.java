package com.haifeng.admin.vo.permission;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class AdminListVO {

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

    private OffsetDateTime createdAt;
}
