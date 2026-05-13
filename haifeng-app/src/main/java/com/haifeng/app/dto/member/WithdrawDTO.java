package com.haifeng.app.dto.member;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawDTO {

    @NotNull(message = "提现金额不能为空")
    private BigDecimal amount;
}
