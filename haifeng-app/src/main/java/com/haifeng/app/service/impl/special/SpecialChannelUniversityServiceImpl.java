package com.haifeng.app.service.impl.special;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.special.SpecialChannelUnivQueryDTO;
import com.haifeng.app.service.special.SpecialChannelUniversityService;
import com.haifeng.app.vo.special.SpecialChannelUnivDetailVO;
import com.haifeng.app.vo.special.SpecialChannelUnivListVO;
import com.haifeng.common.entity.special.SpecialChannelUniversity;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.special.SpecialChannelUniversityMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpecialChannelUniversityServiceImpl implements SpecialChannelUniversityService {

    private final SpecialChannelUniversityMapper specialChannelUniversityMapper;

    @Override
    public IPage<SpecialChannelUnivListVO> page(SpecialChannelUnivQueryDTO dto) {
        if (!ProvinceEnum.isValid(dto.getRegionTag())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "省份参数不合法");
        }
        Page<SpecialChannelUniversity> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<SpecialChannelUniversity> wrapper = new LambdaQueryWrapper<SpecialChannelUniversity>()
                .eq(SpecialChannelUniversity::getIsActive, true)
                .eq(SpecialChannelUniversity::getChannelCode, dto.getChannelCode())
                .like(StringUtils.hasText(dto.getChannelName()), SpecialChannelUniversity::getChannelName, dto.getChannelName())
                .eq(StringUtils.hasText(dto.getRegionTag()), SpecialChannelUniversity::getRegionTag, dto.getRegionTag())
                .ge(dto.getSignupStart() != null, SpecialChannelUniversity::getSignupStart, dto.getSignupStart())
                .le(dto.getSignupEnd() != null, SpecialChannelUniversity::getSignupEnd, dto.getSignupEnd())
                .orderByAsc(SpecialChannelUniversity::getSortOrder)
                .orderByDesc(SpecialChannelUniversity::getId);
        return specialChannelUniversityMapper.selectPage(page, wrapper).convert(this::toListVO);
    }

    @Override
    public SpecialChannelUnivDetailVO detail(Long id) {
        SpecialChannelUniversity entity = specialChannelUniversityMapper.selectById(id);
        if (entity == null || !Boolean.TRUE.equals(entity.getIsActive())) {
            log.debug("通道-大学关联不存在或已禁用, id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND, "通道-大学关联不存在");
        }
        return SpecialChannelUnivDetailVO.builder()
                .id(entity.getId())
                .channelCode(entity.getChannelCode())
                .channelName(entity.getChannelName())
                .universityId(entity.getUniversityId())
                .universityName(entity.getUniversityName())
                .year(entity.getYear())
                .regionTag(entity.getRegionTag())
                .signupStart(entity.getSignupStart())
                .signupEnd(entity.getSignupEnd())
                .officialUrl(entity.getOfficialUrl())
                .brochureTitle(entity.getBrochureTitle())
                .brochureContent(entity.getBrochureContent())
                .build();
    }

    private SpecialChannelUnivListVO toListVO(SpecialChannelUniversity e) {
        return SpecialChannelUnivListVO.builder()
                .universityId(e.getUniversityId())
                .universityName(e.getUniversityName())
                .year(e.getYear())
                .regionTag(e.getRegionTag())
                .signupStart(e.getSignupStart())
                .signupEnd(e.getSignupEnd())
                .build();
    }
}
