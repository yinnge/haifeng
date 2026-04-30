## **1.1 公告表 (announcements)**（新的公告会展示在网页）


| 字段名        | 类型           | 约束                        | 说明   |
| ---------- | ------------ | ------------------------- | ---- |
| id         | SERIAL       | PRIMARY KEY               | 公告ID |
| title      | VARCHAR(100) | NOT NULL                  | 公告标题 |
| content    | TEXT         | NOT NULL                  | 公告内容 |
| Tag        | VARCHAR(20)  |                           | 公告类型 |
| created_at | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP | 更新时间 |


## **1.2 系统设置表 (system_settings)**（设置网站的logos等）


| 字段名             | 类型           | 约束                                    | 说明             |
| --------------- | ------------ | ------------------------------------- | -------------- |
| id              | SERIAL       | PRIMARY KEY                           | 设置ID           |
| site_name       | VARCHAR(50)  |                                       | 网站名称           |
| site_url        | VARCHAR(100) |                                       | 网站Logo         |
| site_icp        | VARCHAR(100) |                                       | ICP备案号         |
| site_descption  | Text         |                                       | 网站描述           |
| api_number      | integer      | DEFAULT 3                             | api调用限制        |
| member_price    | INT          | DEFAULT 199                           | 会员价格           |
| vip_price       | INT          | DEFAULT 599                           | vip会员价格        |
| seo_title       | VARCHAR(200) |                                       | SEO标题（只展示首页即可） |
| seo_keywords    | VARCHAR(100) |                                       | SEO关键词         |
| seo_description | TEXT         |                                       | SEO描述          |
| updated_at      | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP             | 更新时间           |
| wechat_ew_url   | VARCHAR(100) |                                       | 微信url          |
| contact_url     | JSONB        | wechat,weibo,zhihu,douyin,b站          | url            |
| basic_message   | JSONB        | address,phone,email,consultation_time | 全是字符串          |

# 操作日志表（记录管理员操作人的信息）


```
CREATE TABLE admin_logs (
    id              SERIAL        PRIMARY KEY,
    operator_id     INTEGER       NOT NULL,
    operator_name   VARCHAR(50)   NOT NULL,
    action          VARCHAR(100)  NOT NULL,
    ip              VARCHAR(50),
    created_at      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_admin_logs_operator_id ON admin_logs(operator_id);
CREATE INDEX idx_admin_logs_created_at  ON admin_logs(created_at);

COMMENT ON TABLE  admin_logs               IS '管理员操作日志表';
COMMENT ON COLUMN admin_logs.id            IS '日志ID';
COMMENT ON COLUMN admin_logs.operator_id   IS '操作人ID';
COMMENT ON COLUMN admin_logs.operator_name IS '操作人姓名';
COMMENT ON COLUMN admin_logs.action        IS '操作内容，如：编辑了院校"清华大学"信息';
COMMENT ON COLUMN admin_logs.ip            IS '操作时的IP地址';
COMMENT ON COLUMN admin_logs.created_at    IS '操作时间';
```


-- 1. 角色表（管理员的的角色）
```
CREATE TABLE sys_role (
    id          SERIAL PRIMARY KEY,
    role_name   VARCHAR(50) NOT NULL,
    role_code   VARCHAR(50) NOT NULL,
    description VARCHAR(100),
    status      SMALLINT DEFAULT 1,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```
-- 2. 模块表（后端的模块，不同的角色对模块编辑有权限）
```

-- 2. 模块表（支持父子层级）
CREATE TABLE sys_module (
    id            BIGSERIAL PRIMARY KEY,
    module_name   VARCHAR(50) NOT NULL,          
    module_code   VARCHAR(50) NOT NULL UNIQUE,   -- 全局唯一
    parent_id     BIGINT REFERENCES sys_module(id) ON DELETE CASCADE,
    path          VARCHAR(200),                  -- 前端路由 /admin/system/setting
    icon          VARCHAR(50),
    sort_order    INTEGER DEFAULT 0,
    level         SMALLINT NOT NULL,             -- 1=父模块 2=子模块
    description   VARCHAR(255),
    status        SMALLINT DEFAULT 1,
    is_deleted    BOOLEAN DEFAULT FALSE,
    created_at    TIMESTAMPTZ DEFAULT NOW(),
    updated_at    TIMESTAMPTZ DEFAULT NOW()
);

COMMENT ON COLUMN sys_module.parent_id IS '父模块ID，NULL表示顶级';
COMMENT ON COLUMN sys_module.level     IS '1父2子，用于快速判断';

CREATE INDEX idx_module_parent ON sys_module(parent_id);
```

-- 3. 角色-模块关联表（中间表）
```
CREATE TABLE sys_role_module (
    id          SERIAL PRIMARY KEY,
    role_id     INTEGER NOT NULL,   -- 逻辑外键，指向 sys_role.id
    module_id   INTEGER NOT NULL,   -- 逻辑外键，指向 sys_module.id
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
```
-- 4. 管理员表

```
CREATE TABLE sys_admin (
    id          SERIAL PRIMARY KEY,
    username    VARCHAR(50) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    phone       VARCHAR(20),
    role_id     INTEGER NOT NULL,   -- 逻辑外键，指向 sys_role.id
    
    status      SMALLINT DEFAULT 1,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```
