package com.haifeng.admin.service.impl.algorithm.admission;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.admin.dto.algorithm.admission.AdmissionMajorScoreAddDTO;
import com.haifeng.common.entity.algorithm.AdmissionMajorScore;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.AdmissionGroupMapper;
import com.haifeng.common.mapper.algorithm.AdmissionMajorScoreMapper;
import com.haifeng.common.mapper.algorithm.ConstraintDictMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdmissionMajorScoreServiceImplTest {

    @Mock
    private AdmissionMajorScoreMapper admissionMajorScoreMapper;

    @Mock
    private AdmissionGroupMapper admissionGroupMapper;

    @Mock
    private ConstraintDictMapper constraintDictMapper;

    @InjectMocks
    private AdmissionMajorScoreServiceImpl admissionMajorScoreService;

    @Captor
    private ArgumentCaptor<AdmissionMajorScore> entityCaptor;

    @Test
    void addRejectsConstraintNameNotInActiveDict() {
        AdmissionMajorScoreAddDTO dto = addDTO();
        dto.setConstraints(List.of("不存在的限制"));
        when(admissionGroupMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        when(admissionMajorScoreMapper.countByGroupIdAndMajorCode(10, "080901", null)).thenReturn(0);
        lenient().when(constraintDictMapper.selectActiveNames()).thenReturn(List.of("色盲不可报考"));

        assertThatThrownBy(() -> admissionMajorScoreService.add(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不存在的限制")
                .extracting("code")
                .isEqualTo(400);
        verify(admissionMajorScoreMapper, never()).insert(any(AdmissionMajorScore.class));
    }

    @Test
    void addStoresTrimmedConstraintNameWhenItExistsInActiveDict() {
        AdmissionMajorScoreAddDTO dto = addDTO();
        dto.setConstraints(List.of(" 色盲不可报考 "));
        when(admissionGroupMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        when(admissionMajorScoreMapper.countByGroupIdAndMajorCode(10, "080901", null)).thenReturn(0);
        lenient().when(constraintDictMapper.selectActiveNames()).thenReturn(List.of("色盲不可报考"));

        admissionMajorScoreService.add(dto);

        verify(admissionMajorScoreMapper).insert(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getConstraints()).containsExactly("色盲不可报考");
    }

    @Test
    void updateRejectsConstraintNameNotInActiveDict() {
        AdmissionMajorScore existing = AdmissionMajorScore.builder()
                .id(5)
                .groupId(10)
                .majorCode("080901")
                .majorName("计算机科学与技术")
                .build();
        AdmissionMajorScoreAddDTO dto = addDTO();
        dto.setConstraints(List.of("不存在的限制"));
        when(admissionMajorScoreMapper.selectByIdCustom(5)).thenReturn(existing);
        when(admissionGroupMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        when(admissionMajorScoreMapper.countByGroupIdAndMajorCode(10, "080901", 5)).thenReturn(0);
        lenient().when(constraintDictMapper.selectActiveNames()).thenReturn(List.of("色盲不可报考"));

        assertThatThrownBy(() -> admissionMajorScoreService.update(5, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不存在的限制")
                .extracting("code")
                .isEqualTo(400);
        verify(admissionMajorScoreMapper, never()).updateByIdCustom(any(AdmissionMajorScore.class));
    }

    private AdmissionMajorScoreAddDTO addDTO() {
        AdmissionMajorScoreAddDTO dto = new AdmissionMajorScoreAddDTO();
        dto.setGroupId(10);
        dto.setYear(2025);
        dto.setMajorCode("080901");
        dto.setMajorName("计算机科学与技术");
        return dto;
    }
}
