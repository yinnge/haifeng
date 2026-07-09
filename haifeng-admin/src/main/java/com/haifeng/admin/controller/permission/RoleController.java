package com.haifeng.admin.controller.permission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.permission.RoleAddDTO;
import com.haifeng.admin.dto.permission.RoleModuleBindDTO;
import com.haifeng.admin.dto.permission.RoleQueryDTO;
import com.haifeng.admin.dto.permission.RoleUpdateDTO;
import com.haifeng.admin.service.permission.RoleService;
import com.haifeng.admin.vo.permission.RoleDetailVO;
import com.haifeng.admin.vo.permission.RoleListVO;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 权限管理 - 角色管理（含角色-模块绑定）
 */
@Validated
@RestController
@RequestMapping("/api/v1/admin/permission/roles")
@RequireAdminModule("permission_role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public R<IPage<RoleListVO>> page(@Valid RoleQueryDTO dto) {
        IPage<RoleListVO> page = roleService.page(dto);
        return R.ok(page);
    }

    @GetMapping("/{id}")
    public R<RoleDetailVO> detail(@PathVariable Long id) {
        RoleDetailVO vo = roleService.detail(id);
        return R.ok(vo);
    }

    @PostMapping
    public R<Void> add(@Valid @RequestBody RoleAddDTO dto) {
        roleService.add(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody RoleUpdateDTO dto) {
        roleService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return R.ok();
    }

    @PutMapping("/{id}/toggle-status")
    public R<Void> toggleStatus(@PathVariable Long id) {
        roleService.toggleStatus(id);
        return R.ok();
    }

    @PostMapping("/{id}/modules")
    public R<Void> bindModules(@PathVariable Long id, @Valid @RequestBody RoleModuleBindDTO dto) {
        roleService.bindModules(id, dto);
        return R.ok();
    }
}
