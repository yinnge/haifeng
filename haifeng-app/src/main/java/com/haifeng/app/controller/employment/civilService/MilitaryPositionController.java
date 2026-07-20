package com.haifeng.app.controller.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.civilService.MilitaryPositionSearchDTO;
import com.haifeng.app.service.employment.civilService.MilitaryPositionService;
import com.haifeng.app.vo.employment.civilService.MilitaryPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.MilitaryPositionListVO;
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
@RequestMapping("/api/v1/app/employment/civil-service/military")
@RequiredArgsConstructor
public class MilitaryPositionController {

    private final MilitaryPositionService militaryPositionService;

    @GetMapping("/list")
    public R<IPage<MilitaryPositionListVO>> list(@Valid MilitaryPositionSearchDTO dto) {
        return R.ok(militaryPositionService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<MilitaryPositionDetailVO> detail(@PathVariable @Min(value = 1, message = "ID必须大于0") Long id) {
        return R.ok(militaryPositionService.detail(id));
    }
}
