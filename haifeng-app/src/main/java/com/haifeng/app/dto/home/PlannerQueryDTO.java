package com.haifeng.app.dto.home;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlannerQueryDTO extends BasePageQueryDTO {

    /** 所在地区，必须是 ProvinceEnum 中文 desc（可选） */
    @Size(max = 50, message = "地区最长50个字符")
    private String region;
}
