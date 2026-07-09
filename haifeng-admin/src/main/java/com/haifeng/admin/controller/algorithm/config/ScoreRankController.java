package com.haifeng.admin.controller.algorithm.config;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.config.ScoreRankAddDTO;
import com.haifeng.admin.dto.algorithm.config.ScoreRankQueryDTO;
import com.haifeng.admin.service.algorithm.config.ScoreRankService;
import com.haifeng.admin.vo.algorithm.config.ScoreRankDetailVO;
import com.haifeng.admin.vo.algorithm.config.ScoreRankListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/algorithm/config/score-rank")
@RequiredArgsConstructor
@RequireAdminModule("algo_score_rank")
public class ScoreRankController {

    private final ScoreRankService scoreRankService;

    @GetMapping("/page")
    public R<IPage<ScoreRankListVO>> page(@Valid ScoreRankQueryDTO dto) {
        return R.ok(scoreRankService.page(dto));
    }

    @GetMapping("/{id}")
    public R<ScoreRankDetailVO> detail(@PathVariable Long id) {
        return R.ok(scoreRankService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "一分一段管理", action = "新增一分一段记录")
    public R<Long> add(@Valid @RequestBody ScoreRankAddDTO dto) {
        return R.ok(scoreRankService.add(dto));
    }

    @PutMapping("/{id}")
    @OperationLog(module = "一分一段管理", action = "修改一分一段记录")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ScoreRankAddDTO dto) {
        scoreRankService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "一分一段管理", action = "删除一分一段记录")
    public R<Void> delete(@PathVariable Long id) {
        scoreRankService.delete(id);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "一分一段管理", action = "批量删除一分一段记录")
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        scoreRankService.batchDelete(ids);
        return R.ok();
    }

    @PostMapping("/import")
    @OperationLog(module = "一分一段管理", action = "导入一分一段数据")
    public R<Void> importData(@RequestParam("file") MultipartFile file) {
        scoreRankService.importData(file);
        return R.ok();
    }
}
