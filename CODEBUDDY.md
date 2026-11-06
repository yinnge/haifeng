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
你是一个拥有10年经验的JAVA高级程序代码质量检测员



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
#### 注意事项
1. 所有的包和功能，都被模块和角色绑定了：@RequireAdminModule
2. SysAdmin 有 @Version 字段（第39行），OptimisticLockerInnerInterceptor 已全局注册。

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



## 特殊情况
algorithm与employment要再次建立子包结构

### DTO/VO 归属原则（重要）

**核心规则：common 只放两端共用的内容，单端专用的放各自模块**

| 情况 | 归属 | 示例 |
|------|------|------|
| 认证相关（两端登录都用） | common/dto/auth/ 或 common/vo/auth/ | LoginDTO, TokenVO |
| 公共基类 | common/dto/common/ | BasePageQueryDTO |
| 只有admin用 | haifeng-admin/dto/{模块}/ | RoleAddDTO, AdminQueryDTO |
| 只有app用 | haifeng-app/dto/{模块}/ | RegisterDTO |


#### 之前遇到的错误总结
| 问题                                                         | 严重度 | 说明                                                                                                                                    |
|------------------------------------------------------------|-----|---------------------------------------------------------------------------------------------------------------------------------------|
| 乐观锁导致 updateById 必失败                                       | 严重  | 高并发登录必然抛出 OptimisticLockException                                                                                                     |
| Token 先发后写 DB 不一致                                          | 严重  | Redis 与 DB 未保证事务一致性                                                                                                                   |
| 日志泄露手机号                                                    | 中   | 违反规范，需脱敏                                                                                                                              |
| refresh 不更新 lastLoginAt                                    | 低   | 审计缺失                                                                                                                                  |
| 失败计数 TOCTOU                                                | 低   | 极端并发可突破锁定限制                                                                                                                           |
| 硬删除悬空                                                      | 中   | hardDeleteById 可能引入孤儿引用                                                                                                               |
| 接口频率限制缺失                                                   | 中   | 无 IP 级别限流                                                                                                                             |
| 事务缺失                                                       | 中   | 缺少事务                                                                                                                                  |
| HTTP DELETE+Body                                           | 中   | 批量删除改用 @PostMapping，兼容所有代理/网关                                                                                                         |
| page() 和 detail()用 BeanUtils.copyProperties性能差             | 中   | 使用 Spring BeanUtils 反射复制，性能较差。建议改用手动 set 或 MapStruct                                                                                  |
| ids 字段缺少 @NotEmpty                                         | 中   | 当 type=ids 时 ids 应必填，但注解只标了 @NotNull 而非 @NotEmpty，空列表可通过校验                                                                            |
| dto与entity类型不符合                                            | 中   | dto类型与数据库字段类型不一致，导致转换失败                                                                                                               |
| @RequestMapping 混用                                         | 中   | @RequestMapping 混用                                                                                                                    |
| 缺少@Valid混用                                                 | 中   | @NotEmpty 标注在 @RequestBody 参数上，但缺少 @Valid 注解，Spring MVC 不会触发校验。空列表 [] 会直接进入 Service 层。虽然 Service 有手动兜底检查，但注解形同虚设，且不一致。                |
| ids 列表没有 @Size 限制                                          | 中   | @Size(max = 100)                                                                                                                      |
| LIKE 查询无长度校验                                               | 中   | @Size(max = 50)                                                                                                                       |
| 缺少@Validated                                               | 中   | 以防比如：当前 @Valid 仅对 @RequestBody 生效。若后续需对 @PathVariable 做校验（如 @Min(1)）                                                                  |
| 未记日志@OperationLog                                          | 低   | 记录日志                                                                                                                                  |
| detail 返回已删除资源无提示                                          | 低   | 管理员端可以查看，但是提示已删除                                                                                                                      |
| countByUnique 在 year 为 NULL 时失效                            | 中   | year 是 Short，可为 null。当 year = null 时，SQL 变成 year = NULL（而非 year IS NULL），在 SQL 语义中 NULL = NULL 永远为 false。这意味着允许创建多条 year = null 的重复记录。 |
| 日志打印完整 ID 列表                                               | 中   | 若删除上千条记录，日志行会冗余，建议截断打印或只打印数量。                                                                                                         |
| update() 的 keyChanged 存在 NPE 风险                            | 中   | DTO 字段都有 @NotNull/@NotBlank，但 entity 是从 DB 查出来的。若 DB 数据异常（如被直写）导致某字段为 null，.equals() 将抛出 NPE。                                         |
| update() 中用 BeanUtils.copyProperties 会将 DTO 中未传的字段覆盖为 null | 中   | DTO 中都是可选的（无 @NotNull）。如果修改时前端只传了部分字段，那些没传的字段会被覆盖为 null，导致已有配置丢失。                                                                     |
| SnowflakeIdGenerator 调用方式不一致                               | 中   | 雪花算法是静态调用的                                                                                                                            |
| 外键引用未校验                                                    | 高   | 掩盖数据不一致                                                                                                                               |
| 硬删除无外键引用                                                   | 高   | 未被其他表引用的风险未评估就直接硬删除                                                                                                                   |
| 应用 @NotBlank 而非 @NotNull（空串可绕过）                            | 中   | 应用 @NotBlank 而非 @NotNull（空串可绕过）                                                                                                       |
| 幂等防重复下单	                                                   | 中   | 佣金处理无幂等 — 同一笔订单被重复调用（如手动重试）会重复给推荐人加余额、重复创建佣金记录                                                                                        |
| SystemSettings 去重查询                                        | 中   | SystemSettings 重复查询 — calculateAmount 查一次，processCommission 又查一次，同一次调用查了两次                                                            |                 
| VIP→PRO 降级保留时长	                                            | 中   | 降级场景也支持从原到期时间叠加（改为 if (isExpired) { from now } else { from beforeExpireAt }，不再与 isTypeChange 绑定）                                      |
| 恢复订单校验	                                                    | 中   | 恢复操作无业务校验 — 恢复订单后没有验证该订单所属会员是否仍有效，可能恢复一个过期订单导致状态不一致                                                                                   |
|Mapper XML 中 subjects && 和 @> 运算符对空数组的边界行为| 高   | Mapper XML 中 subjects && 和 @> 运算符对空数组的边界行为                                                                                            |
|如果 t_major 改了专业名，关联表里的 major_name 不会自动更新| 高   | 修改源数据库表，关联表的不会修改                                                                                                                      |



## 规范

### mybatis-plus枚举映射规范
1.不足: 系统里有大量的状态位（会员状态、订单状态、审核状态等），如果在代码里全用 Integer 魔法数字会极难维护。
解决: MyBatis-Plus 的枚举映射功能，所有状态位强制使用 Java Enum，并加上 @EnumValue 注解。
2.使用page的时候，在 haifeng-common 中定义一个 BasePageQueryDTO，包含 page 和 size，所有列表查询 DTO 继承它
3.⚠️ 唯一要守住的规矩：应用 JVM 还在跑的时候，不要执行 mvn clean / mvnw clean / IDE 的 Rebuild / Build Project。本项目没引入 spring-boot-devtools，没有安全热更新机制，磁盘改写和运行时类加载一旦撞车，就会再次出现 $Builder 缺失这类 NoClassDefFoundError。
### 补充异常拦截规范
- **参数校验**：所有抛出的 `MethodArgumentNotValidException` 必须在 `GlobalExceptionHandler` 中被捕获，并将具体的字段校验错误信息提取后，封装成统一响应格式的 `msg` 返回。


### 数据库规范

## 日志规范
### Excel 导入
前端不用每次都传全部可选字段，留空即可。
校验和插入分离：第一轮只校验，不 insert
校验失败 = 全部不插入（语义清晰：要么全成功，要么全失败）
不再有部分成功部分失败的混乱状态
错误信息仍然需要限制条数
接口不返回成功个数
每个字段都要做长度和类型限制
类型只能是xlsx或xls

### EasyExcel 使用注意事项（2026-09-02 踩坑实证，EasyExcel 4.0.3）
1. **绝不裸读（无 head class 的 doReadSync）**：`EasyExcel.read(is).sheet(name).doReadSync()` 不指定 `.head(XxxDTO.class)` 时，每行返回的是 `LinkedHashMap<Integer, String>`（列索引→单元格值），**不是 `List<List<String>>`**。用 `instanceof List` 接收永远为 false → 数据静默全空（不报错！）。已实证于 `ModelBuildEventListener.buildNoModel` 字节码。
2. **无 head class 时表头行会被吞掉**：默认 `headRowNumber=1`，第一行表头被当作 head 消费、不出现在数据结果里。若确需裸读并自行解析表头，必须链式加 `.headRowNumber(0)`，否则 `rows.get(0)` 会错位。
3. **裸读时列可能空洞**：Map 中 key 不连续，需按最大列索引建行、逐 key 回填（参考 `UniversityGuideServiceImpl.readSheetRows` / `DepartmentServiceImpl.readSheetRows` 的 Map 分支实现）。
4. **Sheet 名按名读取失败不报错**：`.sheet("名字")` 找不到时 EasyExcel 静默返回空列表（假成功 total=0）。导入前应枚举真实 sheet 名做存在性校验（参考 `UniversityGuideServiceImpl.listSheetNames`，POI `WorkbookFactory`），找不到就报 400 并列出文件中全部 sheet 名。
5. **导入主表 Sheet 名写死为「适应指南-主表」**（UniversityGuideServiceImpl.MAIN_SHEET_NAME），改 Excel 模板 sheet 名必须同步改代码常量。
6. **新写导入代码一律走 `.head(DTO.class)`**；只有"表头不定、每列含义由首行决定"的矩阵式分类 Sheet 才允许裸读，且必须复用 `readSheetRows` 的 Map 兼容实现，禁止再写 `instanceof List` 单分支版本。

### 教训
1. 数据库不使用flyway
2. 限制条件和枚举放在service检测

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
用到page展示列表，提供列表页的分页参数：10，20，30，50，100

### 必须打印日志的场景
- 用户登录/登出
- 管理员操作（增删改）
- 调用外部接口（Spring AI等）
- 异常捕获处（GlobalExceptionHandler里统一打印）
- 定时任务开始/结束

### 不需要打印日志的场景
- 普通查询列表/详情（太频繁）
- 简单的getter/setter

### 之前遇到的问题
我之前遇到的问题:并发修改导致数据互相覆盖,硬删除导致孤儿引用

## AOP规范

### 已有AOP切面（haifeng-common中）
- 权限校验切面：处理 @RequireLogin @RequirePro @RequireVip 注解
- 操作日志切面：管理员的增删改操作自动记录到 sys_operation_log


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

