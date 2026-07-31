package com.haifeng.admin.vo.user;

import lombok.Data;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class NotificationListVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private Long memberId;

    private String memberName;

    private String notificationType;

    private String notificationTypeDesc;

    private String title;

    private String content;

    private Boolean isRead;

    private OffsetDateTime createdAt;

    private OffsetDateTime readAt;

    /** 接收人数（仅群发通知有值，批次总人数，含已禁用的） */
    private Integer recipientCount;

    /** 群发批次ID（仅群发通知有值），用于整批撤回/恢复 */
    private Long broadcastId;

    /** 批次内已禁用人数（仅群发通知有值） */
    private Integer disabledCount;

    /** 是否完全禁用（disabledCount == recipientCount 时为 true；部分禁用时为 false 但 disabledCount > 0） */
    private Boolean disabled;
}
