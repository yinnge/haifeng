package com.haifeng.admin.dto.user;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderQueryDTO extends BasePageQueryDTO {

    private String phone;

    private String wechatId;

    private String operatorName;

    private String orderType;
}
