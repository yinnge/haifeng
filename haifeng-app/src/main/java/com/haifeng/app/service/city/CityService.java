package com.haifeng.app.service.city;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.city.CityQueryDTO;
import com.haifeng.app.vo.city.CityDetailVO;
import com.haifeng.app.vo.city.CityListVO;

public interface CityService {

    /**
     * 分页查询城市列表（isDeleted=false）；cityName LIKE，province/region EQ；排序 id ASC
     */
    IPage<CityListVO> page(CityQueryDTO dto);

    /**
     * 城市详情：通过 cityId 关联 t_city_detail 查询
     * 不存在 → BusinessException(NOT_FOUND)
     */
    CityDetailVO detail(Long cityId);

    /**
     * 城市详情：通过 cityName（唯一）查出 id，再调用 detail(cityId)
     * 不存在 → BusinessException(NOT_FOUND)
     */
    CityDetailVO detailByName(String cityName);
}
