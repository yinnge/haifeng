package com.haifeng.common.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import com.haifeng.common.handler.AESEncryptTypeHandler;
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
@TableName(value = "t_member", autoResultMap = true)
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

    @TableField(typeHandler = AESEncryptTypeHandler.class)
    private String wechatId;

    private String wechatIdIndex;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    @Version
    private Integer version;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    /**
     * 被挂起的会员类型（如Pro升级VIP时暂存pro）
     */
    private String suspendedMemberType;

    /**
     * 被挂起会员的到期时间
     */
    private OffsetDateTime suspendedExpireAt;

    /**
     * 被挂起会员的剩余月数
     */
    private Integer suspendedRemainingMonths;

    /**
     * Token 版本号（升级/撤销时递增，使旧 Token 失效）
     */
    private Integer tokenVersion;

    public boolean isVipActive() {
        if (!"vip".equals(memberType)) {
            return false;
        }
        if (expireAt == null) {
            return false;
        }
        return expireAt.isAfter(OffsetDateTime.now());
    }

    /**
     * 获取有效的会员类型。
     * VIP过期后，如果存在挂起的Pro，自动恢复Pro。
     */
    public String getEffectiveMemberType() {
        if (isVipActive()) {
            return "vip";
        }
        // VIP已过期，检查是否有挂起的Pro需要恢复
        if ("vip".equals(memberType) && suspendedMemberType != null && suspendedRemainingMonths != null) {
            return suspendedMemberType;
        }
        if ("pro".equals(memberType)) {
            return "pro";
        }
        return "normal";
    }

    /**
     * VIP过期后是否需要恢复挂起的Pro
     */
    public boolean needsSuspendedRestore() {
        return "vip".equals(memberType)
                && suspendedMemberType != null
                && suspendedRemainingMonths != null
                && (expireAt == null || expireAt.isBefore(OffsetDateTime.now()));
    }

    public boolean isActive() {
        return "active".equals(status);
    }
}
