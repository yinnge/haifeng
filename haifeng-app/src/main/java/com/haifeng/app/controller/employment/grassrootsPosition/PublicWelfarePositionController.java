package com.haifeng.app.controller.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.grassrootsPosition.PublicWelfarePositionSearchDTO;
import com.haifeng.app.service.employment.grassrootsPosition.PublicWelfarePositionService;
import com.haifeng.app.vo.employment.grassrootsPosition.PublicWelfarePositionDetailVO;
import com.haifeng.app.vo.employment.grassrootsPosition.PublicWelfarePositionListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/app/employment/grassroots/welfare")
@RequiredArgsConstructor
public class PublicWelfarePositionController {

    private final PublicWelfarePositionService publicWelfarePositionService;

    @GetMapping("/list")
    public R<IPage<PublicWelfarePositionListVO>> list(@Valid PublicWelfarePositionSearchDTO dto) {
        return R.ok(publicWelfarePositionService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<PublicWelfarePositionDetailVO> detail(@PathVariable @Min(value = 1, message = "ID必须大于0") Long id) {
        return R.ok(publicWelfarePositionService.detail(id));
    }
}
