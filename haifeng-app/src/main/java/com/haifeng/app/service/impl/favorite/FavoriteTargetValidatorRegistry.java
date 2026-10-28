package com.haifeng.app.service.impl.favorite;

import com.haifeng.app.service.favorite.FavoriteTargetValidator;
import com.haifeng.common.enums.FavoriteTargetType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class FavoriteTargetValidatorRegistry {

    private final Map<FavoriteTargetType, FavoriteTargetValidator> validators;

    public FavoriteTargetValidatorRegistry(List<FavoriteTargetValidator> validatorList) {
        Map<FavoriteTargetType, FavoriteTargetValidator> map = new EnumMap<>(FavoriteTargetType.class);
        for (FavoriteTargetValidator validator : validatorList) {
            map.put(validator.type(), validator);
        }
        this.validators = Map.copyOf(map);
    }

    public boolean exists(FavoriteTargetType type, Long targetId) {
        FavoriteTargetValidator validator = validators.get(type);
        return validator != null && validator.exists(targetId);
    }
}
