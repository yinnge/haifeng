package com.haifeng.app.service.impl.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.university.AdmissionGroupQueryDTO;
import com.haifeng.app.service.university.AdmissionGroupService;
import com.haifeng.app.vo.university.AdmissionGroupDetailVO;
import com.haifeng.app.vo.university.AdmissionGroupListVO;
import com.haifeng.app.vo.university.AdmissionMajorScoreListVO;
import com.haifeng.common.entity.algorithm.AdmissionGroup;
import com.haifeng.common.entity.algorithm.AdmissionMajorScore;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.AdmissionGroupMapper;
import com.haifeng.common.mapper.algorithm.AdmissionMajorScoreMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdmissionGroupServiceImpl implements AdmissionGroupService {

    private final AdmissionGroupMapper groupMapper;
    private final AdmissionMajorScoreMapper majorScoreMapper;

    @Override
    public IPage<AdmissionGroupListVO> pageByUniversity(Long universityId, AdmissionGroupQueryDTO dto) {
        if (!ProvinceEnum.isValid(dto.getProvince())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "省份参数不合法");
        }

        int currentYear = OffsetDateTime.now().getYear();
        short minYear = (short) (currentYear - 5);

        Page<AdmissionGroup> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<AdmissionGroup> wrapper = new LambdaQueryWrapper<AdmissionGroup>()
                .eq(AdmissionGroup::getUniversityId, universityId)
                .eq(AdmissionGroup::getIsDeleted, false)
                .ge(AdmissionGroup::getYear, minYear)
                .eq(StringUtils.hasText(dto.getProvince()),
                        AdmissionGroup::getProvince, dto.getProvince())
                .eq(StringUtils.hasText(dto.getBatch()),
                        AdmissionGroup::getBatch, dto.getBatch())
                .like(StringUtils.hasText(dto.getCityName()),
                        AdmissionGroup::getCityName, dto.getCityName())
                .orderByDesc(AdmissionGroup::getYear)
                .orderByAsc(AdmissionGroup::getId);

        IPage<AdmissionGroup> entityPage = groupMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    @Override
    public AdmissionGroupDetailVO getDetail(Long groupId) {
        AdmissionGroup entity = groupMapper.selectById(groupId);
        if (entity == null || entity.getIsDeleted()) {
            log.debug("录取专业组不存在或已删除, groupId={}", groupId);
            throw new BusinessException(ResultCode.NOT_FOUND, "录取专业组不存在");
        }
        return toDetailVO(entity);
    }

    @Override
    public List<AdmissionMajorScoreListVO> listScores(Long groupId) {
        AdmissionGroup entity = groupMapper.selectById(groupId);
        if (entity == null || entity.getIsDeleted()) {
            log.debug("录取专业组不存在或已删除, groupId={}", groupId);
            throw new BusinessException(ResultCode.NOT_FOUND, "录取专业组不存在");
        }
        List<AdmissionMajorScore> list = majorScoreMapper.selectList(
                new LambdaQueryWrapper<AdmissionMajorScore>()
                        .eq(AdmissionMajorScore::getGroupId, groupId)
                        .eq(AdmissionMajorScore::getIsDeleted, false));
        return list.stream().map(this::toScoreVO).collect(Collectors.toList());
    }

    private AdmissionGroupListVO toListVO(AdmissionGroup e) {
        return AdmissionGroupListVO.builder()
                .id(e.getId())
                .groupCode(e.getGroupCode())
                .groupName(e.getGroupName())
                .year(e.getYear())
                .province(e.getProvince())
                .batch(e.getBatch())
                .cityName(e.getCityName())
                .subjects(e.getSubjects())
                .requirementType(e.getRequirementType())
                .majorCount(e.getMajorCount())
                .admissionCount(e.getAdmissionCount())
                .minScore(e.getMinScore())
                .minRank(e.getMinRank())
                .maxScore(e.getMaxScore())
                .maxRank(e.getMaxRank())
                .avgScore(e.getAvgScore())
                .avgRank(e.getAvgRank())
                .build();
    }

    private AdmissionGroupDetailVO toDetailVO(AdmissionGroup e) {
        return AdmissionGroupDetailVO.builder()
                .id(e.getId())
                .universityId(e.getUniversityId())
                .universityName(e.getUniversityName())
                .cityName(e.getCityName())
                .year(e.getYear())
                .province(e.getProvince())
                .batch(e.getBatch())
                .enrollmentCode(e.getEnrollmentCode())
                .groupCode(e.getGroupCode())
                .groupName(e.getGroupName())
                .subjects(e.getSubjects())
                .requirementType(e.getRequirementType())
                .description(e.getDescription())
                .constraints(e.getConstraints())
                .majorCount(e.getMajorCount())
                .categoryCount(e.getCategoryCount())
                .admissionCount(e.getAdmissionCount())
                .minScore(e.getMinScore())
                .minRank(e.getMinRank())
                .maxScore(e.getMaxScore())
                .maxRank(e.getMaxRank())
                .avgScore(e.getAvgScore())
                .avgRank(e.getAvgRank())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private AdmissionMajorScoreListVO toScoreVO(AdmissionMajorScore e) {
        return AdmissionMajorScoreListVO.builder()
                .id(e.getId())
                .groupId(e.getGroupId())
                .majorCode(e.getMajorCode())
                .majorName(e.getMajorName())
                .educationLevel(e.getEducationLevel())
                .duration(e.getDuration())
                .tuition(e.getTuition())
                .description(e.getDescription())
                .admissionCount(e.getAdmissionCount())
                .minScore(e.getMinScore())
                .minRank(e.getMinRank())
                .maxScore(e.getMaxScore())
                .maxRank(e.getMaxRank())
                .avgScore(e.getAvgScore())
                .avgRank(e.getAvgRank())
                .constraints(e.getConstraints())
                .build();
    }
}
