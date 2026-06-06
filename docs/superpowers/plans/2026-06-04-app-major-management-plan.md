# C 端专业管理模块 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现 C 端专业管理模块的 8 个只读接口（专业列表/详情/分类统计/排行 + 考研专业列表/详情 + 大学↔考研专业关联查询）。

**Architecture:** 遵循现有 app 层分层模式：Controller → Service → Mapper，DTO 继承 BasePageQueryDTO，VO 用 Builder 模式。mapper 层仅在需要自定义 SQL（GROUP BY / JOIN）时新增方法，其余用 MyBatis-Plus LambdaQueryWrapper。

**Tech Stack:** Spring Boot 3 / MyBatis-Plus / PostgreSQL / Lombok

---

## File Structure

### haifeng-common（修改 2 个 mapper）

| 文件 | 操作 | 说明 |
|---|---|---|
| `mapper/major/MajorMapper.java` | 修改 | + `countByCategory()` |
| `mapper/major/PostgradMajorUniversityMapper.java` | 修改 | + `selectPostgradMajorsByUniversity()` + `selectUniversitiesByPostgradMajor()` |

### haifeng-app（新增 17 个文件）

| 文件 | 说明 |
|---|---|
| `controller/major/MajorController.java` | 任务1的4个接口 |
| `controller/major/PostgradMajorController.java` | 任务2的2个接口 + 任务4接口1 |
| `controller/university/UniversityPostgradMajorController.java` | 任务3接口1 |
| `service/major/MajorService.java` | 接口 |
| `service/major/PostgradMajorService.java` | 接口 |
| `service/university/UniversityPostgradMajorService.java` | 接口 |
| `service/impl/major/MajorServiceImpl.java` | 实现 |
| `service/impl/major/PostgradMajorServiceImpl.java` | 实现 |
| `service/impl/university/UniversityPostgradMajorServiceImpl.java` | 实现 |
| `dto/major/MajorListQueryDTO.java` | 任务1接口1 |
| `dto/major/MajorRankingQueryDTO.java` | 任务1接口4 |
| `dto/major/PostgradMajorListQueryDTO.java` | 任务2接口1 |
| `dto/major/PostgradMajorUniversityQueryDTO.java` | 任务4接口1 |
| `dto/university/UniversityPostgradMajorQueryDTO.java` | 任务3接口1 |
| `vo/major/MajorListVO.java` | 任务1接口1+4 |
| `vo/major/MajorDetailVO.java` | 任务1接口2 |
| `vo/major/MajorCategoryStatVO.java` | 任务1接口3 |
| `vo/major/PostgradMajorListVO.java` | 任务2接口1 |
| `vo/major/PostgradMajorDetailVO.java` | 任务2接口2 |
| `vo/major/PostgradMajorBriefVO.java` | 任务3接口1 |
| `vo/major/UniversityBriefForPostgradVO.java` | 任务4接口1 |

### docs（新增 1 个文件）

| 文件 | 说明 |
|---|---|
| `haifeng-app/Products/order7.md` | 对外 API 文档 |

---

## Task 1: Mapper 层 — 新增 3 个自定义 SQL 方法

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/mapper/major/MajorMapper.java`
- Modify: `haifeng-common/src/main/java/com/haifeng/common/mapper/major/PostgradMajorUniversityMapper.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/major/MajorCategoryStatVO.java` (mapper 返回 VO 用于联表映射)
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/major/PostgradMajorBriefVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/major/UniversityBriefForPostgradVO.java`

> 联表 mapper 方法直接返回 VO 对象（列别名与 VO 字段对应），所以 VO 需要先创建。

- [ ] **Step 1: 创建 MajorCategoryStatVO**

```java
package com.haifeng.app.vo.major;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端专业类别统计 VO（spec 任务1接口3） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MajorCategoryStatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String majorCategory;
    private Integer count;
}
```

- [ ] **Step 2: 创建 PostgradMajorBriefVO**

```java
package com.haifeng.app.vo.major;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端考研专业精简 VO（spec 任务3接口1，大学→考研专业列表） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostgradMajorBriefVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String majorName;
    private String degreeType;
}
```

- [ ] **Step 3: 创建 UniversityBriefForPostgradVO**

```java
package com.haifeng.app.vo.major;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端院校精简 VO（spec 任务4接口1，考研专业→大学列表） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityBriefForPostgradVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String category;
}
```

- [ ] **Step 4: 修改 MajorMapper — 添加 countByCategory**

在 `MajorMapper.java` 的接口体末尾追加：

```java
/**
 * 按 major_category 分组统计专业数量（仅 status=1）
 * 返回示例：[{majorCategory="计算机类", count=38}, ...]
 * Service 层负责转换为 List<MajorCategoryStatVO>
 */
@Select("SELECT major_category AS majorCategory, COUNT(*) AS count " +
        "FROM t_major " +
        "WHERE status = 1 AND major_category IS NOT NULL " +
        "GROUP BY major_category " +
        "ORDER BY COUNT(*) DESC")
List<Map<String, Object>> countByCategory();
```

同时在文件顶部 import 中追加 `import java.util.List;` 和 `import java.util.Map;`。

- [ ] **Step 5: 修改 PostgradMajorUniversityMapper — 添加联表方法**

在 `PostgradMajorUniversityMapper.java` 的接口体末尾追加：

```java
/**
 * 任务3接口1：大学 → 考研专业（联表，支持 degreeType 精准筛选）
 * MyBatis-Plus IPage 自动注入 LIMIT/OFFSET
 */
@Select("<script>" +
    "SELECT pm.id AS id, pm.major_name AS majorName, pm.degree_type AS degreeType " +
    "FROM t_postgrad_major_university pmu " +
    "JOIN t_postgrad_major pm ON pm.id = pmu.postgrad_major_id " +
    "WHERE pmu.university_id = #{universityId} " +
    "  AND pmu.status = 1 AND pm.status = 1 " +
    "  <if test='degreeType != null and degreeType != \"\"'> " +
    "    AND pm.degree_type = #{degreeType} " +
    "  </if> " +
    "ORDER BY pmu.sort_order ASC, pm.id DESC" +
    "</script>")
IPage<PostgradMajorBriefVO> selectPostgradMajorsByUniversity(
        Page<?> page,
        @Param("universityId") Long universityId,
        @Param("degreeType")   String degreeType);

/**
 * 任务4接口1：考研专业 → 大学（联表，支持 category 精准筛选）
 */
@Select("<script>" +
    "SELECT u.id AS id, u.name AS name, u.category AS category " +
    "FROM t_postgrad_major_university pmu " +
    "JOIN t_universities u ON u.id = pmu.university_id " +
    "WHERE pmu.postgrad_major_id = #{postgradMajorId} " +
    "  AND pmu.status = 1 AND u.status = 1 " +
    "  <if test='category != null and category != \"\"'> " +
    "    AND u.category = #{category} " +
    "  </if> " +
    "ORDER BY pmu.sort_order ASC, u.id DESC" +
    "</script>")
IPage<UniversityBriefForPostgradVO> selectUniversitiesByPostgradMajor(
        Page<?> page,
        @Param("postgradMajorId") Long postgradMajorId,
        @Param("category")        String category);
```

同时在文件顶部 import 中追加：

```java
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.vo.major.PostgradMajorBriefVO;
import com.haifeng.app.vo.major.UniversityBriefForPostgradVO;
```

> ⚠️ 跨模块引用：`haifeng-common` mapper 引用 `haifeng-app` VO。这在当前项目里是可行的——app 模块最终打包包含 common，MyBatis 运行时能扫描到。如果编译报循环依赖，则改为返回 `List<Map<String,Object>>` + Service 层转 VO（参考 SubjectEvaluationMapper.countByGrade 模式）。

- [ ] **Step 6: 验证编译**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn compile -pl haifeng-common -q 2>&1 | tail -5`

Expected: BUILD SUCCESS

> 如果 `haifeng-common` 编译报找不到 `haifeng-app` 的 VO 类，则将联表 mapper 返回类型改为 `List<Map<String, Object>>`，在 Service 层手动映射到 VO。

---

## Task 2: VO 层 — 创建剩余 4 个 VO

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/major/MajorListVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/major/MajorDetailVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/major/PostgradMajorListVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/major/PostgradMajorDetailVO.java`

- [ ] **Step 1: 创建 MajorListVO**

```java
package com.haifeng.app.vo.major;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/** C 端专业列表 VO（spec 任务1接口1 + 任务1接口4 复用） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MajorListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String majorCode;
    private String majorName;
    private String disciplineName;
    private String majorCategory;
    private String parentCategory;
    private String majorTags;
    private String degreeAwarded;
    private BigDecimal employmentRate;
    private Integer salaryMin;
    private Integer salaryMax;
    private String description;
}
```

- [ ] **Step 2: 创建 MajorDetailVO**

```java
package com.haifeng.app.vo.major;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/** C 端专业详情 VO（spec 任务1接口2，t_major + t_major_detail 合并返回） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MajorDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ---- 来自 t_major ----
    private String majorName;
    private String majorCode;
    private String disciplineName;
    private String majorCategory;
    private String parentCategory;
    private String majorTags;
    private String degreeAwarded;
    private BigDecimal employmentRate;
    private Integer salaryMin;
    private Integer salaryMax;
    private String description;

    // ---- 来自 t_major_detail ----
    private Integer courseCount;
    private String graduateScale;
    private BigDecimal maleRatio;
    private BigDecimal femaleRatio;
    private String majorDescription;
    private String trainingObjective;
    private String trainingRequirement;
    private String subjectRequirement;
    private String careerProspect;
    private String[] mainCourses;
    private String[] knowledgeSkills;
}
```

- [ ] **Step 3: 创建 PostgradMajorListVO**

```java
package com.haifeng.app.vo.major;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端考研专业列表 VO（spec 任务2接口1） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostgradMajorListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String majorName;
    private String majorCode;
    private String degreeType;
    private String disciplineCategory;
    private String popularity;
    private String difficulty;
    private String brief;
    private String[] examSubjects;
}
```

- [ ] **Step 4: 创建 PostgradMajorDetailVO**

```java
package com.haifeng.app.vo.major;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端考研专业详情 VO（spec 任务2接口2） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostgradMajorDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String majorName;
    private String majorCode;
    private String degreeType;
    private String disciplineCategory;
    private String popularity;
    private String difficulty;
    private String introduction;
    private String[] examSubjects;
    private String[] admissionRequirements;
    private String crossExamDifficulty;
    private String crossExamDescription;
    private String[] crossExamFactors;
}
```

---

## Task 3: DTO 层 — 创建 5 个查询 DTO

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/major/MajorListQueryDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/major/MajorRankingQueryDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/major/PostgradMajorListQueryDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/major/PostgradMajorUniversityQueryDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/university/UniversityPostgradMajorQueryDTO.java`

- [ ] **Step 1: 创建 MajorListQueryDTO**

```java
package com.haifeng.app.dto.major;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** C 端专业列表查询 DTO（spec 任务1接口1） */
@Data
@EqualsAndHashCode(callSuper = true)
public class MajorListQueryDTO extends BasePageQueryDTO {

    /** 模糊查询（LIKE %name%） */
    private String name;

    /** 模糊查询（LIKE %code%） */
    private String code;

    /** 精准查询 */
    private String majorType;

    /** 精准查询 */
    private String majorCategory;
}
```

- [ ] **Step 2: 创建 MajorRankingQueryDTO**

```java
package com.haifeng.app.dto.major;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** C 端专业薪资/就业排行查询 DTO（spec 任务1接口4） */
@Data
@EqualsAndHashCode(callSuper = true)
public class MajorRankingQueryDTO extends BasePageQueryDTO {

    /** 模糊查询（LIKE %name%） */
    private String name;

    /** 精准查询 */
    private String majorCategory;

    /** 排序字段，默认 employmentRate */
    @Pattern(regexp = "employmentRate|salaryMin|salaryMax", message = "sortBy 只支持 employmentRate、salaryMin、salaryMax")
    private String sortBy = "employmentRate";

    /** 排序方向，默认 desc */
    @Pattern(regexp = "asc|desc", message = "sortOrder 只支持 asc、desc")
    private String sortOrder = "desc";
}
```

- [ ] **Step 3: 创建 PostgradMajorListQueryDTO**

```java
package com.haifeng.app.dto.major;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** C 端考研专业列表查询 DTO（spec 任务2接口1） */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostgradMajorListQueryDTO extends BasePageQueryDTO {

    /** 模糊查询（LIKE %name%） */
    private String name;

    /** 模糊查询（LIKE %code%） */
    private String code;

    /** 精准查询 */
    private String degreeType;

    /** 精准查询 */
    private String disciplineCategory;

    /** 精准查询 */
    private String popularity;

    /** 精准查询 */
    private String difficulty;
}
```

- [ ] **Step 4: 创建 UniversityPostgradMajorQueryDTO**

```java
package com.haifeng.app.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** C 端大学→考研专业列表查询 DTO（spec 任务3接口1） */
@Data
@EqualsAndHashCode(callSuper = true)
public class UniversityPostgradMajorQueryDTO extends BasePageQueryDTO {

    /** 精准查询（学术学位 / 专业学位） */
    private String degreeType;
}
```

- [ ] **Step 5: 创建 PostgradMajorUniversityQueryDTO**

```java
package com.haifeng.app.dto.major;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** C 端考研专业→大学列表查询 DTO（spec 任务4接口1） */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostgradMajorUniversityQueryDTO extends BasePageQueryDTO {

    /** 精准查询（综合/理工/师范/...） */
    private String category;
}
```

---

## Task 4: MajorService — 任务1的4个接口实现

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/major/MajorService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/major/MajorServiceImpl.java`

- [ ] **Step 1: 创建 MajorService 接口**

```java
package com.haifeng.app.service.major;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.major.MajorListQueryDTO;
import com.haifeng.app.dto.major.MajorRankingQueryDTO;
import com.haifeng.app.vo.major.MajorCategoryStatVO;
import com.haifeng.app.vo.major.MajorDetailVO;
import com.haifeng.app.vo.major.MajorListVO;

import java.util.List;

public interface MajorService {

    /** 任务1接口1：专业列表（公开） */
    IPage<MajorListVO> page(MajorListQueryDTO dto);

    /** 任务1接口2：专业详情（登录） */
    MajorDetailVO detail(Long majorId);

    /** 任务1接口3：按 major_category 分组统计（公开） */
    List<MajorCategoryStatVO> categoryStats();

    /** 任务1接口4：薪资/就业排行（Pro） */
    IPage<MajorListVO> ranking(MajorRankingQueryDTO dto);
}
```

- [ ] **Step 2: 创建 MajorServiceImpl**

```java
package com.haifeng.app.service.impl.major;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.major.MajorListQueryDTO;
import com.haifeng.app.dto.major.MajorRankingQueryDTO;
import com.haifeng.app.service.major.MajorService;
import com.haifeng.app.vo.major.MajorCategoryStatVO;
import com.haifeng.app.vo.major.MajorDetailVO;
import com.haifeng.app.vo.major.MajorListVO;
import com.haifeng.common.entity.major.Major;
import com.haifeng.common.entity.major.MajorDetail;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.major.MajorDetailMapper;
import com.haifeng.common.mapper.major.MajorMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MajorServiceImpl implements MajorService {

    private static final short STATUS_PUBLISHED = 1;

    /** sortBy 参数 → 数据库列名映射 */
    private static final Map<String, String> SORT_COLUMN_MAP = Map.of(
            "employmentRate", "employment_rate",
            "salaryMin", "salary_min",
            "salaryMax", "salary_max"
    );

    private final MajorMapper majorMapper;
    private final MajorDetailMapper majorDetailMapper;

    @Override
    public IPage<MajorListVO> page(MajorListQueryDTO dto) {
        Page<Major> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<Major>()
                .eq(Major::getStatus, STATUS_PUBLISHED)
                .like(dto.getName() != null && !dto.getName().isBlank(),
                        Major::getMajorName, dto.getName())
                .like(dto.getCode() != null && !dto.getCode().isBlank(),
                        Major::getMajorCode, dto.getCode())
                .eq(dto.getMajorType() != null && !dto.getMajorType().isBlank(),
                        Major::getMajorType, dto.getMajorType())
                .eq(dto.getMajorCategory() != null && !dto.getMajorCategory().isBlank(),
                        Major::getMajorCategory, dto.getMajorCategory())
                .orderByDesc(Major::getId);

        IPage<Major> entityPage = majorMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    @Override
    public MajorDetailVO detail(Long majorId) {
        // 1. 查主表
        Major major = majorMapper.selectOne(
                new LambdaQueryWrapper<Major>()
                        .eq(Major::getId, majorId)
                        .eq(Major::getStatus, STATUS_PUBLISHED));
        if (major == null) {
            log.debug("专业不存在或已下架, majorId={}", majorId);
            throw new BusinessException(ResultCode.NOT_FOUND, "专业不存在");
        }

        // 2. 查详情表
        MajorDetail detail = majorDetailMapper.selectByMajorId(majorId);
        if (detail == null) {
            log.debug("专业详情不存在, majorId={}", majorId);
            throw new BusinessException(ResultCode.NOT_FOUND, "专业详情不存在");
        }

        // 3. 合并返回
        return MajorDetailVO.builder()
                // t_major
                .majorName(major.getMajorName())
                .majorCode(major.getMajorCode())
                .disciplineName(major.getDisciplineName())
                .majorCategory(major.getMajorCategory())
                .parentCategory(major.getParentCategory())
                .majorTags(major.getMajorTags())
                .degreeAwarded(major.getDegreeAwarded())
                .employmentRate(major.getEmploymentRate())
                .salaryMin(major.getSalaryMin())
                .salaryMax(major.getSalaryMax())
                .description(major.getDescription())
                // t_major_detail
                .courseCount(detail.getCourseCount())
                .graduateScale(detail.getGraduateScale())
                .maleRatio(detail.getMaleRatio())
                .femaleRatio(detail.getFemaleRatio())
                .majorDescription(detail.getMajorDescription())
                .trainingObjective(detail.getTrainingObjective())
                .trainingRequirement(detail.getTrainingRequirement())
                .subjectRequirement(detail.getSubjectRequirement())
                .careerProspect(detail.getCareerProspect())
                .mainCourses(detail.getMainCourses())
                .knowledgeSkills(detail.getKnowledgeSkills())
                .build();
    }

    @Override
    public List<MajorCategoryStatVO> categoryStats() {
        List<Map<String, Object>> rows = majorMapper.countByCategory();
        return rows.stream()
                .map(row -> MajorCategoryStatVO.builder()
                        .majorCategory(String.valueOf(row.get("majorCategory")))
                        .count(((Number) row.get("count")).intValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public IPage<MajorListVO> ranking(MajorRankingQueryDTO dto) {
        Page<Major> page = new Page<>(dto.getPage(), dto.getSize());

        // 构建 WHERE 条件（用 LambdaQueryWrapper 拼条件部分）
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<Major>()
                .eq(Major::getStatus, STATUS_PUBLISHED)
                .like(dto.getName() != null && !dto.getName().isBlank(),
                        Major::getMajorName, dto.getName())
                .eq(dto.getMajorCategory() != null && !dto.getMajorCategory().isBlank(),
                        Major::getMajorCategory, dto.getMajorCategory());

        // 排序：sortBy 映射到数据库列名 + sortOrder + NULLS LAST
        String column = SORT_COLUMN_MAP.get(dto.getSortBy());
        String direction = "asc".equalsIgnoreCase(dto.getSortOrder()) ? "ASC" : "DESC";
        String nulls = "ASC".equals(direction) ? "NULLS LAST" : "NULLS LAST";
        wrapper.last("ORDER BY " + column + " " + direction + " " + nulls + ", id DESC");

        IPage<Major> entityPage = majorMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    private MajorListVO toListVO(Major e) {
        return MajorListVO.builder()
                .id(e.getId())
                .majorCode(e.getMajorCode())
                .majorName(e.getMajorName())
                .disciplineName(e.getDisciplineName())
                .majorCategory(e.getMajorCategory())
                .parentCategory(e.getParentCategory())
                .majorTags(e.getMajorTags())
                .degreeAwarded(e.getDegreeAwarded())
                .employmentRate(e.getEmploymentRate())
                .salaryMin(e.getSalaryMin())
                .salaryMax(e.getSalaryMax())
                .description(e.getDescription())
                .build();
    }
}
```

---

## Task 5: MajorController — 任务1的4个接口

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/major/MajorController.java`

- [ ] **Step 1: 创建 MajorController**

```java
package com.haifeng.app.controller.major;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.major.MajorListQueryDTO;
import com.haifeng.app.dto.major.MajorRankingQueryDTO;
import com.haifeng.app.service.major.MajorService;
import com.haifeng.app.vo.major.MajorCategoryStatVO;
import com.haifeng.app.vo.major.MajorDetailVO;
import com.haifeng.app.vo.major.MajorListVO;
import com.haifeng.common.annotation.RequirePro;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端专业管理（spec 任务1）
 * 接口1/3 公开，接口2 需登录，接口4 需 Pro
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/major")
@RequiredArgsConstructor
public class MajorController {

    private final MajorService majorService;

    /** 任务1接口1：专业列表（公开） */
    @GetMapping("/list")
    public R<IPage<MajorListVO>> list(@Valid MajorListQueryDTO dto) {
        return R.ok(majorService.page(dto));
    }

    /** 任务1接口2：专业详情（登录） */
    @GetMapping("/{majorId}/detail")
    public R<MajorDetailVO> detail(@PathVariable Long majorId) {
        return R.ok(majorService.detail(majorId));
    }

    /** 任务1接口3：按 major_category 分组统计（公开） */
    @GetMapping("/category-stats")
    public R<List<MajorCategoryStatVO>> categoryStats() {
        return R.ok(majorService.categoryStats());
    }

    /** 任务1接口4：薪资/就业排行（Pro 及以上） */
    @RequirePro
    @GetMapping("/ranking")
    public R<IPage<MajorListVO>> ranking(@Valid MajorRankingQueryDTO dto) {
        return R.ok(majorService.ranking(dto));
    }
}
```

> 注意：任务1接口2 路径 `/{majorId}/detail` 在类级别 `@RequestMapping("/api/v1/app/major")` 下不需要额外登录注解——需求说"接口2需要登录"，但 `/{majorId}/detail` 路径不在公开路径内，需要 `@RequireLogin`。**修正：追加 `@RequireLogin`。**

- [ ] **Step 2: 修正 — 在 detail 方法上添加 @RequireLogin**

将 Step 1 中的 `detail` 方法修改为：

```java
    /** 任务1接口2：专业详情（登录） */
    @RequireLogin
    @GetMapping("/{majorId}/detail")
    public R<MajorDetailVO> detail(@PathVariable Long majorId) {
        return R.ok(majorService.detail(majorId));
    }
```

同时在 import 中追加：`import com.haifeng.common.annotation.RequireLogin;`

---

## Task 6: PostgradMajorService — 任务2的2个接口实现

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/major/PostgradMajorService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/major/PostgradMajorServiceImpl.java`

- [ ] **Step 1: 创建 PostgradMajorService 接口**

```java
package com.haifeng.app.service.major;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.major.PostgradMajorListQueryDTO;
import com.haifeng.app.dto.major.PostgradMajorUniversityQueryDTO;
import com.haifeng.app.vo.major.PostgradMajorDetailVO;
import com.haifeng.app.vo.major.PostgradMajorListVO;
import com.haifeng.app.vo.major.UniversityBriefForPostgradVO;

public interface PostgradMajorService {

    /** 任务2接口1：考研专业列表（登录） */
    IPage<PostgradMajorListVO> page(PostgradMajorListQueryDTO dto);

    /** 任务2接口2：考研专业详情（登录） */
    PostgradMajorDetailVO detail(Long majorId);

    /** 任务4接口1：考研专业 → 大学列表（Pro） */
    IPage<UniversityBriefForPostgradVO> universities(Long majorId, PostgradMajorUniversityQueryDTO dto);
}
```

- [ ] **Step 2: 创建 PostgradMajorServiceImpl**

```java
package com.haifeng.app.service.impl.major;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.major.PostgradMajorListQueryDTO;
import com.haifeng.app.dto.major.PostgradMajorUniversityQueryDTO;
import com.haifeng.app.service.major.PostgradMajorService;
import com.haifeng.app.vo.major.PostgradMajorDetailVO;
import com.haifeng.app.vo.major.PostgradMajorListVO;
import com.haifeng.app.vo.major.UniversityBriefForPostgradVO;
import com.haifeng.common.entity.major.PostgradMajor;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.major.PostgradMajorMapper;
import com.haifeng.common.mapper.major.PostgradMajorUniversityMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostgradMajorServiceImpl implements PostgradMajorService {

    private static final short STATUS_PUBLISHED = 1;

    private final PostgradMajorMapper postgradMajorMapper;
    private final PostgradMajorUniversityMapper postgradMajorUniversityMapper;

    @Override
    public IPage<PostgradMajorListVO> page(PostgradMajorListQueryDTO dto) {
        Page<PostgradMajor> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<PostgradMajor> wrapper = new LambdaQueryWrapper<PostgradMajor>()
                .eq(PostgradMajor::getStatus, STATUS_PUBLISHED)
                .like(dto.getName() != null && !dto.getName().isBlank(),
                        PostgradMajor::getMajorName, dto.getName())
                .like(dto.getCode() != null && !dto.getCode().isBlank(),
                        PostgradMajor::getMajorCode, dto.getCode())
                .eq(dto.getDegreeType() != null && !dto.getDegreeType().isBlank(),
                        PostgradMajor::getDegreeType, dto.getDegreeType())
                .eq(dto.getDisciplineCategory() != null && !dto.getDisciplineCategory().isBlank(),
                        PostgradMajor::getDisciplineCategory, dto.getDisciplineCategory())
                .eq(dto.getPopularity() != null && !dto.getPopularity().isBlank(),
                        PostgradMajor::getPopularity, dto.getPopularity())
                .eq(dto.getDifficulty() != null && !dto.getDifficulty().isBlank(),
                        PostgradMajor::getDifficulty, dto.getDifficulty())
                .orderByDesc(PostgradMajor::getId);

        IPage<PostgradMajor> entityPage = postgradMajorMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    @Override
    public PostgradMajorDetailVO detail(Long majorId) {
        PostgradMajor e = postgradMajorMapper.selectOne(
                new LambdaQueryWrapper<PostgradMajor>()
                        .eq(PostgradMajor::getId, majorId)
                        .eq(PostgradMajor::getStatus, STATUS_PUBLISHED));
        if (e == null) {
            log.debug("考研专业不存在或已下架, majorId={}", majorId);
            throw new BusinessException(ResultCode.NOT_FOUND, "考研专业不存在");
        }

        return PostgradMajorDetailVO.builder()
                .majorName(e.getMajorName())
                .majorCode(e.getMajorCode())
                .degreeType(e.getDegreeType())
                .disciplineCategory(e.getDisciplineCategory())
                .popularity(e.getPopularity())
                .difficulty(e.getDifficulty())
                .introduction(e.getIntroduction())
                .examSubjects(e.getExamSubjects())
                .admissionRequirements(e.getAdmissionRequirements())
                .crossExamDifficulty(e.getCrossExamDifficulty())
                .crossExamDescription(e.getCrossExamDescription())
                .crossExamFactors(e.getCrossExamFactors())
                .build();
    }

    @Override
    public IPage<UniversityBriefForPostgradVO> universities(Long majorId, PostgradMajorUniversityQueryDTO dto) {
        Page<UniversityBriefForPostgradVO> page = new Page<>(dto.getPage(), dto.getSize());
        return postgradMajorUniversityMapper.selectUniversitiesByPostgradMajor(
                page, majorId, dto.getCategory());
    }

    private PostgradMajorListVO toListVO(PostgradMajor e) {
        return PostgradMajorListVO.builder()
                .id(e.getId())
                .majorName(e.getMajorName())
                .majorCode(e.getMajorCode())
                .degreeType(e.getDegreeType())
                .disciplineCategory(e.getDisciplineCategory())
                .popularity(e.getPopularity())
                .difficulty(e.getDifficulty())
                .brief(e.getBrief())
                .examSubjects(e.getExamSubjects())
                .build();
    }
}
```

---

## Task 7: UniversityPostgradMajorService — 任务3接口1实现

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/university/UniversityPostgradMajorService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/university/UniversityPostgradMajorServiceImpl.java`

- [ ] **Step 1: 创建 UniversityPostgradMajorService 接口**

```java
package com.haifeng.app.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.UniversityPostgradMajorQueryDTO;
import com.haifeng.app.vo.major.PostgradMajorBriefVO;

public interface UniversityPostgradMajorService {

    /** 任务3接口1：大学 → 考研专业列表（Pro） */
    IPage<PostgradMajorBriefVO> page(Long universityId, UniversityPostgradMajorQueryDTO dto);
}
```

- [ ] **Step 2: 创建 UniversityPostgradMajorServiceImpl**

```java
package com.haifeng.app.service.impl.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.university.UniversityPostgradMajorQueryDTO;
import com.haifeng.app.service.university.UniversityPostgradMajorService;
import com.haifeng.app.vo.major.PostgradMajorBriefVO;
import com.haifeng.common.mapper.major.PostgradMajorUniversityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UniversityPostgradMajorServiceImpl implements UniversityPostgradMajorService {

    private final PostgradMajorUniversityMapper postgradMajorUniversityMapper;

    @Override
    public IPage<PostgradMajorBriefVO> page(Long universityId, UniversityPostgradMajorQueryDTO dto) {
        Page<PostgradMajorBriefVO> page = new Page<>(dto.getPage(), dto.getSize());
        return postgradMajorUniversityMapper.selectPostgradMajorsByUniversity(
                page, universityId, dto.getDegreeType());
    }
}
```

---

## Task 8: PostgradMajorController + UniversityPostgradMajorController

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/major/PostgradMajorController.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/university/UniversityPostgradMajorController.java`

- [ ] **Step 1: 创建 PostgradMajorController**

```java
package com.haifeng.app.controller.major;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.major.PostgradMajorListQueryDTO;
import com.haifeng.app.dto.major.PostgradMajorUniversityQueryDTO;
import com.haifeng.app.service.major.PostgradMajorService;
import com.haifeng.app.vo.major.PostgradMajorDetailVO;
import com.haifeng.app.vo.major.PostgradMajorListVO;
import com.haifeng.app.vo.major.UniversityBriefForPostgradVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.annotation.RequirePro;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端考研专业管理（spec 任务2 + 任务4）
 * 任务2接口1/2 需登录，任务4接口1 需 Pro
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/postgrad-major")
@RequiredArgsConstructor
public class PostgradMajorController {

    private final PostgradMajorService postgradMajorService;

    /** 任务2接口1：考研专业列表（登录） */
    @RequireLogin
    @GetMapping("/list")
    public R<IPage<PostgradMajorListVO>> list(@Valid PostgradMajorListQueryDTO dto) {
        return R.ok(postgradMajorService.page(dto));
    }

    /** 任务2接口2：考研专业详情（登录） */
    @RequireLogin
    @GetMapping("/{majorId}/detail")
    public R<PostgradMajorDetailVO> detail(@PathVariable Long majorId) {
        return R.ok(postgradMajorService.detail(majorId));
    }

    /** 任务4接口1：考研专业 → 大学列表（Pro 及以上） */
    @RequirePro
    @GetMapping("/{majorId}/universities")
    public R<IPage<UniversityBriefForPostgradVO>> universities(
            @PathVariable Long majorId,
            @Valid PostgradMajorUniversityQueryDTO dto) {
        return R.ok(postgradMajorService.universities(majorId, dto));
    }
}
```

- [ ] **Step 2: 创建 UniversityPostgradMajorController**

```java
package com.haifeng.app.controller.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.UniversityPostgradMajorQueryDTO;
import com.haifeng.app.service.university.UniversityPostgradMajorService;
import com.haifeng.app.vo.major.PostgradMajorBriefVO;
import com.haifeng.common.annotation.RequirePro;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端大学→考研专业列表（spec 任务3接口1）
 * 需 Pro 及以上
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/university")
@RequiredArgsConstructor
public class UniversityPostgradMajorController {

    private final UniversityPostgradMajorService universityPostgradMajorService;

    /** 任务3接口1：大学 → 考研专业列表（Pro 及以上） */
    @RequirePro
    @GetMapping("/{universityId}/postgrad-majors")
    public R<IPage<PostgradMajorBriefVO>> list(
            @PathVariable Long universityId,
            @Valid UniversityPostgradMajorQueryDTO dto) {
        return R.ok(universityPostgradMajorService.page(universityId, dto));
    }
}
```

---

## Task 9: 编译验证

**Files:** 无新增

- [ ] **Step 1: 全模块编译**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn compile -q 2>&1 | tail -20`

Expected: BUILD SUCCESS

- [ ] **Step 2: 修复编译错误（如有）**

如果 Task 1 的 Step 5 警告的跨模块引用导致编译失败（`haifeng-common` 找不到 `haifeng-app` 的 VO 类），则回退方案：

1. 将 `PostgradMajorUniversityMapper` 的两个联表方法返回类型改为 `List<Map<String, Object>>`
2. 在 Service 层手动映射：
   - `selectPostgradMajorsByUniversity` → 返回 `IPage<Map<String,Object>>`，Service 里 `.convert(row -> PostgradMajorBriefVO.builder().id(...).majorName(...).degreeType(...).build())`
   - `selectUniversitiesByPostgradMajor` → 同理

> 此步骤仅在编译失败时执行。

---

## Task 10: 编写 order7.md API 文档

**Files:**
- Create: `haifeng-app/Products/order7.md`

- [ ] **Step 1: 编写 order7.md**

参照 order5.md / order6.md 风格，包含：
- 功能概述（8 个接口 + 权限矩阵）
- 通用说明（权限、统一响应格式、分页参数、错误码）
- 8 个接口各自的小节（URL、请求参数、响应示例、错误响应）
- 模糊 vs 精准字段总览
- 接口路径速查

（内容从 spec 文档直接翻译为 API 文档格式，此处不再重复）

---

## Self-Review Checklist

- [x] **Spec coverage:** 8 个接口 → Task 4-8 实现全部覆盖
- [x] **Placeholder scan:** 无 TBD / TODO / "implement later" / "add validation"
- [x] **Type consistency:** MajorListVO 在 Task 2 定义、Task 4 Service 使用、Task 5 Controller 返回——字段名一致
- [x] **权限注解:** 接口1/3 无注解（公开）、接口2 `@RequireLogin`、接口4/7/8 `@RequirePro`、接口5/6 `@RequireLogin`——与 spec §1 完全一致
- [x] **排序:** 接口1 `id DESC`、接口4 `sortBy sortOrder NULLS LAST, id DESC`、接口3 `count DESC`、接口5 `id DESC`、接口7/8 `sort_order ASC, id DESC`——与 spec §7 一致
- [x] **错误码:** 接口2 双重 404（专业不存在 / 专业详情不存在）、接口6 单 404（考研专业不存在）、接口7/8 空分页——与 spec §6 一致
