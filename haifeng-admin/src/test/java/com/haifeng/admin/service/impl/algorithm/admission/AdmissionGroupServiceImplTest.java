package com.haifeng.admin.service.impl.algorithm.admission;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.admin.excel.algorithm.admission.AdmissionImportDTO;
import com.haifeng.common.entity.algorithm.AdmissionGroup;
import com.haifeng.common.entity.algorithm.AdmissionMajorScore;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.AdmissionGroupMapper;
import com.haifeng.common.mapper.algorithm.AdmissionMajorScoreMapper;
import com.haifeng.common.mapper.algorithm.ConstraintDictMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdmissionGroupServiceImplTest {

    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Mock
    private AdmissionGroupMapper admissionGroupMapper;

    @Mock
    private AdmissionMajorScoreMapper admissionMajorScoreMapper;

    @Mock
    private UniversityMapper universityMapper;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private ConstraintDictMapper constraintDictMapper;

    @Mock
    private TransactionStatus transactionStatus;

    @InjectMocks
    private AdmissionGroupServiceImpl admissionGroupService;

    @Test
    void importRejectsConstraintNameNotInActiveDictBeforeInserting() {
        AdmissionImportDTO row = validRow();
        row.setConstraintsStr("不存在的限制");
        MockMultipartFile file = buildExcel(row);

        when(universityMapper.selectIdAndCityByName("测试大学"))
                .thenReturn(University.builder().id(1L).name("测试大学").cityName("广州").build());
        when(admissionGroupMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        lenient().when(constraintDictMapper.selectActiveNames()).thenReturn(List.of("色盲不可报考"));

        assertThatThrownBy(() -> admissionGroupService.importData(file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("第2行")
                .hasMessageContaining("不存在的限制")
                .extracting("code")
                .isEqualTo(400);

        verify(admissionGroupMapper, never()).insert(any(AdmissionGroup.class));
        verify(admissionMajorScoreMapper, never()).insert(any(AdmissionMajorScore.class));
    }

    @Test
    void importAcceptsConstraintNameInActiveDict() {
        AdmissionImportDTO row = validRow();
        row.setConstraintsStr("色盲不可报考");
        MockMultipartFile file = buildExcel(row);

        when(universityMapper.selectIdAndCityByName("测试大学"))
                .thenReturn(University.builder().id(1L).name("测试大学").cityName("广州").build());
        when(admissionGroupMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(constraintDictMapper.selectActiveNames()).thenReturn(List.of("色盲不可报考"));
        when(transactionManager.getTransaction(any(TransactionDefinition.class))).thenReturn(transactionStatus);
        when(admissionGroupMapper.insert(any(AdmissionGroup.class))).thenAnswer(invocation -> {
            AdmissionGroup group = invocation.getArgument(0);
            group.setId(100);
            return 1;
        });
        ArgumentCaptor<AdmissionMajorScore> majorCaptor = ArgumentCaptor.forClass(AdmissionMajorScore.class);

        admissionGroupService.importData(file);

        verify(admissionMajorScoreMapper).insert(majorCaptor.capture());
        assertThat(majorCaptor.getValue().getConstraints()).containsExactly("色盲不可报考");
    }

    private AdmissionImportDTO validRow() {
        AdmissionImportDTO row = new AdmissionImportDTO();
        row.setUniversityName("测试大学");
        row.setYear((short) 2025);
        row.setProvince("广东");
        row.setBatch("本科批");
        row.setGroupCode("G01");
        row.setMajorCode("080901");
        row.setMajorName("计算机科学与技术");
        return row;
    }

    private MockMultipartFile buildExcel(AdmissionImportDTO row) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EasyExcel.write(out, AdmissionImportDTO.class).sheet("导入").doWrite(List.of(row));
        return new MockMultipartFile("file", "group.xlsx", XLSX_CONTENT_TYPE, out.toByteArray());
    }
}
