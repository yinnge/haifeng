package com.haifeng.app.service.impl.favorite.target;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.service.favorite.FavoriteTargetValidator;
import com.haifeng.common.entity.major.Major;
import com.haifeng.common.enums.FavoriteTargetType;
import com.haifeng.common.mapper.major.MajorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MajorFavoriteValidator implements FavoriteTargetValidator {

    private static final short STATUS_PUBLISHED = 1;

    private final MajorMapper majorMapper;

    @Override
    public FavoriteTargetType type() {
        return FavoriteTargetType.MAJOR;
    }

    @Override
    public boolean exists(Long targetId) {
        Long count = majorMapper.selectCount(new LambdaQueryWrapper<Major>()
                .eq(Major::getId, targetId)
                .eq(Major::getStatus, STATUS_PUBLISHED));
        return count != null && count > 0;
    }
}
