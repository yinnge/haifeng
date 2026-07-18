package com.haifeng.admin.controller.resource;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.resource.ResourceAddDTO;
import com.haifeng.admin.dto.resource.ResourceQueryDTO;
import com.haifeng.admin.dto.resource.ResourceStatusDTO;
import com.haifeng.admin.dto.resource.ResourceUpdateDTO;
import com.haifeng.admin.service.resource.ResourceService;
import com.haifeng.admin.vo.resource.ResourceDetailVO;
import com.haifeng.admin.vo.resource.ResourceListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/admin/resource")
@RequiredArgsConstructor
@RequireAdminModule("resource_info")
public class ResourceController {

    private final ResourceService resourceService;

    /**
     * 分页查询资源列表
     */
    @GetMapping("/list")
    public R<IPage<ResourceListVO>> list(@Valid ResourceQueryDTO dto) {
        return R.ok(resourceService.page(dto));
    }

    /**
     * 获取资源详情
     */
    @GetMapping("/{id}")
    public R<ResourceDetailVO> detail(@PathVariable Long id) {
        return R.ok(resourceService.detail(id));
    }

    /**
     * 新增资源
     */
    @PostMapping
    @OperationLog(module = "资源管理", action = "新增资源")
    public R<Long> add(@Valid @RequestBody ResourceAddDTO dto) {
        return R.ok(resourceService.add(dto));
    }

    /**
     * 修改资源
     */
    @PutMapping("/{id}")
    @OperationLog(module = "资源管理", action = "修改资源")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ResourceUpdateDTO dto) {
        resourceService.update(id, dto);
        return R.ok();
    }

    /**
     * 修改资源状态（禁用/启用）
     */
    @PutMapping("/{id}/status")
    @OperationLog(module = "资源管理", action = "修改资源状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody ResourceStatusDTO dto) {
        resourceService.updateStatus(id, dto);
        return R.ok();
    }

    /**
     * 删除资源（软删除）
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "资源管理", action = "删除资源")
    public R<Void> delete(@PathVariable Long id) {
        resourceService.delete(id);
        return R.ok();
    }

    /**
     * 批量删除资源（软删除）
     */
    @PostMapping("/batch-delete")
    @OperationLog(module = "资源管理", action = "批量删除资源")
    public R<Void> batchDelete(@Valid @RequestBody @NotEmpty(message = "ids 不能为空") @Size(max = 100, message = "批量删除最多100条") List<Long> ids) {
        resourceService.batchDelete(ids);
        return R.ok();
    }

    /**
     * 获取所有不重复的分类（用于前端下拉筛选）
     */
    @GetMapping("/categories")
    public R<List<String>> getCategories() {
        return R.ok(resourceService.getCategories());
    }
}
