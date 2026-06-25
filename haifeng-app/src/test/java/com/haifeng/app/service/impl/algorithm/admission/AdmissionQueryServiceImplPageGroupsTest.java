package com.haifeng.app.service.impl.algorithm.admission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.algorithm.admission.AdmissionGroupQueryDTO;
import com.haifeng.common.entity.algorithm.AdmissionGroup;
import com.haifeng.common.entity.algorithm.MemberGaokao;
import com.haifeng.common.mapper.algorithm.AdmissionGroupMapper;
import com.haifeng.common.mapper.algorithm.AdmissionMajorScoreMapper;
import com.haifeng.common.mapper.algorithm.ConstraintDictMapper;
import com.haifeng.common.mapper.algorithm.MemberGaokaoMapper;
import com.haifeng.common.mapper.algorithm.ProvinceConfigMapper;
import com.haifeng.common.mapper.algorithm.ProvinceReformMapper;
import com.haifeng.common.mapper.algorithm.ScoreRankMapper;
import com.haifeng.common.mapper.major.MajorMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.service.algorithm.matcher.ConstraintMatcherService;
import com.haifeng.common.service.algorithm.matcher.SubjectMatcher;
import com.haifeng.common.service.algorithm.safety.SafetyLevelDictCache;
import com.haifeng.common.service.algorithm.safety.SafetyLevelService;
import com.haifeng.common.util.RedisScanUtil;
import com.haifeng.common.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdmissionQueryServiceImplPageGroupsTest {

    @Mock private UniversityMapper universityMapper;
    @Mock private MajorMapper majorMapper;
    @Mock private MemberGaokaoMapper memberGaokaoMapper;
    @Mock private AdmissionGroupMapper admissionGroupMapper;
    @Mock private AdmissionMajorScoreMapper admissionMajorScoreMapper;
    @Mock private SubjectMatcher subjectMatcher;
    @Mock private SafetyLevelService safetyLevelService;
    @Mock private SafetyLevelDictCache safetyLevelDictCache;
    @Mock private ConstraintMatcherService constraintMatcherService;
    @Mock private ScoreRankMapper scoreRankMapper;
    @Mock private ProvinceConfigMapper provinceConfigMapper;
    @Mock private ProvinceReformMapper provinceReformMapper;
    @Mock private ConstraintDictMapper constraintDictMapper;
    @Mock private StringRedisTemplate stringRedisTemplate;
    @Mock private RedisScanUtil redisScanUtil;
    @Mock private ZSetOperations<String, String> zSetOperations;

    @InjectMocks private AdmissionQueryServiceImpl service;

    private AdmissionGroupQueryDTO dto;
    private MemberGaokao gaokao;

    @BeforeEach
    void setUp() {
        dto = new AdmissionGroupQueryDTO();
        gaokao = MemberGaokao.builder()
                .gaokaoProvince("北京")
                .gaokaoYear((short) 2025)
                .batch("本科批")
                .subjectType("物理")
                .score(600)
                .build();
    }

    // ========== 回归保护:无新筛选 → 原 ZSet 路径 ==========

    @Test
    void pageGroups_paidNoNewFilter_usesZSetRangeByScore() {
        try (MockedStatic<SecurityUtil> sec = mockStatic(SecurityUtil.class)) {
            sec.when(SecurityUtil::getCurrentMemberId).thenReturn(1L);
            sec.when(SecurityUtil::getCurrentMemberType).thenReturn("pro");
            when(memberGaokaoMapper.selectByMemberId(1L)).thenReturn(gaokao);
            when(stringRedisTemplate.hasKey(anyString())).thenReturn(true);
            when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
            Set<ZSetOperations.TypedTuple<String>> tuples = new HashSet<>();
            tuples.add(ZSetOperations.TypedTuple.of("1", 0.85));
            when(zSetOperations.reverseRangeByScoreWithScores(anyString(), anyDouble(), anyDouble(), anyLong(), anyLong()))
                    .thenReturn(tuples);
            when(zSetOperations.count(anyString(), anyDouble(), anyDouble())).thenReturn(1L);
            when(admissionGroupMapper.selectBatchIds(anyList())).thenReturn(Collections.<AdmissionGroup>emptyList());

            IPage<?> result = service.pageGroups(dto);

            verify(zSetOperations).reverseRangeByScoreWithScores(anyString(), anyDouble(), anyDouble(), anyLong(), anyLong());
            verify(zSetOperations, never()).reverseRangeByScoreWithScores(anyString(), anyDouble(), anyDouble());
            assertNotNull(result);
        }
    }

    // ========== 新筛选路径:有 universityIds → 走 ZSet 全量 ==========

    @Test
    void pageGroups_paidWithUniversityIds_usesZSetFullRange() {
        dto.setUniversityIds(List.of(100L, 200L));

        try (MockedStatic<SecurityUtil> sec = mockStatic(SecurityUtil.class)) {
            sec.when(SecurityUtil::getCurrentMemberId).thenReturn(1L);
            sec.when(SecurityUtil::getCurrentMemberType).thenReturn("vip");
            when(memberGaokaoMapper.selectByMemberId(1L)).thenReturn(gaokao);
            when(stringRedisTemplate.hasKey(anyString())).thenReturn(true);
            when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
            Set<ZSetOperations.TypedTuple<String>> tuples = new HashSet<>();
            tuples.add(ZSetOperations.TypedTuple.of("1", 0.85));
            when(zSetOperations.reverseRangeByScoreWithScores(anyString(), anyDouble(), anyDouble()))
                    .thenReturn(tuples);
            when(admissionGroupMapper.selectBatchIds(anyList())).thenReturn(Collections.<AdmissionGroup>emptyList());

            service.pageGroups(dto);

            verify(zSetOperations).reverseRangeByScoreWithScores(anyString(), anyDouble(), anyDouble());
            verify(zSetOperations, never()).reverseRangeByScoreWithScores(anyString(), anyDouble(), anyDouble(), anyLong(), anyLong());
        }
    }

    // ========== 安全范围校验 ==========

    @Test
    void pageGroups_minGreaterThanMax_throwsBusinessException() {
        dto.setMinSafetyLevel(new BigDecimal("0.80"));
        dto.setMaxSafetyLevel(new BigDecimal("0.50"));

        try (MockedStatic<SecurityUtil> sec = mockStatic(SecurityUtil.class)) {
            sec.when(SecurityUtil::getCurrentMemberId).thenReturn(1L);
            when(memberGaokaoMapper.selectByMemberId(1L)).thenReturn(gaokao);

            assertThrows(com.haifeng.common.exception.BusinessException.class,
                    () -> service.pageGroups(dto));
        }
    }
}
