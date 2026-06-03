# App 端首页展示模块设计（公告 / 规划师 / 培训机构）

- **日期**：2026-06-03
- **模块**：`haifeng-app` 首页管理（home）
- **需求文档**：`haifeng-app/Need/6公告展示.md`
- **范围**：C 端只读展示接口 + Redis 缓存

## 1. 背景与目标

C 端首页需要展示三类运营内容：公告、规划师、培训机构。所有接口公开访问（不需要登录、不区分会员等级），数据由 admin 端运营人员维护。

**核心目标**：
- 提供 6 个 RESTful 只读接口（3 个分页列表 + 3 个详情）
- 引入 Redis 缓存降低数据库压力（数据不常变）
- 严格隔离 C/Admin 端模块边界，admin 端写流程零改动

**非目标**：
- 不做缓存主动失效（admin 改后允许 ≤30 分钟延迟生效）
- 不做点赞/收藏/评论（后续模块）
- 不引入新中间件

## 2. 现状摸排

| 已具备 | 位置 |
|--------|------|
| Entity：`Announcement` / `Planner` / `Institution` | `haifeng-common/entity/home/` |
| Mapper：三张表对应 Mapper | `haifeng-common/mapper/home/` |
| Admin 端完整 CRUD（含 ListVO / DetailVO / QueryDTO / Controller / Service） | `haifeng-admin/.../home/` |
| `RedisTemplate<String, Object>` + JSON 序列化（含 JavaTime） | `haifeng-common/config/RedisConfig.java` |
| `RedisKeyConstant` 集中管理 key 前缀 | `haifeng-common/constant/RedisKeyConstant.java` |
| `BasePageQueryDTO`：page≥1, size∈[10,1000] | `haifeng-common/dto/common/` |
| `ProvinceEnum`：用中文 `desc` 校验（`isValid(String)`） | `haifeng-common/enums/ProvinceEnum.java` |
| C 端首页已有公开接口示例 | `haifeng-app/controller/home/SiteController.java` |

App 端目前 `controller/home/` 仅 `SiteController`，`service/home/`、`vo/home/`、`dto/home/` 目录均为空白或缺失。

## 3. 接口契约

所有接口前缀 `/api/v1/app/home/`，**公开访问**（不加 `@RequireLogin`/`@RequirePro`/`@RequireVip`），返回统一 `R<T>`。

### 3.1 公告（announcement）

| 方法 | 路径 | 入参 | 出参 |
|------|------|------|------|
| GET | `/announcements` | `page`（默认 1）、`size`（默认 10）、`tag`（可选，精确匹配） | `R<IPage<AnnouncementListVO>>` |
| GET | `/announcements/{id}` | path `id` | `R<AnnouncementDetailVO>` |

### 3.2 规划师（planner）

| 方法 | 路径 | 入参 | 出参 |
|------|------|------|------|
| GET | `/planners` | `page`、`size`、`region`（可选，必须是 `ProvinceEnum` 的中文 `desc`） | `R<IPage<PlannerListVO>>` |
| GET | `/planners/{id}` | path `id` | `R<PlannerDetailVO>` |

列表按 `sort_order ASC, id DESC` 排序。

### 3.3 培训机构（institution）

| 方法 | 路径 | 入参 | 出参 |
|------|------|------|------|
| GET | `/institutions` | `page`、`size` | `R<IPage<InstitutionListVO>>` |
| GET | `/institutions/{id}` | path `id` | `R<InstitutionDetailVO>` |

列表按 `sort_order ASC, id DESC` 排序。

### 3.4 通用规则

- 所有查询强制 `status = 1`（C 端只看展示中）
- `is_deleted=true` 由 `@TableLogic` 自动过滤
- 详情不存在 / `status=0` → 抛 `BusinessException(ResultCode.NOT_FOUND, "<资源>不存在")`
- `region` 不在 `ProvinceEnum` 中 → 抛 `BusinessException(ResultCode.BAD_REQUEST, "无效的省份")`
- `page`/`size` 校验由 `BasePageQueryDTO` 的 `@Min/@Max` 完成，错误经 `GlobalExceptionHandler` 统一返回

## 4. VO / DTO 字段

### 4.1 列表 VO

```
AnnouncementListVO     { id, title, tag, updatedAt }
PlannerListVO          { id, name, region, position, avatar, specialty, personalDescription }
InstitutionListVO      { id, name, type, description, images }
```

> 文本字段（`personalDescription`、`description`）**原样返回**不做后端截断，由前端 CSS 处理省略号。`images` 数组原样返回。

### 4.2 详情 VO

```
AnnouncementDetailVO   { id, title, content, tag }
PlannerDetailVO        { id, name, position, region, avatar, specialty,
                         douyinName, douyinUrl, personalDescription, experienceJob,
                         achievements, expertiseAreas }
InstitutionDetailVO    { id, name, type, phone, address, description, courses, images, logo }
```

### 4.3 QueryDTO

```
AnnouncementQueryDTO extends BasePageQueryDTO   { tag        }
PlannerQueryDTO      extends BasePageQueryDTO   { region     }
InstitutionQueryDTO  extends BasePageQueryDTO   { /* 无业务字段 */ }
```

> `InstitutionQueryDTO` 虽无业务字段但仍单独建立，保持三模块对称，未来扩展筛选条件时无需改 Controller 签名。

## 5. 文件清单

### 5.1 修改

- `haifeng-common/constant/RedisKeyConstant.java`：追加 6 个 key 前缀、TTL 常量、6 个 key 构造方法

### 5.2 新增（共 19 个文件）

```
haifeng-common/dto/common/
  └── PageCacheDTO.java                    ← 通用分页缓存中转结构

haifeng-app/controller/home/
  ├── AnnouncementController.java
  ├── PlannerController.java
  └── InstitutionController.java

haifeng-app/service/home/
  ├── AnnouncementService.java
  ├── PlannerService.java
  └── InstitutionService.java

haifeng-app/service/impl/home/
  ├── AnnouncementServiceImpl.java
  ├── PlannerServiceImpl.java
  └── InstitutionServiceImpl.java

haifeng-app/dto/home/
  ├── AnnouncementQueryDTO.java
  ├── PlannerQueryDTO.java
  └── InstitutionQueryDTO.java

haifeng-app/vo/home/
  ├── AnnouncementListVO.java
  ├── AnnouncementDetailVO.java
  ├── PlannerListVO.java
  ├── PlannerDetailVO.java
  ├── InstitutionListVO.java
  └── InstitutionDetailVO.java
```

## 6. 缓存设计

### 6.1 Key 规范（追加到 `RedisKeyConstant`）

```java
public static final String HOME_ANNOUNCEMENT_LIST_PREFIX   = "haifeng:app:home:announcement:list:";
public static final String HOME_ANNOUNCEMENT_DETAIL_PREFIX = "haifeng:app:home:announcement:detail:";
public static final String HOME_PLANNER_LIST_PREFIX        = "haifeng:app:home:planner:list:";
public static final String HOME_PLANNER_DETAIL_PREFIX      = "haifeng:app:home:planner:detail:";
public static final String HOME_INSTITUTION_LIST_PREFIX    = "haifeng:app:home:institution:list:";
public static final String HOME_INSTITUTION_DETAIL_PREFIX  = "haifeng:app:home:institution:detail:";

public static final long   HOME_CACHE_TTL_MINUTES = 30L;

// 构造函数（示例）
public static String getAnnouncementListKey(int page, int size, String tag) {
    return HOME_ANNOUNCEMENT_LIST_PREFIX + "p=" + page + ":s=" + size + ":tag=" + (tag == null ? "" : tag);
}
public static String getAnnouncementDetailKey(Long id) {
    return HOME_ANNOUNCEMENT_DETAIL_PREFIX + id;
}
public static String getPlannerListKey(int page, int size, String region)   { ... }
public static String getPlannerDetailKey(Long id)                           { ... }
public static String getInstitutionListKey(int page, int size)              { ... }
public static String getInstitutionDetailKey(Long id)                       { ... }
```

### 6.2 `PageCacheDTO<T>`（新增）

`MyBatis-Plus` 的 `Page<T>` 直接 JSON 序列化稳定性较差（依赖运行时类型推断），引入中转结构：

```java
package com.haifeng.common.dto.common;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageCacheDTO<T> {
    private List<T> records;
    private long    total;
    private long    current;
    private long    size;
}
```

Service 内：
- 写缓存前将 `Page<T>` 转成 `PageCacheDTO<T>`
- 读缓存后再组装回 `Page<T>` 返回（或直接返回 `IPage<T>` 接口）

### 6.3 读写流程（每个查询方法统一套路）

```
1. 构造 cacheKey
2. Object cached = redisTemplate.opsForValue().get(cacheKey)
3. 命中 → 转回目标类型直接返回
4. 未命中 → 查 DB → 写入 redis（30 分钟 TTL）→ 返回
```

### 6.4 关键约定

| 项目 | 决定 |
|------|------|
| TTL | 30 分钟，被动过期，不主动失效 |
| 空列表 | 同样缓存（防穿透） |
| 详情不存在 | **不缓存** `null`，直接抛 404（流量小，可接受） |
| Redis 故障 | 不吞异常，让 `RedisConnectionFailureException` 冒泡到全局处理器 |
| Admin 改动同步 | 最长 30 分钟延迟，不在本期处理 |

## 6.5 region 校验

`PlannerServiceImpl` 在分页查询起始处：

```java
if (queryDTO.getRegion() != null && !ProvinceEnum.isValid(queryDTO.getRegion())) {
    throw new BusinessException(ResultCode.BAD_REQUEST, "无效的省份");
}
```

`ProvinceEnum.isValid(null)` 已返回 true，理论上判 null 是冗余的；保留显式 null 检查以让意图更直白。

**校验顺序**：region 校验必须放在缓存读取之前——非法参数不应消耗缓存槽位、不应被记到任何 key 上。

## 7. 错误处理与日志

### 7.1 错误处理

| 场景 | 处理 |
|------|------|
| 详情 id 不存在 / `status=0` | `BusinessException(NOT_FOUND, "<资源>不存在")` |
| region 非法 | `BusinessException(BAD_REQUEST, "无效的省份")` |
| page/size 越界 | `BasePageQueryDTO` 注解校验 → `GlobalExceptionHandler` |
| Redis 异常 | 冒泡，不降级（本期不引入降级逻辑） |

### 7.2 日志（每个 ServiceImpl 加 `@Slf4j`）

| 场景 | 级别 |
|------|------|
| 列表/详情普通查询 | **不打日志**（遵守 CLAUDE.md） |
| 缓存命中/未命中 | `log.debug` |
| region 非法 / 详情 404 | 不单独打（业务异常由全局处理器统一记录） |

## 8. 测试策略

### 8.1 单元测试（Mockito）

落地路径：`haifeng-app/src/test/java/com/haifeng/app/service/home/`

| 测试类 | 用例 |
|--------|------|
| `AnnouncementServiceImplTest` | 列表-缓存命中直返 / 列表-未命中查 DB 并写缓存 / 列表-带 tag 筛选 / 详情-存在 / 详情-不存在抛 404 / 详情-status=0 抛 404 |
| `PlannerServiceImplTest` | 上面所有用例 + region 合法 / region 非法抛 400 / region=null 不过滤 |
| `InstitutionServiceImplTest` | 列表/详情常规用例 |

Mock：对应 Mapper、`RedisTemplate`、`ValueOperations`。

### 8.2 手动验证清单（实现完毕后跑一遍）

1. `curl /api/v1/app/home/announcements` → 200，结构正确
2. 二次请求 → `redis-cli KEYS "haifeng:app:home:*"` 能看到 key
3. `curl /api/v1/app/home/planners?region=北京` → 200
4. `curl /api/v1/app/home/planners?region=火星` → 400「无效的省份」
5. `curl /api/v1/app/home/announcements/999999` → 404「公告不存在」
6. Admin 改一条数据 → 30 分钟内 C 端仍是旧值（验证 TTL）

### 8.3 集成测试

本期**不写**。手动验证全部通过后视情况补。

## 9. 风险与权衡

| 风险 | 缓解 |
|------|------|
| Admin 改动 ≤30 分钟才能 C 端可见 | 已与产品对齐为可接受；如未来要求更短可缩短 TTL 或后续做主动失效 |
| 不同分页/筛选组合各占 key，缓存项较多 | 数据量小（顶多几百条 × 几十种组合），Redis 内存压力可忽略 |
| Redis 故障导致接口 500 | 当前不做降级；后续可在 Service 加 try/catch 退化为直查 DB |
| `Page<T>` 序列化通过 `PageCacheDTO` 中转，多一次对象拷贝 | 数据量小，性能影响可忽略；换来序列化稳定 |

## 10. 实现里程碑

1. `RedisKeyConstant` 追加 + 新增 `PageCacheDTO`
2. 公告模块（DTO/VO/Service/Controller + 单测）
3. 规划师模块（含 region 校验）
4. 培训机构模块
5. 手动验证 6 项 + 单测全绿
