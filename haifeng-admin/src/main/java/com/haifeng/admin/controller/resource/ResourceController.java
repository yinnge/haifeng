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
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/resource")
@RequiredArgsConstructor
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
     * 硬删除资源
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "资源管理", action = "硬删除资源")
    public R<Void> delete(@PathVariable Long id) {
        resourceService.delete(id);
        return R.ok();
    }

    /**
     * 批量硬删除资源
     */
    @DeleteMapping("/batch")
    @OperationLog(module = "资源管理", action = "批量硬删除资源")
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        resourceService.batchDelete(ids);
        return R.ok();
    }
}
