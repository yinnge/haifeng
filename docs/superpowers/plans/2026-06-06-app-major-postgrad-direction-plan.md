# C 端专业-考研方向关联查询实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 C 端为本科专业/考研专业列表补充 2 个嵌套资源接口（按 id 查关联对端的 id+name 分页列表），均需 Pro 权限。

**Architecture:** 镜像现有 `PostgradMajorUniversityController` 模式（`@Select` + `<script>` 分页联表、Map → VO）。不引入 Redis 缓存，实时读库。`MajorPostgradDirection` 实体/Mapper 已存在，只追加 2 个 mapper 方法；新增 2 个 VO；新增 2 个 service 方法 + 2 个 endpoint；新增 2 个 service 单测。

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, JUnit 5 + Mockito + AssertJ, PostgreSQL, Lombok

**特别说明（与本项目其它计划的差异）：**
- 本计划**不包含 git commit 步骤**。按用户约定，所有修改完成后由用户统一提交。
- 每个任务末尾的"提交"步骤被替换为"本地校验"步骤（编译或单测）。

---

## 文件改动总览

| 模块 | 文件 | 动作 |
|---|---|---|
| haifeng-common | `mapper/major/MajorPostgradDirectionMapper.java` | 改（追加 2 个方法） |
| haifeng-app | `vo/major/PostgradMajorDirectionBriefVO.java` | 新建 |
| haifeng-app | `vo/major/UndergraduateMajorDirectionBriefVO.java` | 新建 |
| haifeng-app | `service/major/MajorService.java` | 改（追加接口） |
| haifeng-app | `service/major/PostgradMajorService.java` | 改（追加接口） |
| haifeng-app | `service/impl/major/MajorServiceImpl.java` | 改（追加实现 + 注入 mapper） |
| haifeng-app | `service/impl/major/PostgradMajorServiceImpl.java` | 改（追加实现 + 注入 mapper） |
| haifeng-app | `controller/major/MajorController.java` | 改（追加 endpoint） |
| haifeng-app | `controller/major/PostgradMajorController.java` | 改（追加 endpoint） |
| haifeng-app | `test/.../service/major/MajorServiceImplTest.java` | 新建 |
| haifeng-app | `test/.../service/major/PostgradMajorServiceImplTest.java` | 新建 |

---

## Task 1: 在 `MajorPostgradDirectionMapper` 追加 2 个分页联表方法

**Files:**
- Modify: `D:\0code\haifeng\backend\haifeng\haifeng-common\src\main\java\com\haifeng\common\mapper\major\MajorPostgradDirectionMapper.java`

- [ ] **Step 1: 在 mapper 接口中追加 2 个方法**

打开 `MajorPostgradDirectionMapper.java`，在 `existsByRelation` 方法之后追加：

```java
    /**
     * 接口1：给定本科专业 id，分页返回关联的考研专业（id + 名称）
     * 走 idx_mpd_major 索引 → 主键回表 t_postgrad_major → status 过滤
     */
    @Select("SELECT pm.id AS id, pm.major_name AS postgradMajorName " +
            "FROM t_major_postgrad_direction mpd " +
            "JOIN t_postgrad_major pm ON pm.id = mpd.postgrad_major_id " +
            "WHERE mpd.major_id = #{majorId} AND pm.status = 1 " +
            "ORDER BY mpd.sort_order ASC, pm.id DESC")
    IPage<Map<String, Object>> selectPostgradMajorsByMajorId(
            Page<?> page,
            @Param("majorId") Long majorId);

    /**
     * 接口2：给定考研专业 id，分页返回关联的本科专业（id + 名称）
     * 走 idx_mpd_postgrad 索引 → 主键回表 t_major → status 过滤
     */
    @Select("SELECT m.id AS id, m.major_name AS majorName " +
            "FROM t_major_postgrad_direction mpd " +
            "JOIN t_major m ON m.id = mpd.major_id " +
            "WHERE mpd.postgrad_major_id = #{postgradMajorId} AND m.status = 1 " +
            "ORDER BY mpd.sort_order ASC, m.id DESC")
    IPage<Map<String, Object>> selectMajorsByPostgradMajorId(
            Page<?> page,
            @Param("postgradMajorId") Long postgradMajorId);
```

- [ ] **Step 2: 确认 import 已存在**

确认文件顶部已有以下 import（如缺则补）：
```java
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.Map;
```

> 当前 `MajorPostgradDirectionMapper.java` 已 import 了 `@Select` / `@Param` / `@Mapper`，但可能缺 `IPage` / `Page` / `Map`。若缺则补齐。

- [ ] **Step 3: 编译 haifeng-common 验证**

```bash
cd D:\0code\haifeng\backend\haifeng
mvn -pl haifeng-common clean compile -DskipTests
```

Expected: `BUILD SUCCESS`

---

## Task 2: 创建接口1返回 VO — `PostgradMajorDirectionBriefVO`

**Files:**
- Create: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\vo\major\PostgradMajorDirectionBriefVO.java`

- [ ] **Step 1: 创建文件**

```java
package com.haifeng.app.vo.major;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端本科专业→考研方向列表 VO（接口1，对端是考研专业） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostgradMajorDirectionBriefVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String postgradMajorName;
}
```

- [ ] **Step 2: 编译 haifeng-app 验证**

```bash
cd D:\0code\haifeng\backend\haifeng
mvn -pl haifeng-app clean compile -DskipTests
```

Expected: `BUILD SUCCESS`

---

## Task 3: 创建接口2返回 VO — `UndergraduateMajorDirectionBriefVO`

**Files:**
- Create: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\vo\major\UndergraduateMajorDirectionBriefVO.java`

- [ ] **Step 1: 创建文件**

```java
package com.haifeng.app.vo.major;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端考研方向→本科专业列表 VO（接口2，对端是本科专业） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UndergraduateMajorDirectionBriefVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String majorName;
}
```

- [ ] **Step 2: 编译 haifeng-app 验证**

```bash
cd D:\0code\haifeng\backend\haifeng
mvn -pl haifeng-app clean compile -DskipTests
```

Expected: `BUILD SUCCESS`

---

## Task 4: 在 `MajorService` 接口追加 `postgradDirections` 方法签名

**Files:**
- Modify: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\service\major\MajorService.java`

- [ ] **Step 1: 添加 import 和方法签名**

修改文件，import 部分加入：
```java
import com.haifeng.app.vo.major.PostgradMajorDirectionBriefVO;
import com.haifeng.common.dto.common.BasePageQueryDTO;
```

接口内（在 `ranking` 方法后）追加：
```java
    /** 任务1接口1：本科专业 → 考研方向列表（Pro） */
    IPage<PostgradMajorDirectionBriefVO> postgradDirections(Long majorId, BasePageQueryDTO dto);
```

完整文件最终形态（关键片段）：
```java
package com.haifeng.app.service.major;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.major.MajorListQueryDTO;
import com.haifeng.app.dto.major.MajorRankingQueryDTO;
import com.haifeng.app.vo.major.MajorCategoryStatVO;
import com.haifeng.app.vo.major.MajorDetailVO;
import com.haifeng.app.vo.major.MajorListVO;
import com.haifeng.app.vo.major.PostgradMajorDirectionBriefVO;
import com.haifeng.common.dto.common.BasePageQueryDTO;

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

    /** 任务1接口1（关联查询）：本科专业 → 考研方向列表（Pro） */
    IPage<PostgradMajorDirectionBriefVO> postgradDirections(Long majorId, BasePageQueryDTO dto);
}
```

- [ ] **Step 2: 编译验证**

```bash
cd D:\0code\haifeng\backend\haifeng
mvn -pl haifeng-app clean compile -DskipTests
```

Expected: `BUILD SUCCESS`

---

## Task 5: 在 `PostgradMajorService` 接口追加 `undergraduateMajors` 方法签名

**Files:**
- Modify: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\service\major\PostgradMajorService.java`

- [ ] **Step 1: 添加 import 和方法签名**

修改文件，import 部分加入：
```java
import com.haifeng.app.vo.major.UndergraduateMajorDirectionBriefVO;
import com.haifeng.common.dto.common.BasePageQueryDTO;
```

接口内（在 `universities` 方法后）追加：
```java
    /** 任务1接口2（关联查询）：考研方向 → 本科专业列表（Pro） */
    IPage<UndergraduateMajorDirectionBriefVO> undergraduateMajors(Long postgradMajorId, BasePageQueryDTO dto);
```

完整文件最终形态（关键片段）：
```java
package com.haifeng.app.service.major;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.major.PostgradMajorListQueryDTO;
import com.haifeng.app.dto.major.PostgradMajorUniversityQueryDTO;
import com.haifeng.app.vo.major.PostgradMajorDetailVO;
import com.haifeng.app.vo.major.PostgradMajorListVO;
import com.haifeng.app.vo.major.UniversityBriefForPostgradVO;
import com.haifeng.app.vo.major.UndergraduateMajorDirectionBriefVO;
import com.haifeng.common.dto.common.BasePageQueryDTO;

public interface PostgradMajorService {

    /** 任务2接口1：考研专业列表（登录） */
    IPage<PostgradMajorListVO> page(PostgradMajorListQueryDTO dto);

    /** 任务2接口2：考研专业详情（登录） */
    PostgradMajorDetailVO detail(Long majorId);

    /** 任务4接口1：考研专业 → 大学列表（Pro） */
    IPage<UniversityBriefForPostgradVO> universities(Long majorId, PostgradMajorUniversityQueryDTO dto);

    /** 任务1接口2（关联查询）：考研方向 → 本科专业列表（Pro） */
    IPage<UndergraduateMajorDirectionBriefVO> undergraduateMajors(Long postgradMajorId, BasePageQueryDTO dto);
}
```

- [ ] **Step 2: 编译验证**

```bash
cd D:\0code\haifeng\backend\haifeng
mvn -pl haifeng-app clean compile -DskipTests
```

Expected: `BUILD SUCCESS`

---

## Task 6: 在 `MajorServiceImpl` 实现 `postgradDirections`（TDD）

**Files:**
- Create: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\test\java\com\haifeng\app\service\major\MajorServiceImplTest.java`
- Modify: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\service\impl\major\MajorServiceImpl.java`

- [ ] **Step 1: 写单测（先红）**

创建 `MajorServiceImplTest.java`：

```java
package com.haifeng.app.service.major;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisMapperBuilderAssistant;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.service.impl.major.MajorServiceImpl;
import com.haifeng.app.vo.major.PostgradMajorDirectionBriefVO;
import com.haifeng.common.dto.common.BasePageQueryDTO;
import com.haifeng.common.entity.major.Major;
import com.haifeng.common.mapper.major.MajorDetailMapper;
import com.haifeng.common.mapper.major.MajorMapper;
import com.haifeng.common.mapper.major.MajorPostgradDirectionMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MajorServiceImplTest {

    @Mock private MajorMapper majorMapper;
    @Mock private MajorDetailMapper majorDetailMapper;
    @Mock private MajorPostgradDirectionMapper majorPostgradDirectionMapper;

    @InjectMocks private MajorServiceImpl service;

    @Captor private ArgumentCaptor<Page<?>> pageCaptor;

    @BeforeAll
    static void initTableInfoCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MybatisMapperBuilderAssistant assistant = new MybatisMapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, Major.class);
    }

    @Test
    void postgradDirections_callsMapperAndConvertsResult() {
        BasePageQueryDTO dto = new BasePageQueryDTO();
        dto.setPage(1);
        dto.setSize(20);

        Map<String, Object> row1 = new HashMap<>();
        row1.put("id", 1001L);
        row1.put("postgradMajorName", "计算机科学与技术");

        Map<String, Object> row2 = new HashMap<>();
        row2.put("id", 1002L);
        row2.put("postgradMajorName", "软件工程");

        Page<Map<String, Object>> page = new Page<>(1, 20);
        page.setRecords(List.of(row1, row2));
        page.setTotal(2);
        when(majorPostgradDirectionMapper.selectPostgradMajorsByMajorId(pageCaptor.capture(), org.mockito.ArgumentMatchers.eq(42L)))
                .thenReturn(page);

        IPage<PostgradMajorDirectionBriefVO> result = service.postgradDirections(42L, dto);

        assertThat(pageCaptor.getValue().getCurrent()).isEqualTo(1);
        assertThat(pageCaptor.getValue().getSize()).isEqualTo(20);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getRecords()).hasSize(2);
        assertThat(result.getRecords().get(0).getId()).isEqualTo(1001L);
        assertThat(result.getRecords().get(0).getPostgradMajorName()).isEqualTo("计算机科学与技术");
        assertThat(result.getRecords().get(1).getId()).isEqualTo(1002L);
        assertThat(result.getRecords().get(1).getPostgradMajorName()).isEqualTo("软件工程");
        verify(majorPostgradDirectionMapper).selectPostgradMajorsByMajorId(pageCaptor.capture(), org.mockito.ArgumentMatchers.eq(42L));
    }

    @Test
    void postgradDirections_emptyMapperResult_returnsEmptyPage() {
        BasePageQueryDTO dto = new BasePageQueryDTO();
        Page<Map<String, Object>> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);
        when(majorPostgradDirectionMapper.selectPostgradMajorsByMajorId(pageCaptor.capture(), org.mockito.ArgumentMatchers.eq(99L)))
                .thenReturn(page);

        IPage<PostgradMajorDirectionBriefVO> result = service.postgradDirections(99L, dto);

        assertThat(result.getTotal()).isZero();
        assertThat(result.getRecords()).isEmpty();
    }

    @Test
    void postgradDirections_nullFieldsInRowMap_yieldsNullVoFields() {
        BasePageQueryDTO dto = new BasePageQueryDTO();
        Map<String, Object> row = new HashMap<>();
        row.put("id", null);
        row.put("postgradMajorName", null);
        Page<Map<String, Object>> page = new Page<>(1, 10);
        page.setRecords(List.of(row));
        page.setTotal(1);
        when(majorPostgradDirectionMapper.selectPostgradMajorsByMajorId(pageCaptor.capture(), org.mockito.ArgumentMatchers.eq(1L)))
                .thenReturn(page);

        IPage<PostgradMajorDirectionBriefVO> result = service.postgradDirections(1L, dto);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getId()).isNull();
        assertThat(result.getRecords().get(0).getPostgradMajorName()).isNull();
    }
}
```

- [ ] **Step 2: 跑测试，确认失败**

```bash
cd D:\0code\haifeng\backend\haifeng
mvn -pl haifeng-app test -Dtest=MajorServiceImplTest
```

Expected: `BUILD FAILURE`（`postgradDirections` 方法未实现）

- [ ] **Step 3: 在 `MajorServiceImpl` 注入 mapper 并实现 `postgradDirections`**

修改 `MajorServiceImpl.java`：

1. import 部分加入：
```java
import com.haifeng.app.vo.major.PostgradMajorDirectionBriefVO;
import com.haifeng.common.dto.common.BasePageQueryDTO;
import com.haifeng.common.mapper.major.MajorPostgradDirectionMapper;
```

2. 类字段加入（在 `majorDetailMapper` 之后）：
```java
    private final MajorPostgradDirectionMapper majorPostgradDirectionMapper;
```

3. 在 `ranking` 方法之后追加：
```java
    @Override
    public IPage<PostgradMajorDirectionBriefVO> postgradDirections(Long majorId, BasePageQueryDTO dto) {
        Page<Map<String, Object>> page = new Page<>(dto.getPage(), dto.getSize());
        IPage<Map<String, Object>> mapPage =
                majorPostgradDirectionMapper.selectPostgradMajorsByMajorId(page, majorId);
        return mapPage.convert(row -> PostgradMajorDirectionBriefVO.builder()
                .id(row.get("id") != null ? ((Number) row.get("id")).longValue() : null)
                .postgradMajorName(row.get("postgradMajorName") != null
                        ? String.valueOf(row.get("postgradMajorName")) : null)
                .build());
    }
```

- [ ] **Step 4: 跑测试，确认通过**

```bash
cd D:\0code\haifeng\backend\haifeng
mvn -pl haifeng-app test -Dtest=MajorServiceImplTest
```

Expected: `Tests run: 3, Failures: 0, Errors: 0, Skipped: 0` → `BUILD SUCCESS`

---

## Task 7: 在 `PostgradMajorServiceImpl` 实现 `undergraduateMajors`（TDD）

**Files:**
- Create: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\test\java\com\haifeng\app\service\major\PostgradMajorServiceImplTest.java`
- Modify: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\service\impl\major\PostgradMajorServiceImpl.java`

- [ ] **Step 1: 写单测（先红）**

创建 `PostgradMajorServiceImplTest.java`：

```java
package com.haifeng.app.service.major;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisMapperBuilderAssistant;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.service.impl.major.PostgradMajorServiceImpl;
import com.haifeng.app.vo.major.UndergraduateMajorDirectionBriefVO;
import com.haifeng.common.dto.common.BasePageQueryDTO;
import com.haifeng.common.entity.major.PostgradMajor;
import com.haifeng.common.mapper.major.PostgradMajorMapper;
import com.haifeng.common.mapper.major.PostgradMajorUniversityMapper;
import com.haifeng.common.mapper.major.MajorPostgradDirectionMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgradMajorServiceImplTest {

    @Mock private PostgradMajorMapper postgradMajorMapper;
    @Mock private PostgradMajorUniversityMapper postgradMajorUniversityMapper;
    @Mock private MajorPostgradDirectionMapper majorPostgradDirectionMapper;

    @InjectMocks private PostgradMajorServiceImpl service;

    @Captor private ArgumentCaptor<Page<?>> pageCaptor;

    @BeforeAll
    static void initTableInfoCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MybatisMapperBuilderAssistant assistant = new MybatisMapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, PostgradMajor.class);
    }

    @Test
    void undergraduateMajors_callsMapperAndConvertsResult() {
        BasePageQueryDTO dto = new BasePageQueryDTO();
        dto.setPage(1);
        dto.setSize(20);

        Map<String, Object> row1 = new HashMap<>();
        row1.put("id", 2001L);
        row1.put("majorName", "计算机科学与技术");

        Map<String, Object> row2 = new HashMap<>();
        row2.put("id", 2002L);
        row2.put("majorName", "软件工程");

        Page<Map<String, Object>> page = new Page<>(1, 20);
        page.setRecords(List.of(row1, row2));
        page.setTotal(2);
        when(majorPostgradDirectionMapper.selectMajorsByPostgradMajorId(pageCaptor.capture(), org.mockito.ArgumentMatchers.eq(88L)))
                .thenReturn(page);

        IPage<UndergraduateMajorDirectionBriefVO> result = service.undergraduateMajors(88L, dto);

        assertThat(pageCaptor.getValue().getCurrent()).isEqualTo(1);
        assertThat(pageCaptor.getValue().getSize()).isEqualTo(20);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getRecords()).hasSize(2);
        assertThat(result.getRecords().get(0).getId()).isEqualTo(2001L);
        assertThat(result.getRecords().get(0).getMajorName()).isEqualTo("计算机科学与技术");
        assertThat(result.getRecords().get(1).getId()).isEqualTo(2002L);
        assertThat(result.getRecords().get(1).getMajorName()).isEqualTo("软件工程");
        verify(majorPostgradDirectionMapper).selectMajorsByPostgradMajorId(pageCaptor.capture(), org.mockito.ArgumentMatchers.eq(88L));
    }

    @Test
    void undergraduateMajors_emptyMapperResult_returnsEmptyPage() {
        BasePageQueryDTO dto = new BasePageQueryDTO();
        Page<Map<String, Object>> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);
        when(majorPostgradDirectionMapper.selectMajorsByPostgradMajorId(pageCaptor.capture(), org.mockito.ArgumentMatchers.eq(7L)))
                .thenReturn(page);

        IPage<UndergraduateMajorDirectionBriefVO> result = service.undergraduateMajors(7L, dto);

        assertThat(result.getTotal()).isZero();
        assertThat(result.getRecords()).isEmpty();
    }

    @Test
    void undergraduateMajors_nullFieldsInRowMap_yieldsNullVoFields() {
        BasePageQueryDTO dto = new BasePageQueryDTO();
        Map<String, Object> row = new HashMap<>();
        row.put("id", null);
        row.put("majorName", null);
        Page<Map<String, Object>> page = new Page<>(1, 10);
        page.setRecords(List.of(row));
        page.setTotal(1);
        when(majorPostgradDirectionMapper.selectMajorsByPostgradMajorId(pageCaptor.capture(), org.mockito.ArgumentMatchers.eq(5L)))
                .thenReturn(page);

        IPage<UndergraduateMajorDirectionBriefVO> result = service.undergraduateMajors(5L, dto);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getId()).isNull();
        assertThat(result.getRecords().get(0).getMajorName()).isNull();
    }
}
```

- [ ] **Step 2: 跑测试，确认失败**

```bash
cd D:\0code\haifeng\backend\haifeng
mvn -pl haifeng-app test -Dtest=PostgradMajorServiceImplTest
```

Expected: `BUILD FAILURE`（`undergraduateMajors` 方法未实现）

- [ ] **Step 3: 在 `PostgradMajorServiceImpl` 注入 mapper 并实现 `undergraduateMajors`**

修改 `PostgradMajorServiceImpl.java`：

1. import 部分加入：
```java
import com.haifeng.app.vo.major.UndergraduateMajorDirectionBriefVO;
import com.haifeng.common.dto.common.BasePageQueryDTO;
import com.haifeng.common.mapper.major.MajorPostgradDirectionMapper;
```

2. 类字段加入（在 `postgradMajorUniversityMapper` 之后）：
```java
    private final MajorPostgradDirectionMapper majorPostgradDirectionMapper;
```

3. 在 `universities` 方法之后追加：
```java
    @Override
    public IPage<UndergraduateMajorDirectionBriefVO> undergraduateMajors(Long postgradMajorId, BasePageQueryDTO dto) {
        Page<Map<String, Object>> page = new Page<>(dto.getPage(), dto.getSize());
        IPage<Map<String, Object>> mapPage =
                majorPostgradDirectionMapper.selectMajorsByPostgradMajorId(page, postgradMajorId);
        return mapPage.convert(row -> UndergraduateMajorDirectionBriefVO.builder()
                .id(row.get("id") != null ? ((Number) row.get("id")).longValue() : null)
                .majorName(row.get("majorName") != null
                        ? String.valueOf(row.get("majorName")) : null)
                .build());
    }
```

- [ ] **Step 4: 跑测试，确认通过**

```bash
cd D:\0code\haifeng\backend\haifeng
mvn -pl haifeng-app test -Dtest=PostgradMajorServiceImplTest
```

Expected: `Tests run: 3, Failures: 0, Errors: 0, Skipped: 0` → `BUILD SUCCESS`

---

## Task 8: 在 `MajorController` 追加接口1 endpoint

**Files:**
- Modify: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\controller\major\MajorController.java`

- [ ] **Step 1: 添加 import**

在 import 部分加入：
```java
import com.haifeng.app.vo.major.PostgradMajorDirectionBriefVO;
import com.haifeng.common.dto.common.BasePageQueryDTO;
import com.haifeng.common.annotation.RequirePro;
```

> 注意：`RequirePro` 已在现有 `MajorController` import 中，无需重复；`BasePageQueryDTO` 同理。实际只需新增 `PostgradMajorDirectionBriefVO` 和 `BasePageQueryDTO` 的 import。

最小化新增 import：
```java
import com.haifeng.app.vo.major.PostgradMajorDirectionBriefVO;
import com.haifeng.common.dto.common.BasePageQueryDTO;
```

- [ ] **Step 2: 在 `ranking` 方法之后追加 endpoint**

```java
    /** 任务1接口1（关联查询）：本科专业 → 考研方向列表（Pro） */
    @RequirePro
    @GetMapping("/{majorId}/postgrad-directions")
    public R<IPage<PostgradMajorDirectionBriefVO>> postgradDirections(
            @PathVariable Long majorId,
            @Valid BasePageQueryDTO dto) {
        return R.ok(majorService.postgradDirections(majorId, dto));
    }
```

完整文件最终形态：
```java
package com.haifeng.app.controller.major;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.major.MajorListQueryDTO;
import com.haifeng.app.dto.major.MajorRankingQueryDTO;
import com.haifeng.app.service.major.MajorService;
import com.haifeng.app.vo.major.MajorCategoryStatVO;
import com.haifeng.app.vo.major.MajorDetailVO;
import com.haifeng.app.vo.major.MajorListVO;
import com.haifeng.app.vo.major.PostgradMajorDirectionBriefVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.annotation.RequirePro;
import com.haifeng.common.dto.common.BasePageQueryDTO;
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
 * 新增：任务1接口1（关联查询）需 Pro
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
    @RequireLogin
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

    /** 任务1接口1（关联查询）：本科专业 → 考研方向列表（Pro 及以上） */
    @RequirePro
    @GetMapping("/{majorId}/postgrad-directions")
    public R<IPage<PostgradMajorDirectionBriefVO>> postgradDirections(
            @PathVariable Long majorId,
            @Valid BasePageQueryDTO dto) {
        return R.ok(majorService.postgradDirections(majorId, dto));
    }
}
```

- [ ] **Step 3: 编译验证**

```bash
cd D:\0code\haifeng\backend\haifeng
mvn -pl haifeng-app clean compile -DskipTests
```

Expected: `BUILD SUCCESS`

---

## Task 9: 在 `PostgradMajorController` 追加接口2 endpoint

**Files:**
- Modify: `D:\0code\haifeng\backend\haifeng\haifeng-app\src\main\java\com\haifeng\app\controller\major\PostgradMajorController.java`

- [ ] **Step 1: 添加 import**

最小化新增 import：
```java
import com.haifeng.app.vo.major.UndergraduateMajorDirectionBriefVO;
import com.haifeng.common.dto.common.BasePageQueryDTO;
```

- [ ] **Step 2: 在 `universities` 方法之后追加 endpoint**

```java
    /** 任务1接口2（关联查询）：考研方向 → 本科专业列表（Pro 及以上） */
    @RequirePro
    @GetMapping("/{postgradMajorId}/undergraduate-majors")
    public R<IPage<UndergraduateMajorDirectionBriefVO>> undergraduateMajors(
            @PathVariable Long postgradMajorId,
            @Valid BasePageQueryDTO dto) {
        return R.ok(postgradMajorService.undergraduateMajors(postgradMajorId, dto));
    }
```

完整文件最终形态：
```java
package com.haifeng.app.controller.major;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.major.PostgradMajorListQueryDTO;
import com.haifeng.app.dto.major.PostgradMajorUniversityQueryDTO;
import com.haifeng.app.service.major.PostgradMajorService;
import com.haifeng.app.vo.major.PostgradMajorDetailVO;
import com.haifeng.app.vo.major.PostgradMajorListVO;
import com.haifeng.app.vo.major.UniversityBriefForPostgradVO;
import com.haifeng.app.vo.major.UndergraduateMajorDirectionBriefVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.annotation.RequirePro;
import com.haifeng.common.dto.common.BasePageQueryDTO;
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
 * 新增：任务1接口2（关联查询）需 Pro
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

    /** 任务1接口2（关联查询）：考研方向 → 本科专业列表（Pro 及以上） */
    @RequirePro
    @GetMapping("/{postgradMajorId}/undergraduate-majors")
    public R<IPage<UndergraduateMajorDirectionBriefVO>> undergraduateMajors(
            @PathVariable Long postgradMajorId,
            @Valid BasePageQueryDTO dto) {
        return R.ok(postgradMajorService.undergraduateMajors(postgradMajorId, dto));
    }
}
```

- [ ] **Step 3: 编译验证**

```bash
cd D:\0code\haifeng\backend\haifeng
mvn -pl haifeng-app clean compile -DskipTests
```

Expected: `BUILD SUCCESS`

---

## Task 10: 最终全量编译 + 跑全部 haifeng-app 测试

**Files:** （无新增文件修改 — 仅验证）

- [ ] **Step 1: 全量编译**

```bash
cd D:\0code\haifeng\backend\haifeng
mvn -pl haifeng-app clean compile -DskipTests
```

Expected: `BUILD SUCCESS`，无 warning。

- [ ] **Step 2: 跑 haifeng-app 全部测试**

```bash
cd D:\0code\haifeng\backend\haifeng
mvn -pl haifeng-app test
```

Expected:
- `MajorServiceImplTest` 通过（3 个测试）
- `PostgradMajorServiceImplTest` 通过（3 个测试）
- 其它已有测试全部通过
- `BUILD SUCCESS`

- [ ] **Step 3: 跑全项目编译（确认 haifeng-common / haifeng-admin / haifeng-app 都正常）**

```bash
cd D:\0code\haifeng\backend\haifeng
mvn clean compile -DskipTests
```

Expected: `BUILD SUCCESS`

- [ ] **Step 4: 列出本次修改的文件清单（供你 git commit 使用）**

```bash
cd D:\0code\haifeng\backend\haifeng
git status
```

预期修改清单（请你用此清单做统一提交）：
```
modified:   haifeng-common/src/main/java/com/haifeng/common/mapper/major/MajorPostgradDirectionMapper.java
new file:   haifeng-app/src/main/java/com/haifeng/app/vo/major/PostgradMajorDirectionBriefVO.java
new file:   haifeng-app/src/main/java/com/haifeng/app/vo/major/UndergraduateMajorDirectionBriefVO.java
modified:   haifeng-app/src/main/java/com/haifeng/app/service/major/MajorService.java
modified:   haifeng-app/src/main/java/com/haifeng/app/service/major/PostgradMajorService.java
modified:   haifeng-app/src/main/java/com/haifeng/app/service/impl/major/MajorServiceImpl.java
modified:   haifeng-app/src/main/java/com/haifeng/app/service/impl/major/PostgradMajorServiceImpl.java
modified:   haifeng-app/src/main/java/com/haifeng/app/controller/major/MajorController.java
modified:   haifeng-app/src/main/java/com/haifeng/app/controller/major/PostgradMajorController.java
new file:   haifeng-app/src/test/java/com/haifeng/app/service/major/MajorServiceImplTest.java
new file:   haifeng-app/src/test/java/com/haifeng/app/service/major/PostgradMajorServiceImplTest.java
```

---

## 自检（写完计划后的复核）

- [x] **Spec 覆盖**：
  - 接口1（section 2 / 4.1）→ Task 8 controller + Task 6 service + Task 1 mapper
  - 接口2（section 2 / 4.2）→ Task 9 controller + Task 7 service + Task 1 mapper
  - VO 定义（section 3.1 / 3.2）→ Task 2 / Task 3
  - SQL（section 6.1 / 6.2）→ Task 1
  - Service 实现要点（section 7）→ Task 6 / Task 7
  - 测试（section 9）→ Task 6 / Task 7 内嵌单测
- [x] **占位符扫描**：无 "TBD" / "TODO" / 含糊描述
- [x] **类型一致性**：
  - `selectPostgradMajorsByMajorId(Page<?>, Long) → IPage<Map<String, Object>>` 在 Task 1 定义，Task 6 Step 3 使用
  - `selectMajorsByPostgradMajorId(Page<?>, Long) → IPage<Map<String, Object>>` 在 Task 1 定义，Task 7 Step 3 使用
  - `PostgradMajorDirectionBriefVO` 在 Task 2 定义，Task 4 / 6 / 8 使用
  - `UndergraduateMajorDirectionBriefVO` 在 Task 3 定义，Task 5 / 7 / 9 使用
  - `MajorService.postgradDirections(Long, BasePageQueryDTO)` 在 Task 4 定义，Task 6 / 8 调用
  - `PostgradMajorService.undergraduateMajors(Long, BasePageQueryDTO)` 在 Task 5 定义，Task 7 / 9 调用
