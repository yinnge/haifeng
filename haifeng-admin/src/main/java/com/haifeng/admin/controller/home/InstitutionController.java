package com.haifeng.admin.controller.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.*;
import com.haifeng.admin.service.home.InstitutionService;
import com.haifeng.admin.vo.home.*;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 首页管理 - 培训机构管理
 */
@Validated
@RestController
@RequestMapping("/api/v1/admin/home/institution")
@RequiredArgsConstructor
@RequireAdminModule("home_institution")
public class InstitutionController {

    private final InstitutionService institutionService;

    /**
     * 分页查询培训机构列表
     */
    @GetMapping("/list")
    public R<IPage<InstitutionListVO>> list(@Valid InstitutionQueryDTO dto) {
        return R.ok(institutionService.page(dto));
    }

    /**
     * 获取培训机构详情
     */
    @GetMapping("/{id}")
    public R<InstitutionDetailVO> detail(@PathVariable Long id) {
        return R.ok(institutionService.detail(id));
    }

    /**
     * 新增培训机构
     */
    @PostMapping
    @OperationLog(module = "首页管理", action = "新增培训机构")
    public R<Long> add(@Valid @RequestBody InstitutionAddDTO dto) {
        return R.ok(institutionService.add(dto));
    }

    /**
     * 修改培训机构
     */
    @PutMapping("/{id}")
    @OperationLog(module = "首页管理", action = "修改培训机构")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody InstitutionUpdateDTO dto) {
        institutionService.update(id, dto);
        return R.ok();
    }

    /**
     * 修改培训机构状态
     */
    @PutMapping("/{id}/status")
    @OperationLog(module = "首页管理", action = "修改培训机构状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        institutionService.updateStatus(id, dto);
        return R.ok();
    }

    /**
     * 硬删除培训机构
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "首页管理", action = "硬删除培训机构")
    public R<Void> delete(@PathVariable Long id) {
        institutionService.delete(id);
        return R.ok();
    }
}
