package com.haifeng.common.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.haifeng.common.enums.FavoriteTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_member_favorite")
public class MemberFavorite {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long memberId;

    private FavoriteTargetType targetType;

    private Long targetId;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
