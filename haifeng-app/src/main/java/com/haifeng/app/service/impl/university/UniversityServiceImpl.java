package com.haifeng.app.service.impl.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.university.UniversityChannelQueryDTO;
import com.haifeng.app.dto.university.UniversityQueryDTO;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.app.service.university.UniversityService;
import com.haifeng.app.vo.university.ChannelOptionVO;
import com.haifeng.app.vo.university.UniversityBriefVO;
import com.haifeng.app.vo.university.UniversityChannelListVO;
import com.haifeng.app.vo.university.UniversityDetailVO;
import com.haifeng.app.vo.university.UniversityListVO;
import com.haifeng.app.vo.university.UniversityStatsVO;
import com.haifeng.common.entity.special.SpecialChannel;
import com.haifeng.common.entity.special.SpecialChannelUniversity;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.entity.university.UniversityDetail;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.special.SpecialChannelMapper;
import com.haifeng.common.mapper.special.SpecialChannelUniversityMapper;
import com.haifeng.common.mapper.university.UniversityDetailMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.ProvinceNameUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UniversityServiceImpl implements UniversityService {

    private static final short STATUS_PUBLISHED = 1;

    /** 本科统计口径：本科 + 本专兼招（本专兼招也属于本科层次办学） */
    private static final List<String> UNDERGRADUATE_LEVELS = List.of("本科", "本专兼招");

    private final UniversityMapper universityMapper;
    private final UniversityDetailMapper universityDetailMapper;
    private final SpecialChannelUniversityMapper specialChannelUniversityMapper;
    private final SpecialChannelMapper specialChannelMapper;

    @Override
    public IPage<UniversityListVO> page(UniversityQueryDTO dto) {
        // 招生通道筛选：统招=前端不传参（不传即查全部，不走关联表）；
        // 非统招经 t_special_channel_university 取 universityIds 后 IN 过滤
        List<Long> channelUniversityIds = null;
        if (StringUtils.hasText(dto.getChannelCode())) {
            channelUniversityIds = specialChannelUniversityMapper.selectList(
                            new LambdaQueryWrapper<SpecialChannelUniversity>()
                                    .select(SpecialChannelUniversity::getUniversityId)
                                    .eq(SpecialChannelUniversity::getChannelCode, dto.getChannelCode())
                                    .eq(SpecialChannelUniversity::getIsActive, true))
                    .stream()
                    .map(SpecialChannelUniversity::getUniversityId)
                    .distinct()
                    .toList();
            if (channelUniversityIds.isEmpty()) {
                // 该通道下无院校 → 直接返回空页，避免 IN () 生成非法 SQL
                return new Page<>(dto.getPage(), dto.getSize());
            }
        }

        // 省份兼容匹配：前端传简称（天津/黑龙江/内蒙古…），库中多存全称（天津市/黑龙江省/内蒙古自治区…），
        // 少量脏数据存简称（四川、北京）。两侧都剥后缀成核名再比较（详见 ProvinceNameUtil）。
        // stripSuffix(null) 返回 null，下面以「是否为 null」决定是否拼接该 SQL 条件。
        String province = StringUtils.hasText(dto.getProvinceName())
                ? ProvinceNameUtil.stripSuffix(dto.getProvinceName())
                : null;

        LambdaQueryWrapper<University> wrapper = new LambdaQueryWrapper<University>()
                .eq(University::getStatus, STATUS_PUBLISHED)
                .like(StringUtils.hasText(dto.getName()), University::getName, dto.getName())
                .apply(province != null,
                        ProvinceNameUtil.strippedColumnEquals("province_name"),
                        province)
                .eq(StringUtils.hasText(dto.getNature()), University::getNature, dto.getNature())
                .eq(StringUtils.hasText(dto.getCategory()), University::getCategory, dto.getCategory())
                .eq(StringUtils.hasText(dto.getEducationLevel()), University::getEducationLevel, dto.getEducationLevel())
                .in(channelUniversityIds != null, University::getId, channelUniversityIds);
        // 标签筛选：tags @> ARRAY['x']::text[]，多标签逐条拼接 = AND 交集；{0} 走绑定参数防注入
        if (dto.getTags() != null) {
            dto.getTags().stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .forEach(tag -> wrapper.apply("tags @> ARRAY[{0}]::text[]", tag));
        }
        // 排序仅 sort_order 倒序（无 id 兜底、无排序参数）
        wrapper.orderByDesc(University::getSortOrder);

        Page<University> page = new Page<>(dto.getPage(), dto.getSize());
        IPage<University> entityPage = universityMapper.selectPage(page, wrapper);

        // 封面图：批查详情表取轮播首图，避免逐行 N+1
        Map<Long, String> coverMap = loadCoverImages(entityPage.getRecords());
        return entityPage.convert(e -> toListVO(e, coverMap.get(e.getId())));
    }

    @Override
    public UniversityStatsVO stats() {
        return UniversityStatsVO.builder()
                .count985(countByTag("985"))
                .count211(countByTag("211"))
                .countDoubleFirstClass(countByTag("双一流"))
                .countUndergraduate(countByEducationLevels(UNDERGRADUATE_LEVELS))
                .build();
    }

    @Override
    public List<ChannelOptionVO> listSpecialChannelOptions() {
        List<SpecialChannel> channels = specialChannelMapper.selectList(
                new LambdaQueryWrapper<SpecialChannel>()
                        .eq(SpecialChannel::getIsActive, true)
                        .orderByAsc(SpecialChannel::getSortOrder));
        return channels.stream()
                .map(e -> ChannelOptionVO.builder()
                        .channelCode(e.getChannelCode())
                        .channelName(e.getChannelName())
                        .build())
                .toList();
    }

    @Override
    public UniversityDetailVO detail(Long universityId) {
        University univ = universityMapper.selectById(universityId);
        if (univ == null || univ.getStatus() == null || univ.getStatus() != STATUS_PUBLISHED) {
            log.debug("院校不存在或已下架, universityId={}", universityId);
            throw new BusinessException(ResultCode.NOT_FOUND, "院校不存在");
        }

        UniversityDetail detail = universityDetailMapper.selectOne(
                new LambdaQueryWrapper<UniversityDetail>()
                        .eq(UniversityDetail::getUniversityId, universityId)
                        .eq(UniversityDetail::getStatus, STATUS_PUBLISHED));
        if (detail == null) {
            log.debug("院校详情未配置, universityId={}", universityId);
            throw new BusinessException(ResultCode.NOT_FOUND, "院校详情不存在");
        }

        return UniversityDetailVO.builder()
                // detail
                .address(detail.getAddress())
                .admissionPhone(detail.getAdmissionPhone())
                .website(detail.getWebsite())
                .historyGroupScore(detail.getHistoryGroupScore())
                .scienceGroupScore(detail.getScienceGroupScore())
                .carouselImages(detail.getCarouselImages())
                .introduction(detail.getIntroduction())
                .rankings(detail.getRankings())
                .abroadRate(detail.getAbroadRate())
                .genderRatio(detail.getGenderRatio())
                // university
                .name(univ.getName())
                .nameEn(univ.getNameEn())
                .provinceName(univ.getProvinceName())
                .cityName(univ.getCityName())
                .region(univ.getRegion())
                .category(univ.getCategory())
                .majorCount(univ.getMajorCount())
                .educationLevel(univ.getEducationLevel())
                .nature(univ.getNature())
                .recommendationRate(univ.getRecommendationRate())
                .recommendationYear(univ.getRecommendationYear())
                .hasDoctorate(univ.getHasDoctorate())
                .hasMaster(univ.getHasMaster())
                .department(univ.getDepartment())
                .tags(univ.getTags())
                .famousUnion(univ.getFamousUnion())
                .build();
    }

    @Override
    public IPage<UniversityChannelListVO> pageChannels(Long universityId, UniversityChannelQueryDTO dto) {
        if (StringUtils.hasText(dto.getRegionTag()) && !ProvinceEnum.isValid(dto.getRegionTag())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "省份参数不合法");
        }
        Page<SpecialChannelUniversity> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<SpecialChannelUniversity> wrapper = new LambdaQueryWrapper<SpecialChannelUniversity>()
                .eq(SpecialChannelUniversity::getIsActive, true)
                .eq(SpecialChannelUniversity::getUniversityId, universityId)
                .like(StringUtils.hasText(dto.getChannelName()), SpecialChannelUniversity::getChannelName, dto.getChannelName())
                .eq(StringUtils.hasText(dto.getRegionTag()), SpecialChannelUniversity::getRegionTag, dto.getRegionTag())
                .orderByAsc(SpecialChannelUniversity::getSortOrder)
                .orderByDesc(SpecialChannelUniversity::getId);
        return specialChannelUniversityMapper.selectPage(page, wrapper).convert(e ->
                UniversityChannelListVO.builder()
                        .channelCode(e.getChannelCode())
                        .channelName(e.getChannelName())
                        .year(e.getYear())
                        .regionTag(e.getRegionTag())
                        .signupStart(e.getSignupStart())
                        .signupEnd(e.getSignupEnd())
                        .build());
    }

    @Override
    public List<ChannelOptionVO> listChannelOptions() {
        List<SpecialChannelUniversity> list = specialChannelUniversityMapper.selectDistinctActiveChannels();
        return list.stream()
                .map(e -> ChannelOptionVO.builder()
                        .channelCode(e.getChannelCode())
                        .channelName(e.getChannelName())
                        .build())
                .toList();
    }

    @Override
    public UniversityBriefVO getByName(String name) {
        University university = universityMapper.selectByName(name);
        if (university == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "院校不存在");
        }
        return UniversityBriefVO.builder()
                .id(university.getId())
                .name(university.getName())
                .provinceName(university.getProvinceName())
                .cityName(university.getCityName())
                .region(university.getRegion())
                .category(university.getCategory())
                .educationLevel(university.getEducationLevel())
                .nature(university.getNature())
                .recommendationRate(university.getRecommendationRate())
                .department(university.getDepartment())
                .tags(university.getTags())
                .imageUrl(university.getImageUrl())
                .build();
    }

    /** 批查 t_universities_detail 取轮播首图作为卡片封面；detail 缺失/轮播为空 → 不入 map（VO 为 null，前端占位图） */
    private Map<Long, String> loadCoverImages(List<University> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyMap();
        }
        List<UniversityDetail> details = universityDetailMapper.selectList(
                new LambdaQueryWrapper<UniversityDetail>()
                        .select(UniversityDetail::getUniversityId, UniversityDetail::getCarouselImages)
                        .in(UniversityDetail::getUniversityId, records.stream().map(University::getId).toList())
                        .eq(UniversityDetail::getStatus, STATUS_PUBLISHED));
        if (details == null || details.isEmpty()) {
            return Collections.emptyMap();
        }
        return details.stream()
                .filter(d -> d.getCarouselImages() != null && !d.getCarouselImages().isEmpty())
                .collect(Collectors.toMap(
                        UniversityDetail::getUniversityId,
                        d -> d.getCarouselImages().get(0),
                        (a, b) -> a));
    }

    private long countByTag(String tag) {
        Long count = universityMapper.selectCount(new LambdaQueryWrapper<University>()
                .eq(University::getStatus, STATUS_PUBLISHED)
                .apply("tags @> ARRAY[{0}]::text[]", tag));
        return count == null ? 0L : count;
    }

    private long countByEducationLevels(List<String> levels) {
        Long count = universityMapper.selectCount(new LambdaQueryWrapper<University>()
                .eq(University::getStatus, STATUS_PUBLISHED)
                .in(University::getEducationLevel, levels));
        return count == null ? 0L : count;
    }

    private UniversityListVO toListVO(University e, String coverImage) {
        return UniversityListVO.builder()
                .id(e.getId())
                .name(e.getName())
                .tags(e.getTags())
                .provinceName(e.getProvinceName())
                .cityName(e.getCityName())
                .region(e.getRegion())
                .category(e.getCategory())
                .majorCount(e.getMajorCount())
                .educationLevel(e.getEducationLevel())
                .nature(e.getNature())
                .imageUrl(e.getImageUrl())
                .coverImage(coverImage)
                .content(e.getIntroduction())
                .build();
    }
}
