package com.haifeng.app.service.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.special.StrongBaseScoreQueryDTO;
import com.haifeng.app.vo.special.StrongBaseScoreDetailVO;
import com.haifeng.app.vo.special.StrongBaseScoreListVO;

public interface StrongBaseScoreService {
    IPage<StrongBaseScoreListVO> page(StrongBaseScoreQueryDTO dto);
    StrongBaseScoreDetailVO detail(Long id);
}
