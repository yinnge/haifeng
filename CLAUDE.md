# 海峰未来规划院 - 开发规范

## 项目简介
教育规划平台，核心功能：高考志愿填报 + 考公考研就业指导
面向付费会员，分 normal / vip 两档

## 技术栈
- 后端：Spring Boot 3.x + MyBatis-Plus + Spring Security + Spring AI
- 数据库：PostgreSQL + Redis
- 认证：JWT 双Token（Access 2h + Refresh 7d）
- 数据库迁移：Flyway
- 构建：Maven
- 部署：Docker + Traefik
- ID生成：雪花算法

## 项目结构规范
haifeng-backend/
├── haifeng-common/          # 公共模块（工具类/枚举/常量）
├── haifeng-admin/           # 管理员端
└── haifeng-app/             # 用户端（C端）

## 包结构（每个模块内）
com.haifeng.xxx/
├── config/                  # 配置类
├── controller/              # 接口层
├── service/                 # 业务层
│   └── impl/
├── mapper/                  # 数据访问层
├── entity/                  # 数据库实体
├── dto/                     # 入参
├── vo/                      # 出参
└── exception/               # 异常

## 命名规范
- 接口路径：/api/v1/{模块}/{资源}
- 管理端前缀：/api/v1/admin/
- 用户端前缀：/api/v1/app/
- 数据库表名：小写下划线，用户端 t_ 前缀，管理端 sys_ 前缀
- Java类：大驼峰
- 方法/变量：小驼峰
- 常量：全大写下划线

## 统一响应格式
{
"code": 200,        // 200成功 其他见错误码表
"msg": "success",
"data": {},
"timestamp": 1234567890
}

## 错误码规范
200   成功
400   参数错误
401   未登录/Token过期
403   无权限
404   资源不存在
429   请求过于频繁
500   服务器内部错误
// 业务错误码从 1000 开始
1001  用户不存在
1002  密码错误
1003  会员已过期
1004  权限不足（非VIP）

## 安全规范
- 密码：BCrypt加密，禁止明文
- 日志：禁止打印密码、Token、手机号完整内容
- 异常：禁止把堆栈信息返回给前端
- 入参：所有Controller入参必须加@Valid校验
- SQL：禁止字符串拼接SQL，全部用MyBatis-Plus或#{}

## 环境配置
- 所有密钥/密码从环境变量读取，禁止硬编码
- 本地开发用 application-dev.yml
- 生产用 application-prod.yml
- .env文件已加入.gitignore

## Flyway规范
- 文件位置：resources/db/migration/
- 命名：V{版本}__{描述}.sql
  例：V1__create_member_table.sql
  V2__add_index_member_phone.sql
- 禁止修改已提交的migration文件
- 每个表一个migration文件

## Redis Key规范
haifeng:{模块}:{标识}
例：
haifeng:token:refresh:{userId}     # Refresh Token
haifeng:member:info:{userId}       # 用户信息缓存
haifeng:limit:api:{ip}:{path}      # 接口限流

## 注意事项
- ID全部用雪花算法生成，禁用数据库自增
- 所有表必须有 created_at / updated_at
- 软删除用 is_deleted = false，禁止物理删除（特殊情况注释说明）
- 金额全部用 DECIMAL(10,2)，禁止float
- 时间字段统一用 TIMESTAMPTZ（带时区）


## 本地开发统一配置
# 数据库配置
DB_HOST=localhost
DB_PORT=5432
DB_NAME=haifeng
DB_USERNAME=postgres
DB_PASSWORD=haifengweilaiguihuayuan

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=haifengweilaiguihuayuan
REDIS_DATABASE=0

# JWT配置（生产环境必须修改，至少32字符）
JWT_ACCESS_SECRET=jwtsecrethaifengweilaiguihuayuan
JWT_REFRESH_SECRET=jwtsecrethaifengweilaiguihuayuan
JWT_ACCESS_EXPIRE=7200
JWT_REFRESH_EXPIRE=604800

# Spring配置
SPRING_PROFILES_ACTIVE=dev

# 服务端口
ADMIN_PORT=8081
APP_PORT=8080

# Spring AI
AI_API_KEY=sk-136e9f42201c4a77b612fe15a34cd72e
AI_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1
