package com.haifeng.common.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import com.haifeng.common.enums.MemberType;
import com.haifeng.common.enums.OrderType;
import com.haifeng.common.handler.AESEncryptTypeHandler;
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
@TableName(value = "member_orders", autoResultMap = true)
public class MemberOrder {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String orderNo;

    private Long memberId;

    private String memberName;

    private String phone;

    @TableField(typeHandler = AESEncryptTypeHandler.class)
    private String wechatId;

    private String wechatIdIndex;

    private OrderType orderType;

    private MemberType beforeType;

    private MemberType afterType;

    private Integer durationMonths;

    private BigDecimal amount;

    private OffsetDateTime beforeExpireAt;

    private OffsetDateTime afterExpireAt;

    private Long operatorId;

    private String operatorName;

    private String remark;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    @Version
    private Integer version;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
