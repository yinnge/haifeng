package com.haifeng.app.controller.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.special.SpecialChannelQueryDTO;
import com.haifeng.app.service.special.SpecialChannelService;
import com.haifeng.app.vo.special.SpecialChannelDetailVO;
import com.haifeng.app.vo.special.SpecialChannelListVO;
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
@RequestMapping("/api/v1/app/special/channel")
@RequiredArgsConstructor
public class SpecialChannelController {

    private final SpecialChannelService specialChannelService;

    @GetMapping("/list")
    public R<IPage<SpecialChannelListVO>> list(@Valid SpecialChannelQueryDTO dto) {
        return R.ok(specialChannelService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}")
    public R<SpecialChannelDetailVO> detail(@PathVariable Long id) {
        return R.ok(specialChannelService.detail(id));
    }
}
