package com.haifeng.admin.vo.user;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderDetailVO extends OrderListVO {

    private Long memberId;

    private OffsetDateTime beforeExpireAt;

    private OffsetDateTime afterExpireAt;

    private Long operatorId;

    private String operatorName;

    private String remark;
}
