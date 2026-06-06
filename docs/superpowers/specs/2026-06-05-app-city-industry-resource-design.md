# App端城市/行业/资源管理 — 设计文档

> 日期: 2026-06-05
> 模块: haifeng-app
> 对应需求: haifeng-app/Need/10城市_行业_资源管理.md

---

## 1. 概述

实现App端三个只读展示模块：城市管理、行业管理、资源管理。前端动态展示数据库中的数据，不涉及增删改操作。

### 核心原则

- **只读展示**: 所有接口均为查询接口，无增删改
- **JSONB透传**: 详情表中的 JSONB 字段直接返回给前端，由前端负责渲染
- **权限分层**: 列表接口公开访问，详情/URL接口需登录
- **并发安全**: 资源浏览计数使用原子更新

---

## 2. 数据库表

| 表名 | 用途 | 主从关系 |
|------|------|----------|
| t_city | 城市主表（列表展示） | 主表 |
| t_city_detail | 城市详情（一对一） | city_id → t_city.id |
| t_industry | 行业主表（列表展示） | 主表 |
| t_industry_detail | 行业详情（一对一） | industry_id → t_industry.id |
| t_resource | 资源表（列表+URL） | 独立表 |

Entity 和 Mapper 已存在于 haifeng-common，无需新建。

---

## 3. 城市模块

### 3.1 API 设计

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 城市列表 | GET | `/api/v1/app/city/list` | 公开 | 分页+模糊/精准查询 |
| 城市详情 | GET | `/api/v1/app/city/{id}/detail` | @RequireLogin | 关联t_city_detail展示详情 |

### 3.2 CityQueryDTO

继承 `BasePageQueryDTO`，新增字段：

| 字段 | 类型 | 查询方式 | 说明 |
|------|------|----------|------|
| cityName | String | LIKE | 城市名模糊查询 |
| province | String | EQ | 省份精准查询 |
| region | String | EQ | 地区精准查询 |

三个查询条件为并列关系（AND），非空时生效。

### 3.3 CityListVO

| 字段 | 类型 | 来源 |
|------|------|------|
| cityName | String | t_city.city_name |
| province | String | t_city.province |
| region | String | t_city.region |
| cityIntro | String | t_city.city_intro |
| collegeCount | Integer | t_city.college_count |
| keyCollegeCount | Integer | t_city.key_college_count |
| residentPopulation | BigDecimal | t_city.resident_population |
| gdp | BigDecimal | t_city.gdp |

### 3.4 CityDetailVO

| 字段 | 类型 | 来源 |
|------|------|------|
| cityName | String | t_city_detail.city_name |
| area | BigDecimal | t_city_detail.area |
| subtitle | String | t_city_detail.subtitle |
| cityLevel | String | t_city_detail.city_level |
| adminCode | String | t_city_detail.admin_code |
| perCapitaGdp | BigDecimal | t_city_detail.per_capita_gdp |
| urbanizationRate | BigDecimal | t_city_detail.urbanization_rate |
| ruralPopRatio | BigDecimal | t_city_detail.rural_pop_ratio |
| agingRate | BigDecimal | t_city_detail.aging_rate |
| migrantPopRatio | BigDecimal | t_city_detail.migrant_pop_ratio |
| gdpGrowthRate | BigDecimal | t_city_detail.gdp_growth_rate |
| fortune500Count | Integer | t_city_detail.fortune_500_count |
| industryStructure | Map\<String, Object\> | t_city_detail.industry_structure (JSONB) |
| industryDescription | String | t_city_detail.industry_description |
| mainIndustries | List\<String\> | t_city_detail.main_industries (JSONB) |
| emergingIndustries | List\<String\> | t_city_detail.emerging_industries (JSONB) |
| futurePlan | Map\<String, Object\> | t_city_detail.future_plan (JSONB) |
| highEducation | Map\<String, Object\> | t_city_detail.high_education (JSONB) |
| basicEducation | Map\<String, Object\> | t_city_detail.basic_education (JSONB) |
| enterpriseStats | Map\<String, Object\> | t_city_detail.enterprise_stats (JSONB) |
| housingPriceLevel | Map\<String, Object\> | t_city_detail.housing_price_level (JSONB) |
| rentalCost | Map\<String, Object\> | t_city_detail.rental_cost (JSONB) |
| housingPolicy | Map\<String, Object\> | t_city_detail.housing_policy (JSONB) |
| consumption | Map\<String, Object\> | t_city_detail.consumption (JSONB) |
| employment | Map\<String, Object\> | t_city_detail.employment (JSONB) |
| transportation | Map\<String, Object\> | t_city_detail.transportation (JSONB) |
| medical | Map\<String, Object\> | t_city_detail.medical (JSONB) |
| culture | Map\<String, Object\> | t_city_detail.culture (JSONB) |

### 3.5 查询逻辑

```java
LambdaQueryWrapper<City> wrapper = new LambdaQueryWrapper<City>()
    .eq(City::getIsDeleted, false)
    .like(StringUtils.isNotBlank(dto.getCityName()), City::getCityName, dto.getCityName())
    .eq(StringUtils.isNotBlank(dto.getProvince()), City::getProvince, dto.getProvince())
    .eq(StringUtils.isNotBlank(dto.getRegion()), City::getRegion, dto.getRegion())
    .orderByAsc(City::getId);
```

详情查询：先通过 `cityDetailMapper.findByCityId(id)` 获取 CityDetail，不存在则抛 `BusinessException(ResultCode.NOT_FOUND, "城市详情不存在")`。

---

## 4. 行业模块

### 4.1 API 设计

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 行业列表 | GET | `/api/v1/app/industry/list` | 公开 | 分页+精准category查询 |
| 行业详情 | GET | `/api/v1/app/industry/{id}/detail` | @RequireLogin | 关联t_industry_detail展示详情 |

### 4.2 IndustryQueryDTO

继承 `BasePageQueryDTO`，新增字段：

| 字段 | 类型 | 查询方式 | 说明 |
|------|------|----------|------|
| category | String | EQ | 行业分类精准匹配 |

### 4.3 IndustryListVO

| 字段 | 类型 | 来源 |
|------|------|------|
| industryName | String | t_industry.industry_name |
| category | String | t_industry.category |
| description | String | t_industry.description |
| annualGrowthRate | BigDecimal | t_industry.annual_growth_rate |
| marketScale | String | t_industry.market_scale |
| talentGap | String | t_industry.talent_gap |
| investmentHeat | BigDecimal | t_industry.investment_heat |

### 4.4 IndustryDetailVO

| 字段 | 类型 | 来源 |
|------|------|------|
| industryName | String | t_industry_detail.industry_name |
| shortDescription | String | t_industry_detail.short_description |
| detailedDescription | String | t_industry_detail.detailed_description |
| industryScale | Map\<String, Object\> | t_industry_detail.industry_scale (JSONB) |
| industryTalentDemand | Map\<String, Object\> | t_industry_detail.industry_talent_demand (JSONB) |
| industrySalary | Map\<String, Object\> | t_industry_detail.industry_salary (JSONB) |
| policyInfo | Map\<String, Object\> | t_industry_detail.policy_info (JSONB) |
| developmentSupportInfo | Map\<String, Object\> | t_industry_detail.development_support_info (JSONB) |
| talentAnalysis | Map\<String, Object\> | t_industry_detail.talent_analysis (JSONB) |
| talentPolicy | Map\<String, Object\> | t_industry_detail.talent_policy (JSONB) |
| salaryData | Map\<String, Object\> | t_industry_detail.salary_data (JSONB) |

### 4.5 查询逻辑

```java
LambdaQueryWrapper<Industry> wrapper = new LambdaQueryWrapper<Industry>()
    .eq(Industry::getIsDeleted, false)
    .eq(StringUtils.isNotBlank(dto.getCategory()), Industry::getCategory, dto.getCategory())
    .orderByAsc(Industry::getId);
```

详情查询：通过 `industryDetailMapper.findByIndustryId(id)` 获取 IndustryDetail，不存在则抛 NOT_FOUND。

---

## 5. 资源模块

### 5.1 API 设计

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 资源列表 | GET | `/api/v1/app/resource/list` | 公开 | 分页查询 |
| 查看资源URL | GET | `/api/v1/app/resource/{id}/url` | @RequireLogin | 返回URL+accessCode，同步view_count+1 |

### 5.2 ResourceQueryDTO

继承 `BasePageQueryDTO`，新增字段：

| 字段 | 类型 | 查询方式 | 说明 |
|------|------|----------|------|
| category | String | EQ | 资源分类精准匹配 |

### 5.3 ResourceListVO

| 字段 | 类型 | 来源 |
|------|------|------|
| resourceName | String | t_resource.resource_name |
| coverUrl | String | t_resource.cover_url |
| description | String | t_resource.description |
| category | String | t_resource.category |
| fileType | String | t_resource.file_type |
| viewCount | Integer | t_resource.view_count |

### 5.4 ResourceUrlVO

| 字段 | 类型 | 来源 |
|------|------|------|
| resourceUrl | String | t_resource.resource_url |
| accessCode | String | t_resource.access_code |

### 5.5 查询与计数逻辑

**列表查询**:
```java
LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<Resource>()
    .eq(Resource::getIsDeleted, false)
    .eq(StringUtils.isNotBlank(dto.getCategory()), Resource::getCategory, dto.getCategory())
    .orderByAsc(Resource::getSortOrder)
    .orderByDesc(Resource::getCreatedAt);
```

**查看URL + 计数**:
1. 根据 id 查询 Resource，不存在或已删除则抛 NOT_FOUND
2. 原子更新 view_count: `UPDATE t_resource SET view_count = view_count + 1 WHERE id = #{id} AND is_deleted = false`
3. 返回 ResourceUrlVO

计数更新在 ResourceMapper 中添加 `@Update` 方法:
```java
@Update("UPDATE t_resource SET view_count = view_count + 1 WHERE id = #{id} AND is_deleted = false")
int incrementViewCount(@Param("id") Long id);
```

---

## 6. 文件清单

### 新建文件 (haifeng-app)

| 文件 | 路径 |
|------|------|
| CityController | controller/city/CityController.java |
| CityService | service/city/CityService.java |
| CityServiceImpl | service/impl/city/CityServiceImpl.java |
| CityQueryDTO | dto/city/CityQueryDTO.java |
| CityListVO | vo/city/CityListVO.java |
| CityDetailVO | vo/city/CityDetailVO.java |
| IndustryController | controller/industry/IndustryController.java |
| IndustryService | service/industry/IndustryService.java |
| IndustryServiceImpl | service/impl/industry/IndustryServiceImpl.java |
| IndustryQueryDTO | dto/industry/IndustryQueryDTO.java |
| IndustryListVO | vo/industry/IndustryListVO.java |
| IndustryDetailVO | vo/industry/IndustryDetailVO.java |
| ResourceController | controller/resource/ResourceController.java |
| ResourceService | service/resource/ResourceService.java |
| ResourceServiceImpl | service/impl/resource/ResourceServiceImpl.java |
| ResourceQueryDTO | dto/resource/ResourceQueryDTO.java |
| ResourceListVO | vo/resource/ResourceListVO.java |
| ResourceUrlVO | vo/resource/ResourceUrlVO.java |

### 修改文件 (haifeng-common)

| 文件 | 修改内容 |
|------|----------|
| ResourceMapper | 添加 incrementViewCount 方法 |

### 已存在文件（无需修改）

- City.java, CityDetail.java, Industry.java, IndustryDetail.java, Resource.java (entities)
- CityMapper.java, CityDetailMapper.java, IndustryMapper.java, IndustryDetailMapper.java (mappers)

---

## 7. 错误处理

| 场景 | 处理 |
|------|------|
| 城市详情不存在 | `BusinessException(ResultCode.NOT_FOUND, "城市详情不存在")` |
| 行业详情不存在 | `BusinessException(ResultCode.NOT_FOUND, "行业详情不存在")` |
| 资源不存在或已删除 | `BusinessException(ResultCode.NOT_FOUND, "资源不存在")` |
| 未登录访问需登录接口 | 由 `@RequireLogin` + `AuthAspect` 自动拦截，返回 401 |
