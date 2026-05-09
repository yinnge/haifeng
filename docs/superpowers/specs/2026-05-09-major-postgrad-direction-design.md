# 考研专业关联管理模块设计文档

## 概述

本模块实现本科专业与考研方向的多对多关联管理，属于「专业管理」父模块下的「专业考研关联」子模块。

## 需求来源

- 需求文档：`Need/11考研专业关联管理.md`

## 数据库设计

### 表结构：t_major_postgrad_direction

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PRIMARY KEY | 主键（雪花算法） |
| major_id | BIGINT | NOT NULL | 本科专业ID（关联 t_major.id） |
| postgrad_major_id | BIGINT | NOT NULL | 考研专业ID（关联 t_postgrad_major.id） |
| major_name | VARCHAR(100) | NOT NULL | 本科专业名称（冗余） |
| postgrad_major_name | VARCHAR(100) | NOT NULL | 考研专业名称（冗余） |
| sort_order | INTEGER | DEFAULT 0 | 推荐排序 |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | 创建时间 |

**约束**：
- `uk_major_postgrad UNIQUE (major_id, postgrad_major_id)` - 防止重复关联

**索引**：
- `idx_mpd_major` - major_id 索引
- `idx_mpd_postgrad` - postgrad_major_id 索引
- `idx_mpd_major_name` - major_name 模糊查询索引
- `idx_mpd_postgrad_name` - postgrad_major_name 模糊查询索引

### Flyway 迁移

文件：`V10__create_major_postgrad_direction.sql`

## 后端架构

### 文件结构

```
haifeng-common/
├── entity/major/
│   └── MajorPostgradDirection.java
└── mapper/major/
    └── MajorPostgradDirectionMapper.java

haifeng-admin/
├── controller/major/
│   └── MajorPostgradDirectionController.java
├── service/major/
│   └── MajorPostgradDirectionService.java
├── service/impl/major/
│   └── MajorPostgradDirectionServiceImpl.java
├── dto/major/
│   ├── MajorPostgradDirectionQueryDTO.java
│   ├── MajorPostgradDirectionAddDTO.java
│   ├── MajorPostgradDirectionUpdateDTO.java
│   └── MajorPostgradDirectionImportDTO.java
└── vo/major/
    ├── MajorPostgradDirectionListVO.java
    └── MajorPostgradDirectionDetailVO.java
```

### 复用组件

- `ImportResultVO` - 导入结果 VO
- `BatchDeleteDTO` - 批量删除 DTO
- `BasePageQueryDTO` - 分页查询基类

## API 接口设计

### 基础路径

`/api/v1/admin/major-postgrad-direction`

### 接口清单

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/list` | 分页列表 | 管理员 |
| GET | `/{id}` | 获取详情 | 管理员 |
| POST | `/add` | 新增关联 | 管理员 |
| PUT | `/{id}` | 修改关联 | 管理员 |
| DELETE | `/{id}` | 硬删除单条 | 管理员 |
| DELETE | `/batch` | 批量硬删除 | 管理员 |
| POST | `/import` | xlsx 批量导入 | 管理员 |

### 接口详情

#### 1. 分页列表

**请求**：
```
GET /api/v1/admin/major-postgrad-direction/list
```

**参数**：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| size | Integer | 否 | 每页数量，默认 10 |
| majorName | String | 否 | 本科专业名称（模糊查询） |
| postgradMajorName | String | 否 | 考研专业名称（模糊查询） |

**响应**：
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

#### 2. 获取详情

**请求**：
```
GET /api/v1/admin/major-postgrad-direction/{id}
```

**响应**：
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

#### 3. 新增关联

**请求**：
```
POST /api/v1/admin/major-postgrad-direction/add
Content-Type: application/json

{
  "majorId": 111,
  "postgradMajorId": 222,
  "sortOrder": 0
}
```

**说明**：majorName 和 postgradMajorName 由后端根据 ID 自动查询填充。

#### 4. 修改关联

**请求**：
```
PUT /api/v1/admin/major-postgrad-direction/{id}
Content-Type: application/json

{
  "majorId": 111,
  "postgradMajorId": 333,
  "sortOrder": 1
}
```

#### 5. 硬删除单条

**请求**：
```
DELETE /api/v1/admin/major-postgrad-direction/{id}
```

#### 6. 批量硬删除

**请求**：
```
DELETE /api/v1/admin/major-postgrad-direction/batch
Content-Type: application/json

{
  "ids": [1234567890, 1234567891]
}
```

#### 7. xlsx 批量导入

**请求**：
```
POST /api/v1/admin/major-postgrad-direction/import
Content-Type: multipart/form-data

file: [Excel文件]
```

**响应**：
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

## xlsx 导入规范

### 模板格式

| 本科专业名称 | 考研专业名称 | 排序权重 |
|--------------|--------------|----------|
| 计算机科学与技术 | 计算机科学与技术 | 0 |
| 软件工程 | 软件工程 | 1 |

### 字段说明

| Excel 列名 | DTO 字段 | 必填 | 说明 |
|------------|----------|------|------|
| 本科专业名称 | majorName | 是 | 联表查询 t_major 获取 major_id |
| 考研专业名称 | postgradMajorName | 是 | 联表查询 t_postgrad_major 获取 postgrad_major_id |
| 排序权重 | sortOrder | 否 | 默认 0 |

### 校验逻辑

1. **必填校验**：majorName、postgradMajorName 不能为空
2. **文件内去重**：检查文件内是否有重复的 (majorName, postgradMajorName) 组合
3. **外键校验**：
   - 根据 majorName 查询 t_major，获取 major_id，不存在则报错
   - 根据 postgradMajorName 查询 t_postgrad_major，获取 postgrad_major_id，不存在则报错
4. **唯一性校验**：检查数据库中是否已存在 (major_id, postgrad_major_id) 组合
5. **错误收集**：收集所有错误信息，返回详细的行号和错误原因

### 导入策略

- 采用「部分成功」模式：有效数据插入，无效数据跳过并记录错误
- 使用 `@Transactional` 保证单行插入的原子性
- 返回 ImportResultVO 包含成功数、失败数、错误详情

## Mapper 扩展方法

`MajorPostgradDirectionMapper` 需要的自定义方法：

```java
// 检查关联是否已存在
boolean existsByRelation(@Param("majorId") Long majorId,
                         @Param("postgradMajorId") Long postgradMajorId);
```

`MajorMapper` 需要的自定义方法（如不存在需新增）：

```java
// 根据专业名称查询ID
Long selectIdByName(@Param("majorName") String majorName);
```

`PostgradMajorMapper` 需要的自定义方法（如不存在需新增）：

```java
// 根据专业名称查询ID
Long selectIdByName(@Param("majorName") String majorName);
```

## 产出文档

- `Products/order11.md` - 接口和功能概述文档

## 技术要点

1. **ID 生成**：统一使用雪花算法
2. **删除策略**：硬删除（永久删除）
3. **导入框架**：EasyExcel
4. **事务管理**：`@Transactional(rollbackFor = Exception.class)`
5. **操作日志**：使用 `@OperationLog` 注解自动记录
