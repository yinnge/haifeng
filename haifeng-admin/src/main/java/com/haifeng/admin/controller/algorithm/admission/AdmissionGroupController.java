package com.haifeng.admin.controller.algorithm.admission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.admission.AdmissionGroupAddDTO;
import com.haifeng.admin.dto.algorithm.admission.AdmissionGroupQueryDTO;
import com.haifeng.admin.service.algorithm.admission.AdmissionGroupService;
import com.haifeng.admin.vo.algorithm.admission.AdmissionGroupDetailVO;
import com.haifeng.admin.vo.algorithm.admission.AdmissionGroupListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/admin/algorithm/admission/group")
@RequiredArgsConstructor
@RequireAdminModule("algo_admission_grp")
public class AdmissionGroupController {

    private final AdmissionGroupService admissionGroupService;

    @GetMapping("/page")
    public R<IPage<AdmissionGroupListVO>> page(@Valid AdmissionGroupQueryDTO dto) {
        return R.ok(admissionGroupService.page(dto));
    }

    @GetMapping("/{id}")
    public R<AdmissionGroupDetailVO> detail(@PathVariable Integer id) {
        return R.ok(admissionGroupService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "专业组管理", action = "新增专业组")
    public R<Integer> add(@Valid @RequestBody AdmissionGroupAddDTO dto) {
        return R.ok(admissionGroupService.add(dto));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "专业组管理", action = "修改专业组")
    public R<Void> update(@PathVariable Integer id, @Valid @RequestBody AdmissionGroupAddDTO dto) {
        admissionGroupService.update(id, dto);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    @OperationLog(module = "专业组管理", action = "修改专业组状态")
    public R<Void> updateStatus(@PathVariable Integer id, @RequestParam Boolean isDeleted) {
        admissionGroupService.updateStatus(id, isDeleted);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "专业组管理", action = "删除专业组")
    public R<Void> delete(@PathVariable Integer id) {
        admissionGroupService.delete(id);
        return R.ok();
    }

    @PostMapping("/batch")
    @OperationLog(module = "专业组管理", action = "批量删除专业组")
    public R<Void> batchDelete(@Valid @RequestBody @NotEmpty(message = "ids不能为空") @Size(max = 100) List<Integer> ids) {
        admissionGroupService.batchDelete(ids);
        return R.ok();
    }

    @PostMapping("/import")
    @OperationLog(module = "专业组管理", action = "导入专业组数据")
    public R<Void> importData(@RequestParam("file") MultipartFile file) {
        admissionGroupService.importData(file);
        return R.ok();
    }

    @PostMapping("/recalc-all")
    @OperationLog(module = "专业组管理", action = "全量重算聚合数据")
    public R<Integer> recalcAll() {
        return R.ok(admissionGroupService.recalcAll());
    }
}
