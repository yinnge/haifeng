package com.haifeng.common.vo.user;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 通知记录 VO（不含 @TableLogic，用于 showDisabled 场景）
 */
@Data
public class NotificationRecordVO {

    private Long id;

    private Long memberId;

    private String notificationType;

    private String title;

    private String content;

    private Boolean isRead;

    private OffsetDateTime createdAt;

    private OffsetDateTime readAt;

    /** 对应 is_deleted 字段，不含 @TableLogic 干扰 */
    private Boolean disabled;
}
