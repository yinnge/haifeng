package com.haifeng.admin.dto.home;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class InstitutionQueryDTO extends BasePageQueryDTO {
    private String name;   // 名称模糊查询
    private String type;   // 类型筛选
    private Short status;  // 状态筛选: 0-下架 1-展示
}
