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
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

    private final CityMapper cityMapper;
    private final CityDetailMapper cityDetailMapper;

    private static final int MAX_IMPORT_ROWS = 500;
    /** 导入报错信息最多展示条数，避免单条 msg 过长（完整错误见后端日志） */
    private static final int MAX_ERROR_DISPLAY = 50;
    private static final Set<String> VALID_CITY_LEVELS = Set.of("直辖市", "省会城市", "地级市", "县级市");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    @Override
    public IPage<CityListVO> page(CityQueryDTO dto) {
        Page<City> page = new Page<>(dto.getPage(), dto.getSize());

        IPage<City> cityPage = cityMapper.selectPageIgnoreLogicDelete(
                page, dto.getIsDeleted(), dto.getCityName(), dto.getProvince(), dto.getRegion());

        return cityPage.convert(city -> {
            CityListVO vo = new CityListVO();
            BeanUtils.copyProperties(city, vo);
            if (city.getCreatedAt() != null) {
                vo.setCreatedAt(city.getCreatedAt().toLocalDateTime());
            }
            return vo;
        });
    }

    @Override
    public CityDetailVO detail(Long id) {
        // 查询主表
        City city = cityMapper.findByIdIgnoreLogicDelete(id);
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
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, CityUpdateDTO dto) {
        City city = cityMapper.findByIdIgnoreLogicDelete(id);
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
        city.setUpdatedAt(OffsetDateTime.now());

        int rows = cityMapper.updateByIdCustom(city);
        if (rows == 0) {
            throw new BusinessException(500, "更新城市失败，记录可能已被修改或不存在，请刷新后重试");
        }

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
    @Transactional(rollbackFor = Exception.class)
    public void updateDetail(Long id, CityDetailUpdateDTO dto) {
        // 先检查城市是否存在
        City city = cityMapper.findByIdIgnoreLogicDelete(id);
        if (city == null) {
            throw new BusinessException(404, "城市不存在");
        }

        // 查找对应的详情记录
        CityDetail detail = cityDetailMapper.findByCityId(id);
        if (detail == null) {
            log.warn("城市详情不存在或已禁用，跳过详情更新: cityId={}", id);
            return;
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
        detail.setUpdatedAt(OffsetDateTime.now());

        cityDetailMapper.updateById(detail);

        log.info("更新城市详情成功: cityId={}, detailId={}", id, detail.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, CityStatusDTO dto) {
        City city = cityMapper.findByIdIgnoreLogicDelete(id);
        if (city == null) {
            throw new BusinessException(404, "城市不存在");
        }

        cityMapper.updateIsDeletedById(id, dto.getIsDeleted());
        cityDetailMapper.updateIsDeletedByCityId(id, dto.getIsDeleted());

        log.info("更新城市状态成功: id={}, isDeleted={}", id, dto.getIsDeleted());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        City city = cityMapper.findByIdIgnoreLogicDelete(id);
        if (city == null) {
            throw new BusinessException(404, "城市不存在");
        }

        // 硬删除详情表
        CityDetail detail = cityDetailMapper.findByCityId(id);
        if (detail != null) {
            cityDetailMapper.hardDeleteById(detail.getId());
        }

        // 硬删除主表
        cityMapper.hardDeleteById(id);

        log.info("硬删除城市成功: id={}, cityName={}", id, city.getCityName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的城市");
        }

        // 批量硬删除详情记录
        cityDetailMapper.deleteByCityIds(ids);

        // 批量硬删除主表记录
        int deleted = cityMapper.hardDeleteBatchByIds(ids);

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

            if (mainData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：单次导入数量不能超过" + MAX_IMPORT_ROWS + "行");
            }

            // 新增/补齐分别收集，避免逐行DB操作互相影响
            List<City> citiesToInsert = new ArrayList<>();
            List<CityDetail> detailsToInsert = new ArrayList<>();
            List<City> citiesToUpdate = new ArrayList<>();
            Set<String> cityNamesInFile = new HashSet<>();
            int insertCount = 0;
            int updateCount = 0;

            for (int i = 0; i < mainData.size(); i++) {
                int rowNum = i + 2;
                CityExcelDTO data = mainData.get(i);

                String cityName = data.getCityName() == null ? null : data.getCityName().trim();
                String province = data.getProvince() == null ? null : data.getProvince().trim();
                String region = data.getRegion() == null ? null : data.getRegion().trim();

                // 校验必填字段
                if (!StringUtils.hasText(cityName)) {
                    errorMsgs.add("第" + rowNum + "行：城市名称不能为空");
                    continue;
                }
                if (!StringUtils.hasText(province)) {
                    errorMsgs.add("第" + rowNum + "行：省份不能为空");
                    continue;
                }

                // 校验字段长度
                if (cityName.length() > 50) {
                    errorMsgs.add("第" + rowNum + "行：城市名称不能超过50个字符");
                    continue;
                }
                if (province.length() > 30) {
                    errorMsgs.add("第" + rowNum + "行：省份不能超过30个字符");
                    continue;
                }
                if (region != null && region.length() > 20) {
                    errorMsgs.add("第" + rowNum + "行：所属地区不能超过20个字符");
                    continue;
                }

                // 检查文件内重复
                if (cityNamesInFile.contains(cityName)) {
                    errorMsgs.add("第" + rowNum + "行：城市名称'" + cityName + "'在文件中重复");
                    continue;
                }
                cityNamesInFile.add(cityName);

                // 本行DB操作整体包try-catch：异常转成"第N行"行级错误，否则丢失行号
                try {
                    City existing = cityMapper.selectOne(new LambdaQueryWrapper<City>()
                            .eq(City::getCityName, cityName)
                            .eq(City::getIsDeleted, false));

                    if (existing == null) {
                        // 新增：空列直接存NULL，便于后续导入补齐
                        OffsetDateTime now = OffsetDateTime.now();
                        Long cityId = SnowflakeIdGenerator.nextId();
                        Long detailId = SnowflakeIdGenerator.nextId();

                        City city = City.builder()
                                .id(cityId)
                                .cityName(cityName)
                                .province(province)
                                .region(region)
                                .cityIntro(data.getCityIntro())
                                .collegeCount(data.getCollegeCount())
                                .keyCollegeCount(data.getKeyCollegeCount())
                                .residentPopulation(data.getResidentPopulation())
                                .gdp(data.getGdp())
                                .isDeleted(false)
                                .createdAt(now)
                                .updatedAt(now)
                                .build();
                        citiesToInsert.add(city);

                        CityDetail detail = CityDetail.builder()
                                .id(detailId)
                                .cityId(cityId)
                                .cityName(cityName)
                                .isDeleted(false)
                                .createdAt(now)
                                .updatedAt(now)
                                .build();
                        detailsToInsert.add(detail);
                        insertCount++;
                    } else {
                        // 已存在：仅补齐库中为空(NULL)的列，已有数据一律不覆盖
                        boolean changed = mergeCityIfBlank(existing, data);
                        if (changed) {
                            citiesToUpdate.add(existing);
                        }
                        // 确保1:1详情记录存在（仅缺失时补骨架，不覆盖已有详情数据）
                        CityDetail d = cityDetailMapper.findByCityId(existing.getId());
                        if (d == null) {
                            OffsetDateTime now = OffsetDateTime.now();
                            CityDetail detail = CityDetail.builder()
                                    .id(SnowflakeIdGenerator.nextId())
                                    .cityId(existing.getId())
                                    .cityName(cityName)
                                    .isDeleted(false)
                                    .createdAt(now)
                                    .updatedAt(now)
                                    .build();
                            detailsToInsert.add(detail);
                        }
                        updateCount++;
                    }
                } catch (Exception e) {
                    errorMsgs.add("第" + rowNum + "行：数据库操作失败[" + cityName + "]：" + e.getMessage());
                }
            }

            if (!errorMsgs.isEmpty()) {
                int shown = Math.min(errorMsgs.size(), MAX_ERROR_DISPLAY);
                throw new BusinessException(400, "导入失败，共" + errorMsgs.size() + "行数据存在错误（仅展示前"
                        + shown + "条）：" + String.join("；", errorMsgs.subList(0, shown)));
            }

            // 批量落库
            if (!citiesToInsert.isEmpty()) {
                cityMapper.batchInsert(citiesToInsert);
            }
            if (!detailsToInsert.isEmpty()) {
                cityDetailMapper.batchInsert(detailsToInsert);
            }
            for (City c : citiesToUpdate) {
                cityMapper.updateByIdCustom(c);
            }

            log.info("导入城市主表成功：新增{}条，补齐{}条", insertCount, updateCount);

        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(500, "读取Excel文件失败");
        } catch (Exception e) {
            log.error("解析Excel数据失败", e);
            throw new BusinessException(400, "解析Excel数据失败，请检查Excel格式和数据类型是否正确");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importCityDetails(MultipartFile file) {
        List<String> errorMsgs = new ArrayList<>();

        try {
            // Sheet: 详情基础字段
            List<CityDetailExcelDTO> detailData = EasyExcel.read(file.getInputStream())
                    .head(CityDetailExcelDTO.class)
                    .sheet("详情基础字段")
                    .doReadSync();

            if (detailData == null || detailData.isEmpty()) {
                throw new BusinessException(400, "导入失败：详情基础字段Sheet为空");
            }

            if (detailData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：单次导入数量不能超过" + MAX_IMPORT_ROWS + "行");
            }

            // Sheet: 产业结构
            List<IndustryStructureExcelDTO> industryStructureData = EasyExcel.read(file.getInputStream())
                    .head(IndustryStructureExcelDTO.class)
                    .sheet("产业结构")
                    .doReadSync();
            if (industryStructureData != null && industryStructureData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：产业结构Sheet数据不能超过" + MAX_IMPORT_ROWS + "行");
            }

            // Sheet: 房价水平
            List<HousingPriceLevelExcelDTO> housingPriceData = EasyExcel.read(file.getInputStream())
                    .head(HousingPriceLevelExcelDTO.class)
                    .sheet("房价水平")
                    .doReadSync();
            if (housingPriceData != null && housingPriceData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：房价水平Sheet数据不能超过" + MAX_IMPORT_ROWS + "行");
            }

            // Sheet: 高等教育
            List<HighEducationExcelDTO> highEducationData = EasyExcel.read(file.getInputStream())
                    .head(HighEducationExcelDTO.class)
                    .sheet("高等教育")
                    .doReadSync();
            if (highEducationData != null && highEducationData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：高等教育Sheet数据不能超过" + MAX_IMPORT_ROWS + "行");
            }

            // Sheet: 基础教育
            List<BasicEducationExcelDTO> basicEducationData = EasyExcel.read(file.getInputStream())
                    .head(BasicEducationExcelDTO.class)
                    .sheet("基础教育")
                    .doReadSync();
            if (basicEducationData != null && basicEducationData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：基础教育Sheet数据不能超过" + MAX_IMPORT_ROWS + "行");
            }

            // Sheet: 交通
            List<TransportationExcelDTO> transportationData = EasyExcel.read(file.getInputStream())
                    .head(TransportationExcelDTO.class)
                    .sheet("交通")
                    .doReadSync();
            if (transportationData != null && transportationData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：交通Sheet数据不能超过" + MAX_IMPORT_ROWS + "行");
            }

            // Sheet: 就业
            List<EmploymentExcelDTO> employmentData = EasyExcel.read(file.getInputStream())
                    .head(EmploymentExcelDTO.class)
                    .sheet("就业")
                    .doReadSync();
            if (employmentData != null && employmentData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：就业Sheet数据不能超过" + MAX_IMPORT_ROWS + "行");
            }

            // Sheet: 企业统计
            List<EnterpriseStatsExcelDTO> enterpriseStatsData = EasyExcel.read(file.getInputStream())
                    .head(EnterpriseStatsExcelDTO.class)
                    .sheet("企业统计")
                    .doReadSync();
            if (enterpriseStatsData != null && enterpriseStatsData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：企业统计Sheet数据不能超过" + MAX_IMPORT_ROWS + "行");
            }

            // Sheet: 未来规划
            List<FuturePlanExcelDTO> futurePlanData = EasyExcel.read(file.getInputStream())
                    .head(FuturePlanExcelDTO.class)
                    .sheet("未来规划")
                    .doReadSync();
            if (futurePlanData != null && futurePlanData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：未来规划Sheet数据不能超过" + MAX_IMPORT_ROWS + "行");
            }

            // Sheet: 文化旅游
            List<CultureExcelDTO> cultureData = EasyExcel.read(file.getInputStream())
                    .head(CultureExcelDTO.class)
                    .sheet("文化旅游")
                    .doReadSync();
            if (cultureData != null && cultureData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：文化旅游Sheet数据不能超过" + MAX_IMPORT_ROWS + "行");
            }

            // Sheet: 消费
            List<ConsumptionExcelDTO> consumptionData = EasyExcel.read(file.getInputStream())
                    .head(ConsumptionExcelDTO.class)
                    .sheet("消费")
                    .doReadSync();
            if (consumptionData != null && consumptionData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：消费Sheet数据不能超过" + MAX_IMPORT_ROWS + "行");
            }

            // Sheet: 医疗
            List<MedicalExcelDTO> medicalData = EasyExcel.read(file.getInputStream())
                    .head(MedicalExcelDTO.class)
                    .sheet("医疗")
                    .doReadSync();
            if (medicalData != null && medicalData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：医疗Sheet数据不能超过" + MAX_IMPORT_ROWS + "行");
            }

            // Sheet: 住房政策
            List<HousingPolicyExcelDTO> housingPolicyData = EasyExcel.read(file.getInputStream())
                    .head(HousingPolicyExcelDTO.class)
                    .sheet("住房政策")
                    .doReadSync();
            if (housingPolicyData != null && housingPolicyData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：住房政策Sheet数据不能超过" + MAX_IMPORT_ROWS + "行");
            }

            // Sheet: 租房成本
            List<RentalCostExcelDTO> rentalCostData = EasyExcel.read(file.getInputStream())
                    .head(RentalCostExcelDTO.class)
                    .sheet("租房成本")
                    .doReadSync();
            if (rentalCostData != null && rentalCostData.size() > MAX_IMPORT_ROWS) {
                throw new BusinessException(400, "导入失败：租房成本Sheet数据不能超过" + MAX_IMPORT_ROWS + "行");
            }

            // 按城市名称分组JSONB数据
            Map<String, Map<String, Object>> industryStructureMap = buildJsonbMap(industryStructureData,
                    dto -> dto.getCityName() == null ? null : dto.getCityName().trim(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("primaryRatio", dto.getPrimaryRatio());
                        m.put("secondaryRatio", dto.getSecondaryRatio());
                        m.put("tertiaryRatio", dto.getTertiaryRatio());
                        return m;
                    });

            Map<String, Map<String, Object>> housingPriceMap = buildJsonbMap(housingPriceData,
                    dto -> dto.getCityName() == null ? null : dto.getCityName().trim(),
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
                    dto -> dto.getCityName() == null ? null : dto.getCityName().trim(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("totalColleges", dto.getTotalColleges());
                        m.put("doubleFirstClassCount", dto.getDoubleFirstClassCount());
                        m.put("undergraduateCount", dto.getUndergraduateCount());
                        m.put("graduateCount", dto.getGraduateCount());
                        return m;
                    });

            Map<String, Map<String, Object>> basicEducationMap = buildJsonbMap(basicEducationData,
                    dto -> dto.getCityName() == null ? null : dto.getCityName().trim(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("totalSchools", dto.getTotalSchools());
                        m.put("modelSchoolCount", dto.getModelSchoolCount());
                        m.put("keySchoolCount", dto.getKeySchoolCount());
                        m.put("educationNote", dto.getEducationNote());
                        return m;
                    });

            Map<String, Map<String, Object>> transportationMap = buildJsonbMap(transportationData,
                    dto -> dto.getCityName() == null ? null : dto.getCityName().trim(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("metroLines", dto.getMetroLines());
                        m.put("metroMileage", dto.getMetroMileage());
                        m.put("highwayMileage", dto.getHighwayMileage());
                        m.put("trafficWorldRank", dto.getTrafficWorldRank());
                        return m;
                    });

            Map<String, Map<String, Object>> employmentMap = buildJsonbMap(employmentData,
                    dto -> dto.getCityName() == null ? null : dto.getCityName().trim(),
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
                    dto -> dto.getCityName() == null ? null : dto.getCityName().trim(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("enterpriseCategories", dto.getEnterpriseCategories());
                        m.put("keyEnterpriseCount", dto.getKeyEnterpriseCount());
                        m.put("fortune500Count", dto.getFortune500Count());
                        return m;
                    });

            Map<String, Map<String, Object>> futurePlanMap = buildJsonbMap(futurePlanData,
                    dto -> dto.getCityName() == null ? null : dto.getCityName().trim(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("targetYear", dto.getTargetYear());
                        m.put("developmentGoal", dto.getDevelopmentGoal());
                        m.put("keyAreas", dto.getKeyAreas());
                        return m;
                    });

            Map<String, Map<String, Object>> cultureMap = buildJsonbMap(cultureData,
                    dto -> dto.getCityName() == null ? null : dto.getCityName().trim(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("worldHeritageCount", dto.getWorldHeritageCount());
                        m.put("annualTourists", dto.getAnnualTourists());
                        m.put("aScenicCount", dto.getAScenicCount());
                        m.put("coreAttractions", dto.getCoreAttractions());
                        return m;
                    });

            Map<String, Map<String, Object>> consumptionMap = buildJsonbMap(consumptionData,
                    dto -> dto.getCityName() == null ? null : dto.getCityName().trim(),
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
                    dto -> dto.getCityName() == null ? null : dto.getCityName().trim(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("topHospitalCount", dto.getTopHospitalCount());
                        m.put("tertiaryHospitalCount", dto.getTertiaryHospitalCount());
                        m.put("doctorDensity", dto.getDoctorDensity());
                        m.put("medicalRank", dto.getMedicalRank());
                        return m;
                    });

            Map<String, Map<String, Object>> housingPolicyMap = buildJsonbMap(housingPolicyData,
                    dto -> dto.getCityName() == null ? null : dto.getCityName().trim(),
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
                    dto -> dto.getCityName() == null ? null : dto.getCityName().trim(),
                    dto -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("downtownRentRange", dto.getDowntownRentRange());
                        m.put("suburbanRentRange", dto.getSuburbanRentRange());
                        m.put("rentIncomeRatio", dto.getRentIncomeRatio());
                        m.put("rentGrowthRate", dto.getRentGrowthRate());
                        return m;
                    });

            // 校验其他Sheet中的城市名是否都在详情基础字段Sheet中存在
            Set<String> sheet0CityNames = new HashSet<>();
            for (CityDetailExcelDTO dto : detailData) {
                if (StringUtils.hasText(dto.getCityName())) {
                    sheet0CityNames.add(dto.getCityName().trim());
                }
            }
            validateSheetCityNames(industryStructureData, "产业结构", IndustryStructureExcelDTO::getCityName, sheet0CityNames, errorMsgs);
            validateSheetCityNames(housingPriceData, "房价水平", HousingPriceLevelExcelDTO::getCityName, sheet0CityNames, errorMsgs);
            validateSheetCityNames(highEducationData, "高等教育", HighEducationExcelDTO::getCityName, sheet0CityNames, errorMsgs);
            validateSheetCityNames(basicEducationData, "基础教育", BasicEducationExcelDTO::getCityName, sheet0CityNames, errorMsgs);
            validateSheetCityNames(transportationData, "交通", TransportationExcelDTO::getCityName, sheet0CityNames, errorMsgs);
            validateSheetCityNames(employmentData, "就业", EmploymentExcelDTO::getCityName, sheet0CityNames, errorMsgs);
            validateSheetCityNames(enterpriseStatsData, "企业统计", EnterpriseStatsExcelDTO::getCityName, sheet0CityNames, errorMsgs);
            validateSheetCityNames(futurePlanData, "未来规划", FuturePlanExcelDTO::getCityName, sheet0CityNames, errorMsgs);
            validateSheetCityNames(cultureData, "文化旅游", CultureExcelDTO::getCityName, sheet0CityNames, errorMsgs);
            validateSheetCityNames(consumptionData, "消费", ConsumptionExcelDTO::getCityName, sheet0CityNames, errorMsgs);
            validateSheetCityNames(medicalData, "医疗", MedicalExcelDTO::getCityName, sheet0CityNames, errorMsgs);
            validateSheetCityNames(housingPolicyData, "住房政策", HousingPolicyExcelDTO::getCityName, sheet0CityNames, errorMsgs);
            validateSheetCityNames(rentalCostData, "租房成本", RentalCostExcelDTO::getCityName, sheet0CityNames, errorMsgs);

            // 缓存城市ID
            Map<String, Long> cityIdCache = new HashMap<>();

            // 处理详情基础字段
            int updatedCount = 0;
            for (int i = 0; i < detailData.size(); i++) {
                int rowNum = i + 2;
                CityDetailExcelDTO data = detailData.get(i);

                String cityName = data.getCityName() == null ? null : data.getCityName().trim();

                if (!StringUtils.hasText(cityName)) {
                    errorMsgs.add("详情基础字段第" + rowNum + "行：城市名称不能为空");
                    continue;
                }

                // 校验cityLevel枚举值
                if (data.getCityLevel() != null && !VALID_CITY_LEVELS.contains(data.getCityLevel())) {
                    errorMsgs.add("详情基础字段第" + rowNum + "行：城市级别'" + data.getCityLevel() + "'不合法，可选值：直辖市、省会城市、地级市、县级市");
                    continue;
                }

                // 校验subtitle长度
                if (data.getSubtitle() != null && data.getSubtitle().length() > 200) {
                    errorMsgs.add("详情基础字段第" + rowNum + "行：副标题不能超过200个字符");
                    continue;
                }

                // 校验百分比字段范围0-100
                String pctError = validatePercentageRange(data.getUrbanizationRate(), "城镇化率", rowNum);
                if (pctError != null) { errorMsgs.add(pctError); continue; }
                pctError = validatePercentageRange(data.getRuralPopRatio(), "农村人口比例", rowNum);
                if (pctError != null) { errorMsgs.add(pctError); continue; }
                pctError = validatePercentageRange(data.getAgingRate(), "老龄化率", rowNum);
                if (pctError != null) { errorMsgs.add(pctError); continue; }
                pctError = validatePercentageRange(data.getMigrantPopRatio(), "外来人口比例", rowNum);
                if (pctError != null) { errorMsgs.add(pctError); continue; }

                // 本行DB操作整体包try-catch：异常转成"第N行"行级错误，否则丢失行号
                try {
                    Long cityId = cityIdCache.get(cityName);
                    if (cityId == null) {
                        LambdaQueryWrapper<City> wrapper = new LambdaQueryWrapper<>();
                        wrapper.eq(City::getCityName, cityName)
                               .eq(City::getIsDeleted, false);
                        City city = cityMapper.selectOne(wrapper);
                        if (city == null) {
                            errorMsgs.add("详情基础字段第" + rowNum + "行：城市名称'" + cityName + "'不存在");
                            continue;
                        }
                        cityId = city.getId();
                        cityIdCache.put(cityName, cityId);
                    }

                    // 查询详情记录
                    CityDetail detail = cityDetailMapper.findByCityId(cityId);
                    if (detail == null) {
                        errorMsgs.add("详情基础字段第" + rowNum + "行：城市'" + cityName + "'的详情记录不存在");
                        continue;
                    }

                    // 补齐标量字段：DB为空(NULL/空串)才填，已有数据一律不覆盖
                    if (!StringUtils.hasText(detail.getSubtitle()) && StringUtils.hasText(data.getSubtitle())) {
                        detail.setSubtitle(data.getSubtitle());
                    }
                    if (!StringUtils.hasText(detail.getCityLevel()) && StringUtils.hasText(data.getCityLevel())) {
                        detail.setCityLevel(data.getCityLevel());
                    }
                    if (!StringUtils.hasText(detail.getAdminCode()) && StringUtils.hasText(data.getAdminCode())) {
                        detail.setAdminCode(data.getAdminCode());
                    }
                    if (detail.getArea() == null && data.getArea() != null) {
                        detail.setArea(data.getArea());
                    }
                    if (detail.getPerCapitaGdp() == null && data.getPerCapitaGdp() != null) {
                        detail.setPerCapitaGdp(data.getPerCapitaGdp());
                    }
                    if (detail.getUrbanizationRate() == null && data.getUrbanizationRate() != null) {
                        detail.setUrbanizationRate(data.getUrbanizationRate());
                    }
                    if (detail.getRuralPopRatio() == null && data.getRuralPopRatio() != null) {
                        detail.setRuralPopRatio(data.getRuralPopRatio());
                    }
                    if (detail.getAgingRate() == null && data.getAgingRate() != null) {
                        detail.setAgingRate(data.getAgingRate());
                    }
                    if (detail.getMigrantPopRatio() == null && data.getMigrantPopRatio() != null) {
                        detail.setMigrantPopRatio(data.getMigrantPopRatio());
                    }
                    if (detail.getGdpGrowthRate() == null && data.getGdpGrowthRate() != null) {
                        detail.setGdpGrowthRate(data.getGdpGrowthRate());
                    }
                    if (detail.getFortune500Count() == null && data.getFortune500Count() != null) {
                        detail.setFortune500Count(data.getFortune500Count());
                    }
                    if (!StringUtils.hasText(detail.getIndustryDescription()) && StringUtils.hasText(data.getIndustryDescription())) {
                        detail.setIndustryDescription(data.getIndustryDescription());
                    }
                    if (isBlankList(detail.getMainIndustries()) && data.getMainIndustries() != null && !data.getMainIndustries().isEmpty()) {
                        detail.setMainIndustries(data.getMainIndustries());
                    }
                    if (isBlankList(detail.getEmergingIndustries()) && data.getEmergingIndustries() != null && !data.getEmergingIndustries().isEmpty()) {
                        detail.setEmergingIndustries(data.getEmergingIndustries());
                    }

                    // 补齐JSONB字段：DB为空对象/空也视为空（建表DEFAULT '{}'），已有真实数据才不覆盖
                    fillJsonbIfBlank(detail::setIndustryStructure, detail::getIndustryStructure, industryStructureMap.get(cityName));
                    fillJsonbIfBlank(detail::setHousingPriceLevel, detail::getHousingPriceLevel, housingPriceMap.get(cityName));
                    fillJsonbIfBlank(detail::setHighEducation, detail::getHighEducation, highEducationMap.get(cityName));
                    fillJsonbIfBlank(detail::setBasicEducation, detail::getBasicEducation, basicEducationMap.get(cityName));
                    fillJsonbIfBlank(detail::setTransportation, detail::getTransportation, transportationMap.get(cityName));
                    fillJsonbIfBlank(detail::setEmployment, detail::getEmployment, employmentMap.get(cityName));
                    fillJsonbIfBlank(detail::setEnterpriseStats, detail::getEnterpriseStats, enterpriseStatsMap.get(cityName));
                    fillJsonbIfBlank(detail::setFuturePlan, detail::getFuturePlan, futurePlanMap.get(cityName));
                    fillJsonbIfBlank(detail::setCulture, detail::getCulture, cultureMap.get(cityName));
                    fillJsonbIfBlank(detail::setConsumption, detail::getConsumption, consumptionMap.get(cityName));
                    fillJsonbIfBlank(detail::setMedical, detail::getMedical, medicalMap.get(cityName));
                    fillJsonbIfBlank(detail::setHousingPolicy, detail::getHousingPolicy, housingPolicyMap.get(cityName));
                    fillJsonbIfBlank(detail::setRentalCost, detail::getRentalCost, rentalCostMap.get(cityName));

                    detail.setUpdatedAt(OffsetDateTime.now());
                    cityDetailMapper.updateById(detail);
                    updatedCount++;
                } catch (Exception e) {
                    errorMsgs.add("详情基础字段第" + rowNum + "行：数据库操作失败[" + cityName + "]：" + e.getMessage());
                }
            }

            if (!errorMsgs.isEmpty()) {
                int shown = Math.min(errorMsgs.size(), MAX_ERROR_DISPLAY);
                throw new BusinessException(400, "导入失败，共" + errorMsgs.size() + "行数据存在错误（仅展示前"
                        + shown + "条）：" + String.join("；", errorMsgs.subList(0, shown)));
            }

            log.info("导入城市详情成功，更新数量={}", updatedCount);

        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(500, "读取Excel文件失败");
        } catch (Exception e) {
            log.error("解析Excel数据失败", e);
            throw new BusinessException(400, "解析Excel数据失败，请检查Excel格式和数据类型是否正确");
        }
    }

    /**
     * 构建JSONB Map的辅助方法（过滤null值）
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
                    Map<String, Object> m = valueExtractor.apply(data);
                    m.values().removeIf(Objects::isNull);
                    result.put(key, m);
                }
            }
        }
        return result;
    }

    /**
     * 校验百分比字段范围0-100
     */
    private String validatePercentageRange(BigDecimal value, String fieldName, int rowNum) {
        if (value != null && (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(HUNDRED) > 0)) {
            return "详情基础字段第" + rowNum + "行：" + fieldName + "必须在0-100之间";
        }
        return null;
    }

    /**
     * 校验Sheet中的城市名是否都在详情基础字段Sheet中存在
     */
    private <T> void validateSheetCityNames(List<T> dataList, String sheetName,
                                             java.util.function.Function<T, String> nameExtractor,
                                             Set<String> validNames, List<String> errorMsgs) {
        if (dataList == null) return;
        for (int i = 0; i < dataList.size(); i++) {
            String cityName = nameExtractor.apply(dataList.get(i));
            if (cityName != null) cityName = cityName.trim();
            if (StringUtils.hasText(cityName) && !validNames.contains(cityName)) {
                errorMsgs.add(sheetName + "第" + (i + 2) + "行：城市名称'" + cityName + "'在详情基础字段Sheet中不存在");
            }
        }
    }

    /**
     * 城市主表"补空"合并：仅当库中该列为 NULL/空 且导入有值时才填入，已有数据一律不覆盖。
     * 返回是否有字段被实际补齐。
     */
    private boolean mergeCityIfBlank(City existing, CityExcelDTO data) {
        boolean changed = false;
        String region = data.getRegion() == null ? null : data.getRegion().trim();
        if (!StringUtils.hasText(existing.getRegion()) && StringUtils.hasText(region)) {
            existing.setRegion(region);
            changed = true;
        }
        if (!StringUtils.hasText(existing.getCityIntro()) && StringUtils.hasText(data.getCityIntro())) {
            existing.setCityIntro(data.getCityIntro());
            changed = true;
        }
        if (existing.getCollegeCount() == null && data.getCollegeCount() != null) {
            existing.setCollegeCount(data.getCollegeCount());
            changed = true;
        }
        if (existing.getKeyCollegeCount() == null && data.getKeyCollegeCount() != null) {
            existing.setKeyCollegeCount(data.getKeyCollegeCount());
            changed = true;
        }
        if (existing.getResidentPopulation() == null && data.getResidentPopulation() != null) {
            existing.setResidentPopulation(data.getResidentPopulation());
            changed = true;
        }
        if (existing.getGdp() == null && data.getGdp() != null) {
            existing.setGdp(data.getGdp());
            changed = true;
        }
        return changed;
    }

    /**
     * 城市详情JSONB字段"补空"合并：仅当导入有数据且库中为空(NULL或空对象)才填入，已有真实数据不覆盖。
     */
    private void fillJsonbIfBlank(java.util.function.Consumer<Map<String, Object>> setter,
                                  java.util.function.Supplier<Map<String, Object>> getter,
                                  Map<String, Object> importValue) {
        if (importValue == null || importValue.isEmpty()) {
            return;
        }
        Map<String, Object> current = getter.get();
        // DB 列 DEFAULT '{}'，加载后是空对象而非 NULL，必须把 empty 也视为"空"才允许补齐
        if (current != null && !current.isEmpty()) {
            return;
        }
        setter.accept(new LinkedHashMap<>(importValue));
    }

    private boolean isBlankList(List<?> list) {
        return list == null || list.isEmpty();
    }
}
