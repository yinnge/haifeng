package com.haifeng.common.mapper.permission;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.permission.SysAdmin;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysAdminMapper extends BaseMapper<SysAdmin> {

    /**
     * 硬删除管理员（绕过 @TableLogic 逻辑删除）
     */
    @Delete("DELETE FROM sys_admin WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);

    /**
     * 检查管理员是否有指定模块的权限
     */
    @Select("SELECT COUNT(1) FROM sys_admin a " +
            "JOIN sys_role_module rm ON a.role_id = rm.role_id " +
            "JOIN sys_module m ON rm.module_id = m.id " +
            "WHERE a.id = #{adminId} AND m.module_code = #{moduleCode} " +
            "AND a.is_deleted = FALSE AND m.is_deleted = FALSE")
    int countModulePermission(@Param("adminId") Long adminId, @Param("moduleCode") String moduleCode);

    /**
     * 获取管理员拥有的所有模块编码
     */
    @Select("SELECT m.module_code FROM sys_admin a " +
            "JOIN sys_role_module rm ON a.role_id = rm.role_id " +
            "JOIN sys_module m ON rm.module_id = m.id " +
            "WHERE a.id = #{adminId} AND a.is_deleted = FALSE AND m.is_deleted = FALSE " +
            "ORDER BY m.sort_order")
    List<String> selectModuleCodesByAdminId(@Param("adminId") Long adminId);
}
