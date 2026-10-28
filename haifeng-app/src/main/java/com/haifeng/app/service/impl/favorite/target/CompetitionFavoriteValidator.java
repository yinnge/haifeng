package com.haifeng.app.service.impl.favorite.target;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.service.favorite.FavoriteTargetValidator;
import com.haifeng.common.entity.certificate.Competition;
import com.haifeng.common.enums.FavoriteTargetType;
import com.haifeng.common.mapper.certificate.CompetitionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompetitionFavoriteValidator implements FavoriteTargetValidator {

    private final CompetitionMapper competitionMapper;

    @Override
    public FavoriteTargetType type() {
        return FavoriteTargetType.COMPETITION;
    }

    @Override
    public boolean exists(Long targetId) {
        Long count = competitionMapper.selectCount(new LambdaQueryWrapper<Competition>()
                .eq(Competition::getId, targetId)
                .eq(Competition::getIsDeleted, false));
        return count != null && count > 0;
    }
}
