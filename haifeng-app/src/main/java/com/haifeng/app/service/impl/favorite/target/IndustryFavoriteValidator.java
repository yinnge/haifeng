package com.haifeng.app.service.impl.favorite.target;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.service.favorite.FavoriteTargetValidator;
import com.haifeng.common.entity.industry.Industry;
import com.haifeng.common.enums.FavoriteTargetType;
import com.haifeng.common.mapper.industry.IndustryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IndustryFavoriteValidator implements FavoriteTargetValidator {

    private final IndustryMapper industryMapper;

    @Override
    public FavoriteTargetType type() {
        return FavoriteTargetType.INDUSTRY;
    }

    @Override
    public boolean exists(Long targetId) {
        Long count = industryMapper.selectCount(new LambdaQueryWrapper<Industry>()
                .eq(Industry::getId, targetId)
                .eq(Industry::getIsDeleted, false));
        return count != null && count > 0;
    }
}
