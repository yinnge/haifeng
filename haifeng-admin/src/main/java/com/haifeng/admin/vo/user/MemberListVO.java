package com.haifeng.admin.vo.user;

import lombok.Data;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class MemberListVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String username;

    private String phone;

    private String memberType;

    /**
     * 微信号（脱敏后）
     */
    private String wechatId;

    private String status;

    private OffsetDateTime lastLoginAt;

    private String lastLoginIp;

    private OffsetDateTime createdAt;
}
