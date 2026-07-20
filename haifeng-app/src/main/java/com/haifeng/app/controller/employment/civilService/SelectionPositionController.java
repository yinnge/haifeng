package com.haifeng.app.controller.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.civilService.SelectionPositionSearchDTO;
import com.haifeng.app.service.employment.civilService.SelectionPositionService;
import com.haifeng.app.vo.employment.civilService.SelectionPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.SelectionPositionListVO;
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
@RequestMapping("/api/v1/app/employment/civil-service/selection")
@RequiredArgsConstructor
public class SelectionPositionController {

    private final SelectionPositionService selectionPositionService;

    @GetMapping("/list")
    public R<IPage<SelectionPositionListVO>> list(@Valid SelectionPositionSearchDTO dto) {
        return R.ok(selectionPositionService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<SelectionPositionDetailVO> detail(@PathVariable @Min(value = 1, message = "ID必须大于0") Long id) {
        return R.ok(selectionPositionService.detail(id));
    }
}
