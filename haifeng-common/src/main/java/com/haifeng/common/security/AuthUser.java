package com.haifeng.common.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 自定义 UserDetails 实现
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser implements UserDetails {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户类型: admin / member
     */
    private String userType;

    /**
     * 会员类型: normal(普通版) / pro(专业版) / vip(旗舰版)（仅 member 有效）
     */
    private String memberType;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（通常不需要在AuthUser中存储）
     */
    private String password;

    /**
     * 权限集合
     */
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities == null) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * 判断是否为管理员
     */
    public boolean isAdmin() {
        return "admin".equals(userType);
    }

    /**
     * 判断是否为会员
     */
    public boolean isMember() {
        return "member".equals(userType);
    }

    /**
     * 判断是否为VIP会员（旗舰版）
     */
    public boolean isVip() {
        return isMember() && "vip".equals(memberType);
    }

    /**
     * 判断是否为Pro会员（专业版）
     */
    public boolean isPro() {
        return isMember() && "pro".equals(memberType);
    }

    /**
     * 判断是否为Pro或更高级别（专业版及以上）
     * 权限层级: normal < pro < vip
     */
    public boolean isProOrAbove() {
        return isMember() && ("pro".equals(memberType) || "vip".equals(memberType));
    }
}
