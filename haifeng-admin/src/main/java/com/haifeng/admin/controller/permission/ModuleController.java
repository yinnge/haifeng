package com.haifeng.admin.controller.permission;

import com.haifeng.admin.dto.permission.ModuleAddDTO;
import com.haifeng.admin.dto.permission.ModuleQueryDTO;
import com.haifeng.admin.dto.permission.ModuleUpdateDTO;
import com.haifeng.admin.service.permission.ModuleService;
import com.haifeng.admin.vo.permission.ModuleTreeVO;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/permission/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    @GetMapping
    public R<List<ModuleTreeVO>> list(ModuleQueryDTO dto) {
        List<ModuleTreeVO> tree = moduleService.listTree(dto);
        return R.ok(tree);
    }

    @PostMapping
    public R<Void> add(@Valid @RequestBody ModuleAddDTO dto) {
        moduleService.add(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ModuleUpdateDTO dto) {
        moduleService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        moduleService.delete(id);
        return R.ok();
    }

    @PutMapping("/{id}/toggle-status")
    public R<Void> toggleStatus(@PathVariable Long id) {
        moduleService.toggleStatus(id);
        return R.ok();
    }
}
