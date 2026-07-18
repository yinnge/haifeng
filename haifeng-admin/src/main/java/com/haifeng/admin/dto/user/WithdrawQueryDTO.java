package com.haifeng.admin.dto.user;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WithdrawQueryDTO extends BasePageQueryDTO {

    @Size(max = 50, message = "成员名称长度不能超过50")
    private String memberName;

    @Size(max = 50, message = "手机号长度不能超过50")
    private String phone;

    @Size(max = 100, message = "微信ID长度不能超过100")
    private String wechatId;

    @Size(max = 20, message = "状态长度不能超过20")
    private String status;
}
