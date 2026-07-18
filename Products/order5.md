# 院校管理模块接口文档

## 概述

本文档描述院校管理模块的后台管理接口，包含院校列表、校园图册、院校适应指南三个子模块。

**基础路径：** `/api/v1/admin/university`

**模块说明：**
| 子模块 | 数据表 | 关系 |
|--------|--------|------|
| 院校列表 | universities + universities_detail | 1:1 |
| 校园图册 | t_campus_gallery | 1:N（与院校） |
| 院校适应指南 | university_guides | 1:1（与院校） |

---

## Excel导入表头规范

### xlsx1：院校主表 (universities)

| 表头名称 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| 院校名称 | 文本 | Y | 院校官方全称，唯一 |
| 院校名称英文 | 文本 | Y | 院校英文名称 |
| 省份 | 文本 | Y | 所在省份 |
| 城市 | 文本 | Y | 所在城市 |
| 所属地区 | 文本 | Y | 地区（华东/华北等） |
| 院校类别 | 文本 | Y | 综合/理工/师范/农林/医药等 |
| 专业数量 | 整数 | | 院校开设专业总数 |
| 办学层次 | 文本 | | 本科/专科/本专兼招 |
| 院校性质 | 文本 | | 公办/民办/中外合作 |
| 是否有博士点 | 布尔 | | TRUE/FALSE |
| 是否有硕士点 | 布尔 | | TRUE/FALSE |
| 隶属部门 | 文本 | | 教育部/省教育厅等 |
| 院校标签 | 文本 | | 逗号分隔，如：985,211,双一流 |
| 知名联盟 | 文本 | | C9/华东五校等 |
| 院校图片URL | 文本 | | 封面图片地址 |
| 院校简介 | 文本 | | 院校简要介绍 |
| 推免率 | 小数 | | 推荐免试研究生比例 |
| 推免年份 | 整数 | | 推免率数据年份 |

---

### xlsx2：院校详情表 (universities_detail)

| 表头名称 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| 院校名称 | 文本 | Y | 外键，必须在主表中存在 |
| 学校地址 | 文本 | | 详细校区地址 |
| 招生电话 | 文本 | | 招生办联系电话 |
| 官方网站 | 文本 | | 院校官网URL |
| 本科批历史组 | 整数 | | 历史组/文科录取分数线 |
| 本科批物理组 | 整数 | | 物理组/理科录取分数线 |
| 轮播图片URL | 文本 | | 逗号分隔，多张轮播图 |
| 院校详细介绍 | 文本 | | 详情页展示的完整介绍 |
| 软科排名 | 整数 | | 软科中国大学排名 |
| 校友会排名 | 整数 | | 校友会中国大学排名 |
| 武书连排名 | 整数 | | 武书连中国大学排名 |
| QS排名 | 整数 | | QS世界大学排名 |
| U.S.NEWS排名 | 整数 | | U.S.NEWS世界大学排名 |
| 出国比例 | 文本 | | 如：15% |
| 男女比例 | 文本 | | 如：6:4 |

---

### xlsx3：校园图册表 (t_campus_gallery)

| 表头名称 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| 院校名称 | 文本 | Y | 外键，必须在主表中存在 |
| 图片类型 | 文本 | Y | 教学楼/宿舍/食堂/图书馆/操场/校门等 |
| 图片URL | 文本 | Y | 图片地址 |
| 排序权重 | 整数 | | 数值越小越靠前 |

---

### xlsx4：院校适应指南表 (university_guides)

#### Sheet0（主表）

| 表头名称 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| 院校名称 | 文本 | Y | 外键，必须在主表中存在 |
| 自定义标签 | 文本 | | 逗号分隔 |
| 备注 | 文本 | | 备注信息 |
| 状态 | 整数 | | 0-下架 1-展示，默认1 |

#### Sheet1-14（JSONB字段）

每个Sheet对应一个JSONB字段，通过"院校名称"关联主表。

| Sheet | JSONB字段 | 字段说明 | 子字段 |
|-------|----------|----------|--------|
| Sheet1 | class_dorm_social | 班级与宿舍社交 | 字段1:班级管理方式, 字段2:宿舍社交建议 |
| Sheet2 | financial_aid | 奖助勤贷与权益保障 | 字段1:奖助学金政策, 字段2:勤工俭学岗位, 字段3:权益申诉渠道 |
| Sheet3 | life_services | 生活服务 | 字段1:校园生活服务, 字段2:医疗资源, 字段3:兼职实习资源 |
| Sheet4 | dormitory_services | 水电网与宿舍管理 | 字段1:水电费缴纳方式, 字段2:宿舍规章制度 |
| Sheet5 | campus_security | 校园安全与应急处理 | 字段1:安全设施, 字段2:安全规则 |
| Sheet6 | campus_events | 校园活动与竞赛 | 字段1:院校品牌活动, 字段2:学科与技能竞赛 |
| Sheet7 | campus_facilities | 校园设施 | 字段1:教学楼分布, 字段2:实验楼与图书馆, 字段3:宿舍区与食堂, 字段4:生活配套设施 |
| Sheet8 | campus_transportation | 校园通勤与校外交通 | 字段1:校内通勤方式, 字段2:校外交通情况 |
| Sheet9 | student_organizations | 学生组织与社团 | 字段1:官方组织, 字段2:社团类型 |
| Sheet10 | academic_support_resources | 学习支持资源 | 字段1:师资力量, 字段2:学习场所, 字段3:学业帮扶 |
| Sheet11 | health_services | 医保与心理健康 | 字段1:医保报销政策, 字段2:心理健康服务 |
| Sheet12 | academic_guidance | 专业与课程核心信息 | 字段1:专业培养方案说明, 字段2:选课系统说明 |
| Sheet13 | major_transfer_constriction | 转专业限制 | 字段1:限制类型, 字段2:具体限制说明 |
| Sheet14 | major_transfer_guidelines | 转专业原则 | 字段1:基本申请条件, 字段2:申请时间与流程 |

**JSONB Sheet通用表头格式：**
| 表头名称 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| 院校名称 | 文本 | Y | 关联主表 |
| 字段1 | 文本 | | 逗号分隔的数组 |
| 字段2 | 文本 | | 逗号分隔的数组 |
| 字段3 | 文本 | | 逗号分隔的数组 |
| 字段4 | 文本 | | 逗号分隔的数组 |

---

## 接口列表

### 1. 院校管理接口（11个）

#### 1.1 分页查询院校列表

- **URL:** `GET /api/v1/admin/university/list`
- **方法:** GET
- **参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | | 院校名称（模糊搜索，最长50字符） |
| provinceName | String | | 省份（精确匹配，最长50字符） |
| category | String | | 院校类别（精确匹配，最长50字符） |
| status | Integer | | 状态：0-下架 1-展示 |
| page | Integer | Y | 页码，从1开始 |
| size | Integer | Y | 每页条数：10/20/30/50/100/200/500/1000 |

- **响应:**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1234567890123456789,
        "name": "清华大学",
        "provinceName": "北京",
        "cityName": "北京",
        "region": "华北",
        "category": "综合",
        "majorCount": 82,
        "educationLevel": "本科",
        "nature": "公办",
        "status": 1,
        "createdAt": "2026-05-07T10:00:00"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  },
  "timestamp": 1234567890
}
```

---

#### 1.2 获取院校详情

- **URL:** `GET /api/v1/admin/university/{id}`
- **方法:** GET
- **路径参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | Y | 院校ID |

- **响应:**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1234567890123456789,
    "name": "清华大学",
    "nameEn": "Tsinghua University",
    "provinceName": "北京",
    "cityName": "北京",
    "region": "华北",
    "category": "综合",
    "majorCount": 82,
    "educationLevel": "本科",
    "nature": "公办",
    "recommendationRate": 50.00,
    "recommendationYear": 2025,
    "hasDoctorate": true,
    "hasMaster": true,
    "department": "教育部",
    "tags": ["985", "211", "双一流"],
    "famousUnion": "C9",
    "imageUrl": "https://xxx.com/tsinghua.jpg",
    "introduction": "清华大学是中国顶尖学府...",
    "sortOrder": 1,
    "status": 1,
    "createdAt": "2026-05-07T10:00:00",
    "updatedAt": "2026-05-07T10:00:00",
    "detailId": 1234567890123456790,
    "address": "北京市海淀区清华园1号",
    "admissionPhone": "010-62770334",
    "website": "https://www.tsinghua.edu.cn",
    "historyGroupScore": 680,
    "scienceGroupScore": 690,
    "carouselImages": ["https://xxx.com/1.jpg", "https://xxx.com/2.jpg"],
    "detailIntroduction": "详细介绍...",
    "rankings": {
      "ruanke": 1,
      "xiaoyouhui": 2,
      "wushulian": 1,
      "qs": 17,
      "usnews": 26
    },
    "abroadRate": "30%",
    "genderRatio": "6:4"
  },
  "timestamp": 1234567890
}
```

---

#### 1.3 新增院校

- **URL:** `POST /api/v1/admin/university`
- **方法:** POST
- **请求体:**
```json
{
  "name": "清华大学",
  "nameEn": "Tsinghua University",
  "provinceName": "北京",
  "cityName": "北京",
  "region": "华北",
  "category": "综合",
  "majorCount": 82,
  "educationLevel": "本科",
  "nature": "公办",
  "recommendationRate": 50.00,
  "recommendationYear": 2025,
  "hasDoctorate": true,
  "hasMaster": true,
  "department": "教育部",
  "tags": ["985", "211", "双一流"],
  "famousUnion": "C9",
  "imageUrl": "https://xxx.com/tsinghua.jpg",
  "introduction": "清华大学是中国顶尖学府..."
}
```

- **响应:**
```json
{
  "code": 200,
  "msg": "success",
  "data": 1234567890123456789,
  "timestamp": 1234567890
}
```

---

#### 1.4 修改院校基础信息

- **URL:** `PUT /api/v1/admin/university/{id}`
- **方法:** PUT
- **路径参数:** id - 院校ID
- **说明:** 全量替换模式。必填字段不可为空，可选字段传null会覆盖为null（与Excel导入行为一致）
- **请求体:**
```json
{
  "name": "清华大学",
  "nameEn": "Tsinghua University",
  "provinceName": "北京",
  "cityName": "北京",
  "region": "华北",
  "category": "综合",
  "majorCount": 82,
  "educationLevel": "本科",
  "nature": "公办",
  "recommendationRate": 50.00,
  "recommendationYear": 2025,
  "hasDoctorate": true,
  "hasMaster": true,
  "department": "教育部",
  "tags": ["985", "211", "双一流"],
  "famousUnion": "C9",
  "imageUrl": "https://xxx.com/tsinghua.jpg",
  "introduction": "清华大学是中国顶尖学府..."
}
```

- **字段说明:**
  - `name`、`nameEn`、`provinceName`、`cityName`、`region`、`category`：必填
  - 其余字段：可选，不传或传null则数据库中对应字段设为null

---

#### 1.5 修改院校详情信息

- **URL:** `PUT /api/v1/admin/university/{id}/detail`
- **方法:** PUT
- **路径参数:** id - 院校ID
- **说明:** 所有字段均为可选，不传或传null则数据库中对应字段设为null
- **请求体:**
```json
{
  "address": "北京市海淀区清华园1号",
  "admissionPhone": "010-62770334",
  "website": "https://www.tsinghua.edu.cn",
  "historyGroupScore": 680,
  "scienceGroupScore": 690,
  "carouselImages": ["https://xxx.com/1.jpg"],
  "introduction": "详细介绍...",
  "rankings": {
    "ruanke": 1,
    "xiaoyouhui": 2,
    "wushulian": 1,
    "qs": 17,
    "usnews": 26
  },
  "abroadRate": "30%",
  "genderRatio": "6:4"
}
```

---

#### 1.6 软删除院校（可恢复）

- **URL:** `DELETE /api/v1/admin/university/{id}`
- **方法:** DELETE
- **说明:** 将status置为0，数据可恢复

---

#### 1.6.1 硬删除院校（永久删除）

- **URL:** `DELETE /api/v1/admin/university/{id}/hard`
- **方法:** DELETE
- **说明:** 物理删除，同时删除关联的详情和适应指南。已软删除的院校不可硬删除

---

#### 1.7 批量软删除院校

- **URL:** `POST /api/v1/admin/university/batch-delete`
- **方法:** POST
- **请求体:**
```json
{
  "ids": [1234567890123456789, 1234567890123456790]
}
```

---

#### 1.7.1 批量硬删除院校

- **URL:** `POST /api/v1/admin/university/batch-hard-delete`
- **方法:** POST
- **请求体:** 同批量软删除

---

#### 1.8 导入院校主表数据

- **URL:** `POST /api/v1/admin/university/import`
- **方法:** POST
- **Content-Type:** multipart/form-data
- **参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | Y | xlsx文件，最大30MB |

- **表头规范:** 见 xlsx1：院校主表

---

#### 1.9 导入院校详情数据

- **URL:** `POST /api/v1/admin/university/import-detail`
- **方法:** POST
- **Content-Type:** multipart/form-data
- **参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | Y | xlsx文件，最大30MB |

- **表头规范:** 见 xlsx2：院校详情表

---

### 2. 校园图册接口（6个）

#### 2.1 分页查询校园图册列表

- **URL:** `GET /api/v1/admin/university/gallery/list`
- **方法:** GET
- **参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| universityName | String | | 院校名称（模糊搜索） |
| imageType | String | | 图片类型 |
| status | Integer | | 状态 |
| page | Integer | Y | 页码 |
| size | Integer | Y | 每页条数 |

- **响应:**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1234567890123456789,
        "universityId": 1234567890123456788,
        "universityName": "清华大学",
        "imageType": "教学楼",
        "imageUrl": "https://xxx.com/building.jpg",
        "sortOrder": 1,
        "status": 1,
        "createdAt": "2026-05-07T10:00:00"
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1,
    "pages": 5
  },
  "timestamp": 1234567890
}
```

---

#### 2.2 新增校园图片

- **URL:** `POST /api/v1/admin/university/gallery`
- **方法:** POST
- **请求体:**
```json
{
  "universityId": 1234567890123456788,
  "imageType": "教学楼",
  "imageUrl": "https://xxx.com/building.jpg",
  "sortOrder": 1
}
```

---

#### 2.3 修改校园图片

- **URL:** `PUT /api/v1/admin/university/gallery/{id}`
- **方法:** PUT
- **请求体:**
```json
{
  "imageType": "教学楼",
  "imageUrl": "https://xxx.com/building_new.jpg",
  "sortOrder": 2,
  "status": 1
}
```

---

#### 2.4 删除校园图片

- **URL:** `DELETE /api/v1/admin/university/gallery/{id}`
- **方法:** DELETE

---

#### 2.5 批量删除校园图片

- **URL:** `DELETE /api/v1/admin/university/gallery/batch`
- **方法:** DELETE
- **请求体:**
```json
{
  "ids": [1234567890123456789, 1234567890123456790]
}
```

---

#### 2.6 导入校园图册数据

- **URL:** `POST /api/v1/admin/university/gallery/import`
- **方法:** POST
- **Content-Type:** multipart/form-data
- **表头规范:** 见 xlsx3：校园图册表

---

### 3. 院校适应指南接口（9个）

#### 3.1 分页查询院校适应指南列表

- **URL:** `GET /api/v1/admin/university/guide/list`
- **方法:** GET
- **参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| universityName | String | | 院校名称（模糊搜索，最长50字符） |
| status | Integer | | 状态 |
| page | Integer | Y | 页码 |
| size | Integer | Y | 每页条数 |

- **响应:**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1234567890123456789,
        "universityId": 1234567890123456788,
        "universityName": "清华大学",
        "customTags": ["学术氛围好", "社团丰富"],
        "remark": "备注信息",
        "status": 1,
        "createdAt": "2026-05-07T10:00:00"
      }
    ],
    "total": 30,
    "size": 10,
    "current": 1,
    "pages": 3
  },
  "timestamp": 1234567890
}
```

---

#### 3.2 获取院校适应指南详情

- **URL:** `GET /api/v1/admin/university/guide/{id}`
- **方法:** GET
- **响应:**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1234567890123456789,
    "universityId": 1234567890123456788,
    "universityName": "清华大学",
    "customTags": ["学术氛围好", "社团丰富"],
    "campusFacilities": {
      "教学楼分布": ["主楼", "新清华学堂"],
      "实验楼与图书馆": ["图书馆", "实验楼"]
    },
    "dormitoryServices": {...},
    "campusTransportation": {...},
    "academicGuidance": {...},
    "majorTransferGuidelines": {...},
    "majorTransferConstriction": {...},
    "academicSupportResources": {...},
    "studentOrganizations": {...},
    "campusEvents": {...},
    "classDormSocial": {...},
    "financialAid": {...},
    "campusSecurity": {...},
    "healthServices": {...},
    "lifeServices": {...},
    "remark": "备注信息",
    "status": 1,
    "createdAt": "2026-05-07T10:00:00",
    "updatedAt": "2026-05-07T10:00:00"
  },
  "timestamp": 1234567890
}
```

---

#### 3.3 新增院校适应指南

- **URL:** `POST /api/v1/admin/university/guide`
- **方法:** POST
- **请求体:**
```json
{
  "universityId": 1234567890123456788,
  "customTags": ["学术氛围好", "社团丰富"],
  "campusFacilities": {
    "教学楼分布": ["主楼", "新清华学堂"]
  },
  "dormitoryServices": {...},
  "remark": "备注信息"
}
```

---

#### 3.4 修改院校适应指南

- **URL:** `PUT /api/v1/admin/university/guide/{id}`
- **方法:** PUT
- **说明:** 所有字段均为可选，不传或传null则数据库中对应字段设为null
- **请求体:**
```json
{
  "customTags": ["学术氛围好", "社团丰富"],
  "campusFacilities": {
    "教学楼分布": ["主楼", "新清华学堂"]
  },
  "remark": "备注信息",
  "status": 1
}
```

---

#### 3.5 删除院校适应指南（可恢复）

- **URL:** `DELETE /api/v1/admin/university/guide/{id}`
- **方法:** DELETE

---

#### 3.5.1 硬删除院校适应指南（永久删除）

- **URL:** `DELETE /api/v1/admin/university/guide/{id}/hard`
- **方法:** DELETE

---

#### 3.6 批量软删除院校适应指南

- **URL:** `POST /api/v1/admin/university/guide/batch-delete`
- **方法:** POST
- **请求体:**
```json
{
  "ids": [1234567890123456789, 1234567890123456790]
}
```

---

#### 3.6.1 批量硬删除院校适应指南

- **URL:** `POST /api/v1/admin/university/guide/batch-hard-delete`
- **方法:** POST
- **请求体:** 同批量软删除

---

#### 3.7 导入院校适应指南数据

- **URL:** `POST /api/v1/admin/university/guide/import`
- **方法:** POST
- **Content-Type:** multipart/form-data
- **表头规范:** 见 xlsx4：院校适应指南表（包含Sheet0主表和Sheet1-14 JSONB字段）

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录/Token过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

**导入错误示例：**
```json
{
  "code": 400,
  "msg": "导入失败，共2行数据存在错误，已全部回滚。错误信息：第3行: 院校名称不能为空; 第5行: 院校[未知大学]不存在",
  "data": null,
  "timestamp": 1234567890
}
```
