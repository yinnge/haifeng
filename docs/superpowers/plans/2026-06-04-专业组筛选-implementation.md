# 专业组筛选（两步式）实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 `/api/v1/app/admission/group/page` 新增 4 维度筛选（大学/城市/专业/学科评估）+ 大学名模糊搜索，采用两步式工作流（`/filter/universities` + `/filter/majors` → `/group/page`），与现有 Redis ZSet 安全系数范围查询共存。

**Architecture:**
- 三层筛选 API：先取大学选项 → 再取专业选项 → 最后回传 ID 列表到 `group/page`
- 双路径：`group/page` 在新筛选全空时走原 ZSet 范围分页（零回归）；有任一新筛选时走 ZSet 全量拉 + 内存过滤
- 公共层只放 DTO/Mapper/SQL；Service 编排；Controller 暴露；Entity 不动

**Tech Stack:** Spring Boot 3.3.5 + MyBatis-Plus 3.5.7 + PostgreSQL + Redis + Flyway + Lombok + JUnit 5 + Mockito

---

## 实施顺序概览

```
第 1 段:公共层
  Task 1:  PopulationBucketEnum + 单元测试（TDD）
  Task 2:  RedisKeyConstant 加 UNIVERSITY_TAGS_DISTINCT
  Task 3:  UniversityMapper 加 selectByFilter + selectDistinctTags
  Task 4:  MajorMapper 加 selectByFilter
  Task 5:  AdmissionMajorScoreMapper 加 selectGroupIdsByMajorCodes

第 2 段:DTO / VO
  Task 6:  AdmissionUniversityFilterDTO
  Task 7:  AdmissionMajorFilterDTO
  Task 8:  AdmissionUniversityOptionVO
  Task 9:  AdmissionMajorOptionVO
  Task 10: AdmissionGroupQueryDTO 加 3 字段 + 校验

第 3 段:Service / Controller
  Task 11: AdmissionQueryService 接口加 3 方法签名
  Task 12: 实现 filterUniversities
  Task 13: 实现 filterMajors
  Task 14: 实现 listUniversityTags（含 Redis 缓存）
  Task 15: AdmissionQueryController 加 3 个接口

第 4 段:扩展 group/page
  Task 16: pageGroupsForPaid 加新筛选分支（双路径）
  Task 17: pageGroupsForPaidFallback 加新筛选过滤
  Task 18: pageGroupsForFree 加新筛选过滤

第 5 段:测试
  Task 19: filterUniversities / filterMajors / listUniversityTags 单测（Mockito）
  Task 20: group/page 双路径单测（回归保护）

第 6 段:文档
  Task 21: 写 haifeng-app/Need/AL6-筛选对接文档.md
```

---

## 第 1 段:公共层

### Task 1: PopulationBucketEnum + 单元测试（TDD）

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/enums/PopulationBucketEnum.java`
- Create: `haifeng-common/src/test/java/com/haifeng/common/enums/PopulationBucketEnumTest.java`

- [ ] **Step 1: 写失败的测试**

在 `haifeng-common/src/test/java/com/haifeng/common/enums/PopulationBucketEnumTest.java`:

```java
package com.haifeng.common.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PopulationBucketEnumTest {

    @Test
    void getMin_LT_2000_returnsZero() {
        assertEquals(0, PopulationBucketEnum.LT_2000.getMin());
    }

    @Test
    void getMax_LT_2000_returns2000() {
        assertEquals(2000, PopulationBucketEnum.LT_2000.getMax());
    }

    @Test
    void getMin_BTW_2000_3000_returns2000() {
        assertEquals(2000, PopulationBucketEnum.BTW_2000_3000.getMin());
    }

    @Test
    void getMax_BTW_2000_3000_returns3000() {
        assertEquals(3000, PopulationBucketEnum.BTW_2000_3000.getMax());
    }

    @Test
    void getMin_BTW_3000_4000_returns3000() {
        assertEquals(3000, PopulationBucketEnum.BTW_3000_4000.getMin());
    }

    @Test
    void getMax_BTW_3000_4000_returns4000() {
        assertEquals(4000, PopulationBucketEnum.BTW_3000_4000.getMax());
    }

    @Test
    void getMin_GTE_4000_returns4000() {
        assertEquals(4000, PopulationBucketEnum.GTE_4000.getMin());
    }

    @Test
    void getMax_GTE_4000_returnsNull() {
        assertNull(PopulationBucketEnum.GTE_4000.getMax(),
                "GTE_4000 上界为 +∞,用 null 表示无上界");
    }

    @Test
    void allFourBucketsExist() {
        assertEquals(4, PopulationBucketEnum.values().length);
    }
}
```

- [ ] **Step 2: 运行测试,确认失败**

```bash
cd D:\exeProject\ideaProjects\Project-HaiFeng
mvn -pl haifeng-common -Dtest=PopulationBucketEnumTest test
```

Expected: `BUILD FAILURE`,`PopulationBucketEnum cannot be resolved` 之类。

- [ ] **Step 3: 实现 PopulationBucketEnum**

在 `haifeng-common/src/main/java/com/haifeng/common/enums/PopulationBucketEnum.java`:

```java
package com.haifeng.common.enums;

import lombok.Getter;

/**
 * 城市常住人口区间桶
 * SQL 条件生成规则:
 *   population &gt;= getMin()
 *   [population &lt; getMax()]  ← 仅当 getMax() != null 时
 */
@Getter
public enum PopulationBucketEnum {

    LT_2000(0, 2000),
    BTW_2000_3000(2000, 3000),
    BTW_3000_4000(3000, 4000),
    GTE_4000(4000, null);

    private final int min;
    private final Integer max;

    PopulationBucketEnum(int min, Integer max) {
        this.min = min;
        this.max = max;
    }
}
```

- [ ] **Step 4: 重新运行测试,确认通过**

```bash
mvn -pl haifeng-common -Dtest=PopulationBucketEnumTest test
```

Expected: `BUILD SUCCESS`,9 tests passed。

- [ ] **Step 5: Commit**

```bash
cd D:\exeProject\ideaProjects\Project-HaiFeng
git add haifeng-common/src/main/java/com/haifeng/common/enums/PopulationBucketEnum.java
git add haifeng-common/src/test/java/com/haifeng/common/enums/PopulationBucketEnumTest.java
git commit -m "feat(common): 新增 PopulationBucketEnum(4 个人口桶,SQL 区间映射)"
```

---

### Task 2: RedisKeyConstant 加 UNIVERSITY_TAGS_DISTINCT

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/constant/RedisKeyConstant.java`

- [ ] **Step 1: 在 RedisKeyConstant 末尾加常量 + 工厂方法**

打开 `RedisKeyConstant.java`,在最后 `}` 之前(第 131 行 `getAlgoSafetyLockKey` 之后)添加:

```java
    /**
     * 大学 tags 字典缓存
     * value: List&lt;String&gt;(去重后的全部 tags)
     */
    public static final String UNIVERSITY_TAGS_DISTINCT_KEY = "haifeng:university:tags:distinct";
```

- [ ] **Step 2: 编译验证**

```bash
mvn -pl haifeng-common compile
```

Expected: `BUILD SUCCESS`。

- [ ] **Step 3: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/constant/RedisKeyConstant.java
git commit -m "feat(common): RedisKeyConstant 加 UNIVERSITY_TAGS_DISTINCT_KEY"
```

---

### Task 3: UniversityMapper 加 selectByFilter + selectDistinctTags

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/mapper/university/UniversityMapper.java`

- [ ] **Step 1: 替换文件内容**

将 `UniversityMapper.java` 完整替换为:

```java
package com.haifeng.common.mapper.university;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.dto.algorithm.admission.AdmissionUniversityFilterDTO;
import com.haifeng.common.vo.algorithm.admission.AdmissionUniversityOptionVO;
import com.haifeng.common.entity.university.University;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UniversityMapper extends BaseMapper<University> {

    @Select("SELECT id FROM t_universities WHERE name = #{name} AND status = 1 LIMIT 1")
    Long selectIdByName(@Param("name") String name);

    @Select("SELECT id, name, city_name AS cityName FROM t_universities WHERE name = #{name} AND status = 1 LIMIT 1")
    University selectIdAndCityByName(@Param("name") String name);

    @Select("SELECT * FROM t_universities WHERE name = #{name} AND status = 1 LIMIT 1")
    University selectByName(@Param("name") String name);

    /**
     * 多维筛选大学选项
     * 入参 AdmissionUniversityFilterDTO 中各字段:null/空=不限
     * - 同一字段多值: OR
     * - 跨字段: AND
     * LIMIT 500
     */
    @Select("<script>" +
            "SELECT DISTINCT u.id, u.name, u.city_name, u.tags " +
            "FROM t_universities u " +
            "<if test='dto.populationBucket != null'>" +
            "LEFT JOIN t_city c ON c.city_name = u.city_name " +
            "</if>" +
            "<if test='dto.evaluationGrades != null and dto.evaluationGrades.size() &gt; 0'>" +
            "INNER JOIN t_subject_evaluation se ON se.university_id = u.id " +
            "</if>" +
            "WHERE u.status = 1 " +
            "<if test='dto.tags != null and dto.tags.size() &gt; 0'>" +
            "AND u.tags &amp;&amp; CAST(#{tagsArray} AS text[]) " +
            "</if>" +
            "<if test='dto.nature != null and dto.nature.size() &gt; 0'>" +
            "AND u.nature IN " +
            "<foreach collection='dto.nature' item='v' open='(' separator=',' close=')'>#{v}</foreach> " +
            "</if>" +
            "<if test='dto.famousUnion != null and dto.famousUnion.size() &gt; 0'>" +
            "AND u.famous_union IN " +
            "<foreach collection='dto.famousUnion' item='v' open='(' separator=',' close=')'>#{v}</foreach> " +
            "</if>" +
            "<if test='dto.category != null and dto.category.size() &gt; 0'>" +
            "AND u.category IN " +
            "<foreach collection='dto.category' item='v' open='(' separator=',' close=')'>#{v}</foreach> " +
            "</if>" +
            "<if test='dto.educationLevel != null and dto.educationLevel.size() &gt; 0'>" +
            "AND u.education_level IN " +
            "<foreach collection='dto.educationLevel' item='v' open='(' separator=',' close=')'>#{v}</foreach> " +
            "</if>" +
            "<if test='dto.department != null and dto.department.size() &gt; 0'>" +
            "AND u.department IN " +
            "<foreach collection='dto.department' item='v' open='(' separator=',' close=')'>#{v}</foreach> " +
            "</if>" +
            "<if test='dto.provinces != null and dto.provinces.size() &gt; 0'>" +
            "AND u.province_name IN " +
            "<foreach collection='dto.provinces' item='v' open='(' separator=',' close=')'>#{v}</foreach> " +
            "</if>" +
            "<if test='dto.regions != null and dto.regions.size() &gt; 0'>" +
            "AND u.region IN " +
            "<foreach collection='dto.regions' item='v' open='(' separator=',' close=')'>#{v}</foreach> " +
            "</if>" +
            "<if test='dto.populationBucket != null'>" +
            "AND c.resident_population &gt;= #{popMin} " +
            "<if test='popMax != null'>" +
            "AND c.resident_population &lt; #{popMax} " +
            "</if>" +
            "</if>" +
            "<if test='dto.evaluationGrades != null and dto.evaluationGrades.size() &gt; 0'>" +
            "AND se.evaluation_grade IN " +
            "<foreach collection='dto.evaluationGrades' item='v' open='(' separator=',' close=')'>#{v}</foreach> " +
            "</if>" +
            "ORDER BY u.sort_order DESC, u.id DESC " +
            "LIMIT 500" +
            "</script>")
    List<AdmissionUniversityOptionVO> selectByFilter(
            @Param("dto") AdmissionUniversityFilterDTO dto,
            @Param("tagsArray") String tagsArray,
            @Param("popMin") Integer popMin,
            @Param("popMax") Integer popMax);

    /**
     * 取大学 tags 去重字典(全表)
     */
    @Select("SELECT DISTINCT unnest(tags) AS tag " +
            "FROM t_universities " +
            "WHERE status = 1 AND tags IS NOT NULL " +
            "ORDER BY tag")
    List<String> selectDistinctTags();
}
```

- [ ] **Step 2: 编译验证**

```bash
mvn -pl haifeng-common compile
```

Expected: `BUILD SUCCESS`(`AdmissionUniversityFilterDTO` 和 `AdmissionUniversityOptionVO` 还没建,会报找不到符号,但只 Task 1 阶段的错;先继续 Task 6 和 Task 8)

> **注**:这一步会因为引用了未建的 DTO/VO 而编译失败,是预期。继续做 Task 6 和 Task 8 后会通过。

- [ ] **Step 3: (暂不 commit)继续 Task 6、Task 8 后再一起 commit**

---

### Task 4: MajorMapper 加 selectByFilter

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/mapper/major/MajorMapper.java`

- [ ] **Step 1: 替换文件内容**

将 `MajorMapper.java` 完整替换为:

```java
package com.haifeng.common.mapper.major;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.dto.algorithm.admission.AdmissionMajorFilterDTO;
import com.haifeng.common.vo.algorithm.admission.AdmissionMajorOptionVO;
import com.haifeng.common.entity.major.Major;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MajorMapper extends BaseMapper<Major> {

    @Select("SELECT id FROM t_major WHERE major_code = #{majorCode} AND status = 1 LIMIT 1")
    Long selectIdByMajorCode(@Param("majorCode") String majorCode);

    @Select("SELECT COUNT(*) > 0 FROM t_major WHERE major_code = #{majorCode}")
    boolean existsByMajorCode(@Param("majorCode") String majorCode);

    @Select("SELECT * FROM t_major WHERE major_name = #{majorName} AND status = 1 LIMIT 1")
    Major findByMajorName(@Param("majorName") String majorName);

    @Select("SELECT major_code FROM t_major WHERE major_name = #{majorName} AND status = 1 LIMIT 1")
    String selectCodeByName(@Param("majorName") String majorName);

    /**
     * 多维筛选专业选项
     * - 同一字段多值: OR
     * - 跨字段: AND
     * LIMIT 1000
     */
    @Select("<script>" +
            "SELECT id, major_code, major_name, major_category, major_tags " +
            "FROM t_major " +
            "WHERE status = 1 " +
            "<if test='dto.majorCategories != null and dto.majorCategories.size() &gt; 0'>" +
            "AND major_category IN " +
            "<foreach collection='dto.majorCategories' item='v' open='(' separator=',' close=')'>#{v}</foreach> " +
            "</if>" +
            "<if test='dto.parentCategories != null and dto.parentCategories.size() &gt; 0'>" +
            "AND parent_category IN " +
            "<foreach collection='dto.parentCategories' item='v' open='(' separator=',' close=')'>#{v}</foreach> " +
            "</if>" +
            "<if test='dto.majorTags != null and dto.majorTags.size() &gt; 0'>" +
            "AND major_tags IN " +
            "<foreach collection='dto.majorTags' item='v' open='(' separator=',' close=')'>#{v}</foreach> " +
            "</if>" +
            "<if test='dto.majorTypes != null and dto.majorTypes.size() &gt; 0'>" +
            "AND major_type IN " +
            "<foreach collection='dto.majorTypes' item='v' open='(' separator=',' close=')'>#{v}</foreach> " +
            "</if>" +
            "ORDER BY id DESC " +
            "LIMIT 1000" +
            "</script>")
    List<AdmissionMajorOptionVO> selectByFilter(@Param("dto") AdmissionMajorFilterDTO dto);
}
```

- [ ] **Step 2: (暂不 commit)继续 Task 7、Task 9 后再一起 commit**

> **注**:这一步也会因为引用了未建的 DTO/VO 而编译失败,是预期。

---

### Task 5: AdmissionMajorScoreMapper 加 selectGroupIdsByMajorCodes

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/AdmissionMajorScoreMapper.java`

- [ ] **Step 1: 在文件末尾追加新方法**

在 `selectMajorHistoryItems` 方法之后(第 80 行 `}` 之后)添加:

```java
    /**
     * 给定 groupId 集合和 majorCode 集合,返回同时满足两条件的 groupId(去重)
     * 用于 group/page 的 majorCodes 内存过滤
     * 入参都非空才调用,否则业务层应短路
     */
    @Select("<script>" +
            "SELECT DISTINCT group_id " +
            "FROM t_admission_major_score " +
            "WHERE group_id IN " +
            "<foreach collection='groupIds' item='g' open='(' separator=',' close=')'>#{g}</foreach> " +
            "AND major_code IN " +
            "<foreach collection='majorCodes' item='c' open='(' separator=',' close=')'>#{c}</foreach>" +
            "</script>")
    List<Integer> selectGroupIdsByMajorCodes(
            @Param("groupIds") List<Integer> groupIds,
            @Param("majorCodes") List<String> majorCodes);
```

- [ ] **Step 2: 编译验证**

```bash
mvn -pl haifeng-common compile
```

Expected: `BUILD SUCCESS`。

- [ ] **Step 3: Commit**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/mapper/algorithm/AdmissionMajorScoreMapper.java
git commit -m "feat(common): AdmissionMajorScoreMapper 加 selectGroupIdsByMajorCodes"
```

---

## 第 2 段:DTO / VO

### Task 6: AdmissionUniversityFilterDTO

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/dto/algorithm/admission/AdmissionUniversityFilterDTO.java`

- [ ] **Step 1: 创建 DTO 文件**

```java
package com.haifeng.app.dto.algorithm.admission;

import com.haifeng.common.enums.PopulationBucketEnum;
import lombok.Data;

import java.util.List;

@Data
public class AdmissionUniversityFilterDTO {

    /** 大学标签: 985/211/双一流/部委直属 等;多选 OR */
    private List<String> tags;

    /** 院校性质: 公办/民办/中外合作;多选 OR */
    private List<String> nature;

    /** 知名联盟: 985/211/双一流/C9/E9/中坚9校 等;多选 OR */
    private List<String> famousUnion;

    /** 院校类别: 综合/理工/师范/医药/农林/财经/政法/语言/艺术/民族/体育/军事;多选 OR */
    private List<String> category;

    /** 办学层次: 本科/专科/本专兼招;多选 OR */
    private List<String> educationLevel;

    /** 隶属部门: 教育部/工信部 等;多选 OR */
    private List<String> department;

    /** 省份: 北京/上海 等;多选 OR */
    private List<String> provinces;

    /** 地区: 华东/华北 等;多选 OR */
    private List<String> regions;

    /** 城市常住人口桶(单选) */
    private PopulationBucketEnum populationBucket;

    /** 学科评估等级: A+/A/A-/B+/B/B-/C+/C/C-;多选 OR */
    private List<String> evaluationGrades;
}
```

- [ ] **Step 2: 编译验证**

```bash
mvn -pl haifeng-app compile
```

Expected: `BUILD SUCCESS`(因为 Task 4 的 `selectByFilter` 引用了这个 DTO,会通过)。

- [ ] **Step 3: (暂不 commit)继续 Task 8 后再一起 commit**

---

### Task 7: AdmissionMajorFilterDTO

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/dto/algorithm/admission/AdmissionMajorFilterDTO.java`

- [ ] **Step 1: 创建 DTO 文件**

```java
package com.haifeng.app.dto.algorithm.admission;

import lombok.Data;

import java.util.List;

@Data
public class AdmissionMajorFilterDTO {

    /** 专业大类: 工学/理学/医学/文学 等;多选 OR */
    private List<String> majorCategories;

    /** 专业父类(专业大类下的子类): 计算机类/电子信息类 等;多选 OR */
    private List<String> parentCategories;

    /** 专业标签: 热门/紧缺/新兴/基础 等;多选 OR */
    private List<String> majorTags;

    /** 专业类型: 本科/专科/职教本科;多选 OR */
    private List<String> majorTypes;
}
```

- [ ] **Step 2: (暂不 commit)继续 Task 9 后再一起 commit**

---

### Task 8: AdmissionUniversityOptionVO

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/vo/algorithm/admission/AdmissionUniversityOptionVO.java`

- [ ] **Step 1: 创建 VO 文件**

```java
package com.haifeng.app.vo.algorithm.admission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdmissionUniversityOptionVO {

    /** 大学 ID(雪花主键,前端调 group/page 时回传) */
    private Long id;

    /** 大学名 */
    private String name;

    /** 城市名(前端展示用) */
    private String cityName;

    /** 大学 tags 列表(前端展示用) */
    private List<String> tags;
}
```

- [ ] **Step 2: 编译 + 与 Task 6 一起 commit**

```bash
mvn -pl haifeng-app compile
```

Expected: `BUILD SUCCESS`。

```bash
git add haifeng-common/src/main/java/com/haifeng/common/mapper/university/UniversityMapper.java
git add haifeng-common/src/main/java/com/haifeng/common/dto/algorithm/admission/AdmissionUniversityFilterDTO.java
git add haifeng-common/src/main/java/com/haifeng/common/vo/algorithm/admission/AdmissionUniversityOptionVO.java
git commit -m "feat(admission): /filter/universities 接入 UniversityMapper+FilterDTO+OptionVO"
```

---

### Task 9: AdmissionMajorOptionVO

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/vo/algorithm/admission/AdmissionMajorOptionVO.java`

- [ ] **Step 1: 创建 VO 文件**

```java
package com.haifeng.app.vo.algorithm.admission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdmissionMajorOptionVO {

    /**
     * t_major.id(雪花主键)
     * ⚠️ 仅用于前端展示/去重,不要回传给 group/page
     */
    private Long id;

    /**
     * t_admission_major_score.major_code(招生专业代码)
     * ✅ 前端调用 group/page 时使用这个字段过滤
     */
    private String majorCode;

    private String majorName;
    private String majorCategory;
    private String majorTags;
}
```

- [ ] **Step 2: 编译 + 与 Task 7 一起 commit**

```bash
mvn -pl haifeng-app compile
```

Expected: `BUILD SUCCESS`。

```bash
git add haifeng-common/src/main/java/com/haifeng/common/mapper/major/MajorMapper.java
git add haifeng-common/src/main/java/com/haifeng/common/dto/algorithm/admission/AdmissionMajorFilterDTO.java
git add haifeng-common/src/main/java/com/haifeng/common/vo/algorithm/admission/AdmissionMajorOptionVO.java
git commit -m "feat(admission): /filter/majors 接入 MajorMapper+FilterDTO+OptionVO"
```

---

### Task 10: AdmissionGroupQueryDTO 加 3 字段 + 自定义校验

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/dto/algorithm/admission/AdmissionGroupQueryDTO.java`

- [ ] **Step 1: 替换文件内容**

将 `AdmissionGroupQueryDTO.java` 完整替换为:

```java
package com.haifeng.app.dto.algorithm.admission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AdmissionGroupQueryDTO {

    @NotBlank(message = "批次不能为空")
    private String batch;

    private Boolean subjectFilter = false;

    private Integer page = 1;

    private Integer size = 20;

    /** 安全系数下限 0.00~1.00 */
    private BigDecimal minSafetyLevel;

    /** 安全系数上限 0.00~1.00 */
    private BigDecimal maxSafetyLevel;

    /** 排序方式: safety_desc(默认) / rank_asc */
    private String orderBy = "safety_desc";

    // ===== 新增:4 维度筛选 =====

    /** 大学 ID 列表(来自 /filter/universities);null/空=不限;最多 100 个 */
    @Size(max = 100, message = "universityIds 最多 100 个")
    private List<Long> universityIds;

    /** 专业代码列表(来自 /filter/majors);null/空=不限;最多 200 个 */
    @Size(max = 200, message = "majorCodes 最多 200 个")
    private List<String> majorCodes;

    /** 大学名模糊搜索;null/空=不限;最大长度 50 */
    @Size(max = 50, message = "universityName 最多 50 字符")
    private String universityName;
}
```

- [ ] **Step 2: 编译验证**

```bash
mvn -pl haifeng-app compile
```

Expected: `BUILD SUCCESS`。

- [ ] **Step 3: Commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/dto/algorithm/admission/AdmissionGroupQueryDTO.java
git commit -m "feat(admission): AdmissionGroupQueryDTO 加 3 字段(universityIds/majorCodes/universityName)"
```

---

## 第 3 段:Service / Controller

### Task 11: AdmissionQueryService 接口加 3 方法签名

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/algorithm/admission/AdmissionQueryService.java`

- [ ] **Step 1: 替换文件内容**

将 `AdmissionQueryService.java` 完整替换为:

```java
package com.haifeng.app.service.algorithm.admission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.algorithm.admission.AdmissionGroupQueryDTO;
import com.haifeng.common.dto.algorithm.admission.AdmissionMajorFilterDTO;
import com.haifeng.app.dto.algorithm.admission.AdmissionMajorQueryDTO;
import com.haifeng.common.dto.algorithm.admission.AdmissionUniversityFilterDTO;
import com.haifeng.app.vo.algorithm.admission.AdmissionGroupPageVO;
import com.haifeng.common.vo.algorithm.admission.AdmissionMajorOptionVO;
import com.haifeng.app.vo.algorithm.admission.AdmissionMajorPageVO;
import com.haifeng.common.vo.algorithm.admission.AdmissionUniversityOptionVO;

import java.util.List;

public interface AdmissionQueryService {

    /**
     * 分页查询专业组
     */
    IPage<AdmissionGroupPageVO> pageGroups(AdmissionGroupQueryDTO dto);

    /**
     * 分页查询专业明细
     */
    IPage<AdmissionMajorPageVO> pageMajors(AdmissionMajorQueryDTO dto);

    /**
     * 第一步筛选:大学选项
     * @param dto 4 维度筛选(大学/城市/学科评估)
     * @return 大学选项列表(上限 500,后端 LIMIT 500)
     */
    List<AdmissionUniversityOptionVO> filterUniversities(AdmissionUniversityFilterDTO dto);

    /**
     * 第二步筛选:专业选项
     * @param dto 1 维度筛选(专业)
     * @return 专业选项列表(上限 1000)
     */
    List<AdmissionMajorOptionVO> filterMajors(AdmissionMajorFilterDTO dto);

    /**
     * 辅助接口:大学 tags 字典(供前端下拉用)
     * Redis 缓存 1h
     */
    List<String> listUniversityTags();
}
```

- [ ] **Step 2: 编译**

```bash
mvn -pl haifeng-app compile
```

Expected: **编译失败**(`ServiceImpl` 还没实现),预期。直接进 Task 12。

- [ ] **Step 3: (暂不 commit)等 Task 15 一起 commit**

---

### Task 12: 实现 filterUniversities

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/admission/AdmissionQueryServiceImpl.java`

- [ ] **Step 1: 在 ServiceImpl 类内加 import**

在 `import` 段最后添加:

```java
import com.haifeng.common.dto.algorithm.admission.AdmissionMajorFilterDTO;
import com.haifeng.common.dto.algorithm.admission.AdmissionUniversityFilterDTO;
import com.haifeng.common.vo.algorithm.admission.AdmissionMajorOptionVO;
import com.haifeng.common.vo.algorithm.admission.AdmissionUniversityOptionVO;
import com.haifeng.common.enums.PopulationBucketEnum;
import com.haifeng.common.mapper.major.MajorMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import org.springframework.util.StringUtils;
```

- [ ] **Step 2: 加 mapper 依赖注入**

找到 `private final MemberGaokaoMapper memberGaokaoMapper;` 这一行(第 85 行),在它前面加:

```java
    private final UniversityMapper universityMapper;
    private final MajorMapper majorMapper;
```

> 这样 Lombok `@RequiredArgsConstructor` 自动给这两个 final 字段注入。

- [ ] **Step 3: 在 `releaseLock` 方法后面(第 322 行后)添加 filterUniversities 实现**

```java
    @Override
    public List<AdmissionUniversityOptionVO> filterUniversities(AdmissionUniversityFilterDTO dto) {
        if (dto == null) {
            return Collections.emptyList();
        }
        log.debug("/filter/universities 入参: tags={}, nature={}, famousUnion={}, category={}, " +
                        "educationLevel={}, department={}, provinces={}, regions={}, " +
                        "populationBucket={}, evaluationGrades={}",
                dto.getTags(), dto.getNature(), dto.getFamousUnion(), dto.getCategory(),
                dto.getEducationLevel(), dto.getDepartment(), dto.getProvinces(), dto.getRegions(),
                dto.getPopulationBucket(), dto.getEvaluationGrades());

        // 组装 tags 数组字符串(Postgres text[] 字面量: {"985","211"})
        String tagsArray = null;
        if (!CollectionUtils.isEmpty(dto.getTags())) {
            tagsArray = dto.getTags().stream()
                    .map(t -> "\"" + t.replace("\"", "\\\"") + "\"")
                    .collect(Collectors.joining(",", "{", "}"));
        }

        Integer popMin = null;
        Integer popMax = null;
        if (dto.getPopulationBucket() != null) {
            popMin = dto.getPopulationBucket().getMin();
            popMax = dto.getPopulationBucket().getMax();
        }

        long start = System.currentTimeMillis();
        List<AdmissionUniversityOptionVO> result =
                universityMapper.selectByFilter(dto, tagsArray, popMin, popMax);
        long cost = System.currentTimeMillis() - start;
        if (cost > 500) {
            log.warn("/filter/universities 慢查询 cost={}ms, dto={}", cost, dto);
        }
        return result == null ? Collections.emptyList() : result;
    }
```

- [ ] **Step 4: 编译验证**

```bash
mvn -pl haifeng-app compile
```

Expected: `BUILD SUCCESS`(可能还有 import 警告,确认无 ERROR 即可)。

- [ ] **Step 5: (暂不 commit)继续 Task 13**

---

### Task 13: 实现 filterMajors

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/admission/AdmissionQueryServiceImpl.java`

- [ ] **Step 1: 在 `filterUniversities` 方法之后追加**

```java
    @Override
    public List<AdmissionMajorOptionVO> filterMajors(AdmissionMajorFilterDTO dto) {
        if (dto == null) {
            return Collections.emptyList();
        }
        log.debug("/filter/majors 入参: majorCategories={}, parentCategories={}, majorTags={}, majorTypes={}",
                dto.getMajorCategories(), dto.getParentCategories(),
                dto.getMajorTags(), dto.getMajorTypes());

        long start = System.currentTimeMillis();
        List<AdmissionMajorOptionVO> result = majorMapper.selectByFilter(dto);
        long cost = System.currentTimeMillis() - start;
        if (cost > 500) {
            log.warn("/filter/majors 慢查询 cost={}ms, dto={}", cost, dto);
        }
        return result == null ? Collections.emptyList() : result;
    }
```

- [ ] **Step 2: 编译验证**

```bash
mvn -pl haifeng-app compile
```

Expected: `BUILD SUCCESS`。

- [ ] **Step 3: (暂不 commit)继续 Task 14**

---

### Task 14: 实现 listUniversityTags(Redis 缓存)

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/admission/AdmissionQueryServiceImpl.java`

- [ ] **Step 1: 在 `filterMajors` 方法之后追加**

```java
    @Override
    public List<String> listUniversityTags() {
        String key = RedisKeyConstant.UNIVERSITY_TAGS_DISTINCT_KEY;
        try {
            String cached = stringRedisTemplate.opsForValue().get(key);
            if (StringUtils.hasText(cached)) {
                // 简单协议:"," 分隔
                return Arrays.asList(cached.split(","));
            }
        } catch (Exception e) {
            log.error("Redis 读取大学 tags 失败,降级查 DB, key={}", key, e);
        }

        List<String> tags = universityMapper.selectDistinctTags();
        if (tags == null) {
            tags = Collections.emptyList();
        }

        try {
            stringRedisTemplate.opsForValue().set(
                    key, String.join(",", tags), Duration.ofHours(1));
        } catch (Exception e) {
            log.error("Redis 写入大学 tags 失败, key={}", key, e);
        }
        return tags;
    }
```

- [ ] **Step 2: 编译验证**

```bash
mvn -pl haifeng-app compile
```

Expected: `BUILD SUCCESS`。

- [ ] **Step 3: 与 Service 接口 + ServiceImpl 三方法一起 commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/service/algorithm/admission/AdmissionQueryService.java
git add haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/admission/AdmissionQueryServiceImpl.java
git commit -m "feat(admission): Service 加 filterUniversities/filterMajors/listUniversityTags 3 个方法"
```

---

### Task 15: AdmissionQueryController 加 3 个接口

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/controller/algorithm/admission/AdmissionQueryController.java`

- [ ] **Step 1: 替换文件内容**

将 `AdmissionQueryController.java` 完整替换为:

```java
package com.haifeng.app.controller.algorithm.admission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.algorithm.admission.AdmissionGroupQueryDTO;
import com.haifeng.common.dto.algorithm.admission.AdmissionMajorFilterDTO;
import com.haifeng.app.dto.algorithm.admission.AdmissionMajorQueryDTO;
import com.haifeng.common.dto.algorithm.admission.AdmissionUniversityFilterDTO;
import com.haifeng.app.service.algorithm.admission.AdmissionQueryService;
import com.haifeng.app.vo.algorithm.admission.AdmissionGroupPageVO;
import com.haifeng.common.vo.algorithm.admission.AdmissionMajorOptionVO;
import com.haifeng.app.vo.algorithm.admission.AdmissionMajorPageVO;
import com.haifeng.common.vo.algorithm.admission.AdmissionUniversityOptionVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/app/admission")
@RequiredArgsConstructor
@RequireLogin
public class AdmissionQueryController {

    private final AdmissionQueryService admissionQueryService;

    /**
     * 分页查询专业组(支持 4 维度筛选 + 大学名搜索)
     */
    @GetMapping("/group/page")
    public R<IPage<AdmissionGroupPageVO>> pageGroups(@Valid AdmissionGroupQueryDTO dto) {
        return R.ok(admissionQueryService.pageGroups(dto));
    }

    /**
     * 分页查询专业明细
     */
    @GetMapping("/major/page")
    public R<IPage<AdmissionMajorPageVO>> pageMajors(@Valid AdmissionMajorQueryDTO dto) {
        return R.ok(admissionQueryService.pageMajors(dto));
    }

    /**
     * 第一步筛选:大学选项
     */
    @GetMapping("/filter/universities")
    public R<List<AdmissionUniversityOptionVO>> filterUniversities(
            @Valid AdmissionUniversityFilterDTO dto) {
        return R.ok(admissionQueryService.filterUniversities(dto));
    }

    /**
     * 第二步筛选:专业选项
     */
    @GetMapping("/filter/majors")
    public R<List<AdmissionMajorOptionVO>> filterMajors(
            @Valid AdmissionMajorFilterDTO dto) {
        return R.ok(admissionQueryService.filterMajors(dto));
    }

    /**
     * 辅助:大学 tags 字典
     */
    @GetMapping("/filter/tags")
    public R<List<String>> listUniversityTags() {
        return R.ok(admissionQueryService.listUniversityTags());
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
mvn -pl haifeng-app compile
```

Expected: `BUILD SUCCESS`。

- [ ] **Step 3: Commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/controller/algorithm/admission/AdmissionQueryController.java
git commit -m "feat(admission): Controller 加 /filter/universities /filter/majors /filter/tags 3 个接口"
```

---

## 第 4 段:扩展 group/page

### Task 16: pageGroupsForPaid 加新筛选分支(双路径)

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/admission/AdmissionQueryServiceImpl.java`

- [ ] **Step 1: 找到 `pageGroupsForPaid` 方法(第 126 行),把整个方法体的前半段(从方法签名到 `catch` 块结束)替换为下面代码**

> 关键变更:在方法入口增加 `hasNewFilter` 判断,启用时调 `pageGroupsForPaidWithFilter`;不启用时走原 ZSet 范围分页路径(完全保留)。

```java
    private IPage<AdmissionGroupPageVO> pageGroupsForPaid(Long memberId, MemberGaokao gaokao, AdmissionGroupQueryDTO dto) {
        String province = gaokao.getGaokaoProvince();
        String batch = dto.getBatch();
        boolean subjectFilter = Boolean.TRUE.equals(dto.getSubjectFilter());
        String userSubjects = buildUserSubjectsArray(gaokao);

        // 判定是否有新筛选(任一非空 = 启用)
        boolean hasNewFilter = !CollectionUtils.isEmpty(dto.getUniversityIds())
                || !CollectionUtils.isEmpty(dto.getMajorCodes())
                || (dto.getUniversityName() != null && !dto.getUniversityName().isBlank());

        if (hasNewFilter) {
            return pageGroupsForPaidWithFilter(memberId, gaokao, dto, province, batch, subjectFilter, userSubjects);
        }

        // 原有 ZSet 范围分页路径(完全保留,零回归)
        String zsetKey = RedisKeyConstant.getAlgoSafetyZSetKey(memberId, batch, subjectFilter);
        ensureZSetExists(memberId, gaokao, zsetKey, province, batch, subjectFilter, userSubjects);

        double minScore = toDouble(dto.getMinSafetyLevel(), 0.00);
        double maxScore = toDouble(dto.getMaxSafetyLevel(), 1.00);
        String orderBy = normalizeOrderBy(dto.getOrderBy());
        boolean desc = ORDER_BY_SAFETY_DESC.equals(orderBy);

        int page = dto.getPage() == null ? 1 : Math.max(dto.getPage(), 1);
        int size = dto.getSize() == null ? PAGE_SIZE : Math.max(dto.getSize(), 1);
        long start = (long) (page - 1) * size;
        long end = start + size - 1;

        Set<ZSetOperations.TypedTuple<String>> tuples;
        try {
            if (desc) {
                tuples = stringRedisTemplate.opsForZSet().reverseRangeByScoreWithScores(
                        zsetKey, minScore, maxScore, start, size);
            } else {
                tuples = stringRedisTemplate.opsForZSet().rangeByScoreWithScores(
                        zsetKey, minScore, maxScore, start, size);
            }
        } catch (Exception e) {
            log.error("Redis ZSet 查询失败,降级为 DB 全算路径, memberId={}, key={}", memberId, zsetKey, e);
            return pageGroupsForPaidFallback(memberId, gaokao, dto, minScore, maxScore, desc);
        }
```

> 后续 `long total` 块到最后 `return result;` 完全保留(原代码第 163-223 行不动)。

- [ ] **Step 2: 在 `pageGroupsForPaidFallback` 之前(第 229 行)插入新方法 `pageGroupsForPaidWithFilter`**

```java
    /**
     * 付费用户走新筛选:ZSet 全量拉 + 内存过滤 + 内存分页
     */
    private IPage<AdmissionGroupPageVO> pageGroupsForPaidWithFilter(Long memberId, MemberGaokao gaokao,
                                                                    AdmissionGroupQueryDTO dto,
                                                                    String province, String batch,
                                                                    boolean subjectFilter, String userSubjects) {
        String zsetKey = RedisKeyConstant.getAlgoSafetyZSetKey(memberId, batch, subjectFilter);
        ensureZSetExists(memberId, gaokao, zsetKey, province, batch, subjectFilter, userSubjects);

        double minScore = toDouble(dto.getMinSafetyLevel(), 0.00);
        double maxScore = toDouble(dto.getMaxSafetyLevel(), 1.00);
        String orderBy = normalizeOrderBy(dto.getOrderBy());
        boolean desc = ORDER_BY_SAFETY_DESC.equals(orderBy);

        int page = dto.getPage() == null ? 1 : Math.max(dto.getPage(), 1);
        int size = dto.getSize() == null ? PAGE_SIZE : Math.max(dto.getSize(), 1);

        // 1. ZSet 全量拉
        Set<ZSetOperations.TypedTuple<String>> tuples;
        try {
            if (desc) {
                tuples = stringRedisTemplate.opsForZSet().reverseRangeByScoreWithScores(zsetKey, minScore, maxScore);
            } else {
                tuples = stringRedisTemplate.opsForZSet().rangeByScoreWithScores(zsetKey, minScore, maxScore);
            }
        } catch (Exception e) {
            log.error("Redis ZSet 全量拉失败,降级到 DB, memberId={}, key={}", memberId, zsetKey, e);
            return pageGroupsForPaidFallback(memberId, gaokao, dto, minScore, maxScore, desc);
        }
        log.info("ZSet 全量拉取: memberId={}, count={}", memberId, tuples == null ? 0 : tuples.size());

        if (tuples == null || tuples.isEmpty()) {
            return new Page<AdmissionGroupPageVO>(page, size).setTotal(0L);
        }

        // 2. 内存按 universityIds 过滤
        Set<Integer> univIdSet = dto.getUniversityIds() == null
                ? null
                : dto.getUniversityIds().stream().map(Long::intValue).collect(Collectors.toSet());

        List<Integer> candidateGroupIds = new ArrayList<>();
        Map<Integer, Double> scoreByGroupId = new HashMap<>();
        for (ZSetOperations.TypedTuple<String> t : tuples) {
            if (t.getValue() == null || t.getScore() == null) continue;
            Integer gid = Integer.parseInt(t.getValue());
            scoreByGroupId.put(gid, t.getScore());
            candidateGroupIds.add(gid);
        }

        // 3. 拉大学基本信息(用于按 universityName ILIKE 过滤 + universityId 校验)
        List<Integer> firstSlice = candidateGroupIds.size() > 2000 ? candidateGroupIds.subList(0, 2000) : candidateGroupIds;
        List<AdmissionGroup> candidateGroups = admissionGroupMapper.selectBatchIds(firstSlice);
        Map<Integer, AdmissionGroup> groupById = candidateGroups.stream()
                .collect(Collectors.toMap(AdmissionGroup::getId, g -> g, (a, b) -> a));

        // 4. universityIds 过滤
        if (univIdSet != null) {
            candidateGroupIds = candidateGroupIds.stream()
                    .filter(gid -> {
                        AdmissionGroup g = groupById.get(gid);
                        return g != null && univIdSet.contains(g.getUniversityId().intValue());
                    })
                    .collect(Collectors.toList());
        }

        // 5. universityName 模糊过滤
        if (dto.getUniversityName() != null && !dto.getUniversityName().isBlank()) {
            String pattern = "%" + dto.getUniversityName().trim() + "%";
            candidateGroupIds = candidateGroupIds.stream()
                    .filter(gid -> {
                        AdmissionGroup g = groupById.get(gid);
                        return g != null && g.getUniversityName() != null
                                && g.getUniversityName().toLowerCase().contains(dto.getUniversityName().trim().toLowerCase());
                    })
                    .collect(Collectors.toList());
            // pattern 仅为语义保留
            if (pattern == null) return new Page<>(0);
        }

        // 6. majorCodes 过滤(查一次 DB)
        if (!CollectionUtils.isEmpty(dto.getMajorCodes()) && !candidateGroupIds.isEmpty()) {
            List<Integer> filteredByMajor = admissionMajorScoreMapper.selectGroupIdsByMajorCodes(
                    candidateGroupIds, dto.getMajorCodes());
            candidateGroupIds = filteredByMajor;
        }

        // 7. 内存分页
        long total = candidateGroupIds.size();
        int from = Math.min((page - 1) * size, candidateGroupIds.size());
        int to = Math.min(from + size, candidateGroupIds.size());
        List<Integer> pageGroupIds = candidateGroupIds.subList(from, to);

        if (pageGroupIds.isEmpty()) {
            return new Page<AdmissionGroupPageVO>(page, size).setTotal(total);
        }

        // 8. 回查 DB + 拼 VO(复用 buildGroupVO)
        List<AdmissionGroup> pageGroups = admissionGroupMapper.selectBatchIds(pageGroupIds);
        Map<Integer, AdmissionGroup> pageGroupById = pageGroups.stream()
                .collect(Collectors.toMap(AdmissionGroup::getId, g -> g, (a, b) -> a));

        SharedContext ctx = buildSharedContext(gaokao, pageGroups, new ArrayList<>(pageGroupIds));

        List<AdmissionGroupPageVO> voList = new ArrayList<>();
        for (Integer gid : pageGroupIds) {
            AdmissionGroup g = pageGroupById.get(gid);
            if (g == null) continue;
            Double score = scoreByGroupId.get(gid);
            BigDecimal safetyLevel = score == null
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
            SafetyLevelDict dict = safetyLevelDictCache.getByCoefficient(safetyLevel);
            String levelShort = dict != null ? dict.getNameShort() : "稳";
            String description = dict != null ? dict.getDescription() : "";

            AdmissionGroupPageVO vo = buildGroupVO(g,
                    ctx.historyMap.getOrDefault(g.getUniversityId() + "_" + g.getGroupCode(), Collections.emptyList()),
                    ctx.majorsByGroupId.getOrDefault(g.getId(), Collections.emptyList()),
                    ctx.majorHistoryByGroupAndCode.getOrDefault(g.getId(), Collections.emptyMap()),
                    gaokao, ctx.userConstraints, ctx.severityMap,
                    ctx.density, ctx.provinceConfig, ctx.reformYear);
            vo.setSafetyLevel(safetyLevel);
            vo.setLevelShort(levelShort);
            vo.setSafetyDescription(description);
            vo.setMasked(false);
            voList.add(vo);
        }

        Page<AdmissionGroupPageVO> result = new Page<>(page, size);
        result.setRecords(voList);
        result.setTotal(total);
        return result;
    }
```

- [ ] **Step 3: 编译验证**

```bash
mvn -pl haifeng-app compile
```

Expected: `BUILD SUCCESS`。

- [ ] **Step 4: Commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/admission/AdmissionQueryServiceImpl.java
git commit -m "feat(admission): pageGroupsForPaid 加新筛选分支(ZSet 全量+内存过滤+内存分页)"
```

---

### Task 17: pageGroupsForPaidFallback 加新筛选过滤

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/admission/AdmissionQueryServiceImpl.java`

- [ ] **Step 1: 找到 `pageGroupsForPaidFallback` 方法中 `List<AdmissionGroup> allGroups = admissionGroupMapper.selectAllByCondition(...)` 之后、`if (allGroups.isEmpty())` 之前,插入筛选逻辑**

在 `if (allGroups.isEmpty())` 这一行**之前**插入:

```java
        // 新筛选过滤(在内存层做)
        allGroups = applyNewFilters(allGroups, dto);
```

完整 patch 上下文(把整个 if-else 块替换):

```java
        List<AdmissionGroup> allGroups = admissionGroupMapper.selectAllByCondition(province, batch, subjectFilter, userSubjects);
        allGroups = applyNewFilters(allGroups, dto);   // <-- 新增
        if (allGroups.isEmpty()) {
            return new Page<AdmissionGroupPageVO>(dto.getPage(), PAGE_SIZE).setTotal(0L);
        }
```

- [ ] **Step 2: 在 `pageGroupsForPaidWithFilter` 方法之后(就是 fallback 之前)添加辅助方法 `applyNewFilters`**

```java
    /**
     * 在 AdmissionGroup 列表层应用新筛选(universityIds/majorCodes/universityName)
     * 复用入口:paid 走新筛选路径、paid fallback、free 三处
     */
    private List<AdmissionGroup> applyNewFilters(List<AdmissionGroup> groups, AdmissionGroupQueryDTO dto) {
        if (CollectionUtils.isEmpty(groups)) {
            return groups;
        }
        boolean hasNewFilter = !CollectionUtils.isEmpty(dto.getUniversityIds())
                || !CollectionUtils.isEmpty(dto.getMajorCodes())
                || (dto.getUniversityName() != null && !dto.getUniversityName().isBlank());
        if (!hasNewFilter) {
            return groups;
        }

        // 1. universityIds
        Set<Integer> univIdSet = null;
        if (!CollectionUtils.isEmpty(dto.getUniversityIds())) {
            univIdSet = dto.getUniversityIds().stream()
                    .map(Long::intValue).collect(Collectors.toSet());
        }

        // 2. majorCodes
        Set<Integer> filteredByMajor = null;
        if (!CollectionUtils.isEmpty(dto.getMajorCodes())) {
            List<Integer> groupIds = groups.stream().map(AdmissionGroup::getId).collect(Collectors.toList());
            filteredByMajor = new HashSet<>(
                    admissionMajorScoreMapper.selectGroupIdsByMajorCodes(groupIds, dto.getMajorCodes()));
        }

        String nameKey = dto.getUniversityName() == null || dto.getUniversityName().isBlank()
                ? null
                : dto.getUniversityName().trim().toLowerCase();

        return groups.stream()
                .filter(g -> univIdSet == null
                        || (g.getUniversityId() != null && univIdSet.contains(g.getUniversityId().intValue())))
                .filter(g -> filteredByMajor == null || filteredByMajor.contains(g.getId()))
                .filter(g -> nameKey == null
                        || (g.getUniversityName() != null && g.getUniversityName().toLowerCase().contains(nameKey)))
                .collect(Collectors.toList());
    }
```

- [ ] **Step 3: 编译验证**

```bash
mvn -pl haifeng-app compile
```

Expected: `BUILD SUCCESS`。

- [ ] **Step 4: Commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/admission/AdmissionQueryServiceImpl.java
git commit -m "feat(admission): pageGroupsForPaidFallback + applyNewFilters 抽出复用"
```

---

### Task 18: pageGroupsForFree 加新筛选过滤

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/admission/AdmissionQueryServiceImpl.java`

- [ ] **Step 1: 找到 `pageGroupsForFree` 方法中 `List<AdmissionGroup> groups = admissionGroupMapper.selectPageByCondition(...)` 之后**

在 `if (groups.isEmpty()) {` 之前插入:

```java
        groups = applyNewFilters(groups, dto);
```

完整 patch 上下文:

```java
        List<AdmissionGroup> groups = admissionGroupMapper.selectPageByCondition(
                province, batch, subjectFilter, userSubjects, size, offset);
        groups = applyNewFilters(groups, dto);   // <-- 新增
        long total = admissionGroupMapper.countByCondition(province, batch, subjectFilter, userSubjects);
```

- [ ] **Step 2: 编译验证**

```bash
mvn -pl haifeng-app compile
```

Expected: `BUILD SUCCESS`。

- [ ] **Step 3: Commit**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/admission/AdmissionQueryServiceImpl.java
git commit -m "feat(admission): pageGroupsForFree 接入 applyNewFilters(10 条限制保留)"
```

---

## 第 5 段:测试

### Task 19: filterUniversities / filterMajors / listUniversityTags 单测

**Files:**
- Create: `haifeng-app/src/test/java/com/haifeng/app/service/impl/algorithm/admission/AdmissionQueryServiceImplFilterTest.java`

- [ ] **Step 1: 写测试文件**

```java
package com.haifeng.app.service.impl.algorithm.admission;

import com.haifeng.common.dto.algorithm.admission.AdmissionMajorFilterDTO;
import com.haifeng.common.dto.algorithm.admission.AdmissionUniversityFilterDTO;
import com.haifeng.common.vo.algorithm.admission.AdmissionMajorOptionVO;
import com.haifeng.common.vo.algorithm.admission.AdmissionUniversityOptionVO;
import com.haifeng.common.enums.PopulationBucketEnum;
import com.haifeng.common.mapper.major.MajorMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdmissionQueryServiceImplFilterTest {

    @Mock private UniversityMapper universityMapper;
    @Mock private MajorMapper majorMapper;
    @Mock private StringRedisTemplate stringRedisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks private AdmissionQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        // StringRedisTemplate 注入由 @InjectMocks 处理
    }

    // ========== filterUniversities ==========

    @Test
    void filterUniversities_nullDto_returnsEmpty() {
        assertTrue(service.filterUniversities(null).isEmpty());
    }

    @Test
    void filterUniversities_allFieldsNull_passesNullsToMapper() {
        AdmissionUniversityFilterDTO dto = new AdmissionUniversityFilterDTO();
        when(universityMapper.selectByFilter(eq(dto), isNull(), isNull(), isNull()))
                .thenReturn(Collections.emptyList());
        List<AdmissionUniversityOptionVO> result = service.filterUniversities(dto);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(universityMapper).selectByFilter(eq(dto), isNull(), isNull(), isNull());
    }

    @Test
    void filterUniversities_populationBucket_LT_2000_setsMin0Max2000() {
        AdmissionUniversityFilterDTO dto = new AdmissionUniversityFilterDTO();
        dto.setPopulationBucket(PopulationBucketEnum.LT_2000);
        when(universityMapper.selectByFilter(eq(dto), isNull(), eq(0), eq(2000)))
                .thenReturn(Collections.emptyList());
        service.filterUniversities(dto);
        verify(universityMapper).selectByFilter(eq(dto), isNull(), eq(0), eq(2000));
    }

    @Test
    void filterUniversities_populationBucket_GTE_4000_passesNullMax() {
        AdmissionUniversityFilterDTO dto = new AdmissionUniversityFilterDTO();
        dto.setPopulationBucket(PopulationBucketEnum.GTE_4000);
        when(universityMapper.selectByFilter(eq(dto), isNull(), eq(4000), isNull()))
                .thenReturn(Collections.emptyList());
        service.filterUniversities(dto);
        verify(universityMapper).selectByFilter(eq(dto), isNull(), eq(4000), isNull());
    }

    @Test
    void filterUniversities_tags_quotedAsPGArray() {
        AdmissionUniversityFilterDTO dto = new AdmissionUniversityFilterDTO();
        dto.setTags(List.of("985", "双一流"));
        when(universityMapper.selectByFilter(eq(dto), eq("{\"985\",\"双一流\"}"), isNull(), isNull()))
                .thenReturn(Collections.emptyList());
        service.filterUniversities(dto);
        verify(universityMapper).selectByFilter(eq(dto), eq("{\"985\",\"双一流\"}"), isNull(), isNull());
    }

    @Test
    void filterUniversities_returnsEmptyWhenMapperReturnsNull() {
        AdmissionUniversityFilterDTO dto = new AdmissionUniversityFilterDTO();
        when(universityMapper.selectByFilter(any(), any(), any(), any())).thenReturn(null);
        assertTrue(service.filterUniversities(dto).isEmpty());
    }

    // ========== filterMajors ==========

    @Test
    void filterMajors_nullDto_returnsEmpty() {
        assertTrue(service.filterMajors(null).isEmpty());
    }

    @Test
    void filterMajors_delegatesToMapper() {
        AdmissionMajorFilterDTO dto = new AdmissionMajorFilterDTO();
        dto.setMajorCategories(List.of("工学"));
        AdmissionMajorOptionVO vo = AdmissionMajorOptionVO.builder()
                .id(1L).majorCode("080901").majorName("计算机").build();
        when(majorMapper.selectByFilter(dto)).thenReturn(List.of(vo));
        List<AdmissionMajorOptionVO> result = service.filterMajors(dto);
        assertEquals(1, result.size());
        assertEquals("080901", result.get(0).getMajorCode());
    }

    // ========== listUniversityTags ==========

    @Test
    void listUniversityTags_cacheHit_returnsFromRedis() {
        ReflectionTestUtils.setField(service, "stringRedisTemplate", stringRedisTemplate);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("haifeng:university:tags:distinct")).thenReturn("985,211,双一流");
        List<String> tags = service.listUniversityTags();
        assertEquals(List.of("985", "211", "双一流"), tags);
        verify(universityMapper, never()).selectDistinctTags();
    }

    @Test
    void listUniversityTags_cacheMiss_loadsFromDbAndCaches() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("haifeng:university:tags:distinct")).thenReturn(null);
        when(universityMapper.selectDistinctTags()).thenReturn(List.of("985", "211"));
        when(valueOperations.set(eq("haifeng:university:tags:distinct"), eq("985,211"), any()))
                .thenReturn(Boolean.TRUE);
        List<String> tags = service.listUniversityTags();
        assertEquals(List.of("985", "211"), tags);
        verify(valueOperations).set(eq("haifeng:university:tags:distinct"), eq("985,211"), any());
    }

    @Test
    void listUniversityTags_redisDown_returnsFromDb() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis down"));
        when(universityMapper.selectDistinctTags()).thenReturn(List.of("985"));
        List<String> tags = service.listUniversityTags();
        assertEquals(List.of("985"), tags);
    }
}
```

- [ ] **Step 2: 运行测试**

```bash
mvn -pl haifeng-app -Dtest=AdmissionQueryServiceImplFilterTest test
```

Expected: `BUILD SUCCESS`,11 tests passed。

> 失败可能原因:
> - `@InjectMocks` 注入 `AdmissionQueryServiceImpl` 时需要 `StringRedisTemplate` 等 11 个依赖 — `@Mock` 仅覆盖 3 个(UniversityMapper/MajorMapper/StringRedisTemplate),其他依赖 null;`@InjectMocks` 只注入匹配的字段,其他 null。`service.filterUniversities` 只用 `UniversityMapper` + `log`,应能跑通。`listUniversityTags` 用 `StringRedisTemplate` 也要 mock。
> - `ReflectionTestUtils.setField(service, "stringRedisTemplate", stringRedisTemplate)` 在 `@InjectMocks` 已经自动注入时会冲突;把 `setUp` 删掉就行(让 `@InjectMocks` 处理)。

- [ ] **Step 3: 如果失败,调整后重跑**

如果 `service.filterUniversities` 调用时 `log` 报 NPE,这是因为 `AdmissionQueryServiceImpl` 用 `@Slf4j` 注入了 `log` 字段,不需要 mock;`@InjectMocks` 不会触碰非 final 且非 null 字段。

- [ ] **Step 4: Commit**

```bash
git add haifeng-app/src/test/java/com/haifeng/app/service/impl/algorithm/admission/AdmissionQueryServiceImplFilterTest.java
git commit -m "test(admission): filterUniversities/filterMajors/listUniversityTags 单测覆盖"
```

---

### Task 20: group/page 双路径单测(回归保护)

**Files:**
- Create: `haifeng-app/src/test/java/com/haifeng/app/service/impl/algorithm/admission/AdmissionQueryServiceImplPageGroupsTest.java`

- [ ] **Step 1: 写测试文件**

```java
package com.haifeng.app.service.impl.algorithm.admission;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.algorithm.admission.AdmissionGroupQueryDTO;
import com.haifeng.common.entity.algorithm.AdmissionGroup;
import com.haifeng.common.entity.algorithm.MemberGaokao;
import com.haifeng.common.entity.algorithm.SafetyLevelDict;
import com.haifeng.common.mapper.algorithm.AdmissionGroupMapper;
import com.haifeng.common.mapper.algorithm.AdmissionMajorScoreMapper;
import com.haifeng.common.mapper.algorithm.ConstraintDictMapper;
import com.haifeng.common.mapper.algorithm.MemberGaokaoMapper;
import com.haifeng.common.mapper.algorithm.ProvinceConfigMapper;
import com.haifeng.common.mapper.algorithm.ProvinceReformMapper;
import com.haifeng.common.mapper.algorithm.ScoreRankMapper;
import com.haifeng.common.service.algorithm.matcher.ConstraintMatcherService;
import com.haifeng.common.service.algorithm.matcher.SubjectMatcher;
import com.haifeng.common.service.algorithm.safety.SafetyLevelDictCache;
import com.haifeng.common.service.algorithm.safety.SafetyLevelService;
import com.haifeng.common.util.RedisScanUtil;
import com.haifeng.common.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdmissionQueryServiceImplPageGroupsTest {

    @Mock private MemberGaokaoMapper memberGaokaoMapper;
    @Mock private AdmissionGroupMapper admissionGroupMapper;
    @Mock private AdmissionMajorScoreMapper admissionMajorScoreMapper;
    @Mock private SubjectMatcher subjectMatcher;
    @Mock private SafetyLevelService safetyLevelService;
    @Mock private SafetyLevelDictCache safetyLevelDictCache;
    @Mock private ConstraintMatcherService constraintMatcherService;
    @Mock private ScoreRankMapper scoreRankMapper;
    @Mock private ProvinceConfigMapper provinceConfigMapper;
    @Mock private ProvinceReformMapper provinceReformMapper;
    @Mock private ConstraintDictMapper constraintDictMapper;
    @Mock private StringRedisTemplate stringRedisTemplate;
    @Mock private RedisScanUtil redisScanUtil;
    @Mock private ZSetOperations<String, String> zSetOperations;

    private AdmissionQueryServiceImpl service;
    private AdmissionGroupQueryDTO dto;
    private MemberGaokao gaokao;

    @BeforeEach
    void setUp() {
        service = new AdmissionQueryServiceImpl(
                memberGaokaoMapper, admissionGroupMapper, admissionMajorScoreMapper,
                subjectMatcher, safetyLevelService, safetyLevelDictCache,
                constraintMatcherService, scoreRankMapper, provinceConfigMapper,
                provinceReformMapper, constraintDictMapper,
                stringRedisTemplate, redisScanUtil
        );
        dto = new AdmissionGroupQueryDTO();
        dto.setBatch("本科批");
        gaokao = MemberGaokao.builder()
                .gaokaoProvince("北京")
                .gaokaoYear((short) 2025)
                .subjectType("物理")
                .score(600)
                .build();
    }

    // ========== 回归保护:无新筛选 → 原 ZSet 路径 ==========

    @Test
    void pageGroups_paidNoNewFilter_usesZSetRangeByScore() {
        // 模拟 pro 用户,dto.orderBy 默认 "safety_desc" → 走 reverseRangeByScoreWithScores
        try (MockedStatic<SecurityUtil> sec = mockStatic(SecurityUtil.class)) {
            sec.when(SecurityUtil::getCurrentMemberId).thenReturn(1L);
            sec.when(SecurityUtil::getCurrentMemberType).thenReturn("pro");
            when(memberGaokaoMapper.selectByMemberId(1L)).thenReturn(gaokao);
            when(stringRedisTemplate.hasKey(anyString())).thenReturn(true);  // ZSet 已存在
            when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
            Set<ZSetOperations.TypedTuple<String>> tuples = new HashSet<>();
            tuples.add(ZSetOperations.TypedTuple.of("1", 0.85));
            when(zSetOperations.reverseRangeByScoreWithScores(anyString(), anyDouble(), anyDouble(), anyLong(), anyLong()))
                    .thenReturn(tuples);
            when(zSetOperations.count(anyString(), anyDouble(), anyDouble())).thenReturn(1L);
            when(admissionGroupMapper.selectBatchIds(anyList())).thenReturn(Collections.emptyList());

            IPage<?> result = service.pageGroups(dto);

            // 验证走的是原 ZSet 范围分页(带 start/end,5 参版本)
            verify(zSetOperations).reverseRangeByScoreWithScores(anyString(), anyDouble(), anyDouble(), anyLong(), anyLong());
            // 验证没有走全量拉(3 参版本)
            verify(zSetOperations, never()).reverseRangeByScoreWithScores(anyString(), anyDouble(), anyDouble());
            assertNotNull(result);
        }
    }

    // ========== 新筛选路径:有 universityIds → 走 ZSet 全量 ==========

    @Test
    void pageGroups_paidWithUniversityIds_usesZSetFullRange() {
        dto.setUniversityIds(List.of(100L, 200L));

        try (MockedStatic<SecurityUtil> sec = mockStatic(SecurityUtil.class)) {
            sec.when(SecurityUtil::getCurrentMemberId).thenReturn(1L);
            sec.when(SecurityUtil::getCurrentMemberType).thenReturn("vip");
            when(memberGaokaoMapper.selectByMemberId(1L)).thenReturn(gaokao);
            when(stringRedisTemplate.hasKey(anyString())).thenReturn(true);
            when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
            Set<ZSetOperations.TypedTuple<String>> tuples = new HashSet<>();
            tuples.add(ZSetOperations.TypedTuple.of("1", 0.85));
            when(zSetOperations.reverseRangeByScoreWithScores(anyString(), anyDouble(), anyDouble()))
                    .thenReturn(tuples);
            when(admissionGroupMapper.selectBatchIds(anyList())).thenReturn(Collections.emptyList());

            service.pageGroups(dto);

            // 验证走的是 ZSet 全量(3 参版本,无 start/end)
            verify(zSetOperations).reverseRangeByScoreWithScores(anyString(), anyDouble(), anyDouble());
            // 验证没有走范围分页(5 参版本)
            verify(zSetOperations, never()).reverseRangeByScoreWithScores(anyString(), anyDouble(), anyDouble(), anyLong(), anyLong());
        }
    }

    // ========== 安全范围校验 ==========

    @Test
    void pageGroups_minGreaterThanMax_throwsBusinessException() {
        dto.setMinSafetyLevel(new java.math.BigDecimal("0.80"));
        dto.setMaxSafetyLevel(new java.math.BigDecimal("0.50"));

        try (MockedStatic<SecurityUtil> sec = mockStatic(SecurityUtil.class)) {
            sec.when(SecurityUtil::getCurrentMemberId).thenReturn(1L);
            when(memberGaokaoMapper.selectByMemberId(1L)).thenReturn(gaokao);

            assertThrows(com.haifeng.common.exception.BusinessException.class,
                    () -> service.pageGroups(dto));
        }
    }
}
```

- [ ] **Step 2: 运行测试**

```bash
mvn -pl haifeng-app -Dtest=AdmissionQueryServiceImplPageGroupsTest test
```

Expected: `BUILD SUCCESS`,3 tests passed。

- [ ] **Step 3: Commit**

```bash
git add haifeng-app/src/test/java/com/haifeng/app/service/impl/algorithm/admission/AdmissionQueryServiceImplPageGroupsTest.java
git commit -m "test(admission): group/page 双路径单测(回归保护:无新筛选走原 ZSet 路径)"
```

---

## 第 6 段:文档

### Task 21: 写 haifeng-app/Need/AL6-筛选对接文档.md

**Files:**
- Create: `haifeng-app/Need/AL6-筛选对接文档.md`

- [ ] **Step 1: 写文档**

```markdown
# AL6 专业组筛选对接文档(前端用)

> **Base**: `https://<host>/api/v1/app/admission`
> **鉴权**: 需要登录(`@RequireLogin`)
> **响应格式**: 统一 R&lt;T&gt;:`{ code, msg, data, timestamp }`

---

## 1. 工作流(三步)

```
┌─────────────────────────────────────────────────────────────┐
│ Step 1: GET /filter/universities                              │
│         4 维度筛选 → 拿回 universityId 列表                    │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│ Step 2: GET /filter/majors                                   │
│         1 维度筛选 → 拿回 majorCode 列表                       │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│ Step 3: GET /group/page                                      │
│         原有参数 + universityIds + majorCodes + universityName │
└─────────────────────────────────────────────────────────────┘
```

> 也可以跳过 Step 1/Step 2,直接调 `group/page`(原行为不变)。

---

## 2. 接口详情

### 2.1 GET /filter/universities

**Query 参数**(全部可选):

| 参数 | 类型 | 示例 | 说明 |
|------|------|------|------|
| tags | `string[]` | `["985", "211"]` | 大学标签(OR);前端用 `/filter/tags` 取字典 |
| nature | `string[]` | `["公办", "民办"]` | 院校性质(OR) |
| famousUnion | `string[]` | `["C9", "E9"]` | 知名联盟(OR) |
| category | `string[]` | `["综合", "理工"]` | 院校类别(OR) |
| educationLevel | `string[]` | `["本科", "专科"]` | 办学层次(OR) |
| department | `string[]` | `["教育部", "工信部"]` | 隶属部门(OR) |
| provinces | `string[]` | `["北京", "上海"]` | 省份(OR) |
| regions | `string[]` | `["华东", "华北"]` | 地区(OR) |
| populationBucket | `string` | `BTW_2000_3000` | 城市常住人口桶(单选) |
| evaluationGrades | `string[]` | `["A+", "A"]` | 学科评估等级(OR) |

**populationBucket 可选值**:
- `LT_2000` — 2000 万以下
- `BTW_2000_3000` — 2000~3000 万
- `BTW_3000_4000` — 3000~4000 万
- `GTE_4000` — 4000 万及以上

**Response**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1001,
      "name": "清华大学",
      "cityName": "北京",
      "tags": ["985", "211", "C9"]
    }
  ]
}
```

**限制**: 后端 `LIMIT 500`,超过截断。

---

### 2.2 GET /filter/majors

**Query 参数**(全部可选):

| 参数 | 类型 | 说明 |
|------|------|------|
| majorCategories | `string[]` | 专业大类(OR) |
| parentCategories | `string[]` | 专业父类(OR) |
| majorTags | `string[]` | 专业标签(OR) |
| majorTypes | `string[]` | 专业类型(OR) |

**Response**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 100,
      "majorCode": "080901",
      "majorName": "计算机科学与技术",
      "majorCategory": "工学",
      "majorTags": "热门,新兴"
    }
  ]
}
```

**⚠️ 关键约定**:
- `id` 是 t_major 雪花主键,**仅用于展示/去重**
- `majorCode` 是招生专业代码,**调 group/page 时用这个回传**
- 不要把 `id` 当成过滤条件传回 `group/page`!

**限制**: 后端 `LIMIT 1000`,超过截断。

---

### 2.3 GET /filter/tags(辅助)

**用途**: 大学 tags 字典(供前端下拉数据源)

**Response**:
```json
{
  "code": 200,
  "data": ["985", "211", "C9", "双一流", "部委直属"]
}
```

**缓存**: Redis 1h。

---

### 2.4 GET /group/page(扩展)

**Query 参数**:
- 原有: `batch`(必填) / `subjectFilter` / `minSafetyLevel` / `maxSafetyLevel` / `orderBy` / `page` / `size`
- 新增: `universityIds` / `majorCodes` / `universityName`

**新增参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| universityIds | `number[]` | 大学 ID 列表(来自 /filter/universities);最多 100 个 |
| majorCodes | `string[]` | 专业代码列表(来自 /filter/majors);最多 200 个 |
| universityName | `string` | 大学名模糊搜索;最多 50 字符 |

**行为变化**:
- (universityIds, majorCodes, universityName) **全空** → 走原 Redis ZSet 范围分页(**零回归**)
- **任一非空** → 走 ZSet 全量拉 + 内存过滤 + 内存分页路径

**示例**:
```http
GET /api/v1/app/admission/group/page
    ?batch=本科批
    &minSafetyLevel=0.50
    &maxSafetyLevel=0.85
    &universityIds=1001,1002
    &majorCodes=080901,080902
    &universityName=清华
    &page=1&size=20
```

---

## 3. 错误码

| 场景 | code | msg |
|------|------|-----|
| 缺 batch | 400 | 批次不能为空 |
| universityIds 长度 > 100 | 400 | universityIds 最多 100 个 |
| majorCodes 长度 > 200 | 400 | majorCodes 最多 200 个 |
| universityName 长度 > 50 | 400 | universityName 最多 50 字符 |
| minSafetyLevel > maxSafetyLevel | 400 | minSafetyLevel 不能大于 maxSafetyLevel |
| 未登录 | 401 | 未登录或Token过期 |
| 没填高考档案 | 1010 | 用户高考档案不存在,请先填写档案 |
| 会员过期 | 1003 | 会员已过期 |
| 筛选无结果 | 200 | (空数组) |

---

## 4. 前端集成示例(Vue 3 + Axios)

```typescript
// 1. 加载大学 tags 字典
const tags = await axios.get('/api/v1/app/admission/filter/tags')

// 2. 选完 4 维度后,获取大学选项
const univOptions = await axios.get('/api/v1/app/admission/filter/universities', {
  params: {
    tags: ['985'],
    provinces: ['北京'],
    populationBucket: 'GTE_4000',
    evaluationGrades: ['A+', 'A']
  }
})

// 3. 用户选完大学后,获取专业选项
const majorOptions = await axios.get('/api/v1/app/admission/filter/majors', {
  params: {
    majorCategories: ['工学'],
    parentCategories: ['计算机类']
  }
})

// 4. 用户选完专业后,调 group/page
const universityIds = univOptions.data.data.map(u => u.id)
const majorCodes = majorOptions.data.data.map(m => m.majorCode)
const groups = await axios.get('/api/v1/app/admission/group/page', {
  params: {
    batch: '本科批',
    universityIds,        // 来自 step 2
    majorCodes,           // 来自 step 3
    page: 1, size: 20
  },
  paramsSerializer: { indexes: null }   // axios 数组参数 key 不加 []
})
```

---

## 5. 性能注意

- `/filter/universities` 单次返回最多 500,超过会截断 → **如果筛选条件太宽,提示用户"请增加筛选条件"**
- `/filter/majors` 单次返回最多 1000
- `group/page` 在新筛选启用时是**全量计算**(ZSet 拉所有 + 内存过滤),memberId 数据量在万级时 ≤ 200ms
- 慢查询(>500ms)会被后端 `log.warn`,可通过监控发现
```

- [ ] **Step 2: Commit**

```bash
git add haifeng-app/Need/AL6-筛选对接文档.md
git commit -m "docs(admission): AL6 筛选对接文档(前端集成用)"
```

---

## 完成检查清单

跑完所有 Task 后,验证:

- [ ] `mvn -pl haifeng-common -Dtest=PopulationBucketEnumTest test` → 9 tests passed
- [ ] `mvn -pl haifeng-app -Dtest=AdmissionQueryServiceImplFilterTest test` → 11 tests passed
- [ ] `mvn -pl haifeng-app -Dtest=AdmissionQueryServiceImplPageGroupsTest test` → 3 tests passed
- [ ] `mvn -pl haifeng-common compile` → BUILD SUCCESS
- [ ] `mvn -pl haifeng-app compile` → BUILD SUCCESS
- [ ] `mvn clean install -DskipTests` → BUILD SUCCESS(整体编译)

**回归保护**:
- [ ] `pageGroupsForPaid` 无新筛选时仍走 `rangeByScoreWithScores(..., start, size)`(原行为)
- [ ] `pageGroupsForFree` 仍保留 10 条限制 + masked 占位
- [ ] `pageGroupsForPaidFallback` 仍走 DB 全量 + 内存分页(原行为)

**前端交付**:
- [ ] `haifeng-app/Need/AL6-筛选对接文档.md` 已生成
