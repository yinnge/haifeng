package com.haifeng.app.dto.home;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlannerQueryDTO extends BasePageQueryDTO {

    /** 所在地区，必须是 ProvinceEnum 中文 desc（可选） */
    private String region;
}
