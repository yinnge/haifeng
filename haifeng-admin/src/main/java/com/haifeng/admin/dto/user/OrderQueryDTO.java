package com.haifeng.admin.dto.user;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderQueryDTO extends BasePageQueryDTO {

    @Size(max = 50, message = "手机号长度不能超过50")
    private String phone;

    private String wechatId;

    @Size(max = 50, message = "操作人名称长度不能超过50")
    private String operatorName;

    private String orderType;
}
