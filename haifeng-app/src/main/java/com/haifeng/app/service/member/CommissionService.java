package com.haifeng.app.service.member;

import com.haifeng.app.dto.member.ReferrerBindDTO;
import com.haifeng.app.dto.member.WithdrawDTO;
import com.haifeng.app.vo.member.CommissionVO;
import com.haifeng.app.vo.member.ReferrerPreviewVO;

public interface CommissionService {

    CommissionVO getCommission();

    Long withdraw(WithdrawDTO dto);

    ReferrerPreviewVO previewReferrer(String inviteCode);

    void bindReferrer(ReferrerBindDTO dto);
}
