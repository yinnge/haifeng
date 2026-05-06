package com.haifeng.common.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import com.haifeng.common.enums.WithdrawStatus;
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
@TableName(value = "t_withdraw_record", autoResultMap = true)
public class WithdrawRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long memberId;

    private String memberName;

    private String phone;

    @TableField(typeHandler = AESEncryptTypeHandler.class)
    private String wechatId;

    private String wechatIdIndex;

    private BigDecimal amount;

    private WithdrawStatus status;

    private Long operatorId;

    private String operatorName;

    private String remark;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
