package com.haifeng.common.mapper.permission;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.permission.SysRoleModule;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleModuleMapper extends BaseMapper<SysRoleModule> {

    @Insert("<script>INSERT INTO sys_role_module (id, role_id, module_id, created_at, updated_at) VALUES <foreach collection='list' item='rm' separator=','>(#{rm.id}, #{rm.roleId}, #{rm.moduleId}, #{rm.createdAt}, #{rm.updatedAt})</foreach></script>")
    void insertBatch(@Param("list") List<SysRoleModule> list);
}
