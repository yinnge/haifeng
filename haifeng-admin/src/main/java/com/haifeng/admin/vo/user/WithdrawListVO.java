package com.haifeng.admin.vo.user;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class WithdrawListVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private Long memberId;

    private String memberName;

    private String phone;

    private String wechatId;

    private BigDecimal amount;

    private String status;

    private String operatorName;

    private String remark;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
