package com.haifeng.admin.dto.user;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WithdrawQueryDTO extends BasePageQueryDTO {

    private String memberName;

    private String phone;

    private String wechatId;

    private String status;
}
