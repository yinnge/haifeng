package com.haifeng.app.util.algorithm.pdf;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.vo.algorithm.pdf.CityEnrichmentVO;
import com.haifeng.app.vo.algorithm.pdf.MajorEnrichmentVO;
import com.haifeng.common.entity.city.City;
import com.haifeng.common.entity.city.CityDetail;
import com.haifeng.common.entity.major.Major;
import com.haifeng.common.entity.major.MajorDetail;
import com.haifeng.common.mapper.city.CityDetailMapper;
import com.haifeng.common.mapper.city.CityMapper;
import com.haifeng.common.mapper.major.MajorDetailMapper;
import com.haifeng.common.mapper.major.MajorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PDF 报告数据增强加载器
 * <p>直接使用 Mapper 查询（非 Service），以便在数据缺失时返回 null 优雅降级。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnrichmentLoader {

    private final CityMapper cityMapper;
    private final CityDetailMapper cityDetailMapper;
    private final MajorMapper majorMapper;
    private final MajorDetailMapper majorDetailMapper;

    /**
     * 按城市名加载城市增强数据
     *
     * @param cityName 城市名
     * @return 城市增强数据，查询失败时返回 null
     */
    public CityEnrichmentVO loadCity(String cityName) {
        if (cityName == null || cityName.isBlank()) {
            return null;
        }
        try {
            City city = cityMapper.selectOne(
                    new LambdaQueryWrapper<City>()
                            .eq(City::getCityName, cityName)
                            .eq(City::getIsDeleted, false)
                            .last("LIMIT 1"));
            if (city == null) {
                return null;
            }

            CityDetail detail = cityDetailMapper.findByCityId(city.getId());

            CityEnrichmentVO.CityEnrichmentVOBuilder builder = CityEnrichmentVO.builder()
                    .cityName(city.getCityName())
                    .gdp(city.getGdp());

            if (detail != null) {
                builder.cityLevel(detail.getCityLevel())
                        .gdpGrowthRate(detail.getGdpGrowthRate())
                        .fortune500Count(detail.getFortune500Count())
                        .mainIndustries(detail.getMainIndustries())
                        .emergingIndustries(detail.getEmergingIndustries())
                        .industryDescription(detail.getIndustryDescription());

                // 从 employment JSONB 提取 avgSalary / unemploymentRate
                Map<String, Object> employment = detail.getEmployment();
                if (employment != null) {
                    Object avgSalary = employment.get("avgSalary");
                    if (avgSalary instanceof Number) {
                        builder.avgSalary(new BigDecimal(avgSalary.toString()));
                    }
                    Object unemploymentRate = employment.get("unemploymentRate");
                    if (unemploymentRate instanceof Number) {
                        builder.unemploymentRate(new BigDecimal(unemploymentRate.toString()));
                    }
                }
            }

            return builder.build();
        } catch (Exception e) {
            log.warn("加载城市增强数据失败, cityName={}: {}", cityName, e.getMessage());
            return null;
        }
    }

    /**
     * 按专业ID加载专业增强数据
     *
     * @param majorId 专业ID
     * @return 专业增强数据，查询失败时返回 null
     */
    public MajorEnrichmentVO loadMajor(Long majorId) {
        if (majorId == null) {
            return null;
        }
        try {
            Major major = majorMapper.selectById(majorId);
            if (major == null) {
                return null;
            }

            MajorDetail detail = majorDetailMapper.selectByMajorId(majorId);

            return buildMajorEnrichment(major, detail);
        } catch (Exception e) {
            log.warn("加载专业增强数据失败, majorId={}: {}", majorId, e.getMessage());
            return null;
        }
    }

    /**
     * 批量按专业ID加载专业增强数据，避免 N+1 查询。
     * <p>对 t_major 和 t_major_detail 各执行一次批量查询（selectBatchIds / IN），
     * 在内存中按 majorId 关联。查询失败时返回空 Map（不抛异常，调用方应做 null 防御）。
     *
     * @param majorIds 专业ID集合
     * @return majorId -> 专业增强数据 的映射，未命中的 majorId 不在 Map 中
     */
    public Map<Long, MajorEnrichmentVO> loadMajorsBatch(Collection<Long> majorIds) {
        if (CollectionUtils.isEmpty(majorIds)) {
            return Collections.emptyMap();
        }
        try {
            List<Long> ids = majorIds.stream().filter(java.util.Objects::nonNull).distinct().collect(Collectors.toList());
            if (ids.isEmpty()) {
                return Collections.emptyMap();
            }

            // 1. 批量查 t_major
            List<Major> majors = majorMapper.selectBatchIds(ids);
            if (CollectionUtils.isEmpty(majors)) {
                return Collections.emptyMap();
            }
            Map<Long, Major> majorMap = majors.stream()
                    .collect(Collectors.toMap(Major::getId, m -> m, (a, b) -> a));

            // 2. 批量查 t_major_detail（按 major_id IN）
            List<MajorDetail> details = majorDetailMapper.selectList(
                    new LambdaQueryWrapper<MajorDetail>()
                            .in(MajorDetail::getMajorId, ids)
                            .eq(MajorDetail::getStatus, 1));
            Map<Long, MajorDetail> detailMap = CollectionUtils.isEmpty(details)
                    ? Collections.emptyMap()
                    : details.stream()
                            .filter(d -> d.getMajorId() != null)
                            .collect(Collectors.toMap(MajorDetail::getMajorId, d -> d, (a, b) -> a));

            // 3. 合并
            Map<Long, MajorEnrichmentVO> result = new HashMap<>(majors.size());
            for (Major major : majors) {
                MajorDetail detail = major.getId() != null ? detailMap.get(major.getId()) : null;
                result.put(major.getId(), buildMajorEnrichment(major, detail));
            }
            return result;
        } catch (Exception e) {
            log.warn("批量加载专业增强数据失败, majorIds.size={}: {}", majorIds.size(), e.getMessage());
            return Collections.emptyMap();
        }
    }

    private MajorEnrichmentVO buildMajorEnrichment(Major major, MajorDetail detail) {
        MajorEnrichmentVO.MajorEnrichmentVOBuilder builder = MajorEnrichmentVO.builder()
                .majorName(major.getMajorName())
                .majorCategory(major.getMajorCategory())
                .parentCategory(major.getParentCategory())
                .majorTags(major.getMajorTags())
                .degreeAwarded(major.getDegreeAwarded())
                .employmentRate(major.getEmploymentRate())
                .salaryMin(major.getSalaryMin())
                .salaryMax(major.getSalaryMax())
                .description(major.getDescription());

        if (detail != null) {
            builder.careerProspect(detail.getCareerProspect());
        }
        return builder.build();
    }
}
