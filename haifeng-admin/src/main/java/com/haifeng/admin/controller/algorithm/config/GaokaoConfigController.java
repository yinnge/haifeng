package com.haifeng.admin.controller.algorithm.config;

import com.haifeng.admin.dto.algorithm.config.GaokaoConfigUpdateDTO;
import com.haifeng.admin.service.algorithm.config.GaokaoConfigService;
import com.haifeng.admin.vo.algorithm.config.GaokaoConfigDetailVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/admin/algorithm/config/gaokao-config")
@RequiredArgsConstructor
@RequireAdminModule("algo_config_gaokao")
public class GaokaoConfigController {

    private final GaokaoConfigService gaokaoConfigService;

    @GetMapping("/current")
    public R<GaokaoConfigDetailVO> current() {
        return R.ok(gaokaoConfigService.getCurrent());
    }

    @PutMapping("/current")
    @OperationLog(module = "高考算法全局配置", action = "修改全局参数")
    public R<Void> update(@Valid @RequestBody GaokaoConfigUpdateDTO dto) {
        gaokaoConfigService.update(dto);
        return R.ok();
    }
}
