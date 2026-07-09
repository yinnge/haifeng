package com.haifeng.admin.controller.system;

import com.haifeng.admin.dto.system.ProviderModelUpdateDTO;
import com.haifeng.admin.dto.system.SystemSettingsUpdateDTO;
import com.haifeng.admin.service.system.SystemSettingsService;
import com.haifeng.admin.vo.system.SystemSettingsVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统管理 - 系统设置（含服务商与模型配置）
 */
@RestController
@RequestMapping("/api/v1/admin/system/settings")
@RequiredArgsConstructor
@RequireAdminModule("system_setting")
public class SystemSettingsController {

    private final SystemSettingsService settingsService;

    /**
     * 获取系统设置
     */
    @GetMapping
    public R<SystemSettingsVO> get() {
        return R.ok(settingsService.get());
    }

    /**
     * 更新系统设置
     */
    @PutMapping
    @OperationLog(module = "系统管理", action = "更新系统设置")
    public R<Void> update(@Valid @RequestBody SystemSettingsUpdateDTO dto) {
        settingsService.update(dto);
        return R.ok();
    }

    /**
     * 获取所有启用的服务商列表
     */
    @GetMapping("/providers")
    @OperationLog(module = "系统管理", action = "获取服务商列表")
    public R<List<String>> listProviders() {
        return R.ok(settingsService.listProviders());
    }

    /**
     * 更新系统设置中的服务商和模型
     */
    @PutMapping("/provider-model")
    @OperationLog(module = "系统管理", action = "更新服务商和模型")
    public R<Void> updateProviderModel(@Valid @RequestBody ProviderModelUpdateDTO dto) {
        settingsService.updateProviderModel(dto);
        return R.ok();
    }
}
