package com.haifeng.admin.vo.user;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class OrderListVO {

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

    private OffsetDateTime createdAt;
}
