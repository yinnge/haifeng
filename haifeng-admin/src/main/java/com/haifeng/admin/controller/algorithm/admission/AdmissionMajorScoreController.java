package com.haifeng.admin.controller.algorithm.admission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.admission.AdmissionMajorScoreAddDTO;
import com.haifeng.admin.dto.algorithm.admission.AdmissionMajorScoreQueryDTO;
import com.haifeng.admin.service.algorithm.admission.AdmissionMajorScoreService;
import com.haifeng.admin.vo.algorithm.admission.AdmissionMajorScoreDetailVO;
import com.haifeng.admin.vo.algorithm.admission.AdmissionMajorScoreListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/algorithm/admission/major-score")
@RequiredArgsConstructor
public class AdmissionMajorScoreController {

    private final AdmissionMajorScoreService admissionMajorScoreService;

    @GetMapping("/page")
    public R<IPage<AdmissionMajorScoreListVO>> page(@Valid AdmissionMajorScoreQueryDTO dto) {
        return R.ok(admissionMajorScoreService.page(dto));
    }

    @GetMapping("/{id}")
    public R<AdmissionMajorScoreDetailVO> detail(@PathVariable Integer id) {
        return R.ok(admissionMajorScoreService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "专业录取明细管理", action = "新增专业明细")
    public R<Integer> add(@Valid @RequestBody AdmissionMajorScoreAddDTO dto) {
        return R.ok(admissionMajorScoreService.add(dto));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "专业录取明细管理", action = "修改专业明细")
    public R<Void> update(@PathVariable Integer id, @Valid @RequestBody AdmissionMajorScoreAddDTO dto) {
        admissionMajorScoreService.update(id, dto);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    @OperationLog(module = "专业录取明细管理", action = "修改专业明细状态")
    public R<Void> updateStatus(@PathVariable Integer id, @RequestParam Boolean isDeleted) {
        admissionMajorScoreService.updateStatus(id, isDeleted);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "专业录取明细管理", action = "删除专业明细")
    public R<Void> delete(@PathVariable Integer id) {
        admissionMajorScoreService.delete(id);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "专业录取明细管理", action = "批量删除专业明细")
    public R<Void> batchDelete(@RequestBody List<Integer> ids) {
        admissionMajorScoreService.batchDelete(ids);
        return R.ok();
    }
}
