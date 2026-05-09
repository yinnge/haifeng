package com.haifeng.admin.controller.algorithm.config;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.config.BatchScoreLineAddDTO;
import com.haifeng.admin.dto.algorithm.config.BatchScoreLineQueryDTO;
import com.haifeng.admin.service.algorithm.config.BatchScoreLineService;
import com.haifeng.admin.vo.algorithm.config.BatchScoreLineDetailVO;
import com.haifeng.admin.vo.algorithm.config.BatchScoreLineListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/algorithm/config/batch-score-line")
@RequiredArgsConstructor
public class BatchScoreLineController {

    private final BatchScoreLineService batchScoreLineService;

    @GetMapping("/page")
    public R<IPage<BatchScoreLineListVO>> page(@Valid BatchScoreLineQueryDTO dto) {
        return R.ok(batchScoreLineService.page(dto));
    }

    @GetMapping("/{id}")
    public R<BatchScoreLineDetailVO> detail(@PathVariable Long id) {
        return R.ok(batchScoreLineService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "批次分数线管理", action = "新增批次分数线记录")
    public R<Long> add(@Valid @RequestBody BatchScoreLineAddDTO dto) {
        return R.ok(batchScoreLineService.add(dto));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "批次分数线管理", action = "修改批次分数线记录")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody BatchScoreLineAddDTO dto) {
        batchScoreLineService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "批次分数线管理", action = "删除批次分数线记录")
    public R<Void> delete(@PathVariable Long id) {
        batchScoreLineService.delete(id);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "批次分数线管理", action = "批量删除批次分数线记录")
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        batchScoreLineService.batchDelete(ids);
        return R.ok();
    }

    @PostMapping("/import")
    @OperationLog(module = "批次分数线管理", action = "导入批次分数线数据")
    public R<Void> importData(@RequestParam("file") MultipartFile file) {
        batchScoreLineService.importData(file);
        return R.ok();
    }
}
