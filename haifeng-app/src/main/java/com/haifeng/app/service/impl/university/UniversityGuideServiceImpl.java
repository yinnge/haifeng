package com.haifeng.app.service.impl.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.service.university.UniversityGuideService;
import com.haifeng.app.vo.university.*;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.entity.university.UniversityGuide;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.UniversityGuideMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UniversityGuideServiceImpl implements UniversityGuideService {

    private static final short STATUS_PUBLISHED = 1;

    private final UniversityGuideMapper guideMapper;
    private final UniversityMapper universityMapper;

    @Override
    public UniversityGuideOverviewVO overview(Long universityId) {
        University univ = universityMapper.selectById(universityId);
        if (univ == null || univ.getStatus() == null || univ.getStatus() != STATUS_PUBLISHED) {
            log.debug("院校不存在或已下架, universityId={}", universityId);
            throw new BusinessException(ResultCode.NOT_FOUND, "院校不存在");
        }

        UniversityGuide guide = loadGuide(universityId);

        return UniversityGuideOverviewVO.builder()
                .customTags(guide.getCustomTags())
                .name(univ.getName())
                .tags(univ.getTags())
                .region(univ.getRegion())
                .category(univ.getCategory())
                .nature(univ.getNature())
                .imageUrl(univ.getImageUrl())
                .build();
    }

    @Override
    public UniversityGuideSurvivalVO survival(Long universityId) {
        UniversityGuide g = loadGuide(universityId);
        return UniversityGuideSurvivalVO.builder()
                .campusFacilities(g.getCampusFacilities())
                .dormitoryServices(g.getDormitoryServices())
                .campusTransportation(g.getCampusTransportation())
                .build();
    }

    @Override
    public UniversityGuideAcademicVO academic(Long universityId) {
        UniversityGuide g = loadGuide(universityId);
        return UniversityGuideAcademicVO.builder()
                .academicGuidance(g.getAcademicGuidance())
                .majorTransferGuidelines(g.getMajorTransferGuidelines())
                .majorTransferConstriction(g.getMajorTransferConstriction())
                .academicSupportResources(g.getAcademicSupportResources())
                .build();
    }

    @Override
    public UniversityGuideSocialVO social(Long universityId) {
        UniversityGuide g = loadGuide(universityId);
        return UniversityGuideSocialVO.builder()
                .studentOrganizations(g.getStudentOrganizations())
                .campusEvents(g.getCampusEvents())
                .classDormSocial(g.getClassDormSocial())
                .build();
    }

    @Override
    public UniversityGuideSafetyVO safety(Long universityId) {
        UniversityGuide g = loadGuide(universityId);
        return UniversityGuideSafetyVO.builder()
                .financialAid(g.getFinancialAid())
                .campusSecurity(g.getCampusSecurity())
                .healthServices(g.getHealthServices())
                .build();
    }

    @Override
    public UniversityGuideLifeVO life(Long universityId) {
        UniversityGuide g = loadGuide(universityId);
        return UniversityGuideLifeVO.builder()
                .lifeServices(g.getLifeServices())
                .build();
    }

    /** 统一指南加载 + 校验，不存在抛 404 */
    private UniversityGuide loadGuide(Long universityId) {
        UniversityGuide g = guideMapper.selectOne(
                new LambdaQueryWrapper<UniversityGuide>()
                        .eq(UniversityGuide::getUniversityId, universityId)
                        .eq(UniversityGuide::getStatus, STATUS_PUBLISHED));
        if (g == null) {
            log.debug("院校适应指南不存在, universityId={}", universityId);
            throw new BusinessException(ResultCode.NOT_FOUND, "院校适应指南不存在");
        }
        return g;
    }
}
