package com.haifeng.app.vo.favorite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * 收藏记录 VO。目标详情由前端按 targetType + targetId 调用对应模块接口获取。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String targetType;

    private Long targetId;

    private OffsetDateTime favoritedAt;
}
