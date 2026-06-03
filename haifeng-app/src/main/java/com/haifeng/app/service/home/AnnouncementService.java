package com.haifeng.app.service.home;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.home.AnnouncementQueryDTO;
import com.haifeng.app.vo.home.AnnouncementDetailVO;
import com.haifeng.app.vo.home.AnnouncementListVO;

public interface AnnouncementService {

    /** 分页查询展示中的公告（status=1） */
    IPage<AnnouncementListVO> page(AnnouncementQueryDTO dto);

    /** 查询公告详情（仅 status=1，不存在抛 404） */
    AnnouncementDetailVO detail(Long id);
}
