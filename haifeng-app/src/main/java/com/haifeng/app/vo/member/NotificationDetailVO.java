package com.haifeng.app.vo.member;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDetailVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String notificationType;

    private String notificationTypeDesc;

    private String title;

    private String content;

    private Boolean isRead;

    private OffsetDateTime createdAt;

    private OffsetDateTime readAt;
}
