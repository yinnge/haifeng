# 城市、行业、资源管理模块设计规范

## 概述

本模块为管理后台提供城市、行业、资源的管理功能，包括CRUD操作和xlsx批量导入。

### 需求来源
- 需求文档：`Need/8城市行业管理.md`
- 数据表定义：`db/migration/table/DataTable8.md`

### 实现范围

| 模块 | CRUD | xlsx导入 | 备注 |
|------|------|----------|------|
| 城市管理 | ✅ | ✅ 2个接口 | 主表+详情表(14个JSONB) |
| 行业管理 | ✅ | ✅ 2个接口 | 主表+详情表(8个JSONB) |
| 资源管理 | ✅ | ❌ | 简单CRUD |
| 企业行业关联 | ❌ | ❌ | 本次不实现 |

---

## 一、数据库设计

### 1.1 Flyway迁移文件

文件：`V8__create_cities_industries.sql`

创建5个表：
- `t_city` - 城市主表
- `t_city_detail` - 城市详情表（1对1）
- `t_industry` - 行业主表
- `t_industry_detail` - 行业详情表（1对1）
- `t_resource` - 资源表

### 1.2 关键设计点

| 设计点 | 说明 |
|--------|------|
| 主键 | 雪花算法生成BIGINT，不用SERIAL自增 |
| 外键 | 代码层面约束，不建物理外键 |
| 软删除 | `is_deleted` BOOLEAN字段 |
| 唯一约束 | city_name、industry_name 唯一 |
| JSONB字段 | 详情表存储复杂嵌套数据 |

---

## 二、城市管理模块

### 2.1 包结构

```
com.haifeng.admin/
├── controller/city/CityController.java
├── service/city/CityService.java
├── service/impl/city/CityServiceImpl.java
├── dto/city/
│   ├── CityQueryDTO.java
│   ├── CityAddDTO.java
│   ├── CityUpdateDTO.java
│   └── CityDetailUpdateDTO.java
├── vo/city/
│   ├── CityListVO.java
│   └── CityDetailVO.java
└── excel/city/
    ├── CityExcelDTO.java
    ├── CityDetailExcelDTO.java
    └── [14个JSONB ExcelDTO]

com.haifeng.common/
├── entity/city/City.java
├── entity/city/CityDetail.java
├── mapper/city/CityMapper.java
└── mapper/city/CityDetailMapper.java
```

### 2.2 API接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/admin/city/list | 分页列表 |
| GET | /api/v1/admin/city/{id} | 详情（主表+详情表） |
| POST | /api/v1/admin/city | 新增（事务：主表+详情） |
| PUT | /api/v1/admin/city/{id} | 修改主表 |
| PUT | /api/v1/admin/city/{id}/detail | 修改详情表 |
| PUT | /api/v1/admin/city/{id}/status | 禁用/启用 |
| DELETE | /api/v1/admin/city/{id} | 硬删除 |
| DELETE | /api/v1/admin/city/batch | 批量硬删除 |
| POST | /api/v1/admin/city/import | 导入主表xlsx |
| POST | /api/v1/admin/city/import-detail | 导入详情xlsx |

### 2.3 列表展示字段

`CityListVO`: city_name, province, college_count, key_college_count, resident_population, is_deleted

### 2.4 模糊查询字段

city_name, province, region, is_deleted

### 2.5 xlsx导入设计

#### 主表xlsx (1个Sheet)

| 字段 | Excel表头 | 类型 |
|------|-----------|------|
| cityName | 城市名称 | String |
| province | 省份 | String |
| region | 所属地区 | String |
| cityIntro | 城市简介 | String |
| collegeCount | 高校数量 | Integer |
| keyCollegeCount | 重点高校数量 | Integer |
| residentPopulation | 常住人口(万人) | BigDecimal |
| gdp | GDP(亿元) | BigDecimal |

#### 详情xlsx (15个Sheet)

**Sheet0: 详情基础字段**

| 字段 | Excel表头 |
|------|-----------|
| cityName | 城市名称 |
| area | 面积(平方公里) |
| subtitle | 副标题 |
| cityLevel | 城市级别 |
| adminCode | 行政区划代码 |
| perCapitaGdp | 人均GDP(万元) |
| urbanizationRate | 城镇化率(%) |
| ruralPopRatio | 农村人口比例(%) |
| agingRate | 老龄化率(%) |
| migrantPopRatio | 外来人口比例(%) |
| gdpGrowthRate | GDP增长率(%) |
| fortune500Count | 世界500强企业数量 |
| industryDescription | 产业描述 |
| mainIndustries | 主要产业(逗号分隔) |
| emergingIndustries | 新兴产业(逗号分隔) |

**Sheet1: industry_structure (产业结构)**

| 字段 | Excel表头 |
|------|-----------|
| cityName | 城市名称 |
| primaryRatio | 第一产业占比(%) |
| secondaryRatio | 第二产业占比(%) |
| tertiaryRatio | 第三产业占比(%) |

**Sheet2: housing_price_level (房价水平)**

| 字段 | Excel表头 |
|------|-----------|
| cityName | 城市名称 |
| avgPrice | 平均房价(万元/㎡) |
| coreAreaPrice | 核心区房价(万元/㎡) |
| suburbanPriceRange | 郊区房价范围(万元/㎡) |
| priceGrowthRate | 房价涨幅(%) |
| priceIncomeRatio | 房价收入比(%) |

**Sheet3: high_education (高等教育资源)**

| 字段 | Excel表头 |
|------|-----------|
| cityName | 城市名称 |
| totalColleges | 高校总数 |
| doubleFirstClassCount | 双一流高校数量 |
| undergraduateCount | 在校生数量(万) |
| graduateCount | 研究生数量(万) |

**Sheet4: basic_education (基础教育资源)**

| 字段 | Excel表头 |
|------|-----------|
| cityName | 城市名称 |
| totalSchools | 学校总数 |
| modelSchoolCount | 示范学校数量 |
| keySchoolCount | 重点学校数量 |
| educationNote | 教育备注 |

**Sheet5: transportation (交通数据)**

| 字段 | Excel表头 |
|------|-----------|
| cityName | 城市名称 |
| metroLines | 地铁线路(条) |
| metroMileage | 地铁里程(公里) |
| highwayMileage | 高速公路里程(公里) |
| trafficWorldRank | 交通世界排名 |

**Sheet6: employment (就业数据)**

| 字段 | Excel表头 |
|------|-----------|
| cityName | 城市名称 |
| unemploymentRate | 城市失业率(%) |
| nationalUnemploymentRate | 全国平均失业率(%) |
| tertiaryEmploymentRatio | 第三产业就业占比(%) |
| newEmployment | 新增就业(万人) |
| avgSalary | 平均工资(万元/年) |
| salaryRank | 工资排名(全国) |
| skilledTalentRatio | 技能人才占比(%) |
| skilledTalentGrowth | 技能人才增长(%) |

**Sheet7: enterprise_stats (企业统计)**

| 字段 | Excel表头 |
|------|-----------|
| cityName | 城市名称 |
| enterpriseCategories | 企业类别数 |
| keyEnterpriseCount | 重点企业总数 |
| fortune500Count | 世界500强企业数量 |

**Sheet8: future_plan (未来规划)**

| 字段 | Excel表头 |
|------|-----------|
| cityName | 城市名称 |
| targetYear | 目标年份 |
| developmentGoal | 发展目标 |
| keyAreas | 重点领域(逗号分隔) |

**Sheet9: culture (文化旅游数据)**

| 字段 | Excel表头 |
|------|-----------|
| cityName | 城市名称 |
| worldHeritageCount | 世界遗产数量(项) |
| annualTourists | 年游客量(万人次) |
| aScenicCount | A级景区数量(家) |
| coreAttractions | 核心景点(逗号分隔) |

**Sheet10: consumption (消费数据)**

| 字段 | Excel表头 |
|------|-----------|
| cityName | 城市名称 |
| perCapitaConsumption | 人均消费(万元/年) |
| consumptionGrowthRate | 消费涨幅(%) |
| engelCoefficient | 恩格尔系数(%) |
| educationExpenseRatio | 教育支出占比(%) |
| consumptionIndex | 消费指数 |
| consumptionRank | 消费排名(全国) |

**Sheet11: medical (医疗数据)**

| 字段 | Excel表头 |
|------|-----------|
| cityName | 城市名称 |
| topHospitalCount | 三甲医院数量(所) |
| tertiaryHospitalCount | 三级医院总数(所) |
| doctorDensity | 医生密度(人/千人) |
| medicalRank | 医疗排名(全国) |

**Sheet12: housing_policy (住房政策)**

| 字段 | Excel表头 |
|------|-----------|
| cityName | 城市名称 |
| purchaseRestriction | 限购政策 |
| sharedPropertyHousing | 共有产权房(万套) |
| publicRentalHousing | 公租房(万套) |
| firstHomeRate | 首套房利率(%) |
| secondHomeRate | 二套房利率(%) |

**Sheet13: rental_cost (租房成本)**

| 字段 | Excel表头 |
|------|-----------|
| cityName | 城市名称 |
| downtownRentRange | 市中心租金范围(元/月) |
| suburbanRentRange | 郊区租金范围(元/月) |
| rentIncomeRatio | 租金收入比(%) |
| rentGrowthRate | 租金涨幅(%) |

---

## 三、行业管理模块

### 3.1 包结构

```
com.haifeng.admin/
├── controller/industry/IndustryController.java
├── service/industry/IndustryService.java
├── service/impl/industry/IndustryServiceImpl.java
├── dto/industry/
│   ├── IndustryQueryDTO.java
│   ├── IndustryAddDTO.java
│   ├── IndustryUpdateDTO.java
│   └── IndustryDetailUpdateDTO.java
├── vo/industry/
│   ├── IndustryListVO.java
│   └── IndustryDetailVO.java
└── excel/industry/
    ├── IndustryExcelDTO.java
    ├── IndustryDetailExcelDTO.java
    └── [8个JSONB ExcelDTO]

com.haifeng.common/
├── entity/industry/Industry.java
├── entity/industry/IndustryDetail.java
├── mapper/industry/IndustryMapper.java
└── mapper/industry/IndustryDetailMapper.java
```

### 3.2 API接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/admin/industry/list | 分页列表 |
| GET | /api/v1/admin/industry/{id} | 详情 |
| POST | /api/v1/admin/industry | 新增 |
| PUT | /api/v1/admin/industry/{id} | 修改主表 |
| PUT | /api/v1/admin/industry/{id}/detail | 修改详情表 |
| PUT | /api/v1/admin/industry/{id}/status | 禁用/启用 |
| DELETE | /api/v1/admin/industry/{id} | 硬删除 |
| DELETE | /api/v1/admin/industry/batch | 批量硬删除 |
| POST | /api/v1/admin/industry/import | 导入主表xlsx |
| POST | /api/v1/admin/industry/import-detail | 导入详情xlsx |

### 3.3 列表展示字段

`IndustryListVO`: industry_name, category, talent_trend, annual_growth_rate, is_deleted

### 3.4 模糊查询字段

industry_name, category, talent_trend, is_deleted

### 3.5 xlsx导入设计

#### 主表xlsx (1个Sheet)

| 字段 | Excel表头 |
|------|-----------|
| industryName | 行业名称 |
| category | 行业分类 |
| iconClass | 图标类名 |
| description | 行业描述 |
| annualGrowthRate | 年增长率(%) |
| marketScale | 市场规模 |
| talentGap | 人才缺口 |
| investmentHeat | 投资热度(%) |
| growthTrend | 增长趋势 |
| marketTrend | 市场趋势 |
| talentTrend | 人才趋势 |
| investmentTrend | 投资趋势 |

#### 详情xlsx (9个Sheet)

**Sheet0: 详情基础字段**

| 字段 | Excel表头 |
|------|-----------|
| industryName | 行业名称 |
| shortDescription | 简短描述 |
| detailedDescription | 详细描述 |

**Sheet1: industry_scale (发展规模)**

| 字段 | Excel表头 |
|------|-----------|
| industryName | 行业名称 |
| scaleValue | 发展规模(万亿元) |
| scaleLabel | 发展规模标签 |
| scaleDescriptions | 发展规模描述(逗号分隔) |

**Sheet2: industry_talent_demand (人才需求)**

| 字段 | Excel表头 |
|------|-----------|
| industryName | 行业名称 |
| demandValue | 人才需求(万人) |
| demandLabel | 人才需求标签 |
| demandDescriptions | 人才需求描述(逗号分隔) |

**Sheet3: industry_salary (行业薪资)**

| 字段 | Excel表头 |
|------|-----------|
| industryName | 行业名称 |
| salaryRange | 薪资范围(万元) |
| salaryLabel | 薪资标签 |
| salaryDescriptions | 薪资描述(逗号分隔) |

**Sheet4: policy_info (政策信息)**

| 字段 | Excel表头 |
|------|-----------|
| industryName | 行业名称 |
| policyOverview | 政策概览 |
| nationalPolicies | 国家政策(逗号分隔) |
| policyHighlights | 政策亮点 |

**Sheet5: development_support_info (发展地域支持)**

| 字段 | Excel表头 |
|------|-----------|
| industryName | 行业名称 |
| regionalOverview | 地域发展概述 |
| keyCities | 重点城市(逗号分隔) |
| cityPolicies | 城市政策(逗号分隔) |

**Sheet6: talent_analysis (人才需求分析)**

| 字段 | Excel表头 |
|------|-----------|
| industryName | 行业名称 |
| analysisTitle | 分析标题 |
| shortagePositions | 紧缺岗位(逗号分隔) |
| educationRequirement | 学历要求 |
| majorRequirement | 专业要求 |
| talentTrendDescription | 人才趋势描述 |

**Sheet7: talent_policy (人才引进政策)**

| 字段 | Excel表头 |
|------|-----------|
| industryName | 行业名称 |
| policyTitle | 政策标题 |
| nationalPolicies | 国家级政策(逗号分隔) |
| localPolicies | 地方级政策(逗号分隔) |
| enterpriseDescription | 企业层面描述 |

**Sheet8: salary_data (薪资数据)**

| 字段 | Excel表头 |
|------|-----------|
| industryName | 行业名称 |
| salaryAnalysisTitle | 薪资分析标题 |
| salaryAnalysisDescription | 薪资分析描述 |
| regionalSalaryTitle | 地域薪资差异标题 |
| regionalSalaryDescription | 地域薪资差异描述 |
| salaryTrendAnalysis | 薪资趋势分析 |

---

## 四、资源管理模块

### 4.1 包结构

```
com.haifeng.admin/
├── controller/resource/ResourceController.java
├── service/resource/ResourceService.java
├── service/impl/resource/ResourceServiceImpl.java
├── dto/resource/
│   ├── ResourceQueryDTO.java
│   ├── ResourceAddDTO.java
│   └── ResourceUpdateDTO.java
└── vo/resource/
    ├── ResourceListVO.java
    └── ResourceDetailVO.java

com.haifeng.common/
├── entity/resource/Resource.java
└── mapper/resource/ResourceMapper.java
```

### 4.2 API接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/admin/resource/list | 分页列表 |
| GET | /api/v1/admin/resource/{id} | 详情 |
| POST | /api/v1/admin/resource | 新增 |
| PUT | /api/v1/admin/resource/{id} | 修改 |
| PUT | /api/v1/admin/resource/{id}/status | 禁用/启用 |
| DELETE | /api/v1/admin/resource/{id} | 硬删除 |
| DELETE | /api/v1/admin/resource/batch | 批量硬删除 |

### 4.3 列表展示字段

`ResourceListVO`: resource_name, category, file_type, view_count, sort_order, is_deleted

### 4.4 模糊查询字段

resource_name, category, is_deleted

---

## 五、xlsx导入通用逻辑

### 5.1 校验流程

```
1. 主表导入:
   ├── 检查表头是否规范
   ├── 检查unique字段(城市名称/行业名称)是否重复
   ├── 逐行校验必填字段
   └── 收集错误信息，有错误则抛出异常回滚

2. 详情表导入:
   ├── 检查表头是否规范
   ├── 通过名称字段查找主表ID (城市名称→city_id)
   ├── 校验外键是否存在
   ├── 1对1关系校验：检查是否已有详情记录
   └── 收集错误信息，有错误则抛出异常回滚
```

### 5.2 错误处理

- 格式：`第{行号}行：{字段名}{错误描述}`
- 示例：`第5行：城市名称'北京市'已存在`
- 多个错误用分号连接后抛出 BusinessException

### 5.3 事务保证

- Service方法使用 `@Transactional(rollbackFor = Exception.class)`
- 任何校验失败都抛出异常，自动回滚

---

## 六、统一规范

### 6.1 删除机制

| 操作 | HTTP方法 | 路径 | 说明 |
|------|----------|------|------|
| 硬删除 | DELETE | `/{id}` | 物理删除，不可恢复 |
| 批量硬删除 | DELETE | `/batch` | 物理删除多条 |
| 禁用/启用 | PUT | `/{id}/status` | is_deleted切换 |

### 6.2 分页参数

继承 `BasePageQueryDTO`，支持：10, 20, 30, 50, 100, 200, 500, 1000

### 6.3 TEXT[]字段处理

使用 `StringArrayConverter`，支持中英文逗号分隔
