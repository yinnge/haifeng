package com.haifeng.app.controller.member;

import com.haifeng.app.dto.member.ProfileUpdateDTO;
import com.haifeng.app.service.member.ProfileService;
import com.haifeng.app.vo.member.ProfileVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/app/member/profile")
@RequiredArgsConstructor
@RequireLogin
public class ProfileController {

    private final ProfileService profileService;

    /**
     * 获取当前用户资料
     */
    @GetMapping
    public R<ProfileVO> getProfile() {
        return R.ok(profileService.getProfile());
    }

    /**
     * 更新用户资料
     */
    @PutMapping
    public R<Void> updateProfile(@Valid @RequestBody ProfileUpdateDTO dto) {
        profileService.updateProfile(dto);
        return R.ok();
    }
}
