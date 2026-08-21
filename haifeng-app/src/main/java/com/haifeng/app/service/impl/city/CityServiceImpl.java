package com.haifeng.app.service.impl.city;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.city.CityQueryDTO;
import com.haifeng.app.service.city.CityService;
import com.haifeng.app.vo.city.CityBriefVO;
import com.haifeng.app.vo.city.CityDetailVO;
import com.haifeng.app.vo.city.CityListVO;
import com.haifeng.common.entity.city.City;
import com.haifeng.common.entity.city.CityDetail;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.city.CityDetailMapper;
import com.haifeng.common.mapper.city.CityMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

    private final CityMapper cityMapper;
    private final CityDetailMapper cityDetailMapper;

    @Override
    public IPage<CityListVO> page(CityQueryDTO dto) {
        Page<City> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<City> wrapper = new LambdaQueryWrapper<City>()
                .eq(City::getIsDeleted, false)
                .like(StringUtils.hasText(dto.getCityName()), City::getCityName, dto.getCityName())
                .eq(StringUtils.hasText(dto.getProvince()), City::getProvince, dto.getProvince())
                .eq(StringUtils.hasText(dto.getRegion()), City::getRegion, dto.getRegion())
                .orderByAsc(City::getId);

        IPage<City> entityPage = cityMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
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

    private City selectActiveByName(String name) {
        return cityMapper.selectOne(new LambdaQueryWrapper<City>()
                .eq(City::getCityName, name)
                .eq(City::getIsDeleted, false)
                .last("LIMIT 1"));
    }

    private CityListVO toListVO(City e) {
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
                .build();
    }
}
