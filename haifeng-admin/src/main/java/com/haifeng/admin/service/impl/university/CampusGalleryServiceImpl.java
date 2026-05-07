package com.haifeng.admin.service.impl.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.university.CampusGalleryAddDTO;
import com.haifeng.admin.dto.university.CampusGalleryQueryDTO;
import com.haifeng.admin.dto.university.CampusGalleryUpdateDTO;
import com.haifeng.admin.service.university.CampusGalleryService;
import com.haifeng.admin.vo.university.CampusGalleryListVO;
import com.haifeng.common.entity.university.CampusGallery;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.CampusGalleryMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import com.alibaba.excel.EasyExcel;
import com.haifeng.admin.excel.university.CampusGalleryExcelDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 校园图册Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CampusGalleryServiceImpl implements CampusGalleryService {

    private final CampusGalleryMapper campusGalleryMapper;
    private final UniversityMapper universityMapper;

    @Override
    public IPage<CampusGalleryListVO> page(CampusGalleryQueryDTO dto) {
        Page<CampusGallery> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<CampusGallery> wrapper = new LambdaQueryWrapper<>();
        // 只查询未删除的（status != 0）
        wrapper.ne(CampusGallery::getStatus, (short) 0);

        // 院校名称模糊查询
        if (StringUtils.hasText(dto.getUniversityName())) {
            wrapper.like(CampusGallery::getUniversityName, dto.getUniversityName());
        }
        // 图片类型精确筛选
        if (StringUtils.hasText(dto.getImageType())) {
            wrapper.eq(CampusGallery::getImageType, dto.getImageType());
        }
        // 状态筛选
        if (dto.getStatus() != null) {
            wrapper.eq(CampusGallery::getStatus, dto.getStatus());
        }

        // 按sortOrder升序 + createdAt降序排列
        wrapper.orderByAsc(CampusGallery::getSortOrder)
               .orderByDesc(CampusGallery::getCreatedAt);

        IPage<CampusGallery> galleryPage = campusGalleryMapper.selectPage(page, wrapper);

        return galleryPage.convert(gallery -> {
            CampusGalleryListVO vo = new CampusGalleryListVO();
            vo.setId(gallery.getId());
            vo.setUniversityId(gallery.getUniversityId());
            vo.setUniversityName(gallery.getUniversityName());
            vo.setImageType(gallery.getImageType());
            vo.setImageUrl(gallery.getImageUrl());
            vo.setSortOrder(gallery.getSortOrder());
            vo.setStatus(gallery.getStatus() != null ? gallery.getStatus().intValue() : null);
            vo.setCreatedAt(gallery.getCreatedAt() != null ? gallery.getCreatedAt().toLocalDateTime() : null);
            return vo;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(CampusGalleryAddDTO dto) {
        // 校验院校是否存在
        University university = universityMapper.selectById(dto.getUniversityId());
        if (university == null || university.getStatus() == 0) {
            throw new BusinessException(404, "关联院校不存在");
        }

        OffsetDateTime now = OffsetDateTime.now();
        Long id = SnowflakeIdGenerator.nextId();

        CampusGallery gallery = CampusGallery.builder()
                .id(id)
                .universityId(dto.getUniversityId())
                .universityName(university.getName())
                .imageType(dto.getImageType())
                .imageUrl(dto.getImageUrl())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .status((short) 1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        campusGalleryMapper.insert(gallery);

        log.info("新增校园图册成功: id={}, universityId={}, imageType={}", id, dto.getUniversityId(), dto.getImageType());
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, CampusGalleryUpdateDTO dto) {
        CampusGallery gallery = campusGalleryMapper.selectById(id);
        if (gallery == null || gallery.getStatus() == 0) {
            throw new BusinessException(404, "校园图册不存在");
        }

        gallery.setImageType(dto.getImageType());
        gallery.setImageUrl(dto.getImageUrl());
        if (dto.getSortOrder() != null) {
            gallery.setSortOrder(dto.getSortOrder());
        }
        if (dto.getStatus() != null) {
            gallery.setStatus(dto.getStatus().shortValue());
        }
        gallery.setUpdatedAt(OffsetDateTime.now());

        campusGalleryMapper.updateById(gallery);

        log.info("修改校园图册成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Short status) {
        CampusGallery gallery = campusGalleryMapper.selectById(id);
        if (gallery == null) {
            throw new BusinessException(404, "校园图册不存在");
        }

        gallery.setStatus(status);
        gallery.setUpdatedAt(OffsetDateTime.now());
        campusGalleryMapper.updateById(gallery);

        log.info("修改校园图册状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        CampusGallery gallery = campusGalleryMapper.selectById(id);
        if (gallery == null) {
            throw new BusinessException(404, "校园图册不存在");
        }

        // 软删除：status = 0
        gallery.setStatus((short) 0);
        gallery.setUpdatedAt(OffsetDateTime.now());
        campusGalleryMapper.updateById(gallery);

        log.info("软删除校园图册成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDelete(Long id) {
        CampusGallery gallery = campusGalleryMapper.selectById(id);
        if (gallery == null) {
            throw new BusinessException(404, "校园图册不存在");
        }

        // 硬删除：物理删除
        campusGalleryMapper.deleteById(id);

        log.info("硬删除校园图册成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "ID列表不能为空");
        }

        OffsetDateTime now = OffsetDateTime.now();
        int successCount = 0;

        for (Long id : ids) {
            CampusGallery gallery = campusGalleryMapper.selectById(id);
            if (gallery != null && gallery.getStatus() != 0) {
                gallery.setStatus((short) 0);
                gallery.setUpdatedAt(now);
                campusGalleryMapper.updateById(gallery);
                successCount++;
            }
        }

        log.info("批量软删除校园图册成功: 请求数量={}, 实际删除数量={}", ids.size(), successCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchHardDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "ID列表不能为空");
        }

        int successCount = 0;

        for (Long id : ids) {
            CampusGallery gallery = campusGalleryMapper.selectById(id);
            if (gallery != null) {
                campusGalleryMapper.deleteById(id);
                successCount++;
            }
        }

        log.info("批量硬删除校园图册成功: 请求数量={}, 实际删除数量={}", ids.size(), successCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importGallery(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }

        List<CampusGalleryExcelDTO> dataList;
        try {
            dataList = EasyExcel.read(file.getInputStream())
                    .head(CampusGalleryExcelDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "读取Excel文件失败: " + e.getMessage());
        }

        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }

        List<String> errors = new ArrayList<>();
        OffsetDateTime now = OffsetDateTime.now();
        int successCount = 0;

        for (int i = 0; i < dataList.size(); i++) {
            int rowNum = i + 2;
            CampusGalleryExcelDTO dto = dataList.get(i);

            // 校验必填字段
            if (!StringUtils.hasText(dto.getUniversityName())) {
                errors.add("第" + rowNum + "行: 院校名称不能为空");
                continue;
            }
            if (!StringUtils.hasText(dto.getImageUrl())) {
                errors.add("第" + rowNum + "行: 图片URL不能为空");
                continue;
            }

            // 根据院校名称查找院校
            LambdaQueryWrapper<University> univWrapper = new LambdaQueryWrapper<>();
            univWrapper.eq(University::getName, dto.getUniversityName())
                       .ne(University::getStatus, (short) 0);
            University university = universityMapper.selectOne(univWrapper);

            if (university == null) {
                errors.add("第" + rowNum + "行: 院校[" + dto.getUniversityName() + "]不存在");
                continue;
            }

            Long id = SnowflakeIdGenerator.nextId();
            CampusGallery gallery = CampusGallery.builder()
                    .id(id)
                    .universityId(university.getId())
                    .universityName(university.getName())
                    .imageType(dto.getImageType())
                    .imageUrl(dto.getImageUrl())
                    .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                    .status((short) 1)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            campusGalleryMapper.insert(gallery);
            successCount++;
        }

        if (!errors.isEmpty()) {
            String errorMsg = String.format("导入完成，成功%d条，失败%d条。错误信息：%s",
                    successCount, errors.size(), String.join("; ", errors));
            throw new BusinessException(400, errorMsg);
        }

        log.info("导入校园图册数据成功: 共{}条", successCount);
    }
}
