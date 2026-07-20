package com.haifeng.admin.service.impl.algorithm.constraint;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.algorithm.constraint.SafetyLevelAddDTO;
import com.haifeng.admin.dto.algorithm.constraint.SafetyLevelQueryDTO;
import com.haifeng.admin.service.algorithm.constraint.SafetyLevelService;
import com.haifeng.admin.vo.algorithm.constraint.SafetyLevelDetailVO;
import com.haifeng.admin.vo.algorithm.constraint.SafetyLevelListVO;
import com.haifeng.common.entity.algorithm.SafetyLevelDict;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.SafetyLevelDictMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service("safetyLevelDictService")
@RequiredArgsConstructor
public class SafetyLevelServiceImpl implements SafetyLevelService {

    private final SafetyLevelDictMapper safetyLevelDictMapper;

    @Override
    public IPage<SafetyLevelListVO> page(SafetyLevelQueryDTO dto) {
        Page<SafetyLevelDict> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<SafetyLevelDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SafetyLevelDict::getLevel);
        IPage<SafetyLevelDict> resultPage = safetyLevelDictMapper.selectPage(page, wrapper);
        return resultPage.convert(this::convertToListVO);
    }

    @Override
    public SafetyLevelDetailVO detail(Short level) {
        SafetyLevelDict entity = safetyLevelDictMapper.selectById(level);
        if (entity == null) {
            throw new BusinessException(404, "安全系数等级不存在");
        }
        return convertToDetailVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SafetyLevelAddDTO dto) {
        if (dto.getMinCoefficient().compareTo(dto.getMaxCoefficient()) >= 0) {
            throw new BusinessException(400, "系数下界必须小于系数上界");
        }
        SafetyLevelDict deleted = safetyLevelDictMapper.selectDeletedByLevel(dto.getLevel());
        if (deleted != null) {
            if (safetyLevelDictMapper.countByCodeExclude(dto.getCode(), dto.getLevel()) > 0) {
                throw new BusinessException(400, "代码已存在");
            }
            deleted.setCode(dto.getCode());
            deleted.setName(dto.getName());
            deleted.setNameShort(dto.getNameShort());
            deleted.setMinCoefficient(dto.getMinCoefficient());
            deleted.setMaxCoefficient(dto.getMaxCoefficient());
            deleted.setColor(dto.getColor());
            deleted.setConfidence(dto.getConfidence());
            deleted.setConfidenceReason(dto.getConfidenceReason());
            deleted.setDescription(dto.getDescription());
            deleted.setIsDeleted(false);
            deleted.setVersion(0);
            safetyLevelDictMapper.updateById(deleted);
            log.info("恢复已删除安全系数等级，level={}", dto.getLevel());
            return;
        }
        if (safetyLevelDictMapper.selectById(dto.getLevel()) != null) {
            throw new BusinessException(400, "等级已存在");
        }
        if (safetyLevelDictMapper.countByCode(dto.getCode()) > 0) {
            throw new BusinessException(400, "代码已存在");
        }
        SafetyLevelDict entity = SafetyLevelDict.builder()
                .level(dto.getLevel()).code(dto.getCode()).name(dto.getName())
                .nameShort(dto.getNameShort()).minCoefficient(dto.getMinCoefficient())
                .maxCoefficient(dto.getMaxCoefficient()).color(dto.getColor())
                .confidence(dto.getConfidence()).confidenceReason(dto.getConfidenceReason())
                .description(dto.getDescription()).isDeleted(false).version(0).build();
        safetyLevelDictMapper.insert(entity);
        log.info("新增安全系数等级，level={}", dto.getLevel());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Short level, SafetyLevelAddDTO dto) {
        SafetyLevelDict existing = safetyLevelDictMapper.selectById(level);
        if (existing == null) {
            throw new BusinessException(404, "安全系数等级不存在");
        }
        if (dto.getMinCoefficient().compareTo(dto.getMaxCoefficient()) >= 0) {
            throw new BusinessException(400, "系数下界必须小于系数上界");
        }
        if (safetyLevelDictMapper.countByCodeExclude(dto.getCode(), level) > 0) {
            throw new BusinessException(400, "代码已存在");
        }
        existing.setCode(dto.getCode());
        existing.setName(dto.getName());
        existing.setNameShort(dto.getNameShort());
        existing.setMinCoefficient(dto.getMinCoefficient());
        existing.setMaxCoefficient(dto.getMaxCoefficient());
        existing.setColor(dto.getColor());
        existing.setConfidence(dto.getConfidence());
        existing.setConfidenceReason(dto.getConfidenceReason());
        existing.setDescription(dto.getDescription());
        safetyLevelDictMapper.updateById(existing);
        log.info("修改安全系数等级，level={}", level);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Short level) {
        SafetyLevelDict existing = safetyLevelDictMapper.selectById(level);
        if (existing == null) {
            throw new BusinessException(404, "安全系数等级不存在");
        }
        safetyLevelDictMapper.deleteById(level);
        log.info("删除安全系数等级，level={}", level);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Short> levels) {
        if (levels == null || levels.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的记录");
        }
        int count = safetyLevelDictMapper.batchSoftDelete(levels);
        log.info("批量删除安全系数等级，count={}", count);
    }

    private SafetyLevelListVO convertToListVO(SafetyLevelDict entity) {
        SafetyLevelListVO vo = new SafetyLevelListVO();
        vo.setLevel(entity.getLevel());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setNameShort(entity.getNameShort());
        vo.setMinCoefficient(entity.getMinCoefficient());
        vo.setMaxCoefficient(entity.getMaxCoefficient());
        vo.setConfidence(entity.getConfidence());
        vo.setIsDeleted(entity.getIsDeleted());
        vo.setVersion(entity.getVersion());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    private SafetyLevelDetailVO convertToDetailVO(SafetyLevelDict entity) {
        SafetyLevelDetailVO vo = new SafetyLevelDetailVO();
        vo.setLevel(entity.getLevel());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setNameShort(entity.getNameShort());
        vo.setMinCoefficient(entity.getMinCoefficient());
        vo.setMaxCoefficient(entity.getMaxCoefficient());
        vo.setColor(entity.getColor());
        vo.setConfidence(entity.getConfidence());
        vo.setConfidenceReason(entity.getConfidenceReason());
        vo.setDescription(entity.getDescription());
        vo.setIsDeleted(entity.getIsDeleted());
        vo.setVersion(entity.getVersion());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
