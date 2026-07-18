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

> 注：已删除（软删除）的专业组不可修改，返回404

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
  "code": 404,
  "msg": "专业组不存在",
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

### 1.6 删除专业组（软删除）

**请求**
```
DELETE /api/v1/admin/algorithm/admission/group/{id}
```

**说明**：软删除操作，设置 `isDeleted=true`，同时级联软删除该专业组下所有专业明细。可通过 1.5 状态接口恢复。删除已删除的记录返回 404。

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

**错误响应示例**
```json
{
  "code": 404,
  "msg": "专业组不存在",
  "data": null,
  "timestamp": 1715241600000
}
```

### 1.7 批量删除专业组（软删除）

**请求**
```
POST /api/v1/admin/algorithm/admission/group/batch
Content-Type: application/json
```

**说明**：批量软删除，设置 `isDeleted=true`，同时级联软删除这些专业组下所有专业明细。仅删除 `isDeleted=false` 的记录，已删除的记录会被跳过。

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
| file | File | 是 | Excel文件(.xlsx或.xls) |

#### Excel 模板列说明

| 列名 | 字段 | 类型 | 必填 | DB列名 / DB类型 | 说明 |
|------|------|------|------|------------------|------|
| 大学名 | universityName | String | 是 | university_name VARCHAR(50) NOT NULL | 须与 t_university.name 精确匹配（trim后），不存在则报错 |
| 年份 | year | Short | 是 | year SMALLINT NOT NULL | 整数，范围 2000-2100 |
| 省份 | province | String | 是 | province VARCHAR(20) NOT NULL | 枚举：31个标准省市名之一（精确匹配），见下方约束详解 |
| 批次 | batch | String | 是 | batch VARCHAR(50) NOT NULL | 枚举：本科批/提前批/专科批 |
| 省招代码 | enrollmentCode | String | 否 | enrollment_code VARCHAR(30) | 自由文本，最长30字符 |
| 专业组代码 | groupCode | String | 是 | group_code VARCHAR(30) NOT NULL | 自由文本，最长30字符；同大学+年份+省份+批次下唯一 |
| 专业组名称 | groupName | String | 否 | group_name VARCHAR(100) | 自由文本，最长100字符；为空时使用专业组代码 |
| 专业组简介 | groupDescription | String | 否 | description TEXT | 自由文本，最长2000字符 |
| 科目 | subjectsStr | String | 否 | subjects TEXT[] | 逗号分隔（支持中英文逗号），总长200字符；每个值必须精确匹配6科之一，见下方约束详解 |
| 选科类型 | requirementType | String | 否 | requirement_type VARCHAR(10) | 枚举：不限/2选1/3选1/必选1/必选2/必选3；为空默认"不限"；DB有CHECK约束；须与科目数量一致 |
| 专业代码 | majorCode | String | 是 | major_code VARCHAR(20) NOT NULL | 自由文本，最长20字符；同专业组内唯一（含DB已有记录） |
| 专业名称 | majorName | String | 是 | major_name VARCHAR(100) NOT NULL | 自由文本，最长100字符 |
| 层次 | educationLevel | String | 否 | education_level VARCHAR(20) | 枚举：本科/专科；为空则存null |
| 学制 | duration | String | 否 | duration VARCHAR(20) | 自由文本，最长20字符，如"四年"、"3年" |
| 学费 | tuition | String | 否 | tuition VARCHAR(50) | 自由文本，最长50字符，如"5000元/年"、"免学费" |
| 专业简介 | majorDescription | String | 否 | description TEXT | 自由文本，最长2000字符 |
| 报考限制条件 | constraintsStr | String | 否 | constraints TEXT[] | 逗号分隔（支持中英文逗号），总长500字符；无枚举限制，如"色盲限报,左眼≥4.0" |
| 录取人数 | admissionCount | Integer | 否 | admission_count INTEGER | 非负整数，0-99999 |
| 最低分 | minScore | Integer | 否 | min_score INTEGER | 非负整数，0-900；须 ≤ 中位分 ≤ 最高分 |
| 中位分 | avgScore | BigDecimal | 否 | avg_score NUMERIC(6,2) | 非负小数，0-9999.99（DB NUMERIC(6,2)）；须 ≥ 最低分 且 ≤ 最高分 |
| 最高分 | maxScore | Integer | 否 | max_score INTEGER | 非负整数，0-900；须 ≥ 中位分 |
| 最低位次 | minRank | Integer | 否 | min_rank INTEGER | 非负整数，0-9999999；须 ≤ 中位位次 ≤ 最高位次 |
| 中位位次 | avgRank | Integer | 否 | avg_rank INTEGER | 非负整数，0-9999999；须 ≥ 最低位次 且 ≤ 最高位次 |
| 最高位次 | maxRank | Integer | 否 | max_rank INTEGER | 非负整数，0-9999999；须 ≥ 中位位次 |

> **注意**：第一行为表头，必须与上述列名完全一致。

#### Excel 示例

以下示例展示2个专业组共4行数据。同一专业组的多行中，专业组字段保持一致，专业明细字段不同：

| 大学名 | 年份 | 省份 | 批次 | 省招代码 | 专业组代码 | 专业组名称 | 专业组简介 | 科目 | 选科类型 | 专业代码 | 专业名称 | 层次 | 学制 | 学费 | 专业简介 | 报考限制条件 | 录取人数 | 最低分 | 中位分 | 最高分 | 最低位次 | 中位位次 | 最高位次 |
|--------|------|------|------|----------|------------|------------|------------|------|----------|----------|----------|------|------|------|----------|--------------|----------|--------|--------|--------|----------|----------|----------|
| 清华大学 | 2025 | 北京 | 本科批 | 10001 | 01 | 理工组 | 理工类专业组 | 物理,化学 | 必选2 | 080901 | 计算机科学与技术 | 本科 | 四年 | 5000元/年 | 计算机类 | 色盲限报 | 10 | 680 | 695.5 | 710 | 500 | 350 | 200 |
| 清华大学 | 2025 | 北京 | 本科批 | 10001 | 01 | 理工组 | 理工类专业组 | 物理,化学 | 必选2 | 080902 | 软件工程 | 本科 | 四年 | 5000元/年 | 软件类 | | 8 | 675 | 690.0 | 705 | 550 | 400 | 250 |
| 清华大学 | 2025 | 北京 | 本科批 | 10001 | 02 | 文史组 | 文史类专业组 | 历史 | 必选1 | 030101 | 法学 | 本科 | 四年 | 5000元/年 | 法学类 | | 5 | 660 | 670.0 | 680 | 600 | 500 | 400 |
| 清华大学 | 2025 | 北京 | 本科批 | 10001 | 02 | 文史组 | 文史类专业组 | 历史 | 必选1 | 050101 | 汉语言文学 | 本科 | 四年 | 5000元/年 | 中国语言文学类 | | 5 | 655 | 665.0 | 675 | 650 | 550 | 450 |

#### 字段取值约束详解

**枚举值字段**

| 字段 | 合法值 | 校验方式 | 为空处理 |
|------|--------|----------|----------|
| 省份 | 北京、天津、河北、山西、内蒙古、辽宁、吉林、黑龙江、上海、江苏、浙江、安徽、福建、江西、山东、河南、湖北、湖南、广东、广西、海南、重庆、四川、贵州、云南、西藏、陕西、甘肃、青海、宁夏、新疆 | 代码枚举校验（31个标准省市，不含港澳台） | 必填，为空报错 |
| 批次 | `本科批`、`提前批`、`专科批` | 代码枚举校验 | 必填，为空报错 |
| 选科类型 | `不限`、`2选1`、`3选1`、`必选1`、`必选2`、`必选3` | 代码枚举校验 + DB CHECK约束（`chk_req_type`） | 为空默认"不限" |
| 科目 | `物理`、`化学`、`生物`、`历史`、`地理`、`政治` | 代码逐个校验每个科目值 | 可空 |
| 层次 | `本科`、`专科` | 代码枚举校验 | 可空，为空存null |

**科目字段说明**

`科目` 字段在 DB 中存储为 PostgreSQL 数组类型 `TEXT[]`，Excel 中以逗号分隔的文本填入。代码处理流程：
1. 按英文逗号 `,` 或中文逗号 `，` 拆分
2. 对每个值执行 `trim()` 去除首尾空格
3. 逐个校验是否在合法值集合 {物理, 化学, 生物, 历史, 地理, 政治} 内
4. 不合法的值会报错，如填入"科学"会报错

示例：`物理,化学` -> 存储为 `["物理", "化学"]`

**选科类型与科目数量一致性**

| 选科类型 | 需要科目数 | 含义 |
|----------|-----------|------|
| 不限 | 不校验 | 任意选科均可报考 |
| 2选1 | 2 | 两门科目任选其一 |
| 3选1 | 3 | 三门科目任选其一 |
| 必选1 | 1 | 单科必选 |
| 必选2 | 2 | 两科均须选 |
| 必选3 | 3 | 三科均须选 |

> 当选科类型不为"不限"时，科目字段填写的科目数量必须与选科类型要求的数量一致，否则报错。

**数值字段约束**

| 字段 | DB类型 | 代码校验范围 | 逻辑关系 |
|------|--------|-------------|----------|
| 录取人数 | INTEGER | ≥ 0，≤ 99999 | — |
| 最低分 | INTEGER | ≥ 0，≤ 900 | ≤ 中位分 ≤ 最高分 |
| 中位分 | NUMERIC(6,2) | ≥ 0，≤ 9999.99 | ≥ 最低分，≤ 最高分 |
| 最高分 | INTEGER | ≥ 0，≤ 900 | ≥ 中位分 |
| 最低位次 | INTEGER | ≥ 0，≤ 9999999 | ≤ 中位位次 ≤ 最高位次 |
| 中位位次 | INTEGER | ≥ 0，≤ 9999999 | ≥ 最低位次，≤ 最高位次 |
| 最高位次 | INTEGER | ≥ 0，≤ 9999999 | ≥ 中位位次 |

> 分数和位次各有三个字段（最低/中位/最高），三者之间需满足递增关系。任一字段可单独为空，但若同时填写则必须满足逻辑关系。

**唯一性约束（DB层）**

| 约束名 | 表 | 字段组合 | 说明 |
|--------|-----|---------|------|
| uk_admission_group | t_admission_group | university_id + year + province + batch + group_code | 同大学同年同省同批次同组代码唯一 |
| uk_group_major | t_admission_major_score | group_id + major_code | 同专业组内专业代码唯一 |

**DB CHECK 约束**

| 约束名 | 表 | 规则 |
|--------|-----|------|
| chk_req_type | t_admission_group | requirement_type 只允许：不限/2选1/3选1/必选1/必选2/必选3 |

**外键校验**

| 外键关系 | DB约束 | 代码校验 | 说明 |
|----------|--------|----------|------|
| t_admission_group.university_id → t_university.id | 无DB FK | 有（查DB验证大学名存在性） | 大学名须与 t_university.name 精确匹配（trim后） |
| t_admission_major_score.group_id → t_admission_group.id | 有DB FK + ON DELETE CASCADE | 有（代码先查/建group再插明细） | 不可能出现悬空 groupId |
| t_admission_major_score.major_id → t_major.id | 无DB FK | 不校验 | 导入时不设置 majorId（DB允许null） |

#### 校验规则汇总

| 校验类型 | 规则 | 校验层级 | 说明 |
|----------|------|----------|------|
| 必填 | 大学名、年份、省份、批次、专业组代码、专业代码、专业名称 | 代码 | 为空则记录错误 |
| 字段长度 | 参见下方字段长度约束表（对齐 DB 列长度） | 代码 | 超长则记录错误，防止打崩 DB |
| 年份范围 | 2000-2100 | 代码 | 超出范围则记录错误 |
| 省份枚举 | 31个标准省市名 | 代码 | 精确匹配，不合法则报错 |
| 批次枚举 | 本科批/提前批/专科批 | 代码 | 精确匹配，不合法则报错 |
| 选科类型枚举 | 不限/2选1/3选1/必选1/必选2/必选3 | 代码 + DB CHECK | 代码枚举校验 + DB `chk_req_type` 约束 |
| 科目枚举 | 物理/化学/生物/历史/地理/政治 | 代码 | 逐个校验每个科目值 |
| 科目数量 | ≤ 6 | 代码 | 超过则记录错误 |
| 层次枚举 | 本科/专科 | 代码 | 为空则存null |
| 选科一致性 | requirementType 与 subjects 数量匹配 | 代码 | 不限=不校验；2选1/必选2=2个；3选1/必选3=3个；必选1=1个 |
| 数值范围 | 录取人数0-99999、分数0-900、位次0-9999999、中位分0-9999.99 | 代码 | 超限则记录错误 |
| 分数逻辑 | 最低分 ≤ 中位分 ≤ 最高分 | 代码 | 同时填写时须满足递增关系 |
| 位次逻辑 | 最低位次 ≤ 中位位次 ≤ 最高位次 | 代码 | 同时填写时须满足递增关系 |
| 大学存在性 | 大学名需与 t_university.name 匹配 | 代码 | 应用层外键校验 |
| Excel内去重 | 同一专业组内专业代码不能重复 | 代码 | 遍历时用 Set 查重 |
| DB已有冲突 | 专业代码不能与 DB 中该专业组已有记录冲突 | 代码 | 校验阶段查询 DB，冲突则报错（不跳过） |
| 专业组业务键唯一 | university_id+year+province+batch+group_code | DB UK | DB `uk_admission_group` 约束 |
| 文件类型 | .xlsx 或 .xls（含 Content-Type 校验） | 代码 | 后缀+Content-Type 双重校验 |
| 行数上限 | 单次导入不超过1000条 | 代码 | |
| 错误截断 | 错误超过20条时仅展示前20条 + 总数 | 代码 | 格式：`前20条错误; ... 等N条错误` |

**字段长度约束（对齐 DB 列长度）**

| 字段 | DB列 | 最大长度 |
|------|------|---------|
| 大学名 | university_name | 50 |
| 省招代码 | enrollment_code | 30 |
| 专业组代码 | group_code | 30 |
| 专业组名称 | group_name | 100 |
| 专业代码 | major_code | 20 |
| 专业名称 | major_name | 100 |
| 学制 | duration | 20 |
| 学费 | tuition | 50 |
| 专业组简介 | description(group) | 2000 |
| 专业简介 | description(major) | 2000 |
| 科目 | subjectsStr(原始) | 200 |
| 报考限制条件 | constraintsStr(原始) | 500 |

#### 导入逻辑

1. **两次遍历、事务分离**：
   - 第一轮遍历：全量校验所有数据行（在事务外），收集所有错误 —— **绝不 insert**
   - 校验全通过后才进入第二轮（事务内）：全量插入，**不再做任何跳过/校验**，保证"要么全成功，要么全失败"
   - 文件解析与校验在事务外执行，避免长事务持有 DB 连接；仅插入阶段使用编程式事务
2. **自动创建/更新专业组**：
   - 若专业组不存在 → 自动创建，`groupName` 取 Excel 值，为空时用 `groupCode`
   - 若专业组已存在 → 更新组字段（enrollmentCode、groupName、subjects、requirementType、description），并追加新明细
3. **重复检测**：
   - Excel 内同一专业组的专业代码不能重复（遍历时 Set 查重）
   - 与 DB 已有专业代码冲突也已在校验阶段检出（查询 DB 中该组已有 majorCode），冲突则直接报错，**不跳过**
4. **单次导入不能超过1000条记录**
5. **数据库触发器自动计算聚合字段**（专业数量、最低分、平均分等）

#### 响应

```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715241600000
}
```

> 固定返回 `data: null`，导入要么全成功要么全失败（失败返回 400）。

#### 错误响应示例

```json
{
  "code": 400,
  "msg": "数据校验失败：第2行: 大学[XX大学]不存在; 第5行: 专业代码[080901]在同一专业组内重复; ... 等25条错误",
  "data": null,
  "timestamp": 1715241600000
}
```

> 注：当错误超过20条时，仅展示前20条并附总数，格式为 `前20条错误; ... 等N条错误`

**其他错误响应**
```json
{
  "code": 400,
  "msg": "请上传Excel文件（.xlsx或.xls）",
  "data": null,
  "timestamp": 1715241600000
}
```
```json
{
  "code": 400,
  "msg": "单次导入不能超过1000条记录",
  "data": null,
  "timestamp": 1715241600000
}
```

#### 注意事项

1. **表头必须完全匹配**：第一行为表头，列名必须与上方"Excel模板列说明"表格中的列名完全一致
2. **同组多行填写规则**：同一专业组的多行数据中，专业组字段（大学名、年份、省份、批次、专业组代码等）应保持一致，专业明细字段（专业代码、专业名称等）各不相同
3. **逗号分隔字段**：科目和报考限制条件支持英文逗号 `,` 和中文逗号 `，` 分隔
4. **已存在的专业组**：导入时会追加新明细，同时更新专业组字段（仅更新 Excel 中有值的字段）
5. **全部校验通过才插入**：先全量校验所有行，有任何错误（含与 DB 已有记录的冲突）则整批不插入，返回 400 错误。校验全通过后事务内全量插入，不再跳过任何行
6. **学费为文本字段**：`tuition` 设计为 String 类型以兼容各种格式（如"5000元/年"、"免学费"等）
7. **省招代码为可选字段**：`enrollmentCode` 可不填，不影响导入
8. **Content-Type 校验**：除文件名后缀外，还会校验文件 MIME 类型，防止篡改后缀上传非法文件
9. **长事务优化**：文件解析与校验在事务外执行，仅插入阶段使用编程式事务，避免大 Excel 导致长时间持有 DB 连接

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
| size | Integer | 否 | - | 每页条数，默认10，范围10-100 |
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

**错误响应示例**
```json
{
  "code": 404,
  "msg": "专业录取明细不存在",
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

### 2.6 删除专业明细（软删除）

**请求**
```
DELETE /api/v1/admin/algorithm/admission/major-score/{id}
```

**说明**：软删除操作，设置 `isDeleted=true`，可通过 2.5 状态接口恢复。删除后会触发数据库触发器自动更新所属专业组的聚合字段。删除已删除的记录返回 404。

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

**错误响应示例**
```json
{
  "code": 404,
  "msg": "专业录取明细不存在",
  "data": null,
  "timestamp": 1715241600000
}
```

### 2.7 批量删除专业明细（软删除）

**请求**
```
POST /api/v1/admin/algorithm/admission/major-score/batch
Content-Type: application/json
```

**说明**：批量软删除，设置 `isDeleted=true`。已删除的记录会被跳过（仅过滤 `isDeleted=false` 的记录）。

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
- `该专业组内已存在相同的专业代码`：专业明细唯一约束冲突
- `专业组不存在`：关联的groupId无效
- `大学[XX大学]不存在或已禁用`：universityName无效
- `大学[XX大学]不存在`：Excel导入时大学名称无法匹配
- `请上传Excel文件（.xlsx或.xls）`：文件类型不合法
- `单次导入不能超过1000条记录`：导入行数超限

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
