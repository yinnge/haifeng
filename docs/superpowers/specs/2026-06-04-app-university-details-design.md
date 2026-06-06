# C 端院校次模块设计文档（实验室 / 院系 / 学科评估）

> 创建时间：2026-06-04
> 模块归属：`haifeng-app`，院校管理父模块下的 3 个新增子模块
> 需求来源：`haifeng-app/Need/8院校次.md`

---

## 1. 目标与范围

为 C 端院校详情页新增 **3 个只读子模块** 的数据查询能力：

| 子模块 | 接口数 | 权限 |
|--------|--------|------|
| 实验室列表 / 详情 | 2 | 登录 |
| 院系列表 / 院系分析报告 | 2 | 登录 |
| 学科评估明细 / 等级统计 | 2 | 登录 |

所有接口**纯查询、无写入**，**实时读库、不加缓存**（与现有 `university` 模块保持一致）。

**不在本次范围**：
- B 端管理后台（增删改）
- 数据导入 / 同步
- DB 索引调整（现有索引已满足全部查询）
- common 模块 entity / mapper 重构（仅按需新增 1 个 mapper 方法）

---

## 2. 总体架构

复用 `haifeng-app` 已建立的 4 层结构，与 `CampusGalleryController` / `UniversityGuideController` 保持完全一致的代码风格：

```
Controller (路由 + @RequireLogin)
    ↓ 依赖
Service (接口)
    ↓ 实现
ServiceImpl (业务编排 + LambdaQueryWrapper)
    ↓ 注入
Mapper (haifeng-common，已存在，仅按需新增 1 方法)
```

**新增文件清单（18 个 java 文件 + 1 个 mapper 方法 + 1 个 API 文档）**：

| 类型 | 路径 | 文件 |
|------|------|------|
| Controller | `haifeng-app/.../controller/university/` | `LaboratoryController.java`、`DepartmentController.java`、`SubjectEvaluationController.java` |
| Service | `haifeng-app/.../service/university/` | `LaboratoryService.java`、`DepartmentService.java`、`SubjectEvaluationService.java` |
| ServiceImpl | `haifeng-app/.../service/impl/university/` | `LaboratoryServiceImpl.java`、`DepartmentServiceImpl.java`、`SubjectEvaluationServiceImpl.java` |
| VO | `haifeng-app/.../vo/university/` | `LaboratoryListVO.java`、`LaboratoryDetailVO.java`、`DepartmentListVO.java`、`DepartmentReportVO.java`、`SubjectEvaluationListVO.java`、`SubjectEvaluationGradeStatsVO.java` |
| DTO | `haifeng-app/.../dto/university/` | `LaboratoryQueryDTO.java`、`DepartmentQueryDTO.java`、`SubjectEvaluationQueryDTO.java` |
| Mapper 方法 | `haifeng-common/.../mapper/university/SubjectEvaluationMapper.java` | 新增 `countByGrade(universityId)` |
| API 文档 | `haifeng-app/Products/` | `order6.md` |

**复用现有资产**（不修改）：
- common entity：`Laboratory`、`Department`、`DepartmentReport`、`SubjectEvaluation`（含 JSONB Jackson handler）
- common mapper：`LaboratoryMapper`、`DepartmentMapper`、`DepartmentReportMapper`（已含 `selectByDepartmentId`）
- 注解：`@RequireLogin`
- 公共类：`BasePageQueryDTO`、`R<>`、`BusinessException`

---

## 3. 接口契约

所有接口均挂载在 `/api/v1/app/university` 前缀下，并标注 `@RequireLogin`。

### 3.1 实验室列表

```
GET /api/v1/app/university/{universityId}/laboratories
Query: page=1&size=10 (BasePageQueryDTO，page≥1，10≤size≤1000)
Response: R<IPage<LaboratoryListVO>>
```

**LaboratoryListVO** 字段：
- `id: Long`（前端通常不渲染，用于点击跳转详情）
- `name: String`
- `labType: String`

**查询逻辑**（`LaboratoryServiceImpl.page`）：
```
WHERE university_id = #{universityId} AND status = 1
ORDER BY sort_order ASC, id DESC
LIMIT/OFFSET 由 MyBatis-Plus 分页插件注入
```
`universityId` 不存在时返回空分页（不报错），与 `CampusGalleryServiceImpl` 一致。

---

### 3.2 实验室详情

```
GET /api/v1/app/university/laboratories/{labId}
Response: R<LaboratoryDetailVO>
```

**LaboratoryDetailVO** 字段（共 20 个，对应需求列出的"全字段"）：
| 字段 | 类型 | 说明 |
|------|------|------|
| universityName | String | 所属院校名称 |
| labType | String | 实验室类型 |
| establishedYear | String | 成立年份 |
| region | String | 所在区域 |
| department | String | 所属院系 |
| director | String | 负责人 |
| staffCount | String | 教职工数 |
| studentCount | String | 学生数 |
| email | String | 联系邮箱 |
| phone | String | 联系电话 |
| introduction | TEXT/String | 简介 |
| researchDescription | TEXT/String | 研究方向描述 |
| labSpace | TEXT/String | 实验空间 |
| openTopics | TEXT/String | 开放课题 |
| cooperation | TEXT/String | 合作单位 |
| visitingScholars | TEXT/String | 访问学者 |
| researchFields | `List<String>` | JSONB，原样返回 |
| statistics | `List<Map<String,Object>>` | JSONB，原样返回 |
| majorEquipment | `List<String>` | JSONB，原样返回 |
| coreTeam | `List<Map<String,Object>>` | JSONB，原样返回 |

**查询逻辑**：
```java
new LambdaQueryWrapper<Laboratory>()
    .eq(Laboratory::getId, labId)
    .eq(Laboratory::getStatus, 1)
```
查询为 null → `throw new BusinessException(404, "实验室不存在")`。

---

### 3.3 院系列表

```
GET /api/v1/app/university/{universityId}/departments
Query: page=1&size=10
Response: R<IPage<DepartmentListVO>>
```

**DepartmentListVO** 字段：
- `id: Long`
- `departmentName: String`
- `departmentType: String`

**查询逻辑**：
```
WHERE university_id = #{universityId} AND status = 1
ORDER BY sort_order ASC, id DESC
```

---

### 3.4 院系分析报告

```
GET /api/v1/app/university/departments/{departmentId}/report
Response: R<DepartmentReportVO>
```

**DepartmentReportVO** 字段（全部为 JSONB 原样返回，类型与 entity 一致）：
| 字段 | 类型 |
|------|------|
| subtitle | String |
| overview | `Map<String,Object>` |
| subjectsDetail | `List<Map<String,Object>>` |
| postgraduate | `Map<String,Object>` |
| citySalary | `List<Map<String,Object>>` |
| salary | `List<Map<String,Object>>` |
| career | `List<Map<String,Object>>` |
| trends | `Map<String,Object>` |
| prospects | `Map<String,Object>` |
| disclaimer | `Map<String,Object>` |
| majorCompose | `List<Map<String,Object>>` |

**查询逻辑**：直接调用现有 `DepartmentReportMapper.selectByDepartmentId(departmentId)`，已带 `status = 1` 过滤。
返回 null → `throw new BusinessException(404, "院系分析报告不存在")`。

---

### 3.5 学科评估明细列表

```
GET /api/v1/app/university/{universityId}/subject-evaluations
Query: page=1&size=10
Response: R<IPage<SubjectEvaluationListVO>>
```

**SubjectEvaluationListVO** 字段：
- `disciplineCode: String`
- `disciplineName: String`
- `evaluationRound: String`
- `evaluationGrade: String`

**查询逻辑**：
```
WHERE university_id = #{universityId} AND status = 1
ORDER BY CASE evaluation_grade
   WHEN 'A+' THEN 1 WHEN 'A' THEN 2 WHEN 'A-' THEN 3
   WHEN 'B+' THEN 4 WHEN 'B' THEN 5 WHEN 'B-' THEN 6
   WHEN 'C+' THEN 7 WHEN 'C' THEN 8 WHEN 'C-' THEN 9
   ELSE 99 END ASC,
   sort_order ASC
```
> 注：`evaluation_grade` 是字符串列，按 ASCII 直接排序会得到 `A, A+, A-, B, B+, B-, ...`（无后缀的字母排在带 `+/-` 前面，错误）。使用 `CASE` 显式映射为分数学意义上的等级顺序。

---

### 3.6 学科评估等级统计

```
GET /api/v1/app/university/{universityId}/subject-evaluations/grade-stats
Response: R<List<SubjectEvaluationGradeStatsVO>>
```

**SubjectEvaluationGradeStatsVO** 字段：
- `grade: String`（取值固定为 `A+`/`A`/`A-`/`B+`/`B`/`B-`/`C+`/`C`/`C-` 之一）
- `count: Integer`

**返回约定**：固定 9 条记录，按 `['A+','A','A-','B+','B','B-','C+','C','C-']` 顺序输出；某等级无数据时 `count = 0` 也返回（便于前端表格直接渲染）。

**SQL**（新增到 `SubjectEvaluationMapper`）：
```java
@Select("SELECT evaluation_grade AS grade, COUNT(*) AS count " +
        "FROM t_subject_evaluation " +
        "WHERE university_id = #{universityId} AND status = 1 " +
        "GROUP BY evaluation_grade")
List<Map<String, Object>> countByGrade(@Param("universityId") Long universityId);
```

**Service 处理**：
1. 调 mapper 拿到 `[{grade, count}]`，可能少于 9 条
2. 转成 `Map<String, Integer> existing`
3. 按固定顺序 9 个等级 `["A+", "A", ..., "C-"]` 遍历，缺失补 0，组装 VO 列表返回

---

## 4. 关键实现细节

### 4.1 JSONB 字段流转

入库 → entity（已有 Jackson handler 反序列化）→ VO（同类型字段直接 `BeanUtils.copyProperties` 或显式 setter）→ Jackson 序列化 JSON 给前端，结构保持原样。无需任何额外转换。

### 4.2 等级统计实现选择

| 方案 | 描述 | 选择 |
|------|------|------|
| A | 单 SQL `GROUP BY` + 内存补齐 9 个等级 | ✅ 采用 |
| B | 9 次独立 `COUNT(*)` + UNION ALL | ❌ SQL 冗长 |

采用 A，理由：单校学科评估行数通常 < 200，`idx_se_university` 索引扫极快，内存补齐成本可忽略。

### 4.3 索引现状（无需新增）

| 表 | 现有索引 | 覆盖查询 |
|----|----------|----------|
| `laboratories` | `idx_labs_university_id`、`idx_labs_status` | 列表（按 universityId+status） |
| `t_department` | `idx_dept_university_id`、`idx_dept_status` | 列表（按 universityId+status） |
| `department_reports` | `idx_dept_reports_department_id` | 报告（按 departmentId） |
| `t_subject_evaluation` | `idx_se_university`、`idx_se_status` | 明细 + 等级统计（按 universityId） |
| 主键 | 各表 PK | 详情接口（按 id） |

### 4.4 错误处理

| 场景 | 处理 |
|------|------|
| 列表接口 universityId 不存在 | 返回空分页 `IPage<>` (records=[], total=0) |
| 详情接口 id 不存在或 status≠1 | `BusinessException(404, "xxx不存在")` |
| 等级统计 universityId 不存在 | 返回 9 条 count=0 的记录 |
| 未登录 | `@RequireLogin` 切面拦截，统一返回 401 |

### 4.5 不修改 common 模块的部分

- 仅在 `SubjectEvaluationMapper` 新增 1 个 `@Select` 方法
- 不动 entity / 不动其他 mapper
- 不动 DB schema、不加索引

### 4.6 命名约定

- VO / DTO Java 字段全部驼峰，依赖项目现有 Jackson 配置自动产生驼峰 JSON
- 接口路径资源名复数：`laboratories` / `departments` / `subject-evaluations`，与 `guides`、`gallery` 风格一致
- VO 类后缀：列表用 `XxxListVO`，详情用 `XxxDetailVO` 或 `XxxReportVO`、`XxxGradeStatsVO`

---

## 5. API 文档

新增 `haifeng-app/Products/order6.md`，沿用 order1–5 的格式：
- 顶部功能概述表
- 通用说明（权限 / 响应格式 / 错误码）
- 6 个接口逐一列出请求示例 + 响应示例 + 字段说明

---

## 6. 测试策略

按现有 `haifeng-app` 模块惯例：**不写单测**（项目当前模块均未配置单测基础设施，避免引入新框架）。

**验收方式**：
1. Postman / Apifox 手工测每个接口，验证：
   - 正常路径（数据存在、字段完整）
   - 边界（universityId 不存在、id 不存在、status=0 软删除数据）
   - 权限（未登录返回 401）
2. JSONB 字段抽样核对，确保结构原样透传

---

## 7. 实施顺序建议

1. **VO + DTO**（无依赖，可并行）
2. **SubjectEvaluationMapper 新增 `countByGrade` 方法**（common 模块改动）
3. **Service + ServiceImpl**（依赖 entity/mapper/VO/DTO）
4. **Controller**（依赖 Service）
5. **API 文档 `order6.md`**
6. **手工接口验收**

实施细节由后续 writing-plans 阶段产出。

---

## 8. 风险与权衡

| 风险 | 影响 | 缓解 |
|------|------|------|
| 详情页 id 跨域可见 | 低（id 是普通自增雪花 ID，不暴露敏感信息） | 接受 |
| 大表 GROUP BY 性能 | 低（单校行数小，索引覆盖） | 接受；如未来全表统计成热点，再加复合索引 |
| 未做幂等 / 防刷 | 低（纯查询） | 后续如有需求接入网关限流 |

---

## 9. 验收清单

- [ ] 6 个接口全部联通，返回结构符合本文档
- [ ] 实验室/院系列表分页参数生效（page、size 校验生效）
- [ ] 详情接口 id 不存在抛 404
- [ ] 学科评估等级统计返回固定 9 条
- [ ] JSONB 字段前端可直接渲染（结构未被破坏）
- [ ] `@RequireLogin` 拦截未登录请求
- [ ] `order6.md` API 文档与实现一致
- [ ] 未引入新的 DB 索引、未改动现有 entity
