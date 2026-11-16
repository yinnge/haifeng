package com.haifeng.admin.dto.home;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlannerQueryDTO extends BasePageQueryDTO {

    /** 姓名模糊查询 */
    private String name;

    /** 服务对象筛选：高考 / 考研（可选，不传查全部） */
    private String serviceTarget;

    /** 所属省份筛选（可选，ProvinceEnum 裸值） */
    private String province;
}
