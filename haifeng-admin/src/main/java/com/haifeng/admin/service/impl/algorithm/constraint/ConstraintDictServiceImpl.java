package com.haifeng.admin.service.impl.algorithm.constraint;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.algorithm.constraint.ConstraintDictAddDTO;
import com.haifeng.admin.dto.algorithm.constraint.ConstraintDictQueryDTO;
import com.haifeng.admin.service.algorithm.constraint.ConstraintDictService;
import com.haifeng.admin.vo.algorithm.constraint.ConstraintDictDetailVO;
import com.haifeng.admin.vo.algorithm.constraint.ConstraintDictListVO;
import com.haifeng.common.entity.algorithm.ConstraintDict;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.ConstraintDictMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConstraintDictServiceImpl implements ConstraintDictService {

    private final ConstraintDictMapper constraintDictMapper;

    private static final Set<String> VALID_CHECK_FIELDS = Set.of(
            "subject_type", "second_subject_type", "third_subject_type",
            "score_chinese", "score_math", "score_english",
            "score_subject_1", "score_subject_2", "score_subject_3",
            "is_color_blind", "is_color_weak", "vision_left", "vision_right", "has_smell_disorder",
            "height_cm", "weight_kg", "is_left_handed", "has_tattoo", "has_scar", "has_stutter",
            "is_fresh_graduate", "political_status", "household_type", "is_poverty_county",
            "foreign_language"
    );

    @Override
    public IPage<ConstraintDictListVO> page(ConstraintDictQueryDTO dto) {
        Page<ConstraintDict> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<ConstraintDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ConstraintDict::getSortOrder).orderByAsc(ConstraintDict::getCode);
        IPage<ConstraintDict> resultPage = constraintDictMapper.selectPage(page, wrapper);
        return resultPage.convert(this::convertToListVO);
    }

    @Override
    public ConstraintDictDetailVO detail(String code) {
        ConstraintDict entity = constraintDictMapper.selectById(code);
        if (entity == null) {
            throw new BusinessException(404, "约束字典不存在");
        }
        return convertToDetailVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(ConstraintDictAddDTO dto) {
        ConstraintDict deleted = constraintDictMapper.selectDeletedByCode(dto.getCode());
        if (deleted != null) {
            if (constraintDictMapper.countByNameExclude(dto.getName(), dto.getCode()) > 0) {
                throw new BusinessException(400, "约束名称已存在");
            }
            validateCheckField(dto.getCheckField());
            deleted.setName(dto.getName());
            deleted.setCategory(dto.getCategory());
            deleted.setDescription(dto.getDescription());
            deleted.setSeverity(dto.getSeverity());
            deleted.setCheckField(dto.getCheckField());
            deleted.setCheckOperator(dto.getCheckOperator());
            deleted.setCheckValue(dto.getCheckValue());
            deleted.setExtraField(dto.getExtraField());
            deleted.setExtraOperator(dto.getExtraOperator());
            deleted.setExtraValue(dto.getExtraValue());
            deleted.setSortOrder(dto.getSortOrder());
            deleted.setIsActive(dto.getIsActive());
            deleted.setIsDeleted(false);
            deleted.setVersion(0);
            constraintDictMapper.updateById(deleted);
            log.info("恢复已删除约束字典，code={}", dto.getCode());
            return;
        }
        if (constraintDictMapper.selectById(dto.getCode()) != null) {
            throw new BusinessException(400, "约束代码已存在");
        }
        if (constraintDictMapper.countByName(dto.getName()) > 0) {
            throw new BusinessException(400, "约束名称已存在");
        }
        validateCheckField(dto.getCheckField());

        ConstraintDict entity = ConstraintDict.builder()
                .code(dto.getCode()).name(dto.getName()).category(dto.getCategory())
                .description(dto.getDescription()).severity(dto.getSeverity())
                .checkField(dto.getCheckField()).checkOperator(dto.getCheckOperator())
                .checkValue(dto.getCheckValue()).extraField(dto.getExtraField())
                .extraOperator(dto.getExtraOperator()).extraValue(dto.getExtraValue())
                .sortOrder(dto.getSortOrder()).isActive(dto.getIsActive())
                .isDeleted(false).version(0).build();
        constraintDictMapper.insert(entity);
        log.info("新增约束字典，code={}", dto.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(String code, ConstraintDictAddDTO dto) {
        ConstraintDict existing = constraintDictMapper.selectById(code);
        if (existing == null) {
            throw new BusinessException(404, "约束字典不存在");
        }
        if (constraintDictMapper.countByNameExclude(dto.getName(), code) > 0) {
            throw new BusinessException(400, "约束名称已存在");
        }
        validateCheckField(dto.getCheckField());

        existing.setName(dto.getName());
        existing.setCategory(dto.getCategory());
        existing.setDescription(dto.getDescription());
        existing.setSeverity(dto.getSeverity());
        existing.setCheckField(dto.getCheckField());
        existing.setCheckOperator(dto.getCheckOperator());
        existing.setCheckValue(dto.getCheckValue());
        existing.setExtraField(dto.getExtraField());
        existing.setExtraOperator(dto.getExtraOperator());
        existing.setExtraValue(dto.getExtraValue());
        existing.setSortOrder(dto.getSortOrder());
        existing.setIsActive(dto.getIsActive());
        constraintDictMapper.updateById(existing);
        log.info("修改约束字典，code={}", code);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleActive(String code) {
        ConstraintDict existing = constraintDictMapper.selectById(code);
        if (existing == null) {
            throw new BusinessException(404, "约束字典不存在");
        }
        existing.setIsActive(!existing.getIsActive());
        constraintDictMapper.updateById(existing);
        log.info("切换约束字典状态，code={}, isActive={}", code, existing.getIsActive());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String code) {
        ConstraintDict existing = constraintDictMapper.selectById(code);
        if (existing == null) {
            throw new BusinessException(404, "约束字典不存在");
        }
        constraintDictMapper.deleteById(code);
        log.info("删除约束字典，code={}", code);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的记录");
        }
        int count = constraintDictMapper.batchSoftDelete(codes);
        log.info("批量删除约束字典，count={}", count);
    }

    private void validateCheckField(String checkField) {
        if (checkField != null && !checkField.isEmpty() && !VALID_CHECK_FIELDS.contains(checkField)) {
            throw new BusinessException(400, "check_field不合法，只允许高考档案表字段");
        }
    }

    private ConstraintDictListVO convertToListVO(ConstraintDict entity) {
        ConstraintDictListVO vo = new ConstraintDictListVO();
        vo.setCode(entity.getCode());
        vo.setCategory(entity.getCategory());
        vo.setSeverity(entity.getSeverity());
        vo.setCheckField(entity.getCheckField());
        vo.setIsActive(entity.getIsActive());
        vo.setIsDeleted(entity.getIsDeleted());
        vo.setVersion(entity.getVersion());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    private ConstraintDictDetailVO convertToDetailVO(ConstraintDict entity) {
        ConstraintDictDetailVO vo = new ConstraintDictDetailVO();
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setCategory(entity.getCategory());
        vo.setDescription(entity.getDescription());
        vo.setSeverity(entity.getSeverity());
        vo.setCheckField(entity.getCheckField());
        vo.setCheckOperator(entity.getCheckOperator());
        vo.setCheckValue(entity.getCheckValue());
        vo.setExtraField(entity.getExtraField());
        vo.setExtraOperator(entity.getExtraOperator());
        vo.setExtraValue(entity.getExtraValue());
        vo.setSortOrder(entity.getSortOrder());
        vo.setIsActive(entity.getIsActive());
        vo.setVersion(entity.getVersion());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
