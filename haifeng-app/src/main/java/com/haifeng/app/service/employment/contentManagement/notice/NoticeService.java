package com.haifeng.app.service.employment.contentManagement.notice;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.contentManagement.notice.NoticeQueryDTO;
import com.haifeng.app.vo.employment.contentManagement.notice.NoticeDetailVO;
import com.haifeng.app.vo.employment.contentManagement.notice.NoticeListVO;

public interface NoticeService {
    IPage<NoticeListVO> page(NoticeQueryDTO dto);
    NoticeDetailVO detail(Long id);
}
