# C 端院校管理模块设计

**日期**：2026-06-03（最后更新：2026-07-08）
**模块**：`haifeng-app` / university（C 端）
**需求来源**：`haifeng-app/Need/7院校管理.md`

## 一、范围

本期实现 5 个任务的接口，对应需求文档任务 1–6：

| # | 任务 | 端点（前缀 `/api/v1/app`） | 权限 |
|---|---|---|---|
| 1 | 院校列表（分页 + 多条件筛选 + 名称模糊） | `GET /university/list` | 公开 |
| 2 | 院校详情（联表 t_universities + t_universities_detail） | `GET /university/{universityId}/detail` | `@RequireLogin` |
| 3.1 | 指南 · 概览 | `GET /university/guides/{universityId}/overview` | `@RequireLogin` |
| 3.2 | 指南 · 基础生存类 | `GET /university/guides/{universityId}/survival` | `@RequireLogin` |
| 3.3 | 指南 · 学业规划类 | `GET /university/guides/{universityId}/academic` | `@RequirePro` |
| 3.4 | 指南 · 社交融入类 | `GET /university/guides/{universityId}/social` | `@RequireLogin` |
| 3.5 | 指南 · 权益与安全类 | `GET /university/guides/{universityId}/safety` | `@RequireLogin` |
| 3.6 | 指南 · 周边生活类 | `GET /university/guides/{universityId}/life` | `@RequireLogin` |
| 4 | 校园图册（按院校 + 分页 + imageType 筛选） | `GET /university/{universityId}/gallery` | `@RequireLogin` |
| 5 | 通道-大学关联查询 | `GET /university/{universityId}/channels` | 公开 |
| 5.2 | 通道选项列表 | `GET /university/channel-options` | 公开 |
| 6.1 | 录取专业组分页查询 | `GET /university/admission-group/{universityId}` | `@RequireVip` |
| 6.2 | 录取专业明细列表 | `GET /university/admission-group/{groupId}/scores` | `@RequireVip` |
| 6.3 | 录取专业组详情 | `GET /university/admission-group/{groupId}/detail` | `@RequireVip` |

**不在本期范围**：院系详情、实验室、学科评估等子模块，留待后续。

## 二、已就绪资产（无需新建）

- Entity：`common/entity/university/{University, UniversityDetail, UniversityGuide, CampusGallery}.java`
- Entity（algorithm 包，复用）：`common/entity/algorithm/{AdmissionGroup, AdmissionMajorScore}.java`
- Mapper：`common/mapper/university/{UniversityMapper, UniversityDetailMapper, UniversityGuideMapper, CampusGalleryMapper}.java`
- Mapper（algorithm 包，复用）：`common/mapper/algorithm/{AdmissionGroupMapper, AdmissionMajorScoreMapper}.java`
- 权限注解：`common/annotation/{RequireLogin, RequirePro, RequireVip}.java`
- 省份枚举：`common/enums/ProvinceEnum.java`
- 分页基类：`common/dto/common/BasePageQueryDTO.java`
- 统一响应：`common/response/R.java`、`ResultCode.NOT_FOUND`
- 业务异常：`common/exception/BusinessException.java`
- 既有的 `app/controller/university/UniversityBriefController.java` 与 `app/vo/university/UniversityBriefVO.java` 服务于 search 模块，**保留不动**。

## 三、新增文件清单

```
haifeng-app/src/main/java/com/haifeng/app/
├── controller/university/
│   ├── UniversityController.java          // 任务 1 + 2
│   ├── UniversityGuideController.java     // 任务 3（6 个子路径）
│   ├── CampusGalleryController.java       // 任务 4
│   └── AdmissionGroupController.java      // 任务 6（3 个接口）
├── service/university/
│   ├── UniversityService.java
│   ├── UniversityGuideService.java
│   ├── CampusGalleryService.java
│   └── AdmissionGroupService.java         // 任务 6
├── service/impl/university/
│   ├── UniversityServiceImpl.java
│   ├── UniversityGuideServiceImpl.java
│   ├── CampusGalleryServiceImpl.java
│   └── AdmissionGroupServiceImpl.java     // 任务 6
├── dto/university/
│   ├── UniversityQueryDTO.java            // 任务 1
│   ├── CampusGalleryQueryDTO.java         // 任务 4
│   └── AdmissionGroupQueryDTO.java        // 任务 6
└── vo/university/
    ├── UniversityListVO.java
    ├── UniversityDetailVO.java
    ├── UniversityGuideOverviewVO.java
    ├── UniversityGuideSurvivalVO.java
    ├── UniversityGuideAcademicVO.java
    ├── UniversityGuideSocialVO.java
    ├── UniversityGuideSafetyVO.java
    ├── UniversityGuideLifeVO.java
    ├── CampusGalleryListVO.java
    ├── AdmissionGroupListVO.java          // 任务 6
    ├── AdmissionGroupDetailVO.java        // 任务 6
    └── AdmissionMajorScoreListVO.java     // 任务 6
```

## 四、接口契约

### 4.1 任务 1 — 院校列表（公开）

```
GET /api/v1/app/university/list
```

**Query 参数（全部 optional，除分页外按需组合 AND）**

| 参数 | 类型 | 匹配方式 | 说明 |
|---|---|---|---|
| page | int | — | 默认 1 |
| size | int | — | 默认 10，可选 10/20/30/50/100 |
| name | String | `LIKE %name%` | 院校名称模糊查询 |
| provinceName | String | `=` | 精准 |
| nature | String | `=` | 公办/民办等 |
| category | String | `=` | 综合/理工等 |
| department | String | `=` | 教育部/工信部等 |
| educationLevel | String | `=` | 本科/专科 |
| hasDoctorate | Boolean | `=` | |
| hasMaster | Boolean | `=` | |

**固定条件**：`status = 1`
**排序**：`sort_order ASC, id DESC`

**`UniversityListVO` 字段**（严格按需求 13 项）：
`name, tags, cityName, educationLevel, provinceName, introduction, imageUrl, nature, category, majorCount, hasDoctorate, hasMaster, department`

**响应**：`R<IPage<UniversityListVO>>`

### 4.2 任务 2 — 院校详情

```
GET /api/v1/app/university/{universityId}/detail
@RequireLogin
```

**实现**：
1. `universityMapper.selectById(universityId)`；为空或 `status != 1` → 抛 `BusinessException(NOT_FOUND, "院校不存在")`
2. `universityDetailMapper.selectOne(eq universityId, status = 1)`；为空 → 抛 `BusinessException(NOT_FOUND, "院校详情不存在")`
3. 合并字段到 `UniversityDetailVO` 返回

**`UniversityDetailVO` 字段**

来自 `t_universities_detail`：
`address, admissionPhone, website, historyGroupScore, scienceGroupScore, carouselImages, introduction, rankings, abroadRate, genderRatio`

来自 `t_universities`：
`name, nameEn, provinceName, cityName, region, category, majorCount, educationLevel, nature, recommendationRate, recommendationYear, hasDoctorate, hasMaster, department, tags, famousUnion`

> `introduction` 以详情表为准（详情表更完整）。

**响应**：`R<UniversityDetailVO>`

### 4.3 任务 3 — 适应指南（6 个子接口）

所有 6 个接口均按 `{universityId}` 查询 `t_university_guides`，找不到或 status≠1 抛 `BusinessException(NOT_FOUND, "院校适应指南不存在")`。

| 子路径 | 权限 | 返回字段（直接透传 JSONB 为 `Map<String,Object>`） |
|---|---|---|
| `/overview` | `@RequireLogin` | `customTags` + 联 `t_universities` 的 `name, tags, region, category, nature, imageUrl` |
| `/survival` | `@RequireLogin` | `campusFacilities, dormitoryServices, campusTransportation` |
| `/academic` | `@RequirePro` | `academicGuidance, majorTransferGuidelines, majorTransferConstriction, academicSupportResources` |
| `/social` | `@RequireLogin` | `studentOrganizations, campusEvents, classDormSocial` |
| `/safety` | `@RequireLogin` | `financialAid, campusSecurity, healthServices` |
| `/life` | `@RequireLogin` | `lifeServices` |

> **需求笔误更正**：任务 3 接口 3 描述里出现了两次 `academic_guidance`。根据字段语义推断，应为 `academic_guidance + major_transfer_guidelines + major_transfer_constriction + academic_support_resources`，按此实现。

每个接口对应独立 VO（如 `UniversityGuideAcademicVO`），字段类型一律 `Map<String, Object>`，与 entity 中 JSONB 字段保持一致。`overview` 额外含 `List<String> customTags` 与 university 字段。

**响应**：`R<UniversityGuideXxxVO>`

### 4.4 任务 4 — 校园图册

```
GET /api/v1/app/university/{universityId}/gallery?page=1&size=10&imageType=校门
@RequireLogin
```

**Query 参数**

| 参数 | 类型 | 匹配方式 |
|---|---|---|
| page | int | 默认 1 |
| size | int | 默认 10，可选 10/20/30/50/100 |
| imageType | String（optional） | `=` |

**固定条件**：`university_id = {universityId}` AND `status = 1`
**排序**：`sort_order ASC, id DESC`

**`CampusGalleryListVO` 字段**：`imageType, imageUrl`

**响应**：`R<IPage<CampusGalleryListVO>>`

### 4.5 任务 6 — 录取专业组查询

#### 4.5.1 接口 1：分页查询录取专业组

```
GET /api/v1/app/university/admission-group/{universityId}
@RequireVip
```

**Query 参数（全部 optional，除分页外按需组合 AND）**

| 参数 | 类型 | 匹配方式 | 说明 |
|---|---|---|---|
| page | int | — | 默认 1 |
| size | int | — | 默认 10，可选 10/20/30/50/100 |
| province | String | `=` | 省份精准匹配，通过 `ProvinceEnum.isValid()` 校验 |
| batch | String | `=` | 批次精准匹配（如：本科批、提前批） |
| cityName | String | `LIKE %cityName%` | 城市名模糊查询 |

**固定条件**：
- `university_id = {universityId}`
- `is_deleted = FALSE`
- `year >= EXTRACT(YEAR FROM CURRENT_DATE) - 5`（近5年动态过滤）

**排序**：`year DESC, id ASC`

**`AdmissionGroupListVO` 字段**：
`id, groupCode, groupName, year, province, batch, cityName, subjects, requirementType, majorCount, admissionCount, minScore, minRank, maxScore, maxRank, avgScore, avgRank`

**响应**：`R<IPage<AdmissionGroupListVO>>`

#### 4.5.2 接口 2：查询录取专业明细

```
GET /api/v1/app/university/admission-group/{groupId}/scores
@RequireVip
```

**Path 参数**：`groupId`（Integer）

**固定条件**：`group_id = {groupId}` AND `is_deleted = FALSE`

**`AdmissionMajorScoreListVO` 字段**（全部字段）：
`id, groupId, majorCode, majorName, educationLevel, duration, tuition, description, admissionCount, minScore, minRank, maxScore, maxRank, avgScore, avgRank, constraints`

**响应**：`R<List<AdmissionMajorScoreListVO>>`

#### 4.5.3 接口 3：录取专业组详情

```
GET /api/v1/app/university/admission-group/{groupId}/detail
@RequireVip
```

复用 city/major 的 `/{id}/detail` 模式。

**Path 参数**：`groupId`（Integer）

**固定条件**：`id = {groupId}` AND `is_deleted = FALSE`

**`AdmissionGroupDetailVO` 字段**（全部字段）：
`id, universityId, universityName, cityName, year, province, batch, enrollmentCode, groupCode, groupName, subjects, requirementType, description, constraints, majorCount, categoryCount, admissionCount, minScore, minRank, maxScore, maxRank, avgScore, avgRank, createdAt, updatedAt`

**响应**：`R<AdmissionGroupDetailVO>`

## 五、约定与规范遵守

- **Controller**：类上 `@Validated + @RestController + @RequestMapping + @RequiredArgsConstructor`，入参 `@Valid`。与 `InstitutionController` 风格保持一致。
- **Service**：接口 + Impl 分离，Impl 加 `@Service + @Slf4j + @RequiredArgsConstructor`。
- **DTO**：分页 DTO 继承 `BasePageQueryDTO`，`@Data + @EqualsAndHashCode(callSuper = true)`。
- **VO**：`@Data + @Builder + @NoArgsConstructor + @AllArgsConstructor`；列表 VO 与详情 VO 严格分离。
- **MyBatis-Plus**：统一使用 `LambdaQueryWrapper`，禁用字符串拼接；分页用 `Page<Entity>` + `convert(this::toVO)`。
- **缓存**：本期不加 Redis 缓存（按用户决定），全部走数据库。
- **日志**：按 CLAUDE.md 约束，普通查询接口不打 info；仅在 NOT_FOUND 抛出前 `log.debug`。
- **异常**：统一 `BusinessException(ResultCode.NOT_FOUND, "...")`，由 `GlobalExceptionHandler` 兜底为 `R.fail`。
- **权限**：方法或类级 `@RequireLogin / @RequirePro`，由 common AOP 切面校验。
- **不写敏感日志**：本模块无密码/手机号/Token，无脱敏负担。

## 六、错误响应

| 场景 | 错误码 | msg |
|---|---|---|
| 院校不存在或下架 | 404 | 院校不存在 |
| 院校详情未配置 | 404 | 院校详情不存在 |
| 院校适应指南未配置 | 404 | 院校适应指南不存在 |
| 未登录访问需登录接口 | 401 | 未登录/Token过期（由权限切面） |
| 非 VIP 访问任务 6 接口 | 403 | 权限不足（需 VIP，由权限切面） |
| 普通用户访问 academic 接口 | 403 | 权限不足（需 Pro，由权限切面） |
| 省份参数不合法 | 400 | 省份参数不合法 |
| 录取专业组不存在 | 404 | 录取专业组不存在 |
| 参数校验失败 | 400 | 字段级 msg（由全局 handler） |

## 七、不做的事（YAGNI）

- 不加 Redis 缓存（待压测后决定）
- 不做模糊搜索 ES 索引（name LIKE 已够用）
- 不做收藏/点赞计数（属于用户管理模块）
- 不做院系详情/实验室/学科评估（独立子模块，单独迭代）
- 列表不实现复合排序参数（仅 sort_order + id）
- 任务 6 不做录取专业组的写操作（仅查询，属于 admin 端）

## 八、测试要点（供后续 TDD 参考）

- 列表：无参 / 单条件 / 多条件 AND / name 模糊 / 仅返回 status=1 / 分页边界
- 详情：正常 / 院校不存在 / 详情未配置 / 未登录
- 指南：每个分类的正常返回 / 找不到 / academic 普通用户 → 403 / academic Pro 用户 → 200
- 图册：正常 / imageType 筛选 / 仅当前院校 / 仅 status=1 / 未登录
