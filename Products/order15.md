``# 企业管理模块

## 模块概述

本模块实现企业信息管理功能，属于「企业管理」父模块，包含两个子模块：

| 子模块 | 说明 |
|--------|------|
| 企业列表 | 管理企业基本信息及其关联岗位，支持Excel批量导入（企业+岗位多Sheet） |
| 企业-行业关联 | 管理企业与行业的多对多关联关系，支持Excel批量导入 |

---

## Excel导入模板

### 企业+岗位导入模板（单文件多Sheet）

**Sheet0: 企业主表**
| 城市名称 | 企业名称 | 企业性质 | 企业类型 | Logo地址 | 官网 | 总部地区 | 企业规模 | 主营业务 | 企业简介 | 招聘状态 |
|----------|----------|----------|----------|----------|------|----------|----------|----------|----------|----------|
| 深圳市 | 腾讯科技 | 民企 | 互联网大厂 | https://xxx.com/logo.png | https://www.tencent.com | 广东省深圳市 | 10000人以上 | 社交、游戏、云服务 | 腾讯是一家... | 招聘中 |
| 北京市 | 中国烟草 | 央企 | 烟草行业 | | | 北京市 | 10000人以上 | 烟草生产与销售 | | 招聘中 |

**Sheet1: 企业岗位**
| 企业名称 | 岗位名称 | 招聘类型 | 岗位要求 | 岗位标签 | 省份 | 城市 | 工作地点 | 学历要求 | 专业要求 | 工作经验 | 最低薪资 | 最高薪资 | 申请链接 | 截止日期 | 岗位状态 |
|----------|----------|----------|----------|----------|------|------|----------|----------|----------|----------|----------|----------|----------|----------|----------|
| 腾讯科技 | 软件工程师 | 校招 | 熟悉Java/Python | 五险一金,弹性工作,免费三餐 | 广东省 | 深圳市 | 深圳市南山区科技园 | 本科 | 计算机相关 | 应届 | 20 | 35 | https://hr.tencent.com | 2026-06-30 | 招聘中 |
| 腾讯科技 | 产品经理 | 社招 | 3年以上产品经验 | 五险一金,股票期权 | 广东省 | 深圳市 | | 本科 | 不限 | 3-5年 | 30 | 50 | | | 招聘中 |

> **注意事项**：
> - 企业名称必填且唯一，Sheet1中的企业名称必须在Sheet0中存在
> - 企业性质必须是：央企、国企、民企、外企、合资
> - 招聘类型可选：校招、社招、实习（可为空）
> - 学历要求可选：不限、大专、本科、硕士、博士（可为空）
> - 岗位标签支持中英文逗号分隔，如：五险一金,弹性工作
> - 薪资单位为k/月，如20表示20k
> - 截止日期格式：yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss

### 企业-行业关联导入模板

**Sheet0: 关联表**
| 企业名称 | 行业名称 |
|----------|----------|
| 腾讯科技 | 信息技术 |
| 腾讯科技 | 互联网 |
| 中国烟草 | 制造业 |

> **注意事项**：
> - 企业名称必须在 t_enterprise 表中存在
> - 行业名称必须在 t_industry 表中存在
> - 同一企业不能重复关联同一行业

---

## 数据表

### 1. t_enterprise（企业表）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | 是 | 主键（雪花算法） |
| city_name | VARCHAR(50) | 否 | 城市名称 |
| enterprise_name | VARCHAR(200) | 是 | 企业名称（唯一） |
| enterprise_nature | VARCHAR(30) | 是 | 企业性质（央企/国企/民企/外企/合资） |
| enterprise_type | VARCHAR(50) | 否 | 企业类型（如：互联网大厂、地方国企） |
| logo_url | VARCHAR(500) | 否 | Logo图片地址 |
| official_website | VARCHAR(500) | 否 | 企业官网 |
| region | VARCHAR(100) | 否 | 总部地区 |
| enterprise_scale | VARCHAR(50) | 否 | 企业规模（如：10000人以上） |
| main_business | VARCHAR(500) | 否 | 主营业务 |
| enterprise_intro | TEXT | 否 | 企业简介 |
| recruitment_status | VARCHAR(20) | 否 | 招聘状态，默认"招聘中" |
| is_deleted | BOOLEAN | 是 | 是否禁用，默认false |
| created_at | TIMESTAMPTZ | 是 | 创建时间 |
| updated_at | TIMESTAMPTZ | 是 | 更新时间 |

### 2. t_enterprise_position（企业岗位表）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | 是 | 主键（雪花算法） |
| enterprise_id | BIGINT | 是 | 企业ID（逻辑外键） |
| position_name | VARCHAR(200) | 是 | 岗位名称 |
| recruitment_type | VARCHAR(30) | 否 | 招聘类型（校招/社招/实习） |
| position_requirement | TEXT | 否 | 岗位要求 |
| position_tags | TEXT[] | 否 | 岗位标签数组 |
| province | VARCHAR(30) | 否 | 省份 |
| city | VARCHAR(50) | 否 | 城市 |
| work_location | VARCHAR(200) | 否 | 详细工作地点 |
| education_requirement | VARCHAR(30) | 否 | 学历要求（不限/大专/本科/硕士/博士） |
| major_requirement | VARCHAR(500) | 否 | 专业要求 |
| work_experience | VARCHAR(50) | 否 | 工作经验要求 |
| salary_min | INTEGER | 否 | 最低月薪（单位：k） |
| salary_max | INTEGER | 否 | 最高月薪（单位：k） |
| apply_link | VARCHAR(500) | 否 | 申请链接 |
| deadline | TIMESTAMPTZ | 否 | 报名截止日期 |
| position_status | VARCHAR(20) | 否 | 岗位状态（招聘中/已结束） |
| is_deleted | BOOLEAN | 是 | 是否删除，默认false |
| created_at | TIMESTAMPTZ | 是 | 创建时间 |
| updated_at | TIMESTAMPTZ | 是 | 更新时间 |

### 3. t_enterprise_industry（企业-行业关联表）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | 是 | 主键（雪花算法） |
| enterprise_id | BIGINT | 是 | 企业ID |
| enterprise_name | VARCHAR(200) | 是 | 企业名称（冗余） |
| industry_id | BIGINT | 是 | 行业ID |
| industry_name | VARCHAR(100) | 是 | 行业名称（冗余） |
| is_primary | BOOLEAN | 是 | 是否主行业，默认false |
| sort_order | SMALLINT | 是 | 排序权重，默认0 |
| created_at | TIMESTAMPTZ | 是 | 创建时间 |

**唯一约束**：enterprise_id + industry_id

---

## 一、企业管理接口

基础路径：`/api/v1/admin/company/enterprise`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list` | 分页查询列表 |
| GET | `/{id}` | 获取详情（含岗位列表） |
| POST | `/` | 新增企业 |
| PUT | `/{id}` | 修改企业 |
| PUT | `/{id}/status` | 修改状态（禁用/启用） |
| DELETE | `/{id}` | 删除企业（硬删除，含关联岗位） |
| DELETE | `/batch` | 批量删除（硬删除） |
| POST | `/import` | Excel批量导入（企业+岗位） |

### 1.1 分页查询列表

**请求**
```
GET /api/v1/admin/company/enterprise/list?page=1&size=10&cityName=深圳&enterpriseName=腾讯&enterpriseNature=民企
```

**查询参数**
| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页数量，默认10，可选：10/20/30/50/100/200/500/1000 |
| cityName | String | 否 | **模糊查询** | 城市名称 |
| enterpriseName | String | 否 | **模糊查询** | 企业名称 |
| enterpriseNature | String | 否 | **精确查询** | 企业性质（央企/国企/民企/外企/合资） |
| enterpriseType | String | 否 | **模糊查询** | 企业类型 |
| recruitmentStatus | String | 否 | **精确查询** | 招聘状态 |
| isDeleted | Boolean | 否 | **精确查询** | 是否禁用 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": "1893000000000001",
        "cityName": "深圳市",
        "enterpriseName": "腾讯科技",
        "enterpriseNature": "民企",
        "enterpriseType": "互联网大厂",
        "recruitmentStatus": "招聘中",
        "isDeleted": false,
        "createdAt": "2026-05-11T10:00:00"
      },
      {
        "id": "1893000000000002",
        "cityName": "北京市",
        "enterpriseName": "中国烟草",
        "enterpriseNature": "央企",
        "enterpriseType": "烟草行业",
        "recruitmentStatus": "招聘中",
        "isDeleted": false,
        "createdAt": "2026-05-11T10:00:00"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  },
  "timestamp": 1715500800000
}
```

**响应字段类型**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 企业ID（雪花算法，前端用字符串避免精度丢失） |
| cityName | String \| null | 城市名称 |
| enterpriseName | String | 企业名称 |
| enterpriseNature | String | 企业性质 |
| enterpriseType | String \| null | 企业类型 |
| recruitmentStatus | String | 招聘状态 |
| isDeleted | Boolean | 是否禁用 |
| createdAt | String | 创建时间（LocalDateTime格式） |

### 1.2 获取详情

**请求**
```
GET /api/v1/admin/company/enterprise/{id}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 企业ID |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1893000000000001",
    "cityName": "深圳市",
    "enterpriseName": "腾讯科技",
    "enterpriseNature": "民企",
    "enterpriseType": "互联网大厂",
    "logoUrl": "https://xxx.com/logo.png",
    "officialWebsite": "https://www.tencent.com",
    "region": "广东省深圳市",
    "enterpriseScale": "10000人以上",
    "mainBusiness": "社交、游戏、云服务",
    "enterpriseIntro": "腾讯是一家领先的互联网科技公司...",
    "recruitmentStatus": "招聘中",
    "isDeleted": false,
    "createdAt": "2026-05-11T10:00:00",
    "updatedAt": "2026-05-11T10:00:00",
    "positions": [
      {
        "id": "1893000000000101",
        "enterpriseId": "1893000000000001",
        "positionName": "软件工程师",
        "recruitmentType": "校招",
        "positionRequirement": "熟悉Java/Python...",
        "positionTags": ["五险一金", "弹性工作", "免费三餐"],
        "province": "广东省",
        "city": "深圳市",
        "workLocation": "深圳市南山区科技园",
        "educationRequirement": "本科",
        "majorRequirement": "计算机相关",
        "workExperience": "应届",
        "salaryMin": 20,
        "salaryMax": 35,
        "applyLink": "https://hr.tencent.com",
        "deadline": "2026-06-30T00:00:00",
        "positionStatus": "招聘中",
        "isDeleted": false,
        "createdAt": "2026-05-11T10:00:00",
        "updatedAt": "2026-05-11T10:00:00"
      }
    ]
  },
  "timestamp": 1715500800000
}
```

**响应字段类型（企业详情）**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 企业ID |
| cityName | String \| null | 城市名称 |
| enterpriseName | String | 企业名称 |
| enterpriseNature | String | 企业性质 |
| enterpriseType | String \| null | 企业类型 |
| logoUrl | String \| null | Logo地址 |
| officialWebsite | String \| null | 官网 |
| region | String \| null | 总部地区 |
| enterpriseScale | String \| null | 企业规模 |
| mainBusiness | String \| null | 主营业务 |
| enterpriseIntro | String \| null | 企业简介 |
| recruitmentStatus | String | 招聘状态 |
| isDeleted | Boolean | 是否禁用 |
| createdAt | String | 创建时间 |
| updatedAt | String | 更新时间 |
| positions | Array | 岗位列表（Tab2数据） |

**响应字段类型（岗位）**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 岗位ID |
| enterpriseId | String | 企业ID |
| positionName | String | 岗位名称 |
| recruitmentType | String \| null | 招聘类型 |
| positionRequirement | String \| null | 岗位要求 |
| positionTags | Array\<String\> \| null | 岗位标签 |
| province | String \| null | 省份 |
| city | String \| null | 城市 |
| workLocation | String \| null | 工作地点 |
| educationRequirement | String \| null | 学历要求 |
| majorRequirement | String \| null | 专业要求 |
| workExperience | String \| null | 工作经验 |
| salaryMin | Integer \| null | 最低薪资（k/月） |
| salaryMax | Integer \| null | 最高薪资（k/月） |
| applyLink | String \| null | 申请链接 |
| deadline | String \| null | 截止日期 |
| positionStatus | String \| null | 岗位状态 |
| isDeleted | Boolean | 是否删除 |
| createdAt | String | 创建时间 |
| updatedAt | String | 更新时间 |

### 1.3 新增企业

**请求**
```
POST /api/v1/admin/company/enterprise
Content-Type: application/json

{
  "cityName": "深圳市",
  "enterpriseName": "腾讯科技",
  "enterpriseNature": "民企",
  "enterpriseType": "互联网大厂",
  "logoUrl": "https://xxx.com/logo.png",
  "officialWebsite": "https://www.tencent.com",
  "region": "广东省深圳市",
  "enterpriseScale": "10000人以上",
  "mainBusiness": "社交、游戏、云服务",
  "enterpriseIntro": "腾讯是一家领先的互联网科技公司...",
  "recruitmentStatus": "招聘中"
}
```

**请求体参数**
| 参数 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| cityName | String | 否 | 最大50字符 | 城市名称 |
| enterpriseName | String | 是 | 最大200字符，唯一 | 企业名称 |
| enterpriseNature | String | 是 | 必须是：央企/国企/民企/外企/合资 | 企业性质 |
| enterpriseType | String | 否 | 最大50字符 | 企业类型 |
| logoUrl | String | 否 | 最大500字符 | Logo地址 |
| officialWebsite | String | 否 | 最大500字符 | 官网 |
| region | String | 否 | 最大100字符 | 总部地区 |
| enterpriseScale | String | 否 | 最大50字符 | 企业规模 |
| mainBusiness | String | 否 | 最大500字符 | 主营业务 |
| enterpriseIntro | String | 否 | - | 企业简介 |
| recruitmentStatus | String | 否 | 最大20字符 | 招聘状态，默认"招聘中" |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": "1893000000000001",
  "timestamp": 1715500800000
}
```

**错误响应**
```json
{
  "code": 400,
  "msg": "企业名称已存在",
  "data": null,
  "timestamp": 1715500800000
}
```

```json
{
  "code": 400,
  "msg": "企业性质必须是：央企、国企、民企、外企、合资",
  "data": null,
  "timestamp": 1715500800000
}
```

### 1.4 修改企业

**请求**
```
PUT /api/v1/admin/company/enterprise/{id}
Content-Type: application/json

{
  "cityName": "深圳市",
  "enterpriseName": "腾讯科技（更新）",
  "enterpriseNature": "民企",
  "enterpriseType": "互联网大厂",
  "logoUrl": "https://xxx.com/logo-new.png",
  "officialWebsite": "https://www.tencent.com",
  "region": "广东省深圳市",
  "enterpriseScale": "10000人以上",
  "mainBusiness": "社交、游戏、云服务、金融科技",
  "enterpriseIntro": "腾讯是一家领先的互联网科技公司（已更新）...",
  "recruitmentStatus": "招聘中",
  "isDeleted": false
}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 企业ID |

**请求体参数**：同新增，额外增加：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| isDeleted | Boolean | 否 | 是否禁用 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

### 1.5 修改状态

**请求**
```
PUT /api/v1/admin/company/enterprise/{id}/status
Content-Type: application/json

{
  "isDeleted": true
}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 企业ID |

**请求体参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| isDeleted | Boolean | 是 | 是否禁用（true=禁用，false=启用） |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

### 1.6 删除企业

**请求**
```
DELETE /api/v1/admin/company/enterprise/{id}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 企业ID |

> **注意**：此为硬删除，会同时删除企业关联的所有岗位数据

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

### 1.7 批量删除

**请求**
```
DELETE /api/v1/admin/company/enterprise/batch
Content-Type: application/json

{
  "ids": [1893000000000001, 1893000000000002]
}
```

**请求体参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | Array\<Long\> | 是 | 企业ID数组 |

> **注意**：此为硬删除，会同时删除所有企业关联的岗位数据

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

### 1.8 Excel批量导入

**请求**
```
POST /api/v1/admin/company/enterprise/import
Content-Type: multipart/form-data

file: [Excel文件]
```

**Excel模板**：见文档开头"Excel导入模板"章节

**成功响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

**校验错误响应**
```json
{
  "code": 400,
  "msg": "导入失败：Sheet0第2行：企业名称不能为空；Sheet0第5行：企业名称'腾讯科技'在文件中重复；Sheet0第8行：企业名称'华为技术'已存在于数据库；Sheet0第10行：企业性质必须是：央企、国企、民企、外企、合资；Sheet1第3行：企业名称'未知企业'在Sheet0中不存在；Sheet1第6行：招聘类型必须是：校招、社招、实习",
  "data": null,
  "timestamp": 1715500800000
}
```

**导入规则**：
1. Sheet0为企业主表，Sheet1为岗位表
2. 企业名称必填且唯一（文件内+数据库）
3. 企业性质必须是：央企、国企、民企、外企、合资
4. Sheet1中的企业名称必须在Sheet0中存在
5. 招聘类型（如果非空）必须是：校招、社招、实习
6. 学历要求（如果非空）必须是：不限、大专、本科、硕士、博士
7. 岗位标签支持中英文逗号分隔
8. 任何错误都会导致整个导入回滚

---

## 二、企业-行业关联接口

基础路径：`/api/v1/admin/company/enterprise-industry`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list` | 分页查询列表 |
| GET | `/{id}` | 获取详情 |
| DELETE | `/{id}` | 删除关联（硬删除） |
| DELETE | `/batch` | 批量删除（硬删除） |
| POST | `/import` | Excel批量导入 |

### 2.1 分页查询列表

**请求**
```
GET /api/v1/admin/company/enterprise-industry/list?page=1&size=10&enterpriseName=腾讯&industryName=信息
```

**查询参数**
| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页数量，默认10，可选：10/20/30/50/100/200/500/1000 |
| enterpriseName | String | 否 | **模糊查询** | 企业名称 |
| industryName | String | 否 | **模糊查询** | 行业名称 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": "1893000000000501",
        "enterpriseId": "1893000000000001",
        "enterpriseName": "腾讯科技",
        "industryId": "1893000000000201",
        "industryName": "信息技术",
        "isPrimary": true,
        "sortOrder": 0,
        "createdAt": "2026-05-11T10:00:00"
      },
      {
        "id": "1893000000000502",
        "enterpriseId": "1893000000000001",
        "enterpriseName": "腾讯科技",
        "industryId": "1893000000000202",
        "industryName": "互联网",
        "isPrimary": false,
        "sortOrder": 1,
        "createdAt": "2026-05-11T10:00:00"
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1,
    "pages": 5
  },
  "timestamp": 1715500800000
}
```

**响应字段类型**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 关联ID |
| enterpriseId | String | 企业ID |
| enterpriseName | String | 企业名称 |
| industryId | String | 行业ID |
| industryName | String | 行业名称 |
| isPrimary | Boolean | 是否主行业 |
| sortOrder | Integer | 排序值 |
| createdAt | String | 创建时间 |

### 2.2 获取详情

**请求**
```
GET /api/v1/admin/company/enterprise-industry/{id}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 关联ID |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1893000000000501",
    "enterpriseId": "1893000000000001",
    "enterpriseName": "腾讯科技",
    "industryId": "1893000000000201",
    "industryName": "信息技术",
    "isPrimary": true,
    "sortOrder": 0,
    "createdAt": "2026-05-11T10:00:00"
  },
  "timestamp": 1715500800000
}
```

### 2.3 删除关联

**请求**
```
DELETE /api/v1/admin/company/enterprise-industry/{id}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 关联ID |

> **注意**：此为硬删除

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

### 2.4 批量删除

**请求**
```
DELETE /api/v1/admin/company/enterprise-industry/batch
Content-Type: application/json

{
  "ids": [1893000000000501, 1893000000000502]
}
```

**请求体参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ids | Array\<Long\> | 是 | 关联ID数组 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

### 2.5 Excel批量导入

**请求**
```
POST /api/v1/admin/company/enterprise-industry/import
Content-Type: multipart/form-data

file: [Excel文件]
```

**Excel模板**：见文档开头"Excel导入模板"章节

**成功响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

**校验错误响应**
```json
{
  "code": 400,
  "msg": "导入失败：第2行：企业名称不能为空；第5行：企业名称'未知企业'不存在；第8行：行业名称'未知行业'不存在；第10行：企业'腾讯科技'-行业'信息技术'关联在文件中重复；第12行：企业'腾讯科技'-行业'信息技术'关联已存在",
  "data": null,
  "timestamp": 1715500800000
}
```

**导入规则**：
1. 企业名称必填，必须在 t_enterprise 表中存在
2. 行业名称必填，必须在 t_industry 表中存在（is_deleted=false）
3. 同一企业不能重复关联同一行业（文件内+数据库）
4. 任何错误都会导致整个导入回滚

---

## 三、错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 / 数据校验失败 / 数据已存在 / 外键不存在 |
| 404 | 记录不存在 |
| 401 | 未登录 / Token过期 |
| 403 | 无权限 |
| 500 | 服务器内部错误 |

---

## 四、字段类型规范

为确保前后端数据一致性，所有接口遵循以下类型规范：

| 后端类型 | 前端类型 | 说明 |
|----------|----------|------|
| Long（ID） | String | 避免JavaScript精度丢失 |
| Short/Integer | Number | 数值类型 |
| String | String | 字符串 |
| Boolean | Boolean | 布尔值 |
| LocalDateTime | String | 格式：yyyy-MM-ddTHH:mm:ss |
| List\<String\> | Array\<String\> | 字符串数组（如岗位标签） |
| null | null | 空值保持null，不转为空字符串 |

> **重要**：所有 `id` 字段返回为**字符串类型**，前端请求时也应传字符串或数字均可。

---

## 五、企业性质枚举值

| 值 | 说明 |
|----|------|
| 央企 | 中央直属企业 |
| 国企 | 地方国有企业 |
| 民企 | 民营企业 |
| 外企 | 外资企业 |
| 合资 | 中外合资企业 |

---

## 六、招聘相关枚举值

### 招聘类型
| 值 | 说明 |
|----|------|
| 校招 | 校园招聘 |
| 社招 | 社会招聘 |
| 实习 | 实习生招聘 |

### 学历要求
| 值 | 说明 |
|----|------|
| 不限 | 不限学历 |
| 大专 | 大专及以上 |
| 本科 | 本科及以上 |
| 硕士 | 硕士及以上 |
| 博士 | 博士及以上 |

### 岗位状态
| 值 | 说明 |
|----|------|
| 招聘中 | 正在招聘 |
| 已结束 | 招聘已结束 |
