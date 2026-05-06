package com.haifeng.common.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import com.haifeng.common.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_member_notification")
public class MemberNotification {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long memberId;

    private NotificationType notificationType;

    private String title;

    private String content;

    private Long relatedId;

    private Boolean isRead;

    private OffsetDateTime readAt;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    private OffsetDateTime createdAt;
}
