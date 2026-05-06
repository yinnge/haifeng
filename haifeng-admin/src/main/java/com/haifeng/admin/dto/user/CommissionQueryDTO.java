package com.haifeng.admin.dto.user;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommissionQueryDTO extends BasePageQueryDTO {

    private String referrerPhone;

    private String referrerName;

    private String refereePhone;

    private String refereeName;

    private String orderNo;
}
