package com.haifeng.admin.controller.certificate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.certificate.BatchDeleteDTO;
import com.haifeng.admin.dto.certificate.CompetitionAddDTO;
import com.haifeng.admin.dto.certificate.CompetitionQueryDTO;
import com.haifeng.admin.dto.certificate.CompetitionUpdateDTO;
import com.haifeng.admin.service.certificate.CompetitionService;
import com.haifeng.admin.vo.certificate.CompetitionDetailVO;
import com.haifeng.admin.vo.certificate.CompetitionListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @DeleteMapping("/soft/{id}")
    @OperationLog(module = "竞赛证书管理", action = "软删除竞赛")
    public R<Void> softDelete(@PathVariable Long id) {
        competitionService.softDeleteCompetition(id);
        return R.ok();
    }

    @DeleteMapping("/hard/{id}")
    @OperationLog(module = "竞赛证书管理", action = "硬删除竞赛")
    public R<Void> hardDelete(@PathVariable Long id) {
        competitionService.hardDeleteCompetition(id);
        return R.ok();
    }

    @PostMapping("/batch/delete")
    @OperationLog(module = "竞赛证书管理", action = "批量硬删除竞赛")
    public R<Void> batchDelete(@Valid @RequestBody BatchDeleteDTO batchDTO) {
        competitionService.batchHardDeleteCompetitions(batchDTO.getIds());
        return R.ok();
    }
}
