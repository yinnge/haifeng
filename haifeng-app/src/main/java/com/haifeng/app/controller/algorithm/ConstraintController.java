package com.haifeng.app.controller.algorithm;

import com.haifeng.app.dto.algorithm.CheckGroupDTO;
import com.haifeng.app.dto.algorithm.ConstraintCodesDTO;
import com.haifeng.app.service.algorithm.ConstraintService;
import com.haifeng.app.vo.algorithm.CheckGroupResultVO;
import com.haifeng.app.vo.algorithm.ConstraintDetailsVO;
import com.haifeng.app.vo.algorithm.ConstraintMatchVO;
import com.haifeng.common.annotation.RateLimit;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 约束匹配控制器
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/constraint")
@RequiredArgsConstructor
@RequireLogin
public class ConstraintController {

    private final ConstraintService constraintService;

    /**
     * 获取当前用户触发的约束列表
     */
    @RateLimit(value = 30, time = 60)
    @GetMapping("/match")
    public R<ConstraintMatchVO> matchConstraints() {
        return R.ok(constraintService.matchConstraints());
    }

    /**
     * 根据约束代码列表获取约束详情
     */
    @RateLimit(value = 20, time = 60)
    @PostMapping("/details")
    public R<ConstraintDetailsVO> getConstraintDetails(@Valid @RequestBody ConstraintCodesDTO dto) {
        return R.ok(constraintService.getConstraintDetails(dto.getCodes()));
    }

    /**
     * 校验当前用户是否满足专业组约束
     */
    @RateLimit(value = 20, time = 60)
    @PostMapping("/check-group")
    public R<CheckGroupResultVO> checkGroupConstraints(@Valid @RequestBody CheckGroupDTO dto) {
        return R.ok(constraintService.checkGroupConstraints(dto.getGroupId()));
    }
}
