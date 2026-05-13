package com.haifeng.app.controller.member;

import com.haifeng.app.dto.member.MemberInfoUpdateDTO;
import com.haifeng.app.dto.member.PasswordUpdateDTO;
import com.haifeng.app.dto.member.WechatUpdateDTO;
import com.haifeng.app.service.member.MemberInfoService;
import com.haifeng.app.vo.member.MemberInfoVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/app/member")
@RequiredArgsConstructor
@RequireLogin
public class MemberInfoController {

    private final MemberInfoService memberInfoService;

    @GetMapping("/info")
    public R<MemberInfoVO> getInfo() {
        return R.ok(memberInfoService.getInfo());
    }

    @PutMapping("/info")
    public R<Void> updateInfo(@Valid @RequestBody MemberInfoUpdateDTO dto) {
        memberInfoService.updateInfo(dto);
        return R.ok();
    }

    @GetMapping("/wechat")
    public R<String> getWechat() {
        return R.ok(memberInfoService.getWechat());
    }

    @PutMapping("/wechat")
    public R<Void> updateWechat(@Valid @RequestBody WechatUpdateDTO dto) {
        memberInfoService.updateWechat(dto);
        return R.ok();
    }

    @PutMapping("/password")
    public R<Void> updatePassword(@Valid @RequestBody PasswordUpdateDTO dto) {
        memberInfoService.updatePassword(dto);
        return R.ok();
    }

    @PutMapping("/avatar")
    public R<Void> updateAvatar(@RequestBody Map<String, String> body) {
        String avatar = body.get("avatar");
        memberInfoService.updateAvatar(avatar);
        return R.ok();
    }
}
