package com.haifeng.app.service.impl.special;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.special.SpecialChannelQueryDTO;
import com.haifeng.app.service.special.SpecialChannelService;
import com.haifeng.app.vo.special.SpecialChannelDetailVO;
import com.haifeng.app.vo.special.SpecialChannelListVO;
import com.haifeng.common.entity.special.SpecialChannel;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.special.SpecialChannelMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpecialChannelServiceImpl implements SpecialChannelService {

    private final SpecialChannelMapper specialChannelMapper;

    @Override
    public IPage<SpecialChannelListVO> page(SpecialChannelQueryDTO dto) {
        Page<SpecialChannel> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<SpecialChannel> wrapper = new LambdaQueryWrapper<SpecialChannel>()
                .eq(SpecialChannel::getIsActive, true)
                .eq(StringUtils.hasText(dto.getDisplayType()), SpecialChannel::getDisplayType, dto.getDisplayType())
                .like(StringUtils.hasText(dto.getChannelName()), SpecialChannel::getChannelName, dto.getChannelName())
                .orderByAsc(SpecialChannel::getSortOrder)
                .orderByDesc(SpecialChannel::getId);
        return specialChannelMapper.selectPage(page, wrapper).convert(this::toListVO);
    }

    @Override
    public SpecialChannelDetailVO detail(Long id) {
        SpecialChannel entity = specialChannelMapper.selectById(id);
        if (entity == null || !Boolean.TRUE.equals(entity.getIsActive())) {
            log.debug("特殊通道不存在或已禁用, id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND, "特殊通道不存在");
        }
        return SpecialChannelDetailVO.builder()
                .id(entity.getId())
                .channelCode(entity.getChannelCode())
                .channelName(entity.getChannelName())
                .subtitle(entity.getSubtitle())
                .filterLabel(entity.getFilterLabel())
                .displayType(entity.getDisplayType())
                .content(entity.getContent())
                .build();
    }

    private SpecialChannelListVO toListVO(SpecialChannel e) {
        return SpecialChannelListVO.builder()
                .id(e.getId())
                .channelCode(e.getChannelCode())
                .channelName(e.getChannelName())
                .subtitle(e.getSubtitle())
                .filterLabel(e.getFilterLabel())
                .displayType(e.getDisplayType())
                .build();
    }
}
