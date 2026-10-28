package com.haifeng.app.service.impl.favorite.target;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.service.favorite.FavoriteTargetValidator;
import com.haifeng.common.entity.city.City;
import com.haifeng.common.enums.FavoriteTargetType;
import com.haifeng.common.mapper.city.CityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CityFavoriteValidator implements FavoriteTargetValidator {

    private final CityMapper cityMapper;

    @Override
    public FavoriteTargetType type() {
        return FavoriteTargetType.CITY;
    }

    @Override
    public boolean exists(Long targetId) {
        Long count = cityMapper.selectCount(new LambdaQueryWrapper<City>()
                .eq(City::getId, targetId)
                .eq(City::getIsDeleted, false));
        return count != null && count > 0;
    }
}
