package com.haifeng.app.service.member;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.member.NotificationQueryDTO;
import com.haifeng.app.vo.member.NotificationDetailVO;
import com.haifeng.app.vo.member.NotificationListVO;
import com.haifeng.app.vo.member.UnreadCountVO;

import java.util.List;

public interface NotificationService {

    List<NotificationListVO> recent();

    IPage<NotificationListVO> page(NotificationQueryDTO dto);

    NotificationDetailVO detail(Long notificationId);

    UnreadCountVO getUnreadCount();

    void markAsRead(Long notificationId);

    void markAllAsRead();

    void markOthersAsRead();

    void delete(Long notificationId);

    int cleanExpired();
}
