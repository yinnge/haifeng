# 权限管理模块实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 重构项目架构并实现完整的权限管理模块（模块管理、角色管理、管理员管理）

**Architecture:** Entity/Mapper 统一放 haifeng-common，Controller/Service/DTO/VO 按模块分子包。采用 Spring Boot 3.x + MyBatis-Plus + JWT 双Token 认证。

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, PostgreSQL, Redis, BCrypt, JWT

---

## 文件结构

### haifeng-common 新增/修改

```
src/main/java/com/haifeng/common/
├── dto/
│   └── BasePageQueryDTO.java                    # 新增
├── entity/
│   ├── permission/
│   │   ├── SysAdmin.java                        # 迁移+修改
│   │   ├── SysRole.java                         # 迁移+修改
│   │   ├── SysModule.java                       # 迁移+修改
│   │   └── SysRoleModule.java                   # 迁移+修改
│   ├── system/
│   │   └── AdminLog.java                        # 迁移
│   └── user/
│       └── Member.java                          # 迁移+修改
├── mapper/
│   ├── permission/
│   │   ├── SysAdminMapper.java                  # 迁移
│   │   ├── SysRoleMapper.java                   # 新增
│   │   ├── SysModuleMapper.java                 # 新增
│   │   └── SysRoleModuleMapper.java             # 新增
│   └── user/
│       └── MemberMapper.java                    # 迁移
└── enums/
    └── StatusEnum.java                          # 新增
```

### haifeng-admin 新增/修改

```
src/main/java/com/haifeng/admin/
├── controller/
│   ├── auth/
│   │   └── AdminAuthController.java             # 迁移
│   └── permission/
│       ├── ModuleController.java                # 新增
│       ├── RoleController.java                  # 新增
│       └── AdminController.java                 # 新增
├── service/
│   ├── auth/
│   │   └── AdminAuthService.java                # 迁移
│   ├── permission/
│   │   ├── ModuleService.java                   # 新增
│   │   ├── RoleService.java                     # 新增
│   │   └── AdminService.java                    # 新增
│   └── impl/
│       ├── auth/
│       │   └── AdminAuthServiceImpl.java        # 迁移+修改
│       └── permission/
│           ├── ModuleServiceImpl.java           # 新增
│           ├── RoleServiceImpl.java             # 新增
│           └── AdminServiceImpl.java            # 新增
├── dto/
│   ├── auth/
│   │   └── AdminLoginDTO.java                   # 新增
│   └── permission/
│       ├── ModuleAddDTO.java                    # 新增
│       ├── ModuleUpdateDTO.java                 # 新增
│       ├── ModuleQueryDTO.java                  # 新增
│       ├── RoleAddDTO.java                      # 新增
│       ├── RoleUpdateDTO.java                   # 新增
│       ├── RoleQueryDTO.java                    # 新增
│       ├── RoleModuleBindDTO.java               # 新增
│       ├── AdminAddDTO.java                     # 新增
│       ├── AdminUpdateDTO.java                  # 新增
│       └── AdminQueryDTO.java                   # 新增
└── vo/
    └── permission/
        ├── ModuleTreeVO.java                    # 新增
        ├── RoleListVO.java                      # 新增
        ├── RoleDetailVO.java                    # 新增
        ├── AdminListVO.java                     # 新增
        └── AdminDetailVO.java                   # 新增
```

### haifeng-app 修改

```
src/main/java/com/haifeng/app/
├── controller/
│   └── auth/
│       └── AppAuthController.java               # 迁移
├── service/
│   ├── auth/
│   │   └── AppAuthService.java                  # 迁移
│   └── impl/
│       └── auth/
│           └── AppAuthServiceImpl.java          # 迁移+修改
└── dto/
    └── auth/
        └── RegisterDTO.java                     # 迁移+修改
```

---

## 阶段1：架构重构

### Task 1: 创建 BasePageQueryDTO 和 StatusEnum

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/dto/BasePageQueryDTO.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/enums/StatusEnum.java`

- [ ] **Step 1: 创建 BasePageQueryDTO**

```java
package com.haifeng.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class BasePageQueryDTO {

    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Min(value = 10, message = "每页最小10条")
    @Max(value = 1000, message = "每页最大1000条")
    private Integer size = 10;
}
```

- [ ] **Step 2: 创建 StatusEnum**

```java
package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum StatusEnum {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    @EnumValue
    private final Integer value;
    private final String desc;

    StatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/dto/BasePageQueryDTO.java
git add haifeng-common/src/main/java/com/haifeng/common/enums/StatusEnum.java
git commit -m "feat: 添加 BasePageQueryDTO 和 StatusEnum"
```

---

### Task 2: 迁移 Entity 到 haifeng-common

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/permission/SysAdmin.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/permission/SysRole.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/permission/SysModule.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/permission/SysRoleModule.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/user/Member.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/system/AdminLog.java`
- Delete: `haifeng-admin/src/main/java/com/haifeng/admin/entity/` (整个目录)
- Delete: `haifeng-app/src/main/java/com/haifeng/app/entity/` (整个目录)

- [ ] **Step 1: 创建 SysRole Entity**

```java
package com.haifeng.common.entity.permission;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_role")
public class SysRole {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String roleName;

    private String roleCode;

    private String description;

    private Integer status;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 2: 创建 SysModule Entity**

```java
package com.haifeng.common.entity.permission;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_module")
public class SysModule {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String moduleName;

    private String moduleCode;

    private Long parentId;

    private String path;

    private String icon;

    private Integer sortOrder;

    private Integer level;

    private String description;

    private Integer status;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 3: 创建 SysRoleModule Entity**

```java
package com.haifeng.common.entity.permission;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_role_module")
public class SysRoleModule {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long roleId;

    private Long moduleId;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 4: 创建 SysAdmin Entity**

```java
package com.haifeng.common.entity.permission;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_admin")
public class SysAdmin {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    private String password;

    private String realName;

    private String phone;

    private String email;

    private String avatar;

    private Long roleId;

    private String roleName;

    private Integer status;

    private OffsetDateTime lastLoginAt;

    private String lastLoginIp;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 5: 创建 Member Entity**

```java
package com.haifeng.common.entity.user;

import com.baomidou.mybatisplus.annotation.*;
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
@TableName("t_member")
public class Member {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    private String password;

    private String avatar;

    private String phone;

    private String inviteCode;

    private String memberType;

    private OffsetDateTime expireAt;

    private Long referrerId;

    private String referrerUsername;

    private BigDecimal commissionBalance;

    private BigDecimal commissionTotalEarned;

    private BigDecimal commissionTotalPaid;

    private String status;

    private OffsetDateTime lastLoginAt;

    private String lastLoginIp;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    public boolean isVipActive() {
        if (!"vip".equals(memberType)) {
            return false;
        }
        if (expireAt == null) {
            return false;
        }
        return expireAt.isAfter(OffsetDateTime.now());
    }

    public String getEffectiveMemberType() {
        if (isVipActive()) {
            return "vip";
        }
        return "normal";
    }

    public boolean isActive() {
        return "active".equals(status);
    }
}
```

- [ ] **Step 6: 创建 AdminLog Entity**

```java
package com.haifeng.common.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("admin_logs")
public class AdminLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long adminId;

    private String adminName;

    private String operation;

    private String requestPath;

    private String requestMethod;

    private String requestParams;

    private String result;

    private String errorMsg;

    private String ip;

    private OffsetDateTime createdAt;
}
```

- [ ] **Step 7: 删除旧 Entity 目录并提交**

```bash
rm -rf haifeng-admin/src/main/java/com/haifeng/admin/entity
rm -rf haifeng-app/src/main/java/com/haifeng/app/entity
git add -A
git commit -m "refactor: 迁移 Entity 到 haifeng-common"
```

---

### Task 3: 迁移 Mapper 到 haifeng-common

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/permission/SysAdminMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/permission/SysRoleMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/permission/SysModuleMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/permission/SysRoleModuleMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/user/MemberMapper.java`
- Delete: `haifeng-admin/src/main/java/com/haifeng/admin/mapper/`
- Delete: `haifeng-app/src/main/java/com/haifeng/app/mapper/`

- [ ] **Step 1: 创建 SysAdminMapper**

```java
package com.haifeng.common.mapper.permission;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.permission.SysAdmin;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysAdminMapper extends BaseMapper<SysAdmin> {
}
```

- [ ] **Step 2: 创建 SysRoleMapper**

```java
package com.haifeng.common.mapper.permission;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.permission.SysRole;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
}
```

- [ ] **Step 3: 创建 SysModuleMapper**

```java
package com.haifeng.common.mapper.permission;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.permission.SysModule;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysModuleMapper extends BaseMapper<SysModule> {
}
```

- [ ] **Step 4: 创建 SysRoleModuleMapper**

```java
package com.haifeng.common.mapper.permission;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.permission.SysRoleModule;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysRoleModuleMapper extends BaseMapper<SysRoleModule> {
}
```

- [ ] **Step 5: 创建 MemberMapper**

```java
package com.haifeng.common.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.user.Member;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper extends BaseMapper<Member> {
}
```

- [ ] **Step 6: 删除旧 Mapper 并提交**

```bash
rm -rf haifeng-admin/src/main/java/com/haifeng/admin/mapper
rm -rf haifeng-app/src/main/java/com/haifeng/app/mapper
git add -A
git commit -m "refactor: 迁移 Mapper 到 haifeng-common"
```

---

### Task 4: 重组 Admin 模块包结构

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/auth/AdminAuthController.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/auth/AdminAuthService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/auth/AdminAuthServiceImpl.java`
- Delete: 旧的平铺结构文件

- [ ] **Step 1: 创建 auth 子包并移动 AdminAuthController**

```java
package com.haifeng.admin.controller.auth;

import com.haifeng.admin.service.auth.AdminAuthService;
import com.haifeng.common.dto.LoginDTO;
import com.haifeng.common.dto.RefreshTokenDTO;
import com.haifeng.common.response.R;
import com.haifeng.common.vo.TokenVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public R<TokenVO> login(@Valid @RequestBody LoginDTO dto) {
        TokenVO tokenVO = adminAuthService.login(dto);
        return R.ok(tokenVO);
    }

    @PostMapping("/refresh")
    public R<TokenVO> refresh(@Valid @RequestBody RefreshTokenDTO dto) {
        TokenVO tokenVO = adminAuthService.refresh(dto);
        return R.ok(tokenVO);
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        adminAuthService.logout();
        return R.ok();
    }
}
```

- [ ] **Step 2: 创建 AdminAuthService 接口**

```java
package com.haifeng.admin.service.auth;

import com.haifeng.common.dto.LoginDTO;
import com.haifeng.common.dto.RefreshTokenDTO;
import com.haifeng.common.vo.TokenVO;

public interface AdminAuthService {

    TokenVO login(LoginDTO dto);

    TokenVO refresh(RefreshTokenDTO dto);

    void logout();
}
```

- [ ] **Step 3: 创建 AdminAuthServiceImpl**

```java
package com.haifeng.admin.service.impl.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.admin.service.auth.AdminAuthService;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.dto.LoginDTO;
import com.haifeng.common.dto.RefreshTokenDTO;
import com.haifeng.common.entity.permission.SysAdmin;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.permission.SysAdminMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.JwtUtil;
import com.haifeng.common.util.SecurityUtil;
import com.haifeng.common.vo.TokenVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final SysAdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Override
    public TokenVO login(LoginDTO dto) {
        SysAdmin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<SysAdmin>()
                        .eq(SysAdmin::getPhone, dto.getPhone())
                        .eq(SysAdmin::getDeleted, false)
        );

        if (admin == null) {
            log.warn("管理员登录失败，手机号不存在: {}", dto.getPhone());
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (admin.getStatus() != 1) {
            log.warn("管理员账号已禁用: {}", dto.getPhone());
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        if (!passwordEncoder.matches(dto.getPassword(), admin.getPassword())) {
            log.warn("管理员登录失败，密码错误: {}", dto.getPhone());
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        String accessToken = jwtUtil.generateAccessToken(admin.getId(), JwtUtil.USER_TYPE_ADMIN, null);
        String refreshToken = jwtUtil.generateRefreshToken(admin.getId(), JwtUtil.USER_TYPE_ADMIN);

        String redisKey = RedisKeyConstant.getRefreshTokenKey(admin.getId(), JwtUtil.USER_TYPE_ADMIN);
        redisTemplate.opsForValue().set(redisKey, refreshToken,
                jwtUtil.getRefreshTokenExpire(), TimeUnit.SECONDS);

        admin.setLastLoginAt(OffsetDateTime.now());
        admin.setUpdatedAt(OffsetDateTime.now());
        adminMapper.updateById(admin);

        log.info("管理员登录成功: {}", admin.getUsername());

        return TokenVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpire(jwtUtil.getAccessTokenExpire())
                .refreshTokenExpire(jwtUtil.getRefreshTokenExpire())
                .build();
    }

    @Override
    public TokenVO refresh(RefreshTokenDTO dto) {
        String refreshToken = dto.getRefreshToken();

        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            log.warn("RefreshToken无效");
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        String userType = jwtUtil.getUserTypeFromToken(refreshToken);
        if (!JwtUtil.USER_TYPE_ADMIN.equals(userType)) {
            log.warn("RefreshToken用户类型不匹配");
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Long adminId = jwtUtil.getUserIdFromToken(refreshToken);

        String redisKey = RedisKeyConstant.getRefreshTokenKey(adminId, JwtUtil.USER_TYPE_ADMIN);
        String storedToken = redisTemplate.opsForValue().get(redisKey);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            log.warn("RefreshToken已失效或不匹配");
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        SysAdmin admin = adminMapper.selectById(adminId);
        if (admin == null || admin.getDeleted() || admin.getStatus() != 1) {
            log.warn("管理员不存在或已禁用: {}", adminId);
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        String newAccessToken = jwtUtil.generateAccessToken(admin.getId(), JwtUtil.USER_TYPE_ADMIN, null);
        String newRefreshToken = jwtUtil.generateRefreshToken(admin.getId(), JwtUtil.USER_TYPE_ADMIN);

        redisTemplate.opsForValue().set(redisKey, newRefreshToken,
                jwtUtil.getRefreshTokenExpire(), TimeUnit.SECONDS);

        log.info("管理员Token刷新成功: {}", admin.getUsername());

        return TokenVO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .accessTokenExpire(jwtUtil.getAccessTokenExpire())
                .refreshTokenExpire(jwtUtil.getRefreshTokenExpire())
                .build();
    }

    @Override
    public void logout() {
        Long adminId = SecurityUtil.getCurrentAdminId();
        if (adminId == null) {
            return;
        }

        String redisKey = RedisKeyConstant.getRefreshTokenKey(adminId, JwtUtil.USER_TYPE_ADMIN);
        redisTemplate.delete(redisKey);

        log.info("管理员登出成功: {}", adminId);
    }
}
```

- [ ] **Step 4: 删除旧文件并提交**

```bash
rm -f haifeng-admin/src/main/java/com/haifeng/admin/controller/AdminAuthController.java
rm -f haifeng-admin/src/main/java/com/haifeng/admin/service/AdminAuthService.java
rm -f haifeng-admin/src/main/java/com/haifeng/admin/service/impl/AdminAuthServiceImpl.java
git add -A
git commit -m "refactor: 重组 admin 模块包结构"
```

---

### Task 5: 重组 App 模块包结构

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/auth/AppAuthController.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/auth/AppAuthService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/auth/AppAuthServiceImpl.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/auth/RegisterDTO.java`

- [ ] **Step 1: 创建 RegisterDTO（带手机号和邀请码校验）**

```java
package com.haifeng.app.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度必须在2-50之间")
    private String username;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,16}$",
             message = "密码必须是数字+字母，长度6-16位")
    private String password;

    @Size(min = 8, max = 8, message = "邀请码必须是8位")
    private String referrerCode;
}
```

- [ ] **Step 2: 创建 AppAuthController**

```java
package com.haifeng.app.controller.auth;

import com.haifeng.app.dto.auth.RegisterDTO;
import com.haifeng.app.service.auth.AppAuthService;
import com.haifeng.common.dto.LoginDTO;
import com.haifeng.common.dto.RefreshTokenDTO;
import com.haifeng.common.response.R;
import com.haifeng.common.vo.TokenVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/app/auth")
@RequiredArgsConstructor
public class AppAuthController {

    private final AppAuthService appAuthService;

    @PostMapping("/register")
    public R<TokenVO> register(@Valid @RequestBody RegisterDTO dto) {
        TokenVO tokenVO = appAuthService.register(dto);
        return R.ok(tokenVO);
    }

    @PostMapping("/login")
    public R<TokenVO> login(@Valid @RequestBody LoginDTO dto) {
        TokenVO tokenVO = appAuthService.login(dto);
        return R.ok(tokenVO);
    }

    @PostMapping("/refresh")
    public R<TokenVO> refresh(@Valid @RequestBody RefreshTokenDTO dto) {
        TokenVO tokenVO = appAuthService.refresh(dto);
        return R.ok(tokenVO);
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        appAuthService.logout();
        return R.ok();
    }
}
```

- [ ] **Step 3: 创建 AppAuthService 接口**

```java
package com.haifeng.app.service.auth;

import com.haifeng.app.dto.auth.RegisterDTO;
import com.haifeng.common.dto.LoginDTO;
import com.haifeng.common.dto.RefreshTokenDTO;
import com.haifeng.common.vo.TokenVO;

public interface AppAuthService {

    TokenVO register(RegisterDTO dto);

    TokenVO login(LoginDTO dto);

    TokenVO refresh(RefreshTokenDTO dto);

    void logout();
}
```

- [ ] **Step 4: 创建 AppAuthServiceImpl（使用手机号登录）**

```java
package com.haifeng.app.service.impl.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.dto.auth.RegisterDTO;
import com.haifeng.app.service.auth.AppAuthService;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.dto.LoginDTO;
import com.haifeng.common.dto.RefreshTokenDTO;
import com.haifeng.common.entity.user.Member;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.JwtUtil;
import com.haifeng.common.util.SecurityUtil;
import com.haifeng.common.util.SnowflakeIdGenerator;
import com.haifeng.common.vo.TokenVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppAuthServiceImpl implements AppAuthService {

    private static final String STATUS_ACTIVE = "active";

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TokenVO register(RegisterDTO dto) {
        // 检查用户名是否已存在
        Long existCount = memberMapper.selectCount(
                new LambdaQueryWrapper<Member>()
                        .eq(Member::getUsername, dto.getUsername())
                        .eq(Member::getDeleted, false)
        );
        if (existCount > 0) {
            log.warn("注册失败，用户名已存在: {}", dto.getUsername());
            throw new BusinessException(400, "用户名已存在");
        }

        // 检查手机号是否已存在
        existCount = memberMapper.selectCount(
                new LambdaQueryWrapper<Member>()
                        .eq(Member::getPhone, dto.getPhone())
                        .eq(Member::getDeleted, false)
        );
        if (existCount > 0) {
            log.warn("注册失败，手机号已存在: {}", dto.getPhone());
            throw new BusinessException(400, "手机号已存在");
        }

        // 查询推荐人
        Member referrer = null;
        if (StringUtils.hasText(dto.getReferrerCode())) {
            referrer = memberMapper.selectOne(
                    new LambdaQueryWrapper<Member>()
                            .eq(Member::getInviteCode, dto.getReferrerCode())
                            .eq(Member::getDeleted, false)
            );
            if (referrer == null) {
                log.warn("注册失败，邀请码无效: {}", dto.getReferrerCode());
                throw new BusinessException(400, "邀请码无效");
            }
        }

        // 创建会员
        OffsetDateTime now = OffsetDateTime.now();
        Member member = Member.builder()
                .id(SnowflakeIdGenerator.nextId())
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .memberType(JwtUtil.MEMBER_TYPE_NORMAL)
                .status(STATUS_ACTIVE)
                .commissionBalance(BigDecimal.ZERO)
                .commissionTotalEarned(BigDecimal.ZERO)
                .commissionTotalPaid(BigDecimal.ZERO)
                .deleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        if (referrer != null) {
            member.setReferrerId(referrer.getId());
            member.setReferrerUsername(referrer.getUsername());
        }

        memberMapper.insert(member);

        // 重新查询以获取数据库生成的邀请码
        member = memberMapper.selectById(member.getId());

        String accessToken = jwtUtil.generateAccessToken(member.getId(), JwtUtil.USER_TYPE_MEMBER, member.getMemberType());
        String refreshToken = jwtUtil.generateRefreshToken(member.getId(), JwtUtil.USER_TYPE_MEMBER);

        String redisKey = RedisKeyConstant.getRefreshTokenKey(member.getId(), JwtUtil.USER_TYPE_MEMBER);
        redisTemplate.opsForValue().set(redisKey, refreshToken,
                jwtUtil.getRefreshTokenExpire(), TimeUnit.SECONDS);

        log.info("用户注册成功: {}, 邀请码: {}", dto.getUsername(), member.getInviteCode());

        return TokenVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpire(jwtUtil.getAccessTokenExpire())
                .refreshTokenExpire(jwtUtil.getRefreshTokenExpire())
                .build();
    }

    @Override
    public TokenVO login(LoginDTO dto) {
        Member member = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>()
                        .eq(Member::getPhone, dto.getPhone())
                        .eq(Member::getDeleted, false)
        );

        if (member == null) {
            log.warn("用户登录失败，手机号不存在: {}", dto.getPhone());
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (!member.isActive()) {
            log.warn("用户账号已禁用: {}", dto.getPhone());
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            log.warn("用户登录失败，密码错误: {}", dto.getPhone());
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        String effectiveMemberType = member.getEffectiveMemberType();

        String accessToken = jwtUtil.generateAccessToken(member.getId(), JwtUtil.USER_TYPE_MEMBER, effectiveMemberType);
        String refreshToken = jwtUtil.generateRefreshToken(member.getId(), JwtUtil.USER_TYPE_MEMBER);

        String redisKey = RedisKeyConstant.getRefreshTokenKey(member.getId(), JwtUtil.USER_TYPE_MEMBER);
        redisTemplate.opsForValue().set(redisKey, refreshToken,
                jwtUtil.getRefreshTokenExpire(), TimeUnit.SECONDS);

        member.setLastLoginAt(OffsetDateTime.now());
        memberMapper.updateById(member);

        log.info("用户登录成功: {}", member.getUsername());

        return TokenVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpire(jwtUtil.getAccessTokenExpire())
                .refreshTokenExpire(jwtUtil.getRefreshTokenExpire())
                .build();
    }

    @Override
    public TokenVO refresh(RefreshTokenDTO dto) {
        String refreshToken = dto.getRefreshToken();

        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            log.warn("RefreshToken无效");
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        String userType = jwtUtil.getUserTypeFromToken(refreshToken);
        if (!JwtUtil.USER_TYPE_MEMBER.equals(userType)) {
            log.warn("RefreshToken用户类型不匹配");
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Long memberId = jwtUtil.getUserIdFromToken(refreshToken);

        String redisKey = RedisKeyConstant.getRefreshTokenKey(memberId, JwtUtil.USER_TYPE_MEMBER);
        String storedToken = redisTemplate.opsForValue().get(redisKey);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            log.warn("RefreshToken已失效或不匹配");
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Member member = memberMapper.selectById(memberId);
        if (member == null || member.getDeleted() || !member.isActive()) {
            log.warn("用户不存在或已禁用: {}", memberId);
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        String effectiveMemberType = member.getEffectiveMemberType();

        String newAccessToken = jwtUtil.generateAccessToken(member.getId(), JwtUtil.USER_TYPE_MEMBER, effectiveMemberType);
        String newRefreshToken = jwtUtil.generateRefreshToken(member.getId(), JwtUtil.USER_TYPE_MEMBER);

        redisTemplate.opsForValue().set(redisKey, newRefreshToken,
                jwtUtil.getRefreshTokenExpire(), TimeUnit.SECONDS);

        log.info("用户Token刷新成功: {}", member.getUsername());

        return TokenVO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .accessTokenExpire(jwtUtil.getAccessTokenExpire())
                .refreshTokenExpire(jwtUtil.getRefreshTokenExpire())
                .build();
    }

    @Override
    public void logout() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        if (memberId == null) {
            return;
        }

        String redisKey = RedisKeyConstant.getRefreshTokenKey(memberId, JwtUtil.USER_TYPE_MEMBER);
        redisTemplate.delete(redisKey);

        log.info("用户登出成功: {}", memberId);
    }
}
```

- [ ] **Step 5: 删除旧文件并提交**

```bash
rm -f haifeng-app/src/main/java/com/haifeng/app/controller/AppAuthController.java
rm -f haifeng-app/src/main/java/com/haifeng/app/service/AppAuthService.java
rm -f haifeng-app/src/main/java/com/haifeng/app/service/impl/AppAuthServiceImpl.java
rm -f haifeng-app/src/main/java/com/haifeng/app/dto/RegisterDTO.java
git add -A
git commit -m "refactor: 重组 app 模块包结构"
```

---

### Task 6: 修改 LoginDTO 为手机号登录

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/dto/LoginDTO.java`

- [ ] **Step 1: 修改 LoginDTO**

```java
package com.haifeng.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginDTO {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,16}$",
             message = "密码必须是数字+字母，长度6-16位")
    private String password;
}
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/dto/LoginDTO.java
git commit -m "feat: 修改 LoginDTO 为手机号登录"
```

---

## 阶段2：数据库和登录模块

### Task 7: 创建数据库迁移脚本

**Files:**
- Create: `haifeng-admin/src/main/resources/db/migration/V1__create_admin_tables.sql`

- [ ] **Step 1: 创建完整的数据库迁移脚本**

```sql
-- V1__create_admin_tables.sql
-- 海峰未来规划院 - 管理端数据库表

-- 1. 角色表
CREATE TABLE sys_role (
    id              BIGSERIAL PRIMARY KEY,
    role_name       VARCHAR(50) NOT NULL,
    role_code       VARCHAR(50) NOT NULL,
    description     VARCHAR(100),
    status          SMALLINT DEFAULT 1,
    is_deleted      BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE UNIQUE INDEX uk_role_name ON sys_role(role_name) WHERE is_deleted = FALSE;
CREATE UNIQUE INDEX uk_role_code ON sys_role(role_code) WHERE is_deleted = FALSE;
CREATE INDEX idx_role_status ON sys_role(status) WHERE is_deleted = FALSE;

COMMENT ON TABLE sys_role IS '角色表';
COMMENT ON COLUMN sys_role.role_name IS '角色名称';
COMMENT ON COLUMN sys_role.role_code IS '角色编码';
COMMENT ON COLUMN sys_role.status IS '状态: 0-禁用, 1-启用';

-- 2. 模块表（支持父子层级）
CREATE TABLE sys_module (
    id            BIGSERIAL PRIMARY KEY,
    module_name   VARCHAR(50) NOT NULL,
    module_code   VARCHAR(50) NOT NULL UNIQUE,
    parent_id     BIGINT REFERENCES sys_module(id) ON DELETE CASCADE,
    path          VARCHAR(200),
    icon          VARCHAR(50),
    sort_order    INTEGER DEFAULT 0,
    level         SMALLINT NOT NULL,
    description   VARCHAR(255),
    status        SMALLINT DEFAULT 1,
    is_deleted    BOOLEAN DEFAULT FALSE,
    created_at    TIMESTAMPTZ DEFAULT NOW(),
    updated_at    TIMESTAMPTZ DEFAULT NOW()
);

CREATE UNIQUE INDEX uk_module_name ON sys_module(module_name) WHERE is_deleted = FALSE;
CREATE INDEX idx_module_parent ON sys_module(parent_id);
CREATE INDEX idx_module_status ON sys_module(status) WHERE is_deleted = FALSE;

COMMENT ON TABLE sys_module IS '模块表';
COMMENT ON COLUMN sys_module.parent_id IS '父模块ID，NULL表示顶级';
COMMENT ON COLUMN sys_module.level IS '1=父模块 2=子模块';

-- 3. 角色-模块关联表
CREATE TABLE sys_role_module (
    id          BIGSERIAL PRIMARY KEY,
    role_id     BIGINT NOT NULL,
    module_id   BIGINT NOT NULL,
    created_at  TIMESTAMPTZ DEFAULT NOW(),
    updated_at  TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(role_id, module_id)
);

CREATE INDEX idx_role_module_role ON sys_role_module(role_id);
CREATE INDEX idx_role_module_module ON sys_role_module(module_id);

COMMENT ON TABLE sys_role_module IS '角色-模块关联表';

-- 4. 管理员表
CREATE TABLE sys_admin (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    real_name       VARCHAR(50),
    phone           VARCHAR(20) NOT NULL UNIQUE,
    email           VARCHAR(100),
    avatar          VARCHAR(500),
    role_id         BIGINT NOT NULL,
    role_name       VARCHAR(50),
    status          SMALLINT DEFAULT 1,
    last_login_at   TIMESTAMPTZ,
    last_login_ip   VARCHAR(50),
    is_deleted      BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_admin_role ON sys_admin(role_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_admin_status ON sys_admin(status) WHERE is_deleted = FALSE;

COMMENT ON TABLE sys_admin IS '管理员表';
COMMENT ON COLUMN sys_admin.phone IS '手机号（用于登录）';
COMMENT ON COLUMN sys_admin.status IS '状态: 0-禁用, 1-启用';

-- 5. 操作日志表
CREATE TABLE admin_logs (
    id              BIGSERIAL PRIMARY KEY,
    admin_id        BIGINT NOT NULL,
    admin_name      VARCHAR(50),
    operation       VARCHAR(100) NOT NULL,
    request_path    VARCHAR(200),
    request_method  VARCHAR(10),
    request_params  TEXT,
    result          VARCHAR(20),
    error_msg       TEXT,
    ip              VARCHAR(50),
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_admin_logs_admin ON admin_logs(admin_id);
CREATE INDEX idx_admin_logs_created ON admin_logs(created_at);

COMMENT ON TABLE admin_logs IS '操作日志表';

-- 6. 会员表
CREATE TABLE IF NOT EXISTS t_member (
    id                          BIGSERIAL PRIMARY KEY,
    username                    VARCHAR(50) NOT NULL UNIQUE,
    password                    VARCHAR(100) NOT NULL,
    avatar                      VARCHAR(500),
    phone                       VARCHAR(20) UNIQUE NOT NULL,
    invite_code                 VARCHAR(8) UNIQUE,
    member_type                 VARCHAR(20) DEFAULT 'normal',
    expire_at                   TIMESTAMPTZ,
    referrer_id                 BIGINT,
    referrer_username           VARCHAR(50),
    commission_balance          DECIMAL(10,2) DEFAULT 0.00,
    commission_total_earned     DECIMAL(10,2) DEFAULT 0.00,
    commission_total_paid       DECIMAL(10,2) DEFAULT 0.00,
    status                      VARCHAR(20) DEFAULT 'active',
    last_login_at               TIMESTAMPTZ,
    last_login_ip               VARCHAR(50),
    is_deleted                  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_member_type CHECK (member_type IN ('normal', 'vip')),
    CONSTRAINT chk_member_status CHECK (status IN ('active', 'disabled')),
    CONSTRAINT chk_commission_balance CHECK (commission_balance >= 0)
);

CREATE INDEX idx_member_phone ON t_member(phone) WHERE is_deleted = FALSE;
CREATE INDEX idx_member_type ON t_member(member_type) WHERE is_deleted = FALSE;
CREATE INDEX idx_member_referrer ON t_member(referrer_id) WHERE is_deleted = FALSE;

-- 邀请码生成函数
CREATE OR REPLACE FUNCTION fn_generate_invite_code()
RETURNS VARCHAR AS $$
DECLARE
    chars TEXT := 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
    code VARCHAR(8);
    code_exists BOOLEAN;
BEGIN
    LOOP
        code := '';
        FOR i IN 1..8 LOOP
            code := code || substr(chars, floor(random() * length(chars) + 1)::int, 1);
        END LOOP;
        SELECT EXISTS(SELECT 1 FROM t_member WHERE invite_code = code) INTO code_exists;
        EXIT WHEN NOT code_exists;
    END LOOP;
    RETURN code;
END;
$$ LANGUAGE plpgsql;

-- 自动生成邀请码触发器
CREATE OR REPLACE FUNCTION fn_auto_invite_code()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.invite_code IS NULL THEN
        NEW.invite_code := fn_generate_invite_code();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_member_invite_code
    BEFORE INSERT ON t_member
    FOR EACH ROW
    EXECUTE FUNCTION fn_auto_invite_code();

COMMENT ON TABLE t_member IS '会员表';
COMMENT ON COLUMN t_member.phone IS '手机号（用于登录，必填）';
COMMENT ON COLUMN t_member.invite_code IS '邀请码（8位，自动生成）';

-- 默认管理员（密码：Admin123）
INSERT INTO sys_role (id, role_name, role_code, description, status)
VALUES (1, '超级管理员', 'super_admin', '拥有所有权限', 1);

INSERT INTO sys_admin (id, username, password, real_name, phone, role_id, role_name, status)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8ioctLkRc2xqV8k1u7QwcEVyRZCJ.', '超级管理员', '13800000000', 1, '超级管理员', 1);
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-admin/src/main/resources/db/migration/V1__create_admin_tables.sql
git commit -m "feat: 添加数据库迁移脚本 V1"
```

---

## 阶段3：权限管理 CRUD

### Task 8: 模块管理 - DTO 和 VO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/permission/ModuleAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/permission/ModuleUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/permission/ModuleQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/permission/ModuleTreeVO.java`

- [ ] **Step 1: 创建 ModuleAddDTO**

```java
package com.haifeng.admin.dto.permission;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ModuleAddDTO {

    @NotBlank(message = "模块名称不能为空")
    @Size(max = 50, message = "模块名称最长50字符")
    private String moduleName;

    @NotBlank(message = "模块编码不能为空")
    @Size(max = 50, message = "模块编码最长50字符")
    private String moduleCode;

    private Long parentId;

    @Size(max = 200, message = "路由路径最长200字符")
    private String path;

    @Size(max = 50, message = "图标最长50字符")
    private String icon;

    private Integer sortOrder = 0;

    @NotNull(message = "层级不能为空")
    @Min(value = 1, message = "层级最小为1")
    @Max(value = 2, message = "层级最大为2")
    private Integer level;

    @Size(max = 255, message = "描述最长255字符")
    private String description;
}
```

- [ ] **Step 2: 创建 ModuleUpdateDTO**

```java
package com.haifeng.admin.dto.permission;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ModuleUpdateDTO {

    @NotBlank(message = "模块名称不能为空")
    @Size(max = 50, message = "模块名称最长50字符")
    private String moduleName;

    @NotBlank(message = "模块编码不能为空")
    @Size(max = 50, message = "模块编码最长50字符")
    private String moduleCode;

    private Long parentId;

    @Size(max = 200, message = "路由路径最长200字符")
    private String path;

    @Size(max = 50, message = "图标最长50字符")
    private String icon;

    private Integer sortOrder;

    @NotNull(message = "层级不能为空")
    @Min(value = 1, message = "层级最小为1")
    @Max(value = 2, message = "层级最大为2")
    private Integer level;

    @Size(max = 255, message = "描述最长255字符")
    private String description;
}
```

- [ ] **Step 3: 创建 ModuleQueryDTO**

```java
package com.haifeng.admin.dto.permission;

import lombok.Data;

@Data
public class ModuleQueryDTO {

    private String moduleCode;
}
```

- [ ] **Step 4: 创建 ModuleTreeVO**

```java
package com.haifeng.admin.vo.permission;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class ModuleTreeVO {

    private Long id;

    private String moduleName;

    private String moduleCode;

    private Long parentId;

    private String path;

    private String icon;

    private Integer sortOrder;

    private Integer level;

    private String description;

    private Integer status;

    private OffsetDateTime createdAt;

    private List<ModuleTreeVO> children;
}
```

- [ ] **Step 5: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/permission/
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/permission/ModuleTreeVO.java
git commit -m "feat: 添加模块管理 DTO 和 VO"
```

---

### Task 9: 模块管理 - Service 和 Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/permission/ModuleService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/permission/ModuleServiceImpl.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/permission/ModuleController.java`

- [ ] **Step 1: 创建 ModuleService 接口**

```java
package com.haifeng.admin.service.permission;

import com.haifeng.admin.dto.permission.ModuleAddDTO;
import com.haifeng.admin.dto.permission.ModuleQueryDTO;
import com.haifeng.admin.dto.permission.ModuleUpdateDTO;
import com.haifeng.admin.vo.permission.ModuleTreeVO;

import java.util.List;

public interface ModuleService {

    List<ModuleTreeVO> listTree(ModuleQueryDTO dto);

    void add(ModuleAddDTO dto);

    void update(Long id, ModuleUpdateDTO dto);

    void delete(Long id);
}
```

- [ ] **Step 2: 创建 ModuleServiceImpl**

```java
package com.haifeng.admin.service.impl.permission;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.admin.dto.permission.ModuleAddDTO;
import com.haifeng.admin.dto.permission.ModuleQueryDTO;
import com.haifeng.admin.dto.permission.ModuleUpdateDTO;
import com.haifeng.admin.service.permission.ModuleService;
import com.haifeng.admin.vo.permission.ModuleTreeVO;
import com.haifeng.common.entity.permission.SysModule;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.permission.SysModuleMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService {

    private final SysModuleMapper moduleMapper;

    @Override
    public List<ModuleTreeVO> listTree(ModuleQueryDTO dto) {
        LambdaQueryWrapper<SysModule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysModule::getDeleted, false);

        if (StringUtils.hasText(dto.getModuleCode())) {
            wrapper.like(SysModule::getModuleCode, dto.getModuleCode());
        }

        wrapper.orderByAsc(SysModule::getSortOrder);

        List<SysModule> modules = moduleMapper.selectList(wrapper);
        return buildTree(modules);
    }

    @Override
    public void add(ModuleAddDTO dto) {
        // 检查模块名称是否重复
        Long count = moduleMapper.selectCount(
                new LambdaQueryWrapper<SysModule>()
                        .eq(SysModule::getModuleName, dto.getModuleName())
                        .eq(SysModule::getDeleted, false)
        );
        if (count > 0) {
            throw new BusinessException(400, "模块名称已存在");
        }

        // 检查模块编码是否重复
        count = moduleMapper.selectCount(
                new LambdaQueryWrapper<SysModule>()
                        .eq(SysModule::getModuleCode, dto.getModuleCode())
        );
        if (count > 0) {
            throw new BusinessException(400, "模块编码已存在");
        }

        // 如果有父模块，检查父模块是否存在
        if (dto.getParentId() != null) {
            SysModule parent = moduleMapper.selectById(dto.getParentId());
            if (parent == null || parent.getDeleted()) {
                throw new BusinessException(400, "父模块不存在");
            }
        }

        OffsetDateTime now = OffsetDateTime.now();
        SysModule module = SysModule.builder()
                .id(SnowflakeIdGenerator.nextId())
                .moduleName(dto.getModuleName())
                .moduleCode(dto.getModuleCode())
                .parentId(dto.getParentId())
                .path(dto.getPath())
                .icon(dto.getIcon())
                .sortOrder(dto.getSortOrder())
                .level(dto.getLevel())
                .description(dto.getDescription())
                .status(1)
                .deleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        moduleMapper.insert(module);
        log.info("新增模块成功: {}", dto.getModuleName());
    }

    @Override
    public void update(Long id, ModuleUpdateDTO dto) {
        SysModule module = moduleMapper.selectById(id);
        if (module == null || module.getDeleted()) {
            throw new BusinessException(404, "模块不存在");
        }

        // 检查模块名称是否重复（排除自己）
        Long count = moduleMapper.selectCount(
                new LambdaQueryWrapper<SysModule>()
                        .eq(SysModule::getModuleName, dto.getModuleName())
                        .eq(SysModule::getDeleted, false)
                        .ne(SysModule::getId, id)
        );
        if (count > 0) {
            throw new BusinessException(400, "模块名称已存在");
        }

        // 检查模块编码是否重复（排除自己）
        count = moduleMapper.selectCount(
                new LambdaQueryWrapper<SysModule>()
                        .eq(SysModule::getModuleCode, dto.getModuleCode())
                        .ne(SysModule::getId, id)
        );
        if (count > 0) {
            throw new BusinessException(400, "模块编码已存在");
        }

        module.setModuleName(dto.getModuleName());
        module.setModuleCode(dto.getModuleCode());
        module.setParentId(dto.getParentId());
        module.setPath(dto.getPath());
        module.setIcon(dto.getIcon());
        module.setSortOrder(dto.getSortOrder());
        module.setLevel(dto.getLevel());
        module.setDescription(dto.getDescription());
        module.setUpdatedAt(OffsetDateTime.now());

        moduleMapper.updateById(module);
        log.info("更新模块成功: {}", dto.getModuleName());
    }

    @Override
    public void delete(Long id) {
        SysModule module = moduleMapper.selectById(id);
        if (module == null || module.getDeleted()) {
            throw new BusinessException(404, "模块不存在");
        }

        module.setDeleted(true);
        module.setUpdatedAt(OffsetDateTime.now());
        moduleMapper.updateById(module);
        log.info("删除模块成功: {}", module.getModuleName());
    }

    private List<ModuleTreeVO> buildTree(List<SysModule> modules) {
        List<ModuleTreeVO> voList = modules.stream().map(m -> {
            ModuleTreeVO vo = new ModuleTreeVO();
            BeanUtils.copyProperties(m, vo);
            vo.setChildren(new ArrayList<>());
            return vo;
        }).collect(Collectors.toList());

        Map<Long, ModuleTreeVO> voMap = voList.stream()
                .collect(Collectors.toMap(ModuleTreeVO::getId, v -> v));

        List<ModuleTreeVO> tree = new ArrayList<>();
        for (ModuleTreeVO vo : voList) {
            if (vo.getParentId() == null) {
                tree.add(vo);
            } else {
                ModuleTreeVO parent = voMap.get(vo.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                }
            }
        }
        return tree;
    }
}
```

- [ ] **Step 3: 创建 ModuleController**

```java
package com.haifeng.admin.controller.permission;

import com.haifeng.admin.dto.permission.ModuleAddDTO;
import com.haifeng.admin.dto.permission.ModuleQueryDTO;
import com.haifeng.admin.dto.permission.ModuleUpdateDTO;
import com.haifeng.admin.service.permission.ModuleService;
import com.haifeng.admin.vo.permission.ModuleTreeVO;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/permission/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    @GetMapping
    public R<List<ModuleTreeVO>> list(ModuleQueryDTO dto) {
        List<ModuleTreeVO> tree = moduleService.listTree(dto);
        return R.ok(tree);
    }

    @PostMapping
    public R<Void> add(@Valid @RequestBody ModuleAddDTO dto) {
        moduleService.add(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ModuleUpdateDTO dto) {
        moduleService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        moduleService.delete(id);
        return R.ok();
    }
}
```

- [ ] **Step 4: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/permission/
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/permission/ModuleController.java
git commit -m "feat: 实现模块管理 CRUD"
```

---

### Task 10: 角色管理 - DTO 和 VO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/permission/RoleAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/permission/RoleUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/permission/RoleQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/permission/RoleModuleBindDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/permission/RoleListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/permission/RoleDetailVO.java`

- [ ] **Step 1: 创建 RoleAddDTO**

```java
package com.haifeng.admin.dto.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleAddDTO {

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称最长50字符")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码最长50字符")
    private String roleCode;

    @Size(max = 100, message = "描述最长100字符")
    private String description;
}
```

- [ ] **Step 2: 创建 RoleUpdateDTO**

```java
package com.haifeng.admin.dto.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleUpdateDTO {

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称最长50字符")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码最长50字符")
    private String roleCode;

    @Size(max = 100, message = "描述最长100字符")
    private String description;
}
```

- [ ] **Step 3: 创建 RoleQueryDTO**

```java
package com.haifeng.admin.dto.permission;

import com.haifeng.common.dto.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleQueryDTO extends BasePageQueryDTO {

    private String roleName;
}
```

- [ ] **Step 4: 创建 RoleModuleBindDTO**

```java
package com.haifeng.admin.dto.permission;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class RoleModuleBindDTO {

    @NotEmpty(message = "模块ID列表不能为空")
    private List<Long> moduleIds;
}
```

- [ ] **Step 5: 创建 RoleListVO**

```java
package com.haifeng.admin.vo.permission;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class RoleListVO {

    private Long id;

    private String roleName;

    private String roleCode;

    private String description;

    private Integer status;

    private OffsetDateTime createdAt;
}
```

- [ ] **Step 6: 创建 RoleDetailVO**

```java
package com.haifeng.admin.vo.permission;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class RoleDetailVO {

    private Long id;

    private String roleName;

    private String roleCode;

    private String description;

    private Integer status;

    private OffsetDateTime createdAt;

    private List<Long> moduleIds;

    private List<ModuleTreeVO> modules;
}
```

- [ ] **Step 7: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/permission/Role*.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/permission/Role*.java
git commit -m "feat: 添加角色管理 DTO 和 VO"
```

---

### Task 11: 角色管理 - Service 和 Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/permission/RoleService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/permission/RoleServiceImpl.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/permission/RoleController.java`

- [ ] **Step 1: 创建 RoleService 接口**

```java
package com.haifeng.admin.service.permission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.permission.RoleAddDTO;
import com.haifeng.admin.dto.permission.RoleModuleBindDTO;
import com.haifeng.admin.dto.permission.RoleQueryDTO;
import com.haifeng.admin.dto.permission.RoleUpdateDTO;
import com.haifeng.admin.vo.permission.RoleDetailVO;
import com.haifeng.admin.vo.permission.RoleListVO;

public interface RoleService {

    IPage<RoleListVO> page(RoleQueryDTO dto);

    RoleDetailVO detail(Long id);

    void add(RoleAddDTO dto);

    void update(Long id, RoleUpdateDTO dto);

    void delete(Long id);

    void bindModules(Long id, RoleModuleBindDTO dto);
}
```

- [ ] **Step 2: 创建 RoleServiceImpl**

```java
package com.haifeng.admin.service.impl.permission;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.permission.RoleAddDTO;
import com.haifeng.admin.dto.permission.RoleModuleBindDTO;
import com.haifeng.admin.dto.permission.RoleQueryDTO;
import com.haifeng.admin.dto.permission.RoleUpdateDTO;
import com.haifeng.admin.service.permission.RoleService;
import com.haifeng.admin.vo.permission.ModuleTreeVO;
import com.haifeng.admin.vo.permission.RoleDetailVO;
import com.haifeng.admin.vo.permission.RoleListVO;
import com.haifeng.common.entity.permission.SysModule;
import com.haifeng.common.entity.permission.SysRole;
import com.haifeng.common.entity.permission.SysRoleModule;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.permission.SysModuleMapper;
import com.haifeng.common.mapper.permission.SysRoleMapper;
import com.haifeng.common.mapper.permission.SysRoleModuleMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper roleMapper;
    private final SysModuleMapper moduleMapper;
    private final SysRoleModuleMapper roleModuleMapper;

    @Override
    public IPage<RoleListVO> page(RoleQueryDTO dto) {
        Page<SysRole> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getDeleted, false);

        if (StringUtils.hasText(dto.getRoleName())) {
            wrapper.like(SysRole::getRoleName, dto.getRoleName());
        }

        wrapper.orderByDesc(SysRole::getCreatedAt);

        IPage<SysRole> rolePage = roleMapper.selectPage(page, wrapper);

        return rolePage.convert(role -> {
            RoleListVO vo = new RoleListVO();
            BeanUtils.copyProperties(role, vo);
            return vo;
        });
    }

    @Override
    public RoleDetailVO detail(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null || role.getDeleted()) {
            throw new BusinessException(404, "角色不存在");
        }

        RoleDetailVO vo = new RoleDetailVO();
        BeanUtils.copyProperties(role, vo);

        // 查询关联的模块ID
        List<SysRoleModule> roleModules = roleModuleMapper.selectList(
                new LambdaQueryWrapper<SysRoleModule>()
                        .eq(SysRoleModule::getRoleId, id)
        );
        List<Long> moduleIds = roleModules.stream()
                .map(SysRoleModule::getModuleId)
                .collect(Collectors.toList());
        vo.setModuleIds(moduleIds);

        // 查询模块详情并构建树
        if (!moduleIds.isEmpty()) {
            List<SysModule> modules = moduleMapper.selectBatchIds(moduleIds);
            vo.setModules(buildModuleTree(modules));
        } else {
            vo.setModules(new ArrayList<>());
        }

        return vo;
    }

    @Override
    public void add(RoleAddDTO dto) {
        // 检查角色名称是否重复
        Long count = roleMapper.selectCount(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleName, dto.getRoleName())
                        .eq(SysRole::getDeleted, false)
        );
        if (count > 0) {
            throw new BusinessException(400, "角色名称已存在");
        }

        // 检查角色编码是否重复
        count = roleMapper.selectCount(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleCode, dto.getRoleCode())
                        .eq(SysRole::getDeleted, false)
        );
        if (count > 0) {
            throw new BusinessException(400, "角色编码已存在");
        }

        OffsetDateTime now = OffsetDateTime.now();
        SysRole role = SysRole.builder()
                .id(SnowflakeIdGenerator.nextId())
                .roleName(dto.getRoleName())
                .roleCode(dto.getRoleCode())
                .description(dto.getDescription())
                .status(1)
                .deleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        roleMapper.insert(role);
        log.info("新增角色成功: {}", dto.getRoleName());
    }

    @Override
    public void update(Long id, RoleUpdateDTO dto) {
        SysRole role = roleMapper.selectById(id);
        if (role == null || role.getDeleted()) {
            throw new BusinessException(404, "角色不存在");
        }

        // 检查角色名称是否重复（排除自己）
        Long count = roleMapper.selectCount(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleName, dto.getRoleName())
                        .eq(SysRole::getDeleted, false)
                        .ne(SysRole::getId, id)
        );
        if (count > 0) {
            throw new BusinessException(400, "角色名称已存在");
        }

        // 检查角色编码是否重复（排除自己）
        count = roleMapper.selectCount(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleCode, dto.getRoleCode())
                        .eq(SysRole::getDeleted, false)
                        .ne(SysRole::getId, id)
        );
        if (count > 0) {
            throw new BusinessException(400, "角色编码已存在");
        }

        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setDescription(dto.getDescription());
        role.setUpdatedAt(OffsetDateTime.now());

        roleMapper.updateById(role);
        log.info("更新角色成功: {}", dto.getRoleName());
    }

    @Override
    public void delete(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null || role.getDeleted()) {
            throw new BusinessException(404, "角色不存在");
        }

        role.setDeleted(true);
        role.setUpdatedAt(OffsetDateTime.now());
        roleMapper.updateById(role);
        log.info("删除角色成功: {}", role.getRoleName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindModules(Long id, RoleModuleBindDTO dto) {
        SysRole role = roleMapper.selectById(id);
        if (role == null || role.getDeleted()) {
            throw new BusinessException(404, "角色不存在");
        }

        // 删除现有的关联
        roleModuleMapper.delete(
                new LambdaQueryWrapper<SysRoleModule>()
                        .eq(SysRoleModule::getRoleId, id)
        );

        // 展开父模块（方案A：存储层展开）
        List<Long> allModuleIds = new ArrayList<>();
        for (Long moduleId : dto.getModuleIds()) {
            allModuleIds.add(moduleId);

            // 查询该模块
            SysModule module = moduleMapper.selectById(moduleId);
            if (module != null && module.getLevel() == 1) {
                // 是父模块，查询所有子模块
                List<SysModule> children = moduleMapper.selectList(
                        new LambdaQueryWrapper<SysModule>()
                                .eq(SysModule::getParentId, moduleId)
                                .eq(SysModule::getDeleted, false)
                );
                for (SysModule child : children) {
                    if (!allModuleIds.contains(child.getId())) {
                        allModuleIds.add(child.getId());
                    }
                }
            }
        }

        // 插入新的关联
        OffsetDateTime now = OffsetDateTime.now();
        for (Long moduleId : allModuleIds) {
            SysRoleModule rm = SysRoleModule.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .roleId(id)
                    .moduleId(moduleId)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            roleModuleMapper.insert(rm);
        }

        log.info("角色绑定模块成功: roleId={}, moduleCount={}", id, allModuleIds.size());
    }

    private List<ModuleTreeVO> buildModuleTree(List<SysModule> modules) {
        List<ModuleTreeVO> voList = modules.stream().map(m -> {
            ModuleTreeVO vo = new ModuleTreeVO();
            BeanUtils.copyProperties(m, vo);
            vo.setChildren(new ArrayList<>());
            return vo;
        }).collect(Collectors.toList());

        List<ModuleTreeVO> tree = new ArrayList<>();
        for (ModuleTreeVO vo : voList) {
            if (vo.getParentId() == null) {
                tree.add(vo);
            }
        }
        return tree;
    }
}
```

- [ ] **Step 3: 创建 RoleController**

```java
package com.haifeng.admin.controller.permission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.permission.RoleAddDTO;
import com.haifeng.admin.dto.permission.RoleModuleBindDTO;
import com.haifeng.admin.dto.permission.RoleQueryDTO;
import com.haifeng.admin.dto.permission.RoleUpdateDTO;
import com.haifeng.admin.service.permission.RoleService;
import com.haifeng.admin.vo.permission.RoleDetailVO;
import com.haifeng.admin.vo.permission.RoleListVO;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/permission/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public R<IPage<RoleListVO>> page(RoleQueryDTO dto) {
        IPage<RoleListVO> page = roleService.page(dto);
        return R.ok(page);
    }

    @GetMapping("/{id}")
    public R<RoleDetailVO> detail(@PathVariable Long id) {
        RoleDetailVO vo = roleService.detail(id);
        return R.ok(vo);
    }

    @PostMapping
    public R<Void> add(@Valid @RequestBody RoleAddDTO dto) {
        roleService.add(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody RoleUpdateDTO dto) {
        roleService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return R.ok();
    }

    @PostMapping("/{id}/modules")
    public R<Void> bindModules(@PathVariable Long id, @Valid @RequestBody RoleModuleBindDTO dto) {
        roleService.bindModules(id, dto);
        return R.ok();
    }
}
```

- [ ] **Step 4: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/permission/RoleService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/permission/RoleServiceImpl.java
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/permission/RoleController.java
git commit -m "feat: 实现角色管理 CRUD"
```

---

### Task 12: 管理员管理 - DTO 和 VO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/permission/AdminAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/permission/AdminUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/permission/AdminQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/permission/AdminListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/permission/AdminDetailVO.java`

- [ ] **Step 1: 创建 AdminAddDTO**

```java
package com.haifeng.admin.dto.permission;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AdminAddDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度必须在2-50之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,16}$",
             message = "密码必须是数字+字母，长度6-16位")
    private String password;

    @Size(max = 50, message = "真实姓名最长50字符")
    private String realName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱最长100字符")
    private String email;

    @Size(max = 500, message = "头像URL最长500字符")
    private String avatar;

    @NotNull(message = "角色ID不能为空")
    private Long roleId;
}
```

- [ ] **Step 2: 创建 AdminUpdateDTO**

```java
package com.haifeng.admin.dto.permission;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AdminUpdateDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度必须在2-50之间")
    private String username;

    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,16}$",
             message = "密码必须是数字+字母，长度6-16位")
    private String password;

    @Size(max = 50, message = "真实姓名最长50字符")
    private String realName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱最长100字符")
    private String email;

    @Size(max = 500, message = "头像URL最长500字符")
    private String avatar;

    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    private Integer status;
}
```

- [ ] **Step 3: 创建 AdminQueryDTO**

```java
package com.haifeng.admin.dto.permission;

import com.haifeng.common.dto.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminQueryDTO extends BasePageQueryDTO {

    private String username;

    private String phone;

    private String realName;

    private Integer status;
}
```

- [ ] **Step 4: 创建 AdminListVO**

```java
package com.haifeng.admin.vo.permission;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class AdminListVO {

    private Long id;

    private String username;

    private String realName;

    private String phone;

    private String email;

    private String avatar;

    private Long roleId;

    private String roleName;

    private Integer status;

    private OffsetDateTime lastLoginAt;

    private OffsetDateTime createdAt;
}
```

- [ ] **Step 5: 创建 AdminDetailVO**

```java
package com.haifeng.admin.vo.permission;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class AdminDetailVO {

    private Long id;

    private String username;

    private String realName;

    private String phone;

    private String email;

    private String avatar;

    private Long roleId;

    private String roleName;

    private Integer status;

    private OffsetDateTime lastLoginAt;

    private String lastLoginIp;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 6: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/dto/permission/Admin*.java
git add haifeng-admin/src/main/java/com/haifeng/admin/vo/permission/Admin*.java
git commit -m "feat: 添加管理员管理 DTO 和 VO"
```

---

### Task 13: 管理员管理 - Service 和 Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/permission/AdminService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/permission/AdminServiceImpl.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/permission/AdminController.java`

- [ ] **Step 1: 创建 AdminService 接口**

```java
package com.haifeng.admin.service.permission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.permission.AdminAddDTO;
import com.haifeng.admin.dto.permission.AdminQueryDTO;
import com.haifeng.admin.dto.permission.AdminUpdateDTO;
import com.haifeng.admin.vo.permission.AdminDetailVO;
import com.haifeng.admin.vo.permission.AdminListVO;

public interface AdminService {

    IPage<AdminListVO> page(AdminQueryDTO dto);

    AdminDetailVO detail(Long id);

    void add(AdminAddDTO dto);

    void update(Long id, AdminUpdateDTO dto);

    void delete(Long id);
}
```

- [ ] **Step 2: 创建 AdminServiceImpl**

```java
package com.haifeng.admin.service.impl.permission;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.permission.AdminAddDTO;
import com.haifeng.admin.dto.permission.AdminQueryDTO;
import com.haifeng.admin.dto.permission.AdminUpdateDTO;
import com.haifeng.admin.service.permission.AdminService;
import com.haifeng.admin.vo.permission.AdminDetailVO;
import com.haifeng.admin.vo.permission.AdminListVO;
import com.haifeng.common.entity.permission.SysAdmin;
import com.haifeng.common.entity.permission.SysRole;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.permission.SysAdminMapper;
import com.haifeng.common.mapper.permission.SysRoleMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final SysAdminMapper adminMapper;
    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public IPage<AdminListVO> page(AdminQueryDTO dto) {
        Page<SysAdmin> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<SysAdmin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAdmin::getDeleted, false);

        if (StringUtils.hasText(dto.getUsername())) {
            wrapper.like(SysAdmin::getUsername, dto.getUsername());
        }
        if (StringUtils.hasText(dto.getPhone())) {
            wrapper.like(SysAdmin::getPhone, dto.getPhone());
        }
        if (StringUtils.hasText(dto.getRealName())) {
            wrapper.like(SysAdmin::getRealName, dto.getRealName());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(SysAdmin::getStatus, dto.getStatus());
        }

        wrapper.orderByDesc(SysAdmin::getCreatedAt);

        IPage<SysAdmin> adminPage = adminMapper.selectPage(page, wrapper);

        return adminPage.convert(admin -> {
            AdminListVO vo = new AdminListVO();
            BeanUtils.copyProperties(admin, vo);
            return vo;
        });
    }

    @Override
    public AdminDetailVO detail(Long id) {
        SysAdmin admin = adminMapper.selectById(id);
        if (admin == null || admin.getDeleted()) {
            throw new BusinessException(404, "管理员不存在");
        }

        AdminDetailVO vo = new AdminDetailVO();
        BeanUtils.copyProperties(admin, vo);
        return vo;
    }

    @Override
    public void add(AdminAddDTO dto) {
        // 检查用户名是否重复
        Long count = adminMapper.selectCount(
                new LambdaQueryWrapper<SysAdmin>()
                        .eq(SysAdmin::getUsername, dto.getUsername())
        );
        if (count > 0) {
            throw new BusinessException(400, "用户名已存在");
        }

        // 检查手机号是否重复
        count = adminMapper.selectCount(
                new LambdaQueryWrapper<SysAdmin>()
                        .eq(SysAdmin::getPhone, dto.getPhone())
        );
        if (count > 0) {
            throw new BusinessException(400, "手机号已存在");
        }

        // 查询角色
        SysRole role = roleMapper.selectById(dto.getRoleId());
        if (role == null || role.getDeleted()) {
            throw new BusinessException(400, "角色不存在");
        }

        OffsetDateTime now = OffsetDateTime.now();
        SysAdmin admin = SysAdmin.builder()
                .id(SnowflakeIdGenerator.nextId())
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .realName(dto.getRealName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .avatar(dto.getAvatar())
                .roleId(dto.getRoleId())
                .roleName(role.getRoleName())
                .status(1)
                .deleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        adminMapper.insert(admin);
        log.info("新增管理员成功: {}", dto.getUsername());
    }

    @Override
    public void update(Long id, AdminUpdateDTO dto) {
        SysAdmin admin = adminMapper.selectById(id);
        if (admin == null || admin.getDeleted()) {
            throw new BusinessException(404, "管理员不存在");
        }

        // 检查用户名是否重复（排除自己）
        Long count = adminMapper.selectCount(
                new LambdaQueryWrapper<SysAdmin>()
                        .eq(SysAdmin::getUsername, dto.getUsername())
                        .ne(SysAdmin::getId, id)
        );
        if (count > 0) {
            throw new BusinessException(400, "用户名已存在");
        }

        // 检查手机号是否重复（排除自己）
        count = adminMapper.selectCount(
                new LambdaQueryWrapper<SysAdmin>()
                        .eq(SysAdmin::getPhone, dto.getPhone())
                        .ne(SysAdmin::getId, id)
        );
        if (count > 0) {
            throw new BusinessException(400, "手机号已存在");
        }

        // 查询角色
        SysRole role = roleMapper.selectById(dto.getRoleId());
        if (role == null || role.getDeleted()) {
            throw new BusinessException(400, "角色不存在");
        }

        admin.setUsername(dto.getUsername());
        if (StringUtils.hasText(dto.getPassword())) {
            admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        admin.setRealName(dto.getRealName());
        admin.setPhone(dto.getPhone());
        admin.setEmail(dto.getEmail());
        admin.setAvatar(dto.getAvatar());
        admin.setRoleId(dto.getRoleId());
        admin.setRoleName(role.getRoleName());
        if (dto.getStatus() != null) {
            admin.setStatus(dto.getStatus());
        }
        admin.setUpdatedAt(OffsetDateTime.now());

        adminMapper.updateById(admin);
        log.info("更新管理员成功: {}", dto.getUsername());
    }

    @Override
    public void delete(Long id) {
        SysAdmin admin = adminMapper.selectById(id);
        if (admin == null || admin.getDeleted()) {
            throw new BusinessException(404, "管理员不存在");
        }

        admin.setDeleted(true);
        admin.setUpdatedAt(OffsetDateTime.now());
        adminMapper.updateById(admin);
        log.info("删除管理员成功: {}", admin.getUsername());
    }
}
```

- [ ] **Step 3: 创建 AdminController**

```java
package com.haifeng.admin.controller.permission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.permission.AdminAddDTO;
import com.haifeng.admin.dto.permission.AdminQueryDTO;
import com.haifeng.admin.dto.permission.AdminUpdateDTO;
import com.haifeng.admin.service.permission.AdminService;
import com.haifeng.admin.vo.permission.AdminDetailVO;
import com.haifeng.admin.vo.permission.AdminListVO;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/permission/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public R<IPage<AdminListVO>> page(AdminQueryDTO dto) {
        IPage<AdminListVO> page = adminService.page(dto);
        return R.ok(page);
    }

    @GetMapping("/{id}")
    public R<AdminDetailVO> detail(@PathVariable Long id) {
        AdminDetailVO vo = adminService.detail(id);
        return R.ok(vo);
    }

    @PostMapping
    public R<Void> add(@Valid @RequestBody AdminAddDTO dto) {
        adminService.add(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody AdminUpdateDTO dto) {
        adminService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        adminService.delete(id);
        return R.ok();
    }
}
```

- [ ] **Step 4: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/service/permission/AdminService.java
git add haifeng-admin/src/main/java/com/haifeng/admin/service/impl/permission/AdminServiceImpl.java
git add haifeng-admin/src/main/java/com/haifeng/admin/controller/permission/AdminController.java
git commit -m "feat: 实现管理员管理 CRUD"
```

---

### Task 14: 更新 Mapper 扫描配置

**Files:**
- Modify: `haifeng-admin/src/main/java/com/haifeng/admin/HaiFengAdminApplication.java`
- Modify: `haifeng-app/src/main/java/com/haifeng/app/HaiFengAppApplication.java`

- [ ] **Step 1: 更新 Admin Application**

```java
package com.haifeng.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.haifeng.admin", "com.haifeng.common"})
@MapperScan(basePackages = "com.haifeng.common.mapper")
public class HaiFengAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(HaiFengAdminApplication.class, args);
    }
}
```

- [ ] **Step 2: 更新 App Application**

```java
package com.haifeng.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.haifeng.app", "com.haifeng.common"})
@MapperScan(basePackages = "com.haifeng.common.mapper")
public class HaiFengAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(HaiFengAppApplication.class, args);
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-admin/src/main/java/com/haifeng/admin/HaiFengAdminApplication.java
git add haifeng-app/src/main/java/com/haifeng/app/HaiFengAppApplication.java
git commit -m "fix: 更新 Mapper 扫描配置"
```

---

### Task 15: 最终验证和提交

- [ ] **Step 1: 编译项目**

```bash
cd D:/exeProject/ideaProjects/Project-HaiFeng
mvn clean compile -DskipTests
```

预期: BUILD SUCCESS

- [ ] **Step 2: 最终提交**

```bash
git add -A
git commit -m "feat: 完成权限管理模块实现"
```
