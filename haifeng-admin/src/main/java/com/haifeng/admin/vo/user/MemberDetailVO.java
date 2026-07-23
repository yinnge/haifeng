package com.haifeng.admin.vo.user;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class MemberDetailVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String username;

    private String avatar;

    private String phone;

    private String inviteCode;

    private String memberType;

    private OffsetDateTime expireAt;

    /**
     * 微信号（脱敏后）
     */
    private String wechatId;

    private Long referrerId;

    private String referrerUsername;

    private BigDecimal commissionBalance;

    private BigDecimal commissionTotalEarned;

    private BigDecimal commissionTotalPaid;

    private String status;

    private OffsetDateTime lastLoginAt;

    private String lastLoginIp;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
