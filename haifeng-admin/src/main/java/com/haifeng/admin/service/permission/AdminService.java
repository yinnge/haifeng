package com.haifeng.admin.service.permission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.permission.AdminAddDTO;
import com.haifeng.admin.dto.permission.AdminQueryDTO;
import com.haifeng.admin.dto.permission.AdminUpdateDTO;
import com.haifeng.admin.vo.permission.AdminDetailVO;
import com.haifeng.admin.vo.permission.AdminListVO;

public interface AdminService {

    IPage<AdminListVO> page(AdminQueryDTO dto);

    AdminDetailVO detail(Long id);

    void add(AdminAddDTO dto);

    void update(Long id, AdminUpdateDTO dto);

    void delete(Long id);
}
