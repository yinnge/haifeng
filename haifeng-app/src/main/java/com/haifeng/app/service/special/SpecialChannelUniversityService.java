package com.haifeng.app.service.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.special.SpecialChannelUnivQueryDTO;
import com.haifeng.app.vo.special.SpecialChannelUnivDetailVO;
import com.haifeng.app.vo.special.SpecialChannelUnivListVO;

public interface SpecialChannelUniversityService {
    IPage<SpecialChannelUnivListVO> page(SpecialChannelUnivQueryDTO dto);
    SpecialChannelUnivDetailVO detail(Long id);
}
