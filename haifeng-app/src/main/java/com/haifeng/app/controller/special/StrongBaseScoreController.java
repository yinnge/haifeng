package com.haifeng.app.controller.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.special.StrongBaseScoreQueryDTO;
import com.haifeng.app.service.special.StrongBaseScoreService;
import com.haifeng.app.vo.special.StrongBaseScoreDetailVO;
import com.haifeng.app.vo.special.StrongBaseScoreListVO;
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
@RequestMapping("/api/v1/app/special/strong-base-score")
@RequiredArgsConstructor
public class StrongBaseScoreController {

    private final StrongBaseScoreService strongBaseScoreService;

    @GetMapping("/list")
    public R<IPage<StrongBaseScoreListVO>> list(@Valid StrongBaseScoreQueryDTO dto) {
        return R.ok(strongBaseScoreService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}")
    public R<StrongBaseScoreDetailVO> detail(@PathVariable @Min(value = 1, message = "ID必须大于0") Long id) {
        return R.ok(strongBaseScoreService.detail(id));
    }
}
