# C 端专业管理模块设计规格

> 日期：2026-06-04
> 对应需求：`haifeng-app/Need/9专业管理.md`
> 对外 API 文档：`haifeng-app/Products/order7.md`

---

## 1. 模块边界

**模块名**：C 端专业管理（app/major）
**父模块**：专业管理（与院校管理平级）
**特性**：只读、不加 Redis 缓存（实时读库）、`status=1` 过滤、JSONB/TEXT[] 字段原样透传

### 接口清单（8 个）

| # | 任务 | URL | 权限 | 说明 |
|---|---|---|---|---|
| 1 | 任务1接口1 | `GET /api/v1/app/major/list` | 公开 | 专业列表（name+code 模糊 + type/category 精准） |
| 2 | 任务1接口2 | `GET /api/v1/app/major/{majorId}/detail` | 登录 | 专业详情（联 t_major + t_major_detail） |
| 3 | 任务1接口3 | `GET /api/v1/app/major/category-stats` | 公开 | 按 major_category 分组统计专业数 |
| 4 | 任务1接口4 | `GET /api/v1/app/major/ranking` | Pro | 薪资/就业排行榜（动态 sortBy + sortOrder） |
| 5 | 任务2接口1 | `GET /api/v1/app/postgrad-major/list` | 登录 | 考研专业列表（多筛选） |
| 6 | 任务2接口2 | `GET /api/v1/app/postgrad-major/{majorId}/detail` | 登录 | 考研专业详情 |
| 7 | 任务3接口1 | `GET /api/v1/app/university/{universityId}/postgrad-majors` | Pro | 大学 → 考研专业（按 degreeType 精准筛选） |
| 8 | 任务4接口1 | `GET /api/v1/app/postgrad-major/{majorId}/universities` | Pro | 考研专业 → 大学（按 category 精准筛选） |

**不实现的接口**：任务 3 接口 2 / 任务 4 接口 2——前端拿返回的 id 自行调用已有详情接口（任务 2 接口 2 / order5 院校详情）。

---

## 2. 权限语义

| 注解 | 语义 |
|---|---|
| 无注解 | 公开访问，无需登录 |
| `@RequireLogin` | 需携带有效 Access Token |
| `@RequirePro` | Pro 及以上（含 Vip） |

与 order5 的 academic 接口语义一致。

---

## 3. 分层与目录结构

### 3.1 haifeng-common（最小改动 — 只加 mapper 方法）

```
mapper/major/MajorMapper.java                       + countByCategory()
mapper/major/PostgradMajorUniversityMapper.java     + selectPostgradMajorsByUniversity()
                                                    + selectUniversitiesByPostgradMajor()
```

entity 全部已存在（Major / MajorDetail / PostgradMajor / PostgradMajorUniversity），无需新建。

### 3.2 haifeng-app（新增）

```
controller/major/
  MajorController.java                           // 任务1的4个接口
  PostgradMajorController.java                   // 任务2的2个接口 + 任务4接口1
controller/university/
  UniversityPostgradMajorController.java         // 任务3接口1（URL 属于 /university/ 域）

service/major/
  MajorService.java
  PostgradMajorService.java
service/impl/major/
  MajorServiceImpl.java
  PostgradMajorServiceImpl.java
service/university/
  UniversityPostgradMajorService.java
service/impl/university/
  UniversityPostgradMajorServiceImpl.java

dto/major/
  MajorListQueryDTO.java                         // 任务1接口1
  MajorRankingQueryDTO.java                      // 任务1接口4
  PostgradMajorListQueryDTO.java                 // 任务2接口1
  PostgradMajorUniversityQueryDTO.java           // 任务4接口1
dto/university/
  UniversityPostgradMajorQueryDTO.java           // 任务3接口1

vo/major/
  MajorListVO.java                               // 任务1接口1 + 任务1接口4 复用
  MajorDetailVO.java                             // 任务1接口2
  MajorCategoryStatVO.java                       // 任务1接口3
  PostgradMajorListVO.java                       // 任务2接口1
  PostgradMajorDetailVO.java                     // 任务2接口2
  PostgradMajorBriefVO.java                      // 任务3接口1
  UniversityBriefForPostgradVO.java              // 任务4接口1
```

### 3.3 docs（新增）

```
docs/superpowers/specs/2026-06-04-app-major-management-design.md   // 本文档
haifeng-app/Products/order7.md                                      // 对外 API 文档
```

---

## 4. DTO 字段

所有 DTO 继承 `BasePageQueryDTO`（自带 page/size + 校验：page≥1，size 10–1000）。

### 4.1 MajorListQueryDTO（任务1接口1）

| 字段 | 类型 | 必填 | 查询方式 | 说明 |
|---|---|---|---|---|
| name | String | 否 | 模糊 LIKE %name% | 对应 major_name |
| code | String | 否 | 模糊 LIKE %code% | 对应 major_code |
| majorType | String | 否 | 精准 = | 专业类型 |
| majorCategory | String | 否 | 精准 = | 专业类别 |

### 4.2 MajorRankingQueryDTO（任务1接口4）

| 字段 | 类型 | 必填 | 默认值 | 查询方式 | 说明 |
|---|---|---|---|---|---|
| name | String | 否 | — | 模糊 LIKE %name% | 专业名称 |
| majorCategory | String | 否 | — | 精准 = | 专业类别 |
| sortBy | String | 否 | employmentRate | 枚举 | `employmentRate` / `salaryMin` / `salaryMax`，`@Pattern` 校验 |
| sortOrder | String | 否 | desc | 枚举 | `asc` / `desc`，`@Pattern` 校验 |

### 4.3 PostgradMajorListQueryDTO（任务2接口1）

| 字段 | 类型 | 必填 | 查询方式 | 说明 |
|---|---|---|---|---|
| name | String | 否 | 模糊 LIKE %name% | 考研专业名 |
| code | String | 否 | 模糊 LIKE %code% | 考研专业代码 |
| degreeType | String | 否 | 精准 = | 学术学位 / 专业学位 |
| disciplineCategory | String | 否 | 精准 = | 学科门类 |
| popularity | String | 否 | 精准 = | 热门 / 一般 / 冷门 |
| difficulty | String | 否 | 精准 = | 高 / 中 / 低 |

### 4.4 UniversityPostgradMajorQueryDTO（任务3接口1）

| 字段 | 类型 | 必填 | 查询方式 | 说明 |
|---|---|---|---|---|
| degreeType | String | 否 | 精准 = | 学术学位 / 专业学位 |

### 4.5 PostgradMajorUniversityQueryDTO（任务4接口1）

| 字段 | 类型 | 必填 | 查询方式 | 说明 |
|---|---|---|---|---|
| category | String | 否 | 精准 = | 院校类型（综合/理工/师范/...） |

---

## 5. VO 字段

### 5.1 MajorListVO（任务1接口1 + 任务1接口4 复用）

| 字段 | 类型 | 来源 | 说明 |
|---|---|---|---|
| id | Long | t_major | 专业 ID |
| majorCode | String | t_major | 专业代码 |
| majorName | String | t_major | 专业名称 |
| disciplineName | String | t_major | 学科名称 |
| majorCategory | String | t_major | 专业类别 |
| parentCategory | String | t_major | 门类 |
| majorTags | String | t_major | 专业标签 |
| degreeAwarded | String | t_major | 授予学位 |
| employmentRate | BigDecimal | t_major | 就业率 |
| salaryMin | Integer | t_major | 最低薪资 |
| salaryMax | Integer | t_major | 最高薪资 |
| description | String | t_major | 简介 |

### 5.2 MajorDetailVO（任务1接口2）——主表+详情表合并返回

**来自 t_major（10 个）**：

| 字段 | 类型 | 说明 |
|---|---|---|
| majorName | String | 专业名称 |
| majorCode | String | 专业代码 |
| disciplineName | String | 学科名称 |
| majorCategory | String | 专业类别 |
| parentCategory | String | 门类 |
| majorTags | String | 专业标签 |
| degreeAwarded | String | 授予学位 |
| employmentRate | BigDecimal | 就业率 |
| salaryMin | Integer | 最低薪资 |
| salaryMax | Integer | 最高薪资 |
| description | String | 简介 |

**来自 t_major_detail（11 个）**：

| 字段 | 类型 | 说明 |
|---|---|---|
| courseCount | Integer | 课程数量 |
| graduateScale | String | 毕业规模 |
| maleRatio | BigDecimal | 男生比例 |
| femaleRatio | BigDecimal | 女生比例 |
| majorDescription | String | 专业描述 |
| trainingObjective | String | 培养目标 |
| trainingRequirement | String | 培养要求 |
| subjectRequirement | String | 选科要求 |
| careerProspect | String | 就业前景 |
| mainCourses | String[] | 主干课程（TEXT[]） |
| knowledgeSkills | String[] | 知识技能（TEXT[]） |

### 5.3 MajorCategoryStatVO（任务1接口3）

| 字段 | 类型 | 说明 |
|---|---|---|
| majorCategory | String | 专业类别 |
| count | Integer | 该类下专业数量 |

### 5.4 PostgradMajorListVO（任务2接口1）

| 字段 | 类型 | 来源 | 说明 |
|---|---|---|---|
| id | Long | t_postgrad_major | ID |
| majorName | String | t_postgrad_major | 专业名称 |
| majorCode | String | t_postgrad_major | 专业代码 |
| degreeType | String | t_postgrad_major | 学位类型 |
| disciplineCategory | String | t_postgrad_major | 学科门类 |
| popularity | String | t_postgrad_major | 热门程度 |
| difficulty | String | t_postgrad_major | 难度 |
| brief | String | t_postgrad_major | 简介 |
| examSubjects | String[] | t_postgrad_major | 考试科目（TEXT[]） |

### 5.5 PostgradMajorDetailVO（任务2接口2）

| 字段 | 类型 | 说明 |
|---|---|---|
| majorName | String | 专业名称 |
| majorCode | String | 专业代码 |
| degreeType | String | 学位类型 |
| disciplineCategory | String | 学科门类 |
| popularity | String | 热门程度 |
| difficulty | String | 难度 |
| introduction | String | 介绍 |
| examSubjects | String[] | 考试科目 |
| admissionRequirements | String[] | 报考条件 |
| crossExamDifficulty | String | 跨考难度 |
| crossExamDescription | String | 跨考说明 |
| crossExamFactors | String[] | 跨考因素 |

### 5.6 PostgradMajorBriefVO（任务3接口1）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | Long | 考研专业 ID |
| majorName | String | 专业名称 |
| degreeType | String | 学位类型 |

### 5.7 UniversityBriefForPostgradVO（任务4接口1）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | Long | 院校 ID |
| name | String | 院校名称 |
| category | String | 院校类型 |

---

## 6. 错误码

| 接口 | 场景 | code | msg |
|---|---|---|---|
| 任务1接口2（专业详情） | t_major 不存在 / status=0 | 404 | 专业不存在 |
| 任务1接口2（专业详情） | t_major 存在但 t_major_detail 未配 | 404 | 专业详情不存在 |
| 任务2接口2（考研专业详情） | t_postgrad_major 不存在 / status=0 | 404 | 考研专业不存在 |
| 任务1接口4（薪资排行） | 普通用户（非 Pro/Vip） | 403 | 权限不足（需要专业版及以上） |
| 任务3/4 接口（关联列表） | 上游 id 不存在 | 200 | 返回空 Page（不抛 404） |
| 任务1接口3（category-stats） | 无数据 | 200 | 返回空数组 |
| 所有接口 | 参数校验失败 | 400 | 字段级校验信息 |
| 所有需登录接口 | 未带 token / token 失效 | 401 | 未登录或 Token 过期 |

---

## 7. 排序规则

| 接口 | 排序 |
|---|---|
| 任务1接口1（专业列表） | `id DESC` |
| 任务1接口4（薪资排行） | `${sortBy} ${sortOrder} NULLS LAST, id DESC` |
| 任务1接口3（category-stats） | `count DESC` |
| 任务2接口1（考研专业列表） | `id DESC` |
| 任务3接口1（大学→考研专业） | `pmu.sort_order ASC, pm.id DESC` |
| 任务4接口1（考研专业→大学） | `pmu.sort_order ASC, u.id DESC` |

---

## 8. 关键 SQL

### 8.1 MajorMapper.countByCategory

```java
@Select("SELECT major_category AS majorCategory, COUNT(*) AS count " +
        "FROM t_major " +
        "WHERE status = 1 AND major_category IS NOT NULL " +
        "GROUP BY major_category " +
        "ORDER BY COUNT(*) DESC")
List<Map<String, Object>> countByCategory();
```

Service 层转 `List<MajorCategoryStatVO>`（参考 `SubjectEvaluationServiceImpl.gradeStats` 写法）。

### 8.2 PostgradMajorUniversityMapper.selectPostgradMajorsByUniversity

```java
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
```

### 8.3 PostgradMajorUniversityMapper.selectUniversitiesByPostgradMajor

```java
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

---

## 9. 关键约束

1. 所有接口 `status=1` 过滤；联表时两表都过滤
2. id 字段裸 `Long` 返回（跟现有 VO 风格一致）
3. JSONB / TEXT[] 字段原样透传（用现成的 `JacksonTypeHandler`）
4. `@RequireLogin` / `@RequirePro`（Pro 及以上含 Vip）打在 controller 方法上
5. Mapper 自定义 SQL 用 `@Select`（含 `<script>`），不引入 XML
6. 模糊查询用 `LIKE %x%`；任务 1 接口 4 的排序加 `NULLS LAST`
7. 不引入 Redis 缓存，实时读库
8. 不调用 git（最后统一提交）
9. 任务 3 接口 1 的 controller 放在 `controller/university/`（URL 属 /university/ 域），其余专业相关 controller 放在 `controller/major/`
10. 任务 1 接口 4 复用 `MajorListVO`，不单独建 `MajorRankingVO`
