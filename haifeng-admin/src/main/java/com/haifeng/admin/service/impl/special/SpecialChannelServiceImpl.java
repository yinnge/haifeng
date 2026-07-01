package com.haifeng.admin.service.impl.special;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.special.SpecialChannelAddDTO;
import com.haifeng.admin.dto.special.SpecialChannelQueryDTO;
import com.haifeng.admin.service.special.SpecialChannelService;
import com.haifeng.admin.vo.special.SpecialChannelDetailVO;
import com.haifeng.admin.vo.special.SpecialChannelListVO;
import com.haifeng.common.entity.special.SpecialChannel;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.special.SpecialChannelMapper;
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
public class SpecialChannelServiceImpl implements SpecialChannelService {

    private final SpecialChannelMapper specialChannelMapper;

    @Override
    public IPage<SpecialChannelListVO> page(SpecialChannelQueryDTO dto) {
        Page<SpecialChannel> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<SpecialChannel> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getDisplayType())) {
            wrapper.eq(SpecialChannel::getDisplayType, dto.getDisplayType());
        }
        if (StringUtils.hasText(dto.getChannelName())) {
            wrapper.like(SpecialChannel::getChannelName, dto.getChannelName());
        }
        wrapper.orderByAsc(SpecialChannel::getSortOrder).orderByDesc(SpecialChannel::getCreatedAt);
        IPage<SpecialChannel> result = specialChannelMapper.selectPage(page, wrapper);
        return result.convert(entity -> {
            SpecialChannelListVO vo = new SpecialChannelListVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        });
    }

    @Override
    public SpecialChannelDetailVO detail(Long id) {
        SpecialChannel entity = specialChannelMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "招生通道不存在");
        }
        SpecialChannelDetailVO vo = new SpecialChannelDetailVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SpecialChannelAddDTO dto) {
        if (specialChannelMapper.countByCode(dto.getChannelCode()) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "通道代码已存在");
        }
        SpecialChannel entity = new SpecialChannel();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(SnowflakeIdGenerator.nextId());
        entity.setIsActive(true);
        entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        specialChannelMapper.insert(entity);
        log.info("新增招生通道: code={}, name={}", dto.getChannelCode(), dto.getChannelName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, SpecialChannelAddDTO dto) {
        SpecialChannel entity = specialChannelMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "招生通道不存在");
        }
        if (!entity.getChannelCode().equals(dto.getChannelCode()) && specialChannelMapper.countByCodeExclude(dto.getChannelCode(), id) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "通道代码已存在");
        }
        BeanUtils.copyProperties(dto, entity);
        specialChannelMapper.updateById(entity);
        log.info("修改招生通道: id={}, code={}", id, dto.getChannelCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleActive(Long id) {
        SpecialChannel entity = specialChannelMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "招生通道不存在");
        }
        entity.setIsActive(!entity.getIsActive());
        specialChannelMapper.updateById(entity);
        log.info("切换招生通道状态: id={}, isActive={}", id, entity.getIsActive());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (specialChannelMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "招生通道不存在");
        }
        specialChannelMapper.deleteById(id);
        log.info("删除招生通道: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "ID列表不能为空");
        }
        specialChannelMapper.deleteBatchIds(ids);
        log.info("批量删除招生通道: ids={}", ids);
    }
}
