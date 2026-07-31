package com.haifeng.admin.vo.user;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class CommissionListVO {

    @JsonSerialize(using = ToStringSerializer.class)
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

    private Boolean deleted;
}
