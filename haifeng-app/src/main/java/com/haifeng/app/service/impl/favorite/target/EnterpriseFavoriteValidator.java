package com.haifeng.app.service.impl.favorite.target;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.service.favorite.FavoriteTargetValidator;
import com.haifeng.common.entity.company.Enterprise;
import com.haifeng.common.enums.FavoriteTargetType;
import com.haifeng.common.mapper.company.EnterpriseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnterpriseFavoriteValidator implements FavoriteTargetValidator {

    private final EnterpriseMapper enterpriseMapper;

    @Override
    public FavoriteTargetType type() {
        return FavoriteTargetType.ENTERPRISE;
    }

    @Override
    public boolean exists(Long targetId) {
        Long count = enterpriseMapper.selectCount(new LambdaQueryWrapper<Enterprise>()
                .eq(Enterprise::getId, targetId)
                .eq(Enterprise::getIsDeleted, false));
        return count != null && count > 0;
    }
}
