package com.haifeng.common.mapper.permission;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.permission.SysRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 硬删除角色（绕过 @TableLogic 逻辑删除）
     */
    @Delete("DELETE FROM sys_role WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);
}
