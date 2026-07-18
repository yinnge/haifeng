package com.haifeng.admin.controller.permission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.permission.AdminAddDTO;
import com.haifeng.admin.dto.permission.AdminQueryDTO;
import com.haifeng.admin.dto.permission.AdminUpdateDTO;
import com.haifeng.admin.service.permission.AdminService;
import com.haifeng.admin.vo.permission.AdminDetailVO;
import com.haifeng.admin.vo.permission.AdminListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 权限管理 - 管理员账号管理
 */
@Validated
@RestController
@RequestMapping("/api/v1/admin/permission/admins")
@RequiredArgsConstructor
@RequireAdminModule("permission_admin")
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public R<IPage<AdminListVO>> page(@Valid AdminQueryDTO dto) {
        IPage<AdminListVO> page = adminService.page(dto);
        return R.ok(page);
    }

    @GetMapping("/{id}")
    public R<AdminDetailVO> detail(@PathVariable Long id) {
        AdminDetailVO vo = adminService.detail(id);
        return R.ok(vo);
    }

    @PostMapping
    @OperationLog(module = "权限管理", action = "新增管理员")
    public R<Void> add(@Valid @RequestBody AdminAddDTO dto) {
        adminService.add(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    @OperationLog(module = "权限管理", action = "修改管理员")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody AdminUpdateDTO dto) {
        adminService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "权限管理", action = "删除管理员")
    public R<Void> delete(@PathVariable Long id) {
        adminService.delete(id);
        return R.ok();
    }

    @PutMapping("/{id}/toggle-status")
    @OperationLog(module = "权限管理", action = "切换管理员状态")
    public R<Void> toggleStatus(@PathVariable Long id) {
        adminService.toggleStatus(id);
        return R.ok();
    }
}
