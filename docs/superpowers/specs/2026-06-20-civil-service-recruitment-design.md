``# 体制内招录模块设计文档

## 概述

为 haifeng-app 端实现体制内招录信息的查询展示，包含 4 类职位：公务员、事业编、部队文职、选调生。每类提供分页列表（无需登录）、详情（需登录）、备考指南（无需登录）、公告（无需登录）接口。

备考指南和公告为内容管理模块的子模块，通过 `guide_category` / `notice_category` 字段与各职位类型关联。

## 数据表

Flyway 迁移文件 `V23__civil_service__tables.sql` 已定义 4 张职位表：

| 表 | 实体 | 说明 |
|---|---|---|
| `t_civil_position` | CivilPosition | 公务员考试职位（国考/省考） |
| `t_institution_position` | InstitutionPosition | 事业编职位 |
| `t_military_position` | MilitaryPosition | 部队文职岗位 |
| `t_selection_position` | SelectionPosition | 选调生岗位 |

Flyway 迁移文件 `V22__content_management__tables.sql` 已定义 2 张内容管理表：

| 表 | 实体 | 说明 |
|---|---|---|
| `t_exam_guide` | ExamGuide | 统一备考指南（guide_category 区分：civil/institution/military/selection 等） |
| `t_notice` | Notice | 统一公告（notice_category 区分：civil/institution/military/selection 等） |

## 架构

遵循现有 `employment/jobIndex` 模式，每张表独立文件：

```
common (entity + mapper) ← app 依赖
app (dto + vo + service + impl + controller)
```

路径规则：`{模块}/{子模块}` = `employment/civilService` 或 `employment/contentManagement/{子模块}`

## 详细设计

### haifeng-common (10 文件)

**Content Management Entity:**
- `ExamGuide` — `t_exam_guide` 实体
- `Notice` — `t_notice` 实体
- `ExamGuideMapper` — 空接口继承 `BaseMapper<ExamGuide>`
- `NoticeMapper` — 空接口继承 `BaseMapper<Notice>`

**Entity** — 字段对应表结构（下划线转小驼峰）：
- `@TableName` 指定表名，`autoResultMap = true`
- `@TableId(type = IdType.ASSIGN_ID)` 雪花ID
- 审计字段：`isDeleted`, `createdAt`(`FieldFill.INSERT`), `updatedAt`(`FieldFill.INSERT_UPDATE`)
- 实现 `Serializable`

**Mapper** — 空接口继承 `BaseMapper<T>`，加 `@Mapper`

### haifeng-app (32 文件)

#### DTO (4 文件)

每个 DTO 继承 `BasePageQueryDTO`，包含：
- `keyword` — 模糊搜索，OR 查询所有模糊字段
- 各精确查询字段 — eq 匹配

| DTO | 模糊字段 (keyword OR) | 精确字段 (eq) | 判断查询 |
|---|---|---|---|---|
| CivilPositionSearchDTO | positionName, recruitingDept, workLocation | examType, positionCode, deptCode, minEducation, majorRequirement, degreeRequirement, politicalStatus, examCategory | — |
| InstitutionPositionSearchDTO | positionName, supervisingDept, institution, workLocation | province, examCategory, positionType, educationRequirement, degreeRequirement, positionStatus, specialPosition | ageLimit (ge) |
| MilitaryPositionSearchDTO | positionName, employerUnit, department | positionType, majorRequirement, educationRequirement, positionStatus, workLocation | — |
| SelectionPositionSearchDTO | positionName, targetUnit, workLocation | selectionType, year, province, majorRequirement, universityRequirement, educationRequirement, degreeRequirement, politicalStatus, positionStatus | ageLimit (ge) |

#### VO (8 文件)

每个实体 2 个 VO：
- `XxxListVO` — 只含列表展示字段，`@Data @Builder @NoArgsConstructor @AllArgsConstructor implements Serializable`
- `XxxDetailVO` — 含全部字段

字段清单按需求文档逐字段对应（详见任务 1-4）。

#### Service (4 Interface + 4 Impl)

每个接口 2 方法：
```java
IPage<XxxListVO> page(XxxSearchDTO dto);
XxxDetailVO detail(Long id);
```

实现类 pattern：
- `@Slf4j @Service @RequiredArgsConstructor`
- 注入对应 Mapper
- `page()`: 构建 `LambdaQueryWrapper` → `eq(isDeleted=false)` → `and(w -> w.like().or()...)` 处理 keyword → 各 `eq()` 精确字段 → `orderByDesc(createdAt)` → `mapper.selectPage(page, wrapper)` → `page.convert(...)`
- `detail()`: `selectById` → null 检查 → 抛 `BusinessException(NOT_FOUND)` → 返回 DetailVO

#### Content Management 模块 (app 端)

| 层 | 文件 | 路径规则 |
|---|---|---|
| DTO | ExamGuideQueryDTO / NoticeQueryDTO | `dto/employment/contentManagement/examGuide/` / `dto/employment/contentManagement/notice/` |
| VO | ExamGuideListVO / ExamGuideDetailVO / NoticeListVO / NoticeDetailVO | `vo/employment/contentManagement/examGuide/` / `vo/employment/contentManagement/notice/` |
| Service | ExamGuideService / NoticeService | `service/employment/contentManagement/examGuide/` / `service/employment/contentManagement/notice/` |
| Impl | ExamGuideServiceImpl / NoticeServiceImpl | `service/impl/employment/contentManagement/examGuide/` / `service/impl/employment/contentManagement/notice/` |
| Controller | ExamGuideController / NoticeController | `controller/employment/contentManagement/examGuide/` / `controller/employment/contentManagement/notice/` |

#### Controller (4 文件)

```java
@Validated @RestController @RequestMapping("/api/v1/app/employment/civil-service/{resource}") @RequiredArgsConstructor

@GetMapping("/list")              → 无登录
@RequireLogin @GetMapping("/{id}/detail") → 需登录
@GetMapping("/exam-guide")        → 无登录（返回备考指南，按 guide_category 过滤）
@GetMapping("/notice")            → 无登录（返回公告，按 notice_category 过滤）
```

| Controller | 路径 resource | guide_category | notice_category |
|---|---|---|---|
| CivilPositionController | `position` | `civil` | `civil` |
| InstitutionPositionController | `institution` | `institution` | `institution` |
| MilitaryPositionController | `military` | `military` | `military` |
| SelectionPositionController | `selection` | `selection` | `selection` |

## API 端点汇总

| 方法 | 路径 | 认证 | 说明 |
|---|---|---|---|
| GET | `/api/v1/app/employment/civil-service/position/list` | 否 | 公务员分页 |
| GET | `/api/v1/app/employment/civil-service/position/{id}/detail` | 是 | 公务员详情 |
| GET | `/api/v1/app/employment/civil-service/position/exam-guide` | 否 | 公务员备考指南 |
| GET | `/api/v1/app/employment/civil-service/position/notice` | 否 | 公务员考试公告 |
| GET | `/api/v1/app/employment/civil-service/institution/list` | 否 | 事业编分页 |
| GET | `/api/v1/app/employment/civil-service/institution/{id}/detail` | 是 | 事业编详情 |
| GET | `/api/v1/app/employment/civil-service/institution/exam-guide` | 否 | 事业编备考指南 |
| GET | `/api/v1/app/employment/civil-service/institution/notice` | 否 | 事业编公告 |
| GET | `/api/v1/app/employment/civil-service/military/list` | 否 | 部队文职分页 |
| GET | `/api/v1/app/employment/civil-service/military/{id}/detail` | 是 | 部队文职详情 |
| GET | `/api/v1/app/employment/civil-service/military/exam-guide` | 否 | 部队文职备考指南 |
| GET | `/api/v1/app/employment/civil-service/military/notice` | 否 | 部队文职公告 |
| GET | `/api/v1/app/employment/civil-service/selection/list` | 否 | 选调生分页 |
| GET | `/api/v1/app/employment/civil-service/selection/{id}/detail` | 是 | 选调生详情 |
| GET | `/api/v1/app/employment/civil-service/selection/exam-guide` | 否 | 选调生备考指南 |
| GET | `/api/v1/app/employment/civil-service/selection/notice` | 否 | 选调生公告 |

## 文件清单 (32 文件)

```
haifeng-common/
  entity/employment/civilService/
    CivilPosition.java
    InstitutionPosition.java
    MilitaryPosition.java
    SelectionPosition.java
  mapper/employment/civilService/
    CivilPositionMapper.java
    InstitutionPositionMapper.java
    MilitaryPositionMapper.java
    SelectionPositionMapper.java

haifeng-app/
  dto/employment/
    civilService/
      CivilPositionSearchDTO.java
      InstitutionPositionSearchDTO.java
      MilitaryPositionSearchDTO.java
      SelectionPositionSearchDTO.java
    contentManagement/
      examGuide/
        ExamGuideQueryDTO.java
      notice/
        NoticeQueryDTO.java
  vo/employment/
    civilService/
      CivilPositionListVO.java
      CivilPositionDetailVO.java
      InstitutionPositionListVO.java
      InstitutionPositionDetailVO.java
      MilitaryPositionListVO.java
      MilitaryPositionDetailVO.java
      SelectionPositionListVO.java
      SelectionPositionDetailVO.java
    contentManagement/
      examGuide/
        ExamGuideListVO.java
        ExamGuideDetailVO.java
      notice/
        NoticeListVO.java
        NoticeDetailVO.java
  service/employment/
    civilService/
      CivilPositionService.java
      InstitutionPositionService.java
      MilitaryPositionService.java
      SelectionPositionService.java
    contentManagement/
      examGuide/
        ExamGuideService.java
      notice/
        NoticeService.java
  service/impl/employment/
    civilService/
      CivilPositionServiceImpl.java
      InstitutionPositionServiceImpl.java
      MilitaryPositionServiceImpl.java
      SelectionPositionServiceImpl.java
    contentManagement/
      examGuide/
        ExamGuideServiceImpl.java
      notice/
        NoticeServiceImpl.java
  controller/employment/
    civilService/
      CivilPositionController.java
      InstitutionPositionController.java
      MilitaryPositionController.java
      SelectionPositionController.java
    contentManagement/
      examGuide/
        ExamGuideController.java
      notice/
        NoticeController.java
```
