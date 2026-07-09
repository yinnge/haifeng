# 考研专业关联管理模块

## 模块概述

本模块实现本科专业与考研方向的多对多关联管理，属于「专业管理」父模块下的「专业考研关联」子模块。

## 数据表

| 表名 | 说明 |
|------|------|
| t_major_postgrad_direction | 本科专业-考研方向关联表 |

### 表结构

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键（雪花算法） |
| major_id | BIGINT | 本科专业ID |
| postgrad_major_id | BIGINT | 考研专业ID |
| major_name | VARCHAR(100) | 本科专业名称 |
| postgrad_major_name | VARCHAR(100) | 考研专业名称 |
| sort_order | INTEGER | 推荐排序 |
| created_at | TIMESTAMPTZ | 创建时间 |

## API 接口清单

基础路径：`/api/v1/admin/major-postgrad-direction`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list` | 分页查询列表 |
| GET | `/{id}` | 获取详情 |
| POST | `/add` | 新增关联 |
| PUT | `/{id}` | 修改关联 |
| DELETE | `/{id}` | 删除关联（硬删除） |
| POST | `/batch-delete` | 批量删除（硬删除） |
| POST | `/import` | xlsx批量导入 |

### 1. 分页查询列表

**请求**
```
GET /api/v1/admin/major-postgrad-direction/list?page=1&size=10&majorName=计算机
```

**参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |
| majorName | String | 否 | 本科专业名称（模糊） |
| postgradMajorName | String | 否 | 考研专业名称（模糊） |

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": "1234567890",
        "majorName": "计算机科学与技术",
        "postgradMajorName": "计算机科学与技术",
        "createdAt": "2026-05-09T10:00:00+08:00"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1
  }
}
```

### 2. 获取详情

**请求**
```
GET /api/v1/admin/major-postgrad-direction/{id}
```

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": "1234567890",
    "majorId": "111",
    "postgradMajorId": "222",
    "majorName": "计算机科学与技术",
    "postgradMajorName": "计算机科学与技术",
    "sortOrder": 0,
    "createdAt": "2026-05-09T10:00:00+08:00"
  }
}
```

### 3. 新增关联

**请求**
```
POST /api/v1/admin/major-postgrad-direction/add
Content-Type: application/json

{
  "majorId": 111,
  "postgradMajorId": 222,
  "sortOrder": 0
}
```

### 4. 修改关联

**请求**
```
PUT /api/v1/admin/major-postgrad-direction/{id}
Content-Type: application/json

{
  "majorId": 111,
  "postgradMajorId": 333,
  "sortOrder": 1
}
```

### 5. 删除关联

**请求**
```
DELETE /api/v1/admin/major-postgrad-direction/{id}
```

### 6. 批量删除

**请求**
```
POST /api/v1/admin/major-postgrad-direction/batch-delete
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**请求参数：**
```json
{
  "ids": [1234567890, 1234567891]
}
```

### 7. xlsx批量导入

**请求**
```
POST /api/v1/admin/major-postgrad-direction/import
Content-Type: multipart/form-data

file: [Excel文件]
```

**响应**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 100,
    "success": 95,
    "failed": 5,
    "errors": [
      "第3行: 本科专业[xxx]不存在",
      "第7行: [计算机科学与技术, 软件工程]关联已存在"
    ]
  }
}
```

## xlsx 导入模板

| 本科专业名称 | 考研专业名称 | 排序权重 |
|--------------|--------------|----------|
| 计算机科学与技术 | 计算机科学与技术 | 0 |
| 软件工程 | 软件工程 | 1 |

**字段说明**
| 列名 | 必填 | 说明 |
|------|------|------|
| 本科专业名称 | 是 | 需在t_major表中存在 |
| 考研专业名称 | 是 | 需在t_postgrad_major表中存在 |
| 排序权重 | 否 | 默认0，数值越小越靠前 |

## 实现文件清单

| 类型 | 文件 |
|------|------|
| Entity | `haifeng-common/.../entity/major/MajorPostgradDirection.java` |
| Mapper | `haifeng-common/.../mapper/major/MajorPostgradDirectionMapper.java` |
| Controller | `haifeng-admin/.../controller/major/MajorPostgradDirectionController.java` |
| Service | `haifeng-admin/.../service/major/MajorPostgradDirectionService.java` |
| ServiceImpl | `haifeng-admin/.../service/impl/major/MajorPostgradDirectionServiceImpl.java` |
| QueryDTO | `haifeng-admin/.../dto/major/MajorPostgradDirectionQueryDTO.java` |
| AddDTO | `haifeng-admin/.../dto/major/MajorPostgradDirectionAddDTO.java` |
| UpdateDTO | `haifeng-admin/.../dto/major/MajorPostgradDirectionUpdateDTO.java` |
| ImportDTO | `haifeng-admin/.../dto/major/MajorPostgradDirectionImportDTO.java` |
| ListVO | `haifeng-admin/.../vo/major/MajorPostgradDirectionListVO.java` |
| DetailVO | `haifeng-admin/.../vo/major/MajorPostgradDirectionDetailVO.java` |
| Flyway | `haifeng-admin/.../db/migration/V10__create_major_postgrad_direction.sql` |
