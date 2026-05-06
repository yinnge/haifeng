package com.haifeng.admin.vo.user;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class MemberListVO {

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
