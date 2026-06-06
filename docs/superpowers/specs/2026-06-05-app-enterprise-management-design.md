# C端企业管理设计文档

## 背景

需求来源：`haifeng-app/Need/13企业管理.md`。

后台企业管理已经实现。本次只实现 C 端展示与跳转配套接口，不实现后台 CRUD、导入、数据库迁移或表结构修改。

## 范围

本次实现 4 个 C 端接口：

1. 企业分页列表：公开访问。
2. 企业岗位列表：按单个企业 ID 查询，需登录。
3. 企业关联行业：按当前页企业 ID 批量查询中间表，需 Pro 及以上。
4. 行业关联企业：按当前页行业 ID 批量查询中间表，需 Pro 及以上。

不实现：

- 后台企业 CRUD / 导入。
- 岗位详情接口。
- 企业列表内嵌行业数据。
- 数据库表或 Flyway 迁移。
- 已有行业分页查询逻辑改造。

## 接口设计

### 1. 企业分页列表

```http
GET /api/v1/app/enterprise/list
```

权限：公开。

查询参数：

| 参数 | 说明 | 查询方式 |
|---|---|---|
| page | 页码 | 分页 |
| size | 每页数量 | 分页 |
| enterpriseName | 企业名称 | 模糊查询 |
| enterpriseNature | 企业性质 | 精准查询 |
| enterpriseType | 企业类型 | 精准查询 |
| cityName | 城市名称 | 精准查询 |
| recruitmentStatus | 招聘状态 | 精准查询 |

查询条件：

- 固定 `is_deleted = false`。
- 模糊和精准查询之间是 AND 关系。
- 排序使用 `id ASC`，对齐 C 端城市、行业列表的简单稳定排序风格。

返回：`IPage<EnterpriseListVO>`。

字段：

```java
Long id;
String cityName;
String enterpriseName;
String enterpriseNature;
String enterpriseType;
String logoUrl;
String officialWebsite;
String region;
String enterpriseScale;
String mainBusiness;
String enterpriseIntro;
```

### 2. 企业岗位列表

```http
GET /api/v1/app/enterprise/{enterpriseId}/positions
```

权限：`@RequireLogin`。

路径参数：

| 参数 | 说明 |
|---|---|
| enterpriseId | 企业 ID |

查询逻辑：

- 先校验企业存在且 `is_deleted = false`。
- 不存在时抛出 `BusinessException(ResultCode.NOT_FOUND, "企业不存在")`。
- 查询 `t_enterprise_position`：`enterprise_id = enterpriseId AND is_deleted = false`。
- 排序使用 `id ASC`。

返回：`List<EnterprisePositionVO>`。

字段：

```java
String positionName;
String recruitmentType;
String positionRequirement;
List<String> positionTags;
String province;
String city;
String workLocation;
String educationRequirement;
String majorRequirement;
String workExperience;
Integer salaryMin;
Integer salaryMax;
String applyLink;
OffsetDateTime deadline;
String positionStatus;
```

岗位接口不返回岗位 `id`、`enterpriseId`、审计字段，因为需求只要求展示字段。

### 3. 企业关联行业

```http
GET /api/v1/app/enterprise/industries?enterpriseIds=1,2,3
```

权限：`@RequirePro`。

查询参数：

| 参数 | 说明 |
|---|---|
| enterpriseIds | 企业 ID 列表，逗号分隔或框架支持的重复参数 |

查询逻辑：

- `enterpriseIds` 不能为空。
- 入参去重后查询，重复 ID 只返回一个分组。
- 查询 `t_enterprise_industry`：`enterprise_id IN (...)`。
- 不额外联查企业表或行业表，直接使用中间表冗余字段 `industry_id`、`industry_name`。
- 排序：`enterprise_id ASC, sort_order ASC, id ASC`。
- 返回时按请求 ID 的去重顺序生成分组；没有关联数据的企业返回空数组。

返回：`List<EnterpriseIndustryGroupVO>`。

```java
EnterpriseIndustryGroupVO {
    Long enterpriseId;
    List<IndustryJumpVO> industries;
}

IndustryJumpVO {
    Long industryId;
    String industryName;
}
```

示例：

```json
[
  {
    "enterpriseId": 1,
    "industries": [
      { "industryId": 10, "industryName": "人工智能" }
    ]
  },
  {
    "enterpriseId": 2,
    "industries": []
  }
]
```

### 4. 行业关联企业

```http
GET /api/v1/app/industry/enterprises?industryIds=10,11
```

权限：`@RequirePro`。

查询参数：

| 参数 | 说明 |
|---|---|
| industryIds | 行业 ID 列表，逗号分隔或框架支持的重复参数 |

查询逻辑：

- `industryIds` 不能为空。
- 入参去重后查询，重复 ID 只返回一个分组。
- 查询 `t_enterprise_industry`：`industry_id IN (...)`。
- 不额外联查企业表或行业表，直接使用中间表冗余字段 `enterprise_id`、`enterprise_name`。
- 排序：`industry_id ASC, sort_order ASC, id ASC`。
- 返回时按请求 ID 的去重顺序生成分组；没有关联数据的行业返回空数组。

返回：`List<IndustryEnterpriseGroupVO>`。

```java
IndustryEnterpriseGroupVO {
    Long industryId;
    List<EnterpriseJumpVO> enterprises;
}

EnterpriseJumpVO {
    Long enterpriseId;
    String enterpriseName;
}
```

示例：

```json
[
  {
    "industryId": 10,
    "enterprises": [
      { "enterpriseId": 1, "enterpriseName": "华为技术有限公司" }
    ]
  },
  {
    "industryId": 11,
    "enterprises": []
  }
]
```

## 代码结构

新增企业模块：

```text
haifeng-app/src/main/java/com/haifeng/app/
├── controller/company/EnterpriseController.java
├── dto/company/EnterpriseQueryDTO.java
├── service/company/EnterpriseService.java
├── service/impl/company/EnterpriseServiceImpl.java
└── vo/company/
    ├── EnterpriseListVO.java
    ├── EnterprisePositionVO.java
    ├── EnterpriseIndustryGroupVO.java
    ├── IndustryJumpVO.java
    ├── IndustryEnterpriseGroupVO.java
    └── EnterpriseJumpVO.java
```

扩展行业模块：

```text
haifeng-app/src/main/java/com/haifeng/app/
├── controller/industry/IndustryController.java
├── service/industry/IndustryService.java
└── service/impl/industry/IndustryServiceImpl.java
```

复用 common 层已存在实体和 Mapper：

```text
haifeng-common/src/main/java/com/haifeng/common/entity/company/
├── Enterprise.java
├── EnterprisePosition.java
└── EnterpriseIndustry.java

haifeng-common/src/main/java/com/haifeng/common/mapper/company/
├── EnterpriseMapper.java
├── EnterprisePositionMapper.java
└── EnterpriseIndustryMapper.java
```

## 数据流

### 企业分页

Controller 接收 `EnterpriseQueryDTO` → Service 组装 `LambdaQueryWrapper<Enterprise>` → `EnterpriseMapper.selectPage` → entity 转 `EnterpriseListVO` → 返回分页结果。

### 企业岗位

Controller 接收 `enterpriseId` → Service 查询企业并校验存在 → 查询岗位列表 → entity 转 `EnterprisePositionVO` → 返回列表。

### 企业关联行业

Controller 接收 `enterpriseIds` → Service 去重并查询 `EnterpriseIndustryMapper` → 按 `enterpriseId` 分组 → 转为 `EnterpriseIndustryGroupVO` → 返回列表。

### 行业关联企业

Controller 接收 `industryIds` → Service 去重并查询 `EnterpriseIndustryMapper` → 按 `industryId` 分组 → 转为 `IndustryEnterpriseGroupVO` → 返回列表。

## 权限

| 接口 | 权限 |
|---|---|
| `GET /api/v1/app/enterprise/list` | 公开 |
| `GET /api/v1/app/enterprise/{enterpriseId}/positions` | `@RequireLogin` |
| `GET /api/v1/app/enterprise/industries` | `@RequirePro` |
| `GET /api/v1/app/industry/enterprises` | `@RequirePro` |

`@RequirePro` 已表示 Pro 及以上，包含 VIP，不叠加 `@RequireLogin`。

## 错误处理

- 企业不存在：`BusinessException(ResultCode.NOT_FOUND, "企业不存在")`。
- `enterpriseIds` 或 `industryIds` 为空：`BusinessException(ResultCode.BAD_REQUEST, "企业ID列表不能为空")` 或 `BusinessException(ResultCode.BAD_REQUEST, "行业ID列表不能为空")`。
- 查询结果为空：不报错；分页返回空页，中间表分组返回空数组。

## 验证计划

编译验证：

```bash
mvn -pl haifeng-app -am compile -DskipTests
```

接口验证：

1. 企业分页公开访问：
   - `GET /api/v1/app/enterprise/list?page=1&size=10`
   - `GET /api/v1/app/enterprise/list?enterpriseName=科技&enterpriseNature=民企&cityName=深圳`
2. 企业岗位登录访问：
   - `GET /api/v1/app/enterprise/{enterpriseId}/positions`
3. 企业关联行业 Pro 访问：
   - `GET /api/v1/app/enterprise/industries?enterpriseIds=1,2,3`
4. 行业关联企业 Pro 访问：
   - `GET /api/v1/app/industry/enterprises?industryIds=10,11`

边界验证：

- 空 ID 列表返回参数错误。
- 重复 ID 去重。
- 无关联数据返回空数组。
- 非 Pro 访问中间表接口被拦截。
