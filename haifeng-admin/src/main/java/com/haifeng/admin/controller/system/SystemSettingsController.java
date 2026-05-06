package com.haifeng.admin.controller.system;

import com.haifeng.admin.dto.system.SystemSettingsUpdateDTO;
import com.haifeng.admin.service.system.SystemSettingsService;
import com.haifeng.admin.vo.system.SystemSettingsVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/system/settings")
@RequiredArgsConstructor
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
}
