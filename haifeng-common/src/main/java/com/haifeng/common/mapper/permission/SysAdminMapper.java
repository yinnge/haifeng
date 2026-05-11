package com.haifeng.common.mapper.permission;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.permission.SysAdmin;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysAdminMapper extends BaseMapper<SysAdmin> {

    /**
     * 硬删除管理员（绕过 @TableLogic 逻辑删除）
     */
    @Delete("DELETE FROM sys_admin WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);
}
