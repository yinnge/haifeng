# 志愿方案模块设计 (Tasks 1, 2, 3)

> 日期：2026-06-07
> 模块：haifeng-app / algorithm (志愿方案)
> 状态：待实施
> 范围：Tasks 1, 2, 3 of AL7-1（缓存 + 默认数量接口 + 3 张快照表 + entity/mapper 基础类）

## 1. 概述

### 1.1 功能目标

实现志愿方案（Wish Plan）模块的第一阶段基础设施：
- **Task 1**：暴露一个 C 端接口，读取 `system_settings` 表中 5 个默认数量配置（搏/冲/稳/保/垫），通过 Redis 缓存 24h。
- **Task 2**：创建 3 张快照表（`t_wish_plan`、`t_wish_group_snapshot`、`t_wish_major_snapshot`），用于后续功能（添加志愿、排序、xlsx 导出）持久化数据。
- **Task 3**：基于 3 张表生成对应的 Entity 和 Mapper 公共类。

### 1.2 权限要求

- Task 1 接口需登录，类级别 `@RequireLogin`（与 `GaokaoArchiveController` 一致）。

### 1.3 不在本期范围

以下功能在更大的 AL7 需求文档中提及，但本期（AL7-1 Tasks 1-3）不实现：
- 志愿方案的增删改查
- 专业组/专业明细的添加、移除
- 排序更新
- xlsx 导出（含 SSE 进度推送）
- 计划数量上限的业务校验

## 2. ID 策略

**关键决定（用户已确认）**：

| 表 | ID 类型 | 理由 |
|---|---|---|
| t_wish_plan | SERIAL（自增 INTEGER） | 与 t_admission_group 一致 |
| t_wish_group_snapshot | SERIAL（自增 INTEGER） | 同上 |
| t_wish_major_snapshot | SERIAL（自增 INTEGER） | 同上 |
| t_wish_plan.member_id | **BIGINT** | 必须与 t_member.id（t_member.id 为 BIGSERIAL=BIGINT）一致 |
| t_wish_group_snapshot.plan_id | INTEGER | 引用 t_wish_plan.id |
| t_wish_group_snapshot.group_id | INTEGER | 引用 t_admission_group.id |
| t_wish_major_snapshot.plan_id | INTEGER | 引用 t_wish_plan.id |
| t_wish_major_snapshot.group_snapshot_id | INTEGER | 引用 t_wish_group_snapshot.id |
| t_wish_major_snapshot.major_id | **BIGINT** | 引用 t_admission_major_score.id (SERIAL) 或 t_major.id (BIGINT) |

> **注意**：t_member.id 在数据库中是 `BIGSERIAL`（自动递增），不是雪花算法。这是项目历史遗留不一致（Member 实体声明 `IdType.ASSIGN_ID`）。本期不动 t_member，不在本任务范围。

## 3. 数据库设计

### 3.1 文件位置

`haifeng-admin/src/main/resources/db/migration/apps_V18__t_wish_plans_tables.sql`（已存在 0 字节空文件，直接填充）

### 3.2 对原始需求 SQL 的修正

| # | 原始 SQL 问题 | 修正 |
|---|---|---|
| 1 | `member_id INTEGER` | 改为 `BIGINT`（与 t_member.id 类型匹配） |
| 2 | `is_exported` 在 t_wish_major_snapshot 中定义两次 | 仅保留一次，置于 `major_sort_order` 之后 |
| 3 | t_wish_group_snapshot 缺少 `constraints TEXT[]`（约束编码数组） | 补充该字段，与 t_admission_group 保持一致 |
| 4 | t_wish_group_snapshot.recommendation_year 为 SMALLINT | 改为 INTEGER（与 t_universities.recommendation_year 一致） |
| 5 | t_wish_group_snapshot.recommendation_year 类型声明位置 | 调整至与 t_universities 字段顺序一致 |

### 3.3 t_wish_plan（志愿方案主表）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | SERIAL PRIMARY KEY | 自增主键 |
| member_id | BIGINT NOT NULL | 用户ID（关联 t_member.id） |
| plan_name | VARCHAR(100) DEFAULT '我的志愿方案1' NOT NULL | 方案名 |
| plan_year | SMALLINT NOT NULL | 高考年份 |
| plan_province | VARCHAR(30) NOT NULL | 高考省份 |
| reform_model | VARCHAR(20) NOT NULL | 改革模式 |
| plan_batch | VARCHAR(50) NOT NULL | 批次（本科批等） |
| user_score | INTEGER NOT NULL | 用户分数快照 |
| user_rank | INTEGER NOT NULL | 用户位次快照 |
| bo_limit | INTEGER DEFAULT 0 NOT NULL | 搏档上限 |
| chong_limit | INTEGER DEFAULT 0 NOT NULL | 冲档上限 |
| wen_limit | INTEGER DEFAULT 0 NOT NULL | 稳档上限 |
| bao_limit | INTEGER DEFAULT 0 NOT NULL | 保档上限 |
| die_limit | INTEGER DEFAULT 0 NOT NULL | 垫档上限 |
| jin_limit | INTEGER DEFAULT 0 NOT NULL | 禁档上限 |
| is_deleted | BOOLEAN NOT NULL DEFAULT FALSE | 软删除 |
| created_at | TIMESTAMPTZ NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMPTZ NOT NULL DEFAULT NOW() | |

**索引**：
- `idx_wp_member (member_id) WHERE is_deleted = FALSE`
- `idx_wp_member_year (member_id, plan_year) WHERE is_deleted = FALSE`

### 3.4 t_wish_group_snapshot（志愿方案-专业组快照表）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | SERIAL PRIMARY KEY | |
| plan_id | INTEGER NOT NULL | 关联 t_wish_plan.id |
| group_id | INTEGER NOT NULL | 大库原始专业组ID（关联 t_admission_group.id） |
| group_sort_order | INTEGER NOT NULL DEFAULT 0 | 专业组全局排序 |
| university_name | VARCHAR(50) NOT NULL | |
| city_name | VARCHAR(50) NOT NULL | |
| year | SMALLINT NOT NULL | |
| province | VARCHAR(20) NOT NULL | |
| batch | VARCHAR(50) NOT NULL | |
| enrollment_code | VARCHAR(30) | |
| group_code | VARCHAR(30) NOT NULL | |
| group_name | VARCHAR(100) | |
| subjects | TEXT[] DEFAULT '{}' | 选科数组 |
| **constraints** | **TEXT[] DEFAULT '{}'** | **约束编码数组（与 t_admission_group 一致，本期补充）** |
| description | TEXT | |
| constraints_description | TEXT[] DEFAULT '{}' | 约束描述数组 |
| recommendation_year | **INTEGER** | 推免年份（修正：原为 SMALLINT） |
| recommendation_rate | NUMERIC(5,2) | 推免率（如 24.50） |
| created_at | TIMESTAMPTZ NOT NULL DEFAULT NOW() | |

**索引**：
- `idx_twg_plan (plan_id, group_sort_order)`

### 3.5 t_wish_major_snapshot（志愿方案-专业明细快照表）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | SERIAL PRIMARY KEY | |
| plan_id | INTEGER NOT NULL | 关联 t_wish_plan.id |
| group_snapshot_id | INTEGER NOT NULL | 关联 t_wish_group_snapshot.id |
| major_id | BIGINT | 大库原始专业ID（可空） |
| major_sort_order | INTEGER NOT NULL DEFAULT 0 | 组内排序 |
| **is_exported** | **BOOLEAN NOT NULL DEFAULT TRUE** | **是否勾选导出到xlsx（仅保留一次）** |
| major_code | VARCHAR(30) NOT NULL | 招生专业代码 |
| major_name | TEXT NOT NULL | 专业名称 |
| duration | VARCHAR(20) | 学制 |
| tuition | NUMERIC(10,2) | 学费 |
| admission_count | INTEGER | 该专业招生计划人数 |
| safety_level | NUMERIC(3,2) | 安全系数（0.00~1.00） |
| level_short | VARCHAR(10) NOT NULL | 档位简称（搏/冲/稳/保/垫/禁） |
| history_scores | JSONB | 历史五年录取分快照 |
| created_at | TIMESTAMPTZ NOT NULL DEFAULT NOW() | |

**索引**：
- `idx_twm_group (group_snapshot_id, major_sort_order)`
- `idx_twm_plan (plan_id) WHERE is_exported = TRUE`

## 4. Entity 设计

所有 Entity 位于 `com.haifeng.common.entity.algorithm.wish` 子包。

### 4.1 WishPlan

- `@TableName(value = "t_wish_plan", autoResultMap = true)`
- `@TableId(type = IdType.AUTO) Integer id`（SERIAL 自增）
- `Long memberId`（对应 BIGINT）
- `String planName`, `Short planYear`, `String planProvince`, `String reformModel`, `String planBatch`
- `Integer userScore`, `Integer userRank`
- `Integer boLimit`, `chongLimit`, `wenLimit`, `baoLimit`, `dieLimit`, `jinLimit`
- `@TableLogic @TableField("is_deleted") Boolean deleted`
- `@TableField(fill = FieldFill.INSERT) OffsetDateTime createdAt`
- `@TableField(fill = FieldFill.INSERT_UPDATE) OffsetDateTime updatedAt`

### 4.2 WishGroupSnapshot

- `@TableId(type = IdType.AUTO) Integer id`
- `Integer planId`, `groupId`, `groupSortOrder`
- 快照字段：universityName, cityName, year(Short), province, batch, enrollmentCode, groupCode, groupName
- `@TableField(typeHandler = JacksonTypeHandler.class) List<String> subjects`
- `@TableField(typeHandler = JacksonTypeHandler.class) List<String> constraints`（新增）
- `String description`
- `@TableField(typeHandler = JacksonTypeHandler.class) List<String> constraintsDescription`
- `Integer recommendationYear`（注意类型）
- `BigDecimal recommendationRate`
- `@TableField(fill = FieldFill.INSERT) OffsetDateTime createdAt`
- **无 is_deleted、无 updated_at**（按 SQL 设计）

### 4.3 WishMajorSnapshot

- `@TableId(type = IdType.AUTO) Integer id`
- `Integer planId`, `groupSnapshotId`, `Long majorId`, `Integer majorSortOrder`
- `Boolean isExported`
- `String majorCode`, `majorName (TEXT)`, `duration`, `BigDecimal tuition`, `Integer admissionCount`
- `BigDecimal safetyLevel`, `String levelShort`
- `@TableField(typeHandler = JacksonTypeHandler.class) List<HistoryScore> historyScores`
- `@TableField(fill = FieldFill.INSERT) OffsetDateTime createdAt`
- 内部类 `HistoryScore`：`Integer year, minScore, minRank, avgScore, maxScore, maxRank`

## 5. Mapper 设计

3 个纯 `BaseMapper<>` 接口，位于 `com.haifeng.common.mapper.algorithm.wish` 子包。

```java
@Mapper
public interface WishPlanMapper extends BaseMapper<WishPlan> { }
@Mapper
public interface WishGroupSnapshotMapper extends BaseMapper<WishGroupSnapshot> { }
@Mapper
public interface WishMajorSnapshotMapper extends BaseMapper<WishMajorSnapshot> { }
```

本期无自定义 SQL，后续添加专业、排序等业务时再扩展。

## 6. Task 1 接口设计

### 6.1 API

| 项 | 值 |
|---|---|
| Method | GET |
| Path | `/api/v1/app/algorithm/wish-plan/default-limits` |
| 鉴权 | `@RequireLogin`（类级别） |
| 请求参数 | 无 |
| 响应 | `R<WishPlanLimitVO>` |

### 6.2 WishPlanLimitVO

位于 `com.haifeng.app.vo.algorithm.wish` 子包。

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WishPlanLimitVO {
    private Integer reachHighCount;  // 搏
    private Integer reachCount;      // 冲
    private Integer matchCount;      // 稳
    private Integer safeCount;       // 保
    private Integer floorCount;      // 垫
}
```

### 6.3 缓存策略

- **Key**: `haifeng:wish-plan:default-limits`
- **TTL**: 24 小时
- **失效策略**: 本期不实现。系统设置变化频率低，24h 内不感知。
- **值类型**: `RedisTemplate<String, Object>`（与 `SiteServiceImpl` 模式一致，复用 `RedisConfig` 的 `Jackson2JsonRedisSerializer`）

### 6.4 业务流程

1. 请求进入 `WishPlanController.getDefaultLimits()`
2. 调用 `wishPlanService.getDefaultLimits()`
3. 查 Redis `haifeng:wish-plan:default-limits`
4. 命中：直接返回 VO
5. 未命中：调用 `SystemSettingsMapper.selectOne(...LIMIT 1)`
6. 若 `settings == null`：返回零值 VO（不抛异常，与 `SiteServiceImpl` 模式一致）
7. 若 `settings != null`：构造 VO，写入 Redis 24h，返回 VO

### 6.5 文件位置

- Controller: `com.haifeng.app.controller.algorithm.WishPlanController`（**注意：包路径直接是 `controller.algorithm`，非子包**，与文件实际位置 `controller/algorithm/WishPlanController.java` 一致）
- Service: `com.haifeng.app.service.algorithm.wish.WishPlanService`
- ServiceImpl: `com.haifeng.app.service.impl.algorithm.wish.WishPlanServiceImpl`

### 6.6 日志

- 仅 `log.debug` 记录缓存命中/未命中（不记录 INFO，避免刷屏）
- 异常处使用 `log.error`（如 Redis 不可用，吞掉异常走 DB 查询路径）

### 6.7 错误处理

| 场景 | 行为 |
|---|---|
| Redis 不可用 | catch 异常，`log.warn`，走 DB 查询路径 |
| SystemSettings 为空 | 返回零值 VO（不抛异常） |
| DB 不可用 | 异常向上抛，被 `GlobalExceptionHandler` 捕获并返回 500 |

## 7. 依赖项

本期无新增 Maven 依赖。`RedisTemplate` 已在 `haifeng-common` 公共配置中提供。

## 8. 测试策略

本期不写单元测试。理由：
- Task 1 业务逻辑简单（30 行）
- Task 3 纯 Entity/Mapper 样板，无业务逻辑
- 集成测试在 `haifeng-app/src/test` 已有模式，可按需后续补充

## 9. 文件清单（10 个）

| # | 类型 | 路径 |
|---|---|---|
| 1 | SQL | `haifeng-admin/src/main/resources/db/migration/apps_V18__t_wish_plans_tables.sql` |
| 2 | Entity | `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/wish/WishPlan.java` |
| 3 | Entity | `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/wish/WishGroupSnapshot.java` |
| 4 | Entity | `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/wish/WishMajorSnapshot.java` |
| 5 | Mapper | `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/wish/WishPlanMapper.java` |
| 6 | Mapper | `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/wish/WishGroupSnapshotMapper.java` |
| 7 | Mapper | `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/wish/WishMajorSnapshotMapper.java` |
| 8 | VO | `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/wish/WishPlanLimitVO.java` |
| 9 | Service+Impl | `haifeng-app/src/main/java/com/haifeng/app/service/algorithm/wish/WishPlanService.java` + `impl/.../WishPlanServiceImpl.java` |
| 10 | Controller | `haifeng-app/src/main/java/com/haifeng/app/controller/algorithm/WishPlanController.java` |
| 11 | Constant | `haifeng-common/src/main/java/com/haifeng/common/constant/RedisKeyConstant.java`（追加 1 个常量） |
