package com.haifeng.app.service.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.special.SpecialChannelQueryDTO;
import com.haifeng.app.vo.special.SpecialChannelDetailVO;
import com.haifeng.app.vo.special.SpecialChannelListVO;

public interface SpecialChannelService {
    IPage<SpecialChannelListVO> page(SpecialChannelQueryDTO dto);
    SpecialChannelDetailVO detail(Long id);
}
