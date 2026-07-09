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
@TableName("sys_role")
public class SysRole {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String roleName;

    private String roleCode;

    private String description;

    private Integer status;

    @Version
    private Integer version;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
