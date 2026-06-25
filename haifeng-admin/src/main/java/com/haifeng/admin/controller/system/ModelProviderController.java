package com.haifeng.admin.controller.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.system.ModelProviderCreateDTO;
import com.haifeng.admin.dto.system.ModelProviderQueryDTO;
import com.haifeng.admin.dto.system.ModelProviderStatusDTO;
import com.haifeng.admin.dto.system.ModelProviderUpdateDTO;
import com.haifeng.admin.service.system.ModelProviderService;
import com.haifeng.admin.vo.system.ModelProviderVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/system/model-providers")
@RequiredArgsConstructor
public class ModelProviderController {

    private final ModelProviderService modelProviderService;

    /**
     * 分页查询模型供应商配置列表
     */
    @GetMapping("/list")
    public R<IPage<ModelProviderVO>> list(@Valid ModelProviderQueryDTO dto) {
        return R.ok(modelProviderService.page(dto));
    }

    /**
     * 获取模型供应商配置详情
     */
    @GetMapping("/{id}")
    public R<ModelProviderVO> detail(@PathVariable Long id) {
        return R.ok(modelProviderService.detail(id));
    }

    /**
     * 新增模型供应商配置
     */
    @PostMapping
    @OperationLog(module = "系统管理", action = "新增模型供应商配置")
    public R<ModelProviderVO> create(@Valid @RequestBody ModelProviderCreateDTO dto) {
        return R.ok(modelProviderService.create(dto));
    }

    /**
     * 修改模型供应商配置
     */
    @PutMapping("/{id}")
    @OperationLog(module = "系统管理", action = "修改模型供应商配置")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ModelProviderUpdateDTO dto) {
        modelProviderService.update(id, dto);
        return R.ok();
    }

    /**
     * 删除模型供应商配置
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "系统管理", action = "删除模型供应商配置")
    public R<Void> delete(@PathVariable Long id) {
        modelProviderService.delete(id);
        return R.ok();
    }

    /**
     * 修改模型供应商配置状态
     */
    @RequestMapping(value = "/{id}/status", method = {RequestMethod.PATCH, RequestMethod.PUT})
    @OperationLog(module = "系统管理", action = "修改模型供应商配置状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody ModelProviderStatusDTO dto) {
        modelProviderService.updateStatus(id, dto);
        return R.ok();
    }
}
