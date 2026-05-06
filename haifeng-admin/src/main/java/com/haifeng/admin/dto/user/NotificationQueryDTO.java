package com.haifeng.admin.dto.user;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationQueryDTO extends BasePageQueryDTO {

    private String notificationType;

    private Long memberId;

    private Boolean isRead;
}
