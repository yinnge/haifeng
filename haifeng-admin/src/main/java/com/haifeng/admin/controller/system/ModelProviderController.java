package com.haifeng.admin.controller.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.system.ModelProviderCreateDTO;
import com.haifeng.admin.dto.system.ModelProviderQueryDTO;
import com.haifeng.admin.dto.system.ModelProviderStatusDTO;
import com.haifeng.admin.dto.system.ModelProviderUpdateDTO;
import com.haifeng.admin.dto.system.ProviderModelUpdateDTO;
import com.haifeng.admin.service.system.AiBalanceService;
import com.haifeng.admin.service.system.ModelProviderService;
import com.haifeng.admin.service.system.SystemSettingsService;
import com.haifeng.admin.vo.system.AiBalanceVO;
import com.haifeng.admin.vo.system.ModelProviderVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统管理 - 模型供应商配置（Spring AI 服务商/模型管理）
 */
@Validated
@RestController
@RequestMapping("/api/v1/admin/system/model-providers")
@RequiredArgsConstructor
@RequireAdminModule("system_provider")
public class ModelProviderController {

    private final ModelProviderService modelProviderService;
    private final AiBalanceService aiBalanceService;
    private final SystemSettingsService settingsService;

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
     * 禁用模型供应商配置
     */
    @PutMapping("/{id}/disable")
    @OperationLog(module = "系统管理", action = "禁用模型供应商配置")
    public R<Void> delete(@PathVariable Long id) {
        modelProviderService.delete(id);
        return R.ok();
    }

    /**
     * 修改模型供应商配置状态
     */
    @PutMapping("/{id}/status")
    @OperationLog(module = "系统管理", action = "修改模型供应商配置状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody ModelProviderStatusDTO dto) {
        modelProviderService.updateStatus(id, dto);
        return R.ok();
    }

    /**
     * 查询 DeepSeek 厂商余额
     */
    @GetMapping("/balance")
    @OperationLog(module = "系统管理", action = "查询AI厂商余额")
    public R<List<AiBalanceVO>> getBalance(
            @RequestParam(required = false, defaultValue = "false") Boolean refresh) {
        return R.ok(aiBalanceService.getDeepSeekBalances(refresh));
    }

    /**
     * 查询「当前生效」的服务商与模型
     * <p>
     * 放在本控制器下（而非 SystemSettingsController），使「模型配置」页的全部读写
     * 都归属同一个模块权限 {@code system_provider}，避免只授予模型配置权限的角色
     * 在调用 system_setting 接口时被 403。
     *
     * @return providerName / modelName，未配置时返回 null 值
     */
    @GetMapping("/current")
    public R<Map<String, String>> getCurrentProviderModel() {
        var vo = settingsService.get();
        Map<String, String> map = new java.util.HashMap<>(2);
        map.put("providerName", vo == null ? null : vo.getProviderName());
        map.put("modelName", vo == null ? null : vo.getModelName());
        return R.ok(map);
    }

    /**
     * 将指定服务商与模型设为「当前生效」
     */
    @PutMapping("/current")
    @OperationLog(module = "系统管理", action = "设置当前服务商和模型")
    public R<Void> updateCurrentProviderModel(@Valid @RequestBody ProviderModelUpdateDTO dto) {
        settingsService.updateProviderModel(dto);
        return R.ok();
    }
}
