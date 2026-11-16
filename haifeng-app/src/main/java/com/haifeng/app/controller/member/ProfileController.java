package com.haifeng.app.controller.member;

import com.haifeng.app.dto.member.ProfileUpdateDTO;
import com.haifeng.app.service.member.ProfileService;
import com.haifeng.app.vo.member.ProfileVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    /**
     * 上传自定义头像（写入头像专用公共读 Bucket，按 memberId 固定 key 覆盖更新）。
     * <p>校验：JPG / PNG、≤2MB、边长 64~2048px、宽高比 1:2~2:1。
     * 返回可直接被 C 端 {@code <img src>} 引用的完整 URL。
     */
    @PostMapping("/avatar/upload")
    public R<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return R.ok(profileService.uploadAvatar(file));
    }
}
