package com.haifeng.app.controller.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.special.SpecialChannelUnivQueryDTO;
import com.haifeng.app.service.special.SpecialChannelUniversityService;
import com.haifeng.app.vo.special.SpecialChannelUnivDetailVO;
import com.haifeng.app.vo.special.SpecialChannelUnivListVO;
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
@RequestMapping("/api/v1/app/special/channel-univ")
@RequiredArgsConstructor
public class SpecialChannelUniversityController {

    private final SpecialChannelUniversityService specialChannelUniversityService;

    @GetMapping("/list")
    public R<IPage<SpecialChannelUnivListVO>> list(@Valid SpecialChannelUnivQueryDTO dto) {
        return R.ok(specialChannelUniversityService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}")
    public R<SpecialChannelUnivDetailVO> detail(@PathVariable Long id) {
        return R.ok(specialChannelUniversityService.detail(id));
    }
}
