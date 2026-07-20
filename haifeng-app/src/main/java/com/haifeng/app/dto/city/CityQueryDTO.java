package com.haifeng.app.dto.city;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * C 端城市列表查询 DTO
 * cityName 走 LIKE，province / region 精准匹配（AND 组合）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CityQueryDTO extends BasePageQueryDTO {

    /** 城市名称模糊（LIKE %cityName%） */
    @Size(max = 50, message = "城市名称长度不能超过50")
    private String cityName;

    /** 省份精准匹配 */
    @Size(max = 20, message = "省份长度不能超过20")
    private String province;

    /** 地区精准匹配 */
    @Size(max = 20, message = "地区长度不能超过20")
    private String region;
}
