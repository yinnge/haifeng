# 高考档案模块实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现用户高考档案管理功能，包含档案 CRUD、改革模式查询、位次查询、批次线查询

**Architecture:**
- haifeng-common 层：新增 MemberGaokao 实体类、Mapper、4 个枚举类
- haifeng-app 层：新增 GaokaoArchiveController（5 个接口）、Service、DTO/VO
- Flyway：apps_V17__member_gaokao__tables.sql

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, PostgreSQL, Flyway

---

## 文件结构

### haifeng-admin（Flyway 迁移）
| 文件 | 职责 |
|------|------|
| `src/main/resources/db/migration/apps_V17__member_gaokao__tables.sql` | 创建 t_member_gaokao 表 |



### haifeng-common（实体/Mapper/枚举）

| 文件 | 职责 |
|------|------|
| `src/main/java/com/haifeng/common/entity/algorithm/MemberGaokao.java` | 高考档案实体类 |
| `src/main/java/com/haifeng/common/mapper/algorithm/MemberGaokaoMapper.java` | Mapper 接口 |
| `src/main/java/com/haifeng/common/enums/ReformModelEnum.java` | 改革模式枚举 |
| `src/main/java/com/haifeng/common/enums/ForeignLanguageEnum.java` | 外语语种枚举 |
| `src/main/java/com/haifeng/common/enums/PoliticalStatusEnum.java` | 政治面貌枚举 |
| `src/main/java/com/haifeng/common/enums/HouseholdTypeEnum.java` | 户籍类型枚举 |

### haifeng-app（Controller/Service/DTO/VO）
| 文件 | 职责 |
|------|------|
| `src/main/java/com/haifeng/app/controller/algorithm/GaokaoArchiveController.java` | 5 个接口的控制器 |
| `src/main/java/com/haifeng/app/service/algorithm/GaokaoArchiveService.java` | 服务接口 |
| `src/main/java/com/haifeng/app/service/impl/algorithm/GaokaoArchiveServiceImpl.java` | 服务实现 |
| `src/main/java/com/haifeng/app/dto/algorithm/GaokaoArchiveSaveDTO.java` | 保存档案请求 |
| `src/main/java/com/haifeng/app/vo/algorithm/GaokaoArchiveVO.java` | 档案详情响应 |
| `src/main/java/com/haifeng/app/vo/algorithm/ReformModelVO.java` | 改革模式响应 |
| `src/main/java/com/haifeng/app/vo/algorithm/ScoreRankVO.java` | 位次查询响应 |
| `src/main/java/com/haifeng/app/vo/algorithm/BatchLineVO.java` | 批次线响应 |
| `src/main/java/com/haifeng/app/vo/algorithm/BatchLineListVO.java` | 批次线列表响应 |

---

## Task 1: 创建数据库迁移文件

**Files:**
- Create: `haifeng-admin/src/main/resources/db/migration/apps_V17__member_gaokao__tables.sql`

- [ ] **Step 1: 创建 Flyway 迁移文件**

```sql
-- ============================================================
-- 用户高考档案表 (t_member_gaokao)
-- 描述：一个用户一条记录，存储高考的所有业务信息
--       系统推荐算法的核心输入数据
-- ============================================================

BEGIN;

CREATE TABLE IF NOT EXISTS t_member_gaokao (
    id                      BIGINT          PRIMARY KEY,
    member_id               BIGINT          NOT NULL UNIQUE,

    -- 一、高考基本信息（必填）
    gaokao_year             SMALLINT,
    gaokao_province         VARCHAR(30),
    score                   INTEGER,
    rank                    INTEGER,

    -- 二、改革模式（系统根据省份+年份自动判断）
    reform_model            VARCHAR(20),

    -- 三、选科信息（必填，与分数字段一一对应）
    subject_type            VARCHAR(20),
    second_subject_type     VARCHAR(20),
    third_subject_type      VARCHAR(20),

    -- 四、各科成绩（可选）
    score_chinese           INTEGER,
    score_math              INTEGER,
    score_english           INTEGER,
    score_subject_1         INTEGER,
    score_subject_2         INTEGER,
    score_subject_3         INTEGER,

    -- 五、外语语种（可选）
    foreign_language        VARCHAR(20),

    -- 六、身体视觉条件（可选，全部允许 NULL）
    is_color_blind          BOOLEAN,
    is_color_weak           BOOLEAN,
    vision_left             NUMERIC(3,1),
    vision_right            NUMERIC(3,1),
    has_smell_disorder      BOOLEAN,

    -- 七、身体指标（可选）
    height_cm               INTEGER,
    weight_kg               NUMERIC(5,1),
    is_left_handed          BOOLEAN,
    has_tattoo              BOOLEAN,
    has_scar                BOOLEAN,
    has_stutter             BOOLEAN,

    -- 八、身份条件（可选）
    is_fresh_graduate       BOOLEAN,
    political_status        VARCHAR(20),
    household_type          VARCHAR(20),
    is_poverty_county       BOOLEAN,

    -- 九、批次与线差
    batch                   VARCHAR(50),
    batch_data_year         SMALLINT,
    batch_line_score        INTEGER,
    score_above_line        INTEGER,

    -- 审计字段
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- 索引
CREATE INDEX idx_mg_member ON t_member_gaokao (member_id);
CREATE INDEX idx_mg_province_year ON t_member_gaokao (gaokao_province, gaokao_year);
CREATE INDEX idx_mg_score ON t_member_gaokao (score DESC NULLS LAST);

-- 外键
ALTER TABLE t_member_gaokao
    ADD CONSTRAINT fk_mg_member
    FOREIGN KEY (member_id) REFERENCES t_member(id);

-- 触发器
CREATE TRIGGER trg_mg_updated_at
    BEFORE UPDATE ON t_member_gaokao
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- 注释
COMMENT ON TABLE  t_member_gaokao IS '用户高考档案表：一人一条，志愿算法核心输入';
COMMENT ON COLUMN t_member_gaokao.id IS '主键ID（雪花算法）';
COMMENT ON COLUMN t_member_gaokao.member_id IS '关联会员表ID（唯一）';
COMMENT ON COLUMN t_member_gaokao.gaokao_year IS '高考年份';
COMMENT ON COLUMN t_member_gaokao.gaokao_province IS '高考省份';
COMMENT ON COLUMN t_member_gaokao.score IS '高考总分';
COMMENT ON COLUMN t_member_gaokao.rank IS '位次（系统查询或用户自定义）';
COMMENT ON COLUMN t_member_gaokao.reform_model IS '改革模式（3+3/3+1+2/传统文理）';
COMMENT ON COLUMN t_member_gaokao.subject_type IS '第一科目';
COMMENT ON COLUMN t_member_gaokao.second_subject_type IS '第二科目';
COMMENT ON COLUMN t_member_gaokao.third_subject_type IS '第三科目';
COMMENT ON COLUMN t_member_gaokao.score_chinese IS '语文成绩';
COMMENT ON COLUMN t_member_gaokao.score_math IS '数学成绩';
COMMENT ON COLUMN t_member_gaokao.score_english IS '外语成绩';
COMMENT ON COLUMN t_member_gaokao.score_subject_1 IS '第一科目分数';
COMMENT ON COLUMN t_member_gaokao.score_subject_2 IS '第二科目分数';
COMMENT ON COLUMN t_member_gaokao.score_subject_3 IS '第三科目分数';
COMMENT ON COLUMN t_member_gaokao.foreign_language IS '外语语种';
COMMENT ON COLUMN t_member_gaokao.is_color_blind IS '是否色盲';
COMMENT ON COLUMN t_member_gaokao.is_color_weak IS '是否色弱';
COMMENT ON COLUMN t_member_gaokao.vision_left IS '左眼视力';
COMMENT ON COLUMN t_member_gaokao.vision_right IS '右眼视力';
COMMENT ON COLUMN t_member_gaokao.has_smell_disorder IS '是否嗅觉迟钝';
COMMENT ON COLUMN t_member_gaokao.height_cm IS '身高（厘米）';
COMMENT ON COLUMN t_member_gaokao.weight_kg IS '体重（公斤）';
COMMENT ON COLUMN t_member_gaokao.is_left_handed IS '是否左利手';
COMMENT ON COLUMN t_member_gaokao.has_tattoo IS '是否有纹身';
COMMENT ON COLUMN t_member_gaokao.has_scar IS '是否有面部疤痕';
COMMENT ON COLUMN t_member_gaokao.has_stutter IS '是否口吃';
COMMENT ON COLUMN t_member_gaokao.is_fresh_graduate IS '是否应届生';
COMMENT ON COLUMN t_member_gaokao.political_status IS '政治面貌';
COMMENT ON COLUMN t_member_gaokao.household_type IS '户籍类型';
COMMENT ON COLUMN t_member_gaokao.is_poverty_county IS '是否贫困县户籍';
COMMENT ON COLUMN t_member_gaokao.batch IS '所在批次名称';
COMMENT ON COLUMN t_member_gaokao.batch_data_year IS '批次数据来源年份';
COMMENT ON COLUMN t_member_gaokao.batch_line_score IS '批次省控线';
COMMENT ON COLUMN t_member_gaokao.score_above_line IS '线差（总分-省控线）';
COMMENT ON COLUMN t_member_gaokao.created_at IS '创建时间';
COMMENT ON COLUMN t_member_gaokao.updated_at IS '更新时间';

COMMIT;
```

- [ ] **Step 2: 提交**

```bash
git add haifeng-admin/src/main/resources/db/migration/apps_V17__member_gaokao__tables.sql
git commit -m "feat(db): add t_member_gaokao table migration V17"
```

---

## Task 2: 创建枚举类

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/enums/ReformModelEnum.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/enums/ForeignLanguageEnum.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/enums/PoliticalStatusEnum.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/enums/HouseholdTypeEnum.java`

- [ ] **Step 1: 创建 ReformModelEnum**

```java
package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 高考改革模式枚举
 */
@Getter
@AllArgsConstructor
public enum ReformModelEnum {

    TRADITIONAL("传统文理"),
    THREE_PLUS_THREE("3+3"),
    THREE_PLUS_ONE_PLUS_TWO("3+1+2");

    @EnumValue
    private final String value;

    public static boolean isValid(String value) {
        for (ReformModelEnum e : values()) {
            if (e.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
```

- [ ] **Step 2: 创建 ForeignLanguageEnum**

```java
package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 外语语种枚举
 */
@Getter
@AllArgsConstructor
public enum ForeignLanguageEnum {

    ENGLISH("英语"),
    JAPANESE("日语"),
    RUSSIAN("俄语"),
    GERMAN("德语"),
    FRENCH("法语"),
    SPANISH("西班牙语");

    @EnumValue
    private final String value;

    public static boolean isValid(String value) {
        for (ForeignLanguageEnum e : values()) {
            if (e.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
```

- [ ] **Step 3: 创建 PoliticalStatusEnum**

```java
package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 政治面貌枚举
 */
@Getter
@AllArgsConstructor
public enum PoliticalStatusEnum {

    MASSES("群众"),
    LEAGUE_MEMBER("共青团员"),
    PARTY_MEMBER("中共党员"),
    PROBATIONARY_PARTY_MEMBER("中共预备党员");

    @EnumValue
    private final String value;

    public static boolean isValid(String value) {
        for (PoliticalStatusEnum e : values()) {
            if (e.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
```

- [ ] **Step 4: 创建 HouseholdTypeEnum**

```java
package com.haifeng.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 户籍类型枚举
 */
@Getter
@AllArgsConstructor
public enum HouseholdTypeEnum {

    URBAN("城镇"),
    RURAL("农村");

    @EnumValue
    private final String value;

    public static boolean isValid(String value) {
        for (HouseholdTypeEnum e : values()) {
            if (e.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
```

- [ ] **Step 5: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/enums/ReformModelEnum.java \
        haifeng-common/src/main/java/com/haifeng/common/enums/ForeignLanguageEnum.java \
        haifeng-common/src/main/java/com/haifeng/common/enums/PoliticalStatusEnum.java \
        haifeng-common/src/main/java/com/haifeng/common/enums/HouseholdTypeEnum.java
git commit -m "feat(common): add gaokao-related enums"
```

---

## Task 3: 创建实体类和 Mapper

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/MemberGaokao.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/MemberGaokaoMapper.java`

- [ ] **Step 1: 创建 MemberGaokao 实体类**

```java
package com.haifeng.common.entity.algorithm;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 用户高考档案实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_member_gaokao")
public class MemberGaokao {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联会员ID（唯一）
     */
    private Long memberId;

    // ========== 一、高考基本信息 ==========

    /**
     * 高考年份
     */
    private Short gaokaoYear;

    /**
     * 高考省份
     */
    private String gaokaoProvince;

    /**
     * 高考总分
     */
    private Integer score;

    /**
     * 位次
     */
    private Integer rank;

    // ========== 二、改革模式 ==========

    /**
     * 改革模式（3+3/3+1+2/传统文理）
     */
    private String reformModel;

    // ========== 三、选科信息 ==========

    /**
     * 第一科目
     */
    private String subjectType;

    /**
     * 第二科目
     */
    private String secondSubjectType;

    /**
     * 第三科目
     */
    private String thirdSubjectType;

    // ========== 四、各科成绩 ==========

    /**
     * 语文成绩
     */
    private Integer scoreChinese;

    /**
     * 数学成绩
     */
    private Integer scoreMath;

    /**
     * 外语成绩
     */
    private Integer scoreEnglish;

    /**
     * 第一科目分数
     */
    private Integer scoreSubject1;

    /**
     * 第二科目分数
     */
    private Integer scoreSubject2;

    /**
     * 第三科目分数
     */
    private Integer scoreSubject3;

    // ========== 五、外语语种 ==========

    /**
     * 外语语种
     */
    private String foreignLanguage;

    // ========== 六、身体视觉条件 ==========

    /**
     * 是否色盲
     */
    private Boolean isColorBlind;

    /**
     * 是否色弱
     */
    private Boolean isColorWeak;

    /**
     * 左眼视力
     */
    private BigDecimal visionLeft;

    /**
     * 右眼视力
     */
    private BigDecimal visionRight;

    /**
     * 是否嗅觉迟钝
     */
    private Boolean hasSmellDisorder;

    // ========== 七、身体指标 ==========

    /**
     * 身高（厘米）
     */
    private Integer heightCm;

    /**
     * 体重（公斤）
     */
    private BigDecimal weightKg;

    /**
     * 是否左利手
     */
    private Boolean isLeftHanded;

    /**
     * 是否有纹身
     */
    private Boolean hasTattoo;

    /**
     * 是否有面部疤痕
     */
    private Boolean hasScar;

    /**
     * 是否口吃
     */
    private Boolean hasStutter;

    // ========== 八、身份条件 ==========

    /**
     * 是否应届生
     */
    private Boolean isFreshGraduate;

    /**
     * 政治面貌
     */
    private String politicalStatus;

    /**
     * 户籍类型
     */
    private String householdType;

    /**
     * 是否贫困县户籍
     */
    private Boolean isPovertyCounty;

    // ========== 九、批次与线差 ==========

    /**
     * 所在批次名称
     */
    private String batch;

    /**
     * 批次数据来源年份
     */
    private Short batchDataYear;

    /**
     * 批次省控线
     */
    private Integer batchLineScore;

    /**
     * 线差（总分-省控线）
     */
    private Integer scoreAboveLine;

    // ========== 审计字段 ==========

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 2: 创建 MemberGaokaoMapper**

```java
package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.MemberGaokao;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户高考档案 Mapper
 */
@Mapper
public interface MemberGaokaoMapper extends BaseMapper<MemberGaokao> {
}
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/algorithm/MemberGaokao.java \
        haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/MemberGaokaoMapper.java
git commit -m "feat(common): add MemberGaokao entity and mapper"
```

---

## Task 4: 创建 VO 类

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/GaokaoArchiveVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/ReformModelVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/ScoreRankVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/BatchLineVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/BatchLineListVO.java`

- [ ] **Step 1: 创建 vo/algorithm 目录**

```bash
mkdir -p haifeng-app/src/main/java/com/haifeng/app/vo/algorithm
```

- [ ] **Step 2: 创建 ReformModelVO**

```java
package com.haifeng.app.vo.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 改革模式响应 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReformModelVO {

    /**
     * 改革模式（3+3/3+1+2/传统文理）
     */
    private String reformModel;

    /**
     * 可选科目
     * - 3+1+2: {"first": ["物理", "历史"], "second": ["化学", "生物", "政治", "地理"]}
     * - 3+3: {"first": ["物理", "化学", "生物", "政治", "历史", "地理"]}
     * - 传统文理: {"first": ["文科", "理科"]}
     */
    private Map<String, List<String>> subjects;
}
```

- [ ] **Step 3: 创建 ScoreRankVO**

```java
package com.haifeng.app.vo.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 位次查询响应 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreRankVO {

    /**
     * 位次
     */
    private Integer rank;

    /**
     * 同分人数
     */
    private Integer sameScoreCount;
}
```

- [ ] **Step 4: 创建 BatchLineVO**

```java
package com.haifeng.app.vo.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批次分数线 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchLineVO {

    /**
     * 批次名称
     */
    private String batch;

    /**
     * 分数线
     */
    private Integer scoreLine;

    /**
     * 位次线
     */
    private Integer rankLine;
}
```

- [ ] **Step 5: 创建 BatchLineListVO**

```java
package com.haifeng.app.vo.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批次分数线列表响应 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchLineListVO {

    /**
     * 数据来源年份
     */
    private Integer dataYear;

    /**
     * 是否为当年数据
     */
    private Boolean isCurrentYear;

    /**
     * 批次列表
     */
    private List<BatchLineVO> batches;
}
```

- [ ] **Step 6: 创建 GaokaoArchiveVO**

```java
package com.haifeng.app.vo.algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 高考档案详情 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GaokaoArchiveVO {

    private Long id;

    // ========== 高考基本信息 ==========
    private Short gaokaoYear;
    private String gaokaoProvince;
    private Integer score;
    private Integer rank;

    // ========== 改革模式 ==========
    private String reformModel;

    // ========== 选科信息 ==========
    private String subjectType;
    private String secondSubjectType;
    private String thirdSubjectType;

    // ========== 各科成绩 ==========
    private Integer scoreChinese;
    private Integer scoreMath;
    private Integer scoreEnglish;
    private Integer scoreSubject1;
    private Integer scoreSubject2;
    private Integer scoreSubject3;

    // ========== 外语语种 ==========
    private String foreignLanguage;

    // ========== 身体视觉条件 ==========
    private Boolean isColorBlind;
    private Boolean isColorWeak;
    private BigDecimal visionLeft;
    private BigDecimal visionRight;
    private Boolean hasSmellDisorder;

    // ========== 身体指标 ==========
    private Integer heightCm;
    private BigDecimal weightKg;
    private Boolean isLeftHanded;
    private Boolean hasTattoo;
    private Boolean hasScar;
    private Boolean hasStutter;

    // ========== 身份条件 ==========
    private Boolean isFreshGraduate;
    private String politicalStatus;
    private String householdType;
    private Boolean isPovertyCounty;

    // ========== 批次与线差 ==========
    private String batch;
    private Short batchDataYear;
    private Integer batchLineScore;
    private Integer scoreAboveLine;
}
```

- [ ] **Step 7: 提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/vo/algorithm/
git commit -m "feat(app): add gaokao archive VO classes"
```

---

## Task 5: 创建 DTO 类

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/algorithm/GaokaoArchiveSaveDTO.java`

- [ ] **Step 1: 创建 dto/algorithm 目录**

```bash
mkdir -p haifeng-app/src/main/java/com/haifeng/app/dto/algorithm
```

- [ ] **Step 2: 创建 GaokaoArchiveSaveDTO**

```java
package com.haifeng.app.dto.algorithm;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 保存高考档案请求 DTO
 */
@Data
public class GaokaoArchiveSaveDTO {

    // ========== 必填字段 ==========

    @NotNull(message = "高考年份不能为空")
    @Min(value = 2020, message = "高考年份不能早于2020")
    @Max(value = 2030, message = "高考年份不能晚于2030")
    private Short gaokaoYear;

    @NotBlank(message = "高考省份不能为空")
    @Size(max = 30, message = "高考省份最多30个字符")
    private String gaokaoProvince;

    @NotNull(message = "高考总分不能为空")
    @Min(value = 0, message = "高考总分不能小于0")
    @Max(value = 750, message = "高考总分不能大于750")
    private Integer score;

    @NotNull(message = "位次不能为空")
    @Min(value = 1, message = "位次必须大于0")
    private Integer rank;

    @NotBlank(message = "改革模式不能为空")
    private String reformModel;

    @NotBlank(message = "第一科目不能为空")
    @Size(max = 20, message = "第一科目最多20个字符")
    private String subjectType;

    @Size(max = 20, message = "第二科目最多20个字符")
    private String secondSubjectType;

    @Size(max = 20, message = "第三科目最多20个字符")
    private String thirdSubjectType;

    @NotBlank(message = "批次不能为空")
    @Size(max = 50, message = "批次最多50个字符")
    private String batch;

    @NotNull(message = "批次数据年份不能为空")
    private Short batchDataYear;

    @NotNull(message = "批次省控线不能为空")
    private Integer batchLineScore;

    // ========== 可选字段：各科成绩 ==========

    @Min(value = 0, message = "语文成绩不能小于0")
    @Max(value = 150, message = "语文成绩不能大于150")
    private Integer scoreChinese;

    @Min(value = 0, message = "数学成绩不能小于0")
    @Max(value = 150, message = "数学成绩不能大于150")
    private Integer scoreMath;

    @Min(value = 0, message = "外语成绩不能小于0")
    @Max(value = 150, message = "外语成绩不能大于150")
    private Integer scoreEnglish;

    @Min(value = 0, message = "第一科目分数不能小于0")
    @Max(value = 100, message = "第一科目分数不能大于100")
    private Integer scoreSubject1;

    @Min(value = 0, message = "第二科目分数不能小于0")
    @Max(value = 100, message = "第二科目分数不能大于100")
    private Integer scoreSubject2;

    @Min(value = 0, message = "第三科目分数不能小于0")
    @Max(value = 100, message = "第三科目分数不能大于100")
    private Integer scoreSubject3;

    // ========== 可选字段：外语语种 ==========

    @Size(max = 20, message = "外语语种最多20个字符")
    private String foreignLanguage;

    // ========== 可选字段：身体条件 ==========

    private Boolean isColorBlind;
    private Boolean isColorWeak;

    @DecimalMin(value = "0.0", message = "左眼视力不能小于0")
    @DecimalMax(value = "5.5", message = "左眼视力不能大于5.5")
    private BigDecimal visionLeft;

    @DecimalMin(value = "0.0", message = "右眼视力不能小于0")
    @DecimalMax(value = "5.5", message = "右眼视力不能大于5.5")
    private BigDecimal visionRight;

    private Boolean hasSmellDisorder;

    @Min(value = 100, message = "身高不能小于100厘米")
    @Max(value = 250, message = "身高不能大于250厘米")
    private Integer heightCm;

    @DecimalMin(value = "20.0", message = "体重不能小于20公斤")
    @DecimalMax(value = "200.0", message = "体重不能大于200公斤")
    private BigDecimal weightKg;

    private Boolean isLeftHanded;
    private Boolean hasTattoo;
    private Boolean hasScar;
    private Boolean hasStutter;

    // ========== 可选字段：身份条件 ==========

    private Boolean isFreshGraduate;

    @Size(max = 20, message = "政治面貌最多20个字符")
    private String politicalStatus;

    @Size(max = 20, message = "户籍类型最多20个字符")
    private String householdType;

    private Boolean isPovertyCounty;
}
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/dto/algorithm/
git commit -m "feat(app): add GaokaoArchiveSaveDTO"
```

---

## Task 6: 创建 Service 接口

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/algorithm/GaokaoArchiveService.java`

- [ ] **Step 1: 创建 service/algorithm 目录**

```bash
mkdir -p haifeng-app/src/main/java/com/haifeng/app/service/algorithm
```

- [ ] **Step 2: 创建 GaokaoArchiveService 接口**

```java
package com.haifeng.app.service.algorithm;

import com.haifeng.app.dto.algorithm.GaokaoArchiveSaveDTO;
import com.haifeng.app.vo.algorithm.*;

/**
 * 高考档案服务接口
 */
public interface GaokaoArchiveService {

    /**
     * 获取改革模式及可选科目
     *
     * @param province 省份
     * @param year     高考年份
     * @return 改革模式及可选科目
     */
    ReformModelVO getReformModel(String province, Integer year);

    /**
     * 查询位次
     *
     * @param province    省份
     * @param year        年份
     * @param subjectType 科类（物理类/历史类/理科/文科）
     * @param score       分数
     * @return 位次信息，未找到返回 null
     */
    ScoreRankVO getRank(String province, Integer year, String subjectType, Integer score);

    /**
     * 获取批次分数线列表
     *
     * @param province    省份
     * @param year        年份
     * @param subjectType 科类
     * @return 批次列表
     */
    BatchLineListVO getBatchLines(String province, Integer year, String subjectType);

    /**
     * 保存高考档案（新增或更新）
     *
     * @param dto 档案数据
     * @return 档案ID
     */
    Long saveArchive(GaokaoArchiveSaveDTO dto);

    /**
     * 获取当前用户的高考档案
     *
     * @return 档案信息，未创建返回 null
     */
    GaokaoArchiveVO getMyArchive();
}
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/service/algorithm/
git commit -m "feat(app): add GaokaoArchiveService interface"
```

---

## Task 7: 创建 Service 实现类

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/GaokaoArchiveServiceImpl.java`

- [ ] **Step 1: 创建 service/impl/algorithm 目录**

```bash
mkdir -p haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm
```

- [ ] **Step 2: 创建 GaokaoArchiveServiceImpl**

```java
package com.haifeng.app.service.impl.algorithm;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.dto.algorithm.GaokaoArchiveSaveDTO;
import com.haifeng.app.service.algorithm.GaokaoArchiveService;
import com.haifeng.app.vo.algorithm.*;
import com.haifeng.common.entity.algorithm.BatchScoreLine;
import com.haifeng.common.entity.algorithm.MemberGaokao;
import com.haifeng.common.entity.algorithm.ProvinceReform;
import com.haifeng.common.entity.algorithm.ScoreRank;
import com.haifeng.common.enums.ReformModelEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.BatchScoreLineMapper;
import com.haifeng.common.mapper.algorithm.MemberGaokaoMapper;
import com.haifeng.common.mapper.algorithm.ProvinceReformMapper;
import com.haifeng.common.mapper.algorithm.ScoreRankMapper;
import com.haifeng.common.util.SecurityUtil;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GaokaoArchiveServiceImpl implements GaokaoArchiveService {

    private final MemberGaokaoMapper memberGaokaoMapper;
    private final ProvinceReformMapper provinceReformMapper;
    private final ScoreRankMapper scoreRankMapper;
    private final BatchScoreLineMapper batchScoreLineMapper;

    // 6选3科目池
    private static final List<String> SUBJECTS_6 = Arrays.asList("物理", "化学", "生物", "政治", "历史", "地理");
    // 3+1+2 首选科目
    private static final List<String> SUBJECTS_FIRST_312 = Arrays.asList("物理", "历史");
    // 3+1+2 再选科目
    private static final List<String> SUBJECTS_SECOND_312 = Arrays.asList("化学", "生物", "政治", "地理");
    // 传统文理选项
    private static final List<String> SUBJECTS_TRADITIONAL = Arrays.asList("文科", "理科");

    @Override
    public ReformModelVO getReformModel(String province, Integer year) {
        String reformModel = determineReformModel(province, year);
        Map<String, List<String>> subjects = buildSubjectOptions(reformModel);

        return ReformModelVO.builder()
                .reformModel(reformModel)
                .subjects(subjects)
                .build();
    }

    @Override
    public ScoreRankVO getRank(String province, Integer year, String subjectType, Integer score) {
        ScoreRank rank = scoreRankMapper.selectOne(
                new LambdaQueryWrapper<ScoreRank>()
                        .eq(ScoreRank::getProvince, province)
                        .eq(ScoreRank::getYear, year.shortValue())
                        .eq(ScoreRank::getSubjectType, subjectType)
                        .eq(ScoreRank::getScore, score.shortValue())
        );

        if (rank == null) {
            return null;
        }

        return ScoreRankVO.builder()
                .rank(rank.getRank())
                .sameScoreCount(rank.getSameScoreCount())
                .build();
    }

    @Override
    public BatchLineListVO getBatchLines(String province, Integer year, String subjectType) {
        // 1. 先查当年数据
        List<BatchScoreLine> lines = batchScoreLineMapper.selectList(
                new LambdaQueryWrapper<BatchScoreLine>()
                        .eq(BatchScoreLine::getProvince, province)
                        .eq(BatchScoreLine::getYear, year.shortValue())
                        .eq(BatchScoreLine::getSubjectType, subjectType)
                        .orderByAsc(BatchScoreLine::getScoreLine)
        );

        if (!lines.isEmpty()) {
            return BatchLineListVO.builder()
                    .dataYear(year)
                    .isCurrentYear(true)
                    .batches(convertToBatchLineVOs(lines))
                    .build();
        }

        // 2. 当年无数据，查最近5年
        lines = batchScoreLineMapper.selectList(
                new LambdaQueryWrapper<BatchScoreLine>()
                        .eq(BatchScoreLine::getProvince, province)
                        .eq(BatchScoreLine::getSubjectType, subjectType)
                        .ge(BatchScoreLine::getYear, (short) (year - 5))
                        .orderByDesc(BatchScoreLine::getYear)
                        .orderByAsc(BatchScoreLine::getScoreLine)
        );

        Integer dataYear = lines.isEmpty() ? null : lines.get(0).getYear().intValue();

        // 只返回最新年份的数据
        if (dataYear != null) {
            final Integer finalDataYear = dataYear;
            lines = lines.stream()
                    .filter(l -> l.getYear().intValue() == finalDataYear)
                    .collect(Collectors.toList());
        }

        return BatchLineListVO.builder()
                .dataYear(dataYear)
                .isCurrentYear(false)
                .batches(convertToBatchLineVOs(lines))
                .build();
    }

    @Override
    @Transactional
    public Long saveArchive(GaokaoArchiveSaveDTO dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        // 校验改革模式
        if (!ReformModelEnum.isValid(dto.getReformModel())) {
            throw new BusinessException(400, "改革模式无效");
        }

        // 处理传统文理模式的科目映射
        processTraditionalSubjects(dto);

        MemberGaokao existing = memberGaokaoMapper.selectOne(
                new LambdaQueryWrapper<MemberGaokao>()
                        .eq(MemberGaokao::getMemberId, memberId)
        );

        MemberGaokao entity = convertToEntity(dto);
        entity.setMemberId(memberId);

        // 自动计算线差
        if (dto.getScore() != null && dto.getBatchLineScore() != null) {
            entity.setScoreAboveLine(dto.getScore() - dto.getBatchLineScore());
        }

        if (existing == null) {
            entity.setId(SnowflakeIdGenerator.nextId());
            entity.setCreatedAt(OffsetDateTime.now());
            entity.setUpdatedAt(OffsetDateTime.now());
            memberGaokaoMapper.insert(entity);
            log.info("创建高考档案成功: memberId={}, archiveId={}", memberId, entity.getId());
        } else {
            entity.setId(existing.getId());
            entity.setCreatedAt(existing.getCreatedAt());
            entity.setUpdatedAt(OffsetDateTime.now());
            memberGaokaoMapper.updateById(entity);
            log.info("更新高考档案成功: memberId={}, archiveId={}", memberId, entity.getId());
        }

        return entity.getId();
    }

    @Override
    public GaokaoArchiveVO getMyArchive() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        MemberGaokao archive = memberGaokaoMapper.selectOne(
                new LambdaQueryWrapper<MemberGaokao>()
                        .eq(MemberGaokao::getMemberId, memberId)
        );

        if (archive == null) {
            return null;
        }

        return convertToVO(archive);
    }

    // ==================== 私有方法 ====================

    /**
     * 判断改革模式
     */
    private String determineReformModel(String province, Integer gaokaoYear) {
        List<ProvinceReform> reforms = provinceReformMapper.selectList(
                new LambdaQueryWrapper<ProvinceReform>()
                        .eq(ProvinceReform::getProvince, province)
                        .orderByAsc(ProvinceReform::getReformYear)
        );

        if (reforms.isEmpty()) {
            return ReformModelEnum.TRADITIONAL.getValue();
        }

        // 倒序遍历，找到第一个 reformYear <= gaokaoYear 的配置
        for (int i = reforms.size() - 1; i >= 0; i--) {
            ProvinceReform reform = reforms.get(i);
            if (reform.getReformYear() == null) {
                return ReformModelEnum.TRADITIONAL.getValue();
            }
            if (gaokaoYear >= reform.getReformYear()) {
                return reform.getReformModel();
            }
        }

        return ReformModelEnum.TRADITIONAL.getValue();
    }

    /**
     * 构建可选科目选项
     */
    private Map<String, List<String>> buildSubjectOptions(String reformModel) {
        Map<String, List<String>> subjects = new LinkedHashMap<>();

        if (ReformModelEnum.THREE_PLUS_THREE.getValue().equals(reformModel)) {
            // 3+3: 6选3
            subjects.put("first", new ArrayList<>(SUBJECTS_6));
        } else if (ReformModelEnum.THREE_PLUS_ONE_PLUS_TWO.getValue().equals(reformModel)) {
            // 3+1+2: 首选2选1，再选4选2
            subjects.put("first", new ArrayList<>(SUBJECTS_FIRST_312));
            subjects.put("second", new ArrayList<>(SUBJECTS_SECOND_312));
        } else {
            // 传统文理
            subjects.put("first", new ArrayList<>(SUBJECTS_TRADITIONAL));
        }

        return subjects;
    }

    /**
     * 处理传统文理模式的科目映射
     * 文科 → 政治、历史、地理
     * 理科 → 物理、化学、生物
     */
    private void processTraditionalSubjects(GaokaoArchiveSaveDTO dto) {
        if (!ReformModelEnum.TRADITIONAL.getValue().equals(dto.getReformModel())) {
            return;
        }

        String subjectType = dto.getSubjectType();
        if ("文科".equals(subjectType)) {
            dto.setSubjectType("政治");
            dto.setSecondSubjectType("历史");
            dto.setThirdSubjectType("地理");
        } else if ("理科".equals(subjectType)) {
            dto.setSubjectType("物理");
            dto.setSecondSubjectType("化学");
            dto.setThirdSubjectType("生物");
        }
    }

    /**
     * 转换为批次线 VO 列表
     */
    private List<BatchLineVO> convertToBatchLineVOs(List<BatchScoreLine> lines) {
        return lines.stream()
                .map(line -> BatchLineVO.builder()
                        .batch(line.getBatch())
                        .scoreLine(line.getScoreLine())
                        .rankLine(line.getRankLine())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * DTO 转 Entity
     */
    private MemberGaokao convertToEntity(GaokaoArchiveSaveDTO dto) {
        return MemberGaokao.builder()
                .gaokaoYear(dto.getGaokaoYear())
                .gaokaoProvince(dto.getGaokaoProvince())
                .score(dto.getScore())
                .rank(dto.getRank())
                .reformModel(dto.getReformModel())
                .subjectType(dto.getSubjectType())
                .secondSubjectType(dto.getSecondSubjectType())
                .thirdSubjectType(dto.getThirdSubjectType())
                .scoreChinese(dto.getScoreChinese())
                .scoreMath(dto.getScoreMath())
                .scoreEnglish(dto.getScoreEnglish())
                .scoreSubject1(dto.getScoreSubject1())
                .scoreSubject2(dto.getScoreSubject2())
                .scoreSubject3(dto.getScoreSubject3())
                .foreignLanguage(dto.getForeignLanguage())
                .isColorBlind(dto.getIsColorBlind())
                .isColorWeak(dto.getIsColorWeak())
                .visionLeft(dto.getVisionLeft())
                .visionRight(dto.getVisionRight())
                .hasSmellDisorder(dto.getHasSmellDisorder())
                .heightCm(dto.getHeightCm())
                .weightKg(dto.getWeightKg())
                .isLeftHanded(dto.getIsLeftHanded())
                .hasTattoo(dto.getHasTattoo())
                .hasScar(dto.getHasScar())
                .hasStutter(dto.getHasStutter())
                .isFreshGraduate(dto.getIsFreshGraduate())
                .politicalStatus(dto.getPoliticalStatus())
                .householdType(dto.getHouseholdType())
                .isPovertyCounty(dto.getIsPovertyCounty())
                .batch(dto.getBatch())
                .batchDataYear(dto.getBatchDataYear())
                .batchLineScore(dto.getBatchLineScore())
                .build();
    }

    /**
     * Entity 转 VO
     */
    private GaokaoArchiveVO convertToVO(MemberGaokao entity) {
        return GaokaoArchiveVO.builder()
                .id(entity.getId())
                .gaokaoYear(entity.getGaokaoYear())
                .gaokaoProvince(entity.getGaokaoProvince())
                .score(entity.getScore())
                .rank(entity.getRank())
                .reformModel(entity.getReformModel())
                .subjectType(entity.getSubjectType())
                .secondSubjectType(entity.getSecondSubjectType())
                .thirdSubjectType(entity.getThirdSubjectType())
                .scoreChinese(entity.getScoreChinese())
                .scoreMath(entity.getScoreMath())
                .scoreEnglish(entity.getScoreEnglish())
                .scoreSubject1(entity.getScoreSubject1())
                .scoreSubject2(entity.getScoreSubject2())
                .scoreSubject3(entity.getScoreSubject3())
                .foreignLanguage(entity.getForeignLanguage())
                .isColorBlind(entity.getIsColorBlind())
                .isColorWeak(entity.getIsColorWeak())
                .visionLeft(entity.getVisionLeft())
                .visionRight(entity.getVisionRight())
                .hasSmellDisorder(entity.getHasSmellDisorder())
                .heightCm(entity.getHeightCm())
                .weightKg(entity.getWeightKg())
                .isLeftHanded(entity.getIsLeftHanded())
                .hasTattoo(entity.getHasTattoo())
                .hasScar(entity.getHasScar())
                .hasStutter(entity.getHasStutter())
                .isFreshGraduate(entity.getIsFreshGraduate())
                .politicalStatus(entity.getPoliticalStatus())
                .householdType(entity.getHouseholdType())
                .isPovertyCounty(entity.getIsPovertyCounty())
                .batch(entity.getBatch())
                .batchDataYear(entity.getBatchDataYear())
                .batchLineScore(entity.getBatchLineScore())
                .scoreAboveLine(entity.getScoreAboveLine())
                .build();
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/
git commit -m "feat(app): add GaokaoArchiveServiceImpl"
```

---

## Task 8: 创建 Controller

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/algorithm/GaokaoArchiveController.java`

- [ ] **Step 1: 创建 controller/algorithm 目录**

```bash
mkdir -p haifeng-app/src/main/java/com/haifeng/app/controller/algorithm
```

- [ ] **Step 2: 创建 GaokaoArchiveController**

```java
package com.haifeng.app.controller.algorithm;

import com.haifeng.app.dto.algorithm.GaokaoArchiveSaveDTO;
import com.haifeng.app.service.algorithm.GaokaoArchiveService;
import com.haifeng.app.vo.algorithm.*;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 高考档案控制器
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/gaokao")
@RequiredArgsConstructor
@RequireLogin
public class GaokaoArchiveController {

    private final GaokaoArchiveService gaokaoArchiveService;

    /**
     * 获取改革模式及可选科目
     *
     * @param province 省份
     * @param year     高考年份
     */
    @GetMapping("/reform-model")
    public R<ReformModelVO> getReformModel(
            @RequestParam @NotBlank(message = "省份不能为空") String province,
            @RequestParam @NotNull(message = "年份不能为空")
            @Min(value = 2020, message = "年份不能早于2020")
            @Max(value = 2030, message = "年份不能晚于2030") Integer year) {
        return R.ok(gaokaoArchiveService.getReformModel(province, year));
    }

    /**
     * 查询位次
     *
     * @param province    省份
     * @param year        年份
     * @param subjectType 科类（物理类/历史类/理科/文科）
     * @param score       分数
     */
    @GetMapping("/rank")
    public R<ScoreRankVO> getRank(
            @RequestParam @NotBlank(message = "省份不能为空") String province,
            @RequestParam @NotNull(message = "年份不能为空") Integer year,
            @RequestParam @NotBlank(message = "科类不能为空") String subjectType,
            @RequestParam @NotNull(message = "分数不能为空")
            @Min(value = 0, message = "分数不能小于0")
            @Max(value = 750, message = "分数不能大于750") Integer score) {
        return R.ok(gaokaoArchiveService.getRank(province, year, subjectType, score));
    }

    /**
     * 获取批次分数线列表
     *
     * @param province    省份
     * @param year        年份
     * @param subjectType 科类
     */
    @GetMapping("/batch-lines")
    public R<BatchLineListVO> getBatchLines(
            @RequestParam @NotBlank(message = "省份不能为空") String province,
            @RequestParam @NotNull(message = "年份不能为空") Integer year,
            @RequestParam @NotBlank(message = "科类不能为空") String subjectType) {
        return R.ok(gaokaoArchiveService.getBatchLines(province, year, subjectType));
    }

    /**
     * 保存高考档案（新增或更新）
     */
    @PostMapping("/archive")
    public R<Long> saveArchive(@Valid @RequestBody GaokaoArchiveSaveDTO dto) {
        Long archiveId = gaokaoArchiveService.saveArchive(dto);
        return R.ok(archiveId);
    }

    /**
     * 获取当前用户的高考档案
     */
    @GetMapping("/archive")
    public R<GaokaoArchiveVO> getMyArchive() {
        return R.ok(gaokaoArchiveService.getMyArchive());
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/controller/algorithm/
git commit -m "feat(app): add GaokaoArchiveController with 5 APIs"
```

---

## Task 9: 验证与最终提交

- [ ] **Step 1: 编译检查**

```bash
cd haifeng-common && mvn compile -q && cd ..
cd haifeng-app && mvn compile -q && cd ..
```

预期：BUILD SUCCESS

- [ ] **Step 2: 启动应用验证 Flyway 迁移**

```bash
cd haifeng-admin && mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

预期：Flyway 执行 V17 迁移成功，无报错

- [ ] **Step 3: 最终提交**

```bash
git add .
git commit -m "feat(app): complete gaokao archive module

- Add Flyway migration V17 for t_member_gaokao table
- Add 4 enums: ReformModelEnum, ForeignLanguageEnum, PoliticalStatusEnum, HouseholdTypeEnum
- Add MemberGaokao entity and mapper
- Add GaokaoArchiveController with 5 APIs:
  - GET /reform-model - get reform mode and subject options
  - GET /rank - query score rank
  - GET /batch-lines - get batch score lines
  - POST /archive - save gaokao archive
  - GET /archive - get my archive
- Add GaokaoArchiveService with full implementation
- Add DTOs and VOs for request/response"
```

---

## 总结

| 任务 | 文件数 | 说明 |
|------|--------|------|
| Task 1 | 1 | Flyway 迁移文件 |
| Task 2 | 4 | 枚举类 |
| Task 3 | 2 | 实体类 + Mapper |
| Task 4 | 5 | VO 类 |
| Task 5 | 1 | DTO 类 |
| Task 6 | 1 | Service 接口 |
| Task 7 | 1 | Service 实现 |
| Task 8 | 1 | Controller |
| Task 9 | - | 验证与提交 |

**总计：16 个新文件**
