package com.haifeng.app.service.impl.favorite.target;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.service.favorite.FavoriteTargetValidator;
import com.haifeng.common.entity.certificate.Certificate;
import com.haifeng.common.enums.FavoriteTargetType;
import com.haifeng.common.mapper.certificate.CertificateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CertificateFavoriteValidator implements FavoriteTargetValidator {

    private final CertificateMapper certificateMapper;

    @Override
    public FavoriteTargetType type() {
        return FavoriteTargetType.CERTIFICATE;
    }

    @Override
    public boolean exists(Long targetId) {
        Long count = certificateMapper.selectCount(new LambdaQueryWrapper<Certificate>()
                .eq(Certificate::getId, targetId)
                .eq(Certificate::getIsDeleted, false));
        return count != null && count > 0;
    }
}
