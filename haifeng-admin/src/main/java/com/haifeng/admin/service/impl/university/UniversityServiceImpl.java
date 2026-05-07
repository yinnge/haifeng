package com.haifeng.admin.service.impl.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.university.*;
import com.haifeng.admin.service.university.UniversityService;
import com.haifeng.admin.vo.university.RankingsVO;
import com.haifeng.admin.vo.university.UniversityDetailVO;
import com.haifeng.admin.vo.university.UniversityListVO;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.entity.university.UniversityDetail;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.UniversityDetailMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * 院校管理Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UniversityServiceImpl implements UniversityService {

    private final UniversityMapper universityMapper;
    private final UniversityDetailMapper universityDetailMapper;

    @Override
    public IPage<UniversityListVO> page(UniversityQueryDTO dto) {
        Page<University> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<University> wrapper = new LambdaQueryWrapper<>();
        // 只查询未删除的（status != 0）
        wrapper.ne(University::getStatus, (short) 0);

        // 名称模糊查询
        if (StringUtils.hasText(dto.getName())) {
            wrapper.like(University::getName, dto.getName());
        }
        // 省份精确筛选
        if (StringUtils.hasText(dto.getProvinceName())) {
            wrapper.eq(University::getProvinceName, dto.getProvinceName());
        }
        // 类别精确筛选
        if (StringUtils.hasText(dto.getCategory())) {
            wrapper.eq(University::getCategory, dto.getCategory());
        }
        // 状态筛选
        if (dto.getStatus() != null) {
            wrapper.eq(University::getStatus, dto.getStatus());
        }

        // 按sortOrder升序 + createdAt降序排列
        wrapper.orderByAsc(University::getSortOrder)
               .orderByDesc(University::getCreatedAt);

        IPage<University> universityPage = universityMapper.selectPage(page, wrapper);

        return universityPage.convert(university -> {
            UniversityListVO vo = new UniversityListVO();
            BeanUtils.copyProperties(university, vo);
            // 转换status类型
            vo.setStatus(university.getStatus() != null ? university.getStatus().intValue() : null);
            return vo;
        });
    }

    @Override
    public UniversityDetailVO detail(Long id) {
        University university = universityMapper.selectById(id);
        if (university == null || university.getStatus() == 0) {
            throw new BusinessException(404, "院校不存在");
        }

        UniversityDetailVO vo = new UniversityDetailVO();
        BeanUtils.copyProperties(university, vo);
        // 转换status类型
        vo.setStatus(university.getStatus() != null ? university.getStatus().intValue() : null);

        // 查询详情表
        LambdaQueryWrapper<UniversityDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UniversityDetail::getUniversityId, id);
        UniversityDetail detail = universityDetailMapper.selectOne(wrapper);

        if (detail != null) {
            vo.setDetailId(detail.getId());
            vo.setAddress(detail.getAddress());
            vo.setAdmissionPhone(detail.getAdmissionPhone());
            vo.setWebsite(detail.getWebsite());
            vo.setHistoryGroupScore(detail.getHistoryGroupScore());
            vo.setScienceGroupScore(detail.getScienceGroupScore());
            vo.setCarouselImages(detail.getCarouselImages());
            vo.setDetailIntroduction(detail.getIntroduction());
            vo.setAbroadRate(detail.getAbroadRate());
            vo.setGenderRatio(detail.getGenderRatio());

            // 转换rankings Map为RankingsVO
            if (detail.getRankings() != null && !detail.getRankings().isEmpty()) {
                RankingsVO rankingsVO = new RankingsVO();
                Map<String, Integer> rankings = detail.getRankings();
                rankingsVO.setRuanke(rankings.get("ruanke"));
                rankingsVO.setXiaoyouhui(rankings.get("xiaoyouhui"));
                rankingsVO.setWushulian(rankings.get("wushulian"));
                rankingsVO.setQs(rankings.get("qs"));
                rankingsVO.setUsnews(rankings.get("usnews"));
                vo.setRankings(rankingsVO);
            }
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(UniversityAddDTO dto) {
        // 检查名称是否重复（只检查未删除的）
        LambdaQueryWrapper<University> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(University::getName, dto.getName())
               .ne(University::getStatus, (short) 0);
        if (universityMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "院校名称已存在");
        }

        OffsetDateTime now = OffsetDateTime.now();
        Long id = SnowflakeIdGenerator.nextId();

        University university = University.builder()
                .id(id)
                .name(dto.getName())
                .nameEn(dto.getNameEn())
                .provinceName(dto.getProvinceName())
                .cityName(dto.getCityName())
                .region(dto.getRegion())
                .category(dto.getCategory())
                .majorCount(dto.getMajorCount() != null ? dto.getMajorCount() : 0)
                .educationLevel(dto.getEducationLevel())
                .nature(dto.getNature())
                .recommendationRate(dto.getRecommendationRate())
                .recommendationYear(dto.getRecommendationYear())
                .hasDoctorate(dto.getHasDoctorate() != null ? dto.getHasDoctorate() : false)
                .hasMaster(dto.getHasMaster() != null ? dto.getHasMaster() : false)
                .department(dto.getDepartment())
                .tags(dto.getTags())
                .famousUnion(dto.getFamousUnion())
                .imageUrl(dto.getImageUrl())
                .introduction(dto.getIntroduction())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .status((short) 1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        universityMapper.insert(university);

        log.info("新增院校成功: id={}, name={}", id, dto.getName());
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, UniversityUpdateDTO dto) {
        University university = universityMapper.selectById(id);
        if (university == null || university.getStatus() == 0) {
            throw new BusinessException(404, "院校不存在");
        }

        // 如果名称变更，检查是否与其他院校重复
        if (!university.getName().equals(dto.getName())) {
            LambdaQueryWrapper<University> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(University::getName, dto.getName())
                   .ne(University::getStatus, (short) 0)
                   .ne(University::getId, id);
            if (universityMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(400, "院校名称已存在");
            }
        }

        university.setName(dto.getName());
        university.setNameEn(dto.getNameEn());
        university.setProvinceName(dto.getProvinceName());
        university.setCityName(dto.getCityName());
        university.setRegion(dto.getRegion());
        university.setCategory(dto.getCategory());
        university.setMajorCount(dto.getMajorCount());
        university.setEducationLevel(dto.getEducationLevel());
        university.setNature(dto.getNature());
        university.setRecommendationRate(dto.getRecommendationRate());
        university.setRecommendationYear(dto.getRecommendationYear());
        university.setHasDoctorate(dto.getHasDoctorate());
        university.setHasMaster(dto.getHasMaster());
        university.setDepartment(dto.getDepartment());
        university.setTags(dto.getTags());
        university.setFamousUnion(dto.getFamousUnion());
        university.setImageUrl(dto.getImageUrl());
        university.setIntroduction(dto.getIntroduction());
        university.setSortOrder(dto.getSortOrder());
        if (dto.getStatus() != null) {
            university.setStatus(dto.getStatus().shortValue());
        }
        university.setUpdatedAt(OffsetDateTime.now());

        universityMapper.updateById(university);

        log.info("修改院校成功: id={}, name={}", id, dto.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDetail(Long id, UniversityDetailUpdateDTO dto) {
        University university = universityMapper.selectById(id);
        if (university == null || university.getStatus() == 0) {
            throw new BusinessException(404, "院校不存在");
        }

        LambdaQueryWrapper<UniversityDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UniversityDetail::getUniversityId, id);
        UniversityDetail detail = universityDetailMapper.selectOne(wrapper);

        OffsetDateTime now = OffsetDateTime.now();

        if (detail == null) {
            // 新建详情记录
            Long detailId = SnowflakeIdGenerator.nextId();

            detail = UniversityDetail.builder()
                    .id(detailId)
                    .universityId(id)
                    .address(dto.getAddress())
                    .admissionPhone(dto.getAdmissionPhone())
                    .website(dto.getWebsite())
                    .historyGroupScore(dto.getHistoryGroupScore())
                    .scienceGroupScore(dto.getScienceGroupScore())
                    .carouselImages(dto.getCarouselImages())
                    .introduction(dto.getIntroduction())
                    .rankings(dto.getRankings())
                    .abroadRate(dto.getAbroadRate())
                    .genderRatio(dto.getGenderRatio())
                    .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                    .status(dto.getStatus() != null ? dto.getStatus().shortValue() : (short) 1)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            universityDetailMapper.insert(detail);
            log.info("新增院校详情成功: universityId={}, detailId={}", id, detailId);
        } else {
            // 更新详情记录
            detail.setAddress(dto.getAddress());
            detail.setAdmissionPhone(dto.getAdmissionPhone());
            detail.setWebsite(dto.getWebsite());
            detail.setHistoryGroupScore(dto.getHistoryGroupScore());
            detail.setScienceGroupScore(dto.getScienceGroupScore());
            detail.setCarouselImages(dto.getCarouselImages());
            detail.setIntroduction(dto.getIntroduction());
            detail.setRankings(dto.getRankings());
            detail.setAbroadRate(dto.getAbroadRate());
            detail.setGenderRatio(dto.getGenderRatio());
            if (dto.getSortOrder() != null) {
                detail.setSortOrder(dto.getSortOrder());
            }
            if (dto.getStatus() != null) {
                detail.setStatus(dto.getStatus().shortValue());
            }
            detail.setUpdatedAt(now);

            universityDetailMapper.updateById(detail);
            log.info("修改院校详情成功: universityId={}, detailId={}", id, detail.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        University university = universityMapper.selectById(id);
        if (university == null || university.getStatus() == 0) {
            throw new BusinessException(404, "院校不存在");
        }

        // 软删除：status = 0
        university.setStatus((short) 0);
        university.setUpdatedAt(OffsetDateTime.now());

        universityMapper.updateById(university);

        log.info("删除院校成功: id={}", id);
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
            University university = universityMapper.selectById(id);
            if (university != null && university.getStatus() != 0) {
                university.setStatus((short) 0);
                university.setUpdatedAt(now);
                universityMapper.updateById(university);
                successCount++;
            }
        }

        log.info("批量删除院校成功: 请求数量={}, 实际删除数量={}", ids.size(), successCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importUniversities(MultipartFile file) {
        // TODO: 待Task 11创建Excel DTO类后实现
        // 需要依赖: UniversityExcelDTO
        throw new BusinessException(501, "Excel导入功能待实现，请先创建Excel DTO类");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importUniversityDetails(MultipartFile file) {
        // TODO: 待Task 11创建Excel DTO类后实现
        // 需要依赖: UniversityDetailExcelDTO
        throw new BusinessException(501, "Excel导入功能待实现，请先创建Excel DTO类");
    }
}
