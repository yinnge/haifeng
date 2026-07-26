package com.haifeng.admin.vo.permission;

import lombok.Data;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class AdminDetailVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String username;

    private String realName;

    private String phone;

    private String email;

    private String avatar;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long roleId;

    private String roleName;

    private Integer status;

    private OffsetDateTime lastLoginAt;

    private String lastLoginIp;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
