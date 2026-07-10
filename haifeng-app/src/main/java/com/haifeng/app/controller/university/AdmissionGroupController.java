package com.haifeng.app.controller.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.AdmissionGroupQueryDTO;
import com.haifeng.app.service.university.AdmissionGroupService;
import com.haifeng.app.vo.university.AdmissionGroupDetailVO;
import com.haifeng.app.vo.university.AdmissionGroupListVO;
import com.haifeng.app.vo.university.AdmissionMajorScoreListVO;
import com.haifeng.common.annotation.RequireVip;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/app/university/admission-group")
@RequiredArgsConstructor
public class AdmissionGroupController {

    private final AdmissionGroupService admissionGroupService;

    @RequireVip
    @GetMapping("/{universityId}")
    public R<IPage<AdmissionGroupListVO>> page(
            @PathVariable @Min(value = 1, message = "ID必须大于0") Long universityId,
            @Valid AdmissionGroupQueryDTO dto) {
        return R.ok(admissionGroupService.pageByUniversity(universityId, dto));
    }

    @RequireVip
    @GetMapping("/{groupId}/scores")
    public R<List<AdmissionMajorScoreListVO>> scores(@PathVariable @Min(value = 1, message = "ID必须大于0") Long groupId) {
        return R.ok(admissionGroupService.listScores(groupId));
    }

    @RequireVip
    @GetMapping("/{groupId}/detail")
    public R<AdmissionGroupDetailVO> detail(@PathVariable @Min(value = 1, message = "ID必须大于0") Long groupId) {
        return R.ok(admissionGroupService.getDetail(groupId));
    }
}
