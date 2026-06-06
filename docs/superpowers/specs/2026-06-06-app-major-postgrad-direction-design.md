# C 端专业-考研方向关联查询设计规格

> 日期：2026-06-06
> 对应需求：`haifeng-app/Need/12本科专业-考研方向管理.md`（任务1）
> 父模块：C 端专业管理（app/major）

---

## 1. 概述

在 C 端为本科专业列表、考研专业列表补充「关联对端」的嵌套查询接口。前端拿到列表中的 `id` 后，调用本模块的 2 个嵌套资源接口，渲染「本专业可考的考研方向」/「可考本考研方向的本科专业」。

**不修改任何 admin 端**（admin 端已有完整 CRUD，见 `docs/superpowers/specs/2026-05-09-major-postgrad-direction-design.md`）。

---

## 2. 接口清单

| # | URL | 权限 | 说明 |
|---|---|---|---|
| 接口1 | `GET /api/v1/app/major/{majorId}/postgrad-directions` | `@RequirePro` | 给定本科专业 id，分页返回关联的考研专业（id + postgradMajorName） |
| 接口2 | `GET /api/v1/app/postgrad-major/{postgradMajorId}/undergraduate-majors` | `@RequirePro` | 给定考研专业 id，分页返回关联的本科专业（id + majorName） |

两个接口均：
- 无 DTO 筛选参数
- 走默认分页（Controller 直接复用 `BasePageQueryDTO` 接收 page/size）
- 排序：`mpd.sort_order ASC, 对端.id DESC`
- 关联对端 `status=1` 过滤

---

## 3. 字段定义

### 3.1 PostgradMajorDirectionBriefVO（接口1 返回）

| 字段 | 类型 | 来源 | 说明 |
|---|---|---|---|
| id | Long | t_postgrad_major.id | 考研专业 id |
| postgradMajorName | String | t_postgrad_major.major_name | 考研专业名称 |

### 3.2 UndergraduateMajorDirectionBriefVO（接口2 返回）

| 字段 | 类型 | 来源 | 说明 |
|---|---|---|---|
| id | Long | t_major.id | 本科专业 id |
| majorName | String | t_major.major_name | 本科专业名称 |

---

## 4. URL / 请求 / 响应

### 4.1 接口1

```
GET /api/v1/app/major/{majorId}/postgrad-directions?page=1&size=10
```

| 参数 | 位置 | 类型 | 必填 | 默认 | 说明 |
|---|---|---|---|---|---|
| majorId | path | Long | 是 | — | 本科专业 id |
| page | query | int | 否 | 1 | 页码（`@Min(1)`） |
| size | query | int | 否 | 10 | 每页条数（`@Min(10) @Max(1000)`） |

Controller 签名：
```java
@RequirePro
@GetMapping("/{majorId}/postgrad-directions")
public R<IPage<PostgradMajorDirectionBriefVO>> postgradDirections(
        @PathVariable Long majorId,
        @Valid BasePageQueryDTO dto) {
    return R.ok(majorService.postgradDirections(majorId, dto));
}
```

无 DTO 筛选参数，直接复用 `BasePageQueryDTO`。

**响应**：
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      { "id": 1234567890, "postgradMajorName": "计算机科学与技术" }
    ],
    "total": 5,
    "size": 10,
    "current": 1
  }
}
```

### 4.2 接口2

```
GET /api/v1/app/postgrad-major/{postgradMajorId}/undergraduate-majors?page=1&size=10
```

| 参数 | 位置 | 类型 | 必填 | 默认 | 说明 |
|---|---|---|---|---|---|
| postgradMajorId | path | Long | 是 | — | 考研专业 id |
| page | query | int | 否 | 1 | 页码（`@Min(1)`） |
| size | query | int | 否 | 10 | 每页条数（`@Min(10) @Max(1000)`） |

Controller 签名：
```java
@RequirePro
@GetMapping("/{postgradMajorId}/undergraduate-majors")
public R<IPage<UndergraduateMajorDirectionBriefVO>> undergraduateMajors(
        @PathVariable Long postgradMajorId,
        @Valid BasePageQueryDTO dto) {
    return R.ok(postgradMajorService.undergraduateMajors(postgradMajorId, dto));
}
```

无 DTO 筛选参数，直接复用 `BasePageQueryDTO`。

**响应**：
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      { "id": 1234567891, "majorName": "计算机科学与技术" }
    ],
    "total": 3,
    "size": 10,
    "current": 1
  }
}
```

---

## 5. 文件结构

### 5.1 haifeng-common（最小改动）

```
mapper/major/
└── MajorPostgradDirectionMapper.java   + selectPostgradMajorsByMajorId()
                                        + selectMajorsByPostgradMajorId()
```

`MajorPostgradDirection` 实体已存在（不动）。仅在 `MajorPostgradDirectionMapper` 接口中追加 2 个方法。

### 5.2 haifeng-app（新增/改）

```
controller/major/
├── MajorController.java                + 接口1 endpoint
└── PostgradMajorController.java        + 接口2 endpoint

service/major/
├── MajorService.java                   + 接口1 service 方法签名
└── PostgradMajorService.java           + 接口2 service 方法签名

service/impl/major/
├── MajorServiceImpl.java               + 接口1 实现
└── PostgradMajorServiceImpl.java       + 接口2 实现

vo/major/
├── PostgradMajorDirectionBriefVO.java  NEW（接口1 返回）
└── UndergraduateMajorDirectionBriefVO.java  NEW（接口2 返回）

注：两个接口均无 DTO 筛选参数，Controller 直接复用 `BasePageQueryDTO`（位于 haifeng-common），**不新建 DTO**。

test/  （新增）
service/major/
├── MajorServiceImplTest.java           + 接口1 单测
└── PostgradMajorServiceImplTest.java   + 接口2 单测
```

---

## 6. 关键 SQL

### 6.1 selectPostgradMajorsByMajorId（接口1）

```java
@Select("SELECT pm.id AS id, pm.major_name AS postgradMajorName " +
        "FROM t_major_postgrad_direction mpd " +
        "JOIN t_postgrad_major pm ON pm.id = mpd.postgrad_major_id " +
        "WHERE mpd.major_id = #{majorId} AND pm.status = 1 " +
        "ORDER BY mpd.sort_order ASC, pm.id DESC")
IPage<Map<String, Object>> selectPostgradMajorsByMajorId(Page<?> page,
        @Param("majorId") Long majorId);
```

走 `idx_mpd_major` (major_id) 索引 → 主键回表 `t_postgrad_major` → status 过滤。

### 6.2 selectMajorsByPostgradMajorId（接口2）

```java
@Select("SELECT m.id AS id, m.major_name AS majorName " +
        "FROM t_major_postgrad_direction mpd " +
        "JOIN t_major m ON m.id = mpd.major_id " +
        "WHERE mpd.postgrad_major_id = #{postgradMajorId} AND m.status = 1 " +
        "ORDER BY mpd.sort_order ASC, m.id DESC")
IPage<Map<String, Object>> selectMajorsByPostgradMajorId(Page<?> page,
        @Param("postgradMajorId") Long postgradMajorId);
```

走 `idx_mpd_postgrad` (postgrad_major_id) 索引 → 主键回表 `t_major` → status 过滤。

---

## 7. Service 实现要点

参照 `PostgradMajorServiceImpl.universities()` / `toUniversityBriefVO()`（任务4接口1 已存在）。

**接口1** — `MajorServiceImpl.postgradDirections(Long majorId, BasePageQueryDTO dto)`：
```java
Page<Map<String, Object>> page = new Page<>(dto.getPage(), dto.getSize());
IPage<Map<String, Object>> mapPage =
    majorPostgradDirectionMapper.selectPostgradMajorsByMajorId(page, majorId);
return mapPage.convert(row -> PostgradMajorDirectionBriefVO.builder()
    .id(row.get("id") != null ? ((Number) row.get("id")).longValue() : null)
    .postgradMajorName(row.get("postgradMajorName") != null
        ? String.valueOf(row.get("postgradMajorName")) : null)
    .build());
```

**接口2** — `PostgradMajorServiceImpl.undergraduateMajors(Long postgradMajorId, BasePageQueryDTO dto)`：
结构同上，反向调用 `selectMajorsByPostgradMajorId`，转 `UndergraduateMajorDirectionBriefVO`（字段 `id`、`majorName`）。

**Controller 直接调用 service，参数透传**，无业务逻辑。

---

## 8. 错误码

| 场景 | HTTP | code | msg |
|---|---|---|---|
| 路径 id 缺失 / 类型错 | 400 | — | Spring 自动校验 |
| `page` / `size` 越界 | 400 | — | `BasePageQueryDTO` 已有 `@Min(1)` 校验 |
| 未登录 | 401 | — | 拦截器 |
| 已登录但非 Pro/Vip | 403 | — | `@RequirePro` 拦截 |
| `majorId` / `postgradMajorId` 在主表不存在 | 200 | 200 | 返回空 Page（JOIN 无结果） |
| 父专业存在但无任何关联 | 200 | 200 | 返回空 Page |

**不抛 404**：与现有 `/{id}/universities` / `/{universityId}/postgrad-majors` 语义一致 — 关联空结果属于正常情况。

---

## 9. 测试

参照 `IndustryServiceImplTest`（Mockito + `@InjectMocks` + `LambdaQueryWrapper` 断言）。

**Service 单测覆盖**：
- mapper 方法被以正确参数调用（`page` 对象 + path id）
- 返回 Map 列表被正确转 VO（id 字段、name 字段）
- 空 Map 列表 → 空 VO Page（不抛异常）
- id/name 为 null 的 Map 行 → VO 字段为 null（不 NPE）

**不写 Controller 集成测试**：现有 C 端 controller 没有集成测试，保持一致。

---

## 10. 关键约束

1. 严格按现有 `PostgradMajorUniversityController` 模式实现，认知负担最小
2. 关联对端 `status=1` 过滤；关联表 `t_major_postgrad_direction` 无 status 字段，不需过滤
3. 不引入 Redis 缓存，实时读库
4. id 字段裸 `Long` 返回（跟现有 VO 风格一致）
5. 不调用 git（最后由用户统一提交）
6. VO 类加 `@Builder` / `@Data` / `@NoArgsConstructor` / `@AllArgsConstructor` / `implements Serializable`（与现有 VO 一致）
7. service 注入用 `@RequiredArgsConstructor`（与现有 service 一致）
8. URL 命名：`postgrad-directions` / `undergraduate-majors`（与现有 `universities` / `postgrad-majors` 同形）
9. 接口1 controller 加在 `MajorController`，接口2 加在 `PostgradMajorController`（不新建 controller 类）
10. 接口1 service 方法加在 `MajorService` / `MajorServiceImpl`；接口2 加在 `PostgradMajorService` / `PostgradMajorServiceImpl`
