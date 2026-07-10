package com.haifeng.app.service.member;

import com.haifeng.app.dto.member.AvatarUpdateDTO;
import com.haifeng.app.dto.member.MemberInfoUpdateDTO;
import com.haifeng.app.dto.member.PasswordUpdateDTO;
import com.haifeng.app.dto.member.WechatUpdateDTO;
import com.haifeng.app.vo.member.MemberInfoVO;

public interface MemberInfoService {

    MemberInfoVO getInfo();

    void updateInfo(MemberInfoUpdateDTO dto);

    String getWechat();

    void updateWechat(WechatUpdateDTO dto);

    void updatePassword(PasswordUpdateDTO dto);

    void updateAvatar(AvatarUpdateDTO dto);
}
