package com.haifeng.common.dto.user;

import lombok.Data;

/**
 * 会员收藏类型统计查询结果
 */
@Data
public class FavoriteTypeCountDTO {

    private String targetType;

    private Long cnt;
}
