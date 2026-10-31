package com.haifeng.app.service.impl.city;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.city.CityQueryDTO;
import com.haifeng.app.service.city.CityService;
import com.haifeng.app.vo.city.CityBriefVO;
import com.haifeng.app.vo.city.CityDetailVO;
import com.haifeng.app.vo.city.CityListVO;
import com.haifeng.app.vo.city.CityRankVO;
import com.haifeng.app.vo.city.CityStatsVO;
import com.haifeng.common.entity.city.City;
import com.haifeng.common.entity.city.CityDetail;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.city.CityDetailMapper;
import com.haifeng.common.mapper.city.CityMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.ProvinceNameUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

    private final CityMapper cityMapper;
    private final CityDetailMapper cityDetailMapper;

    /** 排行榜指标 → 排序列白名单（DTO @Pattern 已做第一层校验，此处兜底） */
    private static final Map<String, SFunction<City, ?>> RANK_COLUMNS = Map.of(
            "college", City::getCollegeCount,
            "keyCollege", City::getKeyCollegeCount,
            "gdp", City::getGdp,
            "population", City::getResidentPopulation);

    private static final int RANK_LIMIT_MAX = 50;
    private static final int RANK_LIMIT_DEFAULT = 10;

    @Override
    public IPage<CityListVO> page(CityQueryDTO dto) {
        Page<City> page = new Page<>(dto.getPage(), dto.getSize());

        // 省份兼容匹配：stripSuffix(null) 返回 null，
        // 以「剥离结果是否为 null」作为 apply 的条件，避免空参拼接 SQL。
        String province = StringUtils.hasText(dto.getProvince())
                ? ProvinceNameUtil.stripSuffix(dto.getProvince())
                : null;

        LambdaQueryWrapper<City> wrapper = new LambdaQueryWrapper<City>()
                .eq(City::getIsDeleted, false)
                .like(StringUtils.hasText(dto.getCityName()), City::getCityName, dto.getCityName())
                .apply(province != null,
                        ProvinceNameUtil.strippedColumnEquals("province"),
                        province)
                .eq(StringUtils.hasText(dto.getRegion()), City::getRegion, dto.getRegion());

        // 排行榜"查看全部"：白名单指标倒序；默认 id 正序
        applySort(wrapper, dto.getSortBy());

        IPage<City> entityPage = cityMapper.selectPage(page, wrapper);
        Map<Long, String> levelMap = loadCityLevels(entityPage.getRecords());
        return entityPage.convert(e -> toListVO(e, levelMap.get(e.getId())));
    }

    @Override
    public List<CityRankVO> rank(String metric, Integer limit) {
        SFunction<City, ?> column = RANK_COLUMNS.get(metric);
        if (column == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "排行榜指标不合法");
        }
        int n = limit == null ? RANK_LIMIT_DEFAULT : Math.min(limit, RANK_LIMIT_MAX);
        n = Math.max(1, n);

        List<City> cities = cityMapper.selectList(new LambdaQueryWrapper<City>()
                .select(City::getId, City::getCityName, City::getProvince, column)
                .eq(City::getIsDeleted, false)
                .orderByDesc(column)
                .last("LIMIT " + n));

        List<CityRankVO> result = new ArrayList<>(cities.size());
        int rank = 1;
        for (City c : cities) {
            Object v = column.apply(c);
            result.add(CityRankVO.builder()
                    .rank(rank++)
                    .id(c.getId())
                    .cityName(c.getCityName())
                    .province(c.getProvince())
                    .value(v == null ? null : new BigDecimal(v.toString()))
                    .build());
        }
        return result;
    }

    @Override
    public CityStatsVO stats() {
        Map<String, Object> row = cityMapper.selectOverviewStats();
        if (row == null) {
            return CityStatsVO.builder()
                    .cityCount(0L).collegeTotal(0L)
                    .populationTotal(BigDecimal.ZERO).gdpTotal(BigDecimal.ZERO)
                    .build();
        }
        return CityStatsVO.builder()
                .cityCount(asLong(row.get("cityCount")))
                .collegeTotal(asLong(row.get("collegeTotal")))
                .populationTotal(asBigDecimal(row.get("populationTotal")))
                .gdpTotal(asBigDecimal(row.get("gdpTotal")))
                .build();
    }

    @Override
    public Long findIdByName(String name) {
        City city = resolveCityByName(name);
        if (city == null) {
            log.debug("城市不存在, name={}", name);
            throw new BusinessException(ResultCode.NOT_FOUND, "城市不存在");
        }
        return city.getId();
    }

    @Override
    public CityDetailVO detail(Long cityId) {
        City city = cityMapper.selectById(cityId);
        if (city == null || Boolean.TRUE.equals(city.getIsDeleted())) {
            log.debug("城市不存在或已删除, cityId={}", cityId);
            throw new BusinessException(ResultCode.NOT_FOUND, "城市详情不存在");
        }

        CityDetail detail = cityDetailMapper.findByCityId(cityId);
        if (detail == null) {
            log.debug("城市详情不存在, cityId={}", cityId);
            throw new BusinessException(ResultCode.NOT_FOUND, "城市详情不存在");
        }

        return CityDetailVO.builder()
                .cityName(detail.getCityName())
                .area(detail.getArea())
                .subtitle(detail.getSubtitle())
                .cityLevel(detail.getCityLevel())
                .adminCode(detail.getAdminCode())
                .perCapitaGdp(detail.getPerCapitaGdp())
                .urbanizationRate(detail.getUrbanizationRate())
                .ruralPopRatio(detail.getRuralPopRatio())
                .agingRate(detail.getAgingRate())
                .migrantPopRatio(detail.getMigrantPopRatio())
                .gdpGrowthRate(detail.getGdpGrowthRate())
                .fortune500Count(detail.getFortune500Count())
                .industryStructure(detail.getIndustryStructure())
                .industryDescription(detail.getIndustryDescription())
                .mainIndustries(detail.getMainIndustries())
                .emergingIndustries(detail.getEmergingIndustries())
                .futurePlan(detail.getFuturePlan())
                .highEducation(detail.getHighEducation())
                .basicEducation(detail.getBasicEducation())
                .enterpriseStats(detail.getEnterpriseStats())
                .housingPriceLevel(detail.getHousingPriceLevel())
                .rentalCost(detail.getRentalCost())
                .housingPolicy(detail.getHousingPolicy())
                .consumption(detail.getConsumption())
                .employment(detail.getEmployment())
                .transportation(detail.getTransportation())
                .medical(detail.getMedical())
                .culture(detail.getCulture())
                .build();
    }

    @Override
    public CityDetailVO detailByName(String cityName) {
        City city = resolveCityByName(cityName);
        if (city == null) {
            log.debug("城市不存在, cityName={}", cityName);
            throw new BusinessException(ResultCode.NOT_FOUND, "城市不存在");
        }
        return detail(city.getId());
    }

    @Override
    public CityBriefVO getBriefByName(String cityName) {
        City city = resolveCityByName(cityName);
        if (city == null) {
            log.debug("城市不存在, cityName={}", cityName);
            throw new BusinessException(ResultCode.NOT_FOUND, "城市不存在");
        }
        return CityBriefVO.builder()
                .id(city.getId())
                .cityName(city.getCityName())
                .province(city.getProvince())
                .region(city.getRegion())
                .cityIntro(city.getCityIntro())
                .collegeCount(city.getCollegeCount())
                .build();
    }

    /**
     * 地级行政区常见后缀，用于「带后缀 / 不带后缀」兼容匹配。
     * 市 / 自治州 / 地区 / 盟 / 林区 覆盖全部地级行政区类型。
     */
    private static final String[] CITY_SUFFIXES = {"市", "自治州", "地区", "盟", "林区"};

    /**
     * 按城市名解析城市实体，兼容带 / 不带行政后缀的差异。
     * 匹配顺序：精确 -> 去后缀（含自治州民族前缀剥离）-> 加后缀兜底。
     * 城市名全局唯一，不会误匹配。
     * 示例：北京市↔北京、延边朝鲜族自治州↔延边、阿里地区↔阿里。
     */
    private City resolveCityByName(String cityName) {
        if (cityName == null || cityName.isEmpty()) {
            return null;
        }
        City city = selectActiveByName(cityName);
        if (city != null) {
            return city;
        }
        // 去后缀再试：北京市→北京、延边朝鲜族自治州→延边、阿里地区→阿里
        String stripped = stripCitySuffix(cityName);
        if (!stripped.equals(cityName)) {
            city = selectActiveByName(stripped);
            if (city != null) {
                return city;
            }
        }
        // 加后缀兜底：北京→北京市、延边→延边朝鲜族自治州、阿里→阿里地区
        for (String suffix : CITY_SUFFIXES) {
            city = selectActiveByName(cityName + suffix);
            if (city != null) {
                return city;
            }
        }
        return null;
    }

    /**
     * 剥离地级行政区后缀。自治州可能带民族前缀（延边朝鲜族自治州），
     * 一并剥到「族」字之前，得到核心名（延边）。
     */
    private String stripCitySuffix(String name) {
        if (name == null) {
            return null;
        }
        String s = name.trim();
        for (String suffix : CITY_SUFFIXES) {
            if (s.endsWith(suffix)) {
                s = s.substring(0, s.length() - suffix.length());
                break;
            }
        }
        int ethnicIdx = s.indexOf("族");
        if (ethnicIdx > 0) {
            s = s.substring(0, ethnicIdx);
        }
        return s;
    }

    /**
     * 省级行政区名称归一化已抽取为公共方法：com.haifeng.common.util.ProvinceNameUtil。
     */

    private City selectActiveByName(String name) {
        return cityMapper.selectOne(new LambdaQueryWrapper<City>()
                .eq(City::getCityName, name)
                .eq(City::getIsDeleted, false)
                .last("LIMIT 1"));
    }

    private CityListVO toListVO(City e, String cityLevel) {
        return CityListVO.builder()
                .id(e.getId())
                .cityName(e.getCityName())
                .province(e.getProvince())
                .region(e.getRegion())
                .cityIntro(e.getCityIntro())
                .collegeCount(e.getCollegeCount())
                .keyCollegeCount(e.getKeyCollegeCount())
                .residentPopulation(e.getResidentPopulation())
                .gdp(e.getGdp())
                .coverImageUrl(e.getCoverImageUrl())
                .cityLevel(cityLevel)
                .build();
    }

    /** 排序白名单（DTO @Pattern 已做第一层校验），均为倒序 + id 正序稳定排序 */
    private void applySort(LambdaQueryWrapper<City> wrapper, String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            wrapper.orderByDesc(City::getSortOrder).orderByDesc(City::getCreatedAt);
            return;
        }
        switch (sortBy) {
            case "college" -> wrapper.orderByDesc(City::getCollegeCount).orderByAsc(City::getId);
            case "keyCollege" -> wrapper.orderByDesc(City::getKeyCollegeCount).orderByAsc(City::getId);
            case "gdp" -> wrapper.orderByDesc(City::getGdp).orderByAsc(City::getId);
            case "population" -> wrapper.orderByDesc(City::getResidentPopulation).orderByAsc(City::getId);
            default -> wrapper.orderByAsc(City::getId);
        }
    }

    /**
     * 批量取当前页城市的 cityLevel（单次 IN 查询，避免 N+1）。
     * 无详情记录或 cityLevel 为空的城市不出现在 Map 中，取值侧得到 null。
     */
    private Map<Long, String> loadCityLevels(List<City> cities) {
        if (cities == null || cities.isEmpty()) {
            return Map.of();
        }
        List<Long> cityIds = cities.stream().map(City::getId).toList();
        List<CityDetail> details = cityDetailMapper.selectList(new LambdaQueryWrapper<CityDetail>()
                .in(CityDetail::getCityId, cityIds)
                .select(CityDetail::getCityId, CityDetail::getCityLevel));
        Map<Long, String> levelMap = new HashMap<>();
        for (CityDetail d : details) {
            if (d.getCityId() != null && StringUtils.hasText(d.getCityLevel())) {
                levelMap.put(d.getCityId(), d.getCityLevel());
            }
        }
        return levelMap;
    }

    private Long asLong(Object v) {
        if (v == null) {
            return 0L;
        }
        return v instanceof Number n ? n.longValue() : Long.parseLong(v.toString());
    }

    private BigDecimal asBigDecimal(Object v) {
        if (v == null) {
            return BigDecimal.ZERO;
        }
        return v instanceof BigDecimal b ? b : new BigDecimal(v.toString());
    }
}
