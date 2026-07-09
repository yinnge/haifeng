# 城市、行业、资源管理模块实施报告

## 模块概述

城市、行业、资源管理模块为管理员提供城市信息、行业数据、学习资源的增删改查及批量导入功能，用于支撑高考志愿规划的地域与行业分析。

### 功能清单
| 子模块 | 功能 |
|--------|------|
| 城市管理 | 城市列表、详情、新增、修改、修改详情、禁用/启用（状态切换）、硬删除、批量删除、xlsx导入（主表+详情多Sheet） |
| 行业管理 | 行业列表、详情、新增、修改、修改详情、禁用/启用（状态切换）、硬删除、批量删除、xlsx导入（主表+详情9个Sheet） |
| 资源管理 | 资源列表、详情、新增、修改、禁用/启用（状态切换）、硬删除、批量删除 |

### 删除机制说明
| 操作 | HTTP方法 | 路径 | 说明 |
|------|----------|------|------|
| 硬删除 | DELETE | `/{id}` | 物理删除记录，数据不可恢复（已软删除的记录不可再硬删除） |
| 批量硬删除 | POST | `/batch/delete` | 批量物理删除记录（请求体传ids） |
| 禁用/启用 | PUT | `/{id}/status` | isDeleted=true禁用，isDeleted=false启用（软删除可恢复） |
| 详情 | GET | `/{id}` | 查看详情（已软删除的记录不可查看） |

前端列表每行应显示：删除（硬删除）、禁用/启用、详情按钮

---

## API 接口文档

### 管理端接口 (端口: 8081)

---

## 一、城市管理接口

路由前缀：`/api/v1/admin/city`

---

### 1.1 分页查询城市列表
```
GET /api/v1/admin/city/list
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| cityName | String | 否 | 城市名称模糊查询 |
| province | String | 否 | 省份模糊查询 |
| region | String | 否 | 所属地区模糊查询 |
| isDeleted | Boolean | 否 | 删除状态筛选，不传默认只查未删除的（isDeleted=false） |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1234567890123456789,
        "cityName": "北京",
        "province": "北京市",
        "collegeCount": 92,
        "keyCollegeCount": 26,
        "residentPopulation": 2189.30,
        "isDeleted": false,
        "createdAt": "2026-05-08T10:30:00"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1
  },
  "timestamp": 1714300000000
}
```

---

### 1.2 获取城市详情
```
GET /api/v1/admin/city/{id}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 城市ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1234567890123456789,
    "cityName": "北京",
    "province": "北京市",
    "region": "华北",
    "cityIntro": "中国首都，政治、文化、国际交往中心...",
    "collegeCount": 92,
    "keyCollegeCount": 26,
    "residentPopulation": 2189.30,
    "gdp": 41610.90,
    "isDeleted": false,
    "createdAt": "2026-05-08T10:30:00",
    "updatedAt": "2026-05-08T10:30:00",
    "detailId": 1234567890123456790,
    "area": 16410.54,
    "subtitle": "中国首都",
    "cityLevel": "直辖市",
    "adminCode": "110000",
    "perCapitaGdp": 19.01,
    "urbanizationRate": 87.50,
    "ruralPopRatio": 12.50,``
    "agingRate": 21.30,
    "migrantPopRatio": 38.50,
    "gdpGrowthRate": 5.20,
    "fortune500Count": 56,
    "industryStructure": {"first": 0.3, "second": 16.2, "third": 83.5},
    "industryDescription": "以第三产业为主导...",
    "mainIndustries": ["金融", "信息技术", "科技研发"],
    "emergingIndustries": ["人工智能", "新能源", "生物医药"],
    "futurePlan": {"focus": ["数字经济", "高精尖产业"]},
    "highEducation": {"universities": 92, "keyUniversities": 26},
    "basicEducation": {"primarySchools": 1000, "middleSchools": 600},
    "enterpriseStats": {"total": 200000, "fortune500": 56},
    "housingPriceLevel": {"avgPrice": 65000, "trend": "稳定"},
    "rentalCost": {"oneRoom": 4000, "twoRoom": 6000},
    "housingPolicy": {"providentFund": true, "subsidies": []},
    "consumption": {"avgMonthly": 5000},
    "employment": {"avgSalary": 13000, "unemploymentRate": 3.2},
    "transportation": {"subway": true, "lines": 27},
    "medical": {"hospitals": 800, "topHospitals": 50},
    "culture": {"museums": 200, "attractions": 100}
  },
  "timestamp": 1714300000000
}
```

---

### 1.3 新增城市
```
POST /api/v1/admin/city
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| cityName | String | 是 | 城市名称（最长50字符） |
| province | String | 是 | 省份（最长50字符） |
| region | String | 是 | 所属地区（最长50字符） |
| cityIntro | String | 否 | 城市简介 |
| collegeCount | Integer | 否 | 高校数量 |
| keyCollegeCount | Integer | 否 | 重点高校数量 |
| residentPopulation | BigDecimal | 否 | 常住人口（万人） |
| gdp | BigDecimal | 否 | GDP（亿元） |
| area | BigDecimal | 否 | 城市面积（平方公里） |
| subtitle | String | 否 | 城市副标题 |
| cityLevel | String | 否 | 城市等级（直辖市/省会城市/地级市/县级市） |
| adminCode | String | 否 | 行政区划代码 |
| perCapitaGdp | BigDecimal | 否 | 人均GDP（万元） |
| urbanizationRate | BigDecimal | 否 | 城镇化率（%） |
| ruralPopRatio | BigDecimal | 否 | 农村人口占比（%） |
| agingRate | BigDecimal | 否 | 老龄化率（%） |
| migrantPopRatio | BigDecimal | 否 | 外来人口占比（%） |
| gdpGrowthRate | BigDecimal | 否 | GDP增长率（%） |
| fortune500Count | Integer | 否 | 世界500强企业数量 |
| industryStructure | Map | 否 | 产业结构（JSONB） |
| industryDescription | String | 否 | 产业描述 |
| mainIndustries | List<String> | 否 | 主导产业列表 |
| emergingIndustries | List<String> | 否 | 新兴产业列表 |
| futurePlan | Map | 否 | 未来规划（JSONB） |
| highEducation | Map | 否 | 高等教育情况（JSONB） |
| basicEducation | Map | 否 | 基础教育情况（JSONB） |
| enterpriseStats | Map | 否 | 企业统计（JSONB） |
| housingPriceLevel | Map | 否 | 房价水平（JSONB） |
| rentalCost | Map | 否 | 租房成本（JSONB） |
| housingPolicy | Map | 否 | 住房政策（JSONB） |
| consumption | Map | 否 | 消费水平（JSONB） |
| employment | Map | 否 | 就业情况（JSONB） |
| transportation | Map | 否 | 交通情况（JSONB） |
| medical | Map | 否 | 医疗资源（JSONB） |
| culture | Map | 否 | 文化娱乐（JSONB） |

**请求示例：**
```json
{
  "cityName": "北京",
  "province": "北京市",
  "region": "华北",
  "cityIntro": "中国首都，政治、文化、国际交往中心...",
  "collegeCount": 92,
  "keyCollegeCount": 26,
  "residentPopulation": 2189.30,
  "gdp": 41610.90,
  "cityLevel": "直辖市",
  "mainIndustries": ["金融", "信息技术", "科技研发"]
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": 1234567890123456789,
  "timestamp": 1714300000000
}
```

**操作日志：** 此接口自动记录操作日志

---

### 1.4 修改城市主表信息
```
PUT /api/v1/admin/city/{id}
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 城市ID |

**请求参数：** 同新增（除cityName外均可选），不包含 isDeleted 字段。修改禁用/启用状态请使用状态切换接口（1.6）。

**业务说明：** 此接口不允许修改删除状态，仅可修改城市基础信息。如需禁用/启用城市，请使用 `PUT /{id}/status` 接口。

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

**操作日志：** 此接口自动记录操作日志

---

### 1.5 修改城市详情表信息
```
PUT /api/v1/admin/city/{id}/detail
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 城市ID |

**请求参数：** 详情表所有JSONB字段，不包含 isDeleted 字段。修改禁用/启用状态请使用状态切换接口（1.6）。

**业务说明：** 此接口不允许修改删除状态，仅可修改详情信息。如需禁用/启用城市，请使用 `PUT /{id}/status` 接口。

**操作日志：** 此接口自动记录操作日志

---

### 1.6 修改城市状态
```
PUT /api/v1/admin/city/{id}/status
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 城市ID |

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| isDeleted | Boolean | 是 | 状态: true-禁用 false-启用 |

**请求示例：**
```json
{
  "isDeleted": true
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

**操作日志：** 此接口自动记录操作日志

---

### 1.7 硬删除城市
```
DELETE /api/v1/admin/city/{id}
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 城市ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

**业务说明：** 物理删除主表+详情表记录，数据不可恢复。仅未软删除的城市可执行硬删除，已软删除的城市请通过状态切换接口（1.6）恢复。如需保留数据，请使用状态切换接口（1.6）。

**操作日志：** 此接口自动记录操作日志

---

### 1.8 批量硬删除城市
```
POST /api/v1/admin/city/batch/delete
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | List&lt;Long&gt; | 是 | 城市ID列表，单次最多200条 |

```json
[1234567890123456789, 1234567890123456790]
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

**操作日志：** 此接口自动记录操作日志

---

### 1.9 导入城市主表xlsx
```
POST /api/v1/admin/city/import
Content-Type: multipart/form-data
Authorization: Bearer {accessToken}
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | MultipartFile | 是 | xlsx文件 |

**xlsx表头格式：**
| 列名 | 说明 |
|------|------|
| cityName | 城市名称（必填） |
| province | 省份（必填） |
| region | 所属地区 |
| cityIntro | 城市简介 |
| collegeCount | 高校数量 |
| keyCollegeCount | 重点高校数量 |
| residentPopulation | 常住人口（万人） |
| gdp | GDP（亿元） |

**业务说明：**
- 仅支持 .xlsx 格式文件
- 文件不能为空

**操作日志：** 此接口自动记录操作日志

---

### 1.10 导入城市详情xlsx（多Sheet）
```
POST /api/v1/admin/city/import-detail
Content-Type: multipart/form-data
Authorization: Bearer {accessToken}
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | MultipartFile | 是 | xlsx文件（含多个Sheet） |

**xlsx多Sheet结构：**
| Sheet名 | 用途 |
|---------|------|
| CityDetail | 详情基础字段 |
| IndustryStructure | 产业结构数据 |
| HousingPriceLevel | 房价水平数据 |
| HighEducation | 高等教育数据 |
| BasicEducation | 基础教育数据 |
| Transportation | 交通数据 |
| Employment | 就业数据 |
| EnterpriseStats | 企业统计数据 |
| FuturePlan | 未来规划数据 |
| Culture | 文化旅游数据 |
| Consumption | 消费数据 |
| Medical | 医疗数据 |
| HousingPolicy | 住房政策数据 |
| RentalCost | 租房成本数据 |

**业务说明：**
- 仅支持 .xlsx 格式文件
- 文件不能为空

**操作日志：** 此接口自动记录操作日志

---

## 二、行业管理接口

路由前缀：`/api/v1/admin/industry`

---

### 2.1 分页查询行业列表
```
GET /api/v1/admin/industry/list
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| industryName | String | 否 | 行业名称模糊查询 |
| category | String | 否 | 行业分类模糊查询 |
| talentTrend | String | 否 | 人才趋势筛选（上升/稳定/下降） |
| isDeleted | Boolean | 否 | 删除状态筛选 |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1234567890123456789,
        "industryName": "人工智能",
        "category": "信息技术",
        "talentTrend": "上升",
        "annualGrowthRate": 25.50,
        "isDeleted": false,
        "createdAt": "2026-05-08T10:30:00"
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1
  },
  "timestamp": 1714300000000
}
```

---

### 2.2 获取行业详情
```
GET /api/v1/admin/industry/{id}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 行业ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1234567890123456789,
    "industryName": "人工智能",
    "category": "信息技术",
    "iconClass": "fa-solid fa-robot",
    "description": "人工智能是当前最热门的技术领域...",
    "annualGrowthRate": 25.50,
    "marketScale": "1.8万亿",
    "talentGap": "120万",
    "investmentHeat": 85.00,
    "growthTrend": "上升",
    "marketTrend": "上升",
    "talentTrend": "上升",
    "investmentTrend": "上升",
    "isDeleted": false,
    "createdAt": "2026-05-08T10:30:00",
    "updatedAt": "2026-05-08T10:30:00",
    "detailId": 1234567890123456790,
    "shortDescription": "AI领域领跑者",
    "detailedDescription": "人工智能是模拟人类智能的技术...",
    "industryScale": {"value": 18000, "unit": "亿元", "year": 2025},
    "industryTalentDemand": {"total": 1200000, "positions": ["算法工程师", "数据科学家"]},
    "industrySalary": {"avg": 35000, "median": 30000},
    "policyInfo": {"支持政策": ["十四五规划", "新基建"]},
    "developmentSupportInfo": {"重点城市": ["北京", "上海", "深圳"]},
    "talentAnalysis": {"需求增长": 35, "供给缺口": 120},
    "talentPolicy": {"人才引进": true, "落户优惠": true},
    "salaryData": {"分位数": {"p25": 20000, "p50": 30000, "p75": 45000}}
  },
  "timestamp": 1714300000000
}
```

---

### 2.3 新增行业
```
POST /api/v1/admin/industry
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| industryName | String | 是 | 行业名称（最长100字符） |
| category | String | 是 | 行业分类（最长50字符） |
| iconClass | String | 否 | 图标样式类名（最长100字符） |
| description | String | 否 | 行业描述 |
| annualGrowthRate | BigDecimal | 否 | 年增长率（%） |
| marketScale | String | 否 | 市场规模（最长50字符） |
| talentGap | String | 否 | 人才缺口（最长50字符） |
| investmentHeat | BigDecimal | 否 | 投资热度 |
| growthTrend | String | 否 | 增长趋势（上升/稳定/下降） |
| marketTrend | String | 否 | 市场趋势（上升/稳定/下降） |
| talentTrend | String | 否 | 人才趋势（上升/稳定/下降） |
| investmentTrend | String | 否 | 投资趋势（上升/稳定/下降） |
| shortDescription | String | 否 | 简短描述 |
| detailedDescription | String | 否 | 详细描述 |
| industryScale | Map | 否 | 行业规模数据（JSONB） |
| industryTalentDemand | Map | 否 | 行业人才需求数据（JSONB） |
| industrySalary | Map | 否 | 行业薪资数据（JSONB） |
| policyInfo | Map | 否 | 政策信息（JSONB） |
| developmentSupportInfo | Map | 否 | 发展支持信息（JSONB） |
| talentAnalysis | Map | 否 | 人才分析（JSONB） |
| talentPolicy | Map | 否 | 人才政策（JSONB） |
| salaryData | Map | 否 | 薪资数据（JSONB） |

**请求示例：**
```json
{
  "industryName": "人工智能",
  "category": "信息技术",
  "iconClass": "fa-solid fa-robot",
  "description": "人工智能是当前最热门的技术领域...",
  "annualGrowthRate": 25.50,
  "marketScale": "1.8万亿",
  "talentGap": "120万",
  "talentTrend": "上升"
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": 1234567890123456789,
  "timestamp": 1714300000000
}
```

**操作日志：** 此接口自动记录操作日志

---

### 2.4 修改行业主表信息
```
PUT /api/v1/admin/industry/{id}
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 行业ID |

**请求参数：** 同新增（除industryName外均可选）

**操作日志：** 此接口自动记录操作日志

---

### 2.5 修改行业详情表信息
```
PUT /api/v1/admin/industry/{id}/detail
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 行业ID |

**请求参数：** 详情表所有JSONB字段

**操作日志：** 此接口自动记录操作日志

---

### 2.6 修改行业状态
```
PUT /api/v1/admin/industry/{id}/status
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 行业ID |

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| isDeleted | Boolean | 是 | 状态: true-禁用 false-启用 |

**操作日志：** 此接口自动记录操作日志

---

### 2.7 硬删除行业
```
DELETE /api/v1/admin/industry/{id}
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 行业ID |

**业务说明：** 物理删除主表+详情表记录，数据不可恢复。

**操作日志：** 此接口自动记录操作日志

---

### 2.8 批量硬删除行业
```
POST /api/v1/admin/industry/batch/delete
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**请求参数：**
```json
[1234567890123456789, 1234567890123456790]
```

**操作日志：** 此接口自动记录操作日志

---

### 2.9 导入行业主表xlsx
```
POST /api/v1/admin/industry/import
Content-Type: multipart/form-data
Authorization: Bearer {accessToken}
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | MultipartFile | 是 | xlsx文件 |

**xlsx表头格式：**
| 列名 | 说明 |
|------|------|
| industryName | 行业名称（必填） |
| category | 行业分类（必填） |
| iconClass | 图标样式类名 |
| description | 行业描述 |
| annualGrowthRate | 年增长率（%） |
| marketScale | 市场规模 |
| talentGap | 人才缺口 |
| investmentHeat | 投资热度 |
| growthTrend | 增长趋势 |
| marketTrend | 市场趋势 |
| talentTrend | 人才趋势 |
| investmentTrend | 投资趋势 |

**操作日志：** 此接口自动记录操作日志

---

### 2.10 导入行业详情xlsx（9个Sheet）
```
POST /api/v1/admin/industry/import-detail
Content-Type: multipart/form-data
Authorization: Bearer {accessToken}
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | MultipartFile | 是 | xlsx文件（含9个Sheet） |

**xlsx多Sheet结构：**
| Sheet名 | 用途 |
|---------|------|
| IndustryDetail | 详情基础字段 |
| IndustryScale | 行业规模数据 |
| TalentDemand | 人才需求数据 |
| IndustrySalary | 行业薪资数据 |
| PolicyInfo | 政策信息数据 |
| DevelopmentSupport | 发展支持数据 |
| TalentAnalysis | 人才分析数据 |
| TalentPolicy | 人才政策数据 |
| SalaryData | 薪资分布数据 |

**操作日志：** 此接口自动记录操作日志

---

## 三、资源管理接口

路由前缀：`/api/v1/admin/resource`

---

### 3.1 分页查询资源列表
```
GET /api/v1/admin/resource/list
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| resourceName | String | 否 | 资源名称模糊查询 |
| category | String | 否 | 分类模糊查询 |
| isDeleted | Boolean | 否 | 删除状态筛选 |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1234567890123456789,
        "resourceName": "2025年考研英语真题及解析",
        "category": "考研真题",
        "fileType": "PDF",
        "viewCount": 1520,
        "sortOrder": 1,
        "isDeleted": false,
        "updatedAt": "2026-05-08T10:30:00+08:00"
      }
    ],
    "total": 200,
    "size": 10,
    "current": 1
  },
  "timestamp": 1714300000000
}
```

---

### 3.2 获取资源详情
```
GET /api/v1/admin/resource/{id}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 资源ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1234567890123456789,
    "resourceName": "2025年考研英语真题及解析",
    "coverUrl": "https://example.com/cover.jpg",
    "description": "包含近10年考研英语一/二真题及详细解析...",
    "resourceUrl": "https://pan.baidu.com/s/xxxxx",
    "accessCode": "abcd",
    "category": "考研真题",
    "fileType": "PDF",
    "viewCount": 1520,
    "sortOrder": 1,
    "isDeleted": false,
    "createdAt": "2026-01-01T09:00:00+08:00",
    "updatedAt": "2026-05-08T10:30:00+08:00"
  },
  "timestamp": 1714300000000
}
```

---

### 3.3 新增资源
```
POST /api/v1/admin/resource
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| resourceName | String | 是 | 资源名称（最长100字符） |
| coverUrl | String | 否 | 封面URL（最长500字符） |
| description | String | 否 | 描述（最长1000字符） |
| resourceUrl | String | 是 | 资源URL（最长500字符） |
| accessCode | String | 否 | 访问码/提取码（最长50字符） |
| category | String | 否 | 分类（最长50字符） |
| fileType | String | 否 | 文件类型（最长20字符） |
| sortOrder | Integer | 否 | 排序序号，默认0 |

**请求示例：**
```json
{
  "resourceName": "2025年考研英语真题及解析",
  "coverUrl": "https://example.com/cover.jpg",
  "description": "包含近10年考研英语一/二真题及详细解析...",
  "resourceUrl": "https://pan.baidu.com/s/xxxxx",
  "accessCode": "abcd",
  "category": "考研真题",
  "fileType": "PDF",
  "sortOrder": 1
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": 1234567890123456789,
  "timestamp": 1714300000000
}
```

**操作日志：** 此接口自动记录操作日志

---

### 3.4 修改资源
```
PUT /api/v1/admin/resource/{id}
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 资源ID |

**请求参数：** 同新增

**操作日志：** 此接口自动记录操作日志

---

### 3.5 修改资源状态
```
PUT /api/v1/admin/resource/{id}/status
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 资源ID |

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| isDeleted | Boolean | 是 | 状态: true-禁用 false-启用 |

**请求示例：**
```json
{
  "isDeleted": true
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

**操作日志：** 此接口自动记录操作日志

---

### 3.6 硬删除资源
```
DELETE /api/v1/admin/resource/{id}
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 资源ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

**业务说明：** 物理删除记录，数据不可恢复。如需保留数据，请使用状态切换接口（3.5）。

**操作日志：** 此接口自动记录操作日志

---

### 3.7 批量硬删除资源
```
DELETE /api/v1/admin/resource/batch
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**请求参数：**
```json
[1234567890123456789, 1234567890123456790]
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1714300000000
}
```

**操作日志：** 此接口自动记录操作日志

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录或 Token 过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |

---

## 数据库表结构

### t_city (城市主表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 城市ID（雪花算法） |
| city_name | VARCHAR(50) | 城市名称（未删除记录唯一，软删除后同名可复用） |
| province | VARCHAR(30) | 所属省份 |
| region | VARCHAR(20) | 所属地区（华东/华南/华北等） |
| city_intro | TEXT | 城市简介 |
| college_count | INTEGER | 高校数量 |
| key_college_count | INTEGER | 重点高校数量 |
| resident_population | NUMERIC(8,2) | 常住人口（万人） |
| gdp | NUMERIC(10,2) | GDP（亿元） |
| is_deleted | BOOLEAN | 是否删除 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

### t_city_detail (城市详情表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 详情ID（雪花算法） |
| city_id | BIGINT | 关联城市ID（一对一） |
| city_name | VARCHAR(50) | 城市名称（冗余） |
| area | NUMERIC(10,2) | 面积（平方公里） |
| subtitle | VARCHAR(200) | 副标题 |
| city_level | VARCHAR(20) | 城市级别（直辖市/省会城市/地级市/县级市） |
| admin_code | VARCHAR(20) | 行政区划代码 |
| per_capita_gdp | NUMERIC(8,2) | 人均GDP（万元） |
| urbanization_rate | NUMERIC(5,2) | 城镇化率（%） |
| rural_pop_ratio | NUMERIC(5,2) | 农村人口比例（%） |
| aging_rate | NUMERIC(5,2) | 老龄化率（%） |
| migrant_pop_ratio | NUMERIC(5,2) | 外来人口比例（%） |
| gdp_growth_rate | NUMERIC(5,2) | GDP增长率（%） |
| fortune_500_count | INTEGER | 世界500强企业数量 |
| industry_structure | JSONB | 产业结构占比 |
| industry_description | TEXT | 产业描述 |
| main_industries | TEXT[] | 主要产业列表 |
| emerging_industries | TEXT[] | 新兴产业列表 |
| future_plan | JSONB | 未来规划 |
| high_education | JSONB | 高等教育资源 |
| basic_education | JSONB | 基础教育资源 |
| enterprise_stats | JSONB | 企业统计 |
| housing_price_level | JSONB | 房价水平 |
| rental_cost | JSONB | 租房成本 |
| housing_policy | JSONB | 住房政策 |
| consumption | JSONB | 消费数据 |
| employment | JSONB | 就业数据 |
| transportation | JSONB | 交通数据 |
| medical | JSONB | 医疗数据 |
| culture | JSONB | 文化旅游数据 |
| is_deleted | BOOLEAN | 是否删除 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

### t_industry (行业主表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 行业ID（雪花算法） |
| industry_name | VARCHAR(100) | 行业名称（唯一） |
| category | VARCHAR(50) | 行业分类 |
| icon_class | VARCHAR(100) | 图标CSS类名 |
| description | TEXT | 行业描述 |
| annual_growth_rate | NUMERIC(5,2) | 年增长率（%） |
| market_scale | VARCHAR(50) | 市场规模 |
| talent_gap | VARCHAR(50) | 人才缺口 |
| investment_heat | NUMERIC(5,2) | 投资热度（%） |
| growth_trend | VARCHAR(10) | 增长趋势（上升/稳定/下降） |
| market_trend | VARCHAR(10) | 市场趋势（上升/稳定/下降） |
| talent_trend | VARCHAR(10) | 人才趋势（上升/稳定/下降） |
| investment_trend | VARCHAR(10) | 投资趋势（上升/稳定/下降） |
| is_deleted | BOOLEAN | 是否删除 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

### t_industry_detail (行业详情表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 详情ID（雪花算法） |
| industry_id | BIGINT | 关联行业ID（一对一） |
| industry_name | VARCHAR(100) | 行业名称（冗余） |
| short_description | VARCHAR(500) | 简短描述 |
| detailed_description | TEXT | 详细描述 |
| industry_scale | JSONB | 发展规模 |
| industry_talent_demand | JSONB | 人才需求 |
| industry_salary | JSONB | 行业薪资 |
| policy_info | JSONB | 政策信息 |
| development_support_info | JSONB | 发展地域与城市支持 |
| talent_analysis | JSONB | 人才需求分析 |
| talent_policy | JSONB | 人才政策 |
| salary_data | JSONB | 薪资数据 |
| is_deleted | BOOLEAN | 是否删除 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

### t_resource (资源表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 资源ID（雪花算法） |
| resource_name | VARCHAR(200) | 资源名称 |
| cover_url | VARCHAR(500) | 封面图URL |
| description | TEXT | 资源描述 |
| resource_url | VARCHAR(500) | 资源链接（百度网盘地址） |
| access_code | VARCHAR(20) | 百度网盘提取码 |
| category | VARCHAR(50) | 分类（考研真题/四六级/公务员/专业课） |
| file_type | VARCHAR(20) | 文件类型（PDF/视频/压缩包） |
| view_count | INTEGER | 浏览统计 |
| sort_order | INTEGER | 排序权重 |
| is_deleted | BOOLEAN | 是否删除 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

---

## 文件清单

### haifeng-common (公共模块)
- `entity/city/City.java` - 城市实体
- `entity/city/CityDetail.java` - 城市详情实体
- `entity/industry/Industry.java` - 行业实体
- `entity/industry/IndustryDetail.java` - 行业详情实体
- `entity/resource/Resource.java` - 资源实体
- `mapper/city/CityMapper.java` - 城市 Mapper
- `mapper/city/CityDetailMapper.java` - 城市详情 Mapper
- `mapper/industry/IndustryMapper.java` - 行业 Mapper
- `mapper/industry/IndustryDetailMapper.java` - 行业详情 Mapper
- `mapper/resource/ResourceMapper.java` - 资源 Mapper

### haifeng-admin (管理端)
- `db/migration/V8__create_cities_industries.sql` - 数据库迁移脚本
- `controller/city/CityController.java` - 城市控制器
- `controller/industry/IndustryController.java` - 行业控制器
- `controller/resource/ResourceController.java` - 资源控制器
- `service/city/CityService.java` - 城市服务接口
- `service/industry/IndustryService.java` - 行业服务接口
- `service/resource/ResourceService.java` - 资源服务接口
- `service/impl/city/CityServiceImpl.java` - 城市服务实现
- `service/impl/industry/IndustryServiceImpl.java` - 行业服务实现
- `service/impl/resource/ResourceServiceImpl.java` - 资源服务实现
- `dto/city/CityQueryDTO.java` - 城市查询 DTO
- `dto/city/CityAddDTO.java` - 城市新增 DTO
- `dto/city/CityUpdateDTO.java` - 城市修改 DTO
- `dto/city/CityDetailUpdateDTO.java` - 城市详情修改 DTO
- `dto/city/CityStatusDTO.java` - 城市状态 DTO
- `dto/industry/IndustryQueryDTO.java` - 行业查询 DTO
- `dto/industry/IndustryAddDTO.java` - 行业新增 DTO
- `dto/industry/IndustryUpdateDTO.java` - 行业修改 DTO
- `dto/industry/IndustryDetailUpdateDTO.java` - 行业详情修改 DTO
- `dto/industry/IndustryStatusDTO.java` - 行业状态 DTO
- `dto/resource/ResourceQueryDTO.java` - 资源查询 DTO
- `dto/resource/ResourceAddDTO.java` - 资源新增 DTO
- `dto/resource/ResourceUpdateDTO.java` - 资源修改 DTO
- `dto/resource/ResourceStatusDTO.java` - 资源状态 DTO
- `vo/city/CityListVO.java` - 城市列表 VO
- `vo/city/CityDetailVO.java` - 城市详情 VO
- `vo/industry/IndustryListVO.java` - 行业列表 VO
- `vo/industry/IndustryDetailVO.java` - 行业详情 VO
- `vo/resource/ResourceListVO.java` - 资源列表 VO
- `vo/resource/ResourceDetailVO.java` - 资源详情 VO
- `excel/city/*.java` - 城市Excel导入DTO类（16个文件）
- `excel/industry/*.java` - 行业Excel导入DTO类（10个文件）