package com.haifeng.admin.vo.home;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class InstitutionListVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String category;

    private String name;

    private String province;

    private String foundedYear;

    private String address;

    private String phone;

    private String email;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 管理账号用户名
     */
    private String managerUsername;

    /**
     * 管理账号手机号（Admin 端完整展示）
     */
    private String managerPhone;

    private Short status;

    private Integer sortOrder;

    private OffsetDateTime createdAt;
}
