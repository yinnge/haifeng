# 算法约束模块

## 模块概述

本模块实现高考志愿填报算法所需的约束条件管理，属于「算法约束模块」父模块，包含三个子模块：

| 子模块 | 说明 |
|--------|------|
| 约束字典 | 管理各类报考约束条件（如色盲限制、身高要求、语种要求等） |
| 专业约束关联 | 管理专业与约束条件的多对多关联关系，支持Excel批量导入 |
| 安全系数 | 管理志愿填报安全系数等级字典（冲/稳/保/垫等） |

## 数据表

### 1. t_constraint_dict（约束条件字典表）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| code | VARCHAR(50) | 是 | 主键，约束代码（唯一标识） |
| name | VARCHAR(100) | 是 | 约束名称（唯一） |
| category | VARCHAR(30) | 是 | 约束大类（身体视觉/身体指标/语种限制等） |
| description | TEXT | 否 | 详细说明 |
| severity | VARCHAR(10) | 是 | 严重程度：HARD（硬限制）/ SOFT（软提示），默认HARD |
| check_field | VARCHAR(50) | 否 | 对应t_member_gaokao表的字段 |
| check_operator | VARCHAR(20) | 否 | 判断运算符（EQ/NEQ/LT/LTE/GT/GTE/IS_TRUE/IS_FALSE/IN/NOT_IN） |
| check_value | VARCHAR(100) | 否 | 判断值 |
| extra_field | VARCHAR(50) | 否 | 附加条件字段 |
| extra_operator | VARCHAR(20) | 否 | 附加条件运算符 |
| extra_value | VARCHAR(100) | 否 | 附加条件值 |
| sort_order | INTEGER | 否 | 排序值，默认0 |
| is_active | BOOLEAN | 是 | 是否启用，默认true |
| created_at | TIMESTAMPTZ | 是 | 创建时间 |
| updated_at | TIMESTAMPTZ | 是 | 更新时间 |

### 2. t_major_constraint（专业约束关联表）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | 是 | 主键（雪花算法） |
| major_code | VARCHAR(20) | 是 | 专业代码 → t_major.major_code |
| major_name | VARCHAR(50) | 是 | 专业名称 → t_major.major_name |
| constraint_code | VARCHAR(50) | 是 | 约束代码 → t_constraint_dict.code |
| constraint_name | VARCHAR(50) | 是 | 约束名称 → t_constraint_dict.name |
| remark | VARCHAR(200) | 否 | 备注 |
| created_at | TIMESTAMPTZ | 是 | 创建时间 |

**唯一约束**：major_code + constraint_code

### 3. t_safety_level_dict（安全系数等级字典）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| level | SMALLINT | 是 | 主键，等级编号（1-10） |
| code | VARCHAR(20) | 是 | 代码（唯一） |
| name | VARCHAR(30) | 是 | 中文名称 |
| name_short | VARCHAR(10) | 是 | 简称（前端标签用） |
| min_coefficient | NUMERIC(3,2) | 是 | 系数下界（含），0.00-1.00 |
| max_coefficient | NUMERIC(3,2) | 是 | 系数上界（不含），0.00-1.00 |
| color | VARCHAR(20) | 否 | 前端显示颜色 |
| confidence | VARCHAR(20) | 否 | 置信度（HIGH/MEDIUM/LOW） |
| confidence_reason | VARCHAR(150) | 否 | 置信度说明 |
| description | TEXT | 否 | 说明 |

**约束**：min_coefficient < max_coefficient

---

## 一、约束字典接口

基础路径：`/api/v1/admin/algorithm/constraint/dict`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page` | 分页查询列表 |
| GET | `/{code}` | 获取详情 |
| POST | `/` | 新增约束 |
| PUT | `/{code}` | 修改约束 |
| PUT | `/{code}/toggle` | 切换启用状态 |
| DELETE | `/{code}` | 删除约束（硬删除） |
| DELETE | `/batch` | 批量删除（硬删除） |

### 1.1 分页查询列表

**请求**
```
GET /api/v1/admin/algorithm/constraint/dict/page?page=1&size=10
```

**查询参数**
| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页数量，默认10，可选：10/20/30/50/100/200/500/1000 |

> **注意**：此接口无查询条件，返回全部约束字典

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "code": "NO_COLOR_BLIND",
        "category": "身体视觉",
        "severity": "HARD",
        "checkField": "is_color_blind",
        "isActive": true
      },
      {
        "code": "HEIGHT_MIN_170",
        "category": "身体指标",
        "severity": "HARD",
        "checkField": "height_cm",
        "isActive": true
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
| code | String | 约束代码（主键） |
| category | String | 约束大类 |
| severity | String | 严重程度（HARD/SOFT） |
| checkField | String \| null | 检查字段 |
| isActive | Boolean | 是否启用 |

### 1.2 获取详情

**请求**
```
GET /api/v1/admin/algorithm/constraint/dict/{code}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| code | String | 是 | 约束代码 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "code": "NO_COLOR_BLIND",
    "name": "不招色盲",
    "category": "身体视觉",
    "description": "色盲考生不能报考该专业",
    "severity": "HARD",
    "checkField": "is_color_blind",
    "checkOperator": "IS_TRUE",
    "checkValue": null,
    "extraField": null,
    "extraOperator": null,
    "extraValue": null,
    "sortOrder": 0,
    "isActive": true,
    "createdAt": "2026-05-11T10:00:00+08:00",
    "updatedAt": "2026-05-11T10:00:00+08:00"
  },
  "timestamp": 1715500800000
}
```

**响应字段类型**
| 字段 | 类型 | 说明 |
|------|------|------|
| code | String | 约束代码 |
| name | String | 约束名称 |
| category | String | 约束大类 |
| description | String \| null | 详细说明 |
| severity | String | 严重程度（HARD/SOFT） |
| checkField | String \| null | 检查字段（对应t_member_gaokao字段） |
| checkOperator | String \| null | 检查运算符 |
| checkValue | String \| null | 检查值 |
| extraField | String \| null | 附加条件字段 |
| extraOperator | String \| null | 附加条件运算符 |
| extraValue | String \| null | 附加条件值 |
| sortOrder | Integer | 排序值 |
| isActive | Boolean | 是否启用 |
| createdAt | String | 创建时间（ISO 8601格式） |
| updatedAt | String | 更新时间（ISO 8601格式） |

### 1.3 新增约束

**请求**
```
POST /api/v1/admin/algorithm/constraint/dict
Content-Type: application/json

{
  "code": "NO_COLOR_BLIND",
  "name": "不招色盲",
  "category": "身体视觉",
  "description": "色盲考生不能报考该专业",
  "severity": "HARD",
  "checkField": "is_color_blind",
  "checkOperator": "IS_TRUE",
  "checkValue": null,
  "extraField": null,
  "extraOperator": null,
  "extraValue": null,
  "sortOrder": 0,
  "isActive": true
}
```

**请求体参数**
| 参数 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| code | String | 是 | 最大50字符，唯一 | 约束代码 |
| name | String | 是 | 最大100字符，唯一 | 约束名称 |
| category | String | 是 | 最大30字符 | 约束大类 |
| description | String | 否 | - | 详细说明 |
| severity | String | 否 | 只能是HARD或SOFT | 严重程度，默认HARD |
| checkField | String | 否 | 最大50字符 | 检查字段 |
| checkOperator | String | 否 | 最大20字符 | 检查运算符 |
| checkValue | String | 否 | 最大100字符 | 检查值 |
| extraField | String | 否 | 最大50字符 | 附加条件字段 |
| extraOperator | String | 否 | 最大20字符 | 附加条件运算符 |
| extraValue | String | 否 | 最大100字符 | 附加条件值 |
| sortOrder | Integer | 否 | - | 排序值，默认0 |
| isActive | Boolean | 否 | - | 是否启用，默认true |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

**错误响应**
```json
{
  "code": 400,
  "msg": "该约束代码已存在",
  "data": null,
  "timestamp": 1715500800000
}
```

### 1.4 修改约束

**请求**
```
PUT /api/v1/admin/algorithm/constraint/dict/{code}
Content-Type: application/json

{
  "code": "NO_COLOR_BLIND",
  "name": "不招色盲（更新）",
  "category": "身体视觉",
  "description": "色盲考生不能报考该专业（已更新说明）",
  "severity": "HARD",
  "checkField": "is_color_blind",
  "checkOperator": "IS_TRUE",
  "checkValue": null,
  "extraField": null,
  "extraOperator": null,
  "extraValue": null,
  "sortOrder": 1,
  "isActive": true
}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| code | String | 是 | 约束代码 |

**请求体参数**：同新增

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

### 1.5 切换启用状态

**请求**
```
PUT /api/v1/admin/algorithm/constraint/dict/{code}/toggle
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| code | String | 是 | 约束代码 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

### 1.6 删除约束

**请求**
```
DELETE /api/v1/admin/algorithm/constraint/dict/{code}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| code | String | 是 | 约束代码 |

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
DELETE /api/v1/admin/algorithm/constraint/dict/batch
Content-Type: application/json

["NO_COLOR_BLIND", "NO_COLOR_WEAK", "HEIGHT_MIN_170"]
```

**请求体**
| 类型 | 说明 |
|------|------|
| Array\<String\> | 约束代码数组 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

---

## 二、专业约束关联接口

基础路径：`/api/v1/admin/algorithm/constraint/major`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page` | 分页查询列表 |
| GET | `/{id}` | 获取详情 |
| POST | `/` | 新增关联 |
| DELETE | `/{id}` | 删除关联（硬删除） |
| DELETE | `/batch` | 批量删除（硬删除） |
| POST | `/import` | Excel批量导入 |

### 2.1 分页查询列表

**请求**
```
GET /api/v1/admin/algorithm/constraint/major/page?page=1&size=10&majorCode=080901&majorName=计算机&constraintCode=NO_COLOR_BLIND&constraintName=不招色盲
```

**查询参数**
| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页数量，默认10，可选：10/20/30/50/100/200/500/1000 |
| majorCode | String | 否 | **精确查询** | 专业代码 |
| majorName | String | 否 | **精确查询** | 专业名称 |
| constraintCode | String | 否 | **精确查询** | 约束代码 |
| constraintName | String | 否 | **精确查询** | 约束名称 |

> **注意**：所有查询条件均为**精确匹配**，不支持模糊查询

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": "1893000000000001",
        "majorCode": "100201",
        "majorName": "临床医学",
        "constraintCode": "NO_COLOR_BLIND_WEAK",
        "constraintName": "不招色盲色弱"
      },
      {
        "id": "1893000000000002",
        "majorCode": "100201",
        "majorName": "临床医学",
        "constraintCode": "NO_SMELL_DISORDER",
        "constraintName": "不招嗅觉迟钝"
      }
    ],
    "total": 500,
    "size": 10,
    "current": 1,
    "pages": 50
  },
  "timestamp": 1715500800000
}
```

**响应字段类型**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 主键ID（雪花算法，前端用字符串避免精度丢失） |
| majorCode | String | 专业代码 |
| majorName | String | 专业名称 |
| constraintCode | String | 约束代码 |
| constraintName | String | 约束名称 |

### 2.2 获取详情

**请求**
```
GET /api/v1/admin/algorithm/constraint/major/{id}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1893000000000001",
    "majorCode": "100201",
    "majorName": "临床医学",
    "constraintCode": "NO_COLOR_BLIND_WEAK",
    "constraintName": "不招色盲色弱",
    "remark": "体检标准要求",
    "createdAt": "2026-05-11T10:00:00+08:00"
  },
  "timestamp": 1715500800000
}
```

**响应字段类型**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 主键ID |
| majorCode | String | 专业代码 |
| majorName | String | 专业名称 |
| constraintCode | String | 约束代码 |
| constraintName | String | 约束名称 |
| remark | String \| null | 备注 |
| createdAt | String | 创建时间（ISO 8601格式） |

### 2.3 新增关联

**请求**
```
POST /api/v1/admin/algorithm/constraint/major
Content-Type: application/json

{
  "majorName": "临床医学",
  "constraintName": "不招色盲色弱",
  "remark": "体检标准要求"
}
```

**请求体参数**
| 参数 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| majorName | String | 是 | 最大100字符 | 专业名称（系统自动查找对应专业代码） |
| constraintName | String | 是 | 最大100字符 | 约束名称（系统自动查找对应约束代码） |
| remark | String | 否 | 最大200字符 | 备注 |

> **注意**：系统会根据专业名称自动查找 t_major.major_code，根据约束名称自动查找 t_constraint_dict.code

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": "1893000000000003",
  "timestamp": 1715500800000
}
```

**错误响应**
```json
{
  "code": 400,
  "msg": "专业不存在：临床医学ABC",
  "data": null,
  "timestamp": 1715500800000
}
```

```json
{
  "code": 400,
  "msg": "约束不存在：不招色盲XXX",
  "data": null,
  "timestamp": 1715500800000
}
```

```json
{
  "code": 400,
  "msg": "该专业已存在该约束关联",
  "data": null,
  "timestamp": 1715500800000
}
```

### 2.4 删除关联

**请求**
```
DELETE /api/v1/admin/algorithm/constraint/major/{id}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 记录ID |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

### 2.5 批量删除

**请求**
```
DELETE /api/v1/admin/algorithm/constraint/major/batch
Content-Type: application/json

[1893000000000001, 1893000000000002]
```

**请求体**
| 类型 | 说明 |
|------|------|
| Array\<Long\> | ID数组 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

### 2.6 Excel批量导入

**请求**
```
POST /api/v1/admin/algorithm/constraint/major/import
Content-Type: multipart/form-data

file: [Excel文件]
```

**Excel模板列**
| 列名 | 类型 | 必填 | 说明 |
|------|------|------|------|
| 专业名称 | String | 是 | 专业名称（系统自动查找专业代码） |
| 约束名称 | String | 是 | 约束名称（系统自动查找约束代码） |
| 备注 | String | 否 | 备注说明 |

**Excel示例**
| 专业名称 | 约束名称 | 备注 |
|----------|----------|------|
| 临床医学 | 不招色盲色弱 | 体检标准要求 |
| 临床医学 | 不招嗅觉迟钝 | 临床诊断需要嗅觉 |
| 护理学 | 不招色盲 | |
| 播音与主持艺术 | 不招面部疤痕 | 镜前形象要求 |

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
  "msg": "数据校验失败：第2行: 专业不存在（专业名称=临床医学ABC）; 第5行: 约束不存在（约束名称=不招XXX）; 第8行: 数据库已存在该关联（专业=临床医学, 约束=不招色盲色弱）",
  "data": null,
  "timestamp": 1715500800000
}
```

**导入规则**：
1. 所有必填字段不能为空
2. 专业名称必须在 t_major 表中存在
3. 约束名称必须在 t_constraint_dict 表中存在
4. 同一专业不能重复关联同一约束（major_code + constraint_code 唯一）
5. Excel内不能有重复记录
6. 任何错误都会导致整个导入回滚

---

## 三、安全系数接口

基础路径：`/api/v1/admin/algorithm/constraint/safety-level`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/page` | 分页查询列表 |
| GET | `/{level}` | 获取详情 |
| POST | `/` | 新增等级 |
| PUT | `/{level}` | 修改等级 |
| DELETE | `/{level}` | 删除等级（硬删除） |
| DELETE | `/batch` | 批量删除（硬删除） |

### 3.1 分页查询列表

**请求**
```
GET /api/v1/admin/algorithm/constraint/safety-level/page?page=1&size=10
```

**查询参数**
| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认1 |
| size | Integer | 否 | - | 每页数量，默认10，可选：10/20/30/50/100/200/500/1000 |

> **注意**：此接口无查询条件，返回全部安全系数等级

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "level": 1,
        "code": "REACH_HIGH",
        "name": "大胆冲刺",
        "nameShort": "搏",
        "minCoefficient": 0.00,
        "maxCoefficient": 0.30,
        "confidence": "LOW"
      },
      {
        "level": 2,
        "code": "REACH",
        "name": "可以冲击",
        "nameShort": "冲",
        "minCoefficient": 0.30,
        "maxCoefficient": 0.50,
        "confidence": "LOW"
      },
      {
        "level": 3,
        "code": "MATCH",
        "name": "较为稳妥",
        "nameShort": "稳",
        "minCoefficient": 0.50,
        "maxCoefficient": 0.70,
        "confidence": "MEDIUM"
      },
      {
        "level": 4,
        "code": "SAFE",
        "name": "比较安全",
        "nameShort": "保",
        "minCoefficient": 0.70,
        "maxCoefficient": 0.85,
        "confidence": "HIGH"
      },
      {
        "level": 5,
        "code": "FLOOR",
        "name": "高度保底",
        "nameShort": "垫",
        "minCoefficient": 0.85,
        "maxCoefficient": 1.00,
        "confidence": "HIGH"
      }
    ],
    "total": 5,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1715500800000
}
```

**响应字段类型**
| 字段 | 类型 | 说明 |
|------|------|------|
| level | Integer | 等级编号（主键） |
| code | String | 代码 |
| name | String | 名称 |
| nameShort | String | 简称 |
| minCoefficient | BigDecimal | 系数下界 |
| maxCoefficient | BigDecimal | 系数上界 |
| confidence | String \| null | 置信度 |

### 3.2 获取详情

**请求**
```
GET /api/v1/admin/algorithm/constraint/safety-level/{level}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| level | Short | 是 | 等级编号 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "level": 1,
    "code": "REACH_HIGH",
    "name": "大胆冲刺",
    "nameShort": "搏",
    "minCoefficient": 0.00,
    "maxCoefficient": 0.30,
    "color": "#FF4D4F",
    "confidence": "LOW",
    "confidenceReason": "录取概率极低，数据支撑有限",
    "description": "录取概率极低，属于"彩票"志愿。历年数据显示您的位次远低于该校录取位次。建议最多填1-2个冲刺志愿，且必须搭配稳妥志愿。"
  },
  "timestamp": 1715500800000
}
```

**响应字段类型**
| 字段 | 类型 | 说明 |
|------|------|------|
| level | Integer | 等级编号 |
| code | String | 代码 |
| name | String | 名称 |
| nameShort | String | 简称 |
| minCoefficient | BigDecimal | 系数下界 |
| maxCoefficient | BigDecimal | 系数上界 |
| color | String \| null | 前端显示颜色 |
| confidence | String \| null | 置信度（HIGH/MEDIUM/LOW） |
| confidenceReason | String \| null | 置信度说明 |
| description | String \| null | 详细说明 |

### 3.3 新增等级

**请求**
```
POST /api/v1/admin/algorithm/constraint/safety-level
Content-Type: application/json

{
  "level": 6,
  "code": "SUPER_FLOOR",
  "name": "绝对保底",
  "nameShort": "底",
  "minCoefficient": 0.95,
  "maxCoefficient": 1.00,
  "color": "#1890FF",
  "confidence": "HIGH",
  "confidenceReason": "几乎100%能录取",
  "description": "录取概率几乎100%，用于极端保底"
}
```

**请求体参数**
| 参数 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| level | Short | 是 | 1-10，唯一 | 等级编号 |
| code | String | 是 | 最大20字符，唯一 | 代码 |
| name | String | 是 | 最大30字符 | 名称 |
| nameShort | String | 是 | 最大10字符 | 简称 |
| minCoefficient | BigDecimal | 是 | 0.00-1.00 | 系数下界 |
| maxCoefficient | BigDecimal | 是 | 0.00-1.00，必须大于minCoefficient | 系数上界 |
| color | String | 否 | 最大20字符 | 颜色 |
| confidence | String | 否 | 最大20字符 | 置信度 |
| confidenceReason | String | 否 | 最大150字符 | 置信度说明 |
| description | String | 否 | - | 详细说明 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

**错误响应**
```json
{
  "code": 400,
  "msg": "该等级已存在",
  "data": null,
  "timestamp": 1715500800000
}
```

```json
{
  "code": 400,
  "msg": "该代码已存在",
  "data": null,
  "timestamp": 1715500800000
}
```

### 3.4 修改等级

**请求**
```
PUT /api/v1/admin/algorithm/constraint/safety-level/{level}
Content-Type: application/json

{
  "level": 1,
  "code": "REACH_HIGH",
  "name": "大胆冲刺（更新）",
  "nameShort": "搏",
  "minCoefficient": 0.00,
  "maxCoefficient": 0.25,
  "color": "#FF4D4F",
  "confidence": "LOW",
  "confidenceReason": "录取概率极低",
  "description": "更新后的说明"
}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| level | Short | 是 | 等级编号 |

**请求体参数**：同新增

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

### 3.5 删除等级

**请求**
```
DELETE /api/v1/admin/algorithm/constraint/safety-level/{level}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| level | Short | 是 | 等级编号 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

### 3.6 批量删除

**请求**
```
DELETE /api/v1/admin/algorithm/constraint/safety-level/batch
Content-Type: application/json

{
  "levels": [6, 7, 8]
}
```

**请求体参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| levels | Array\<Short\> | 是 | 等级编号数组 |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715500800000
}
```

---

## 四、错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 / 数据校验失败 / 数据已存在 / 外键不存在 |
| 404 | 记录不存在 |
| 401 | 未登录 / Token过期 |
| 403 | 无权限 |
| 500 | 服务器内部错误 |

---

## 五、字段类型规范

为确保前后端数据一致性，所有接口遵循以下类型规范：

| 后端类型 | 前端类型 | 说明 |
|----------|----------|------|
| Long（ID） | String | 避免JavaScript精度丢失 |
| Short/Integer | Number | 数值类型 |
| BigDecimal | Number | 小数类型，如0.85 |
| String | String | 字符串 |
| Boolean | Boolean | 布尔值 |
| OffsetDateTime | String | ISO 8601格式，如 `2026-05-11T10:00:00+08:00` |
| null | null | 空值保持null，不转为空字符串 |

> **重要**：专业约束关联的 `id` 字段返回为**字符串类型**，前端请求时也应传字符串。约束字典和安全系数使用 `code` / `level` 作为主键，无需担心精度问题。

---

## 六、check_field 可选值参考

约束字典的 `checkField` 字段对应 `t_member_gaokao` 表的字段，可选值包括：

### 选科信息
| 字段 | 说明 |
|------|------|
| subject_type | 首选科目 |
| second_subject_type | 第二科目 |
| third_subject_type | 第三科目 |

### 各科成绩
| 字段 | 说明 |
|------|------|
| score_chinese | 语文成绩 |
| score_math | 数学成绩 |
| score_english | 外语成绩 |
| score_subject_1 | 首选科目分数 |
| score_subject_2 | 再选科目1分数 |
| score_subject_3 | 再选科目2分数 |

### 外语语种
| 字段 | 说明 |
|------|------|
| foreign_language | 外语语种（英语/日语/俄语/德语/法语/西班牙语/其他） |

### 身体视觉条件
| 字段 | 说明 |
|------|------|
| is_color_blind | 是否色盲 |
| is_color_weak | 是否色弱 |
| vision_left | 左眼裸眼视力 |
| vision_right | 右眼裸眼视力 |
| has_smell_disorder | 是否嗅觉迟钝 |

### 身体指标
| 字段 | 说明 |
|------|------|
| height_cm | 身高（厘米） |
| weight_kg | 体重（公斤） |
| is_left_handed | 是否左利手 |
| has_tattoo | 是否有纹身 |
| has_scar | 是否有面部明显疤痕 |
| has_stutter | 是否口吃 |

### 身份条件
| 字段 | 说明 |
|------|------|
| is_fresh_graduate | 是否应届生 |
| political_status | 政治面貌（群众/共青团员/中共党员） |
| household_type | 户籍类型（城镇/农村） |
| is_poverty_county | 是否国家级贫困县户籍 |
