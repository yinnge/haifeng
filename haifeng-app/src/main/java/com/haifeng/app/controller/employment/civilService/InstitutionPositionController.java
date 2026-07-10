package com.haifeng.app.controller.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.civilService.InstitutionPositionSearchDTO;
import com.haifeng.app.service.employment.civilService.InstitutionPositionService;
import com.haifeng.app.vo.employment.civilService.InstitutionPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.InstitutionPositionListVO;
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
@RequestMapping("/api/v1/app/employment/civil-service/institution")
@RequiredArgsConstructor
public class InstitutionPositionController {

    private final InstitutionPositionService institutionPositionService;

    @GetMapping("/list")
    public R<IPage<InstitutionPositionListVO>> list(@Valid InstitutionPositionSearchDTO dto) {
        return R.ok(institutionPositionService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<InstitutionPositionDetailVO> detail(@PathVariable @Min(value = 1, message = "ID必须大于0") Long id) {
        return R.ok(institutionPositionService.detail(id));
    }
}
