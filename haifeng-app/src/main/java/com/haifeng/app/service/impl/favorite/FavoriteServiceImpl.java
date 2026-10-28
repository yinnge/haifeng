package com.haifeng.app.service.impl.favorite;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.favorite.FavoriteAddDTO;
import com.haifeng.app.dto.favorite.FavoriteQueryDTO;
import com.haifeng.app.dto.favorite.FavoriteStatusQueryDTO;
import com.haifeng.app.service.favorite.FavoriteService;
import com.haifeng.app.vo.favorite.FavoriteCountItemVO;
import com.haifeng.app.vo.favorite.FavoriteCountsVO;
import com.haifeng.app.vo.favorite.FavoriteItemVO;
import com.haifeng.common.dto.user.FavoriteTypeCountDTO;
import com.haifeng.common.entity.user.MemberFavorite;
import com.haifeng.common.enums.FavoriteTargetType;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.user.MemberFavoriteMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.SecurityUtil;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final MemberFavoriteMapper memberFavoriteMapper;
    private final FavoriteTargetValidatorRegistry validatorRegistry;

    @Override
    public void add(FavoriteAddDTO dto) {
        Long memberId = requireMemberId();
        FavoriteTargetType targetType = parseType(dto.getTargetType());
        if (dto.getTargetId() == null || dto.getTargetId() < 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "收藏目标ID必须大于0");
        }

        if (!validatorRegistry.exists(targetType, dto.getTargetId())) {
            log.warn("收藏目标不存在或已下架: memberId={}, targetType={}, targetId={}",
                    memberId, targetType.getValue(), dto.getTargetId());
            throw new BusinessException(ResultCode.NOT_FOUND, "收藏目标不存在或已下架");
        }

        memberFavoriteMapper.insertIgnoreConflict(
                SnowflakeIdGenerator.nextId(),
                memberId,
                targetType.getValue(),
                dto.getTargetId());
        log.info("会员添加收藏: memberId={}, targetType={}, targetId={}",
                memberId, targetType.getValue(), dto.getTargetId());
    }

    @Override
    public void remove(String targetType, Long targetId) {
        Long memberId = requireMemberId();
        FavoriteTargetType type = parseType(targetType);
        if (targetId == null || targetId < 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "收藏目标ID必须大于0");
        }

        int deleted = memberFavoriteMapper.delete(new LambdaQueryWrapper<MemberFavorite>()
                .eq(MemberFavorite::getMemberId, memberId)
                .eq(MemberFavorite::getTargetType, type)
                .eq(MemberFavorite::getTargetId, targetId));
        log.info("会员取消收藏: memberId={}, targetType={}, targetId={}, deleted={}",
                memberId, type.getValue(), targetId, deleted);
    }

    @Override
    public IPage<FavoriteItemVO> page(FavoriteQueryDTO dto) {
        Long memberId = requireMemberId();
        FavoriteTargetType targetType = StringUtils.hasText(dto.getTargetType())
                ? parseType(dto.getTargetType())
                : null;

        Page<MemberFavorite> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<MemberFavorite> wrapper = new LambdaQueryWrapper<MemberFavorite>()
                .eq(MemberFavorite::getMemberId, memberId)
                .eq(targetType != null, MemberFavorite::getTargetType, targetType)
                .orderByDesc(MemberFavorite::getCreatedAt)
                .orderByDesc(MemberFavorite::getId);

        return memberFavoriteMapper.selectPage(page, wrapper).convert(this::toItemVO);
    }

    @Override
    public FavoriteCountsVO counts() {
        Long memberId = requireMemberId();

        Map<FavoriteTargetType, Long> countMap = new EnumMap<>(FavoriteTargetType.class);
        for (FavoriteTypeCountDTO row : memberFavoriteMapper.countByType(memberId)) {
            FavoriteTargetType type = FavoriteTargetType.fromValue(row.getTargetType());
            if (type != null) {
                countMap.merge(type, row.getCnt() == null ? 0L : row.getCnt(), Long::sum);
            }
        }

        List<FavoriteCountItemVO> items = new ArrayList<>(FavoriteTargetType.values().length);
        for (FavoriteTargetType type : FavoriteTargetType.values()) {
            items.add(FavoriteCountItemVO.builder()
                    .targetType(type.getValue())
                    .label(type.getDesc())
                    .count(countMap.getOrDefault(type, 0L))
                    .build());
        }

        long total = countMap.values().stream().mapToLong(Long::longValue).sum();
        return FavoriteCountsVO.builder()
                .total(total)
                .items(items)
                .build();
    }

    @Override
    public List<Long> favoritedIds(FavoriteStatusQueryDTO dto) {
        Long memberId = requireMemberId();
        FavoriteTargetType type = parseType(dto.getTargetType());
        if (dto.getTargetIds() == null || dto.getTargetIds().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "目标ID列表不能为空");
        }

        return memberFavoriteMapper.selectList(new LambdaQueryWrapper<MemberFavorite>()
                        .select(MemberFavorite::getTargetId)
                        .eq(MemberFavorite::getMemberId, memberId)
                        .eq(MemberFavorite::getTargetType, type)
                        .in(MemberFavorite::getTargetId, dto.getTargetIds()))
                .stream()
                .map(MemberFavorite::getTargetId)
                .collect(Collectors.toList());
    }

    private FavoriteItemVO toItemVO(MemberFavorite entity) {
        return FavoriteItemVO.builder()
                .id(entity.getId())
                .targetType(entity.getTargetType().getValue())
                .targetId(entity.getTargetId())
                .favoritedAt(entity.getCreatedAt())
                .build();
    }

    private FavoriteTargetType parseType(String targetType) {
        FavoriteTargetType type = FavoriteTargetType.fromValue(targetType);
        if (type == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不支持的收藏类型: " + targetType);
        }
        return type;
    }

    private Long requireMemberId() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        if (memberId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return memberId;
    }
}
