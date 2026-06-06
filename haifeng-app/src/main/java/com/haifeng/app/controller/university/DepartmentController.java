package com.haifeng.app.controller.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.DepartmentQueryDTO;
import com.haifeng.app.service.university.DepartmentService;
import com.haifeng.app.vo.university.DepartmentListVO;
import com.haifeng.app.vo.university.DepartmentReportVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端院系列表 / 院系分析报告（spec §3.3、§3.4）
 * 均需登录
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/university")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /** spec §3.3：按 universityId 分页查询院系列表 */
    @RequireLogin
    @GetMapping("/{universityId}/departments")
    public R<IPage<DepartmentListVO>> list(
            @PathVariable Long universityId,
            @Valid DepartmentQueryDTO dto) {
        return R.ok(departmentService.page(universityId, dto));
    }

    /** spec §3.4：按院系 id 查询其分析报告 */
    @RequireLogin
    @GetMapping("/departments/{departmentId}/report")
    public R<DepartmentReportVO> report(@PathVariable Long departmentId) {
        return R.ok(departmentService.report(departmentId));
    }
}
