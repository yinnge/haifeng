package com.haifeng.admin.service.impl.special;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.special.StrongBaseScoreAddDTO;
import com.haifeng.admin.dto.special.StrongBaseScoreQueryDTO;
import com.haifeng.admin.service.special.StrongBaseScoreService;
import com.haifeng.admin.vo.special.StrongBaseScoreDetailVO;
import com.haifeng.admin.vo.special.StrongBaseScoreListVO;
import com.haifeng.common.entity.special.StrongBaseScore;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.special.StrongBaseScoreMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrongBaseScoreServiceImpl implements StrongBaseScoreService {

    private final StrongBaseScoreMapper strongBaseScoreMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public IPage<StrongBaseScoreListVO> page(StrongBaseScoreQueryDTO dto) {
        Page<StrongBaseScore> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<StrongBaseScore> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getUniversityName())) {
            wrapper.eq(StrongBaseScore::getUniversityName, dto.getUniversityName());
        }
        if (dto.getYear() != null) {
            wrapper.eq(StrongBaseScore::getYear, dto.getYear());
        }
        if (StringUtils.hasText(dto.getProvince())) {
            wrapper.eq(StrongBaseScore::getProvince, dto.getProvince());
        }
        if (StringUtils.hasText(dto.getSubjectType())) {
            wrapper.eq(StrongBaseScore::getSubjectType, dto.getSubjectType());
        }
        wrapper.orderByDesc(StrongBaseScore::getYear).orderByDesc(StrongBaseScore::getCreatedAt);
        IPage<StrongBaseScore> result = strongBaseScoreMapper.selectPage(page, wrapper);
        return result.convert(entity -> {
            StrongBaseScoreListVO vo = new StrongBaseScoreListVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        });
    }

    @Override
    public StrongBaseScoreDetailVO detail(Long id) {
        StrongBaseScore entity = strongBaseScoreMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "强基计划数据不存在");
        }
        StrongBaseScoreDetailVO vo = new StrongBaseScoreDetailVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(StrongBaseScoreAddDTO dto) {
        if (strongBaseScoreMapper.countByUnique(dto.getUniversityId(), dto.getYear(), dto.getProvince(), dto.getSubjectType(), dto.getMajorName()) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该强基计划数据已存在");
        }
        StrongBaseScore entity = new StrongBaseScore();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(snowflakeIdGenerator.nextId());
        entity.setIsActive(true);
        if (entity.getEntryScoreType() == null) {
            entity.setEntryScoreType("高考成绩");
        }
        if (entity.getAdmissionFormula() == null) {
            entity.setAdmissionFormula("高考成绩×85%+校测成绩×15%");
        }
        strongBaseScoreMapper.insert(entity);
        log.info("新增强基计划数据: universityId={}, year={}, province={}, majorName={}", dto.getUniversityId(), dto.getYear(), dto.getProvince(), dto.getMajorName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, StrongBaseScoreAddDTO dto) {
        StrongBaseScore entity = strongBaseScoreMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "强基计划数据不存在");
        }
        boolean keyChanged = !entity.getUniversityId().equals(dto.getUniversityId())
                || !entity.getYear().equals(dto.getYear())
                || !entity.getProvince().equals(dto.getProvince())
                || !entity.getSubjectType().equals(dto.getSubjectType())
                || !entity.getMajorName().equals(dto.getMajorName());
        if (keyChanged && strongBaseScoreMapper.countByUniqueExclude(dto.getUniversityId(), dto.getYear(), dto.getProvince(), dto.getSubjectType(), dto.getMajorName(), id) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该强基计划数据已存在");
        }
        BeanUtils.copyProperties(dto, entity);
        strongBaseScoreMapper.updateById(entity);
        log.info("修改强基计划数据: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleActive(Long id) {
        StrongBaseScore entity = strongBaseScoreMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "强基计划数据不存在");
        }
        entity.setIsActive(!entity.getIsActive());
        strongBaseScoreMapper.updateById(entity);
        log.info("切换强基计划数据状态: id={}, isActive={}", id, entity.getIsActive());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (strongBaseScoreMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "强基计划数据不存在");
        }
        strongBaseScoreMapper.deleteById(id);
        log.info("删除强基计划数据: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "ID列表不能为空");
        }
        strongBaseScoreMapper.deleteBatchIds(ids);
        log.info("批量删除强基计划数据: ids={}", ids);
    }
}
