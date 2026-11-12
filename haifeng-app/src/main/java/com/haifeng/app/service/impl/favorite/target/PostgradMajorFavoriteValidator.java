package com.haifeng.app.service.impl.favorite.target;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.service.favorite.FavoriteTargetValidator;
import com.haifeng.common.entity.major.PostgradMajor;
import com.haifeng.common.enums.FavoriteTargetType;
import com.haifeng.common.mapper.major.PostgradMajorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 考研专业收藏校验器。
 * <p>
 * 考研专业是独立表 t_postgrad_major，ID 空间与本科专业 t_major 不重叠，
 * 因此单独使用 FavoriteTargetType.POSTGRAD_MAJOR，不可复用 MAJOR。
 */
@Component
@RequiredArgsConstructor
public class PostgradMajorFavoriteValidator implements FavoriteTargetValidator {

    private static final short STATUS_PUBLISHED = 1;

    private final PostgradMajorMapper postgradMajorMapper;

    @Override
    public FavoriteTargetType type() {
        return FavoriteTargetType.POSTGRAD_MAJOR;
    }

    @Override
    public boolean exists(Long targetId) {
        Long count = postgradMajorMapper.selectCount(new LambdaQueryWrapper<PostgradMajor>()
                .eq(PostgradMajor::getId, targetId)
                .eq(PostgradMajor::getStatus, STATUS_PUBLISHED));
        return count != null && count > 0;
    }
}
