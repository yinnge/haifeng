package com.haifeng.admin.service.impl.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.university.CampusGalleryAddDTO;
import com.haifeng.admin.dto.university.CampusGalleryQueryDTO;
import com.haifeng.admin.dto.university.CampusGalleryUpdateDTO;
import com.haifeng.admin.service.university.CampusGalleryService;
import com.haifeng.admin.vo.university.CampusGalleryDetailVO;
import com.haifeng.admin.vo.university.CampusGalleryListVO;
import com.haifeng.common.entity.university.CampusGallery;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.CampusGalleryMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.response.ResultCode;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public CampusGalleryDetailVO detail(Long id) {
        CampusGallery gallery = campusGalleryMapper.selectById(id);
        if (gallery == null || gallery.getStatus() == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "校园图册不存在");
        }

        CampusGalleryDetailVO vo = new CampusGalleryDetailVO();
        vo.setId(gallery.getId());
        vo.setUniversityId(gallery.getUniversityId());
        vo.setUniversityName(gallery.getUniversityName());
        vo.setImageType(gallery.getImageType());
        vo.setImageUrl(gallery.getImageUrl());
        vo.setSortOrder(gallery.getSortOrder());
        vo.setStatus(gallery.getStatus() != null ? gallery.getStatus().intValue() : null);
        vo.setCreatedAt(gallery.getCreatedAt() != null ? gallery.getCreatedAt().toLocalDateTime() : null);
        vo.setUpdatedAt(gallery.getUpdatedAt() != null ? gallery.getUpdatedAt().toLocalDateTime() : null);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(CampusGalleryAddDTO dto) {
        // 校验院校是否存在
        University university = universityMapper.selectById(dto.getUniversityId());
        if (university == null || university.getStatus() == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "关联院校不存在");
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
            throw new BusinessException(ResultCode.NOT_FOUND, "校园图册不存在");
        }

        gallery.setImageType(dto.getImageType());
        gallery.setImageUrl(dto.getImageUrl());
        if (dto.getSortOrder() != null) {
            gallery.setSortOrder(dto.getSortOrder());
        }
        if (dto.getStatus() != null) {
            gallery.setStatus(dto.getStatus());
        }
        gallery.setUpdatedAt(OffsetDateTime.now());

        int affected = campusGalleryMapper.updateById(gallery);
        if (affected == 0) {
            throw new BusinessException(400, "数据已被其他人修改，请刷新后重试");
        }

        log.info("修改校园图册成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Short status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "状态值无效，只能为0或1");
        }

        CampusGallery gallery = campusGalleryMapper.selectById(id);
        if (gallery == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "校园图册不存在");
        }

        gallery.setStatus(status);
        gallery.setUpdatedAt(OffsetDateTime.now());
        int affected = campusGalleryMapper.updateById(gallery);
        if (affected == 0) {
            throw new BusinessException(400, "数据已被其他人修改，请刷新后重试");
        }

        log.info("修改校园图册状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        CampusGallery gallery = campusGalleryMapper.selectById(id);
        if (gallery == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "校园图册不存在");
        }

        // 软删除：status = 0
        gallery.setStatus((short) 0);
        gallery.setUpdatedAt(OffsetDateTime.now());
        int affected = campusGalleryMapper.updateById(gallery);
        if (affected == 0) {
            throw new BusinessException(400, "数据已被其他人修改，请刷新后重试");
        }

        log.info("软删除校园图册成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDelete(Long id) {
        CampusGallery gallery = campusGalleryMapper.selectById(id);
        if (gallery == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "校园图册不存在");
        }

        // 硬删除：物理删除
        campusGalleryMapper.deleteById(id);

        log.info("硬删除校园图册成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;

        OffsetDateTime now = OffsetDateTime.now();
        LambdaUpdateWrapper<CampusGallery> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(CampusGallery::getId, ids)
               .ne(CampusGallery::getStatus, (short) 0)
               .set(CampusGallery::getStatus, (short) 0)
               .set(CampusGallery::getUpdatedAt, now);
        int affected = campusGalleryMapper.update(null, wrapper);

        log.info("批量软删除校园图册成功，数量={}", affected);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchHardDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;

        int affected = campusGalleryMapper.deleteBatchIds(ids);

        log.info("批量硬删除校园图册成功，数量={}", affected);
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

        // 第一轮：纯校验，收集所有错误和待插入数据
        List<String> errors = new ArrayList<>();
        List<CampusGallery> validList = new ArrayList<>();
        Map<String, University> universityCache = new HashMap<>();
        OffsetDateTime now = OffsetDateTime.now();
        int maxErrors = 50;

        for (int i = 0; i < dataList.size(); i++) {
            int rowNum = i + 2;
            CampusGalleryExcelDTO dto = dataList.get(i);

            if (!StringUtils.hasText(dto.getUniversityName())) {
                errors.add("第" + rowNum + "行: 院校名称不能为空");
                continue;
            }
            if (!StringUtils.hasText(dto.getImageUrl())) {
                errors.add("第" + rowNum + "行: 图片URL不能为空");
                continue;
            }

            University university = universityCache.get(dto.getUniversityName());
            if (university == null) {
                LambdaQueryWrapper<University> univWrapper = new LambdaQueryWrapper<>();
                univWrapper.eq(University::getName, dto.getUniversityName())
                           .ne(University::getStatus, (short) 0);
                university = universityMapper.selectOne(univWrapper);
                if (university != null) {
                    universityCache.put(dto.getUniversityName(), university);
                }
            }

            if (university == null) {
                errors.add("第" + rowNum + "行: 院校[" + dto.getUniversityName() + "]不存在");
                continue;
            }

            CampusGallery gallery = CampusGallery.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .universityId(university.getId())
                    .universityName(university.getName())
                    .imageType(dto.getImageType())
                    .imageUrl(dto.getImageUrl())
                    .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                    .status((short) 1)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            validList.add(gallery);
        }

        // 校验失败 = 全部不插入
        if (!errors.isEmpty()) {
            String errorSummary;
            if (errors.size() <= maxErrors) {
                errorSummary = String.join("; ", errors);
            } else {
                errorSummary = String.join("; ", errors.subList(0, maxErrors))
                        + String.format("...等共%d条错误", errors.size());
            }
            throw new BusinessException(400, "导入校验失败，共" + errors.size() + "条错误：" + errorSummary);
        }

        // 第二轮：全部校验通过，批量插入
        for (CampusGallery gallery : validList) {
            campusGalleryMapper.insert(gallery);
        }

        log.info("导入校园图册数据成功: 共{}条", validList.size());
    }
}
