package com.haifeng.app.service.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.grassrootsPosition.CommunityPositionSearchDTO;
import com.haifeng.app.vo.employment.grassrootsPosition.CommunityPositionDetailVO;
import com.haifeng.app.vo.employment.grassrootsPosition.CommunityPositionListVO;

public interface CommunityPositionService {
    IPage<CommunityPositionListVO> page(CommunityPositionSearchDTO dto);
    CommunityPositionDetailVO detail(Long id);
}
