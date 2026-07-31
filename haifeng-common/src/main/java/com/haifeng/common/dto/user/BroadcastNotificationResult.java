package com.haifeng.common.dto.user;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 群发通知分组查询结果
 */
@Data
public class BroadcastNotificationResult {

    private Long id;

    private Long broadcastId;

    private String notificationType;

    private String title;

    private String content;

    private OffsetDateTime createdAt;

    /** 批次总人数（含已禁用的） */
    private Integer recipientCount;

    /** 批次内已禁用人数 */
    private Integer disabledCount;
}
