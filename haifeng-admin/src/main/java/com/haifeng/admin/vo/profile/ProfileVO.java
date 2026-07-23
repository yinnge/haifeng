package com.haifeng.admin.vo.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileVO {

    private Long id;

    private String username;

    private String realName;

    private String phone;

    private String email;

    private String avatar;

    private String roleName;

    private List<String> moduleCodes;

    private Boolean isTotpEnabled;

    private OffsetDateTime lastLoginAt;

    private OffsetDateTime createdAt;
}
