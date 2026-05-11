package com.haifeng.admin.controller.permission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.permission.AdminAddDTO;
import com.haifeng.admin.dto.permission.AdminQueryDTO;
import com.haifeng.admin.dto.permission.AdminUpdateDTO;
import com.haifeng.admin.service.permission.AdminService;
import com.haifeng.admin.vo.permission.AdminDetailVO;
import com.haifeng.admin.vo.permission.AdminListVO;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/permission/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public R<IPage<AdminListVO>> page(AdminQueryDTO dto) {
        IPage<AdminListVO> page = adminService.page(dto);
        return R.ok(page);
    }

    @GetMapping("/{id}")
    public R<AdminDetailVO> detail(@PathVariable Long id) {
        AdminDetailVO vo = adminService.detail(id);
        return R.ok(vo);
    }

    @PostMapping
    public R<Void> add(@Valid @RequestBody AdminAddDTO dto) {
        adminService.add(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody AdminUpdateDTO dto) {
        adminService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        adminService.delete(id);
        return R.ok();
    }

    @PutMapping("/{id}/toggle-status")
    public R<Void> toggleStatus(@PathVariable Long id) {
        adminService.toggleStatus(id);
        return R.ok();
    }
}
