package com.haifeng.admin.service.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.user.NotificationBroadcastDTO;
import com.haifeng.admin.dto.user.NotificationQueryDTO;
import com.haifeng.admin.vo.user.NotificationListVO;
import com.haifeng.common.enums.NotificationType;

public interface NotificationService {

    IPage<NotificationListVO> page(NotificationQueryDTO dto);

    void broadcast(NotificationBroadcastDTO dto);

    void delete(Long id);

    void sendNotification(Long memberId, NotificationType type, String title, String content, Long relatedId);

    void hardDelete(Long id);

    void restore(Long id);
}
