package com.haifeng.common.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_member")
public class Member {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    private String password;

    private String avatar;

    private String phone;

    private String inviteCode;

    private String memberType;

    private OffsetDateTime expireAt;

    private Long referrerId;

    private String referrerUsername;

    private BigDecimal commissionBalance;

    private BigDecimal commissionTotalEarned;

    private BigDecimal commissionTotalPaid;

    private String status;

    private OffsetDateTime lastLoginAt;

    private String lastLoginIp;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    public boolean isVipActive() {
        if (!"vip".equals(memberType)) {
            return false;
        }
        if (expireAt == null) {
            return false;
        }
        return expireAt.isAfter(OffsetDateTime.now());
    }

    public String getEffectiveMemberType() {
        if (isVipActive()) {
            return "vip";
        }
        return "normal";
    }

    public boolean isActive() {
        return "active".equals(status);
    }
}
