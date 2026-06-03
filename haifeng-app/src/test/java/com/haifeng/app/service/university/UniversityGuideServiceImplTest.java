package com.haifeng.app.service.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.service.impl.university.UniversityGuideServiceImpl;
import com.haifeng.app.vo.university.*;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.entity.university.UniversityGuide;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.UniversityGuideMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UniversityGuideServiceImplTest {

    @Mock private UniversityGuideMapper guideMapper;
    @Mock private UniversityMapper universityMapper;

    @InjectMocks private UniversityGuideServiceImpl service;

    private UniversityGuide sampleGuide() {
        return UniversityGuide.builder()
                .id(1L).universityId(100L).status((short) 1)
                .customTags(List.of("好食堂", "图书馆爆款"))
                .campusFacilities(Map.of("canteen", "5 个食堂"))
                .dormitoryServices(Map.of("ac", true))
                .campusTransportation(Map.of("shuttle", "校车"))
                .academicGuidance(Map.of("tutor", "导师制"))
                .majorTransferGuidelines(Map.of("rules", "..."))
                .majorTransferConstriction(Map.of("limit", "..."))
                .academicSupportResources(Map.of("library", "..."))
                .studentOrganizations(Map.of("clubs", 100))
                .campusEvents(Map.of("sports", "运动会"))
                .classDormSocial(Map.of("class", "..."))
                .financialAid(Map.of("scholarship", "..."))
                .campusSecurity(Map.of("guard", "24h"))
                .healthServices(Map.of("hospital", "校医院"))
                .lifeServices(Map.of("shop", "便利店"))
                .build();
    }

    @Test
    void overview_returnsTagsAndUniversityFields() {
        when(guideMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleGuide());
        University u = University.builder()
                .id(100L).name("清华").region("华北").category("综合")
                .nature("公办").imageUrl("img.png").tags(List.of("985"))
                .status((short) 1).build();
        when(universityMapper.selectById(100L)).thenReturn(u);

        UniversityGuideOverviewVO vo = service.overview(100L);

        assertThat(vo.getCustomTags()).contains("好食堂");
        assertThat(vo.getName()).isEqualTo("清华");
        assertThat(vo.getTags()).contains("985");
    }

    @Test
    void survival_returnsThreeSurvivalMaps() {
        when(guideMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleGuide());

        UniversityGuideSurvivalVO vo = service.survival(100L);

        assertThat(vo.getCampusFacilities()).containsEntry("canteen", "5 个食堂");
        assertThat(vo.getDormitoryServices()).containsEntry("ac", true);
        assertThat(vo.getCampusTransportation()).isNotNull();
    }

    @Test
    void academic_returnsFourAcademicMaps() {
        when(guideMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleGuide());

        UniversityGuideAcademicVO vo = service.academic(100L);

        assertThat(vo.getAcademicGuidance()).isNotNull();
        assertThat(vo.getMajorTransferGuidelines()).isNotNull();
        assertThat(vo.getMajorTransferConstriction()).isNotNull();
        assertThat(vo.getAcademicSupportResources()).isNotNull();
    }

    @Test
    void social_returnsThreeSocialMaps() {
        when(guideMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleGuide());

        UniversityGuideSocialVO vo = service.social(100L);

        assertThat(vo.getStudentOrganizations()).containsEntry("clubs", 100);
        assertThat(vo.getCampusEvents()).isNotNull();
        assertThat(vo.getClassDormSocial()).isNotNull();
    }

    @Test
    void safety_returnsThreeSafetyMaps() {
        when(guideMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleGuide());

        UniversityGuideSafetyVO vo = service.safety(100L);

        assertThat(vo.getFinancialAid()).isNotNull();
        assertThat(vo.getCampusSecurity()).containsEntry("guard", "24h");
        assertThat(vo.getHealthServices()).isNotNull();
    }

    @Test
    void life_returnsLifeServices() {
        when(guideMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleGuide());

        UniversityGuideLifeVO vo = service.life(100L);

        assertThat(vo.getLifeServices()).containsEntry("shop", "便利店");
    }

    @Test
    void survival_guideMissing_throws404() {
        when(guideMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThatThrownBy(() -> service.survival(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("院校适应指南不存在");
    }

    @Test
    void overview_universityMissing_throws404() {
        when(guideMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleGuide());
        when(universityMapper.selectById(100L)).thenReturn(null);

        assertThatThrownBy(() -> service.overview(100L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("院校不存在");
    }
}
