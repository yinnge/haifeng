package com.haifeng.admin.vo.user;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class OrderListVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String orderNo;

    private String memberName;

    private String phone;

    private String wechatId;

    private String orderType;

    private String beforeType;

    private String afterType;

    private Integer durationMonths;

    private BigDecimal amount;

    private String status;

    private String paymentMethod;

    private OffsetDateTime createdAt;
}
