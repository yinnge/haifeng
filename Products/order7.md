# 专业管理模块接口文档

## 概述

本文档描述专业管理模块的接口，包含专业列表、考研专业、考研专业-大学关联三个子模块。

**基础路径：** `/api/v1/admin`

**模块说明：**
| 子模块 | 数据表 | 关系 |
|--------|--------|------|
| 专业列表 | t_major + t_major_detail | 1:1 |
| 考研专业 | t_postgrad_major | 独立表 |
| 考研专业-大学关联 | t_postgrad_major_university | N:N（关联t_postgrad_major和t_universities） |

---

## Excel导入表头规范

### xlsx1：专业主表 (t_major)

| 表头名称 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| 专业代码 | 文本 | Y | 唯一标识，如：080901 |
| 专业名称 | 文本 | Y | 专业全称 |
| 学科名称 | 文本 | N | 所属学科名称 |
| 专业类型 | 文本 | Y | 本科/专科 |
| 学科门类 | 文本 | N | 如：工学、理学 |
| 专业类 | 文本 | N | 专业所属类别 |
| 专业标签 | 文本 | N | 专业标签 |
| 授予学位 | 文本 | N | 如：工学学士 |
| 就业率 | 小数 | N | 0-100之间 |
| 薪资下限 | 整数 | N | 元/月 |
| 薪资上限 | 整数 | N | 元/月，需≥薪资下限 |
| 专业描述 | 文本 | N | 专业介绍 |

---

### xlsx2：专业详情表 (t_major_detail)

| 表头名称 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| 专业代码 | 文本 | Y | 逻辑外键，必须在t_major表中存在 |
| 课程数量 | 整数 | N | 课程总数 |
| 毕业生规模 | 文本 | N | 如：5000-10000人 |
| 男生比例 | 小数 | N | 0-100之间 |
| 女生比例 | 小数 | N | 0-100之间 |
| 专业描述 | 文本 | N | 详细描述 |
| 培养目标 | 文本 | N | 培养目标说明 |
| 培养要求 | 文本 | N | 培养要求说明 |
| 学科要求 | 文本 | N | 学科要求说明 |
| 职业前景 | 文本 | N | 就业前景分析 |
| 主要课程 | 文本 | N | 逗号分隔，如：高等数学,线性代数 |
| 知识技能 | 文本 | N | 逗号分隔，如：编程能力,数据分析 |

---

### xlsx3：考研专业表 (t_postgrad_major)

| 表头名称 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| 专业名称 | 文本 | Y | 专业全称 |
| 专业代码 | 文本 | Y | 唯一标识 |
| 学位类型 | 文本 | Y | 学术学位/专业学位 |
| 学科门类 | 文本 | Y | 如：工学、理学 |
| 热门程度 | 文本 | N | 热门/一般/冷门 |
| 难度等级 | 文本 | N | 高/中/低 |
| 专业简介 | 文本 | N | 简短介绍 |
| 详细介绍 | 文本 | N | 详细介绍 |
| 考试科目 | 文本 | N | 逗号分隔，如：政治,英语,数学 |
| 录取条件 | 文本 | N | 逗号分隔 |
| 跨考难度 | 文本 | N | 较易/中等/较难 |
| 跨考说明 | 文本 | N | 跨考说明文字 |
| 跨考因素 | 文本 | N | 逗号分隔 |

---

### xlsx4：考研专业-大学关联表 (t_postgrad_major_university)

| 表头名称 | 类型 | 必填 | 说明 |
|---------|------|------|------|
| 大学名称 | 文本 | Y | 逻辑外键，必须在t_universities表中存在 |
| 考研专业代码 | 文本 | Y | 逻辑外键，必须在t_postgrad_major表中存在 |
| 排序权重 | 整数 | N | 数值越小越靠前，默认0 |

---

## 接口列表

### 一、专业管理接口 (MajorController)

**路径前缀：** `/api/v1/admin/major`

| 序号 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 1 | GET | /list | 分页查询专业列表 |
| 2 | GET | /{id} | 获取专业详情（含详情表数据） |
| 3 | POST | / | 新增专业 |
| 4 | PUT | /{id} | 修改专业基础信息 |
| 5 | PUT | /{id}/detail | 修改专业详情信息 |
| 6 | PUT | /{id}/status | 修改专业状态（禁用/启用） |
| 7 | DELETE | /{id} | 软删除专业 |
| 8 | DELETE | /{id}/hard | 硬删除专业 |
| 9 | DELETE | /batch | 批量软删除 |
| 10 | DELETE | /batch/hard | 批量硬删除 |
| 11 | POST | /import | 导入专业主表xlsx |
| 12 | POST | /import-detail | 导入专业详情xlsx |
| 13 | PUT | /{id}/restore | 恢复已禁用的专业 |

---

#### 1. 分页查询专业列表

**请求：**
```http
GET /api/v1/admin/major/list?page=1&size=10&majorCode=080901&majorName=计算机&majorType=本科&status=1
```

**响应：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1234567890,
        "majorCode": "080901",
        "majorName": "计算机科学与技术",
        "disciplineName": "计算机类",
        "majorType": "本科",
        "majorCategory": "工学",
        "status": 1,
        "createdAt": "2026-05-07T10:00:00+08:00"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1
  }
}
```

---

#### 2. 获取专业详情

**请求：**
```http
GET /api/v1/admin/major/1234567890
```

**响应：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1234567890,
    "majorCode": "080901",
    "majorName": "计算机科学与技术",
    "disciplineName": "计算机类",
    "majorType": "本科",
    "majorCategory": "工学",
    "parentCategory": "计算机类",
    "majorTags": "热门",
    "degreeAwarded": "工学学士",
    "employmentRate": 95.50,
    "salaryMin": 8000,
    "salaryMax": 15000,
    "description": "培养计算机领域高级人才",
    "status": 1,
    "createdAt": "2026-05-07T10:00:00+08:00",
    "updatedAt": "2026-05-07T10:00:00+08:00",
    "detailId": 1234567891,
    "courseCount": 50,
    "graduateScale": "10000-20000人",
    "maleRatio": 70.00,
    "femaleRatio": 30.00,
    "majorDescription": "详细描述...",
    "trainingObjective": "培养目标...",
    "trainingRequirement": "培养要求...",
    "subjectRequirement": "学科要求...",
    "careerProspect": "就业前景...",
    "mainCourses": ["高等数学", "数据结构", "操作系统"],
    "knowledgeSkills": ["编程能力", "算法设计"]
  }
}
```

---

#### 3. 新增专业

**请求：**
```http
POST /api/v1/admin/major
Content-Type: application/json

{
  "majorCode": "080901",
  "majorName": "计算机科学与技术",
  "majorType": "本科",
  "disciplineName": "计算机类",
  "majorCategory": "工学"
}
```

**响应：**
```json
{
  "code": 200,
  "msg": "success",
  "data": 1234567890
}
```

---

#### 11. 导入专业主表xlsx

**请求：**
```http
POST /api/v1/admin/major/import
Content-Type: multipart/form-data

file: [xlsx文件]
```

**响应：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 100,
    "success": 100,
    "failed": 0,
    "errors": []
  }
}
```

---

### 二、考研专业接口 (PostgradMajorController)

**路径前缀：** `/api/v1/admin/postgrad-major`

| 序号 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 1 | GET | /list | 分页查询考研专业列表 |
| 2 | GET | /{id} | 获取考研专业详情 |
| 3 | POST | / | 新增考研专业 |
| 4 | PUT | /{id} | 修改考研专业 |
| 5 | PUT | /{id}/status | 修改状态 |
| 6 | DELETE | /{id} | 软删除 |
| 7 | DELETE | /{id}/hard | 硬删除 |
| 8 | DELETE | /batch | 批量软删除 |
| 9 | DELETE | /batch/hard | 批量硬删除 |
| 10 | POST | /import | 导入考研专业xlsx |
| 11 | PUT | /{id}/restore | 恢复 |

---

#### 1. 分页查询考研专业列表

**请求：**
```http
GET /api/v1/admin/postgrad-major/list?page=1&size=10&majorName=计算机&degreeType=学术学位&popularity=热门
```

**响应：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1234567890,
        "majorName": "计算机科学与技术",
        "majorCode": "0812",
        "degreeType": "学术学位",
        "disciplineCategory": "工学",
        "popularity": "热门",
        "difficulty": "高",
        "status": 1,
        "createdAt": "2026-05-07T10:00:00+08:00"
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1
  }
}
```

---

### 三、考研专业-大学关联接口 (PostgradMajorUniversityController)

**路径前缀：** `/api/v1/admin/postgrad-major-university`

| 序号 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 1 | GET | /list | 分页查询关联列表 |
| 2 | DELETE | /{id} | 软删除 |
| 3 | DELETE | /{id}/hard | 硬删除 |
| 4 | DELETE | /batch | 批量软删除 |
| 5 | DELETE | /batch/hard | 批量硬删除 |
| 6 | POST | /import | 导入关联xlsx |
| 7 | PUT | /{id}/restore | 恢复 |

---

#### 1. 分页查询关联列表

**请求：**
```http
GET /api/v1/admin/postgrad-major-university/list?page=1&size=10&universityName=清华&postgradMajorName=计算机
```

**响应：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1234567890,
        "universityName": "清华大学",
        "postgradMajorName": "计算机科学与技术",
        "sortOrder": 1,
        "status": 1,
        "createdAt": "2026-05-07T10:00:00+08:00"
      }
    ],
    "total": 200,
    "size": 10,
    "current": 1
  }
}
```

---

## 通用说明

### 状态码

| 状态值 | 说明 |
|--------|------|
| 1 | 启用/正常 |
| 0 | 禁用/软删除 |

### 删除说明

- **软删除**：将status设为0，数据保留，可恢复
- **硬删除**：永久删除数据，不可恢复
- **恢复**：将status设为1，恢复已软删除的数据

### 批量操作

批量删除请求体格式：
```json
{
  "ids": [1234567890, 1234567891, 1234567892]
}
```

### 导入错误处理

当Excel导入存在错误时，返回详细错误信息：
```json
{
  "code": 400,
  "msg": "第2行：专业代码已存在; 第5行：薪资下限大于薪资上限",
  "data": null
}
```

---

## 接口总数

| 子模块 | 接口数量 |
|--------|----------|
| 专业管理 | 13 |
| 考研专业 | 11 |
| 考研专业-大学关联 | 7 |
| **总计** | **31** |
