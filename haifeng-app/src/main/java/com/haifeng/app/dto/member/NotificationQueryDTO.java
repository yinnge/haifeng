package com.haifeng.app.dto.member;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import com.haifeng.common.enums.NotificationType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationQueryDTO extends BasePageQueryDTO {

    private NotificationType notificationType;

    private Boolean isRead;
}
