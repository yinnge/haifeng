package com.haifeng.app.service.member;

import com.haifeng.app.dto.member.ProfileUpdateDTO;
import com.haifeng.app.vo.member.ProfileVO;

public interface ProfileService {

    ProfileVO getProfile();

    void updateProfile(ProfileUpdateDTO dto);
}
