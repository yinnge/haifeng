package com.haifeng.app.dto.city;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Pattern;
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

    /**
     * 排序字段（排行榜"查看全部"跳转用），白名单校验防注入：
     * college=高校数量 / keyCollege=重点院校 / gdp=GDP / population=人口，均为倒序；空则默认按 id 正序
     */
    @Pattern(regexp = "college|keyCollege|gdp|population", message = "排序字段不合法")
    private String sortBy;
}
