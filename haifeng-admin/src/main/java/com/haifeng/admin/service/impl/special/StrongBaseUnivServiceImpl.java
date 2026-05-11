package com.haifeng.admin.service.impl.special;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.special.StrongBaseUnivAddDTO;
import com.haifeng.admin.dto.special.StrongBaseUnivQueryDTO;
import com.haifeng.admin.service.special.StrongBaseUnivService;
import com.haifeng.admin.vo.special.StrongBaseUnivDetailVO;
import com.haifeng.admin.vo.special.StrongBaseUnivListVO;
import com.haifeng.common.entity.special.StrongBaseUniversity;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.special.StrongBaseUniversityMapper;
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
public class StrongBaseUnivServiceImpl implements StrongBaseUnivService {

    private final StrongBaseUniversityMapper strongBaseUniversityMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public IPage<StrongBaseUnivListVO> page(StrongBaseUnivQueryDTO dto) {
        Page<StrongBaseUniversity> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<StrongBaseUniversity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getUniversityName())) {
            wrapper.eq(StrongBaseUniversity::getUniversityName, dto.getUniversityName());
        }
        if (dto.getIsPilot() != null) {
            wrapper.eq(StrongBaseUniversity::getIsPilot, dto.getIsPilot());
        }
        if (dto.getPilotYear() != null) {
            wrapper.eq(StrongBaseUniversity::getPilotYear, dto.getPilotYear());
        }
        if (dto.getTestBeforeScore() != null) {
            wrapper.eq(StrongBaseUniversity::getTestBeforeScore, dto.getTestBeforeScore());
        }
        wrapper.orderByDesc(StrongBaseUniversity::getCreatedAt);
        IPage<StrongBaseUniversity> result = strongBaseUniversityMapper.selectPage(page, wrapper);
        return result.convert(entity -> {
            StrongBaseUnivListVO vo = new StrongBaseUnivListVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        });
    }

    @Override
    public StrongBaseUnivDetailVO detail(Long id) {
        StrongBaseUniversity entity = strongBaseUniversityMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "强基院校配置不存在");
        }
        StrongBaseUnivDetailVO vo = new StrongBaseUnivDetailVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(StrongBaseUnivAddDTO dto) {
        if (strongBaseUniversityMapper.countByUniversityId(dto.getUniversityId()) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该大学的强基配置已存在");
        }
        StrongBaseUniversity entity = new StrongBaseUniversity();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(snowflakeIdGenerator.nextId());
        if (entity.getIsPilot() == null) {
            entity.setIsPilot(true);
        }
        if (entity.getTestBeforeScore() == null) {
            entity.setTestBeforeScore(false);
        }
        if (entity.getDefaultEntryRatio() == null) {
            entity.setDefaultEntryRatio("1:5");
        }
        if (entity.getDefaultAdmissionFormula() == null) {
            entity.setDefaultAdmissionFormula("高考成绩×85%+校测成绩×15%");
        }
        strongBaseUniversityMapper.insert(entity);
        log.info("新增强基院校配置: universityId={}, universityName={}", dto.getUniversityId(), dto.getUniversityName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, StrongBaseUnivAddDTO dto) {
        StrongBaseUniversity entity = strongBaseUniversityMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "强基院校配置不存在");
        }
        if (!entity.getUniversityId().equals(dto.getUniversityId()) && strongBaseUniversityMapper.countByUniversityIdExclude(dto.getUniversityId(), id) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该大学的强基配置已存在");
        }
        BeanUtils.copyProperties(dto, entity);
        strongBaseUniversityMapper.updateById(entity);
        log.info("修改强基院校配置: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (strongBaseUniversityMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "强基院校配置不存在");
        }
        strongBaseUniversityMapper.deleteById(id);
        log.info("删除强基院校配置: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "ID列表不能为空");
        }
        strongBaseUniversityMapper.deleteBatchIds(ids);
        log.info("批量删除强基院校配置: ids={}", ids);
    }
}
