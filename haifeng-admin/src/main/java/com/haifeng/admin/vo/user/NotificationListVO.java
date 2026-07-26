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

    private String title;

    private String content;

    private Boolean isRead;

    private OffsetDateTime createdAt;

    private OffsetDateTime readAt;
}
