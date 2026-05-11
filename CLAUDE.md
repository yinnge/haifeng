# 海峰未来规划院 - 开发规范

## 项目简介
教育规划平台，核心功能：高考志愿填报

### 访问权限分级
| 角色      | member_type值 | 说明                 |
|---------|--------------|--------------------|
| normal  | normal       | 具体权限在具体模块说明        |
| Pro     | Pro          | 具体权限在具体模块说明       |
| VIP会员   | vip          | 具体权限在具体模块说明  |


## 角色
你是一个JAVA高级程序员，根据需求进行以下操作

## 管理员后台界面：【限制管理员才能看到】
后台模块有：
1. 系统管理[父模块]--控制面板[子模块]--系统设置[子模块]--操作记录[子模块]--AI调用记录[子模块]
2. 首页管理[父模块]--公告列表[子模块]--规划师列表[子模块]--培训机构列表[子模块]
3. 权限管理[父模块]--角色列表[子模块]--权限列表[子模块]--管理员列表[子模块]
4. 用户管理[父模块]--用户列表[子模块]--订单/续费记录[子模块]--推荐佣金列表[子模块]--消息通知[子模块]--提现记录[子模块]
5. 院校管理[父模块]--院校列表[子模块]--校园图册[子模块]--院校适应指南[子模块]--院系详情[子模块]--实验室列表[子模块]--学科评估[子模块]
6. 资源管理[父模块]--资源列表[子模块]
7. 专业管理[父模块]--专业列表[子模块]--专业详情[子模块]--考研专业[子模块]
8. 城市管理[父模块]--城市列表[子模块]
9. 行业管理[父模块]--行业列表[子模块]
10. 专业组管理[父模块]--专业组录取列表[子模块]--专业组明细列表[子模块]--选科要求列表[子模块]
11. 算法配置管理[父模块]--建省份改革配置[子模块]--院校标签字典[子模块]--一分一段位次[子模块]--批次分数线[子模块]
12. 算法约束模块[父模块]--约束字典[子模块]--专业约束关联[子模块]--安全系数[子模块]
13. 特殊通道模块[父模块]--特殊招生通道列表[子模块]--通道详情[子模块]--通道-大学[子模块]--强基计划列表[子模块]
14. 竞赛证书管理[父模块]--科研竞赛列表[子模块]--职业技能证书列表[子模块]
15. 企业管理[父模块]--企业列表[子模块]--企业行业关联[子模块]
16. 公务员考试管理[父模块]--公务员考试列表[子模块]--公告列表[子模块]--备考指南列表[子模块]
17. 事业编管理[父模块]--岗位列表[子模块]--公告列表[子模块]--备考指南列表[子模块]
18. 部队文职管理[父模块]--岗位列表[子模块]--公告列表[子模块]--备考指南列表[子模块]
19. 选调生管理[父模块]--岗位列表[子模块]--公告列表[子模块]--备考指南列表[子模块]
20. 教师招聘管理[父模块]--岗位列表[子模块]--公告列表[子模块]--备考指南列表[子模块]
21. 医疗卫生招聘管理[父模块]--岗位列表[子模块]--公告列表[子模块]--备考指南列表[子模块]
22. 银行/金融系统招聘管理[父模块]--岗位列表[子模块]--公告列表[子模块]--备考指南列表[子模块]
23. 基层服务管理[父模块]--岗位列表[子模块]--公告列表[子模块]--备考指南列表[子模块]
24. 社区工作管理[父模块]--岗位列表[子模块]--公告列表[子模块]--备考指南列表[子模块]
25. 公益性岗位管理[父模块]--岗位列表[子模块]--公告列表[子模块]--备考指南列表[子模块]


## 技术栈
- 后端：Spring Boot 3.x + MyBatis-Plus + Spring Security + Spring AI
- 数据库：PostgreSQL + Redis
- 认证：JWT 双Token（Access 2h + Refresh 7d）
- 数据库迁移：Flyway
- 构建：Maven
- 部署：Docker + Traefik
- ID生成：雪花算法

## 后端结构规范

### 核心原则
- Entity 和 Mapper 统一放 haifeng-common，两端共用
- controller / service / dto / vo 按功能模块分子包
- 禁止把所有类平铺在同一层目录下

### 模块子包命名
| 模块 | 子包名 |
|----|--|
| 认证 | auth |
| 系统 | system |
| 首页管理 | home |
| 权限管理 | permission |
| 用户管理 | user |
| 院校 | university |
| 资源 | resource |
| 专业 | major |
| 城市 | city |
| 行业 | industry |
| 高考算法 | algorithm |
| 特殊通道 |  special |
| 证书竞赛 | certificate |
| 企业 | company |
| 就业 | employment |


### 包结构示意
>除了employment和algorithm的多层结构

com.haifeng.admin/
├── controller/{模块名}/XxxController.java
├── service/{模块名}/XxxService.java
├── service/impl/{模块名}/XxxServiceImpl.java
├── dto/{模块名}/XxxAddDTO.java
└── vo/{模块名}/XxxListVO.java
（注：admin端绝不包含entity和mapper文件夹）
com.haifeng.common/
├── annotation/                    ← 自定义注解（@RequireLogin @RequirePro @RequireVip @OperationLog）
├── aspect/                        ← AOP切面（权限校验、操作日志）
├── config/                        ← 通用配置（MyBatis-Plus、Redis等）
├── constant/                      ← 常量定义（RedisKey等）
├── dto/auth/                      ← 认证相关DTO（两端共用：LoginDTO、RefreshTokenDTO）
├── dto/common/                    ← 公共基类（BasePageQueryDTO）
├── entity/{模块名}/Xxx.java       ← 核心约束：所有Entity在这里
├── enums/                         ← 枚举（状态、类型等）
├── exception/                     ← 自定义异常（BusinessException）
├── mapper/{模块名}/XxxMapper.java ← 核心约束：所有Mapper在这里
├── response/                      ← 统一响应（R<T>、ResultCode）
├── security/                      ← 安全相关（JWT过滤器、SecurityUtil）
├── util/                          ← 工具类（JwtUtil、SnowflakeIdGenerator）
└── vo/auth/                       ← 认证相关VO（两端共用：TokenVO）

### 结构示例
com.haifeng.admin/
├── config/                          # 配置类（不分子包，数量少）
├── controller/
│   ├── auth/                        # 认证
│   ├── permission/                  # 权限管理（角色/模块/管理员）
│   ├── user/                        # 用户管理
│   ├── home/                        # 首页管理（公告/规划师/机构）
│   ├── university/                  # 院校管理
│   ├── major/                       # 专业管理
│   ├── algorithm/                   # 算法配置
│   ├── employment/                  # 就业管理（公务员/事业编等）
│   └── system/                      # 系统管理
│
├── service/
│   ├── auth/
│   ├── permission/
│   ├── user/
│   ├── home/
│   ├── university/
│   ├── major/
│   ├── algorithm/
│   ├── employment/
│   ├── system/
│   └── impl/
│       ├── auth/
│       ├── permission/
│       ├── user/
│       └── ...                      # 与上面一一对应
│
├── dto/
│   ├── permission/                  # RoleAddDTO / ModuleAddDTO
│   ├── user/                        # MemberQueryDTO
│   ├── home/                        # AnnouncementAddDTO
│   ├── ...

│
└── vo/
├── permission/                  # RoleVO / ModuleTreeVO
├── user/                        # MemberListVO / MemberDetailVO
├── ...                          # 同上


com.haifeng.app/
├── controller/
│   ├── auth/          # 注册/登录
│   ├── member/        # 个人中心/资料/收藏/浏览记录
│   ├── university/    # 院校查询（C端视图）
│   ├── major/         # 专业查询
│   ├── algorithm/     # 志愿方案/高考档案
│   ├── employment/    # 就业信息
│   ├── city/
│   ├── industry/
│   ├── resource/
│   └── certificate/
├── service/ ...       # 同上
├── dto/ ...
└── vo/ ...
（注：app端同样不包含entity和mapper文件夹）

## 特殊情况
algorithm与employment要再次建立子包结构

### algorithm 二级子包
| 后台模块 | 子包名 |
|---------|--------|
| 专业组管理 | admission |
| 算法配置管理 | config |
| 算法约束模块 | constraint |

### employment 二级子包
| 后台模块 | 子包名 |
|---------|--------|
| 公务员考试管理 | civil |
| 事业编管理 | institution |
| 部队文职管理 | military |
| 选调生管理 | selection |
| 教师招聘管理 | teacher |
| 医疗卫生管理 | healthcare |
| 银行金融管理 | finance |
| 基层服务管理 | grassroots |
| 社区工作管理 | community |
| 公益性岗位管理 | welfare |



### 包结构示意
com.haifeng.admin/
├── controller/{模块名}/{子模块名}/XxxController.java
├── service/{模块名}/{子模块名}/XxxService.java
├── service/impl/{模块名}/{子模块名}/XxxServiceImpl.java
├── dto/{模块名}/{子模块名}/XxxAddDTO.java
└── vo/{模块名}/{子模块名}/XxxListVO.java


### DTO/VO 归属原则（重要）

**核心规则：common 只放两端共用的内容，单端专用的放各自模块**

| 情况 | 归属 | 示例 |
|------|------|------|
| 认证相关（两端登录都用） | common/dto/auth/ 或 common/vo/auth/ | LoginDTO, TokenVO |
| 公共基类 | common/dto/common/ | BasePageQueryDTO |
| 只有admin用 | haifeng-admin/dto/{模块}/ | RoleAddDTO, AdminQueryDTO |
| 只有app用 | haifeng-app/dto/{模块}/ | RegisterDTO |

**为什么这样设计？**
- 如果 AdminRoleDTO 放 common，app 模块也能 import，破坏模块边界
- 就算字段相同，admin 和 app 的 DTO 也要分开定义（未来可能各自演进）




## 规范

### VO命名规范
XxxListVO      列表页（字段精简）
XxxDetailVO    详情页（字段完整）
XxxCardVO      卡片展示（首页/推荐）
注意：admin端和app端的VO分别定义，返回字段不同

### mybatis-plus枚举映射规范
1.不足: 系统里有大量的状态位（会员状态、订单状态、审核状态等），如果在代码里全用 Integer 魔法数字会极难维护。
解决: MyBatis-Plus 的枚举映射功能，所有状态位强制使用 Java Enum，并加上 @EnumValue 注解。
2.使用page的时候，在 haifeng-common 中定义一个 BasePageQueryDTO，包含 page 和 size，所有列表查询 DTO 继承它

### 补充异常拦截规范
- **参数校验**：所有抛出的 `MethodArgumentNotValidException` 必须在 `GlobalExceptionHandler` 中被捕获，并将具体的字段校验错误信息提取后，封装成统一响应格式的 `msg` 返回。

### skills使用技巧
1. 在进行明确需求时使用：superpowers的brainstorming
2. 系统性调试使用superpowers的systematic-debugging
3. 测试驱动开发用：superpowers的test-driven-development
4. 计划编写与执行用：superpowers的writing-plans / executing-plans
5. 代码审查使用：superpowers的code-reviewer
6. 完成前验证用：superpowers的verification-before-completion

### 数据库规范

## 日志规范

### 强制要求
- 每个 Service 类必须声明 Logger：
  private static final Logger log = LoggerFactory.getLogger(XXXServiceImpl.class);
  或使用 Lombok：类上加 @Slf4j，直接用 log.xxx()

- 禁止使用 System.out.println()

- 禁止打印敏感信息：
  ❌ log.info("密码：{}", password)
  ❌ log.info("Token：{}", token)
  ❌ log.info("手机号：{}", phone)  ← 如需打印用脱敏：138****0000

### 日志级别使用规范
log.debug()  → 调试信息，开发时看（如：SQL参数、方法入参）
log.info()   → 关键业务节点（如：用户登录成功、订单创建）
log.warn()   → 警告但不影响流程（如：缓存未命中、重试）
log.error()  → 异常和错误，必须带异常对象
log.error("查询用户失败，userId={}", id, e)  ← e要带上！

### 列表规范
用到page展示列表，提供列表页的分页参数：10，20，30，50，100，200，500，1000

### 必须打印日志的场景
- 用户登录/登出
- 管理员操作（增删改）
- 调用外部接口（Spring AI等）
- 异常捕获处（GlobalExceptionHandler里统一打印）
- 定时任务开始/结束

### 不需要打印日志的场景
- 普通查询列表/详情（太频繁）
- 简单的getter/setter

## AOP规范

### 已有AOP切面（haifeng-common中）
- 权限校验切面：处理 @RequireLogin @RequirePro @RequireVip 注解
- 操作日志切面：管理员的增删改操作自动记录到 sys_operation_log

### 操作日志AOP（重要）
管理员端所有写操作（POST/PUT/DELETE）
通过AOP自动记录到操作日志表，无需在业务代码里手动写

自动记录以下信息：
- 操作人ID/姓名（从SecurityContext取）
- 操作时间
- 请求路径
- 操作描述（通过自定义注解 @OperationLog("新增院校") 标注）
- 请求参数（脱敏后）
- 操作结果（成功/失败）

使用方式：
@PostMapping("/add")
@OperationLog("新增院校")   ← 加这个注解，AOP自动记录
public R<Void> add(@Valid @RequestBody UniversityAddDTO dto) {
...
}

### 禁止在业务代码里手动写操作日志
❌ operationLogService.save(new OperationLog(...))  // 不要这样
✅ 加 @OperationLog 注解，AOP自动处理


## 命名规范
- 接口路径：/api/v1/{模块}/{资源}
- 管理端前缀：/api/v1/admin/
- 用户端前缀：/api/v1/app/
- 数据库表名：小写下划线，用户端 t_ 前缀，管理端 sys_ 前缀,业务数据(如大学等): 统一归入 t_
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
1004  权限不足（需要专业版及以上）
1005  权限不足（需要旗舰版）

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
- 文件位置：统一放到:haifeng-admin\src\main\resources\db\migration 
- 命名：V{版本}__{描述}.sql
  例：V1__create_member_table.sql
  V2__add_index_member_phone.sql
- 禁止修改已提交的migration文件


## Redis Key规范
haifeng:{模块}:{标识}
例：
haifeng:token:refresh:{userId}     # Refresh Token
haifeng:member:info:{userId}       # 用户信息缓存
haifeng:limit:api:{ip}:{path}      # 接口限流

## 注意事项
- ID全部用雪花算法生成，禁用数据库自增
- 所有表必须有 created_at / updated_at
- 软删除用 is_deleted = false/status=0
- 允许硬删除和软删除，软删除- 软删除用 is_deleted = false/status=0
- 金额全部用 DECIMAL(10,2)，禁止float
- 时间字段统一用 TIMESTAMPTZ（带时区）


## 本地开发统一配置

详情请看 .env

