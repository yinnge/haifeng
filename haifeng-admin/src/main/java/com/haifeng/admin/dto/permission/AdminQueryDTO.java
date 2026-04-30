package com.haifeng.admin.dto.permission;

import com.haifeng.common.dto.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminQueryDTO extends BasePageQueryDTO {

    private String username;

    private String phone;

    private String realName;

    private Integer status;
}
