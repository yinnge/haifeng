package com.haifeng.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 会员实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_member")
public class Member {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（BCrypt加密）
     */
    private String password;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邀请码（唯一，自动生成）
     */
    private String inviteCode;

    /**
     * 会员类型: normal-普通, vip-VIP
     */
    private String memberType;

    /**
     * 会员到期时间
     */
    private OffsetDateTime expireAt;

    /**
     * 推荐人ID
     */
    private Long referrerId;

    /**
     * 推荐人用户名（冗余）
     */
    private String referrerUsername;

    /**
     * 可提现佣金余额
     */
    private BigDecimal commissionBalance;

    /**
     * 累计获得佣金
     */
    private BigDecimal commissionTotalEarned;

    /**
     * 累计已发放佣金
     */
    private BigDecimal commissionTotalPaid;

    /**
     * 账号状态: active-正常, disabled-禁用
     */
    private String status;

    /**
     * 最后登录时间
     */
    private OffsetDateTime lastLoginAt;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 是否删除
     */
    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    /**
     * 判断是否为VIP（且未过期）
     */
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
     * 获取实际的会员类型（考虑VIP过期）
     */
    public String getEffectiveMemberType() {
        if (isVipActive()) {
            return "vip";
        }
        return "normal";
    }

    /**
     * 判断账号是否激活
     */
    public boolean isActive() {
        return "active".equals(status);
    }
}
