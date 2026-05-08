package com.haifeng.admin.service.impl.city;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.city.CityAddDTO;
import com.haifeng.admin.dto.city.CityDetailUpdateDTO;
import com.haifeng.admin.dto.city.CityQueryDTO;
import com.haifeng.admin.dto.city.CityStatusDTO;
import com.haifeng.admin.dto.city.CityUpdateDTO;
import com.haifeng.admin.service.city.CityService;
import com.haifeng.admin.vo.city.CityDetailVO;
import com.haifeng.admin.vo.city.CityListVO;
import com.haifeng.common.entity.city.City;
import com.haifeng.common.entity.city.CityDetail;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.city.CityDetailMapper;
import com.haifeng.common.mapper.city.CityMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

    private final CityMapper cityMapper;
    private final CityDetailMapper cityDetailMapper;

    @Override
    public IPage<CityListVO> page(CityQueryDTO dto) {
        Page<City> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<City> wrapper = new LambdaQueryWrapper<>();

        // 城市名称模糊查询
        if (StringUtils.hasText(dto.getCityName())) {
            wrapper.like(City::getCityName, dto.getCityName());
        }
        // 省份模糊查询
        if (StringUtils.hasText(dto.getProvince())) {
            wrapper.like(City::getProvince, dto.getProvince());
        }
        // 所属地区模糊查询
        if (StringUtils.hasText(dto.getRegion())) {
            wrapper.like(City::getRegion, dto.getRegion());
        }
        // 删除状态筛选
        if (dto.getIsDeleted() != null) {
            wrapper.eq(City::getIsDeleted, dto.getIsDeleted());
        }

        // 按省份升序，城市名称升序
        wrapper.orderByAsc(City::getProvince)
               .orderByAsc(City::getCityName);

        IPage<City> cityPage = cityMapper.selectPage(page, wrapper);

        return cityPage.convert(city -> {
            CityListVO vo = new CityListVO();
            BeanUtils.copyProperties(city, vo);
            // 处理时间类型转换
            if (city.getCreatedAt() != null) {
                vo.setCreatedAt(city.getCreatedAt().toLocalDateTime());
            }
            return vo;
        });
    }

    @Override
    public CityDetailVO detail(Long id) {
        // 查询主表
        City city = cityMapper.selectById(id);
        if (city == null) {
            throw new BusinessException(404, "城市不存在");
        }

        CityDetailVO vo = new CityDetailVO();
        BeanUtils.copyProperties(city, vo);

        // 处理时间类型转换
        if (city.getCreatedAt() != null) {
            vo.setCreatedAt(city.getCreatedAt().toLocalDateTime());
        }
        if (city.getUpdatedAt() != null) {
            vo.setUpdatedAt(city.getUpdatedAt().toLocalDateTime());
        }

        // 查询详情表
        CityDetail detail = cityDetailMapper.findByCityId(id);
        if (detail != null) {
            vo.setDetailId(detail.getId());
            vo.setArea(detail.getArea());
            vo.setSubtitle(detail.getSubtitle());
            vo.setCityLevel(detail.getCityLevel());
            vo.setAdminCode(detail.getAdminCode());
            vo.setPerCapitaGdp(detail.getPerCapitaGdp());
            vo.setUrbanizationRate(detail.getUrbanizationRate());
            vo.setRuralPopRatio(detail.getRuralPopRatio());
            vo.setAgingRate(detail.getAgingRate());
            vo.setMigrantPopRatio(detail.getMigrantPopRatio());
            vo.setGdpGrowthRate(detail.getGdpGrowthRate());
            vo.setFortune500Count(detail.getFortune500Count());
            vo.setIndustryStructure(detail.getIndustryStructure());
            vo.setIndustryDescription(detail.getIndustryDescription());
            vo.setMainIndustries(detail.getMainIndustries());
            vo.setEmergingIndustries(detail.getEmergingIndustries());
            vo.setFuturePlan(detail.getFuturePlan());
            vo.setHighEducation(detail.getHighEducation());
            vo.setBasicEducation(detail.getBasicEducation());
            vo.setEnterpriseStats(detail.getEnterpriseStats());
            vo.setHousingPriceLevel(detail.getHousingPriceLevel());
            vo.setRentalCost(detail.getRentalCost());
            vo.setHousingPolicy(detail.getHousingPolicy());
            vo.setConsumption(detail.getConsumption());
            vo.setEmployment(detail.getEmployment());
            vo.setTransportation(detail.getTransportation());
            vo.setMedical(detail.getMedical());
            vo.setCulture(detail.getCulture());
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(CityAddDTO dto) {
        // 检查城市名称是否已存在
        if (cityMapper.existsByCityName(dto.getCityName())) {
            throw new BusinessException(400, "城市名称已存在");
        }

        OffsetDateTime now = OffsetDateTime.now();
        Long cityId = SnowflakeIdGenerator.nextId();
        Long detailId = SnowflakeIdGenerator.nextId();

        // 创建主表记录
        City city = City.builder()
                .id(cityId)
                .cityName(dto.getCityName())
                .province(dto.getProvince())
                .region(dto.getRegion())
                .cityIntro(dto.getCityIntro())
                .collegeCount(dto.getCollegeCount() != null ? dto.getCollegeCount() : 0)
                .keyCollegeCount(dto.getKeyCollegeCount() != null ? dto.getKeyCollegeCount() : 0)
                .residentPopulation(dto.getResidentPopulation())
                .gdp(dto.getGdp())
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        cityMapper.insert(city);

        // 创建详情表记录
        CityDetail detail = CityDetail.builder()
                .id(detailId)
                .cityId(cityId)
                .cityName(dto.getCityName())
                .area(dto.getArea())
                .subtitle(dto.getSubtitle())
                .cityLevel(dto.getCityLevel())
                .adminCode(dto.getAdminCode())
                .perCapitaGdp(dto.getPerCapitaGdp())
                .urbanizationRate(dto.getUrbanizationRate())
                .ruralPopRatio(dto.getRuralPopRatio())
                .agingRate(dto.getAgingRate())
                .migrantPopRatio(dto.getMigrantPopRatio())
                .gdpGrowthRate(dto.getGdpGrowthRate())
                .fortune500Count(dto.getFortune500Count())
                .industryStructure(dto.getIndustryStructure())
                .industryDescription(dto.getIndustryDescription())
                .mainIndustries(dto.getMainIndustries())
                .emergingIndustries(dto.getEmergingIndustries())
                .futurePlan(dto.getFuturePlan())
                .highEducation(dto.getHighEducation())
                .basicEducation(dto.getBasicEducation())
                .enterpriseStats(dto.getEnterpriseStats())
                .housingPriceLevel(dto.getHousingPriceLevel())
                .rentalCost(dto.getRentalCost())
                .housingPolicy(dto.getHousingPolicy())
                .consumption(dto.getConsumption())
                .employment(dto.getEmployment())
                .transportation(dto.getTransportation())
                .medical(dto.getMedical())
                .culture(dto.getCulture())
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        cityDetailMapper.insert(detail);

        log.info("新增城市成功: id={}, cityName={}", cityId, dto.getCityName());
        return cityId;
    }

    @Override
    public void update(Long id, CityUpdateDTO dto) {
        City city = cityMapper.selectById(id);
        if (city == null) {
            throw new BusinessException(404, "城市不存在");
        }

        // 如果修改了城市名称，检查是否与其他城市重名
        if (!city.getCityName().equals(dto.getCityName()) && cityMapper.existsByCityName(dto.getCityName())) {
            throw new BusinessException(400, "城市名称已存在");
        }

        city.setCityName(dto.getCityName());
        city.setProvince(dto.getProvince());
        city.setRegion(dto.getRegion());
        city.setCityIntro(dto.getCityIntro());
        city.setCollegeCount(dto.getCollegeCount());
        city.setKeyCollegeCount(dto.getKeyCollegeCount());
        city.setResidentPopulation(dto.getResidentPopulation());
        city.setGdp(dto.getGdp());
        if (dto.getIsDeleted() != null) {
            city.setIsDeleted(dto.getIsDeleted());
        }
        city.setUpdatedAt(OffsetDateTime.now());

        cityMapper.updateById(city);

        // 同步更新详情表中的城市名称
        CityDetail detail = cityDetailMapper.findByCityId(id);
        if (detail != null) {
            detail.setCityName(dto.getCityName());
            detail.setUpdatedAt(OffsetDateTime.now());
            cityDetailMapper.updateById(detail);
        }

        log.info("更新城市成功: id={}, cityName={}", id, dto.getCityName());
    }

    @Override
    public void updateDetail(Long id, CityDetailUpdateDTO dto) {
        // 先检查城市是否存在
        City city = cityMapper.selectById(id);
        if (city == null) {
            throw new BusinessException(404, "城市不存在");
        }

        // 查找对应的详情记录
        CityDetail detail = cityDetailMapper.findByCityId(id);
        if (detail == null) {
            throw new BusinessException(404, "城市详情不存在");
        }

        detail.setArea(dto.getArea());
        detail.setSubtitle(dto.getSubtitle());
        detail.setCityLevel(dto.getCityLevel());
        detail.setAdminCode(dto.getAdminCode());
        detail.setPerCapitaGdp(dto.getPerCapitaGdp());
        detail.setUrbanizationRate(dto.getUrbanizationRate());
        detail.setRuralPopRatio(dto.getRuralPopRatio());
        detail.setAgingRate(dto.getAgingRate());
        detail.setMigrantPopRatio(dto.getMigrantPopRatio());
        detail.setGdpGrowthRate(dto.getGdpGrowthRate());
        detail.setFortune500Count(dto.getFortune500Count());
        detail.setIndustryStructure(dto.getIndustryStructure());
        detail.setIndustryDescription(dto.getIndustryDescription());
        detail.setMainIndustries(dto.getMainIndustries());
        detail.setEmergingIndustries(dto.getEmergingIndustries());
        detail.setFuturePlan(dto.getFuturePlan());
        detail.setHighEducation(dto.getHighEducation());
        detail.setBasicEducation(dto.getBasicEducation());
        detail.setEnterpriseStats(dto.getEnterpriseStats());
        detail.setHousingPriceLevel(dto.getHousingPriceLevel());
        detail.setRentalCost(dto.getRentalCost());
        detail.setHousingPolicy(dto.getHousingPolicy());
        detail.setConsumption(dto.getConsumption());
        detail.setEmployment(dto.getEmployment());
        detail.setTransportation(dto.getTransportation());
        detail.setMedical(dto.getMedical());
        detail.setCulture(dto.getCulture());
        if (dto.getIsDeleted() != null) {
            detail.setIsDeleted(dto.getIsDeleted());
        }
        detail.setUpdatedAt(OffsetDateTime.now());

        cityDetailMapper.updateById(detail);

        log.info("更新城市详情成功: cityId={}, detailId={}", id, detail.getId());
    }

    @Override
    public void updateStatus(Long id, CityStatusDTO dto) {
        City city = cityMapper.selectById(id);
        if (city == null) {
            throw new BusinessException(404, "城市不存在");
        }

        city.setIsDeleted(dto.getIsDeleted());
        city.setUpdatedAt(OffsetDateTime.now());

        cityMapper.updateById(city);

        // 同步更新详情表状态
        CityDetail detail = cityDetailMapper.findByCityId(id);
        if (detail != null) {
            detail.setIsDeleted(dto.getIsDeleted());
            detail.setUpdatedAt(OffsetDateTime.now());
            cityDetailMapper.updateById(detail);
        }

        log.info("更新城市状态成功: id={}, isDeleted={}", id, dto.getIsDeleted());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        City city = cityMapper.selectById(id);
        if (city == null) {
            throw new BusinessException(404, "城市不存在");
        }

        // 删除详情表
        CityDetail detail = cityDetailMapper.findByCityId(id);
        if (detail != null) {
            cityDetailMapper.deleteById(detail.getId());
        }

        // 删除主表
        cityMapper.deleteById(id);

        log.info("硬删除城市成功: id={}, cityName={}", id, city.getCityName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的城市");
        }

        // 删除所有关联的详情记录
        for (Long cityId : ids) {
            CityDetail detail = cityDetailMapper.findByCityId(cityId);
            if (detail != null) {
                cityDetailMapper.deleteById(detail.getId());
            }
        }

        // 批量删除主表记录
        int deleted = cityMapper.deleteBatchIds(ids);

        log.info("批量硬删除城市成功: 删除数量={}, ids={}", deleted, ids);
    }
}
