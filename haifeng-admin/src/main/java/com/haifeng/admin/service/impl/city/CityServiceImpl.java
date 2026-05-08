package com.haifeng.admin.service.impl.city;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.city.CityAddDTO;
import com.haifeng.admin.dto.city.CityDetailUpdateDTO;
import com.haifeng.admin.dto.city.CityQueryDTO;
import com.haifeng.admin.dto.city.CityStatusDTO;
import com.haifeng.admin.dto.city.CityUpdateDTO;
import com.haifeng.admin.excel.city.*;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importCities(MultipartFile file) {
        List<String> errorMsgs = new ArrayList<>();

        try {
            // 读取主表数据
            List<CityExcelDTO> mainData = EasyExcel.read(file.getInputStream())
                    .head(CityExcelDTO.class)
                    .sheet(0)
                    .doReadSync();

            if (mainData == null || mainData.isEmpty()) {
                throw new BusinessException(400, "导入失败：Excel文件为空");
            }

            List<City> cities = new ArrayList<>();
            List<CityDetail> cityDetails = new ArrayList<>();
            Set<String> cityNamesInFile = new HashSet<>();

            for (int i = 0; i < mainData.size(); i++) {
                int rowNum = i + 2;
                CityExcelDTO data = mainData.get(i);

                // 校验必填字段
                if (!StringUtils.hasText(data.getCityName())) {
                    errorMsgs.add("第" + rowNum + "行：城市名称不能为空");
                    continue;
                }

                // 检查文件内重复
                if (cityNamesInFile.contains(data.getCityName())) {
                    errorMsgs.add("第" + rowNum + "行：城市名称'" + data.getCityName() + "'在文件中重复");
                    continue;
                }
                cityNamesInFile.add(data.getCityName());

                // 检查数据库中是否已存在
                if (cityMapper.existsByCityName(data.getCityName())) {
                    errorMsgs.add("第" + rowNum + "行：城市名称'" + data.getCityName() + "'已存在");
                    continue;
                }

                OffsetDateTime now = OffsetDateTime.now();
                Long cityId = SnowflakeIdGenerator.nextId();
                Long detailId = SnowflakeIdGenerator.nextId();

                City city = City.builder()
                        .id(cityId)
                        .cityName(data.getCityName())
                        .province(data.getProvince())
                        .region(data.getRegion())
                        .cityIntro(data.getCityIntro())
                        .collegeCount(data.getCollegeCount() != null ? data.getCollegeCount() : 0)
                        .keyCollegeCount(data.getKeyCollegeCount() != null ? data.getKeyCollegeCount() : 0)
                        .residentPopulation(data.getResidentPopulation())
                        .gdp(data.getGdp())
                        .isDeleted(false)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

                CityDetail detail = CityDetail.builder()
                        .id(detailId)
                        .cityId(cityId)
                        .cityName(data.getCityName())
                        .isDeleted(false)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

                cities.add(city);
                cityDetails.add(detail);
            }

            if (!errorMsgs.isEmpty()) {
                throw new BusinessException(400, "导入失败：" + String.join("；", errorMsgs));
            }

            // 批量插入
            if (!cities.isEmpty()) {
                for (City city : cities) {
                    cityMapper.insert(city);
                }
                for (CityDetail detail : cityDetails) {
                    cityDetailMapper.insert(detail);
                }
                log.info("导入城市主表成功，数量={}", cities.size());
            }

        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(500, "读取Excel文件失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importCityDetails(MultipartFile file) {
        List<String> errorMsgs = new ArrayList<>();

        try {
            // Sheet0: 详情基础字段
            List<CityDetailExcelDTO> detailData = EasyExcel.read(file.getInputStream())
                    .head(CityDetailExcelDTO.class)
                    .sheet(0)
                    .doReadSync();

            // Sheet1: industry_structure
            List<IndustryStructureExcelDTO> industryStructureData = EasyExcel.read(file.getInputStream())
                    .head(IndustryStructureExcelDTO.class)
                    .sheet(1)
                    .doReadSync();

            // Sheet2: housing_price_level
            List<HousingPriceLevelExcelDTO> housingPriceData = EasyExcel.read(file.getInputStream())
                    .head(HousingPriceLevelExcelDTO.class)
                    .sheet(2)
                    .doReadSync();

            // Sheet3: high_education
            List<HighEducationExcelDTO> highEducationData = EasyExcel.read(file.getInputStream())
                    .head(HighEducationExcelDTO.class)
                    .sheet(3)
                    .doReadSync();

            // Sheet4: basic_education
            List<BasicEducationExcelDTO> basicEducationData = EasyExcel.read(file.getInputStream())
                    .head(BasicEducationExcelDTO.class)
                    .sheet(4)
                    .doReadSync();

            // Sheet5: transportation
            List<TransportationExcelDTO> transportationData = EasyExcel.read(file.getInputStream())
                    .head(TransportationExcelDTO.class)
                    .sheet(5)
                    .doReadSync();

            // Sheet6: employment
            List<EmploymentExcelDTO> employmentData = EasyExcel.read(file.getInputStream())
                    .head(EmploymentExcelDTO.class)
                    .sheet(6)
                    .doReadSync();

            // Sheet7: enterprise_stats
            List<EnterpriseStatsExcelDTO> enterpriseStatsData = EasyExcel.read(file.getInputStream())
                    .head(EnterpriseStatsExcelDTO.class)
                    .sheet(7)
                    .doReadSync();

            // Sheet8: future_plan
            List<FuturePlanExcelDTO> futurePlanData = EasyExcel.read(file.getInputStream())
                    .head(FuturePlanExcelDTO.class)
                    .sheet(8)
                    .doReadSync();

            // Sheet9: culture
            List<CultureExcelDTO> cultureData = EasyExcel.read(file.getInputStream())
                    .head(CultureExcelDTO.class)
                    .sheet(9)
                    .doReadSync();

            // Sheet10: consumption
            List<ConsumptionExcelDTO> consumptionData = EasyExcel.read(file.getInputStream())
                    .head(ConsumptionExcelDTO.class)
                    .sheet(10)
                    .doReadSync();

            // Sheet11: medical
            List<MedicalExcelDTO> medicalData = EasyExcel.read(file.getInputStream())
                    .head(MedicalExcelDTO.class)
                    .sheet(11)
                    .doReadSync();

            // Sheet12: housing_policy
            List<HousingPolicyExcelDTO> housingPolicyData = EasyExcel.read(file.getInputStream())
                    .head(HousingPolicyExcelDTO.class)
                    .sheet(12)
                    .doReadSync();

            // Sheet13: rental_cost
            List<RentalCostExcelDTO> rentalCostData = EasyExcel.read(file.getInputStream())
                    .head(RentalCostExcelDTO.class)
                    .sheet(13)
                    .doReadSync();

            // 按城市名称分组JSONB数据
            Map<String, Map<String, Object>> industryStructureMap = buildJsonbMap(industryStructureData,
                    dto -> dto.getCityName(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("primaryRatio", dto.getPrimaryRatio());
                        m.put("secondaryRatio", dto.getSecondaryRatio());
                        m.put("tertiaryRatio", dto.getTertiaryRatio());
                        return m;
                    });

            Map<String, Map<String, Object>> housingPriceMap = buildJsonbMap(housingPriceData,
                    dto -> dto.getCityName(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("avgPrice", dto.getAvgPrice());
                        m.put("coreAreaPrice", dto.getCoreAreaPrice());
                        m.put("suburbanPriceRange", dto.getSuburbanPriceRange());
                        m.put("priceGrowthRate", dto.getPriceGrowthRate());
                        m.put("priceIncomeRatio", dto.getPriceIncomeRatio());
                        return m;
                    });

            Map<String, Map<String, Object>> highEducationMap = buildJsonbMap(highEducationData,
                    dto -> dto.getCityName(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("totalColleges", dto.getTotalColleges());
                        m.put("doubleFirstClassCount", dto.getDoubleFirstClassCount());
                        m.put("undergraduateCount", dto.getUndergraduateCount());
                        m.put("graduateCount", dto.getGraduateCount());
                        return m;
                    });

            Map<String, Map<String, Object>> basicEducationMap = buildJsonbMap(basicEducationData,
                    dto -> dto.getCityName(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("totalSchools", dto.getTotalSchools());
                        m.put("modelSchoolCount", dto.getModelSchoolCount());
                        m.put("keySchoolCount", dto.getKeySchoolCount());
                        m.put("educationNote", dto.getEducationNote());
                        return m;
                    });

            Map<String, Map<String, Object>> transportationMap = buildJsonbMap(transportationData,
                    dto -> dto.getCityName(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("metroLines", dto.getMetroLines());
                        m.put("metroMileage", dto.getMetroMileage());
                        m.put("highwayMileage", dto.getHighwayMileage());
                        m.put("trafficWorldRank", dto.getTrafficWorldRank());
                        return m;
                    });

            Map<String, Map<String, Object>> employmentMap = buildJsonbMap(employmentData,
                    dto -> dto.getCityName(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("unemploymentRate", dto.getUnemploymentRate());
                        m.put("nationalUnemploymentRate", dto.getNationalUnemploymentRate());
                        m.put("tertiaryEmploymentRatio", dto.getTertiaryEmploymentRatio());
                        m.put("newEmployment", dto.getNewEmployment());
                        m.put("avgSalary", dto.getAvgSalary());
                        m.put("salaryRank", dto.getSalaryRank());
                        m.put("skilledTalentRatio", dto.getSkilledTalentRatio());
                        m.put("skilledTalentGrowth", dto.getSkilledTalentGrowth());
                        return m;
                    });

            Map<String, Map<String, Object>> enterpriseStatsMap = buildJsonbMap(enterpriseStatsData,
                    dto -> dto.getCityName(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("enterpriseCategories", dto.getEnterpriseCategories());
                        m.put("keyEnterpriseCount", dto.getKeyEnterpriseCount());
                        m.put("fortune500Count", dto.getFortune500Count());
                        return m;
                    });

            Map<String, Map<String, Object>> futurePlanMap = buildJsonbMap(futurePlanData,
                    dto -> dto.getCityName(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("targetYear", dto.getTargetYear());
                        m.put("developmentGoal", dto.getDevelopmentGoal());
                        m.put("keyAreas", dto.getKeyAreas());
                        return m;
                    });

            Map<String, Map<String, Object>> cultureMap = buildJsonbMap(cultureData,
                    dto -> dto.getCityName(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("worldHeritageCount", dto.getWorldHeritageCount());
                        m.put("annualTourists", dto.getAnnualTourists());
                        m.put("aScenicCount", dto.getAScenicCount());
                        m.put("coreAttractions", dto.getCoreAttractions());
                        return m;
                    });

            Map<String, Map<String, Object>> consumptionMap = buildJsonbMap(consumptionData,
                    dto -> dto.getCityName(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("perCapitaConsumption", dto.getPerCapitaConsumption());
                        m.put("consumptionGrowthRate", dto.getConsumptionGrowthRate());
                        m.put("engelCoefficient", dto.getEngelCoefficient());
                        m.put("educationExpenseRatio", dto.getEducationExpenseRatio());
                        m.put("consumptionIndex", dto.getConsumptionIndex());
                        m.put("consumptionRank", dto.getConsumptionRank());
                        return m;
                    });

            Map<String, Map<String, Object>> medicalMap = buildJsonbMap(medicalData,
                    dto -> dto.getCityName(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("topHospitalCount", dto.getTopHospitalCount());
                        m.put("tertiaryHospitalCount", dto.getTertiaryHospitalCount());
                        m.put("doctorDensity", dto.getDoctorDensity());
                        m.put("medicalRank", dto.getMedicalRank());
                        return m;
                    });

            Map<String, Map<String, Object>> housingPolicyMap = buildJsonbMap(housingPolicyData,
                    dto -> dto.getCityName(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("purchaseRestriction", dto.getPurchaseRestriction());
                        m.put("sharedPropertyHousing", dto.getSharedPropertyHousing());
                        m.put("publicRentalHousing", dto.getPublicRentalHousing());
                        m.put("firstHomeRate", dto.getFirstHomeRate());
                        m.put("secondHomeRate", dto.getSecondHomeRate());
                        return m;
                    });

            Map<String, Map<String, Object>> rentalCostMap = buildJsonbMap(rentalCostData,
                    dto -> dto.getCityName(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("downtownRentRange", dto.getDowntownRentRange());
                        m.put("suburbanRentRange", dto.getSuburbanRentRange());
                        m.put("rentIncomeRatio", dto.getRentIncomeRatio());
                        m.put("rentGrowthRate", dto.getRentGrowthRate());
                        return m;
                    });

            // 缓存城市ID
            Map<String, Long> cityIdCache = new HashMap<>();

            // 处理详情基础字段
            int updatedCount = 0;
            for (int i = 0; i < detailData.size(); i++) {
                int rowNum = i + 2;
                CityDetailExcelDTO data = detailData.get(i);

                if (!StringUtils.hasText(data.getCityName())) {
                    errorMsgs.add("Sheet0第" + rowNum + "行：城市名称不能为空");
                    continue;
                }

                // 查询城市ID
                Long cityId = cityIdCache.get(data.getCityName());
                if (cityId == null) {
                    LambdaQueryWrapper<City> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(City::getCityName, data.getCityName())
                           .eq(City::getIsDeleted, false);
                    City city = cityMapper.selectOne(wrapper);
                    if (city == null) {
                        errorMsgs.add("Sheet0第" + rowNum + "行：城市名称'" + data.getCityName() + "'不存在");
                        continue;
                    }
                    cityId = city.getId();
                    cityIdCache.put(data.getCityName(), cityId);
                }

                // 查询详情记录
                CityDetail detail = cityDetailMapper.findByCityId(cityId);
                if (detail == null) {
                    errorMsgs.add("Sheet0第" + rowNum + "行：城市'" + data.getCityName() + "'的详情记录不存在");
                    continue;
                }

                // 更新详情基础字段
                detail.setArea(data.getArea());
                detail.setSubtitle(data.getSubtitle());
                detail.setCityLevel(data.getCityLevel());
                detail.setAdminCode(data.getAdminCode());
                detail.setPerCapitaGdp(data.getPerCapitaGdp());
                detail.setUrbanizationRate(data.getUrbanizationRate());
                detail.setRuralPopRatio(data.getRuralPopRatio());
                detail.setAgingRate(data.getAgingRate());
                detail.setMigrantPopRatio(data.getMigrantPopRatio());
                detail.setGdpGrowthRate(data.getGdpGrowthRate());
                detail.setFortune500Count(data.getFortune500Count());
                detail.setIndustryDescription(data.getIndustryDescription());
                detail.setMainIndustries(data.getMainIndustries());
                detail.setEmergingIndustries(data.getEmergingIndustries());

                // 设置JSONB字段
                detail.setIndustryStructure(industryStructureMap.get(data.getCityName()));
                detail.setHousingPriceLevel(housingPriceMap.get(data.getCityName()));
                detail.setHighEducation(highEducationMap.get(data.getCityName()));
                detail.setBasicEducation(basicEducationMap.get(data.getCityName()));
                detail.setTransportation(transportationMap.get(data.getCityName()));
                detail.setEmployment(employmentMap.get(data.getCityName()));
                detail.setEnterpriseStats(enterpriseStatsMap.get(data.getCityName()));
                detail.setFuturePlan(futurePlanMap.get(data.getCityName()));
                detail.setCulture(cultureMap.get(data.getCityName()));
                detail.setConsumption(consumptionMap.get(data.getCityName()));
                detail.setMedical(medicalMap.get(data.getCityName()));
                detail.setHousingPolicy(housingPolicyMap.get(data.getCityName()));
                detail.setRentalCost(rentalCostMap.get(data.getCityName()));

                detail.setUpdatedAt(OffsetDateTime.now());
                cityDetailMapper.updateById(detail);
                updatedCount++;
            }

            if (!errorMsgs.isEmpty()) {
                throw new BusinessException(400, "导入失败：" + String.join("；", errorMsgs));
            }

            log.info("导入城市详情成功，更新数量={}", updatedCount);

        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(500, "读取Excel文件失败");
        }
    }

    /**
     * 构建JSONB Map的辅助方法
     */
    private <T> Map<String, Map<String, Object>> buildJsonbMap(
            List<T> dataList,
            java.util.function.Function<T, String> keyExtractor,
            java.util.function.Function<T, Map<String, Object>> valueExtractor) {
        Map<String, Map<String, Object>> result = new HashMap<>();
        if (dataList != null) {
            for (T data : dataList) {
                String key = keyExtractor.apply(data);
                if (StringUtils.hasText(key)) {
                    result.put(key, valueExtractor.apply(data));
                }
            }
        }
        return result;
    }
}
