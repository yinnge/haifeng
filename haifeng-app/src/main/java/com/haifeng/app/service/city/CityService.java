package com.haifeng.app.service.city;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.city.CityQueryDTO;
import com.haifeng.app.vo.city.CityBriefVO;
import com.haifeng.app.vo.city.CityDetailVO;
import com.haifeng.app.vo.city.CityListVO;
import com.haifeng.app.vo.city.CityRankVO;
import com.haifeng.app.vo.city.CityStatsVO;

import java.util.List;

public interface CityService {

    /**
     * 分页查询城市列表（isDeleted=false）；cityName LIKE，province/region EQ；
     * dto.sortBy 非空时按对应指标倒序（排行榜"查看全部"），否则排序 id ASC。
     * 附带 t_city_detail 的 cityLevel（无详情记录则为 null）
     */
    IPage<CityListVO> page(CityQueryDTO dto);

    /**
     * 城市排行榜：metric ∈ college|keyCollege|gdp|population（已由 Controller 校验），
     * 按指标倒序取前 limit 条（limit ∈ [1,50]）
     */
    List<CityRankVO> rank(String metric, Integer limit);

    /**
     * 城市数据概览聚合：城市总数 / 高校总数 / 常住人口合计(万人) / GDP合计(亿元)
     */
    CityStatsVO stats();

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

    /**
     * 城市简要信息：通过 cityName 查询 t_city 主表返回简要信息
     * 不存在 → BusinessException(NOT_FOUND)
     */
    CityBriefVO getBriefByName(String cityName);
    /**
     * 根据城市名称精确查询城市ID（isDeleted=false）
     * 不存在 → BusinessException(NOT_FOUND)
     */
    Long findIdByName(String name);
}
