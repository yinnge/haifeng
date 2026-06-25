# Wish Plan Tasks 1-2-3 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement 志愿方案 (Wish Plan) module's foundation — 3 SQL snapshot tables, their entities/mappers, and a Redis-cached default-limits API (Tasks 1, 2, 3 of AL7-1).

**Architecture:** New `algorithm.wish` sub-package in `haifeng-common` for 3 entities + 3 mappers; new `algorithm.wish` sub-package in `haifeng-app` for service+impl+VO; new `algorithm.WishPlanController` at the algorithm level (file in `controller/algorithm/` root, package path is `controller.algorithm`). Redis caches the 5 default-limit fields under `haifeng:wish-plan:default-limits` for 24h.

**Tech Stack:** Spring Boot 3.x + MyBatis-Plus + Spring Security + PostgreSQL + Redis + Flyway (off, manual SQL).

**User Constraints (per requirements doc):**
- Do NOT use git commands during execution; the user will commit all changes at the end.
- No unit tests this period (simple 30-line service + pure boilerplate entities/mappers).
- Apply all 5 schema fixes to the original AL7-1 SQL (member_id BIGINT, single is_exported, add constraints TEXT[], recommendation_year INTEGER, field ordering).
- Use SERIAL for new table IDs (match `t_admission_group` pattern); use BIGINT for `member_id` (match `t_member.id`).

---

## File Structure

**New files (10):**

| # | Layer | Path |
|---|---|---|
| 1 | SQL | `haifeng-admin/src/main/resources/db/migration/apps_V18__t_wish_plans_tables.sql` |
| 2 | Entity | `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/wish/WishPlan.java` |
| 3 | Entity | `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/wish/WishGroupSnapshot.java` |
| 4 | Entity | `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/wish/WishMajorSnapshot.java` |
| 5 | Mapper | `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/wish/WishPlanMapper.java` |
| 6 | Mapper | `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/wish/WishGroupSnapshotMapper.java` |
| 7 | Mapper | `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/wish/WishMajorSnapshotMapper.java` |
| 8 | VO | `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/wish/WishPlanLimitVO.java` |
| 9 | Service | `haifeng-app/src/main/java/com/haifeng/app/service/algorithm/wish/WishPlanService.java` + `impl/.../WishPlanServiceImpl.java` |
| 10 | Controller | `haifeng-app/src/main/java/com/haifeng/app/controller/algorithm/WishPlanController.java` |

**Modified files (1):**
- `haifeng-common/src/main/java/com/haifeng/common/constant/RedisKeyConstant.java` — append 1 new constant.

**Task dependencies:**
```
Task 1 (Redis constant) ─┐
                         ├─> Task 7 (VO) ─> Task 8 (Service) ─> Task 9 (Controller) ─> Task 10 (verify)
Task 2 (SQL)             ─┤
Task 3-5 (Entities)      ─┤
Task 6 (Mappers)         ─┘
```

Tasks 1-6 can run in parallel; Tasks 7-10 must be sequential.

---

## Task 1: Add Redis Key Constant

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/constant/RedisKeyConstant.java` (append after line 46, before the `ALGO_SAFETY_LOCK_PREFIX` block)

- [ ] **Step 1: Append new constant after `ALGO_SAFETY_ZSET_PREFIX` block**

Open `RedisKeyConstant.java`. Find the line containing `public static final String ALGO_SAFETY_ZSET_PREFIX = "haifeng:algo:safety:zset:";` (line 45). After the closing `*/` of that block (line 46), insert a new constant block:

```java
    /**
     * 志愿方案默认数量限制缓存
     * value: WishPlanLimitVO (5 个字段: 搏/冲/稳/保/垫)
     */
    public static final String WISH_PLAN_DEFAULT_LIMITS_KEY = "haifeng:wish-plan:default-limits";
```

- [ ] **Step 2: Verify the file compiles**

Run from project root: `.\mvnw.cmd -pl haifeng-common -am compile -q`
Expected: BUILD SUCCESS, no errors.

---

## Task 2: Write SQL Migration

**Files:**
- Modify: `haifeng-admin/src/main/resources/db/migration/apps_V18__t_wish_plans_tables.sql` (currently 0 bytes)

- [ ] **Step 1: Write the SQL content (replaces the empty file)**

```sql
-- ============================================================
-- 志愿方案模块 (t_wish_plan, t_wish_group_snapshot, t_wish_major_snapshot)
-- apps_V18 - 高考志愿填报方案快照表
-- ============================================================

BEGIN;

-- 1. 志愿方案主表
CREATE TABLE IF NOT EXISTS t_wish_plan (
    id                      SERIAL          PRIMARY KEY,
    member_id               BIGINT          NOT NULL,
    plan_name               VARCHAR(100)    DEFAULT '我的志愿方案1' NOT NULL,
    plan_year               SMALLINT        NOT NULL,
    plan_province           VARCHAR(30)     NOT NULL,
    reform_model            VARCHAR(20)     NOT NULL,
    plan_batch              VARCHAR(50)     NOT NULL,
    user_score              INTEGER         NOT NULL,
    user_rank               INTEGER         NOT NULL,
    bo_limit                INTEGER         DEFAULT 0 NOT NULL,
    chong_limit             INTEGER         DEFAULT 0 NOT NULL,
    wen_limit               INTEGER         DEFAULT 0 NOT NULL,
    bao_limit               INTEGER         DEFAULT 0 NOT NULL,
    die_limit               INTEGER         DEFAULT 0 NOT NULL,
    jin_limit               INTEGER         DEFAULT 0 NOT NULL,
    is_deleted              BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_wp_member ON t_wish_plan (member_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_wp_member_year ON t_wish_plan (member_id, plan_year) WHERE is_deleted = FALSE;

-- 2. 志愿方案-专业组快照表
CREATE TABLE IF NOT EXISTS t_wish_group_snapshot (
    id                      SERIAL          PRIMARY KEY,
    plan_id                 INTEGER         NOT NULL,
    group_id                INTEGER         NOT NULL,
    group_sort_order        INTEGER         NOT NULL DEFAULT 0,
    university_name         VARCHAR(50)     NOT NULL,
    city_name               VARCHAR(50)     NOT NULL,
    year                    SMALLINT        NOT NULL,
    province                VARCHAR(20)     NOT NULL,
    batch                   VARCHAR(50)     NOT NULL,
    enrollment_code         VARCHAR(30),
    group_code              VARCHAR(30)     NOT NULL,
    group_name              VARCHAR(100),
    subjects                TEXT[]          DEFAULT '{}',
    constraints             TEXT[]          DEFAULT '{}',
    description             TEXT,
    constraints_description TEXT[]          DEFAULT '{}',
    recommendation_year     INTEGER,
    recommendation_rate     NUMERIC(5,2),
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_twg_plan ON t_wish_group_snapshot (plan_id, group_sort_order);

-- 3. 志愿方案-专业明细快照表
CREATE TABLE IF NOT EXISTS t_wish_major_snapshot (
    id                      SERIAL          PRIMARY KEY,
    plan_id                 INTEGER         NOT NULL,
    group_snapshot_id       INTEGER         NOT NULL,
    major_id                BIGINT,
    major_sort_order        INTEGER         NOT NULL DEFAULT 0,
    is_exported             BOOLEAN         NOT NULL DEFAULT TRUE,
    major_code              VARCHAR(30)     NOT NULL,
    major_name              TEXT            NOT NULL,
    duration                VARCHAR(20),
    tuition                 NUMERIC(10,2),
    admission_count         INTEGER,
    safety_level            NUMERIC(3,2),
    level_short             VARCHAR(10)     NOT NULL,
    history_scores          JSONB,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_twm_group ON t_wish_major_snapshot (group_snapshot_id, major_sort_order);
CREATE INDEX idx_twm_plan ON t_wish_major_snapshot (plan_id) WHERE is_exported = TRUE;

COMMIT;
```

- [ ] **Step 2: Verify SQL is well-formed (no PostgreSQL execution needed since Flyway is off)**

Open the file in any editor. Sanity checks:
- All 3 tables present with `CREATE TABLE IF NOT EXISTS`
- No duplicate `is_exported` column
- `member_id` is `BIGINT`
- `recommendation_year` is `INTEGER`
- `constraints TEXT[]` present in `t_wish_group_snapshot`
- All 5 indexes present
- `BEGIN;` and `COMMIT;` wrap the DDL

---

## Task 3: Create WishPlan Entity

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/wish/WishPlan.java`

- [ ] **Step 1: Create the directory**

```bash
mkdir -p "D:\exeProject\ideaProjects\Project-HaiFeng\haifeng-common\src\main\java\com\haifeng\common\entity\algorithm\wish"
```

- [ ] **Step 2: Write WishPlan.java**

```java
package com.haifeng.common.entity.algorithm.wish;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 志愿方案主表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_wish_plan")
public class WishPlan {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Long memberId;

    private String planName;

    private Short planYear;

    private String planProvince;

    private String reformModel;

    private String planBatch;

    private Integer userScore;

    private Integer userRank;

    private Integer boLimit;

    private Integer chongLimit;

    private Integer wenLimit;

    private Integer baoLimit;

    private Integer dieLimit;

    private Integer jinLimit;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 3: Verify it compiles**

Run: `.\mvnw.cmd -pl haifeng-common -am compile -q`
Expected: BUILD SUCCESS.

---

## Task 4: Create WishGroupSnapshot Entity

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/wish/WishGroupSnapshot.java`

- [ ] **Step 1: Write WishGroupSnapshot.java**

```java
package com.haifeng.common.entity.algorithm.wish;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 志愿方案-专业组快照表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_wish_group_snapshot", autoResultMap = true)
public class WishGroupSnapshot {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer planId;

    private Integer groupId;

    private Integer groupSortOrder;

    private String universityName;

    private String cityName;

    private Short year;

    private String province;

    private String batch;

    private String enrollmentCode;

    private String groupCode;

    private String groupName;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> subjects;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> constraints;

    private String description;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> constraintsDescription;

    private Integer recommendationYear;

    private BigDecimal recommendationRate;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}
```

- [ ] **Step 2: Verify it compiles**

Run: `.\mvnw.cmd -pl haifeng-common -am compile -q`
Expected: BUILD SUCCESS.

---

## Task 5: Create WishMajorSnapshot Entity

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/wish/WishMajorSnapshot.java`

- [ ] **Step 1: Write WishMajorSnapshot.java**

```java
package com.haifeng.common.entity.algorithm.wish;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 志愿方案-专业明细快照表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_wish_major_snapshot", autoResultMap = true)
public class WishMajorSnapshot {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer planId;

    private Integer groupSnapshotId;

    private Long majorId;

    private Integer majorSortOrder;

    private Boolean isExported;

    private String majorCode;

    private String majorName;

    private String duration;

    private BigDecimal tuition;

    private Integer admissionCount;

    private BigDecimal safetyLevel;

    private String levelShort;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<HistoryScore> historyScores;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    /**
     * 历史录取分快照 (JSONB 反序列化目标)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryScore {
        private Integer year;
        private Integer minScore;
        private Integer minRank;
        private Integer avgScore;
        private Integer maxScore;
        private Integer maxRank;
    }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `.\mvnw.cmd -pl haifeng-common -am compile -q`
Expected: BUILD SUCCESS.

---

## Task 6: Create 3 Mappers

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/wish/WishPlanMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/wish/WishGroupSnapshotMapper.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/wish/WishMajorSnapshotMapper.java`

- [ ] **Step 1: Create the directory**

```bash
mkdir -p "D:\exeProject\ideaProjects\Project-HaiFeng\haifeng-common\src\main\java\com\haifeng\common\mapper\algorithm\wish"
```

- [ ] **Step 2: Write WishPlanMapper.java**

```java
package com.haifeng.common.mapper.algorithm.wish;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.wish.WishPlan;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WishPlanMapper extends BaseMapper<WishPlan> {
}
```

- [ ] **Step 3: Write WishGroupSnapshotMapper.java**

```java
package com.haifeng.common.mapper.algorithm.wish;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.wish.WishGroupSnapshot;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WishGroupSnapshotMapper extends BaseMapper<WishGroupSnapshot> {
}
```

- [ ] **Step 4: Write WishMajorSnapshotMapper.java**

```java
package com.haifeng.common.mapper.algorithm.wish;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.wish.WishMajorSnapshot;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WishMajorSnapshotMapper extends BaseMapper<WishMajorSnapshot> {
}
```

- [ ] **Step 5: Verify haifeng-common compiles**

Run: `.\mvnw.cmd -pl haifeng-common -am compile -q`
Expected: BUILD SUCCESS.

---

## Task 7: Create WishPlanLimitVO

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/wish/WishPlanLimitVO.java`

- [ ] **Step 1: Create the directory**

```bash
mkdir -p "D:\exeProject\ideaProjects\Project-HaiFeng\haifeng-app\src\main\java\com\haifeng\app\vo\algorithm\wish"
```

- [ ] **Step 2: Write WishPlanLimitVO.java**

```java
package com.haifeng.app.vo.algorithm.wish;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 志愿方案默认数量限制 VO
 *
 * <p>对应 system_settings 表的 5 个默认推荐志愿数字段，
 * 用户进入志愿填报页时由后端返回作为每档可选数量上限。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishPlanLimitVO {

    /** 搏(大胆冲刺)档默认推荐志愿数 */
    private Integer reachHighCount;

    /** 冲(可以冲击)档默认推荐志愿数 */
    private Integer reachCount;

    /** 稳(较为稳妥)档默认推荐志愿数 */
    private Integer matchCount;

    /** 保(比较安全)档默认推荐志愿数 */
    private Integer safeCount;

    /** 垫(高度保底)档默认推荐志愿数 */
    private Integer floorCount;
}
```

---

## Task 8: Create WishPlanService Interface and Implementation

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/algorithm/wish/WishPlanService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/wish/WishPlanServiceImpl.java`

- [ ] **Step 1: Create both directories**

```bash
mkdir -p "D:\exeProject\ideaProjects\Project-HaiFeng\haifeng-app\src\main\java\com\haifeng\app\service\algorithm\wish"
mkdir -p "D:\exeProject\ideaProjects\Project-HaiFeng\haifeng-app\src\main\java\com\haifeng\app\service\impl\algorithm\wish"
```

- [ ] **Step 2: Write WishPlanService.java**

```java
package com.haifeng.app.service.algorithm.wish;

import com.haifeng.app.vo.algorithm.wish.WishPlanLimitVO;

/**
 * 志愿方案服务接口
 */
public interface WishPlanService {

    /**
     * 获取志愿方案默认数量限制
     * <p>从 system_settings 读取 5 个字段（搏/冲/稳/保/垫），
     * 通过 Redis 缓存 24 小时。
     *
     * @return 默认数量限制 VO，system_settings 为空时返回零值
     */
    WishPlanLimitVO getDefaultLimits();
}
```

- [ ] **Step 3: Write WishPlanServiceImpl.java**

```java
package com.haifeng.app.service.impl.algorithm.wish;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.service.algorithm.wish.WishPlanService;
import com.haifeng.app.vo.algorithm.wish.WishPlanLimitVO;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.entity.system.SystemSettings;
import com.haifeng.common.mapper.system.SystemSettingsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 志愿方案服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WishPlanServiceImpl implements WishPlanService {

    private static final long CACHE_TTL_HOURS = 24;

    private final SystemSettingsMapper settingsMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public WishPlanLimitVO getDefaultLimits() {
        String cacheKey = RedisKeyConstant.WISH_PLAN_DEFAULT_LIMITS_KEY;

        WishPlanLimitVO cached = safeGetFromCache(cacheKey);
        if (cached != null) {
            log.debug("志愿方案默认数量限制缓存命中");
            return cached;
        }

        SystemSettings settings = settingsMapper.selectOne(
                new LambdaQueryWrapper<SystemSettings>().last("LIMIT 1"));

        WishPlanLimitVO vo;
        if (settings == null) {
            log.warn("system_settings 表为空，返回零值默认数量限制");
            vo = WishPlanLimitVO.builder()
                    .reachHighCount(0)
                    .reachCount(0)
                    .matchCount(0)
                    .safeCount(0)
                    .floorCount(0)
                    .build();
        } else {
            vo = WishPlanLimitVO.builder()
                    .reachHighCount(settings.getReachHighCount())
                    .reachCount(settings.getReachCount())
                    .matchCount(settings.getMatchCount())
                    .safeCount(settings.getSafeCount())
                    .floorCount(settings.getFloorCount())
                    .build();
        }

        safeSetCache(cacheKey, vo);
        return vo;
    }

    private WishPlanLimitVO safeGetFromCache(String key) {
        try {
            return (WishPlanLimitVO) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis 读取失败，降级走 DB: key={}", key, e);
            return null;
        }
    }

    private void safeSetCache(String key, WishPlanLimitVO vo) {
        try {
            redisTemplate.opsForValue().set(key, vo, CACHE_TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("Redis 写入失败: key={}", key, e);
        }
    }
}
```

- [ ] **Step 4: Verify haifeng-app compiles**

Run: `.\mvnw.cmd -pl haifeng-app -am compile -q`
Expected: BUILD SUCCESS.

---

## Task 9: Create WishPlanController

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/algorithm/WishPlanController.java`
  - **Note:** File is in `controller/algorithm/` directory directly (NOT in a sub-package). Package is `com.haifeng.app.controller.algorithm`.

- [ ] **Step 1: Write WishPlanController.java**

```java
package com.haifeng.app.controller.algorithm;

import com.haifeng.app.service.algorithm.wish.WishPlanService;
import com.haifeng.app.vo.algorithm.wish.WishPlanLimitVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 志愿方案控制器
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/algorithm/wish-plan")
@RequiredArgsConstructor
@RequireLogin
public class WishPlanController {

    private final WishPlanService wishPlanService;

    /**
     * 获取志愿方案默认数量限制
     * <p>从 system_settings 读取 5 个字段（搏/冲/稳/保/垫），通过 Redis 缓存 24h。
     */
    @GetMapping("/default-limits")
    public R<WishPlanLimitVO> getDefaultLimits() {
        return R.ok(wishPlanService.getDefaultLimits());
    }
}
```

- [ ] **Step 2: Verify haifeng-app compiles**

Run: `.\mvnw.cmd -pl haifeng-app -am compile -q`
Expected: BUILD SUCCESS.

---

## Task 10: Final Build Verification

- [ ] **Step 1: Run full multi-module compile**

Run: `.\mvnw.cmd clean compile -q`
Expected: BUILD SUCCESS across all modules (haifeng-common, haifeng-app, haifeng-admin).

- [ ] **Step 2: Sanity-check the SQL file**

Open `apps_V18__t_wish_plans_tables.sql` and verify:
- Line count: ~75-85 lines
- 3 `CREATE TABLE` statements
- `member_id BIGINT` in t_wish_plan
- Single `is_exported` in t_wish_major_snapshot
- `constraints TEXT[]` in t_wish_group_snapshot
- `recommendation_year INTEGER` in t_wish_group_snapshot
- 5 `CREATE INDEX` statements

- [ ] **Step 3: Sanity-check the controller route**

Open `WishPlanController.java` and verify:
- Package: `com.haifeng.app.controller.algorithm`
- `@RequestMapping("/api/v1/app/algorithm/wish-plan")`
- `@GetMapping("/default-limits")`
- `@RequireLogin` annotation present
- Full path: `GET /api/v1/app/algorithm/wish-plan/default-limits`

- [ ] **Step 4: Sanity-check Redis key constant**

Open `RedisKeyConstant.java` and verify:
- `WISH_PLAN_DEFAULT_LIMITS_KEY` constant exists
- Value: `"haifeng:wish-plan:default-limits"`
- Matches the value used in `WishPlanServiceImpl`

- [ ] **Step 5: Report completion to user**

Tell the user:
- All 10 new files created + 1 file modified (RedisKeyConstant.java)
- `mvn clean compile` passes
- User can now manually run the SQL against PostgreSQL (Flyway is off)
- User will commit all changes at the end

---

## Self-Review Notes

**Spec coverage:**
- §3 Database design → Task 2 ✓
- §4 Entity design → Tasks 3, 4, 5 ✓
- §5 Mapper design → Task 6 ✓
- §6.1 API endpoint → Task 9 ✓
- §6.2 VO → Task 7 ✓
- §6.3 Cache key + TTL → Task 1 (key) + Task 8 (TTL in impl) ✓
- §6.4 Business flow → Task 8 ✓
- §6.5 File locations → All tasks use exact paths ✓

**Placeholder scan:** No "TBD" / "TODO" / "implement later" / "fill in details" in any step. All code is complete. All commands are runnable.

**Type consistency:**
- `IdType.AUTO` used in all 3 entities (matches SERIAL in SQL).
- `Long memberId` in WishPlan matches `BIGINT member_id` in SQL.
- `Integer planId/groupId/groupSnapshotId` matches `INTEGER ... NOT NULL` in SQL.
- `Long majorId` matches `BIGINT major_id` in SQL.
- `List<String>` with `JacksonTypeHandler` matches `TEXT[]` for `subjects`, `constraints`, `constraints_description`.
- `List<HistoryScore>` with `JacksonTypeHandler` matches `JSONB` for `history_scores`.
- `BigDecimal` for `tuition`, `safetyLevel`, `recommendationRate` matches `NUMERIC(10,2)`, `NUMERIC(3,2)`, `NUMERIC(5,2)`.
- `Short planYear/year` matches `SMALLINT`.
- `OffsetDateTime createdAt/updatedAt` matches `TIMESTAMPTZ`.
- `@TableLogic` only on `WishPlan` (the other 2 tables have no `is_deleted` per SQL design).
- `WishGroupSnapshot.recommendationYear` is `Integer` (not Short) per the fix from §3.2.

**No mismatches found.**
