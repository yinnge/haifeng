package com.haifeng.app.controller.member;

import com.haifeng.app.dto.member.ReferrerBindDTO;
import com.haifeng.app.dto.member.WithdrawDTO;
import com.haifeng.app.service.member.CommissionService;
import com.haifeng.app.vo.member.CommissionVO;
import com.haifeng.app.vo.member.ReferrerPreviewVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/app/member")
@RequiredArgsConstructor
@RequireLogin
public class CommissionController {

    private final CommissionService commissionService;

    @GetMapping("/commission")
    public R<CommissionVO> getCommission() {
        return R.ok(commissionService.getCommission());
    }

    @PostMapping("/withdraw")
    public R<Long> withdraw(@Valid @RequestBody WithdrawDTO dto) {
        return R.ok(commissionService.withdraw(dto));
    }

    @GetMapping("/referrer/preview")
    public R<ReferrerPreviewVO> previewReferrer(@RequestParam String inviteCode) {
        return R.ok(commissionService.previewReferrer(inviteCode));
    }

    @PostMapping("/referrer/bind")
    public R<Void> bindReferrer(@Valid @RequestBody ReferrerBindDTO dto) {
        commissionService.bindReferrer(dto);
        return R.ok();
    }
}
