# 院校次表模块接口文档

## 概述

本文档描述院校管理模块的次表管理接口，包含院系详情、实验室列表、学科评估三个子模块。

**基础路径：** `/api/v1/admin/university`

**模块说明：**
| 子模块 | 数据表 | 关系 |
|--------|--------|------|
| 院系详情 | t_department + department_reports(JSONB) | 1:1（与院校N:1） |
| 实验室列表 | t_laboratory + laboratory_core_team/laboratory_statistics | 1:N（与院校） |
| 学科评估 | t_subject_evaluation | 1:N（与院校） |

---

## Excel导入表头规范

### xlsx1：院系管理 (12 Sheets)

#### Sheet0 - t_department主表

| 表头名称 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| 院校名称 | 文本 | Y | 外键，必须在universities表中存在 |
| 院系名称 | 文本 | Y | 院系官方全称 |
| 院系类型 | 文本 | Y | 工学院/理学院/文学院/管理学院等 |
| 页面主标题 | 文本 | | 展示页面标题 |
| 院系标签 | 文本 | | 逗号分隔，如：国家重点,双一流学科 |
| 排序 | 整数 | | 数值越小越靠前，默认0 |
| 状态 | 整数 | | 0-下架 1-展示，默认1 |

---

#### Sheet1 - department_reports基础信息

| 表头名称 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| 院系名称 | 文本 | Y | 外键，必须在Sheet0中存在 |
| 副标题 | 文本 | | 报告副标题 |

---

#### Sheet2 - city_salary 城市薪资

| 表头名称 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| 院系名称 | 文本 | Y | 外键，必须在Sheet0中存在 |
| 城市名称 | 文本 | Y | 城市名称 |
| 最低薪资(万元/年) | 小数 | Y | 年薪最低值 |
| 最高薪资(万元/年) | 小数 | Y | 年薪最高值 |

---

#### Sheet3 - postgraduate 考研方向

| 表头名称 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| 院系名称 | 文本 | Y | 外键，必须在Sheet0中存在 |
| 标题 | 文本 | Y | 考研方向标题 |
| 考研方向内容 | 文本 | | 逗号分隔的方向列表 |

---

#### Sheet4 - disclaimer 免责声明

| 表头名称 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| 院系名称 | 文本 | Y | 外键，必须在Sheet0中存在 |
| 免责声明文本 | 文本 | | 免责声明内容 |
| 更新时间 | 文本 | | 数据更新时间 |
| 报告版本 | 文本 | | 报告版本号 |
| 编制单位 | 文本 | | 报告编制单位 |

---

#### Sheet5 - prospects 就业前景

| 表头名称 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| 院系名称 | 文本 | Y | 外键，必须在Sheet0中存在 |
| 综合就业率 | 文本 | | 如：95.6% |
| 硕士平均起薪 | 文本 | | 如：18万元/年 |
| 继续深造率 | 文本 | | 如：45% |
| 进入世界500强 | 文本 | | 如：30% |
| 年薪增长率 | 文本 | | 如：15% |
| 海外深造占比 | 文本 | | 如：20% |

---

#### Sheet6 - trends 就业趋势

| 表头名称 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| 院系名称 | 文本 | Y | 外键，必须在Sheet0中存在 |
| 高速增长赛道 | 文本 | | 逗号分隔，如：人工智能,新能源 |
| 核心政策导向 | 文本 | | 逗号分隔 |
| 就业环境分析 | 文本 | | 逗号分隔 |

---

#### Sheet7 - overview 概述

| 表头名称 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| 院系名称 | 文本 | Y | 外键，必须在Sheet0中存在 |
| 标题 | 文本 | | 概述标题 |
| 内容描述 | 文本 | | 逗号分隔的描述列表 |

---

#### Sheet8 - career 职业路径

| 表头名称 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| 院系名称 | 文本 | Y | 外键，必须在Sheet0中存在 |
| 路径标题 | 文本 | Y | 职业路径名称 |
| 路径描述 | 文本 | | 路径说明 |
| 阶段小标题 | 文本 | Y | 职业阶段名称 |
| 工作年限 | 文本 | | 如：0-3年 |
| 职位名称 | 文本 | | 具体职位名称 |
| 核心目标 | 文本 | | 该阶段核心目标 |
| 薪资范围(万元/年) | 文本 | | 如：15-25 |

---

#### Sheet9 - subjects_detail 专业详情

| 表头名称 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| 院系名称 | 文本 | Y | 外键，必须在Sheet0中存在 |
| 专业名称 | 文本 | Y | 专业全称 |
| 专业标签 | 文本 | | 逗号分隔，如：国家级特色专业,热门 |
| 核心学科 | 文本 | | 核心学科名称 |
| 支撑学科 | 文本 | | 支撑学科名称 |
| 专业定位 | 文本 | | 专业定位说明 |
| 核心课程 | 文本 | | 逗号分隔的课程列表 |
| 培养能力 | 文本 | | 逗号分隔的能力列表 |
| 推荐证书 | 文本 | | 逗号分隔的证书列表 |

---

#### Sheet10 - salary 专业薪资

| 表头名称 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| 院系名称 | 文本 | Y | 外键，必须在Sheet0中存在 |
| 专业名称 | 文本 | Y | 必须与Sheet9专业名称对应 |
| 最低薪资(万元/年) | 小数 | Y | 年薪最低值 |
| 最高薪资(万元/年) | 小数 | Y | 年薪最高值 |

---

#### Sheet11 - major_compose 学科组成

| 表头名称 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| 院系名称 | 文本 | Y | 外键，必须在Sheet0中存在 |
| 学科名称 | 文本 | Y | 学科名称 |
| 占比(%) | 小数 | Y | 学科占比百分数 |

---

### xlsx2：实验室管理 (3 Sheets)

#### Sheet0 - t_laboratory主表

| 表头名称 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| 院校名称 | 文本 | Y | 外键，必须在universities表中存在 |
| 实验室名称 | 文本 | Y | 实验室全称，唯一 |
| 实验室类型 | 文本 | Y | 国家重点/教育部重点/省级重点等 |
| 成立时间 | 日期 | | 格式：yyyy-MM-dd |
| 所在地区 | 文本 | | 省市信息 |
| 主管部门 | 文本 | | 教育部/科技部等 |
| 实验室主任 | 文本 | | 主任姓名 |
| 人员规模 | 整数 | | 总人数 |
| 学生规模 | 整数 | | 学生人数 |
| 联系邮箱 | 文本 | | 官方邮箱 |
| 联系电话 | 文本 | | 联系电话 |
| 实验室简介 | 文本 | | 简要介绍 |
| 研究方向描述 | 文本 | | 研究方向说明 |
| 实验室空间 | 文本 | | 如：5000平方米 |
| 开放课题 | 文本 | | 开放课题说明 |
| 合作交流 | 文本 | | 国际合作情况 |
| 访问学者 | 文本 | | 访问学者接收说明 |
| 研究领域 | 文本 | | 逗号分隔，如：人工智能,大数据 |
| 主要设备 | 文本 | | 逗号分隔的设备列表 |
| 排序 | 整数 | | 数值越小越靠前，默认0 |
| 状态 | 整数 | | 0-下架 1-展示，默认1 |

---

#### Sheet1 - laboratory_core_team 核心团队

| 表头名称 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| 实验室名称 | 文本 | Y | 外键，必须在Sheet0中存在 |
| 成员姓名 | 文本 | Y | 团队成员姓名 |
| 职务 | 文本 | | 如：教授/副教授/研究员 |
| 岗位名称 | 文本 | | 如：课题负责人/骨干成员 |

---

#### Sheet2 - laboratory_statistics 统计数据

| 表头名称 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| 实验室名称 | 文本 | Y | 外键，必须在Sheet0中存在 |
| 统计标签 | 文本 | Y | 如：发表论文数/获奖项目数/专利数 |
| 数量 | 整数 | Y | 统计数量 |

---

### xlsx3：学科评估 (1 Sheet)

#### Sheet0 - t_subject_evaluation主表

| 表头名称 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| 院校名称 | 文本 | Y | 外键，必须在universities表中存在 |
| 学科代码 | 文本 | Y | 学科代码，如：0812 |
| 学科名称 | 文本 | Y | 学科名称，如：计算机科学与技术 |
| 评估轮次 | 文本 | Y | 如：第五轮 |
| 评估等级 | 文本 | Y | A+/A/A-/B+/B/B-/C+/C/C- |
| 排序 | 整数 | | 数值越小越靠前，默认0 |
| 状态 | 整数 | | 0-下架 1-展示，默认1 |

---

## 接口列表

### 1. 院系管理接口（12个）

#### 1.1 分页查询院系列表

- **URL:** `GET /api/v1/admin/university/department/list`
- **方法:** GET
- **参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| universityName | String | | 院校名称（模糊搜索） |
| departmentName | String | | 院系名称（模糊搜索） |
| departmentType | String | | 院系类型（精确匹配） |
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
        "universityId": 1234567890123456788,
        "universityName": "清华大学",
        "departmentName": "计算机科学与技术系",
        "departmentType": "工学院",
        "pageTitle": "计算机科学与技术系就业报告",
        "tags": ["国家重点", "双一流学科"],
        "sortOrder": 1,
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

#### 1.2 获取院系详情

- **URL:** `GET /api/v1/admin/university/department/{id}`
- **方法:** GET
- **路径参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | Y | 院系ID |

- **响应:**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1234567890123456789,
    "universityId": 1234567890123456788,
    "universityName": "清华大学",
    "departmentName": "计算机科学与技术系",
    "departmentType": "工学院",
    "pageTitle": "计算机科学与技术系就业报告",
    "tags": ["国家重点", "双一流学科"],
    "sortOrder": 1,
    "status": 1,
    "report": {
      "subtitle": "2025年度就业质量报告",
      "citySalary": [
        {"cityName": "北京", "minSalary": 25.0, "maxSalary": 50.0},
        {"cityName": "上海", "minSalary": 22.0, "maxSalary": 45.0}
      ],
      "postgraduate": {
        "title": "考研深造方向",
        "directions": ["计算机系统结构", "计算机软件与理论", "计算机应用技术"]
      },
      "disclaimer": {
        "text": "本报告数据来源于...",
        "updateTime": "2025-12-01",
        "version": "V1.0",
        "compileUnit": "清华大学就业指导中心"
      },
      "prospects": {
        "employmentRate": "98.5%",
        "masterSalary": "25万元/年",
        "furtherStudyRate": "55%",
        "fortune500Rate": "40%",
        "salaryGrowthRate": "18%",
        "overseasRate": "25%"
      },
      "trends": {
        "highGrowthTracks": ["人工智能", "大数据", "云计算"],
        "policyOrientations": ["新基建", "数字经济"],
        "environmentAnalysis": ["人才需求旺盛", "薪资水平领先"]
      },
      "overview": {
        "title": "院系概述",
        "descriptions": ["清华大学计算机系成立于1958年...", "拥有计算机科学与技术一级学科博士点"]
      },
      "career": [
        {
          "pathTitle": "技术路线",
          "pathDesc": "从工程师到技术专家",
          "stages": [
            {
              "stageTitle": "初级阶段",
              "workYears": "0-3年",
              "position": "软件工程师",
              "coreGoal": "掌握核心技术",
              "salaryRange": "15-25"
            }
          ]
        }
      ],
      "subjectsDetail": [
        {
          "majorName": "计算机科学与技术",
          "tags": ["国家级特色专业", "热门"],
          "coreSubject": "计算机科学与技术",
          "supportSubject": "数学、电子信息",
          "positioning": "培养高层次计算机专业人才",
          "coreCourses": ["数据结构", "算法设计", "操作系统"],
          "abilities": ["编程能力", "系统设计能力", "算法分析能力"],
          "certificates": ["软件设计师", "系统架构师"]
        }
      ],
      "salary": [
        {"majorName": "计算机科学与技术", "minSalary": 18.0, "maxSalary": 40.0}
      ],
      "majorCompose": [
        {"subjectName": "计算机科学与技术", "percentage": 60.0},
        {"subjectName": "软件工程", "percentage": 25.0},
        {"subjectName": "人工智能", "percentage": 15.0}
      ]
    },
    "createdAt": "2026-05-07T10:00:00",
    "updatedAt": "2026-05-07T10:00:00"
  },
  "timestamp": 1234567890
}
```

---

#### 1.3 新增院系

- **URL:** `POST /api/v1/admin/university/department`
- **方法:** POST
- **请求体:**
```json
{
  "universityId": 1234567890123456788,
  "departmentName": "计算机科学与技术系",
  "departmentType": "工学院",
  "pageTitle": "计算机科学与技术系就业报告",
  "tags": ["国家重点", "双一流学科"],
  "sortOrder": 1
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

#### 1.4 修改院系基础信息

- **URL:** `PUT /api/v1/admin/university/department/{id}`
- **方法:** PUT
- **路径参数:** id - 院系ID
- **请求体:** 同新增院系

---

#### 1.5 切换院系状态

- **URL:** `PUT /api/v1/admin/university/department/{id}/status`
- **方法:** PUT
- **路径参数:** id - 院系ID
- **请求体:**
```json
{
  "status": 0
}
```

- **响应:**
```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1234567890
}
```

---

#### 1.6 软删除院系

- **URL:** `DELETE /api/v1/admin/university/department/{id}`
- **方法:** DELETE
- **说明:** 软删除，将status置为0

---

#### 1.7 硬删除院系

- **URL:** `DELETE /api/v1/admin/university/department/{id}/hard`
- **方法:** DELETE
- **说明:** 物理删除，同时删除关联的报告数据

---

#### 1.8 批量软删除院系

- **URL:** `DELETE /api/v1/admin/university/department/batch`
- **方法:** DELETE
- **请求体:**
```json
{
  "ids": [1234567890123456789, 1234567890123456790]
}
```

---

#### 1.9 批量硬删除院系

- **URL:** `DELETE /api/v1/admin/university/department/batch/hard`
- **方法:** DELETE
- **请求体:**
```json
{
  "ids": [1234567890123456789, 1234567890123456790]
}
```

---

#### 1.10 导入院系主表数据

- **URL:** `POST /api/v1/admin/university/department/import`
- **方法:** POST
- **Content-Type:** multipart/form-data
- **参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | Y | xlsx文件，最大30MB，Sheet0主表 |

- **表头规范:** 见 xlsx1 Sheet0：t_department主表

- **响应:**
```json
{
  "code": 200,
  "msg": "导入成功，共处理100条数据",
  "data": {
    "total": 100,
    "success": 98,
    "failed": 2,
    "errors": [
      "第5行：院校名称'未知大学'在主表中不存在",
      "第12行：院系名称不能为空"
    ]
  },
  "timestamp": 1234567890
}
```

---

#### 1.11 导入院系报告数据

- **URL:** `POST /api/v1/admin/university/department/import-report`
- **方法:** POST
- **Content-Type:** multipart/form-data
- **参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | Y | xlsx文件，最大30MB，包含Sheet1-11报告数据 |

- **表头规范:** 见 xlsx1 Sheet1-Sheet11

---

#### 1.12 修改院系报告数据

- **URL:** `PUT /api/v1/admin/university/department/{id}/report`
- **方法:** PUT
- **路径参数:** id - 院系ID
- **请求体:**
```json
{
  "subtitle": "2025年度就业质量报告",
  "citySalary": [
    {"cityName": "北京", "minSalary": 25.0, "maxSalary": 50.0}
  ],
  "postgraduate": {
    "title": "考研深造方向",
    "directions": ["计算机系统结构", "计算机软件与理论"]
  },
  "disclaimer": {
    "text": "本报告数据来源于...",
    "updateTime": "2025-12-01",
    "version": "V1.0",
    "compileUnit": "清华大学就业指导中心"
  },
  "prospects": {
    "employmentRate": "98.5%",
    "masterSalary": "25万元/年",
    "furtherStudyRate": "55%",
    "fortune500Rate": "40%",
    "salaryGrowthRate": "18%",
    "overseasRate": "25%"
  },
  "trends": {
    "highGrowthTracks": ["人工智能", "大数据"],
    "policyOrientations": ["新基建"],
    "environmentAnalysis": ["人才需求旺盛"]
  },
  "overview": {
    "title": "院系概述",
    "descriptions": ["清华大学计算机系成立于1958年..."]
  },
  "career": [...],
  "subjectsDetail": [...],
  "salary": [...],
  "majorCompose": [...]
}
```

---

### 2. 实验室管理接口（11个）

#### 2.1 分页查询实验室列表

- **URL:** `GET /api/v1/admin/university/laboratory/list`
- **方法:** GET
- **参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| universityName | String | | 院校名称（模糊搜索） |
| labName | String | | 实验室名称（模糊搜索） |
| labType | String | | 实验室类型（精确匹配） |
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
        "universityId": 1234567890123456788,
        "universityName": "清华大学",
        "labName": "智能技术与系统国家重点实验室",
        "labType": "国家重点实验室",
        "establishedDate": "1990-01-01",
        "region": "北京",
        "director": "张三",
        "staffSize": 150,
        "studentSize": 300,
        "researchAreas": ["人工智能", "机器学习", "自然语言处理"],
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

#### 2.2 获取实验室详情

- **URL:** `GET /api/v1/admin/university/laboratory/{id}`
- **方法:** GET
- **路径参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | Y | 实验室ID |

- **响应:**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1234567890123456789,
    "universityId": 1234567890123456788,
    "universityName": "清华大学",
    "labName": "智能技术与系统国家重点实验室",
    "labType": "国家重点实验室",
    "establishedDate": "1990-01-01",
    "region": "北京",
    "supervisingDepartment": "科技部",
    "director": "张三",
    "staffSize": 150,
    "studentSize": 300,
    "contactEmail": "lab@tsinghua.edu.cn",
    "contactPhone": "010-12345678",
    "introduction": "智能技术与系统国家重点实验室是...",
    "researchDescription": "主要从事人工智能基础理论...",
    "labSpace": "8000平方米",
    "openProjects": "年度开放课题...",
    "cooperation": "与MIT、斯坦福等建立合作",
    "visitingScholar": "每年接收访问学者20人",
    "researchAreas": ["人工智能", "机器学习", "自然语言处理"],
    "mainEquipment": ["高性能计算集群", "GPU服务器"],
    "coreTeam": [
      {"memberName": "李四", "position": "教授", "role": "课题负责人"},
      {"memberName": "王五", "position": "副教授", "role": "骨干成员"}
    ],
    "statistics": [
      {"label": "发表论文数", "count": 500},
      {"label": "获奖项目数", "count": 30},
      {"label": "专利数", "count": 100}
    ],
    "sortOrder": 1,
    "status": 1,
    "createdAt": "2026-05-07T10:00:00",
    "updatedAt": "2026-05-07T10:00:00"
  },
  "timestamp": 1234567890
}
```

---

#### 2.3 新增实验室

- **URL:** `POST /api/v1/admin/university/laboratory`
- **方法:** POST
- **请求体:**
```json
{
  "universityId": 1234567890123456788,
  "labName": "智能技术与系统国家重点实验室",
  "labType": "国家重点实验室",
  "establishedDate": "1990-01-01",
  "region": "北京",
  "supervisingDepartment": "科技部",
  "director": "张三",
  "staffSize": 150,
  "studentSize": 300,
  "contactEmail": "lab@tsinghua.edu.cn",
  "contactPhone": "010-12345678",
  "introduction": "智能技术与系统国家重点实验室是...",
  "researchDescription": "主要从事人工智能基础理论...",
  "labSpace": "8000平方米",
  "openProjects": "年度开放课题...",
  "cooperation": "与MIT、斯坦福等建立合作",
  "visitingScholar": "每年接收访问学者20人",
  "researchAreas": ["人工智能", "机器学习"],
  "mainEquipment": ["高性能计算集群", "GPU服务器"],
  "sortOrder": 1
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

#### 2.4 修改实验室信息

- **URL:** `PUT /api/v1/admin/university/laboratory/{id}`
- **方法:** PUT
- **路径参数:** id - 实验室ID
- **请求体:** 同新增实验室

---

#### 2.5 切换实验室状态

- **URL:** `PUT /api/v1/admin/university/laboratory/{id}/status`
- **方法:** PUT
- **路径参数:** id - 实验室ID
- **请求体:**
```json
{
  "status": 0
}
```

---

#### 2.6 软删除实验室

- **URL:** `DELETE /api/v1/admin/university/laboratory/{id}`
- **方法:** DELETE
- **说明:** 软删除，将status置为0

---

#### 2.7 硬删除实验室

- **URL:** `DELETE /api/v1/admin/university/laboratory/{id}/hard`
- **方法:** DELETE
- **说明:** 物理删除，同时删除关联的核心团队和统计数据

---

#### 2.8 批量软删除实验室

- **URL:** `DELETE /api/v1/admin/university/laboratory/batch`
- **方法:** DELETE
- **请求体:**
```json
{
  "ids": [1234567890123456789, 1234567890123456790]
}
```

---

#### 2.9 批量硬删除实验室

- **URL:** `DELETE /api/v1/admin/university/laboratory/batch/hard`
- **方法:** DELETE
- **请求体:**
```json
{
  "ids": [1234567890123456789, 1234567890123456790]
}
```

---

#### 2.10 导入实验室数据

- **URL:** `POST /api/v1/admin/university/laboratory/import`
- **方法:** POST
- **Content-Type:** multipart/form-data
- **参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | Y | xlsx文件，最大30MB，包含3个Sheet |

- **表头规范:** 见 xlsx2：实验室管理（Sheet0主表、Sheet1核心团队、Sheet2统计数据）

- **响应:**
```json
{
  "code": 200,
  "msg": "导入成功，共处理50条数据",
  "data": {
    "total": 50,
    "success": 48,
    "failed": 2,
    "errors": [
      "第3行：院校名称'未知大学'在主表中不存在",
      "第8行：实验室名称不能为空"
    ]
  },
  "timestamp": 1234567890
}
```

---

#### 2.11 修改实验室关联数据

- **URL:** `PUT /api/v1/admin/university/laboratory/{id}/relations`
- **方法:** PUT
- **路径参数:** id - 实验室ID
- **请求体:**
```json
{
  "coreTeam": [
    {"memberName": "李四", "position": "教授", "role": "课题负责人"},
    {"memberName": "王五", "position": "副教授", "role": "骨干成员"}
  ],
  "statistics": [
    {"label": "发表论文数", "count": 500},
    {"label": "获奖项目数", "count": 30}
  ]
}
```

---

### 3. 学科评估管理接口（10个）

#### 3.1 分页查询学科评估列表

- **URL:** `GET /api/v1/admin/university/subject-evaluation/list`
- **方法:** GET
- **参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| universityName | String | | 院校名称（模糊搜索） |
| subjectCode | String | | 学科代码（精确匹配） |
| subjectName | String | | 学科名称（模糊搜索） |
| evaluationRound | String | | 评估轮次（精确匹配） |
| evaluationGrade | String | | 评估等级（精确匹配） |
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
        "universityId": 1234567890123456788,
        "universityName": "清华大学",
        "subjectCode": "0812",
        "subjectName": "计算机科学与技术",
        "evaluationRound": "第五轮",
        "evaluationGrade": "A+",
        "sortOrder": 1,
        "status": 1,
        "createdAt": "2026-05-07T10:00:00"
      }
    ],
    "total": 200,
    "size": 10,
    "current": 1,
    "pages": 20
  },
  "timestamp": 1234567890
}
```

---

#### 3.2 获取学科评估详情

- **URL:** `GET /api/v1/admin/university/subject-evaluation/{id}`
- **方法:** GET
- **路径参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | Y | 学科评估ID |

- **响应:**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1234567890123456789,
    "universityId": 1234567890123456788,
    "universityName": "清华大学",
    "subjectCode": "0812",
    "subjectName": "计算机科学与技术",
    "evaluationRound": "第五轮",
    "evaluationGrade": "A+",
    "sortOrder": 1,
    "status": 1,
    "createdAt": "2026-05-07T10:00:00",
    "updatedAt": "2026-05-07T10:00:00"
  },
  "timestamp": 1234567890
}
```

---

#### 3.3 新增学科评估

- **URL:** `POST /api/v1/admin/university/subject-evaluation`
- **方法:** POST
- **请求体:**
```json
{
  "universityId": 1234567890123456788,
  "subjectCode": "0812",
  "subjectName": "计算机科学与技术",
  "evaluationRound": "第五轮",
  "evaluationGrade": "A+",
  "sortOrder": 1
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

#### 3.4 修改学科评估

- **URL:** `PUT /api/v1/admin/university/subject-evaluation/{id}`
- **方法:** PUT
- **路径参数:** id - 学科评估ID
- **请求体:** 同新增学科评估

---

#### 3.5 切换学科评估状态

- **URL:** `PUT /api/v1/admin/university/subject-evaluation/{id}/status`
- **方法:** PUT
- **路径参数:** id - 学科评估ID
- **请求体:**
```json
{
  "status": 0
}
```

---

#### 3.6 软删除学科评估

- **URL:** `DELETE /api/v1/admin/university/subject-evaluation/{id}`
- **方法:** DELETE
- **说明:** 软删除，将status置为0

---

#### 3.7 硬删除学科评估

- **URL:** `DELETE /api/v1/admin/university/subject-evaluation/{id}/hard`
- **方法:** DELETE
- **说明:** 物理删除

---

#### 3.8 批量软删除学科评估

- **URL:** `DELETE /api/v1/admin/university/subject-evaluation/batch`
- **方法:** DELETE
- **请求体:**
```json
{
  "ids": [1234567890123456789, 1234567890123456790]
}
```

---

#### 3.9 批量硬删除学科评估

- **URL:** `DELETE /api/v1/admin/university/subject-evaluation/batch/hard`
- **方法:** DELETE
- **请求体:**
```json
{
  "ids": [1234567890123456789, 1234567890123456790]
}
```

---

#### 3.10 导入学科评估数据

- **URL:** `POST /api/v1/admin/university/subject-evaluation/import`
- **方法:** POST
- **Content-Type:** multipart/form-data
- **参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | Y | xlsx文件，最大30MB |

- **表头规范:** 见 xlsx3：学科评估

- **响应:**
```json
{
  "code": 200,
  "msg": "导入成功，共处理200条数据",
  "data": {
    "total": 200,
    "success": 198,
    "failed": 2,
    "errors": [
      "第15行：院校名称'未知大学'在主表中不存在",
      "第28行：评估等级'S'不在有效范围内"
    ]
  },
  "timestamp": 1234567890
}
```

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
  "msg": "导入失败：第3行：'院校名称'不能为空；第5行：院校名称'未知大学'在主表中不存在",
  "data": null,
  "timestamp": 1234567890
}
```

---

## 附录

### 评估等级枚举

| 等级 | 说明 |
|------|------|
| A+ | 前2%（或前2名） |
| A | 2%~5% |
| A- | 5%~10% |
| B+ | 10%~20% |
| B | 20%~30% |
| B- | 30%~40% |
| C+ | 40%~50% |
| C | 50%~60% |
| C- | 60%~70% |

### 实验室类型枚举

| 类型 | 说明 |
|------|------|
| 国家重点实验室 | 国家科技部认定 |
| 教育部重点实验室 | 教育部认定 |
| 省级重点实验室 | 省级科技厅认定 |
| 国家工程实验室 | 国家发改委认定 |
| 国家工程研究中心 | 国家发改委认定 |

### 院系类型枚举

| 类型 | 说明 |
|------|------|
| 工学院 | 工程技术类 |
| 理学院 | 理科类 |
| 文学院 | 文科类 |
| 商学院 | 商科管理类 |
| 法学院 | 法律类 |
| 医学院 | 医学类 |
| 艺术学院 | 艺术类 |
| 农学院 | 农业类 |
| 教育学院 | 教育类 |
