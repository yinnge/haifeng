package com.haifeng.admin.dto.permission;

import com.haifeng.common.dto.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleQueryDTO extends BasePageQueryDTO {

    private String roleName;
}
