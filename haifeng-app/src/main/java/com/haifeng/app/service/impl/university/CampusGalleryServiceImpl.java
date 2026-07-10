package com.haifeng.app.service.impl.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.university.CampusGalleryQueryDTO;
import com.haifeng.app.service.university.CampusGalleryService;
import com.haifeng.app.vo.university.CampusGalleryListVO;
import com.haifeng.common.entity.university.CampusGallery;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.CampusGalleryMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampusGalleryServiceImpl implements CampusGalleryService {

    private static final short STATUS_PUBLISHED = 1;

    private final CampusGalleryMapper galleryMapper;
    private final UniversityMapper universityMapper;

    @Override
    public IPage<CampusGalleryListVO> page(Long universityId, CampusGalleryQueryDTO dto) {
        University univ = universityMapper.selectById(universityId);
        if (univ == null || univ.getStatus() == null || univ.getStatus() != STATUS_PUBLISHED) {
            log.debug("院校不存在或已下架, universityId={}", universityId);
            throw new BusinessException(ResultCode.NOT_FOUND, "院校不存在");
        }
        Page<CampusGallery> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<CampusGallery> wrapper = new LambdaQueryWrapper<CampusGallery>()
                .eq(CampusGallery::getUniversityId, universityId)
                .eq(CampusGallery::getStatus, STATUS_PUBLISHED)
                .eq(StringUtils.hasText(dto.getImageType()),
                        CampusGallery::getImageType, dto.getImageType())
                .orderByAsc(CampusGallery::getSortOrder)
                .orderByDesc(CampusGallery::getId);

        IPage<CampusGallery> entityPage = galleryMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    private CampusGalleryListVO toListVO(CampusGallery e) {
        return CampusGalleryListVO.builder()
                .imageType(e.getImageType())
                .imageUrl(e.getImageUrl())
                .build();
    }
}
