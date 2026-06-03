package com.haifeng.app.service.university;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.university.CampusGalleryQueryDTO;
import com.haifeng.app.service.impl.university.CampusGalleryServiceImpl;
import com.haifeng.app.vo.university.CampusGalleryListVO;
import com.haifeng.common.entity.university.CampusGallery;
import com.haifeng.common.mapper.university.CampusGalleryMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampusGalleryServiceImplTest {

    @Mock private CampusGalleryMapper galleryMapper;

    @InjectMocks private CampusGalleryServiceImpl service;

    @Test
    void page_returnsConvertedVOs() {
        CampusGallery e = CampusGallery.builder()
                .id(1L).universityId(100L).universityName("清华")
                .imageType("校门").imageUrl("https://x/y.jpg")
                .status((short) 1).build();
        Page<CampusGallery> p = new Page<>(1, 10);
        p.setRecords(List.of(e));
        p.setTotal(1);
        when(galleryMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(p);

        IPage<CampusGalleryListVO> result = service.page(100L, new CampusGalleryQueryDTO());

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getImageType()).isEqualTo("校门");
        assertThat(result.getRecords().get(0).getImageUrl()).isEqualTo("https://x/y.jpg");
    }

    @Test
    void page_passesPageAndSizeFromDto() {
        CampusGalleryQueryDTO dto = new CampusGalleryQueryDTO();
        dto.setPage(2);
        dto.setSize(30);

        Page<CampusGallery> p = new Page<>(2, 30);
        p.setRecords(List.of());
        p.setTotal(0);

        ArgumentCaptor<Page> cap = ArgumentCaptor.forClass(Page.class);
        when(galleryMapper.selectPage(cap.capture(), any(Wrapper.class))).thenReturn(p);

        service.page(100L, dto);

        assertThat(cap.getValue().getCurrent()).isEqualTo(2L);
        assertThat(cap.getValue().getSize()).isEqualTo(30L);
    }
}
