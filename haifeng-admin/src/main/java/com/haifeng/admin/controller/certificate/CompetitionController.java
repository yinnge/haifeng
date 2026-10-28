package com.haifeng.admin.controller.certificate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.certificate.BatchDeleteDTO;
import com.haifeng.admin.dto.certificate.CompetitionAddDTO;
import com.haifeng.admin.dto.certificate.CompetitionQueryDTO;
import com.haifeng.admin.dto.certificate.CompetitionUpdateDTO;
import com.haifeng.admin.service.certificate.CompetitionService;
import com.haifeng.admin.vo.certificate.CompetitionDetailVO;
import com.haifeng.admin.vo.certificate.CompetitionListVO;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/competition")
@RequiredArgsConstructor
@RequireAdminModule("certificate_comp")
public class CompetitionController {

    private final CompetitionService competitionService;

    @GetMapping("/list")
    public R<IPage<CompetitionListVO>> list(@Valid CompetitionQueryDTO queryDTO) {
        return R.ok(competitionService.listCompetitions(queryDTO));
    }

    @GetMapping("/{id}")
    @OperationLog(module = "竞赛证书管理", action = "查看竞赛详情")
    public R<CompetitionDetailVO> detail(@PathVariable Long id) {
        return R.ok(competitionService.getCompetitionDetail(id));
    }

    @PostMapping("/add")
    @OperationLog(module = "竞赛证书管理", action = "新增竞赛")
    public R<Long> add(@Valid @RequestBody CompetitionAddDTO addDTO) {
        return R.ok(competitionService.addCompetition(addDTO));
    }

    @PutMapping("/update")
    @OperationLog(module = "竞赛证书管理", action = "更新竞赛")
    public R<Void> update(@Valid @RequestBody CompetitionUpdateDTO updateDTO) {
        competitionService.updateCompetition(updateDTO);
        return R.ok();
    }

    @PutMapping("/{id}/enable")
    @OperationLog(module = "竞赛证书管理", action = "启用竞赛")
    public R<Void> enable(@PathVariable Long id) {
        competitionService.enableCompetition(id);
        return R.ok();
    }

    @DeleteMapping("/soft/{id}")
    @OperationLog(module = "竞赛证书管理", action = "禁用竞赛")
    public R<Void> softDelete(@PathVariable Long id) {
        competitionService.softDeleteCompetition(id);
        return R.ok();
    }

    @DeleteMapping("/hard/{id}")
    @OperationLog(module = "竞赛证书管理", action = "删除竞赛")
    public R<Void> hardDelete(@PathVariable Long id) {
        competitionService.hardDeleteCompetition(id);
        return R.ok();
    }

    @PostMapping("/batch/delete")
    @OperationLog(module = "竞赛证书管理", action = "批量删除竞赛")
    public R<Void> batchDelete(@Valid @RequestBody BatchDeleteDTO batchDTO) {
        competitionService.batchHardDeleteCompetitions(batchDTO.getIds());
        return R.ok();
    }

    /**
     * 批量导入竞赛主表（读取 sheet「竞赛主表」：竞赛名称/竞赛级别/报名时间）。
     * 返回 ImportResultVO 供前端弹窗展示 新增/补齐 数量。
     */
    @PostMapping("/import/main")
    @OperationLog(module = "竞赛证书管理", action = "导入竞赛主表")
    public R<ImportResultVO> importMain(@RequestParam("file") MultipartFile file) {
        return R.ok(competitionService.importMain(file));
    }

    /**
     * 批量导入竞赛详情（读取 竞赛次表/基本信息/竞赛规则/参赛流程指南/奖项设置展示 共 5 个 sheet）。
     * 所有行的竞赛名称必须已存在主表，否则整批回滚并报告「第[Sheet]第X行: 竞赛名称[xxx]不存在」。
     */
    @PostMapping("/import/detail")
    @OperationLog(module = "竞赛证书管理", action = "导入竞赛详情")
    public R<ImportResultVO> importDetail(@RequestParam("file") MultipartFile file) {
        return R.ok(competitionService.importDetail(file));
    }
}
