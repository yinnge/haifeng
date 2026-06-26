package com.haifeng.app.service.employment.contentManagement.notice;

import com.haifeng.app.vo.employment.contentManagement.notice.NoticeDetailVO;

import java.util.List;

public interface NoticeService {
    List<NoticeDetailVO> listByCategoryAndType(String noticeCategory, String noticeType);
}
