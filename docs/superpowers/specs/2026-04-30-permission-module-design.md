# 权限管理模块设计规格

## 概述

本文档定义了海峰未来规划院项目的权限管理模块重构设计，包含架构调整、数据库修正、登录模块改造和权限管理CRUD实现。

## 分阶段实施计划

| 阶段 | 内容 | 依赖 |
|------|------|------|
| 阶段1 | 架构重构（Entity/Mapper迁移、包结构调整） | 无 |
| 阶段2 | 数据库表修正、登录模块改造 | 阶段1 |
| 阶段3 | 权限管理CRUD实现 | 阶段2 |

---

## 阶段1：架构重构

### 1.1 Entity/Mapper 迁移

**从 haifeng-admin 迁移到 haifeng-common：**
- `SysAdmin.java` → `entity/permission/SysAdmin.java`
- `SysRole.java` → `entity/permission/SysRole.java`
- `SysModule.java` → `entity/permission/SysModule.java`
- `SysRoleModule.java` → `entity/permission/SysRoleModule.java`
- `AdminLog.java` → `entity/system/AdminLog.java`
- `SysAdminMapper.java` → `mapper/permission/SysAdminMapper.java`

**从 haifeng-app 迁移到 haifeng-common：**
- `Member.java` → `entity/user/Member.java`
- `MemberProfile.java` → `entity/user/MemberProfile.java`
- `MemberMapper.java` → `mapper/user/MemberMapper.java`

### 1.2 包结构调整

**haifeng-admin 调整：**
```
com.haifeng.admin/
├── controller/
│   ├── auth/
│   │   └── AdminAuthController.java
│   └── permission/
│       ├── ModuleController.java
│       ├── RoleController.java
│       └── AdminController.java
├── service/
│   ├── auth/
│   │   └── AdminAuthService.java
│   ├── permission/
│   │   ├── ModuleService.java
│   │   ├── RoleService.java
│   │   └── AdminService.java
│   └── impl/
│       ├── auth/
│       │   └── AdminAuthServiceImpl.java
│       └── permission/
│           ├── ModuleServiceImpl.java
│           ├── RoleServiceImpl.java
│           └── AdminServiceImpl.java
├── dto/
│   ├── auth/
│   │   └── AdminLoginDTO.java
│   └── permission/
│       ├── ModuleAddDTO.java
│       ├── ModuleUpdateDTO.java
│       ├── ModuleQueryDTO.java
│       ├── RoleAddDTO.java
│       ├── RoleUpdateDTO.java
│       ├── RoleQueryDTO.java
│       ├── RoleModuleBindDTO.java
│       ├── AdminAddDTO.java
│       ├── AdminUpdateDTO.java
│       └── AdminQueryDTO.java
└── vo/
    └── permission/
        ├── ModuleTreeVO.java
        ├── RoleListVO.java
        ├── RoleDetailVO.java
        ├── AdminListVO.java
        └── AdminDetailVO.java
```

**haifeng-app 调整：**
```
com.haifeng.app/
├── controller/
│   └── auth/
│       └── AppAuthController.java
├── service/
│   └── auth/
│       └── AppAuthService.java
│   └── impl/
│       └── auth/
│           └── AppAuthServiceImpl.java
└── dto/
    └── auth/
        └── RegisterDTO.java
```

### 1.3 新增 BasePageQueryDTO

```java
// haifeng-common/src/main/java/com/haifeng/common/dto/BasePageQueryDTO.java
@Data
public class BasePageQueryDTO {
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Min(value = 10, message = "每页最小10条")
    @Max(value = 1000, message = "每页最大1000条")
    private Integer size = 10;
}
```

---

## 阶段2：数据库表与登录模块

### 2.1 数据库表修正

**V1__create_admin_tables.sql 完整内容：**

```sql
-- 1. 角色表
CREATE TABLE sys_role (
    id              BIGSERIAL PRIMARY KEY,
    role_name       VARCHAR(50) NOT NULL,
    role_code       VARCHAR(50) NOT NULL,
    description     VARCHAR(100),
    status          SMALLINT DEFAULT 1,
    is_deleted      BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT uk_role_name UNIQUE (role_name) WHERE is_deleted = FALSE,
    CONSTRAINT uk_role_code UNIQUE (role_code) WHERE is_deleted = FALSE
);

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
    updated_at    TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT uk_module_name UNIQUE (module_name) WHERE is_deleted = FALSE
);

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

### 2.2 登录模块改造

**LoginDTO（手机号登录）：**
```java
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

**RegisterDTO（用户注册）：**
```java
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

**登录逻辑改造：**
- 管理员：通过 `phone` 字段查询 `sys_admin` 表
- 用户：通过 `phone` 字段查询 `t_member` 表

---

## 阶段3：权限管理CRUD

### 3.1 模块管理

**接口列表：**

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/v1/admin/permission/modules | 树形列表，支持 moduleCode 模糊查询 |
| POST | /api/v1/admin/permission/modules | 新增模块 |
| PUT | /api/v1/admin/permission/modules/{id} | 修改模块 |
| DELETE | /api/v1/admin/permission/modules/{id} | 软删除 |

**ModuleAddDTO：**
```java
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

**ModuleTreeVO：**
```java
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
    private List<ModuleTreeVO> children;
}
```

### 3.2 角色管理

**接口列表：**

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/v1/admin/permission/roles | 分页列表，支持 roleName 模糊查询 |
| GET | /api/v1/admin/permission/roles/{id} | 详情（含关联模块） |
| POST | /api/v1/admin/permission/roles | 新增角色 |
| PUT | /api/v1/admin/permission/roles/{id} | 修改角色 |
| DELETE | /api/v1/admin/permission/roles/{id} | 软删除 |
| POST | /api/v1/admin/permission/roles/{id}/modules | 绑定模块 |

**RoleAddDTO：**
```java
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

**RoleModuleBindDTO：**
```java
@Data
public class RoleModuleBindDTO {
    @NotEmpty(message = "模块ID列表不能为空")
    private List<Long> moduleIds;
}
```

**绑定模块逻辑（方案A：存储层展开）：**
1. 删除该角色的所有现有模块关联
2. 遍历传入的 moduleIds
3. 对于每个 moduleId，如果是父模块（level=1），查询所有子模块并一并插入
4. 插入所有关联记录到 sys_role_module

### 3.3 管理员管理

**接口列表：**

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/v1/admin/permission/admins | 分页列表，支持 username/phone/realName 模糊查询 |
| GET | /api/v1/admin/permission/admins/{id} | 详情 |
| POST | /api/v1/admin/permission/admins | 新增管理员 |
| PUT | /api/v1/admin/permission/admins/{id} | 修改管理员 |
| DELETE | /api/v1/admin/permission/admins/{id} | 软删除 |

**AdminAddDTO：**
```java
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

**AdminQueryDTO：**
```java
@Data
public class AdminQueryDTO extends BasePageQueryDTO {
    private String username;
    private String phone;
    private String realName;
    private Integer status;
}
```

**AdminListVO：**
```java
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

---

## 唯一性约束

| 表 | 字段 | 约束 |
|------|------|------|
| sys_role | role_name | 同一 is_deleted=FALSE 范围内唯一 |
| sys_role | role_code | 同一 is_deleted=FALSE 范围内唯一 |
| sys_module | module_name | 同一 is_deleted=FALSE 范围内唯一 |
| sys_module | module_code | 全局唯一（不考虑软删除） |
| sys_admin | username | 全局唯一 |
| sys_admin | phone | 全局唯一 |
| t_member | phone | 全局唯一 |

---

## 注意事项

1. 所有写操作（POST/PUT/DELETE）需要添加 `@OperationLog` 注解，AOP 自动记录操作日志
2. Entity 中的 ID 使用 `@TableId(type = IdType.ASSIGN_ID)` 配合雪花算法
3. 所有列表查询 DTO 需继承 `BasePageQueryDTO`
4. 分页参数支持：10, 20, 30, 50, 100, 200, 500, 1000
