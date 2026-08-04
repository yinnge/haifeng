package com.haifeng.app.service.employment.contentManagement.notice;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.contentManagement.notice.NoticeQueryDTO;
import com.haifeng.app.vo.employment.contentManagement.notice.NoticeDetailVO;

import java.util.List;

public interface NoticeService {
    List<NoticeDetailVO> listByCategoryAndType(String noticeCategory, String noticeType);

    IPage<NoticeDetailVO> page(NoticeQueryDTO dto);

    NoticeDetailVO detail(Long id);
}
