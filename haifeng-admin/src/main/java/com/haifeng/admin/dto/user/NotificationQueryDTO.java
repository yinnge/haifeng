package com.haifeng.admin.dto.user;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationQueryDTO extends BasePageQueryDTO {

    @Size(max = 50, message = "通知类型长度不能超过50")
    private String notificationType;

    private Long memberId;

    private Boolean isRead;
}
