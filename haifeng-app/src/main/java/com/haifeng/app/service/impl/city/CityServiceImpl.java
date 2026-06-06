package com.haifeng.app.service.impl.city;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.city.CityQueryDTO;
import com.haifeng.app.service.city.CityService;
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
    public CityDetailVO detail(Long cityId) {
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
