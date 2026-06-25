package com.haifeng.app.service.impl.algorithm.admission;

import com.haifeng.common.dto.algorithm.admission.AdmissionMajorFilterDTO;
import com.haifeng.common.dto.algorithm.admission.AdmissionUniversityFilterDTO;
import com.haifeng.common.enums.PopulationBucketEnum;
import com.haifeng.common.mapper.major.MajorMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.vo.algorithm.admission.AdmissionMajorOptionVO;
import com.haifeng.common.vo.algorithm.admission.AdmissionUniversityOptionVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdmissionQueryServiceImplFilterTest {

    @Mock private UniversityMapper universityMapper;
    @Mock private MajorMapper majorMapper;
    @Mock private StringRedisTemplate stringRedisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks private AdmissionQueryServiceImpl service;

    // ========== filterUniversities ==========

    @Test
    void filterUniversities_nullDto_returnsEmpty() {
        assertTrue(service.filterUniversities(null).isEmpty());
    }

    @Test
    void filterUniversities_allFieldsNull_passesNullsToMapper() {
        AdmissionUniversityFilterDTO dto = new AdmissionUniversityFilterDTO();
        when(universityMapper.selectByFilter(eq(dto), isNull(), isNull(), isNull()))
                .thenReturn(Collections.emptyList());
        List<AdmissionUniversityOptionVO> result = service.filterUniversities(dto);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(universityMapper).selectByFilter(eq(dto), isNull(), isNull(), isNull());
    }

    @Test
    void filterUniversities_populationBucket_LT_2000_setsMin0Max2000() {
        AdmissionUniversityFilterDTO dto = new AdmissionUniversityFilterDTO();
        dto.setPopulationBucket(PopulationBucketEnum.LT_2000);
        when(universityMapper.selectByFilter(eq(dto), isNull(), eq(0), eq(2000)))
                .thenReturn(Collections.emptyList());
        service.filterUniversities(dto);
        verify(universityMapper).selectByFilter(eq(dto), isNull(), eq(0), eq(2000));
    }

    @Test
    void filterUniversities_populationBucket_GTE_4000_passesNullMax() {
        AdmissionUniversityFilterDTO dto = new AdmissionUniversityFilterDTO();
        dto.setPopulationBucket(PopulationBucketEnum.GTE_4000);
        when(universityMapper.selectByFilter(eq(dto), isNull(), eq(4000), isNull()))
                .thenReturn(Collections.emptyList());
        service.filterUniversities(dto);
        verify(universityMapper).selectByFilter(eq(dto), isNull(), eq(4000), isNull());
    }

    @Test
    void filterUniversities_tags_quotedAsPGArray() {
        AdmissionUniversityFilterDTO dto = new AdmissionUniversityFilterDTO();
        dto.setTags(List.of("985", "双一流"));
        when(universityMapper.selectByFilter(eq(dto), eq("{\"985\",\"双一流\"}"), isNull(), isNull()))
                .thenReturn(Collections.emptyList());
        service.filterUniversities(dto);
        verify(universityMapper).selectByFilter(eq(dto), eq("{\"985\",\"双一流\"}"), isNull(), isNull());
    }

    @Test
    void filterUniversities_returnsEmptyWhenMapperReturnsNull() {
        AdmissionUniversityFilterDTO dto = new AdmissionUniversityFilterDTO();
        when(universityMapper.selectByFilter(any(), any(), any(), any())).thenReturn(null);
        assertTrue(service.filterUniversities(dto).isEmpty());
    }

    // ========== filterMajors ==========

    @Test
    void filterMajors_nullDto_returnsEmpty() {
        assertTrue(service.filterMajors(null).isEmpty());
    }

    @Test
    void filterMajors_delegatesToMapper() {
        AdmissionMajorFilterDTO dto = new AdmissionMajorFilterDTO();
        dto.setMajorCategories(List.of("工学"));
        AdmissionMajorOptionVO vo = AdmissionMajorOptionVO.builder()
                .id(1L).majorCode("080901").majorName("计算机").build();
        when(majorMapper.selectByFilter(dto)).thenReturn(List.of(vo));
        List<AdmissionMajorOptionVO> result = service.filterMajors(dto);
        assertEquals(1, result.size());
        assertEquals("080901", result.get(0).getMajorCode());
    }

    // ========== listUniversityTags ==========

    @Test
    void listUniversityTags_cacheHit_returnsFromRedis() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("haifeng:university:tags:distinct")).thenReturn("985,211,双一流");
        List<String> tags = service.listUniversityTags();
        assertEquals(List.of("985", "211", "双一流"), tags);
        verify(universityMapper, never()).selectDistinctTags();
    }

    @Test
    void listUniversityTags_cacheMiss_loadsFromDbAndCaches() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("haifeng:university:tags:distinct")).thenReturn(null);
        when(universityMapper.selectDistinctTags()).thenReturn(List.of("985", "211"));
        doNothing().when(valueOperations).set(eq("haifeng:university:tags:distinct"), eq("985,211"), any(Duration.class));
        List<String> tags = service.listUniversityTags();
        assertEquals(List.of("985", "211"), tags);
        verify(valueOperations).set(eq("haifeng:university:tags:distinct"), eq("985,211"), any(Duration.class));
    }

    @Test
    void listUniversityTags_redisDown_returnsFromDb() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis down"));
        when(universityMapper.selectDistinctTags()).thenReturn(List.of("985"));
        List<String> tags = service.listUniversityTags();
        assertEquals(List.of("985"), tags);
    }
}
