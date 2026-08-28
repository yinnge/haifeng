package com.haifeng.admin.service.impl.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.university.*;
import com.haifeng.admin.service.university.UniversityService;
import com.haifeng.admin.vo.university.RankingsVO;
import com.haifeng.admin.vo.university.UniversityDetailVO;
import com.haifeng.admin.vo.university.UniversityListVO;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.entity.university.UniversityDetail;
import com.haifeng.common.entity.university.UniversityGuide;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.UniversityDetailMapper;
import com.haifeng.common.mapper.university.UniversityGuideMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.SnowflakeIdGenerator;
import com.alibaba.excel.EasyExcel;
import com.haifeng.admin.excel.university.UniversityExcelDTO;
import com.haifeng.admin.excel.university.UniversityDetailExcelDTO;
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
 * 院校管理Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UniversityServiceImpl implements UniversityService {

    private final UniversityMapper universityMapper;

    private static final int MAX_IMPORT_ROWS = 1000;

    /** 导入错误信息最多展示条数，超出部分只提示总数，避免 msg 过长 */
    private static final int MAX_ERROR_SHOWN = 50;
    private final UniversityDetailMapper universityDetailMapper;
    private final UniversityGuideMapper universityGuideMapper;

    @Override
    public IPage<UniversityListVO> page(UniversityQueryDTO dto) {
        Page<University> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<University> wrapper = new LambdaQueryWrapper<>();

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
        // 状态筛选（管理员可查看所有状态）
        if (dto.getStatus() != null) {
            wrapper.eq(University::getStatus, dto.getStatus());
        }

        // 按sortOrder升序 + createdAt降序排列
        wrapper.orderByAsc(University::getSortOrder)
               .orderByDesc(University::getCreatedAt);

        IPage<University> universityPage = universityMapper.selectPage(page, wrapper);

        return universityPage.convert(university -> UniversityListVO.builder()
                .id(university.getId())
                .name(university.getName())
                .provinceName(university.getProvinceName())
                .cityName(university.getCityName())
                .region(university.getRegion())
                .category(university.getCategory())
                .majorCount(university.getMajorCount())
                .educationLevel(university.getEducationLevel())
                .nature(university.getNature())
                .status(university.getStatus() != null ? university.getStatus().intValue() : null)
                .createdAt(university.getCreatedAt() != null ? university.getCreatedAt().toLocalDateTime() : null)
                .build());
    }

    @Override
    public UniversityDetailVO detail(Long id) {
        University university = universityMapper.selectById(id);
        if (university == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "院校不存在");
        }

        UniversityDetailVO vo = UniversityDetailVO.builder()
                .id(university.getId())
                .name(university.getName())
                .nameEn(university.getNameEn())
                .provinceName(university.getProvinceName())
                .cityName(university.getCityName())
                .region(university.getRegion())
                .category(university.getCategory())
                .majorCount(university.getMajorCount())
                .educationLevel(university.getEducationLevel())
                .nature(university.getNature())
                .recommendationRate(university.getRecommendationRate())
                .recommendationYear(university.getRecommendationYear())
                .hasDoctorate(university.getHasDoctorate())
                .hasMaster(university.getHasMaster())
                .department(university.getDepartment())
                .tags(university.getTags())
                .famousUnion(university.getFamousUnion())
                .imageUrl(university.getImageUrl())
                .introduction(university.getIntroduction())
                .sortOrder(university.getSortOrder())
                .status(university.getStatus() != null ? university.getStatus().intValue() : null)
                .createdAt(university.getCreatedAt() != null ? university.getCreatedAt().toLocalDateTime() : null)
                .updatedAt(university.getUpdatedAt() != null ? university.getUpdatedAt().toLocalDateTime() : null)
                .build();

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
                Map<String, Integer> rankings = detail.getRankings();
                RankingsVO rankingsVO = RankingsVO.builder()
                        .ruanke(rankings.get("ruanke"))
                        .xiaoyouhui(rankings.get("xiaoyouhui"))
                        .wushulian(rankings.get("wushulian"))
                        .qs(rankings.get("qs"))
                        .usnews(rankings.get("usnews"))
                        .build();
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
        if (university == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "院校不存在");
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
        if (university == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "院校不存在");
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
    public void updateStatus(Long id, Short status) {
        University university = universityMapper.selectById(id);
        if (university == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "院校不存在");
        }

        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "状态值无效，仅支持0（下架）或1（展示）");
        }

        university.setStatus(status);
        university.setUpdatedAt(OffsetDateTime.now());
        universityMapper.updateById(university);

        log.info("修改院校状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        University university = universityMapper.selectById(id);
        if (university == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "院校不存在");
        }

        // 软删除：status = 0
        university.setStatus((short) 0);
        university.setUpdatedAt(OffsetDateTime.now());
        universityMapper.updateById(university);

        log.info("软删除院校成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDelete(Long id) {
        University university = universityMapper.selectById(id);
        if (university == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "院校不存在");
        }

        // 先删除关联的详情记录
        LambdaQueryWrapper<UniversityDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(UniversityDetail::getUniversityId, id);
        universityDetailMapper.delete(detailWrapper);

        // 删除关联的适应指南
        LambdaQueryWrapper<UniversityGuide> guideWrapper = new LambdaQueryWrapper<>();
        guideWrapper.eq(UniversityGuide::getUniversityId, id);
        universityGuideMapper.delete(guideWrapper);

        // 硬删除：物理删除
        universityMapper.deleteById(id);

        log.info("硬删除院校成功: id={}", id);
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

        log.info("批量软删除院校成功: 请求数量={}, 实际删除数量={}", ids.size(), successCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchHardDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "ID列表不能为空");
        }

        int successCount = 0;

        for (Long id : ids) {
            University university = universityMapper.selectById(id);
            if (university != null) {
                // 先删除关联的详情记录
                LambdaQueryWrapper<UniversityDetail> detailWrapper = new LambdaQueryWrapper<>();
                detailWrapper.eq(UniversityDetail::getUniversityId, id);
                universityDetailMapper.delete(detailWrapper);

                // 硬删除
                universityMapper.deleteById(id);
                successCount++;
            }
        }

        log.info("批量硬删除院校成功: 请求数量={}, 实际删除数量={}", ids.size(), successCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importUniversities(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }

        List<UniversityExcelDTO> dataList;
        try {
            dataList = EasyExcel.read(file.getInputStream())
                    .head(UniversityExcelDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "读取Excel文件失败: " + e.getMessage());
        }

        if (dataList != null && dataList.size() > MAX_IMPORT_ROWS) {
            throw new BusinessException(400, "单次导入不能超过" + MAX_IMPORT_ROWS + "条记录");
        }

        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }

        List<String> errors = new ArrayList<>();
        OffsetDateTime now = OffsetDateTime.now();
        int insertCount = 0;
        int updateCount = 0;

        for (int i = 0; i < dataList.size(); i++) {
            int rowNum = i + 2; // Excel行号（从2开始，1是表头）
            UniversityExcelDTO dto = dataList.get(i);

            // 校验必填字段
            if (!StringUtils.hasText(dto.getName())) {
                errors.add("第" + rowNum + "行: 院校名称不能为空");
                continue;
            }

            // 字段长度校验（对应 t_universities 列定义，避免保存时触发 varchar 超长）
            int errBefore = errors.size();
            checkLength(dto.getName(), "院校名称", 50, rowNum, errors);
            checkLength(dto.getNameEn(), "院校名称英文", 100, rowNum, errors);
            checkLength(dto.getProvinceName(), "省份", 50, rowNum, errors);
            checkLength(dto.getCityName(), "城市", 50, rowNum, errors);
            checkLength(dto.getRegion(), "所属地区", 50, rowNum, errors);
            checkLength(dto.getCategory(), "院校类别", 50, rowNum, errors);
            checkLength(dto.getEducationLevel(), "办学层次", 50, rowNum, errors);
            checkLength(dto.getNature(), "院校性质", 50, rowNum, errors);
            checkLength(dto.getDepartment(), "隶属部门", 100, rowNum, errors);
            checkLength(dto.getFamousUnion(), "知名联盟", 50, rowNum, errors);
            checkLength(dto.getImageUrl(), "院校图片URL", 500, rowNum, errors);
            if (errors.size() > errBefore) {
                continue;
            }

            // 查找是否已存在同一院校（按名称，排除已下架）
            LambdaQueryWrapper<University> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(University::getName, dto.getName())
                   .ne(University::getStatus, (short) 0);
            List<University> existingList = universityMapper.selectList(wrapper);
            University existing = existingList.isEmpty() ? null : existingList.get(0);

            try {
                if (existing != null) {
                    // 已存在：仅补齐数据库中为 NULL 的字段，已有数据一律不覆盖
                    fillUniversityGaps(existing, dto, now);
                    universityMapper.updateById(existing);
                    updateCount++;
                } else {
                    // 新增：数据库 NOT NULL 列必须齐全
                    int reqBefore = errors.size();
                    requireText(dto.getNameEn(), "院校名称英文", rowNum, errors);
                    requireText(dto.getProvinceName(), "省份", rowNum, errors);
                    requireText(dto.getCityName(), "城市", rowNum, errors);
                    requireText(dto.getRegion(), "所属地区", rowNum, errors);
                    requireText(dto.getCategory(), "院校类别", rowNum, errors);
                    if (errors.size() > reqBefore) {
                        continue;
                    }

                    University university = University.builder()
                            .id(SnowflakeIdGenerator.nextId())
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
                            .sortOrder(0)
                            .status((short) 1)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    universityMapper.insert(university);
                    insertCount++;
                }
            } catch (Exception e) {
                log.error("第{}行导入院校主表失败", rowNum, e);
                errors.add("第" + rowNum + "行: 保存失败(" + e.getMessage() + ")");
            }
        }

        if (!errors.isEmpty()) {
            String errorMsg = String.format("导入失败，共%d行数据存在错误，已全部回滚。错误信息：%s",
                    errors.size(), joinErrors(errors));
            throw new BusinessException(400, errorMsg);
        }

        log.info("导入院校主表数据成功: 新增{}条, 补齐{}条", insertCount, updateCount);
        int total = dataList.size();
        int failed = 0; // 整批回滚，无部分成功
        return ImportResultVO.builder()
                .total(total)
                .success(total - failed)
                .failed(failed)
                .updated(updateCount)
                .errors(errors)
                .build();
    }

    /**
     * 校验导入字段长度是否超过数据库列定义，超出则收集错误信息（指明行号/字段/实际长度）。
     */
    private void checkLength(String value, String fieldLabel, int max, int rowNum, List<String> errors) {
        if (value != null && value.length() > max) {
            errors.add(String.format("第%d行: %s超过%d个字符限制(实际%d个字符)",
                    rowNum, fieldLabel, max, value.length()));
        }
    }

    /**
     * 新增记录时校验数据库 NOT NULL 列是否有值。
     */
    private void requireText(String value, String fieldLabel, int rowNum, List<String> errors) {
        if (!StringUtils.hasText(value)) {
            errors.add("第" + rowNum + "行: 新增记录时" + fieldLabel + "不能为空");
        }
    }

    /**
     * 已存在的院校记录：仅补齐数据库中为 NULL 的字段，已有数据一律不覆盖。
     */
    private void fillUniversityGaps(University db, UniversityExcelDTO dto, OffsetDateTime now) {
        if (db.getNameEn() == null) {
            db.setNameEn(dto.getNameEn());
        }
        if (db.getProvinceName() == null) {
            db.setProvinceName(dto.getProvinceName());
        }
        if (db.getCityName() == null) {
            db.setCityName(dto.getCityName());
        }
        if (db.getRegion() == null) {
            db.setRegion(dto.getRegion());
        }
        if (db.getCategory() == null) {
            db.setCategory(dto.getCategory());
        }
        if (db.getMajorCount() == null) {
            db.setMajorCount(dto.getMajorCount() != null ? dto.getMajorCount() : 0);
        }
        if (db.getEducationLevel() == null) {
            db.setEducationLevel(dto.getEducationLevel());
        }
        if (db.getNature() == null) {
            db.setNature(dto.getNature());
        }
        if (db.getRecommendationRate() == null) {
            db.setRecommendationRate(dto.getRecommendationRate());
        }
        if (db.getRecommendationYear() == null) {
            db.setRecommendationYear(dto.getRecommendationYear());
        }
        if (db.getHasDoctorate() == null) {
            db.setHasDoctorate(dto.getHasDoctorate() != null ? dto.getHasDoctorate() : false);
        }
        if (db.getHasMaster() == null) {
            db.setHasMaster(dto.getHasMaster() != null ? dto.getHasMaster() : false);
        }
        if (db.getDepartment() == null) {
            db.setDepartment(dto.getDepartment());
        }
        if (isBlankList(db.getTags()) && !isBlankList(dto.getTags())) {
            db.setTags(dto.getTags());
        }
        if (db.getFamousUnion() == null) {
            db.setFamousUnion(dto.getFamousUnion());
        }
        if (db.getImageUrl() == null) {
            db.setImageUrl(dto.getImageUrl());
        }
        if (db.getIntroduction() == null) {
            db.setIntroduction(dto.getIntroduction());
        }
        db.setUpdatedAt(now);
    }

    private boolean isBlankList(List<String> list) {
        return list == null || list.isEmpty();
    }

    /**
     * 汇总错误信息（限制展示条数，避免一次性返回过长文本）。
     */
    private String joinErrors(List<String> errors) {
        if (errors.size() <= MAX_ERROR_SHOWN) {
            return String.join("; ", errors);
        }
        return String.join("; ", errors.subList(0, MAX_ERROR_SHOWN))
                + "; ...(仅显示前" + MAX_ERROR_SHOWN + "条，共" + errors.size() + "行存在错误)";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importUniversityDetails(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }

        List<UniversityDetailExcelDTO> dataList;
        try {
            // 按 sheet 名读取「院校详情」，避免误读第一个 sheet（如主表 sheet）
            dataList = EasyExcel.read(file.getInputStream())
                    .head(UniversityDetailExcelDTO.class)
                    .sheet("院校详情")
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "读取Excel文件失败: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("读取Excel详情sheet失败", e);
            throw new BusinessException(400, "读取Excel详情数据失败，请确认sheet名称为「院校详情」且表头与模板一致: " + e.getMessage());
        }

        if (dataList != null && dataList.size() > MAX_IMPORT_ROWS) {
            throw new BusinessException(400, "单次导入不能超过" + MAX_IMPORT_ROWS + "条记录");
        }

        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }

        List<String> errors = new ArrayList<>();
        OffsetDateTime now = OffsetDateTime.now();
        int insertCount = 0;
        int updateCount = 0;

        for (int i = 0; i < dataList.size(); i++) {
            int rowNum = i + 2;
            UniversityDetailExcelDTO dto = dataList.get(i);

            // 校验必填字段
            if (!StringUtils.hasText(dto.getUniversityName())) {
                errors.add("第" + rowNum + "行: 院校名称不能为空");
                continue;
            }

            // 根据院校名称查找院校
            LambdaQueryWrapper<University> univWrapper = new LambdaQueryWrapper<>();
            univWrapper.eq(University::getName, dto.getUniversityName())
                       .ne(University::getStatus, (short) 0);
            List<University> univList = universityMapper.selectList(univWrapper);
            University university = univList.isEmpty() ? null : univList.get(0);

            if (university == null) {
                errors.add("第" + rowNum + "行: 院校[" + dto.getUniversityName() + "]不存在");
                continue;
            }

            // 字段长度校验（对应 t_universities_detail 列定义）
            int errBefore = errors.size();
            checkLength(dto.getAddress(), "学校地址", 200, rowNum, errors);
            checkLength(dto.getAdmissionPhone(), "招生电话", 200, rowNum, errors);
            checkLength(dto.getWebsite(), "官方网站", 500, rowNum, errors);
            checkLength(dto.getAbroadRate(), "出国比例", 10, rowNum, errors);
            checkLength(dto.getGenderRatio(), "男女比例", 10, rowNum, errors);
            if (errors.size() > errBefore) {
                continue;
            }

            // 检查是否已有详情记录
            LambdaQueryWrapper<UniversityDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.eq(UniversityDetail::getUniversityId, university.getId());
            List<UniversityDetail> detailList = universityDetailMapper.selectList(detailWrapper);
            UniversityDetail existingDetail = detailList.isEmpty() ? null : detailList.get(0);

            try {
                if (existingDetail != null) {
                    // 已存在：仅补齐数据库中为 NULL 的字段，已有数据一律不覆盖
                    fillDetailGaps(existingDetail, dto, now);
                    universityDetailMapper.updateById(existingDetail);
                    updateCount++;
                } else {
                    // 新建详情记录
                    UniversityDetail detail = UniversityDetail.builder()
                            .id(SnowflakeIdGenerator.nextId())
                            .universityId(university.getId())
                            .address(dto.getAddress())
                            .admissionPhone(dto.getAdmissionPhone())
                            .website(dto.getWebsite())
                            .historyGroupScore(parseIntOrNull(dto.getHistoryGroupScore()))
                            .scienceGroupScore(parseIntOrNull(dto.getScienceGroupScore()))
                            .carouselImages(dto.getCarouselImages())
                            .introduction(dto.getIntroduction())
                            .rankings(buildRankings(dto))
                            .abroadRate(dto.getAbroadRate())
                            .genderRatio(dto.getGenderRatio())
                            .sortOrder(0)
                            .status((short) 1)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    universityDetailMapper.insert(detail);
                    insertCount++;
                }
            } catch (Exception e) {
                log.error("第{}行导入院校详情失败", rowNum, e);
                errors.add("第" + rowNum + "行: 保存失败(" + e.getMessage() + ")");
            }
        }

        if (!errors.isEmpty()) {
            String errorMsg = String.format("导入失败，共%d行数据存在错误，已全部回滚。错误信息：%s",
                    errors.size(), joinErrors(errors));
            throw new BusinessException(400, errorMsg);
        }

        log.info("导入院校详情数据成功: 新增{}条, 补齐{}条", insertCount, updateCount);
        int total = dataList.size();
        int failed = 0; // 整批回滚，无部分成功
        return ImportResultVO.builder()
                .total(total)
                .success(total - failed)
                .failed(failed)
                .updated(updateCount)
                .errors(errors)
                .build();
    }

    /**
     * 安全解析字符串为 Integer，空串/非数字返回 null
     */
    private Integer parseIntOrNull(String val) {
        if (val == null || val.trim().isEmpty()) {
            return null;
        }
        try {
            // 处理 "450.0" 这类带小数的数值，去掉小数部分
            return (int) Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            log.debug("无法解析为整数: {}", val);
            return null;
        }
    }

    /**
     * 组装排名 JSONB Map（跳过空值）
     */
    private Map<String, Integer> buildRankings(UniversityDetailExcelDTO dto) {
        Map<String, Integer> rankings = new HashMap<>();
        Integer ruanke = parseIntOrNull(dto.getRuanke());
        Integer xiaoyouhui = parseIntOrNull(dto.getXiaoyouhui());
        Integer wushulian = parseIntOrNull(dto.getWushulian());
        Integer qs = parseIntOrNull(dto.getQs());
        Integer usnews = parseIntOrNull(dto.getUsnews());
        if (ruanke != null) {
            rankings.put("ruanke", ruanke);
        }
        if (xiaoyouhui != null) {
            rankings.put("xiaoyouhui", xiaoyouhui);
        }
        if (wushulian != null) {
            rankings.put("wushulian", wushulian);
        }
        if (qs != null) {
            rankings.put("qs", qs);
        }
        if (usnews != null) {
            rankings.put("usnews", usnews);
        }
        return rankings.isEmpty() ? null : rankings;
    }

    /**
     * 已存在的详情记录：仅补齐数据库中为 NULL 的字段，已有数据一律不覆盖。
     * rankings 为 JSONB，按排名项逐项补齐（已有排名项不覆盖，只补缺失项）。
     */
    private void fillDetailGaps(UniversityDetail db, UniversityDetailExcelDTO dto, OffsetDateTime now) {
        Map<String, Integer> importedRankings = buildRankings(dto);
        if (db.getAddress() == null) {
            db.setAddress(dto.getAddress());
        }
        if (db.getAdmissionPhone() == null) {
            db.setAdmissionPhone(dto.getAdmissionPhone());
        }
        if (db.getWebsite() == null) {
            db.setWebsite(dto.getWebsite());
        }
        if (db.getHistoryGroupScore() == null) {
            db.setHistoryGroupScore(parseIntOrNull(dto.getHistoryGroupScore()));
        }
        if (db.getScienceGroupScore() == null) {
            db.setScienceGroupScore(parseIntOrNull(dto.getScienceGroupScore()));
        }
        if (isBlankList(db.getCarouselImages()) && !isBlankList(dto.getCarouselImages())) {
            db.setCarouselImages(dto.getCarouselImages());
        }
        if (db.getIntroduction() == null) {
            db.setIntroduction(dto.getIntroduction());
        }
        if (db.getRankings() == null) {
            db.setRankings(importedRankings);
        } else if (importedRankings != null) {
            // 排名按项补齐：已有排名项不覆盖
            Map<String, Integer> merged = new HashMap<>(db.getRankings());
            importedRankings.forEach((key, value) -> {
                if (merged.get(key) == null) {
                    merged.put(key, value);
                }
            });
            db.setRankings(merged);
        }
        if (db.getAbroadRate() == null) {
            db.setAbroadRate(dto.getAbroadRate());
        }
        if (db.getGenderRatio() == null) {
            db.setGenderRatio(dto.getGenderRatio());
        }
        db.setUpdatedAt(now);
    }
}
