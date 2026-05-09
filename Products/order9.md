# 竞赛证书管理模块实施报告

## 模块概述

竞赛证书管理模块为管理员提供职业技能证书、学科竞赛信息的增删改查功能，支持竞赛与专业的多对多关联管理，用于支撑高考志愿规划的竞赛资质分析。

### 功能清单
| 子模块 | 功能 |
|--------|------|
| 证书管理 | 证书列表、详情、新增、修改、软删除、硬删除、批量硬删除 |
| 竞赛管理 | 竞赛列表、详情（含详情表）、新增、修改、软删除、硬删除、批量硬删除 |
| 竞赛-专业关联 | 关联列表、按竞赛/专业查询、新增（基于名称）、删除、批量删除 |

### 删除机制说明
| 操作 | HTTP方法 | 路径 | 说明 |
|------|----------|------|------|
| 软删除 | DELETE | `/soft/{id}` | 设置is_deleted=true，数据保留可恢复 |
| 硬删除 | DELETE | `/hard/{id}` | 物理删除记录，数据不可恢复 |
| 批量硬删除 | DELETE | `/batch` | 批量物理删除记录 |

前端列表每行应显示：详情、软删除、硬删除按钮；顶部显示批量删除按钮

### 关联关系说明
- **竞赛 ↔ 竞赛详情**：一对一关系，新增/更新/删除同步进行，使用事务保证一致性
- **竞赛 ↔ 专业**：多对多关系，通过t_competition_major关联表实现
- **级联删除**：硬删除竞赛时，自动删除关联的竞赛详情和竞赛-专业关联记录

---

## API 接口文档

### 管理端接口 (端口: 8081)

---

## 一、证书管理接口

路由前缀：`/api/v1/admin/certificate`

---

### 1.1 分页查询证书列表
```
GET /api/v1/admin/certificate/list
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| certName | String | 否 | 证书名称模糊查询 |
| category | String | 否 | 证书分类精确查询 |
| certLevel | String | 否 | 证书等级精确查询 |
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
        "certName": "软件设计师",
        "category": "IT类",
        "certLevel": "中级",
        "applicableMajor": "计算机类",
        "registrationTime": "每年3月/9月",
        "examTime": "5月中旬/11月上旬",
        "examFee": 168,
        "createdAt": "2026-05-09T10:30:00+08:00",
        "updatedAt": "2026-05-09T10:30:00+08:00"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1
  },
  "timestamp": 1715300000000
}
```

---

### 1.2 获取证书详情
```
GET /api/v1/admin/certificate/{id}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 证书ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1234567890123456789,
    "certName": "软件设计师",
    "category": "IT类",
    "certLevel": "中级",
    "applicableMajor": "计算机类",
    "registrationTime": "每年3月/9月",
    "examTime": "5月中旬/11月上旬",
    "examFee": 168,
    "certIntro": "软件设计师是计算机技术与软件专业技术资格考试中的中级资格...",
    "examRequirements": ["具有一定计算机基础", "无学历限制"],
    "examArrangement": "上午考基础知识，下午考应用技术",
    "officialWebsite": "https://www.ruankao.org.cn",
    "createdAt": "2026-05-09T10:30:00+08:00",
    "updatedAt": "2026-05-09T10:30:00+08:00"
  },
  "timestamp": 1715300000000
}
```

---

### 1.3 新增证书
```
POST /api/v1/admin/certificate/add
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| certName | String | 是 | 证书名称（最长150字符，唯一） |
| category | String | 否 | 证书分类（最长50字符） |
| certLevel | String | 否 | 证书等级（最长50字符） |
| applicableMajor | String | 否 | 适用专业（最长200字符） |
| registrationTime | String | 否 | 报名时间（最长100字符） |
| examTime | String | 否 | 考试时间（最长100字符） |
| examFee | Integer | 否 | 考试费用（元，≥0） |
| certIntro | String | 否 | 证书简介 |
| examRequirements | List<String> | 否 | 报考条件列表 |
| examArrangement | String | 否 | 考试安排详情 |
| officialWebsite | String | 否 | 官方网站（最长500字符） |

**请求示例：**
```json
{
  "certName": "软件设计师",
  "category": "IT类",
  "certLevel": "中级",
  "applicableMajor": "计算机类",
  "registrationTime": "每年3月/9月",
  "examTime": "5月中旬/11月上旬",
  "examFee": 168,
  "certIntro": "软件设计师是计算机技术与软件专业技术资格考试中的中级资格...",
  "examRequirements": ["具有一定计算机基础", "无学历限制"],
  "officialWebsite": "https://www.ruankao.org.cn"
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": 1234567890123456789,
  "timestamp": 1715300000000
}
```

**操作日志：** 此接口自动记录操作日志

---

### 1.4 更新证书
```
PUT /api/v1/admin/certificate/update
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 证书ID |
| certName | String | 是 | 证书名称 |
| 其他字段 | - | 否 | 同新增接口 |

**请求示例：**
```json
{
  "id": 1234567890123456789,
  "certName": "软件设计师",
  "examFee": 198
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715300000000
}
```

**操作日志：** 此接口自动记录操作日志

---

### 1.5 软删除证书
```
DELETE /api/v1/admin/certificate/soft/{id}
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 证书ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715300000000
}
```

**业务说明：** 设置is_deleted=true，数据保留可恢复。

**操作日志：** 此接口自动记录操作日志

---

### 1.6 硬删除证书
```
DELETE /api/v1/admin/certificate/hard/{id}
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 证书ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715300000000
}
```

**业务说明：** 物理删除记录，数据不可恢复。

**操作日志：** 此接口自动记录操作日志

---

### 1.7 批量硬删除证书
```
DELETE /api/v1/admin/certificate/batch
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**请求参数：**
```json
{
  "ids": [1234567890123456789, 1234567890123456790]
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715300000000
}
```

**操作日志：** 此接口自动记录操作日志

---

## 二、竞赛管理接口

路由前缀：`/api/v1/admin/competition`

---

### 2.1 分页查询竞赛列表
```
GET /api/v1/admin/competition/list
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| compName | String | 否 | 竞赛名称模糊查询 |
| compLevel | String | 否 | 竞赛级别精确查询 |
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
        "compName": "全国大学生数学建模竞赛",
        "compLevel": "国家级",
        "registrationTime": "每年6月-9月",
        "createdAt": "2026-05-09T10:30:00+08:00",
        "updatedAt": "2026-05-09T10:30:00+08:00"
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1
  },
  "timestamp": 1715300000000
}
```

---

### 2.2 获取竞赛详情
```
GET /api/v1/admin/competition/{id}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 竞赛ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1234567890123456789,
    "compName": "全国大学生数学建模竞赛",
    "compLevel": "国家级",
    "registrationTime": "每年6月-9月",
    "createdAt": "2026-05-09T10:30:00+08:00",
    "updatedAt": "2026-05-09T10:30:00+08:00",
    "detailId": 1234567890123456790,
    "basicInfo": {
      "organizer": "中国工业与应用数学学会",
      "eventTime": "每年9月",
      "participants": "全日制在校大学生",
      "format": "团队赛（3人）",
      "level": "国家级A类",
      "fee": 0,
      "website": "http://www.mcm.edu.cn",
      "email": "mcm@mcm.edu.cn",
      "phone": "010-12345678"
    },
    "awards": ["国家一等奖", "国家二等奖", "省级一等奖", "省级二等奖", "省级三等奖"],
    "background": "全国大学生数学建模竞赛创办于1992年...",
    "purposes": ["培养学生创新意识", "提高数学建模能力", "团队协作能力"],
    "competitionRules": [
      {"title": "组队规则", "content": "每队3名学生，可跨专业组队"},
      {"title": "竞赛时间", "content": "连续72小时"}
    ],
    "scoringCriteria": ["假设合理性", "模型创新性", "结果正确性", "论文规范性"],
    "notices": ["自带电脑", "可查阅资料", "禁止与队外人员讨论"],
    "processGuide": [
      {"title": "报名阶段", "content": "通过学校教务处统一报名"},
      {"title": "竞赛阶段", "content": "在指定时间内完成建模论文"}
    ],
    "awardsDisplay": [
      {"title": "国家一等奖", "content": "约1%，保研加分、奖学金优先"},
      {"title": "国家二等奖", "content": "约5%，保研加分"}
    ]
  },
  "timestamp": 1715300000000
}
```

---

### 2.3 新增竞赛
```
POST /api/v1/admin/competition/add
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| compName | String | 是 | 竞赛名称（最长200字符，唯一） |
| compLevel | String | 否 | 竞赛级别（最长50字符） |
| registrationTime | String | 否 | 报名时间（最长100字符） |
| detail | Object | 否 | 竞赛详情对象 |

**detail对象字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| basicInfo | Map<String, Object> | 基本信息（主办方、举办时间、参赛对象等） |
| awards | List<String> | 奖项设置列表 |
| background | String | 竞赛背景与意义 |
| purposes | List<String> | 竞赛目的列表 |
| competitionRules | List<Map<String, String>> | 竞赛规则（每项含title和content） |
| scoringCriteria | List<String> | 评分标准列表 |
| notices | List<String> | 注意事项列表 |
| processGuide | List<Map<String, String>> | 参赛流程指南（每项含title和content） |
| awardsDisplay | List<Map<String, String>> | 奖项设置展示（每项含title和content） |

**请求示例：**
```json
{
  "compName": "全国大学生数学建模竞赛",
  "compLevel": "国家级",
  "registrationTime": "每年6月-9月",
  "detail": {
    "basicInfo": {
      "organizer": "中国工业与应用数学学会",
      "eventTime": "每年9月",
      "level": "国家级A类"
    },
    "awards": ["国家一等奖", "国家二等奖"],
    "background": "全国大学生数学建模竞赛创办于1992年...",
    "purposes": ["培养学生创新意识", "提高数学建模能力"]
  }
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": 1234567890123456789,
  "timestamp": 1715300000000
}
```

**业务说明：** 新增时自动创建竞赛详情记录（1:1关系），使用事务保证一致性。

**操作日志：** 此接口自动记录操作日志

---

### 2.4 更新竞赛
```
PUT /api/v1/admin/competition/update
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 竞赛ID |
| compName | String | 是 | 竞赛名称 |
| compLevel | String | 否 | 竞赛级别 |
| registrationTime | String | 否 | 报名时间 |
| detail | Object | 否 | 竞赛详情对象（同新增） |

**业务说明：** 更新时同步更新竞赛详情记录，使用事务保证一致性。

**操作日志：** 此接口自动记录操作日志

---

### 2.5 软删除竞赛
```
DELETE /api/v1/admin/competition/soft/{id}
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 竞赛ID |

**业务说明：** 同时软删除竞赛主表和详情表记录。

**操作日志：** 此接口自动记录操作日志

---

### 2.6 硬删除竞赛
```
DELETE /api/v1/admin/competition/hard/{id}
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 竞赛ID |

**业务说明：** 级联删除：竞赛-专业关联记录 → 竞赛详情记录 → 竞赛主表记录。

**操作日志：** 此接口自动记录操作日志

---

### 2.7 批量硬删除竞赛
```
DELETE /api/v1/admin/competition/batch
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**请求参数：**
```json
{
  "ids": [1234567890123456789, 1234567890123456790]
}
```

**业务说明：** 对每个ID执行级联删除。

**操作日志：** 此接口自动记录操作日志

---

## 三、竞赛-专业关联管理接口

路由前缀：`/api/v1/admin/competition-major`

---

### 3.1 分页查询关联列表
```
GET /api/v1/admin/competition-major/list
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| competitionId | Long | 否 | 竞赛ID精确查询 |
| majorId | Long | 否 | 专业ID精确查询 |
| competitionName | String | 否 | 竞赛名称模糊查询 |
| majorName | String | 否 | 专业名称模糊查询 |
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
        "competitionId": 1234567890123456780,
        "majorId": 1234567890123456781,
        "competitionName": "全国大学生数学建模竞赛",
        "majorName": "数学与应用数学",
        "createdAt": "2026-05-09T10:30:00+08:00"
      }
    ],
    "total": 200,
    "size": 10,
    "current": 1
  },
  "timestamp": 1715300000000
}
```

---

### 3.2 按竞赛ID查询关联专业
```
GET /api/v1/admin/competition-major/by-competition/{competitionId}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| competitionId | Long | 是 | 竞赛ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 1234567890123456789,
      "competitionId": 1234567890123456780,
      "majorId": 1234567890123456781,
      "competitionName": "全国大学生数学建模竞赛",
      "majorName": "数学与应用数学",
      "createdAt": "2026-05-09T10:30:00+08:00"
    },
    {
      "id": 1234567890123456790,
      "competitionId": 1234567890123456780,
      "majorId": 1234567890123456782,
      "competitionName": "全国大学生数学建模竞赛",
      "majorName": "统计学",
      "createdAt": "2026-05-09T10:30:00+08:00"
    }
  ],
  "timestamp": 1715300000000
}
```

---

### 3.3 按专业ID查询关联竞赛
```
GET /api/v1/admin/competition-major/by-major/{majorId}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| majorId | Long | 是 | 专业ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 1234567890123456789,
      "competitionId": 1234567890123456780,
      "majorId": 1234567890123456781,
      "competitionName": "全国大学生数学建模竞赛",
      "majorName": "数学与应用数学",
      "createdAt": "2026-05-09T10:30:00+08:00"
    }
  ],
  "timestamp": 1715300000000
}
```

---

### 3.4 新增竞赛-专业关联
```
POST /api/v1/admin/competition-major/add
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**请求参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| competitionName | String | 是 | 竞赛名称（用于查找竞赛ID） |
| majorName | String | 是 | 专业名称（用于查找专业ID） |

**请求示例：**
```json
{
  "competitionName": "全国大学生数学建模竞赛",
  "majorName": "数学与应用数学"
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": 1234567890123456789,
  "timestamp": 1715300000000
}
```

**业务说明：**
1. 根据竞赛名称查找竞赛ID，找不到返回404
2. 根据专业名称查找专业ID，找不到返回404
3. 检查关联是否已存在，已存在返回400
4. 创建关联记录，同时冗余存储名称字段

**操作日志：** 此接口自动记录操作日志

---

### 3.5 删除关联
```
DELETE /api/v1/admin/competition-major/{id}
Authorization: Bearer {accessToken}
```

**路径参数：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 关联记录ID |

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715300000000
}
```

**操作日志：** 此接口自动记录操作日志

---

### 3.6 批量删除关联
```
DELETE /api/v1/admin/competition-major/batch
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**请求参数：**
```json
{
  "ids": [1234567890123456789, 1234567890123456790]
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715300000000
}
```

**操作日志：** 此接口自动记录操作日志

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 / 数据已存在 |
| 401 | 未登录或 Token 过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 数据库表结构

### t_certificate (证书表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 证书ID（雪花算法） |
| cert_name | VARCHAR(150) | 证书名称（唯一） |
| category | VARCHAR(50) | 证书分类（IT类/财会类/语言类/工程类） |
| cert_level | VARCHAR(50) | 证书等级（初级/中级/高级） |
| applicable_major | VARCHAR(200) | 适用专业 |
| registration_time | VARCHAR(100) | 报名时间 |
| exam_time | VARCHAR(100) | 考试时间 |
| exam_fee | INTEGER | 考试费用（元） |
| cert_intro | TEXT | 证书简介 |
| exam_requirements | TEXT[] | 报考条件列表 |
| exam_arrangement | TEXT | 考试安排详情 |
| official_website | VARCHAR(500) | 官方网站 |
| is_deleted | BOOLEAN | 是否删除 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

### t_competition (竞赛表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 竞赛ID（雪花算法） |
| comp_name | VARCHAR(200) | 竞赛名称（唯一） |
| comp_level | VARCHAR(50) | 竞赛级别（国家级/省级/校级） |
| registration_time | VARCHAR(100) | 报名时间 |
| is_deleted | BOOLEAN | 是否删除 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

### t_competition_detail (竞赛详情表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 详情ID（雪花算法） |
| competition_id | BIGINT | 关联竞赛ID（一对一，唯一） |
| basic_info | JSONB | 基本信息（主办方、举办时间、参赛对象等） |
| awards | TEXT[] | 奖项设置列表 |
| background | TEXT | 竞赛背景与意义 |
| purposes | TEXT[] | 竞赛目的列表 |
| competition_rules | JSONB | 竞赛规则（数组，每项含title和content） |
| scoring_criteria | TEXT[] | 评分标准列表 |
| notices | TEXT[] | 注意事项列表 |
| process_guide | JSONB | 参赛流程指南（数组，每项含title和content） |
| awards_display | JSONB | 奖项设置展示（数组，每项含title和content） |
| is_deleted | BOOLEAN | 是否删除 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

### t_competition_major (竞赛-专业关联表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 关联ID（雪花算法） |
| competition_id | BIGINT | 竞赛ID |
| major_id | BIGINT | 专业ID |
| competition_name | VARCHAR(200) | 竞赛名称（冗余） |
| major_name | VARCHAR(100) | 专业名称（冗余） |
| created_at | TIMESTAMPTZ | 创建时间 |

---

## 文件清单

### haifeng-common (公共模块)
- `entity/certificate/Certificate.java` - 证书实体
- `entity/certificate/Competition.java` - 竞赛实体
- `entity/certificate/CompetitionDetail.java` - 竞赛详情实体
- `entity/certificate/CompetitionMajor.java` - 竞赛-专业关联实体
- `mapper/certificate/CertificateMapper.java` - 证书 Mapper
- `mapper/certificate/CompetitionMapper.java` - 竞赛 Mapper
- `mapper/certificate/CompetitionDetailMapper.java` - 竞赛详情 Mapper
- `mapper/certificate/CompetitionMajorMapper.java` - 竞赛-专业关联 Mapper

### haifeng-admin (管理端)
- `db/migration/V9__create_certificate_competition.sql` - 数据库迁移脚本
- `controller/certificate/CertificateController.java` - 证书控制器
- `controller/certificate/CompetitionController.java` - 竞赛控制器
- `controller/certificate/CompetitionMajorController.java` - 竞赛-专业关联控制器
- `service/certificate/CertificateService.java` - 证书服务接口
- `service/certificate/CompetitionService.java` - 竞赛服务接口
- `service/certificate/CompetitionMajorService.java` - 竞赛-专业关联服务接口
- `service/impl/certificate/CertificateServiceImpl.java` - 证书服务实现
- `service/impl/certificate/CompetitionServiceImpl.java` - 竞赛服务实现
- `service/impl/certificate/CompetitionMajorServiceImpl.java` - 竞赛-专业关联服务实现
- `dto/certificate/CertificateQueryDTO.java` - 证书查询 DTO
- `dto/certificate/CertificateAddDTO.java` - 证书新增 DTO
- `dto/certificate/CertificateUpdateDTO.java` - 证书更新 DTO
- `dto/certificate/CompetitionQueryDTO.java` - 竞赛查询 DTO
- `dto/certificate/CompetitionAddDTO.java` - 竞赛新增 DTO
- `dto/certificate/CompetitionUpdateDTO.java` - 竞赛更新 DTO
- `dto/certificate/CompetitionDetailDTO.java` - 竞赛详情 DTO
- `dto/certificate/CompetitionMajorQueryDTO.java` - 关联查询 DTO
- `dto/certificate/CompetitionMajorAddDTO.java` - 关联新增 DTO
- `dto/certificate/BatchDeleteDTO.java` - 批量删除 DTO
- `vo/certificate/CertificateListVO.java` - 证书列表 VO
- `vo/certificate/CertificateDetailVO.java` - 证书详情 VO
- `vo/certificate/CompetitionListVO.java` - 竞赛列表 VO
- `vo/certificate/CompetitionDetailVO.java` - 竞赛详情 VO
- `vo/certificate/CompetitionMajorVO.java` - 竞赛-专业关联 VO
