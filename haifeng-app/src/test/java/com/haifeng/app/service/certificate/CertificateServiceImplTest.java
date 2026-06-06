package com.haifeng.app.service.certificate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.certificate.CertificateListQueryDTO;
import com.haifeng.app.service.impl.certificate.CertificateServiceImpl;
import com.haifeng.app.vo.certificate.CertificateDetailVO;
import com.haifeng.app.vo.certificate.CertificateListVO;
import com.haifeng.common.entity.certificate.Certificate;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.certificate.CertificateMapper;
import com.haifeng.common.response.ResultCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificateServiceImplTest {

    @Mock
    private CertificateMapper certificateMapper;

    @InjectMocks
    private CertificateServiceImpl service;

    @Test
    void page_WithCategory_ShouldFilter() {
        CertificateListQueryDTO dto = new CertificateListQueryDTO();
        dto.setPage(1);
        dto.setSize(10);
        dto.setCategory("计算机");

        Certificate entity = Certificate.builder()
                .id(1L).certName("软件设计师").category("计算机")
                .certLevel("中级").isDeleted(false)
                .build();
        Page<Certificate> mybatisPage = new Page<>(1, 10);
        mybatisPage.setRecords(List.of(entity));
        mybatisPage.setTotal(1);
        when(certificateMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mybatisPage);

        IPage<CertificateListVO> result = service.page(dto);

        assertEquals(1, result.getTotal());
        assertEquals("软件设计师", result.getRecords().get(0).getCertName());
        assertEquals("计算机", result.getRecords().get(0).getCategory());

        ArgumentCaptor<LambdaQueryWrapper<Certificate>> captor =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(certificateMapper).selectPage(any(Page.class), captor.capture());
        assertNotNull(captor.getValue());
    }

    @Test
    void page_WithoutCategory_ShouldReturnAll() {
        CertificateListQueryDTO dto = new CertificateListQueryDTO();
        dto.setPage(1);
        dto.setSize(10);

        Page<Certificate> empty = new Page<>(1, 10);
        when(certificateMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(empty);

        IPage<CertificateListVO> result = service.page(dto);
        assertNotNull(result);
        assertEquals(0, result.getTotal());
    }

    @Test
    void detail_NotFound_ShouldThrow() {
        when(certificateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.detail(999L));
        assertEquals(ResultCode.NOT_FOUND.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("证书"));
    }

    @Test
    void detail_Found_ShouldReturnFullVO() {
        Certificate entity = Certificate.builder()
                .id(1L).certName("软件设计师").category("计算机").certLevel("中级")
                .applicableMajor("计算机科学与技术")
                .registrationTime("上半年3月").examTime("上半年5月")
                .examFee(100).certIntro("软件行业证书")
                .examRequirements(List.of("本科及以上", "相关工作经验"))
                .examArrangement("全国统考")
                .officialWebsite("https://example.com")
                .isDeleted(false)
                .build();
        when(certificateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(entity);

        CertificateDetailVO vo = service.detail(1L);

        assertEquals("软件设计师", vo.getCertName());
        assertEquals(2, vo.getExamRequirements().size());
        assertEquals("全国统考", vo.getExamArrangement());
        assertEquals("https://example.com", vo.getOfficialWebsite());
    }
}
