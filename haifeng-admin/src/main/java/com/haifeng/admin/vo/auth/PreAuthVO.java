package com.haifeng.admin.vo.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreAuthVO {

    /**
     * 预认证令牌
     */
    private String preAuthToken;
}
