package com.haifeng.admin.controller.certificate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.certificate.BatchDeleteDTO;
import com.haifeng.admin.dto.certificate.CompetitionMajorAddDTO;
import com.haifeng.admin.dto.certificate.CompetitionMajorQueryDTO;
import com.haifeng.admin.service.certificate.CompetitionMajorService;
import com.haifeng.admin.vo.certificate.CompetitionMajorVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/competition-major")
@RequiredArgsConstructor
@RequireAdminModule("cert_comp_major")
public class CompetitionMajorController {

    private final CompetitionMajorService competitionMajorService;

    @GetMapping("/list")
    public R<IPage<CompetitionMajorVO>> list(@Valid CompetitionMajorQueryDTO queryDTO) {
        return R.ok(competitionMajorService.listCompetitionMajors(queryDTO));
    }

    @GetMapping("/by-competition/{competitionId}")
    public R<List<CompetitionMajorVO>> listByCompetition(@PathVariable Long competitionId) {
        return R.ok(competitionMajorService.listByCompetitionId(competitionId));
    }

    @GetMapping("/by-major/{majorId}")
    public R<List<CompetitionMajorVO>> listByMajor(@PathVariable Long majorId) {
        return R.ok(competitionMajorService.listByMajorId(majorId));
    }

    @PostMapping("/add")
    @OperationLog(module = "竞赛证书管理", action = "新增竞赛-专业关联")
    public R<Long> add(@Valid @RequestBody CompetitionMajorAddDTO addDTO) {
        return R.ok(competitionMajorService.addCompetitionMajor(addDTO));
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "竞赛证书管理", action = "删除竞赛-专业关联")
    public R<Void> delete(@PathVariable Long id) {
        competitionMajorService.deleteCompetitionMajor(id);
        return R.ok();
    }

    @PostMapping("/batch/delete")
    @OperationLog(module = "竞赛证书管理", action = "批量删除竞赛-专业关联")
    public R<Void> batchDelete(@Valid @RequestBody BatchDeleteDTO batchDTO) {
        competitionMajorService.batchDeleteCompetitionMajors(batchDTO.getIds());
        return R.ok();
    }
}
