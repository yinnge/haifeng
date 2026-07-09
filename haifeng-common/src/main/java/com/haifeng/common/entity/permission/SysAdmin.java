package com.haifeng.common.entity.permission;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_admin")
public class SysAdmin {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    private String password;

    private String realName;

    private String phone;

    private String email;

    private String avatar;

    private Long roleId;

    private String roleName;

    private Integer status;

    @Version
    private Integer version;

    private Integer tokenVersion;

    private OffsetDateTime lastLoginAt;

    private String lastLoginIp;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    private String totpSecret;

    @TableField("is_totp_enabled")
    private Boolean totpEnabled;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
