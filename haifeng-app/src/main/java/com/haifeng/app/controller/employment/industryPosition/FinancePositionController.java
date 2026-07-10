package com.haifeng.app.controller.employment.industryPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.industryPosition.FinancePositionSearchDTO;
import com.haifeng.app.service.employment.industryPosition.FinancePositionService;
import com.haifeng.app.vo.employment.industryPosition.FinancePositionDetailVO;
import com.haifeng.app.vo.employment.industryPosition.FinancePositionListVO;
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

@Validated
@RestController
@RequestMapping("/api/v1/app/employment/finance")
@RequiredArgsConstructor
public class FinancePositionController {

    private final FinancePositionService financePositionService;

    @GetMapping("/list")
    public R<IPage<FinancePositionListVO>> list(@Valid FinancePositionSearchDTO dto) {
        return R.ok(financePositionService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<FinancePositionDetailVO> detail(@PathVariable @Min(value = 1, message = "ID必须大于0") Long id) {
        return R.ok(financePositionService.detail(id));
    }
}
