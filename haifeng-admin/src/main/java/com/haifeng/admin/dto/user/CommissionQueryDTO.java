package com.haifeng.admin.dto.user;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommissionQueryDTO extends BasePageQueryDTO {

    @Size(max = 20, message = "推荐人手机号长度不能超过20")
    private String referrerPhone;

    @Size(max = 50, message = "推荐人名称长度不能超过50")
    private String referrerName;

    @Size(max = 20, message = "被推荐人手机号长度不能超过20")
    private String refereePhone;

    @Size(max = 50, message = "被推荐人名称长度不能超过50")
    private String refereeName;

    @Size(max = 50, message = "订单号长度不能超过50")
    private String orderNo;
}
