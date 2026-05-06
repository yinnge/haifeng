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
@TableName("t_referral_commission")
public class ReferralCommission {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long referrerId;

    private String referrerName;

    private String referrerPhone;

    private Long refereeId;

    private String refereeName;

    private String refereePhone;

    private Long orderId;

    private BigDecimal orderAmount;

    private BigDecimal commissionRate;

    private BigDecimal commissionAmount;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
