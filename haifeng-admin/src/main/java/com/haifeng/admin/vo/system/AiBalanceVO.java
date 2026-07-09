package com.haifeng.admin.vo.system;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * AI 厂商余额信息
 */
@Data
public class AiBalanceVO {

    private String providerName;

    private List<String> models;

    private Boolean isAvailable;

    private String currency;

    private BigDecimal totalBalance;

    private BigDecimal grantedBalance;

    private BigDecimal toppedUpBalance;
}
