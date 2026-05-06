package com.haifeng.admin.vo.user;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class CommissionListVO {

    private Long id;

    private String referrerName;

    private String referrerPhone;

    private String refereeName;

    private String refereePhone;

    private Long orderId;

    private BigDecimal orderAmount;

    private BigDecimal commissionRate;

    private BigDecimal commissionAmount;

    private OffsetDateTime createdAt;
}
