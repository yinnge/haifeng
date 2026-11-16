package com.haifeng.app.service.member;

import com.haifeng.app.dto.member.ProfileUpdateDTO;
import com.haifeng.app.vo.member.ProfileVO;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {

    ProfileVO getProfile();

    void updateProfile(ProfileUpdateDTO dto);

    /**
     * 上传会员自定义头像（写入头像专用公共读 Bucket，按 memberId 固定 key 覆盖更新），
     * 返回可直接被前端 <img> 引用的完整 URL。
     */
    String uploadAvatar(MultipartFile file);
}
