package com.haifeng.app.controller.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.civilService.CivilPositionSearchDTO;
import com.haifeng.app.service.employment.civilService.CivilPositionService;
import com.haifeng.app.vo.employment.civilService.CivilPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.CivilPositionListVO;
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
@RequestMapping("/api/v1/app/employment/civil-service/position")
@RequiredArgsConstructor
public class CivilPositionController {

    private final CivilPositionService civilPositionService;

    @GetMapping("/list")
    public R<IPage<CivilPositionListVO>> list(@Valid CivilPositionSearchDTO dto) {
        return R.ok(civilPositionService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<CivilPositionDetailVO> detail(@PathVariable @Min(value = 1, message = "ID必须大于0") Long id) {
        return R.ok(civilPositionService.detail(id));
    }
}
