package com.haifeng.admin.service.permission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.permission.RoleAddDTO;
import com.haifeng.admin.dto.permission.RoleModuleBindDTO;
import com.haifeng.admin.dto.permission.RoleQueryDTO;
import com.haifeng.admin.dto.permission.RoleUpdateDTO;
import com.haifeng.admin.vo.permission.RoleDetailVO;
import com.haifeng.admin.vo.permission.RoleListVO;

public interface RoleService {

    IPage<RoleListVO> page(RoleQueryDTO dto);

    RoleDetailVO detail(Long id);

    void add(RoleAddDTO dto);

    void update(Long id, RoleUpdateDTO dto);

    void delete(Long id);

    void bindModules(Long id, RoleModuleBindDTO dto);
}
