package com.haifeng.app.controller.employment.industryPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.industryPosition.HealthcarePositionSearchDTO;
import com.haifeng.app.service.employment.industryPosition.HealthcarePositionService;
import com.haifeng.app.vo.employment.industryPosition.HealthcarePositionDetailVO;
import com.haifeng.app.vo.employment.industryPosition.HealthcarePositionListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/app/employment/healthcare")
@RequiredArgsConstructor
public class HealthcarePositionController {

    private final HealthcarePositionService healthcarePositionService;

    @GetMapping("/list")
    public R<IPage<HealthcarePositionListVO>> list(@Valid HealthcarePositionSearchDTO dto) {
        return R.ok(healthcarePositionService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<HealthcarePositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(healthcarePositionService.detail(id));
    }
}
