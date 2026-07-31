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

    /**
     * 挂起的会员类型（如Pro升级VIP时暂存pro，VIP到期后恢复）
     */
    private String suspendedMemberType;

    /**
     * 挂起会员的恢复日期（VIP到期日，恢复后Pro有效期从该日期顺延）
     */
    private OffsetDateTime suspendedExpireAt;
}
