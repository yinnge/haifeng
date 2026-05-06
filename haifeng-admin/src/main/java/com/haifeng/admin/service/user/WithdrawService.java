package com.haifeng.admin.service.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.user.WithdrawProcessDTO;
import com.haifeng.admin.dto.user.WithdrawQueryDTO;
import com.haifeng.admin.vo.user.WithdrawListVO;

public interface WithdrawService {

    IPage<WithdrawListVO> page(WithdrawQueryDTO dto);

    String getWechatPlaintext(Long id);

    void process(Long id, WithdrawProcessDTO dto);

    void delete(Long id);
}
