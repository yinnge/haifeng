package com.haifeng.app.service.impl.favorite.target;

import com.haifeng.app.service.favorite.FavoriteTargetValidator;
import com.haifeng.common.enums.FavoriteTargetType;
import com.haifeng.common.mapper.resource.FileInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * t_file_info 收藏目标合法性校验器。
 * 显式判定「存在且未逻辑删除」，不依赖 @TableLogic 注解的自动过滤。
 */
@Component
@RequiredArgsConstructor
public class FileInfoFavoriteValidator implements FavoriteTargetValidator {

    private final FileInfoMapper fileInfoMapper;

    @Override
    public FavoriteTargetType type() {
        return FavoriteTargetType.FILE_INFO;
    }

    @Override
    public boolean exists(Long targetId) {
        return fileInfoMapper.countActiveById(targetId) > 0;
    }
}
