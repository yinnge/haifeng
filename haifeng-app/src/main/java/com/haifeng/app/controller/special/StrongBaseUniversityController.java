package com.haifeng.app.controller.special;

import com.haifeng.app.service.special.StrongBaseUniversityService;
import com.haifeng.app.vo.special.StrongBaseUniversityDetailVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/app/special/strong-base-univ")
@RequiredArgsConstructor
public class StrongBaseUniversityController {

    private final StrongBaseUniversityService strongBaseUniversityService;

    @RequireLogin
    @GetMapping("/{universityId}")
    public R<StrongBaseUniversityDetailVO> detailByUniversityId(@PathVariable @Min(value = 1, message = "ID必须大于0") Long universityId) {
        return R.ok(strongBaseUniversityService.detailByUniversityId(universityId));
    }
}
