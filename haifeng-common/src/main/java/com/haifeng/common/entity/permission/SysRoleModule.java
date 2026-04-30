package com.haifeng.common.entity.permission;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_role_module")
public class SysRoleModule {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long roleId;

    private Long moduleId;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
