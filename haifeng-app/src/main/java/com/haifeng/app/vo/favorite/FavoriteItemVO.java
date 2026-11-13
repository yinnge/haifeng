package com.haifeng.app.vo.favorite;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String targetType;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetId;

    private OffsetDateTime favoritedAt;
}
