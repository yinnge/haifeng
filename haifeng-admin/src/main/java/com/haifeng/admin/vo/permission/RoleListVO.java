package com.haifeng.admin.vo.permission;

import lombok.Data;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class RoleListVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String roleName;
    private String roleCode;
    private String description;
    private Integer status;
    private OffsetDateTime createdAt;
}
