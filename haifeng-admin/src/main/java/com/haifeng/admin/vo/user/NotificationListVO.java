package com.haifeng.admin.vo.user;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class NotificationListVO {

    private Long id;

    private Long memberId;

    private String memberName;

    private String notificationType;

    private String title;

    private String content;

    private Boolean isRead;

    private OffsetDateTime createdAt;

    private OffsetDateTime readAt;
}
