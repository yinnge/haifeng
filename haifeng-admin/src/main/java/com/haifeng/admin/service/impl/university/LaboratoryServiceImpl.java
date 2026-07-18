package com.haifeng.admin.service.impl.university;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haifeng.admin.dto.university.LaboratoryAddDTO;
import com.haifeng.admin.dto.university.LaboratoryQueryDTO;
import com.haifeng.admin.dto.university.LaboratoryUpdateDTO;
import com.haifeng.admin.excel.university.CoreTeamExcelDTO;
import com.haifeng.admin.excel.university.LaboratoryExcelDTO;
import com.haifeng.admin.excel.university.StatisticsExcelDTO;
import com.haifeng.admin.service.university.LaboratoryService;
import com.haifeng.admin.vo.university.LaboratoryDetailVO;
import com.haifeng.admin.vo.university.LaboratoryListVO;
import com.haifeng.common.entity.university.Laboratory;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.LaboratoryMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LaboratoryServiceImpl extends ServiceImpl<LaboratoryMapper, Laboratory> implements LaboratoryService {

    private final LaboratoryMapper laboratoryMapper;
    private final UniversityMapper universityMapper;

    @Override
    public IPage<LaboratoryListVO> page(LaboratoryQueryDTO dto) {
        Page<Laboratory> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Laboratory> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(Laboratory::getStatus, (short) 0);

        if (StringUtils.hasText(dto.getUniversityName())) {
            wrapper.like(Laboratory::getUniversityName, dto.getUniversityName());
        }
        if (StringUtils.hasText(dto.getName())) {
            wrapper.like(Laboratory::getName, dto.getName());
        }
        if (StringUtils.hasText(dto.getLabType())) {
            wrapper.eq(Laboratory::getLabType, dto.getLabType());
        }
        if (StringUtils.hasText(dto.getRegion())) {
            wrapper.eq(Laboratory::getRegion, dto.getRegion());
        }
        if (StringUtils.hasText(dto.getDepartment())) {
            wrapper.eq(Laboratory::getDepartment, dto.getDepartment());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(Laboratory::getStatus, dto.getStatus());
        }

        wrapper.orderByAsc(Laboratory::getSortOrder).orderByDesc(Laboratory::getCreatedAt);

        IPage<Laboratory> labPage = laboratoryMapper.selectPage(page, wrapper);

        return labPage.convert(lab -> LaboratoryListVO.builder()
                .id(lab.getId())
                .universityId(lab.getUniversityId())
                .universityName(lab.getUniversityName())
                .name(lab.getName())
                .labType(lab.getLabType())
                .region(lab.getRegion())
                .department(lab.getDepartment())
                .director(lab.getDirector())
                .status(lab.getStatus() != null ? lab.getStatus().intValue() : null)
                .createdAt(lab.getCreatedAt())
                .build());
    }

    @Override
    public LaboratoryDetailVO detail(Long id) {
        Laboratory lab = laboratoryMapper.selectById(id);
        if (lab == null || (lab.getStatus() != null && lab.getStatus() == 0)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "实验室不存在");
        }

        return LaboratoryDetailVO.builder()
                .id(lab.getId())
                .universityId(lab.getUniversityId())
                .universityName(lab.getUniversityName())
                .name(lab.getName())
                .labType(lab.getLabType())
                .establishedYear(lab.getEstablishedYear())
                .region(lab.getRegion())
                .department(lab.getDepartment())
                .director(lab.getDirector())
                .staffCount(lab.getStaffCount())
                .studentCount(lab.getStudentCount())
                .email(lab.getEmail())
                .phone(lab.getPhone())
                .introduction(lab.getIntroduction())
                .researchDescription(lab.getResearchDescription())
                .labSpace(lab.getLabSpace())
                .openTopics(lab.getOpenTopics())
                .cooperation(lab.getCooperation())
                .visitingScholars(lab.getVisitingScholars())
                .researchFields(lab.getResearchFields())
                .statistics(lab.getStatistics())
                .majorEquipment(lab.getMajorEquipment())
                .coreTeam(lab.getCoreTeam())
                .sortOrder(lab.getSortOrder())
                .status(lab.getStatus() != null ? lab.getStatus().intValue() : null)
                .createdAt(lab.getCreatedAt())
                .updatedAt(lab.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(LaboratoryAddDTO dto) {
        University university = universityMapper.selectById(dto.getUniversityId());
        if (university == null || university.getStatus() == 0) {
            throw new BusinessException(400, "院校不存在");
        }

        if (laboratoryMapper.existsByUniversityIdAndName(dto.getUniversityId(), dto.getName())) {
            throw new BusinessException(400, "该院校下实验室名称已存在");
        }

        OffsetDateTime now = OffsetDateTime.now();
        Long id = SnowflakeIdGenerator.nextId();

        Laboratory lab = Laboratory.builder()
                .id(id)
                .universityId(dto.getUniversityId())
                .universityName(university.getName())
                .name(dto.getName())
                .labType(dto.getLabType())
                .establishedYear(dto.getEstablishedYear())
                .region(dto.getRegion())
                .department(dto.getDepartment())
                .director(dto.getDirector())
                .staffCount(dto.getStaffCount())
                .studentCount(dto.getStudentCount())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .introduction(dto.getIntroduction())
                .researchDescription(dto.getResearchDescription())
                .labSpace(dto.getLabSpace())
                .openTopics(dto.getOpenTopics())
                .cooperation(dto.getCooperation())
                .visitingScholars(dto.getVisitingScholars())
                .researchFields(dto.getResearchFields())
                .statistics(dto.getStatistics())
                .majorEquipment(dto.getMajorEquipment())
                .coreTeam(dto.getCoreTeam())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .status((short) 1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        laboratoryMapper.insert(lab);
        log.info("新增实验室成功，id={}, name={}", id, dto.getName());
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, LaboratoryUpdateDTO dto) {
        Laboratory lab = laboratoryMapper.selectById(id);
        if (lab == null || lab.getStatus() == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "实验室不存在");
        }

        if (StringUtils.hasText(dto.getName()) && !dto.getName().equals(lab.getName())) {
            if (laboratoryMapper.existsByUniversityIdAndName(lab.getUniversityId(), dto.getName())) {
                throw new BusinessException(400, "该院校下实验室名称已存在");
            }
            lab.setName(dto.getName());
        }

        if (dto.getLabType() != null) lab.setLabType(dto.getLabType());
        if (dto.getEstablishedYear() != null) lab.setEstablishedYear(dto.getEstablishedYear());
        if (dto.getRegion() != null) lab.setRegion(dto.getRegion());
        if (dto.getDepartment() != null) lab.setDepartment(dto.getDepartment());
        if (dto.getDirector() != null) lab.setDirector(dto.getDirector());
        if (dto.getStaffCount() != null) lab.setStaffCount(dto.getStaffCount());
        if (dto.getStudentCount() != null) lab.setStudentCount(dto.getStudentCount());
        if (dto.getEmail() != null) lab.setEmail(dto.getEmail());
        if (dto.getPhone() != null) lab.setPhone(dto.getPhone());
        if (dto.getIntroduction() != null) lab.setIntroduction(dto.getIntroduction());
        if (dto.getResearchDescription() != null) lab.setResearchDescription(dto.getResearchDescription());
        if (dto.getLabSpace() != null) lab.setLabSpace(dto.getLabSpace());
        if (dto.getOpenTopics() != null) lab.setOpenTopics(dto.getOpenTopics());
        if (dto.getCooperation() != null) lab.setCooperation(dto.getCooperation());
        if (dto.getVisitingScholars() != null) lab.setVisitingScholars(dto.getVisitingScholars());
        if (dto.getResearchFields() != null) lab.setResearchFields(dto.getResearchFields());
        if (dto.getStatistics() != null) lab.setStatistics(dto.getStatistics());
        if (dto.getMajorEquipment() != null) lab.setMajorEquipment(dto.getMajorEquipment());
        if (dto.getCoreTeam() != null) lab.setCoreTeam(dto.getCoreTeam());
        if (dto.getSortOrder() != null) lab.setSortOrder(dto.getSortOrder());
        if (dto.getStatus() != null) lab.setStatus(dto.getStatus().shortValue());

        lab.setUpdatedAt(OffsetDateTime.now());
        laboratoryMapper.updateById(lab);
        log.info("更新实验室成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Laboratory lab = laboratoryMapper.selectById(id);
        if (lab == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "实验室不存在");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "状态值无效，只允许0或1");
        }

        LambdaUpdateWrapper<Laboratory> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Laboratory::getId, id)
               .set(Laboratory::getStatus, status.shortValue())
               .set(Laboratory::getUpdatedAt, OffsetDateTime.now());
        laboratoryMapper.update(null, wrapper);
        log.info("更新实验室状态，id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        updateStatus(id, 0);
        log.info("软删除实验室，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDelete(Long id) {
        Laboratory lab = laboratoryMapper.selectById(id);
        if (lab == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "实验室不存在");
        }
        if (lab.getStatus() != null && lab.getStatus() == 0) {
            throw new BusinessException(400, "该记录已软删除，不可硬删除");
        }
        laboratoryMapper.deleteById(id);
        log.info("硬删除实验室，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        int count = laboratoryMapper.selectBatchIds(ids).size();
        if (count != ids.size()) {
            throw new BusinessException(400, "部分记录不存在，共查询到" + count + "条");
        }
        LambdaUpdateWrapper<Laboratory> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(Laboratory::getId, ids)
               .set(Laboratory::getStatus, (short) 0)
               .set(Laboratory::getUpdatedAt, OffsetDateTime.now());
        laboratoryMapper.update(null, wrapper);
        log.info("批量软删除实验室，ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchHardDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        List<Laboratory> records = laboratoryMapper.selectBatchIds(ids);
        if (records.size() != ids.size()) {
            throw new BusinessException(400, "部分记录不存在");
        }
        boolean hasSoftDeleted = records.stream()
                .anyMatch(r -> r.getStatus() != null && r.getStatus() == 0);
        if (hasSoftDeleted) {
            throw new BusinessException(400, "包含已软删除的记录，不可硬删除");
        }
        laboratoryMapper.deleteBatchIds(ids);
        log.info("批量硬删除实验室，ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importLaboratories(MultipartFile file) {
        List<String> errorMsgs = new ArrayList<>();

        try {
            byte[] fileBytes = file.getBytes();

            // Sheet0: 主表数据
            List<LaboratoryExcelDTO> mainData = EasyExcel.read(new ByteArrayInputStream(fileBytes))
                    .head(LaboratoryExcelDTO.class)
                    .sheet(0)
                    .doReadSync();

            // Sheet1: core_team数据
            List<CoreTeamExcelDTO> coreTeamData = EasyExcel.read(new ByteArrayInputStream(fileBytes))
                    .head(CoreTeamExcelDTO.class)
                    .sheet(1)
                    .doReadSync();

            // Sheet2: statistics数据
            List<StatisticsExcelDTO> statisticsData = EasyExcel.read(new ByteArrayInputStream(fileBytes))
                    .head(StatisticsExcelDTO.class)
                    .sheet(2)
                    .doReadSync();

            // 按实验室名称分组JSONB数据
            Map<String, List<Map<String, Object>>> coreTeamMap = coreTeamData.stream()
                    .filter(d -> StringUtils.hasText(d.getLabName()))
                    .collect(Collectors.groupingBy(
                            CoreTeamExcelDTO::getLabName,
                            Collectors.mapping(d -> {
                                Map<String, Object> m = new HashMap<>();
                                m.put("name", d.getMemberName());
                                m.put("position", d.getPosition());
                                m.put("title", d.getJobTitle());
                                return m;
                            }, Collectors.toList())
                    ));

            Map<String, List<Map<String, Object>>> statisticsMap = statisticsData.stream()
                    .filter(d -> StringUtils.hasText(d.getLabName()))
                    .collect(Collectors.groupingBy(
                            StatisticsExcelDTO::getLabName,
                            Collectors.mapping(d -> {
                                Map<String, Object> m = new HashMap<>();
                                m.put("label", d.getLabel());
                                m.put("count", d.getCount());
                                return m;
                            }, Collectors.toList())
                    ));

            // 校验主表数据
            Map<String, Long> universityIdCache = new HashMap<>();
            Map<String, String> universityNameCache = new HashMap<>();
            List<Laboratory> laboratories = new ArrayList<>();

            for (int i = 0; i < mainData.size(); i++) {
                int rowNum = i + 2;
                LaboratoryExcelDTO data = mainData.get(i);

                if (!StringUtils.hasText(data.getUniversityName())) {
                    errorMsgs.add("第" + rowNum + "行：院校名称不能为空");
                    continue;
                }
                if (!StringUtils.hasText(data.getName())) {
                    errorMsgs.add("第" + rowNum + "行：实验室名称不能为空");
                    continue;
                }
                if (!StringUtils.hasText(data.getLabType())) {
                    errorMsgs.add("第" + rowNum + "行：实验室类型不能为空");
                    continue;
                }

                // 查询院校ID
                Long universityId = universityIdCache.get(data.getUniversityName());
                if (universityId == null) {
                    LambdaQueryWrapper<University> uniWrapper = new LambdaQueryWrapper<>();
                    uniWrapper.eq(University::getName, data.getUniversityName()).eq(University::getStatus, (short) 1);
                    University university = universityMapper.selectOne(uniWrapper);
                    if (university == null) {
                        errorMsgs.add("第" + rowNum + "行：院校名称'" + data.getUniversityName() + "'不存在");
                        continue;
                    }
                    universityId = university.getId();
                    universityIdCache.put(data.getUniversityName(), universityId);
                    universityNameCache.put(data.getUniversityName(), university.getName());
                }

                // 检查重复
                if (laboratoryMapper.existsByUniversityIdAndName(universityId, data.getName())) {
                    errorMsgs.add("第" + rowNum + "行：该院校下实验室名称'" + data.getName() + "'已存在");
                    continue;
                }

                OffsetDateTime now = OffsetDateTime.now();
                Laboratory lab = Laboratory.builder()
                        .id(SnowflakeIdGenerator.nextId())
                        .universityId(universityId)
                        .universityName(universityNameCache.get(data.getUniversityName()))
                        .name(data.getName())
                        .labType(data.getLabType())
                        .establishedYear(data.getEstablishedYear())
                        .region(data.getRegion())
                        .department(data.getDepartment())
                        .director(data.getDirector())
                        .staffCount(data.getStaffCount())
                        .studentCount(data.getStudentCount())
                        .email(data.getEmail())
                        .phone(data.getPhone())
                        .introduction(data.getIntroduction())
                        .researchDescription(data.getResearchDescription())
                        .labSpace(data.getLabSpace())
                        .openTopics(data.getOpenTopics())
                        .cooperation(data.getCooperation())
                        .visitingScholars(data.getVisitingScholars())
                        .researchFields(data.getResearchFields())
                        .majorEquipment(data.getMajorEquipment())
                        .coreTeam(coreTeamMap.get(data.getName()))
                        .statistics(statisticsMap.get(data.getName()))
                        .sortOrder(data.getSortOrder() != null ? data.getSortOrder() : 0)
                        .status(data.getStatus() != null ? data.getStatus().shortValue() : (short) 1)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

                laboratories.add(lab);
            }

            if (!errorMsgs.isEmpty()) {
                throw new BusinessException(400, "导入失败，共" + errorMsgs.size() + "行数据存在错误，已全部回滚。错误信息：" + String.join("；", errorMsgs));
            }

            if (!laboratories.isEmpty()) {
                saveBatch(laboratories);
                log.info("导入实验室成功，数量={}", laboratories.size());
            }

        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(500, "读取Excel文件失败");
        }
    }
}
