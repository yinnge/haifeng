package com.haifeng.app.service.impl.favorite.target;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.service.favorite.FavoriteTargetValidator;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.enums.FavoriteTargetType;
import com.haifeng.common.mapper.university.UniversityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniversityFavoriteValidator implements FavoriteTargetValidator {

    private static final short STATUS_PUBLISHED = 1;

    private final UniversityMapper universityMapper;

    @Override
    public FavoriteTargetType type() {
        return FavoriteTargetType.UNIVERSITY;
    }

    @Override
    public boolean exists(Long targetId) {
        Long count = universityMapper.selectCount(new LambdaQueryWrapper<University>()
                .eq(University::getId, targetId)
                .eq(University::getStatus, STATUS_PUBLISHED));
        return count != null && count > 0;
    }
}
