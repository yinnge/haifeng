package com.haifeng.common.mapper.permission;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.permission.SysModule;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysModuleMapper extends BaseMapper<SysModule> {

    /**
     * 硬删除模块（绕过 @TableLogic 逻辑删除）
     */
    @Delete("DELETE FROM sys_module WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);
}
