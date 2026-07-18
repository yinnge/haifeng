package com.haifeng.admin.controller.algorithm.config;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.config.ProvinceReformAddDTO;
import com.haifeng.admin.dto.algorithm.config.ProvinceReformQueryDTO;
import com.haifeng.admin.service.algorithm.config.ProvinceReformService;
import com.haifeng.admin.vo.algorithm.config.ProvinceReformDetailVO;
import com.haifeng.admin.vo.algorithm.config.ProvinceReformListVO;
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
@RequestMapping("/api/v1/admin/algorithm/config/province-reform")
@RequiredArgsConstructor
@RequireAdminModule("algo_score_prov")
public class ProvinceReformController {

    private final ProvinceReformService provinceReformService;

    @GetMapping("/page")
    public R<IPage<ProvinceReformListVO>> page(@Valid ProvinceReformQueryDTO dto) {
        return R.ok(provinceReformService.page(dto));
    }

    @GetMapping("/{id}")
    public R<ProvinceReformDetailVO> detail(@PathVariable Long id) {
        return R.ok(provinceReformService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "省份改革配置", action = "新增省份配置")
    public R<Long> add(@Valid @RequestBody ProvinceReformAddDTO dto) {
        return R.ok(provinceReformService.add(dto));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "省份改革配置", action = "修改省份配置")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ProvinceReformAddDTO dto) {
        provinceReformService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "省份改革配置", action = "删除省份配置")
    public R<Void> delete(@PathVariable Long id) {
        provinceReformService.delete(id);
        return R.ok();
    }

    @PostMapping("/batch-delete")
    @OperationLog(module = "省份改革配置", action = "批量删除省份配置")
    public R<Void> batchDelete(@Valid @RequestBody @NotEmpty @Size(max = 100) List<Long> ids) {
        provinceReformService.batchDelete(ids);
        return R.ok();
    }
}
