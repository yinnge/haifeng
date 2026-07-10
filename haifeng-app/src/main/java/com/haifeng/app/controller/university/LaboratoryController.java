package com.haifeng.app.controller.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.LaboratoryQueryDTO;
import com.haifeng.app.service.university.LaboratoryService;
import com.haifeng.app.vo.university.LaboratoryDetailVO;
import com.haifeng.app.vo.university.LaboratoryListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端实验室列表 / 详情（spec §3.1、§3.2）
 * 均需登录
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/university")
@RequiredArgsConstructor
public class LaboratoryController {

    private final LaboratoryService laboratoryService;

    /** spec §3.1：按 universityId 分页查询实验室列表 */
    @RequireLogin
    @GetMapping("/{universityId}/laboratories")
    public R<IPage<LaboratoryListVO>> list(
            @PathVariable @Min(value = 1, message = "ID必须大于0") Long universityId,
            @Valid LaboratoryQueryDTO dto) {
        return R.ok(laboratoryService.page(universityId, dto));
    }

    /** spec §3.2：按主键查询实验室详情 */
    @RequireLogin
    @GetMapping("/laboratories/{labId}")
    public R<LaboratoryDetailVO> detail(
            @PathVariable @Min(value = 1, message = "ID必须大于0") Long labId) {
        return R.ok(laboratoryService.detail(labId));
    }
}
