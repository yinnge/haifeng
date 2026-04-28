-- 角色表
CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    status SMALLINT DEFAULT 1 NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE sys_role IS '角色表';
COMMENT ON COLUMN sys_role.id IS '角色ID';
COMMENT ON COLUMN sys_role.role_name IS '角色名称';
COMMENT ON COLUMN sys_role.role_code IS '角色编码';
COMMENT ON COLUMN sys_role.description IS '角色描述';
COMMENT ON COLUMN sys_role.status IS '状态: 0-禁用, 1-启用';

-- 模块表
CREATE TABLE sys_module (
    id BIGINT PRIMARY KEY,
    module_name VARCHAR(50) NOT NULL,
    module_code VARCHAR(50) NOT NULL UNIQUE,
    parent_id BIGINT DEFAULT 0,
    path VARCHAR(200),
    icon VARCHAR(100),
    sort_order INT DEFAULT 0,
    status SMALLINT DEFAULT 1 NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE sys_module IS '模块表';
COMMENT ON COLUMN sys_module.id IS '模块ID';
COMMENT ON COLUMN sys_module.module_name IS '模块名称';
COMMENT ON COLUMN sys_module.module_code IS '模块编码';
COMMENT ON COLUMN sys_module.parent_id IS '父模块ID';
COMMENT ON COLUMN sys_module.path IS '路由路径';
COMMENT ON COLUMN sys_module.icon IS '图标';
COMMENT ON COLUMN sys_module.sort_order IS '排序';
COMMENT ON COLUMN sys_module.status IS '状态: 0-禁用, 1-启用';

-- 角色模块关联表
CREATE TABLE sys_role_module (
    id BIGINT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_role_module UNIQUE (role_id, module_id)
);

COMMENT ON TABLE sys_role_module IS '角色模块关联表';
COMMENT ON COLUMN sys_role_module.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_module.module_id IS '模块ID';

-- 管理员表
CREATE TABLE sys_admin (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    avatar VARCHAR(255),
    role_id BIGINT NOT NULL,
    role_name VARCHAR(50),
    status SMALLINT DEFAULT 1 NOT NULL,
    last_login_at TIMESTAMPTZ,
    last_login_ip VARCHAR(50),
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE sys_admin IS '管理员表';
COMMENT ON COLUMN sys_admin.id IS '管理员ID';
COMMENT ON COLUMN sys_admin.username IS '用户名';
COMMENT ON COLUMN sys_admin.password IS '密码（BCrypt加密）';
COMMENT ON COLUMN sys_admin.real_name IS '真实姓名';
COMMENT ON COLUMN sys_admin.phone IS '手机号';
COMMENT ON COLUMN sys_admin.email IS '邮箱';
COMMENT ON COLUMN sys_admin.avatar IS '头像URL';
COMMENT ON COLUMN sys_admin.role_id IS '角色ID';
COMMENT ON COLUMN sys_admin.role_name IS '角色名称（冗余）';
COMMENT ON COLUMN sys_admin.status IS '状态: 0-禁用, 1-启用';
COMMENT ON COLUMN sys_admin.last_login_at IS '最后登录时间';
COMMENT ON COLUMN sys_admin.last_login_ip IS '最后登录IP';
COMMENT ON COLUMN sys_admin.is_deleted IS '是否删除';

-- 管理员操作日志表
CREATE TABLE admin_logs (
    id BIGINT PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    admin_name VARCHAR(50),
    operation VARCHAR(100) NOT NULL,
    method VARCHAR(200),
    params TEXT,
    ip VARCHAR(50),
    user_agent VARCHAR(500),
    status SMALLINT DEFAULT 1 NOT NULL,
    error_msg TEXT,
    execute_time BIGINT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE admin_logs IS '管理员操作日志表';
COMMENT ON COLUMN admin_logs.admin_id IS '管理员ID';
COMMENT ON COLUMN admin_logs.admin_name IS '管理员用户名';
COMMENT ON COLUMN admin_logs.operation IS '操作描述';
COMMENT ON COLUMN admin_logs.method IS '请求方法';
COMMENT ON COLUMN admin_logs.params IS '请求参数';
COMMENT ON COLUMN admin_logs.ip IS '请求IP';
COMMENT ON COLUMN admin_logs.user_agent IS 'User-Agent';
COMMENT ON COLUMN admin_logs.status IS '状态: 0-失败, 1-成功';
COMMENT ON COLUMN admin_logs.error_msg IS '错误信息';
COMMENT ON COLUMN admin_logs.execute_time IS '执行时间(ms)';

-- 索引
CREATE INDEX idx_sys_admin_username ON sys_admin(username);
CREATE INDEX idx_sys_admin_phone ON sys_admin(phone);
CREATE INDEX idx_sys_admin_status ON sys_admin(status);
CREATE INDEX idx_admin_logs_admin_id ON admin_logs(admin_id);
CREATE INDEX idx_admin_logs_created_at ON admin_logs(created_at);

-- 初始化超级管理员角色
INSERT INTO sys_role (id, role_name, role_code, description, status, created_at, updated_at)
VALUES (1, '超级管理员', 'SUPER_ADMIN', '拥有所有权限', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 初始化管理员账号（密码: 123456）
INSERT INTO sys_admin (id, username, password, real_name, role_id, role_name, status, created_at, updated_at)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', 1, '超级管理员', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
