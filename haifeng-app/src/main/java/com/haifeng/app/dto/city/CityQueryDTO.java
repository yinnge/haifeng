package com.haifeng.app.dto.city;

import com.haifeng.common.dto.common.BasePageQueryDTO;
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
    private String cityName;

    /** 省份精准匹配 */
    private String province;

    /** 地区精准匹配 */
    private String region;
}
