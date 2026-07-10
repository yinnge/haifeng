package com.haifeng.app.controller.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.grassrootsPosition.CommunityPositionSearchDTO;
import com.haifeng.app.service.employment.grassrootsPosition.CommunityPositionService;
import com.haifeng.app.vo.employment.grassrootsPosition.CommunityPositionDetailVO;
import com.haifeng.app.vo.employment.grassrootsPosition.CommunityPositionListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/app/employment/grassroots/community")
@RequiredArgsConstructor
public class CommunityPositionController {

    private final CommunityPositionService communityPositionService;

    @GetMapping("/list")
    public R<IPage<CommunityPositionListVO>> list(@Valid CommunityPositionSearchDTO dto) {
        return R.ok(communityPositionService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<CommunityPositionDetailVO> detail(@PathVariable @Min(value = 1, message = "ID必须大于0") Long id) {
        return R.ok(communityPositionService.detail(id));
    }
}
