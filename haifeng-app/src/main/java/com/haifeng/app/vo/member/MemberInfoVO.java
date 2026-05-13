package com.haifeng.app.vo.member;

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
public class MemberInfoVO {

    private String username;

    private String phone;

    private String avatar;

    private Boolean hasWechat;

    private String inviteCode;

    private BigDecimal commissionBalance;

    private BigDecimal commissionTotalEarned;

    private BigDecimal commissionTotalPaid;

    private String memberType;

    private OffsetDateTime expireAt;
}
