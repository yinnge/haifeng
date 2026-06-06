# C 端院校次模块实施计划（实验室 / 院系 / 学科评估）

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 haifeng-app 院校管理父模块下新增 3 个只读子模块（实验室 / 院系 / 学科评估），共 6 个接口，均需登录。

**Architecture:** Controller → Service → ServiceImpl 三层，复用 haifeng-common 已有的 entity / mapper。每个子模块独立一套 Controller/Service/Impl/VO/DTO，与现有 `CampusGalleryController` / `UniversityGuideController` 风格一致。仅在 `SubjectEvaluationMapper` 新增 1 个 `@Select` 方法用于等级 GROUP BY 统计。

**Tech Stack:** Spring Boot 3 + MyBatis-Plus + Jackson（JSONB 自动序列化）+ Lombok + Java 17

**Spec：** `docs/superpowers/specs/2026-06-04-app-university-details-design.md`

---

## ⚠️ 项目特殊约定（必读）

1. **不执行 `git commit`**：用户要求所有改动最后统一提交。每个 Task 末尾**只列出修改文件清单**，不运行 `git add` / `git commit`。
2. **不写单测**：项目现有模块均无单测基础设施（参见 spec §6）。验证方式为：
   - 每个 Task 后用 `mvn -pl haifeng-app -am compile` 验证编译通过
   - 全部完成后用 Postman/Apifox 按 spec §3 逐一手工验收
3. **JSONB 字段直接透传**：VO 字段类型与 entity 完全对齐（`List<String>` / `Map<String,Object>` / `List<Map<String,Object>>`），不做 Bean 转换。
4. **状态过滤**：所有查询都加 `status = 1`，统一用 `private static final short STATUS_PUBLISHED = 1;` 常量。
5. **错误码**：使用 `BusinessException(ResultCode.NOT_FOUND, "xxx不存在")`，与 `UniversityServiceImpl` 一致。

---

## 文件结构总览

```
haifeng-common/src/main/java/com/haifeng/common/
└── mapper/university/
    └── SubjectEvaluationMapper.java          [MODIFY] 新增 countByGrade 方法

haifeng-app/src/main/java/com/haifeng/app/
├── controller/university/
│   ├── LaboratoryController.java             [NEW] 实验室 2 接口
│   ├── DepartmentController.java             [NEW] 院系 2 接口
│   └── SubjectEvaluationController.java      [NEW] 学科评估 2 接口
├── service/university/
│   ├── LaboratoryService.java                [NEW]
│   ├── DepartmentService.java                [NEW]
│   └── SubjectEvaluationService.java         [NEW]
├── service/impl/university/
│   ├── LaboratoryServiceImpl.java            [NEW]
│   ├── DepartmentServiceImpl.java            [NEW]
│   └── SubjectEvaluationServiceImpl.java     [NEW]
├── vo/university/
│   ├── LaboratoryListVO.java                 [NEW] id + name + labType
│   ├── LaboratoryDetailVO.java               [NEW] 20 字段（含 4 个 JSONB）
│   ├── DepartmentListVO.java                 [NEW] id + departmentName + departmentType
│   ├── DepartmentReportVO.java               [NEW] 11 个 JSONB 字段
│   ├── SubjectEvaluationListVO.java          [NEW] 4 字段
│   └── SubjectEvaluationGradeStatsVO.java    [NEW] grade + count
└── dto/university/
    ├── LaboratoryQueryDTO.java               [NEW] 仅继承 BasePageQueryDTO
    ├── DepartmentQueryDTO.java               [NEW] 仅继承 BasePageQueryDTO
    └── SubjectEvaluationQueryDTO.java        [NEW] 仅继承 BasePageQueryDTO

haifeng-app/Products/
└── order6.md                                 [NEW] API 文档
```

---

## 实施顺序

按依赖正向推进，每个 Task 完成后做一次编译验证：

1. **Task 1-3**：新增 3 个 DTO（无依赖）
2. **Task 4-9**：新增 6 个 VO（无依赖）
3. **Task 10**：修改 `SubjectEvaluationMapper`，新增 `countByGrade`
4. **Task 11-13**：3 个 Service 接口
5. **Task 14-16**：3 个 ServiceImpl
6. **Task 17-19**：3 个 Controller
7. **Task 20**：编译总验证
8. **Task 21**：写 `order6.md` API 文档
9. **Task 22**：手工接口验收清单（最后人工执行，本计划不自动执行）

---

## Task 1：新增 LaboratoryQueryDTO

**Files:**
- Create: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\dto\university\LaboratoryQueryDTO.java`

- [ ] **Step 1.1：写入 DTO 文件**

```java
package com.haifeng.app.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * C 端实验室列表分页查询 DTO
 * universityId 在 path 上，本 DTO 仅承载分页参数（page/size）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LaboratoryQueryDTO extends BasePageQueryDTO {
}
```

- [ ] **Step 1.2：编译验证**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -q`
Expected: BUILD SUCCESS（如有 warning 关于 LF/CRLF 可忽略）

**Modified files for final commit:**
- `haifeng-app/.../dto/university/LaboratoryQueryDTO.java` (NEW)

---

## Task 2：新增 DepartmentQueryDTO

**Files:**
- Create: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\dto\university\DepartmentQueryDTO.java`

- [ ] **Step 2.1：写入 DTO 文件**

```java
package com.haifeng.app.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * C 端院系列表分页查询 DTO
 * universityId 在 path 上，本 DTO 仅承载分页参数（page/size）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DepartmentQueryDTO extends BasePageQueryDTO {
}
```

- [ ] **Step 2.2：编译验证**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -q`
Expected: BUILD SUCCESS

**Modified files for final commit:**
- `haifeng-app/.../dto/university/DepartmentQueryDTO.java` (NEW)

---

## Task 3：新增 SubjectEvaluationQueryDTO

**Files:**
- Create: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\dto\university\SubjectEvaluationQueryDTO.java`

- [ ] **Step 3.1：写入 DTO 文件**

```java
package com.haifeng.app.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * C 端学科评估明细分页查询 DTO
 * universityId 在 path 上，本 DTO 仅承载分页参数（page/size）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SubjectEvaluationQueryDTO extends BasePageQueryDTO {
}
```

- [ ] **Step 3.2：编译验证**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -q`
Expected: BUILD SUCCESS

**Modified files for final commit:**
- `haifeng-app/.../dto/university/SubjectEvaluationQueryDTO.java` (NEW)

---

## Task 4：新增 LaboratoryListVO

**Files:**
- Create: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\vo\university\LaboratoryListVO.java`

- [ ] **Step 4.1：写入 VO 文件**

```java
package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端实验室列表 VO（spec §3.1） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaboratoryListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String labType;
}
```

- [ ] **Step 4.2：编译验证**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -q`
Expected: BUILD SUCCESS

**Modified files for final commit:**
- `haifeng-app/.../vo/university/LaboratoryListVO.java` (NEW)

---

## Task 5：新增 LaboratoryDetailVO

**Files:**
- Create: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\vo\university\LaboratoryDetailVO.java`

- [ ] **Step 5.1：写入 VO 文件**

```java
package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/** C 端实验室详情 VO（spec §3.2，20 字段，含 4 个 JSONB） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaboratoryDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String universityName;
    private String labType;
    private String establishedYear;
    private String region;
    private String department;
    private String director;
    private String staffCount;
    private String studentCount;
    private String email;
    private String phone;
    private String introduction;
    private String researchDescription;
    private String labSpace;
    private String openTopics;
    private String cooperation;
    private String visitingScholars;

    // JSONB 字段，原样透传给前端
    private List<String> researchFields;
    private List<Map<String, Object>> statistics;
    private List<String> majorEquipment;
    private List<Map<String, Object>> coreTeam;
}
```

- [ ] **Step 5.2：编译验证**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -q`
Expected: BUILD SUCCESS

**Modified files for final commit:**
- `haifeng-app/.../vo/university/LaboratoryDetailVO.java` (NEW)

---

## Task 6：新增 DepartmentListVO

**Files:**
- Create: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\vo\university\DepartmentListVO.java`

- [ ] **Step 6.1：写入 VO 文件**

```java
package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端院系列表 VO（spec §3.3） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String departmentName;
    private String departmentType;
}
```

- [ ] **Step 6.2：编译验证**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -q`
Expected: BUILD SUCCESS

**Modified files for final commit:**
- `haifeng-app/.../vo/university/DepartmentListVO.java` (NEW)

---

## Task 7：新增 DepartmentReportVO

**Files:**
- Create: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\vo\university\DepartmentReportVO.java`

- [ ] **Step 7.1：写入 VO 文件**

```java
package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/** C 端院系分析报告 VO（spec §3.4，全 JSONB 透传） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String subtitle;
    private Map<String, Object> overview;
    private List<Map<String, Object>> subjectsDetail;
    private Map<String, Object> postgraduate;
    private List<Map<String, Object>> citySalary;
    private List<Map<String, Object>> salary;
    private List<Map<String, Object>> career;
    private Map<String, Object> trends;
    private Map<String, Object> prospects;
    private Map<String, Object> disclaimer;
    private List<Map<String, Object>> majorCompose;
}
```

- [ ] **Step 7.2：编译验证**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -q`
Expected: BUILD SUCCESS

**Modified files for final commit:**
- `haifeng-app/.../vo/university/DepartmentReportVO.java` (NEW)

---

## Task 8：新增 SubjectEvaluationListVO

**Files:**
- Create: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\vo\university\SubjectEvaluationListVO.java`

- [ ] **Step 8.1：写入 VO 文件**

```java
package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端学科评估明细列表 VO（spec §3.5） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectEvaluationListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String disciplineCode;
    private String disciplineName;
    private String evaluationRound;
    private String evaluationGrade;
}
```

- [ ] **Step 8.2：编译验证**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -q`
Expected: BUILD SUCCESS

**Modified files for final commit:**
- `haifeng-app/.../vo/university/SubjectEvaluationListVO.java` (NEW)

---

## Task 9：新增 SubjectEvaluationGradeStatsVO

**Files:**
- Create: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\vo\university\SubjectEvaluationGradeStatsVO.java`

- [ ] **Step 9.1：写入 VO 文件**

```java
package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * C 端学科评估等级统计 VO（spec §3.6）
 * grade 取值固定为 A+/A/A-/B+/B/B-/C+/C/C- 之一；count 缺数据时为 0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectEvaluationGradeStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String grade;
    private Integer count;
}
```

- [ ] **Step 9.2：编译验证**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -q`
Expected: BUILD SUCCESS

**Modified files for final commit:**
- `haifeng-app/.../vo/university/SubjectEvaluationGradeStatsVO.java` (NEW)

---

## Task 10：SubjectEvaluationMapper 新增 countByGrade 方法

**Files:**
- Modify: `D:\0code\haifeng\backend\haifeng\haifeng-common\src\main\java\com\haifeng\common\mapper\university\SubjectEvaluationMapper.java`

- [ ] **Step 10.1：在 mapper 接口尾部追加方法**

打开文件，在最后一个方法 `existsByUniversityAndDiscipline(...)` 之后、`}` 之前插入：

```java

    /**
     * 按等级统计该院校的学科评估数量（仅 status=1）
     * 返回示例：[{grade=A+, count=37}, {grade=A, count=25}, ...]
     * 注意：返回结果不保证 9 个等级齐全，Service 层负责补齐缺失等级为 0。
     */
    @Select("SELECT evaluation_grade AS grade, COUNT(*) AS count " +
            "FROM t_subject_evaluation " +
            "WHERE university_id = #{universityId} AND status = 1 " +
            "GROUP BY evaluation_grade")
    java.util.List<java.util.Map<String, Object>> countByGrade(@Param("universityId") Long universityId);
```

> 注：文件顶部已 `import org.apache.ibatis.annotations.Param/Select`，无需新增 import；`List/Map` 使用全限定名以避免污染现有 imports。如你倾向加 import，自行追加 `import java.util.List;` 与 `import java.util.Map;` 即可。

- [ ] **Step 10.2：编译验证**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-common -am compile -q`
Expected: BUILD SUCCESS

**Modified files for final commit:**
- `haifeng-common/.../mapper/university/SubjectEvaluationMapper.java` (MODIFIED)

---

## Task 11：新增 LaboratoryService 接口

**Files:**
- Create: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\service\university\LaboratoryService.java`

- [ ] **Step 11.1：写入接口文件**

```java
package com.haifeng.app.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.LaboratoryQueryDTO;
import com.haifeng.app.vo.university.LaboratoryDetailVO;
import com.haifeng.app.vo.university.LaboratoryListVO;

public interface LaboratoryService {

    /**
     * 按 universityId 分页查询实验室（仅 status=1）
     * 排序 sort_order ASC, id DESC
     * universityId 不存在时返回空分页（不报错）
     */
    IPage<LaboratoryListVO> page(Long universityId, LaboratoryQueryDTO dto);

    /**
     * 按主键查询实验室详情（仅 status=1）
     * 不存在或已下架时抛 BusinessException(404, "实验室不存在")
     */
    LaboratoryDetailVO detail(Long labId);
}
```

- [ ] **Step 11.2：编译验证**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -q`
Expected: BUILD SUCCESS

**Modified files for final commit:**
- `haifeng-app/.../service/university/LaboratoryService.java` (NEW)

---

## Task 12：新增 DepartmentService 接口

**Files:**
- Create: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\service\university\DepartmentService.java`

- [ ] **Step 12.1：写入接口文件**

```java
package com.haifeng.app.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.DepartmentQueryDTO;
import com.haifeng.app.vo.university.DepartmentListVO;
import com.haifeng.app.vo.university.DepartmentReportVO;

public interface DepartmentService {

    /**
     * 按 universityId 分页查询院系（仅 status=1）
     * 排序 sort_order ASC, id DESC
     */
    IPage<DepartmentListVO> page(Long universityId, DepartmentQueryDTO dto);

    /**
     * 按院系 id 查询其分析报告
     * 报告不存在时抛 BusinessException(404, "院系分析报告不存在")
     */
    DepartmentReportVO report(Long departmentId);
}
```

- [ ] **Step 12.2：编译验证**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -q`
Expected: BUILD SUCCESS

**Modified files for final commit:**
- `haifeng-app/.../service/university/DepartmentService.java` (NEW)

---

## Task 13：新增 SubjectEvaluationService 接口

**Files:**
- Create: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\service\university\SubjectEvaluationService.java`

- [ ] **Step 13.1：写入接口文件**

```java
package com.haifeng.app.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.SubjectEvaluationQueryDTO;
import com.haifeng.app.vo.university.SubjectEvaluationGradeStatsVO;
import com.haifeng.app.vo.university.SubjectEvaluationListVO;

import java.util.List;

public interface SubjectEvaluationService {

    /**
     * 按 universityId 分页查询学科评估明细（仅 status=1）
     * 排序 evaluation_grade ASC, sort_order ASC
     */
    IPage<SubjectEvaluationListVO> page(Long universityId, SubjectEvaluationQueryDTO dto);

    /**
     * 按等级统计该院校的学科评估数量
     * 返回固定 9 条，按 ['A+','A','A-','B+','B','B-','C+','C','C-'] 顺序，缺失等级 count=0
     */
    List<SubjectEvaluationGradeStatsVO> gradeStats(Long universityId);
}
```

- [ ] **Step 13.2：编译验证**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -q`
Expected: BUILD SUCCESS

**Modified files for final commit:**
- `haifeng-app/.../service/university/SubjectEvaluationService.java` (NEW)

---

## Task 14：新增 LaboratoryServiceImpl

**Files:**
- Create: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\service\impl\university\LaboratoryServiceImpl.java`

- [ ] **Step 14.1：写入实现类文件**

```java
package com.haifeng.app.service.impl.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.university.LaboratoryQueryDTO;
import com.haifeng.app.service.university.LaboratoryService;
import com.haifeng.app.vo.university.LaboratoryDetailVO;
import com.haifeng.app.vo.university.LaboratoryListVO;
import com.haifeng.common.entity.university.Laboratory;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.LaboratoryMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LaboratoryServiceImpl implements LaboratoryService {

    private static final short STATUS_PUBLISHED = 1;

    private final LaboratoryMapper laboratoryMapper;

    @Override
    public IPage<LaboratoryListVO> page(Long universityId, LaboratoryQueryDTO dto) {
        Page<Laboratory> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Laboratory> wrapper = new LambdaQueryWrapper<Laboratory>()
                .eq(Laboratory::getUniversityId, universityId)
                .eq(Laboratory::getStatus, STATUS_PUBLISHED)
                .orderByAsc(Laboratory::getSortOrder)
                .orderByDesc(Laboratory::getId);

        IPage<Laboratory> entityPage = laboratoryMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    @Override
    public LaboratoryDetailVO detail(Long labId) {
        Laboratory e = laboratoryMapper.selectOne(
                new LambdaQueryWrapper<Laboratory>()
                        .eq(Laboratory::getId, labId)
                        .eq(Laboratory::getStatus, STATUS_PUBLISHED));
        if (e == null) {
            log.debug("实验室不存在或已下架, labId={}", labId);
            throw new BusinessException(ResultCode.NOT_FOUND, "实验室不存在");
        }

        return LaboratoryDetailVO.builder()
                .universityName(e.getUniversityName())
                .labType(e.getLabType())
                .establishedYear(e.getEstablishedYear())
                .region(e.getRegion())
                .department(e.getDepartment())
                .director(e.getDirector())
                .staffCount(e.getStaffCount())
                .studentCount(e.getStudentCount())
                .email(e.getEmail())
                .phone(e.getPhone())
                .introduction(e.getIntroduction())
                .researchDescription(e.getResearchDescription())
                .labSpace(e.getLabSpace())
                .openTopics(e.getOpenTopics())
                .cooperation(e.getCooperation())
                .visitingScholars(e.getVisitingScholars())
                .researchFields(e.getResearchFields())
                .statistics(e.getStatistics())
                .majorEquipment(e.getMajorEquipment())
                .coreTeam(e.getCoreTeam())
                .build();
    }

    private LaboratoryListVO toListVO(Laboratory e) {
        return LaboratoryListVO.builder()
                .id(e.getId())
                .name(e.getName())
                .labType(e.getLabType())
                .build();
    }
}
```

- [ ] **Step 14.2：编译验证**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -q`
Expected: BUILD SUCCESS

**Modified files for final commit:**
- `haifeng-app/.../service/impl/university/LaboratoryServiceImpl.java` (NEW)

---

## Task 15：新增 DepartmentServiceImpl

**Files:**
- Create: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\service\impl\university\DepartmentServiceImpl.java`

- [ ] **Step 15.1：写入实现类文件**

```java
package com.haifeng.app.service.impl.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.university.DepartmentQueryDTO;
import com.haifeng.app.service.university.DepartmentService;
import com.haifeng.app.vo.university.DepartmentListVO;
import com.haifeng.app.vo.university.DepartmentReportVO;
import com.haifeng.common.entity.university.Department;
import com.haifeng.common.entity.university.DepartmentReport;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.DepartmentMapper;
import com.haifeng.common.mapper.university.DepartmentReportMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private static final short STATUS_PUBLISHED = 1;

    private final DepartmentMapper departmentMapper;
    private final DepartmentReportMapper departmentReportMapper;

    @Override
    public IPage<DepartmentListVO> page(Long universityId, DepartmentQueryDTO dto) {
        Page<Department> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<Department>()
                .eq(Department::getUniversityId, universityId)
                .eq(Department::getStatus, STATUS_PUBLISHED)
                .orderByAsc(Department::getSortOrder)
                .orderByDesc(Department::getId);

        IPage<Department> entityPage = departmentMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    @Override
    public DepartmentReportVO report(Long departmentId) {
        // mapper 自带 status=1 过滤，见 DepartmentReportMapper.selectByDepartmentId
        DepartmentReport r = departmentReportMapper.selectByDepartmentId(departmentId);
        if (r == null) {
            log.debug("院系分析报告不存在, departmentId={}", departmentId);
            throw new BusinessException(ResultCode.NOT_FOUND, "院系分析报告不存在");
        }

        return DepartmentReportVO.builder()
                .subtitle(r.getSubtitle())
                .overview(r.getOverview())
                .subjectsDetail(r.getSubjectsDetail())
                .postgraduate(r.getPostgraduate())
                .citySalary(r.getCitySalary())
                .salary(r.getSalary())
                .career(r.getCareer())
                .trends(r.getTrends())
                .prospects(r.getProspects())
                .disclaimer(r.getDisclaimer())
                .majorCompose(r.getMajorCompose())
                .build();
    }

    private DepartmentListVO toListVO(Department e) {
        return DepartmentListVO.builder()
                .id(e.getId())
                .departmentName(e.getDepartmentName())
                .departmentType(e.getDepartmentType())
                .build();
    }
}
```

- [ ] **Step 15.2：编译验证**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -q`
Expected: BUILD SUCCESS

**Modified files for final commit:**
- `haifeng-app/.../service/impl/university/DepartmentServiceImpl.java` (NEW)

---

## Task 16：新增 SubjectEvaluationServiceImpl

**Files:**
- Create: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\service\impl\university\SubjectEvaluationServiceImpl.java`

- [ ] **Step 16.1：写入实现类文件**

```java
package com.haifeng.app.service.impl.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.university.SubjectEvaluationQueryDTO;
import com.haifeng.app.service.university.SubjectEvaluationService;
import com.haifeng.app.vo.university.SubjectEvaluationGradeStatsVO;
import com.haifeng.app.vo.university.SubjectEvaluationListVO;
import com.haifeng.common.entity.university.SubjectEvaluation;
import com.haifeng.common.mapper.university.SubjectEvaluationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectEvaluationServiceImpl implements SubjectEvaluationService {

    private static final short STATUS_PUBLISHED = 1;

    /** 固定 9 个等级的输出顺序（spec §3.6） */
    private static final List<String> GRADE_ORDER = List.of(
            "A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-");

    private final SubjectEvaluationMapper subjectEvaluationMapper;

    @Override
    public IPage<SubjectEvaluationListVO> page(Long universityId, SubjectEvaluationQueryDTO dto) {
        Page<SubjectEvaluation> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<SubjectEvaluation> wrapper = new LambdaQueryWrapper<SubjectEvaluation>()
                .eq(SubjectEvaluation::getUniversityId, universityId)
                .eq(SubjectEvaluation::getStatus, STATUS_PUBLISHED)
                .orderByAsc(SubjectEvaluation::getEvaluationGrade)
                .orderByAsc(SubjectEvaluation::getSortOrder);

        IPage<SubjectEvaluation> entityPage = subjectEvaluationMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    @Override
    public List<SubjectEvaluationGradeStatsVO> gradeStats(Long universityId) {
        List<Map<String, Object>> rows = subjectEvaluationMapper.countByGrade(universityId);

        // 把 mapper 结果归一化成 grade -> count
        Map<String, Integer> existing = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object g = row.get("grade");
            Object c = row.get("count");
            if (g != null && c != null) {
                existing.put(String.valueOf(g), ((Number) c).intValue());
            }
        }

        // 按固定 9 个等级顺序输出，缺失补 0
        return GRADE_ORDER.stream()
                .map(grade -> SubjectEvaluationGradeStatsVO.builder()
                        .grade(grade)
                        .count(existing.getOrDefault(grade, 0))
                        .build())
                .collect(Collectors.toList());
    }

    private SubjectEvaluationListVO toListVO(SubjectEvaluation e) {
        return SubjectEvaluationListVO.builder()
                .disciplineCode(e.getDisciplineCode())
                .disciplineName(e.getDisciplineName())
                .evaluationRound(e.getEvaluationRound())
                .evaluationGrade(e.getEvaluationGrade())
                .build();
    }
}
```

- [ ] **Step 16.2：编译验证**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -q`
Expected: BUILD SUCCESS

**Modified files for final commit:**
- `haifeng-app/.../service/impl/university/SubjectEvaluationServiceImpl.java` (NEW)

---

## Task 17：新增 LaboratoryController

**Files:**
- Create: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\controller\university\LaboratoryController.java`

- [ ] **Step 17.1：写入 Controller 文件**

```java
package com.haifeng.app.controller.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.LaboratoryQueryDTO;
import com.haifeng.app.service.university.LaboratoryService;
import com.haifeng.app.vo.university.LaboratoryDetailVO;
import com.haifeng.app.vo.university.LaboratoryListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端实验室列表 / 详情（spec §3.1、§3.2）
 * 均需登录
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/university")
@RequiredArgsConstructor
public class LaboratoryController {

    private final LaboratoryService laboratoryService;

    /** spec §3.1：按 universityId 分页查询实验室列表 */
    @RequireLogin
    @GetMapping("/{universityId}/laboratories")
    public R<IPage<LaboratoryListVO>> list(
            @PathVariable Long universityId,
            @Valid LaboratoryQueryDTO dto) {
        return R.ok(laboratoryService.page(universityId, dto));
    }

    /** spec §3.2：按主键查询实验室详情 */
    @RequireLogin
    @GetMapping("/laboratories/{labId}")
    public R<LaboratoryDetailVO> detail(@PathVariable Long labId) {
        return R.ok(laboratoryService.detail(labId));
    }
}
```

- [ ] **Step 17.2：编译验证**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -q`
Expected: BUILD SUCCESS

**Modified files for final commit:**
- `haifeng-app/.../controller/university/LaboratoryController.java` (NEW)

---

## Task 18：新增 DepartmentController

**Files:**
- Create: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\controller\university\DepartmentController.java`

- [ ] **Step 18.1：写入 Controller 文件**

```java
package com.haifeng.app.controller.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.DepartmentQueryDTO;
import com.haifeng.app.service.university.DepartmentService;
import com.haifeng.app.vo.university.DepartmentListVO;
import com.haifeng.app.vo.university.DepartmentReportVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端院系列表 / 院系分析报告（spec §3.3、§3.4）
 * 均需登录
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/university")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /** spec §3.3：按 universityId 分页查询院系列表 */
    @RequireLogin
    @GetMapping("/{universityId}/departments")
    public R<IPage<DepartmentListVO>> list(
            @PathVariable Long universityId,
            @Valid DepartmentQueryDTO dto) {
        return R.ok(departmentService.page(universityId, dto));
    }

    /** spec §3.4：按院系 id 查询其分析报告 */
    @RequireLogin
    @GetMapping("/departments/{departmentId}/report")
    public R<DepartmentReportVO> report(@PathVariable Long departmentId) {
        return R.ok(departmentService.report(departmentId));
    }
}
```

- [ ] **Step 18.2：编译验证**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -q`
Expected: BUILD SUCCESS

**Modified files for final commit:**
- `haifeng-app/.../controller/university/DepartmentController.java` (NEW)

---

## Task 19：新增 SubjectEvaluationController

**Files:**
- Create: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\controller\university\SubjectEvaluationController.java`

- [ ] **Step 19.1：写入 Controller 文件**

```java
package com.haifeng.app.controller.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.SubjectEvaluationQueryDTO;
import com.haifeng.app.service.university.SubjectEvaluationService;
import com.haifeng.app.vo.university.SubjectEvaluationGradeStatsVO;
import com.haifeng.app.vo.university.SubjectEvaluationListVO;
import com.haifeng.common.annotation.RequireLogin;
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
 * C 端学科评估明细列表 / 等级统计（spec §3.5、§3.6）
 * 均需登录
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/university")
@RequiredArgsConstructor
public class SubjectEvaluationController {

    private final SubjectEvaluationService subjectEvaluationService;

    /** spec §3.5：按 universityId 分页查询学科评估明细 */
    @RequireLogin
    @GetMapping("/{universityId}/subject-evaluations")
    public R<IPage<SubjectEvaluationListVO>> list(
            @PathVariable Long universityId,
            @Valid SubjectEvaluationQueryDTO dto) {
        return R.ok(subjectEvaluationService.page(universityId, dto));
    }

    /** spec §3.6：按 universityId 查询 9 个等级的 count 统计 */
    @RequireLogin
    @GetMapping("/{universityId}/subject-evaluations/grade-stats")
    public R<List<SubjectEvaluationGradeStatsVO>> gradeStats(
            @PathVariable Long universityId) {
        return R.ok(subjectEvaluationService.gradeStats(universityId));
    }
}
```

- [ ] **Step 19.2：编译验证**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am compile -q`
Expected: BUILD SUCCESS

**Modified files for final commit:**
- `haifeng-app/.../controller/university/SubjectEvaluationController.java` (NEW)

---

## Task 20：整体编译 + 启动冒烟（可选）

**Files:** 无新增

- [ ] **Step 20.1：整工程编译**

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app -am clean compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 20.2：（可选）启动应用看 Spring 是否能装配所有 Bean**

> 此步若数据库 / Redis 未就绪可跳过。仅做静态分析的话 20.1 已足够。

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app spring-boot:run -DskipTests -q` （另开终端，启动后立即 Ctrl+C 终止）
Expected: 控制台出现 "Started HaiFengAppApplication" 且无 BeanCreationException

**Modified files for final commit:** 无

---

## Task 21：编写 API 文档 order6.md

**Files:**
- Create: `D:\0code\haifeng\backend\haifeng\haifeng-app\Products\order6.md`

- [ ] **Step 21.1：写入 API 文档**

文档结构沿用 `order5.md` 风格（顶部功能概述表 + 通用说明 + 每个接口 详情 + 请求示例 + 响应示例）。完整内容：

````markdown
# C 端院校次模块 API 文档（实验室 / 院系 / 学科评估）

## 功能概述

本模块在院校管理父模块下提供 3 个只读子模块，共 6 个接口。所有接口均需登录，不加 Redis 缓存（实时读库）。

| 子模块 | 功能 | 权限要求 |
|--------|------|----------|
| 实验室 | 按院校分页查询实验室列表 | 登录用户 |
| 实验室 | 按实验室 id 查询详情 | 登录用户 |
| 院系 | 按院校分页查询院系列表 | 登录用户 |
| 院系 | 按院系 id 查询分析报告 | 登录用户 |
| 学科评估 | 按院校分页查询学科评估明细 | 登录用户 |
| 学科评估 | 按院校查询 9 个等级的数量统计 | 登录用户 |

---

## 通用说明

### 权限说明

| 权限标识 | 说明 |
|----------|------|
| 登录用户 | 需携带有效 Access Token；由 `@RequireLogin` 切面校验 |

### 统一响应格式

```json
{
  "code": 200,
  "msg": "success",
  "data": { ... },
  "timestamp": 1717392000000
}
```

### 错误码

| code | 含义 |
|------|------|
| 200 | 成功 |
| 401 | 未登录或 Token 过期 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 分页参数（BasePageQueryDTO）

| 字段 | 类型 | 必填 | 默认 | 校验 |
|------|------|------|------|------|
| page | int | 否 | 1 | ≥1 |
| size | int | 否 | 10 | 10–1000 |

---

## 1. 实验室列表

`GET /api/v1/app/university/{universityId}/laboratories`

**Path：**
| 字段 | 类型 | 说明 |
|------|------|------|
| universityId | Long | 院校 id |

**Query：** page、size（见通用说明）

**响应示例：**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      { "id": 10001, "name": "人工智能实验室", "labType": "国家重点实验室" },
      { "id": 10002, "name": "智能感知实验室", "labType": "省部级重点实验室" }
    ],
    "total": 2,
    "size": 10,
    "current": 1
  }
}
```

---

## 2. 实验室详情

`GET /api/v1/app/university/laboratories/{labId}`

**Path：**
| 字段 | 类型 | 说明 |
|------|------|------|
| labId | Long | 实验室主键（从列表接口获取） |

**响应示例（节选）：**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "universityName": "清华大学",
    "labType": "国家重点实验室",
    "establishedYear": "1985",
    "region": "北京",
    "department": "计算机系",
    "director": "张某某",
    "staffCount": "120",
    "studentCount": "300",
    "email": "lab@xxx.edu.cn",
    "phone": "010-12345678",
    "introduction": "……",
    "researchDescription": "……",
    "labSpace": "……",
    "openTopics": "……",
    "cooperation": "……",
    "visitingScholars": "……",
    "researchFields": ["AI", "机器学习"],
    "statistics": [{"year": 2024, "papers": 42}],
    "majorEquipment": ["GPU 集群", "光谱仪"],
    "coreTeam": [{"name": "张某某", "title": "教授"}]
  }
}
```

**错误：** 实验室不存在 → `{"code":404,"msg":"实验室不存在"}`

---

## 3. 院系列表

`GET /api/v1/app/university/{universityId}/departments`

**Path：** universityId（Long）
**Query：** page、size

**响应示例：**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      { "id": 20001, "departmentName": "计算机科学与技术学院", "departmentType": "工学" },
      { "id": 20002, "departmentName": "外国语学院", "departmentType": "文学" }
    ],
    "total": 2,
    "size": 10,
    "current": 1
  }
}
```

---

## 4. 院系分析报告

`GET /api/v1/app/university/departments/{departmentId}/report`

**Path：**
| 字段 | 类型 | 说明 |
|------|------|------|
| departmentId | Long | 院系主键（从院系列表接口获取） |

**响应示例（节选，JSONB 字段原样返回，结构以 DB 实际存储为准）：**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "subtitle": "2024 年度深度分析",
    "overview": {"summary": "……"},
    "subjectsDetail": [{"name": "……"}],
    "postgraduate": {"rate": 0.42},
    "citySalary": [{"city": "北京", "salary": 18000}],
    "salary": [{"level": "毕业生", "value": 12000}],
    "career": [{"industry": "互联网", "rate": 0.55}],
    "trends": {"hotness": "……"},
    "prospects": {"forecast": "……"},
    "disclaimer": {"text": "……"},
    "majorCompose": [{"name": "……", "ratio": 0.3}]
  }
}
```

**错误：** 报告未配置 → `{"code":404,"msg":"院系分析报告不存在"}`

---

## 5. 学科评估明细列表

`GET /api/v1/app/university/{universityId}/subject-evaluations`

**Path：** universityId（Long）
**Query：** page、size

**响应示例：**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "disciplineCode": "0812",
        "disciplineName": "计算机科学与技术",
        "evaluationRound": "第四轮",
        "evaluationGrade": "A+"
      },
      {
        "disciplineCode": "0701",
        "disciplineName": "数学",
        "evaluationRound": "第四轮",
        "evaluationGrade": "A"
      }
    ],
    "total": 2,
    "size": 10,
    "current": 1
  }
}
```

---

## 6. 学科评估等级统计

`GET /api/v1/app/university/{universityId}/subject-evaluations/grade-stats`

**Path：** universityId（Long）
**Query：** 无

**响应示例（固定 9 条，count=0 也返回）：**

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    { "grade": "A+", "count": 37 },
    { "grade": "A",  "count": 25 },
    { "grade": "A-", "count": 18 },
    { "grade": "B+", "count": 12 },
    { "grade": "B",  "count": 8 },
    { "grade": "B-", "count": 5 },
    { "grade": "C+", "count": 2 },
    { "grade": "C",  "count": 1 },
    { "grade": "C-", "count": 0 }
  ]
}
```
````

- [ ] **Step 21.2：核对文档**

打开新写的 `order6.md`，确认：
- 6 个接口路径与 Controller 一致
- 所有响应示例 JSON 可解析（在线 JSON 校验器走一遍）

**Modified files for final commit:**
- `haifeng-app/Products/order6.md` (NEW)

---

## Task 22：手工接口验收（人工执行，本计划不自动执行）

**Files:** 无

- [ ] **Step 22.1：启动应用**（依赖 PG + Redis 就绪）

Run: `cd D:/0code/haifeng/backend/haifeng && mvn -pl haifeng-app spring-boot:run -q`
Expected: 控制台出现 "Started HaiFengAppApplication"

- [ ] **Step 22.2：用 Postman/Apifox 逐接口验收**

按 spec §9「验收清单」逐项打勾：

- [ ] 实验室列表：选一所有实验室的院校 id，验证返回字段为 `{id, name, labType}`，分页元数据正确
- [ ] 实验室列表：传一个不存在的 universityId，验证返回空分页（不是 404）
- [ ] 实验室详情：用上一步返回的 id，验证 20 个字段齐全；JSONB 字段（researchFields/statistics/majorEquipment/coreTeam）结构原样
- [ ] 实验室详情：传一个不存在的 labId，验证返回 `{"code":404,"msg":"实验室不存在"}`
- [ ] 院系列表：同上验证字段 `{id, departmentName, departmentType}`
- [ ] 院系报告：用院系 id 查，验证 11 个 JSONB 字段齐全
- [ ] 院系报告：用不存在的 departmentId，验证 404
- [ ] 学科评估明细：分页可用，字段 4 个齐全
- [ ] 学科评估等级统计：固定返回 9 条，缺失等级 count=0
- [ ] 任一接口去掉 Token → 401

- [ ] **Step 22.3：（用户自行）统一 git commit**

按你的项目惯例统一提交所有改动：

```bash
cd D:/0code/haifeng/backend/haifeng
git status   # 核对改动文件清单
git add <files>
git commit -m "feat(app/university): add laboratory/department/subject-evaluation read APIs"
```

**Modified files for final commit:** 无（本步用户手动）

---

## 最终提交文件总览（供 Task 22.3 参考）

```
# 新增 (NEW)
haifeng-app/src/main/java/com/haifeng/app/dto/university/LaboratoryQueryDTO.java
haifeng-app/src/main/java/com/haifeng/app/dto/university/DepartmentQueryDTO.java
haifeng-app/src/main/java/com/haifeng/app/dto/university/SubjectEvaluationQueryDTO.java
haifeng-app/src/main/java/com/haifeng/app/vo/university/LaboratoryListVO.java
haifeng-app/src/main/java/com/haifeng/app/vo/university/LaboratoryDetailVO.java
haifeng-app/src/main/java/com/haifeng/app/vo/university/DepartmentListVO.java
haifeng-app/src/main/java/com/haifeng/app/vo/university/DepartmentReportVO.java
haifeng-app/src/main/java/com/haifeng/app/vo/university/SubjectEvaluationListVO.java
haifeng-app/src/main/java/com/haifeng/app/vo/university/SubjectEvaluationGradeStatsVO.java
haifeng-app/src/main/java/com/haifeng/app/service/university/LaboratoryService.java
haifeng-app/src/main/java/com/haifeng/app/service/university/DepartmentService.java
haifeng-app/src/main/java/com/haifeng/app/service/university/SubjectEvaluationService.java
haifeng-app/src/main/java/com/haifeng/app/service/impl/university/LaboratoryServiceImpl.java
haifeng-app/src/main/java/com/haifeng/app/service/impl/university/DepartmentServiceImpl.java
haifeng-app/src/main/java/com/haifeng/app/service/impl/university/SubjectEvaluationServiceImpl.java
haifeng-app/src/main/java/com/haifeng/app/controller/university/LaboratoryController.java
haifeng-app/src/main/java/com/haifeng/app/controller/university/DepartmentController.java
haifeng-app/src/main/java/com/haifeng/app/controller/university/SubjectEvaluationController.java
haifeng-app/Products/order6.md
docs/superpowers/specs/2026-06-04-app-university-details-design.md  (已 git add，未 commit)
docs/superpowers/plans/2026-06-04-app-university-details-plan.md    (本计划)

# 修改 (MODIFIED)
haifeng-common/src/main/java/com/haifeng/common/mapper/university/SubjectEvaluationMapper.java
haifeng-app/Need/8院校次.md  (已是 AM 状态，详见 git status 起始快照)
```
