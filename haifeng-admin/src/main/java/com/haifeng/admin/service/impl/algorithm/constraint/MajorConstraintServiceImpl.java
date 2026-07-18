package com.haifeng.admin.service.impl.algorithm.constraint;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.algorithm.constraint.MajorConstraintAddDTO;
import com.haifeng.admin.dto.algorithm.constraint.MajorConstraintQueryDTO;
import com.haifeng.admin.excel.algorithm.constraint.MajorConstraintImportDTO;
import com.haifeng.admin.service.algorithm.constraint.MajorConstraintService;
import com.haifeng.admin.vo.algorithm.constraint.MajorConstraintDetailVO;
import com.haifeng.admin.vo.algorithm.constraint.MajorConstraintListVO;
import com.haifeng.common.entity.algorithm.MajorConstraint;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.ConstraintDictMapper;
import com.haifeng.common.mapper.algorithm.MajorConstraintMapper;
import com.haifeng.common.mapper.major.MajorMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MajorConstraintServiceImpl implements MajorConstraintService {

    private final MajorConstraintMapper majorConstraintMapper;
    private final MajorMapper majorMapper;
    private final ConstraintDictMapper constraintDictMapper;

    private static final int MAX_ERRORS = 20;
    private static final int MAX_IMPORT_ROWS = 1000;

    @lombok.AllArgsConstructor
    private static class ValidRow {
        String majorCode;
        String constraintCode;
        MajorConstraintImportDTO dto;
    }

    @Override
    public IPage<MajorConstraintListVO> page(MajorConstraintQueryDTO dto) {
        Page<MajorConstraint> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<MajorConstraint> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getMajorCode())) {
            wrapper.eq(MajorConstraint::getMajorCode, dto.getMajorCode());
        }
        if (StringUtils.hasText(dto.getMajorName())) {
            wrapper.eq(MajorConstraint::getMajorName, dto.getMajorName());
        }
        if (StringUtils.hasText(dto.getConstraintCode())) {
            wrapper.eq(MajorConstraint::getConstraintCode, dto.getConstraintCode());
        }
        if (StringUtils.hasText(dto.getConstraintName())) {
            wrapper.eq(MajorConstraint::getConstraintName, dto.getConstraintName());
        }
        wrapper.orderByAsc(MajorConstraint::getMajorCode).orderByAsc(MajorConstraint::getConstraintCode);
        IPage<MajorConstraint> resultPage = majorConstraintMapper.selectPage(page, wrapper);
        return resultPage.convert(this::convertToListVO);
    }

    @Override
    public MajorConstraintDetailVO detail(Long id) {
        MajorConstraint entity = majorConstraintMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "专业约束关联不存在");
        }
        return convertToDetailVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(MajorConstraintAddDTO dto) {
        String majorCode = majorMapper.selectCodeByName(dto.getMajorName());
        if (majorCode == null) {
            throw new BusinessException(400, "专业名称[" + dto.getMajorName() + "]不存在");
        }
        String constraintCode = constraintDictMapper.selectCodeByName(dto.getConstraintName());
        if (constraintCode == null) {
            throw new BusinessException(400, "约束名称[" + dto.getConstraintName() + "]不存在");
        }
        MajorConstraint deleted = majorConstraintMapper.selectDeletedByBusinessKey(majorCode, constraintCode);
        if (deleted != null) {
            deleted.setMajorName(dto.getMajorName());
            deleted.setConstraintName(dto.getConstraintName());
            deleted.setRemark(dto.getRemark());
            deleted.setIsDeleted(false);
            majorConstraintMapper.updateById(deleted);
            log.info("恢复已删除专业约束关联，majorCode={}, constraintCode={}", majorCode, constraintCode);
            return deleted.getId();
        }
        if (majorConstraintMapper.countByBusinessKey(majorCode, constraintCode) > 0) {
            throw new BusinessException(400, "该专业约束关联已存在");
        }
        MajorConstraint entity = MajorConstraint.builder()
                .id(SnowflakeIdGenerator.nextId())
                .majorCode(majorCode).majorName(dto.getMajorName())
                .constraintCode(constraintCode).constraintName(dto.getConstraintName())
                .remark(dto.getRemark()).isDeleted(false).version(0).build();
        majorConstraintMapper.insert(entity);
        log.info("新增专业约束关联，majorCode={}, constraintCode={}", majorCode, constraintCode);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        MajorConstraint existing = majorConstraintMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "专业约束关联不存在");
        }
        majorConstraintMapper.deleteById(id);
        log.info("删除专业约束关联，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的记录");
        }
        int count = majorConstraintMapper.batchSoftDelete(ids);
        log.info("批量删除专业约束关联，count={}", count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer importData(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }

        // P2: 文件类型校验
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.toLowerCase().endsWith(".xlsx") && !filename.toLowerCase().endsWith(".xls"))) {
            throw new BusinessException(400, "请上传Excel文件（.xlsx或.xls）");
        }

        List<MajorConstraintImportDTO> dataList;
        try {
            dataList = EasyExcel.read(file.getInputStream())
                    .head(MajorConstraintImportDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "读取Excel文件失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("解析Excel文件失败", e);
            throw new BusinessException(400, "Excel文件格式错误，请检查文件内容");
        }

        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }

        // P4: 行数上限
        if (dataList.size() > MAX_IMPORT_ROWS) {
            throw new BusinessException(400, "单次导入不能超过1000条记录");
        }

        // ==================== 预加载阶段（P3: 消除 N+1 查询） ====================
        // 预加载 majorName -> majorCode（去重后逐个查询，unique name 数量远小于行数）
        Map<String, String> majorNameToCode = new HashMap<>();
        for (MajorConstraintImportDTO dto : dataList) {
            if (StringUtils.hasText(dto.getMajorName()) && !majorNameToCode.containsKey(dto.getMajorName())) {
                majorNameToCode.put(dto.getMajorName(), majorMapper.selectCodeByName(dto.getMajorName()));
            }
        }

        // 预加载 constraintName -> constraintCode
        Map<String, String> constraintNameToCode = new HashMap<>();
        for (MajorConstraintImportDTO dto : dataList) {
            if (StringUtils.hasText(dto.getConstraintName()) && !constraintNameToCode.containsKey(dto.getConstraintName())) {
                constraintNameToCode.put(dto.getConstraintName(), constraintDictMapper.selectCodeByName(dto.getConstraintName()));
            }
        }

        // 批量查询已存在的业务键（active + deleted）
        Set<String> dbExistingKeys = new HashSet<>();
        Map<String, MajorConstraint> deletedKeyToEntity = new HashMap<>();

        // 收集所有有效业务键
        Map<String, Object[]> businessKeyFields = new LinkedHashMap<>();
        for (MajorConstraintImportDTO dto : dataList) {
            String majorCode = majorNameToCode.get(dto.getMajorName());
            String constraintCode = constraintNameToCode.get(dto.getConstraintName());
            if (majorCode != null && constraintCode != null) {
                String key = majorCode + "_" + constraintCode;
                if (!businessKeyFields.containsKey(key)) {
                    businessKeyFields.put(key, new Object[]{majorCode, constraintCode});
                }
            }
        }

        if (!businessKeyFields.isEmpty()) {
            List<Map<String, Object>> queryKeys = new ArrayList<>();
            for (Object[] fields : businessKeyFields.values()) {
                Map<String, Object> m = new HashMap<>();
                m.put("majorCode", fields[0]);
                m.put("constraintCode", fields[1]);
                queryKeys.add(m);
            }
            List<String> existingActiveKeys = majorConstraintMapper.selectExistingKeys(queryKeys);
            dbExistingKeys.addAll(existingActiveKeys);

            List<MajorConstraint> deletedRecords = majorConstraintMapper.selectDeletedByKeys(queryKeys);
            for (MajorConstraint mc : deletedRecords) {
                String key = mc.getMajorCode() + "_" + mc.getConstraintCode();
                deletedKeyToEntity.put(key, mc);
            }
        }

        // ==================== 校验阶段 ====================
        List<String> errors = new ArrayList<>();
        Set<String> excelKeys = new HashSet<>();
        List<ValidRow> validRows = new ArrayList<>();
        List<MajorConstraint> restoreRows = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            int rowNum = i + 2;
            MajorConstraintImportDTO dto = dataList.get(i);

            if (!StringUtils.hasText(dto.getMajorName())) {
                errors.add("第" + rowNum + "行: 专业名称不能为空");
                continue;
            }
            if (!StringUtils.hasText(dto.getConstraintName())) {
                errors.add("第" + rowNum + "行: 约束名称不能为空");
                continue;
            }

            // P5: 字段长度校验
            if (dto.getMajorName().length() > 100) {
                errors.add("第" + rowNum + "行: 专业名称长度不能超过100");
                continue;
            }
            if (dto.getConstraintName().length() > 100) {
                errors.add("第" + rowNum + "行: 约束名称长度不能超过100");
                continue;
            }
            if (dto.getRemark() != null && dto.getRemark().length() > 200) {
                errors.add("第" + rowNum + "行: 备注长度不能超过200");
                continue;
            }

            String majorCode = majorNameToCode.get(dto.getMajorName());
            if (majorCode == null) {
                errors.add("第" + rowNum + "行: 专业名称[" + dto.getMajorName() + "]不存在");
                continue;
            }
            String constraintCode = constraintNameToCode.get(dto.getConstraintName());
            if (constraintCode == null) {
                errors.add("第" + rowNum + "行: 约束名称[" + dto.getConstraintName() + "]不存在");
                continue;
            }

            String businessKey = majorCode + "_" + constraintCode;
            if (excelKeys.contains(businessKey)) {
                errors.add("第" + rowNum + "行: Excel内存在重复记录");
                continue;
            }
            excelKeys.add(businessKey);

            if (dbExistingKeys.contains(businessKey)) {
                errors.add("第" + rowNum + "行: 数据库已存在该关联（专业=" + dto.getMajorName() + ", 约束=" + dto.getConstraintName() + "）");
                continue;
            }

            MajorConstraint deleted = deletedKeyToEntity.get(businessKey);
            if (deleted != null) {
                deleted.setMajorName(dto.getMajorName());
                deleted.setConstraintName(dto.getConstraintName());
                deleted.setRemark(dto.getRemark());
                deleted.setIsDeleted(false);
                // P1: 不手动 setVersion(0)，让 MyBatis-Plus 自动处理乐观锁
                restoreRows.add(deleted);
                continue;
            }

            if (errors.size() >= MAX_ERRORS) {
                break;
            }

            validRows.add(new ValidRow(majorCode, constraintCode, dto));
        }

        // P6 + P8: 错误信息显示（统一为 ScoreRank 格式）
        if (!errors.isEmpty()) {
            String detail = errors.size() > MAX_ERRORS
                    ? String.join("; ", errors.subList(0, MAX_ERRORS)) + " 等" + errors.size() + "条错误"
                    : String.join("; ", errors);
            throw new BusinessException(400, "数据校验失败：" + detail);
        }

        // ==================== 插入阶段 ====================
        int restoreCount = 0;
        for (MajorConstraint row : restoreRows) {
            majorConstraintMapper.updateById(row);
            restoreCount++;
        }
        int insertCount = 0;
        for (ValidRow row : validRows) {
            MajorConstraint entity = MajorConstraint.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .majorCode(row.majorCode).majorName(row.dto.getMajorName())
                    .constraintCode(row.constraintCode).constraintName(row.dto.getConstraintName())
                    .remark(row.dto.getRemark()).isDeleted(false).version(0).build();
            majorConstraintMapper.insert(entity);
            insertCount++;
        }
        log.info("导入专业约束关联成功: 新增记录={}条, 恢复记录={}条", insertCount, restoreCount);
        return insertCount + restoreCount;
    }

    private MajorConstraintListVO convertToListVO(MajorConstraint entity) {
        MajorConstraintListVO vo = new MajorConstraintListVO();
        vo.setId(entity.getId());
        vo.setMajorCode(entity.getMajorCode());
        vo.setMajorName(entity.getMajorName());
        vo.setConstraintCode(entity.getConstraintCode());
        vo.setConstraintName(entity.getConstraintName());
        vo.setIsDeleted(entity.getIsDeleted());
        vo.setVersion(entity.getVersion());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    private MajorConstraintDetailVO convertToDetailVO(MajorConstraint entity) {
        MajorConstraintDetailVO vo = new MajorConstraintDetailVO();
        vo.setId(entity.getId());
        vo.setMajorCode(entity.getMajorCode());
        vo.setMajorName(entity.getMajorName());
        vo.setConstraintCode(entity.getConstraintCode());
        vo.setConstraintName(entity.getConstraintName());
        vo.setRemark(entity.getRemark());
        vo.setVersion(entity.getVersion());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
