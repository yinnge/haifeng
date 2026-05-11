package com.haifeng.admin.service.impl.special;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.special.SpecialChannelUnivAddDTO;
import com.haifeng.admin.dto.special.SpecialChannelUnivQueryDTO;
import com.haifeng.admin.service.special.SpecialChannelUnivService;
import com.haifeng.admin.vo.special.SpecialChannelUnivDetailVO;
import com.haifeng.admin.vo.special.SpecialChannelUnivListVO;
import com.haifeng.common.entity.special.SpecialChannelUniversity;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.special.SpecialChannelUniversityMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpecialChannelUnivServiceImpl implements SpecialChannelUnivService {

    private final SpecialChannelUniversityMapper specialChannelUniversityMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public IPage<SpecialChannelUnivListVO> page(SpecialChannelUnivQueryDTO dto) {
        Page<SpecialChannelUniversity> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<SpecialChannelUniversity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SpecialChannelUniversity::getSortOrder).orderByDesc(SpecialChannelUniversity::getCreatedAt);
        IPage<SpecialChannelUniversity> result = specialChannelUniversityMapper.selectPage(page, wrapper);
        return result.convert(entity -> {
            SpecialChannelUnivListVO vo = new SpecialChannelUnivListVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        });
    }

    @Override
    public SpecialChannelUnivDetailVO detail(Long id) {
        SpecialChannelUniversity entity = specialChannelUniversityMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "通道-大学关联不存在");
        }
        SpecialChannelUnivDetailVO vo = new SpecialChannelUnivDetailVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SpecialChannelUnivAddDTO dto) {
        if (specialChannelUniversityMapper.countByUnique(dto.getChannelCode(), dto.getUniversityId(), dto.getYear()) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该通道下该大学该年份的记录已存在");
        }
        SpecialChannelUniversity entity = new SpecialChannelUniversity();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(snowflakeIdGenerator.nextId());
        entity.setIsActive(true);
        entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        specialChannelUniversityMapper.insert(entity);
        log.info("新增通道-大学关联: channelCode={}, universityId={}, year={}", dto.getChannelCode(), dto.getUniversityId(), dto.getYear());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, SpecialChannelUnivAddDTO dto) {
        SpecialChannelUniversity entity = specialChannelUniversityMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "通道-大学关联不存在");
        }
        boolean keyChanged = !entity.getChannelCode().equals(dto.getChannelCode())
                || !entity.getUniversityId().equals(dto.getUniversityId())
                || (entity.getYear() == null ? dto.getYear() != null : !entity.getYear().equals(dto.getYear()));
        if (keyChanged && specialChannelUniversityMapper.countByUniqueExclude(dto.getChannelCode(), dto.getUniversityId(), dto.getYear(), id) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该通道下该大学该年份的记录已存在");
        }
        BeanUtils.copyProperties(dto, entity);
        specialChannelUniversityMapper.updateById(entity);
        log.info("修改通道-大学关联: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleActive(Long id) {
        SpecialChannelUniversity entity = specialChannelUniversityMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "通道-大学关联不存在");
        }
        entity.setIsActive(!entity.getIsActive());
        specialChannelUniversityMapper.updateById(entity);
        log.info("切换通道-大学关联状态: id={}, isActive={}", id, entity.getIsActive());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (specialChannelUniversityMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "通道-大学关联不存在");
        }
        specialChannelUniversityMapper.deleteById(id);
        log.info("删除通道-大学关联: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "ID列表不能为空");
        }
        specialChannelUniversityMapper.deleteBatchIds(ids);
        log.info("批量删除通道-大学关联: ids={}", ids);
    }
}
