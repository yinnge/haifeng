package com.haifeng.admin.controller.algorithm.config;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.config.ProvinceConfigQueryDTO;
import com.haifeng.admin.dto.algorithm.config.ProvinceConfigUpdateDTO;
import com.haifeng.admin.service.algorithm.config.ProvinceConfigService;
import com.haifeng.admin.vo.algorithm.config.ProvinceConfigDetailVO;
import com.haifeng.admin.vo.algorithm.config.ProvinceConfigListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/algorithm/config/province-config")
@RequiredArgsConstructor
@RequireAdminModule("algo_config_prov")
public class ProvinceConfigController {

    private final ProvinceConfigService provinceConfigService;

    @GetMapping("/page")
    public R<IPage<ProvinceConfigListVO>> page(@Valid ProvinceConfigQueryDTO dto) {
        return R.ok(provinceConfigService.page(dto));
    }

    @GetMapping("/{province}")
    public R<ProvinceConfigDetailVO> detail(@PathVariable String province) {
        return R.ok(provinceConfigService.detail(province));
    }

    @PutMapping("/{province}")
    @OperationLog(module = "省份算法配置", action = "修改省份算法参数")
    public R<Void> update(@PathVariable String province, @Valid @RequestBody ProvinceConfigUpdateDTO dto) {
        provinceConfigService.update(province, dto);
        return R.ok();
    }
}
