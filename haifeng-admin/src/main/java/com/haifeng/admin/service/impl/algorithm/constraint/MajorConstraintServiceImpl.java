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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MajorConstraintServiceImpl implements MajorConstraintService {

    private final MajorConstraintMapper majorConstraintMapper;
    private final MajorMapper majorMapper;
    private final ConstraintDictMapper constraintDictMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

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
    public Long add(MajorConstraintAddDTO dto) {
        String majorCode = majorMapper.selectCodeByName(dto.getMajorName());
        if (majorCode == null) {
            throw new BusinessException(400, "专业名称[" + dto.getMajorName() + "]不存在");
        }
        String constraintCode = constraintDictMapper.selectCodeByName(dto.getConstraintName());
        if (constraintCode == null) {
            throw new BusinessException(400, "约束名称[" + dto.getConstraintName() + "]不存在");
        }
        if (majorConstraintMapper.countByBusinessKey(majorCode, constraintCode) > 0) {
            throw new BusinessException(400, "该专业约束关联已存在");
        }
        MajorConstraint entity = MajorConstraint.builder()
                .id(snowflakeIdGenerator.nextId())
                .majorCode(majorCode).majorName(dto.getMajorName())
                .constraintCode(constraintCode).constraintName(dto.getConstraintName())
                .remark(dto.getRemark()).build();
        majorConstraintMapper.insert(entity);
        log.info("新增专业约束关联，majorCode={}, constraintCode={}", majorCode, constraintCode);
        return entity.getId();
    }

    @Override
    public void delete(Long id) {
        int rows = majorConstraintMapper.deleteById(id);
        if (rows == 0) {
            throw new BusinessException(404, "专业约束关联不存在");
        }
        log.info("删除专业约束关联，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的记录");
        }
        majorConstraintMapper.deleteBatchIds(ids);
        log.info("批量删除专业约束关联，ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importData(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }
        List<MajorConstraintImportDTO> dataList;
        try {
            dataList = EasyExcel.read(file.getInputStream()).head(MajorConstraintImportDTO.class).sheet().doReadSync();
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "读取Excel文件失败: " + e.getMessage());
        }
        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException(400, "Excel文件中没有数据");
        }

        List<String> errors = new ArrayList<>();
        Set<String> excelKeys = new HashSet<>();

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
            String majorCode = majorMapper.selectCodeByName(dto.getMajorName());
            if (majorCode == null) {
                errors.add("第" + rowNum + "行: 专业名称[" + dto.getMajorName() + "]不存在");
                continue;
            }
            String constraintCode = constraintDictMapper.selectCodeByName(dto.getConstraintName());
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
            if (majorConstraintMapper.countByBusinessKey(majorCode, constraintCode) > 0) {
                errors.add("第" + rowNum + "行: 数据库已存在该关联（专业=" + dto.getMajorName() + ", 约束=" + dto.getConstraintName() + "）");
            }
        }
        if (!errors.isEmpty()) {
            throw new BusinessException(400, "数据校验失败：" + String.join("; ", errors));
        }

        int insertCount = 0;
        for (MajorConstraintImportDTO dto : dataList) {
            String majorCode = majorMapper.selectCodeByName(dto.getMajorName());
            String constraintCode = constraintDictMapper.selectCodeByName(dto.getConstraintName());
            MajorConstraint entity = MajorConstraint.builder()
                    .id(snowflakeIdGenerator.nextId())
                    .majorCode(majorCode).majorName(dto.getMajorName())
                    .constraintCode(constraintCode).constraintName(dto.getConstraintName())
                    .remark(dto.getRemark()).build();
            majorConstraintMapper.insert(entity);
            insertCount++;
        }
        log.info("导入专业约束关联成功: 新增记录={}条", insertCount);
    }

    private MajorConstraintListVO convertToListVO(MajorConstraint entity) {
        MajorConstraintListVO vo = new MajorConstraintListVO();
        vo.setId(entity.getId());
        vo.setMajorCode(entity.getMajorCode());
        vo.setMajorName(entity.getMajorName());
        vo.setConstraintCode(entity.getConstraintCode());
        vo.setConstraintName(entity.getConstraintName());
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
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}
