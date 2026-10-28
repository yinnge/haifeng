package com.haifeng.app.service.favorite;

import com.haifeng.common.enums.FavoriteTargetType;

/**
 * 收藏目标合法性校验器。
 * 新增收藏模块时，只需新增一个实现 Bean，无需改动收藏主流程。
 */
public interface FavoriteTargetValidator {

    FavoriteTargetType type();

    boolean exists(Long targetId);
}
