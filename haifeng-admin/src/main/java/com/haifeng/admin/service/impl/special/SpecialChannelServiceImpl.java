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
import com.haifeng.common.entity.special.SpecialChannelUniversity;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.special.SpecialChannelMapper;
import com.haifeng.common.mapper.special.SpecialChannelUniversityMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpecialChannelServiceImpl implements SpecialChannelService {

    private final SpecialChannelMapper specialChannelMapper;
    private final SpecialChannelUniversityMapper specialChannelUniversityMapper;

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
        return result.convert(entity -> SpecialChannelListVO.builder()
                .id(entity.getId())
                .channelCode(entity.getChannelCode())
                .channelName(entity.getChannelName())
                .displayType(entity.getDisplayType())
                .isActive(entity.getIsActive())
                .build());
    }

    @Override
    public SpecialChannelDetailVO detail(Long id) {
        SpecialChannel entity = specialChannelMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "招生通道不存在");
        }
        return SpecialChannelDetailVO.builder()
                .id(entity.getId())
                .channelCode(entity.getChannelCode())
                .channelName(entity.getChannelName())
                .subtitle(entity.getSubtitle())
                .parentCode(entity.getParentCode())
                .filterLabel(entity.getFilterLabel())
                .displayType(entity.getDisplayType())
                .content(entity.getContent())
                .sortOrder(entity.getSortOrder())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SpecialChannelAddDTO dto) {
        if (specialChannelMapper.countByCode(dto.getChannelCode()) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "通道代码已存在");
        }
        SpecialChannel entity = SpecialChannel.builder()
                .id(SnowflakeIdGenerator.nextId())
                .channelCode(dto.getChannelCode())
                .channelName(dto.getChannelName())
                .subtitle(dto.getSubtitle())
                .parentCode(dto.getParentCode())
                .filterLabel(dto.getFilterLabel())
                .displayType(dto.getDisplayType())
                .content(dto.getContent())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .isActive(true)
                .build();
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
        entity.setChannelCode(dto.getChannelCode());
        entity.setChannelName(dto.getChannelName());
        entity.setSubtitle(dto.getSubtitle());
        entity.setParentCode(dto.getParentCode());
        entity.setFilterLabel(dto.getFilterLabel());
        entity.setDisplayType(dto.getDisplayType());
        entity.setContent(dto.getContent());
        if (dto.getSortOrder() != null) {
            entity.setSortOrder(dto.getSortOrder());
        }
        entity.setUpdatedAt(OffsetDateTime.now());
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
        SpecialChannel entity = specialChannelMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "招生通道不存在");
        }
        Long relatedCount = specialChannelUniversityMapper.selectCount(
                new LambdaQueryWrapper<SpecialChannelUniversity>()
                        .eq(SpecialChannelUniversity::getChannelCode, entity.getChannelCode())
        );
        if (relatedCount > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该通道下仍有" + relatedCount + "条大学关联，请先删除");
        }
        specialChannelMapper.deleteById(id);
        log.info("删除招生通道: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        List<SpecialChannel> channels = specialChannelMapper.selectBatchIds(ids);
        if (channels.size() != ids.size()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "部分招生通道不存在");
        }
        List<String> codes = channels.stream().map(SpecialChannel::getChannelCode).toList();
        Long relatedCount = specialChannelUniversityMapper.selectCount(
                new LambdaQueryWrapper<SpecialChannelUniversity>()
                        .in(SpecialChannelUniversity::getChannelCode, codes)
        );
        if (relatedCount > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "部分通道下仍有大学关联，请先删除");
        }
        specialChannelMapper.deleteBatchIds(ids);
        log.info("批量删除招生通道: count={}", ids.size());
    }
}
