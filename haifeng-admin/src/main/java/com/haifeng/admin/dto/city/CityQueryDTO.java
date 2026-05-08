package com.haifeng.admin.dto.city;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CityQueryDTO extends BasePageQueryDTO {

    /**
     * 城市名称（模糊查询）
     */
    private String cityName;

    /**
     * 省份（模糊查询）
     */
    private String province;

    /**
     * 所属地区（模糊查询）
     */
    private String region;

    /**
     * 删除状态
     */
    private Boolean isDeleted;
}
