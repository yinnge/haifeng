package com.haifeng.app.controller.algorithm;

import com.haifeng.app.dto.algorithm.GaokaoArchiveSaveDTO;
import com.haifeng.app.service.algorithm.GaokaoArchiveService;
import com.haifeng.app.vo.algorithm.*;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 高考档案控制器
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/gaokao")
@RequiredArgsConstructor
@RequireLogin
public class GaokaoArchiveController {

    private final GaokaoArchiveService gaokaoArchiveService;

    /**
     * 获取改革模式及可选科目
     *
     * @param province 省份
     * @param year     高考年份
     */
    @GetMapping("/reform-model")
    public R<ReformModelVO> getReformModel(
            @RequestParam @NotBlank(message = "省份不能为空") String province,
            @RequestParam @NotNull(message = "年份不能为空")
            @Min(value = 2020, message = "年份不能早于2020")
            @Max(value = 2030, message = "年份不能晚于2030") Integer year) {
        return R.ok(gaokaoArchiveService.getReformModel(province, year));
    }

    /**
     * 查询位次
     *
     * @param province    省份
     * @param year        年份
     * @param subjectType 科类（物理类/历史类/理科/文科）
     * @param score       分数
     */
    @GetMapping("/rank")
    public R<ScoreRankVO> getRank(
            @RequestParam @NotBlank(message = "省份不能为空") String province,
            @RequestParam @NotNull(message = "年份不能为空") Integer year,
            @RequestParam @NotBlank(message = "科类不能为空") String subjectType,
            @RequestParam @NotNull(message = "分数不能为空")
            @Min(value = 0, message = "分数不能小于0")
            @Max(value = 750, message = "分数不能大于750") Integer score) {
        return R.ok(gaokaoArchiveService.getRank(province, year, subjectType, score));
    }

    /**
     * 获取批次分数线列表
     *
     * @param province    省份
     * @param year        年份
     * @param subjectType 科类
     */
    @GetMapping("/batch-lines")
    public R<BatchLineListVO> getBatchLines(
            @RequestParam @NotBlank(message = "省份不能为空") String province,
            @RequestParam @NotNull(message = "年份不能为空") Integer year,
            @RequestParam @NotBlank(message = "科类不能为空") String subjectType) {
        return R.ok(gaokaoArchiveService.getBatchLines(province, year, subjectType));
    }

    /**
     * 保存高考档案（新增或更新）
     */
    @PostMapping("/archive")
    public R<Long> saveArchive(@Valid @RequestBody GaokaoArchiveSaveDTO dto) {
        Long archiveId = gaokaoArchiveService.saveArchive(dto);
        return R.ok(archiveId);
    }

    /**
     * 获取当前用户的高考档案
     */
    @GetMapping("/archive")
    public R<GaokaoArchiveVO> getMyArchive() {
        return R.ok(gaokaoArchiveService.getMyArchive());
    }
}
