# 专业组管理模块 API 文档

## 功能概述

专业组管理模块用于管理高考志愿填报系统中的专业组录取数据，包含两个子模块：

| 子模块 | 说明 | 删除方式 |
|--------|------|----------|
| 专业组录取列表 | 管理专业组汇总信息 | 软删除（禁用）+ 硬删除 |
| 专业组明细列表 | 管理专业组内各专业录取明细 | 软删除（禁用）+ 硬删除 |

**选科要求字段说明：**
- `subjects`：科目数组，如 `["物理", "化学"]`
- `requirementType`：要求类型，枚举值：`不限` / `2选1` / `3选1` / `必选1` / `必选2` / `必选3`

**匹配逻辑示例（用户选科：物理+化学+生物）：**
```sql
WHERE requirement_type = '不限'
   OR (requirement_type IN ('2选1','3选1') AND subjects && user_subjects)
   OR (requirement_type IN ('必选1','必选2','必选3') AND subjects <@ user_subjects)
```

**核心功能：**
- 支持Excel批量导入，一个xlsx文件同时写入专业组表和专业明细表
- 数据库触发器自动计算聚合字段（专业数量、最低分、平均分等）
- 手动全量重算接口用于数据修复场景

---

## 一、专业组录取管理

基础路径：`/api/v1/admin/algorithm/admission/group`

### 1.1 分页查询专业组

**请求**
```
GET /api/v1/admin/algorithm/admission/group/page
```

**Query参数**
| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1，最小1 |
| size | Integer | 否 | - | 每页条数，默认10，范围10-1000 |
| universityName | String | 否 | 模糊查询 | 大学名称 |
| year | Short | 否 | 精确匹配 | 年份 |
| province | String | 否 | 精确匹配 | 省份 |
| requirementType | String | 否 | 精确匹配 | 选科类型：不限/2选1/3选1/必选1/必选2/必选3 |
| enrollmentCode | String | 否 | 模糊查询 | 省招代码 |
| groupCode | String | 否 | 模糊查询 | 专业组代码 |
| groupName | String | 否 | 模糊查询 | 专业组名称 |
| isDeleted | Boolean | 否 | 精确匹配 | 是否已删除，默认false |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "universityId": 1001,
        "universityName": "清华大学",
        "cityName": "北京市",
        "year": 2025,
        "province": "北京",
        "batch": "本科批",
        "enrollmentCode": "10001",
        "groupCode": "01",
        "groupName": "理工组",
        "subjects": ["物理", "化学"],
        "requirementType": "必选2",
        "majorCount": 15,
        "admissionCount": 120,
        "minScore": 680,
        "minRank": 500,
        "avgScore": 695.5,
        "isDeleted": false
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  },
  "timestamp": 1715241600000
}
```

### 1.2 获取专业组详情

**请求**
```
GET /api/v1/admin/algorithm/admission/group/{id}
```

**Path参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Integer | 是 | 专业组ID |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "universityId": 1001,
    "universityName": "清华大学",
    "cityName": "北京市",
    "year": 2025,
    "province": "北京",
    "batch": "本科批",
    "enrollmentCode": "10001",
    "groupCode": "01",
    "groupName": "理工组",
    "subjects": ["物理", "化学"],
    "requirementType": "必选2",
    "description": "理工类专业组",
    "constraints": ["限理科生", "色盲色弱不宜"],
    "majorCount": 15,
    "categoryCount": 5,
    "admissionCount": 120,
    "minScore": 680,
    "minRank": 500,
    "avgScore": 695.5,
    "avgRank": 350,
    "maxScore": 710,
    "maxRank": 200,
    "isDeleted": false,
    "createdAt": "2026-05-09T10:00:00+08:00",
    "updatedAt": "2026-05-09T10:00:00+08:00"
  },
  "timestamp": 1715241600000
}
```

### 1.3 新增专业组

**请求**
```
POST /api/v1/admin/algorithm/admission/group
Content-Type: application/json
```

**Body参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| universityName | String | 是 | 大学名称（必须是系统中已存在的大学） |
| year | Short | 是 | 年份 |
| province | String | 是 | 省份 |
| batch | String | 是 | 批次：本科批/提前批/专科批 |
| enrollmentCode | String | 否 | 省招代码 |
| groupCode | String | 是 | 专业组代码 |
| groupName | String | 否 | 专业组名称 |
| subjects | String[] | 否 | 科目列表（如：["物理", "化学"]） |
| requirementType | String | 否 | 选科类型：不限/2选1/3选1/必选1/必选2/必选3 |
| description | String | 否 | 专业组简介 |
| constraints | String[] | 否 | 约束条件列表 |

**唯一约束**：(universityId, year, province, batch, groupCode)

> 注：接口传入 `universityName`，后端自动查询对应的 `universityId` 并存储

**请求示例**
```json
{
  "universityName": "清华大学",
  "year": 2025,
  "province": "北京",
  "batch": "本科批",
  "enrollmentCode": "10001",
  "groupCode": "01",
  "groupName": "理工组",
  "subjects": ["物理", "化学"],
  "requirementType": "必选2",
  "description": "理工类专业组",
  "constraints": ["限理科生"]
}
```

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": 1,
  "timestamp": 1715241600000
}
```

### 1.4 修改专业组

**请求**
```
PUT /api/v1/admin/algorithm/admission/group/{id}
Content-Type: application/json
```

**Path参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Integer | 是 | 专业组ID |

**Body参数**：同新增接口

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715241600000
}
```

### 1.5 修改专业组状态（软删除/恢复）

**请求**
```
PUT /api/v1/admin/algorithm/admission/group/{id}/status?isDeleted=true
```

**Path参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Integer | 是 | 专业组ID |

**Query参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| isDeleted | Boolean | 是 | true=软删除，false=恢复 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715241600000
}
```

### 1.6 删除专业组（硬删除）

**请求**
```
DELETE /api/v1/admin/algorithm/admission/group/{id}
```

**说明**：会同时删除该专业组下所有专业明细记录

**Path参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Integer | 是 | 专业组ID |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715241600000
}
```

### 1.7 批量删除专业组

**请求**
```
DELETE /api/v1/admin/algorithm/admission/group/batch
Content-Type: application/json
```

**说明**：会同时删除这些专业组下所有专业明细记录

**Body参数**
```json
[1, 2, 3]
```

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715241600000
}
```

### 1.8 导入专业组数据

**请求**
```
POST /api/v1/admin/algorithm/admission/group/import
Content-Type: multipart/form-data
```

**Form参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | Excel文件(.xlsx) |

**Excel模板列**
| 列名 | 类型 | 必填 | 说明 |
|------|------|------|------|
| 大学名 | String | 是 | 必须是系统中已存在的大学 |
| 年份 | Short | 是 | 如：2025 |
| 省份 | String | 是 | 如：北京 |
| 批次 | String | 是 | 本科批/提前批/专科批 |
| 省招代码 | String | 否 | 如：10001 |
| 专业组代码 | String | 是 | 如：01 |
| 专业组简介 | String | 否 | 专业组描述 |
| 科目 | String | 否 | 如：物理,化学（英文逗号或中文逗号分隔） |
| 选科类型 | String | 否 | 不限/2选1/3选1/必选1/必选2/必选3 |
| 专业代码 | String | 是 | 如：080901 |
| 专业名称 | String | 是 | 如：计算机科学与技术 |
| 层次 | String | 否 | 如：本科 |
| 学费 | String | 否 | 如：5000元/年 |
| 专业简介 | String | 否 | 专业描述 |
| 录取人数 | Integer | 否 | 如：10 |
| 最低分 | Integer | 否 | 如：680 |
| 中位分 | Decimal | 否 | 如：695.5 |
| 最高分 | Integer | 否 | 如：710 |
| 最低位次 | Integer | 否 | 如：500 |
| 中位位次 | Integer | 否 | 如：350 |
| 最高位次 | Integer | 否 | 如：200 |

**导入逻辑**
1. 两次遍历验证：先校验所有数据，再批量插入
2. 自动创建专业组：若专业组不存在则自动创建
3. 重复检测：同一专业组内专业代码不能重复
4. 数据库触发器自动计算聚合字段

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715241600000
}
```

**错误响应示例**
```json
{
  "code": 400,
  "msg": "数据校验失败：第2行: 大学[XX大学]不存在; 第5行: 专业代码[080901]在同一专业组内重复",
  "data": null,
  "timestamp": 1715241600000
}
```

### 1.9 全量重算聚合数据

**请求**
```
POST /api/v1/admin/algorithm/admission/group/recalc-all
```

**说明**：手动触发全量重算所有专业组的聚合字段，用于数据修复场景

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": 150,
  "timestamp": 1715241600000
}
```

**data**：处理的专业组数量

---

## 二、专业明细管理

基础路径：`/api/v1/admin/algorithm/admission/major-score`

### 2.1 分页查询专业明细

**请求**
```
GET /api/v1/admin/algorithm/admission/major-score/page
```

**Query参数**
| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1，最小1 |
| size | Integer | 否 | - | 每页条数，默认10，范围10-1000 |
| groupId | Integer | 否 | 精确匹配 | 专业组ID |
| majorCode | String | 否 | 模糊查询 | 专业代码 |
| majorName | String | 否 | 模糊查询 | 专业名称 |
| educationLevel | String | 否 | 精确匹配 | 层次 |
| isDeleted | Boolean | 否 | 精确匹配 | 是否已删除，默认false |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "groupId": 1,
        "majorCode": "080901",
        "majorName": "计算机科学与技术",
        "educationLevel": "本科",
        "admissionCount": 10,
        "minScore": 685,
        "minRank": 450,
        "avgScore": 690.5,
        "isDeleted": false
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  },
  "timestamp": 1715241600000
}
```

### 2.2 获取专业明细详情

**请求**
```
GET /api/v1/admin/algorithm/admission/major-score/{id}
```

**Path参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Integer | 是 | 专业明细ID |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "groupId": 1,
    "majorId": 10001,
    "majorCode": "080901",
    "majorName": "计算机科学与技术",
    "educationLevel": "本科",
    "duration": "4年",
    "tuition": "5000元/年",
    "description": "专业简介...",
    "admissionCount": 10,
    "minScore": 685,
    "minRank": 450,
    "avgScore": 690.5,
    "avgRank": 400,
    "maxScore": 700,
    "maxRank": 300,
    "constraints": ["色盲色弱不宜"],
    "isDeleted": false,
    "createdAt": "2026-05-09T10:00:00+08:00",
    "updatedAt": "2026-05-09T10:00:00+08:00"
  },
  "timestamp": 1715241600000
}
```

### 2.3 新增专业明细

**请求**
```
POST /api/v1/admin/algorithm/admission/major-score
Content-Type: application/json
```

**Body参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| groupId | Integer | 是 | 专业组ID |
| majorId | Long | 否 | 专业ID（关联t_majors） |
| majorCode | String | 是 | 专业代码 |
| majorName | String | 是 | 专业名称 |
| educationLevel | String | 否 | 层次 |
| duration | String | 否 | 学制 |
| tuition | String | 否 | 学费 |
| description | String | 否 | 专业简介 |
| admissionCount | Integer | 否 | 录取人数 |
| minScore | Integer | 否 | 最低分 |
| minRank | Integer | 否 | 最低位次 |
| avgScore | BigDecimal | 否 | 中位分 |
| avgRank | Integer | 否 | 中位位次 |
| maxScore | Integer | 否 | 最高分 |
| maxRank | Integer | 否 | 最高位次 |
| constraints | String[] | 否 | 约束条件列表 |

**唯一约束**：(groupId, majorCode)

**请求示例**
```json
{
  "groupId": 1,
  "majorCode": "080901",
  "majorName": "计算机科学与技术",
  "educationLevel": "本科",
  "duration": "4年",
  "tuition": "5000元/年",
  "admissionCount": 10,
  "minScore": 685,
  "minRank": 450,
  "avgScore": 690.5,
  "avgRank": 400,
  "maxScore": 700,
  "maxRank": 300,
  "constraints": ["色盲色弱不宜"]
}
```

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": 1,
  "timestamp": 1715241600000
}
```

### 2.4 修改专业明细

**请求**
```
PUT /api/v1/admin/algorithm/admission/major-score/{id}
Content-Type: application/json
```

**Path参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Integer | 是 | 专业明细ID |

**Body参数**：同新增接口

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715241600000
}
```

### 2.5 修改专业明细状态（软删除/恢复）

**请求**
```
PUT /api/v1/admin/algorithm/admission/major-score/{id}/status?isDeleted=true
```

**说明**：用于"禁用 / 启用"专业明细。软删除后该明细不会出现在 App 端志愿查询结果中，可随时恢复。

**Path参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Integer | 是 | 专业明细ID |

**Query参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| isDeleted | Boolean | 是 | true=软删除（禁用），false=恢复（启用） |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715241600000
}
```

### 2.6 删除专业明细（硬删除）

**请求**
```
DELETE /api/v1/admin/algorithm/admission/major-score/{id}
```

**说明**：物理删除，无法恢复。删除后会触发数据库触发器自动更新所属专业组的聚合字段。如只需临时下架，请使用 2.5 软删除接口。

**Path参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Integer | 是 | 专业明细ID |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715241600000
}
```

### 2.7 批量删除专业明细（硬删除）

**请求**
```
DELETE /api/v1/admin/algorithm/admission/major-score/batch
Content-Type: application/json
```

**说明**：批量物理删除，无法恢复。

**Body参数**
```json
[1, 2, 3]
```

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715241600000
}
```

---

## 三、错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

**常见业务错误**
- `该专业组已存在`：专业组业务键唯一约束冲突
- `该专业代码在该专业组内已存在`：专业明细唯一约束冲突
- `专业组不存在`：关联的groupId无效
- `大学[XX大学]不存在或已禁用`：universityName无效
- `大学[XX大学]不存在`：Excel导入时大学名称无法匹配

---

## 四、数据库设计

### 4.1 表结构

| 表名 | 说明 |
|------|------|
| t_admission_group | 专业组汇总表 |
| t_admission_major_score | 专业明细表 |

### 4.2 选科要求字段

**t_admission_group表**
- `subjects`：科目数组，如 `{物理,化学}`
- `requirement_type`：选科类型枚举值

**枚举值说明**
| requirementType | 含义 | 匹配规则 |
|-----------------|------|----------|
| 不限 | 任意选科均可报考 | 无限制 |
| 2选1 | 两门科目任选其一 | 用户选科与subjects有交集 |
| 3选1 | 三门科目任选其一 | 用户选科与subjects有交集 |
| 必选1 | 单科必选 | 用户选科包含subjects中的科目 |
| 必选2 | 两科均须选 | 用户选科包含subjects中所有科目 |
| 必选3 | 三科均须选 | 用户选科包含subjects中所有科目 |

### 4.3 冗余字段

**t_admission_group表**
- `university_name`：大学名称（冗余字段，与 `university_id` 同步存储）
- `city_name`：城市名称（冗余字段，通过大学表自动带出）

> 注：新增/修改时只需传入 `universityName`，后端自动查询大学表获取 `university_id`、`university_name`、`city_name` 三个字段

### 4.4 自动聚合字段

以下字段由数据库触发器自动计算，**接口不接受传值**：

**t_admission_group表**
- `major_count`：专业数量
- `category_count`：专业门类数量
- `admission_count`：总录取人数
- `min_score` / `min_rank`：最低分/位次
- `avg_score` / `avg_rank`：平均分/位次
- `max_score` / `max_rank`：最高分/位次

**触发时机**
- 新增专业明细时
- 修改专业明细时
- 删除专业明细时
- 调用`recalc-all`接口时
