# 专业管理模块设计规格

## 概述

本文档描述专业管理模块的设计规格，包含专业列表、专业详情、考研专业三个子模块。

**基础路径：** `/api/v1/admin/major`

**模块说明：**

| 子模块 | 数据表 | 关系 |
|--------|--------|------|
| 专业列表 | t_major + t_major_detail | 1:1 |
| 考研专业 | t_postgrad_major | 独立 |
| 考研专业-大学 | t_postgrad_major_university | N:N 关联表 |

---

## 1. 数据库设计

### 1.1 Flyway迁移文件

**文件名：** `V7__create_major_tables.sql`

**主键策略：** 雪花算法（BIGINT）

**软删除字段：** `status SMALLINT DEFAULT 1`（1=启用，0=禁用）

### 1.2 表结构

#### t_major（专业主表）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | Y | 主键（雪花算法） |
| major_code | VARCHAR(20) | Y | 专业代码（唯一） |
| major_name | VARCHAR(100) | Y | 专业名称 |
| discipline_name | VARCHAR(100) | | 学科名称 |
| major_type | VARCHAR(30) | Y | 专业类型（本科/专科） |
| major_category | VARCHAR(50) | | 学科门类 |
| parent_category | VARCHAR(50) | | 专业类 |
| major_tags | VARCHAR(50) | | 专业标签 |
| degree_awarded | VARCHAR(50) | | 授予学位 |
| study_duration | VARCHAR(20) | | 学制 |
| employment_rate | NUMERIC(5,2) | | 就业率（0-100） |
| salary_min | INTEGER | | 薪资下限（元/月） |
| salary_max | INTEGER | | 薪资上限（元/月） |
| description | TEXT | | 专业描述 |
| status | SMALLINT | Y | 状态（1=启用，0=禁用） |
| created_at | TIMESTAMPTZ | Y | 创建时间 |
| updated_at | TIMESTAMPTZ | Y | 更新时间 |

**索引：**
- `uk_major_code` - 唯一索引
- `idx_major_name` - 专业名称模糊搜索
- `idx_major_category` - 学科门类筛选
- `idx_major_type` - 专业类型筛选
- `idx_major_employment_rate` - 就业率排序
- `idx_major_parent_category` - 专业类筛选

---

#### t_major_detail（专业详情表）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | Y | 主键（雪花算法） |
| major_id | BIGINT | Y | 关联专业表ID（一对一） |
| course_count | INTEGER | | 课程数量 |
| graduate_scale | VARCHAR(20) | | 毕业生规模 |
| male_ratio | NUMERIC(5,2) | | 男生比例 |
| female_ratio | NUMERIC(5,2) | | 女生比例 |
| major_description | TEXT | | 专业描述 |
| training_objective | TEXT | | 培养目标 |
| training_requirement | TEXT | | 培养要求 |
| subject_requirement | TEXT | | 学科要求 |
| career_prospect | TEXT | | 就业前景 |
| main_courses | TEXT[] | | 主要课程 |
| knowledge_skills | TEXT[] | | 知识能力 |
| status | SMALLINT | Y | 状态 |
| created_at | TIMESTAMPTZ | Y | 创建时间 |
| updated_at | TIMESTAMPTZ | Y | 更新时间 |

**约束：**
- `uk_major_detail_major_id` - major_id唯一
- `chk_male_ratio` - 0-100范围
- `chk_female_ratio` - 0-100范围

---

#### t_postgrad_major（考研专业表）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | Y | 主键（雪花算法） |
| major_name | VARCHAR(100) | Y | 专业名称 |
| major_code | VARCHAR(20) | Y | 专业代码（唯一） |
| degree_type | VARCHAR(20) | Y | 学位类型 |
| discipline_category | VARCHAR(50) | Y | 学科门类 |
| popularity | VARCHAR(10) | | 热度 |
| difficulty | VARCHAR(10) | | 难度 |
| brief | TEXT | | 专业简介 |
| introduction | TEXT | | 专业介绍 |
| exam_subjects | TEXT[] | | 考试科目 |
| admission_requirements | TEXT[] | | 报考要求 |
| cross_exam_difficulty | VARCHAR(10) | | 跨考难度 |
| cross_exam_description | TEXT | | 跨考说明 |
| cross_exam_factors | TEXT[] | | 跨考因素 |
| status | SMALLINT | Y | 状态 |
| created_at | TIMESTAMPTZ | Y | 创建时间 |
| updated_at | TIMESTAMPTZ | Y | 更新时间 |

**约束：**
- `uk_postgrad_major_code` - 专业代码唯一
- `chk_degree_type` - IN ('学术学位', '专业学位')
- `chk_popularity` - IN ('热门', '一般', '冷门')
- `chk_pg_difficulty` - IN ('高', '中', '低')
- `chk_cross_exam_difficulty` - IN ('较易', '中等', '较难')

---

#### t_postgrad_major_university（关联表）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | Y | 主键（雪花算法） |
| postgrad_major_id | BIGINT | Y | 考研专业ID |
| university_id | BIGINT | Y | 大学ID |
| university_name | VARCHAR(100) | Y | 大学名称（冗余） |
| postgrad_major_name | VARCHAR(100) | Y | 考研专业名称（冗余） |
| sort_order | INTEGER | | 排序权重 |
| status | SMALLINT | Y | 状态 |
| created_at | TIMESTAMPTZ | Y | 创建时间 |

**约束：**
- `uk_pg_major_university` - (postgrad_major_id, university_id) 唯一

---

## 2. 架构设计

### 2.1 包结构

```
haifeng-admin/
├── controller/major/
│   ├── MajorController.java              # 专业主表+详情（合并）
│   ├── PostgradMajorController.java      # 考研专业
│   └── PostgradMajorUniversityController.java  # 关联表
├── service/major/
│   ├── MajorService.java
│   ├── PostgradMajorService.java
│   └── PostgradMajorUniversityService.java
├── service/impl/major/
│   ├── MajorServiceImpl.java
│   ├── PostgradMajorServiceImpl.java
│   └── PostgradMajorUniversityServiceImpl.java
├── dto/major/
│   ├── MajorQueryDTO.java
│   ├── MajorAddDTO.java
│   ├── MajorDetailAddDTO.java
│   ├── MajorImportDTO.java
│   ├── MajorDetailImportDTO.java
│   ├── PostgradMajorQueryDTO.java
│   ├── PostgradMajorAddDTO.java
│   ├── PostgradMajorImportDTO.java
│   └── PostgradMajorUniversityImportDTO.java
└── vo/major/
    ├── MajorListVO.java
    ├── MajorDetailVO.java
    ├── PostgradMajorListVO.java
    ├── PostgradMajorDetailVO.java
    └── PostgradMajorUniversityListVO.java

haifeng-common/
├── entity/major/
│   ├── Major.java
│   ├── MajorDetail.java
│   ├── PostgradMajor.java
│   └── PostgradMajorUniversity.java
└── mapper/major/
    ├── MajorMapper.java
    ├── MajorDetailMapper.java
    ├── PostgradMajorMapper.java
    └── PostgradMajorUniversityMapper.java
```

---

## 3. 接口设计

### 3.1 专业管理接口 (MajorController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /list | 分页查询（模糊：major_code/name/discipline_name/type/category/status） |
| GET | /{id} | 获取详情（返回主表+详情表，tabs展示） |
| POST | / | 新增专业（主表） |
| PUT | /{id} | 修改专业（主表） |
| PUT | /{id}/status | 切换状态（禁用/启用） |
| DELETE | /{id} | 软删除（status=0） |
| DELETE | /{id}/hard | 硬删除（物理删除） |
| DELETE | /batch | 批量软删除 |
| DELETE | /batch/hard | 批量硬删除 |
| PUT | /{id}/detail | 修改专业详情（详情表） |
| POST | /import | 导入专业主表xlsx |
| POST | /import-detail | 导入专业详情xlsx |
| PUT | /{id}/restore | 恢复已禁用数据（status=1） |

### 3.2 考研专业接口 (PostgradMajorController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /postgrad/list | 分页查询 |
| GET | /postgrad/{id} | 获取详情 |
| POST | /postgrad | 新增 |
| PUT | /postgrad/{id} | 修改 |
| PUT | /postgrad/{id}/status | 切换状态 |
| DELETE | /postgrad/{id} | 软删除 |
| DELETE | /postgrad/{id}/hard | 硬删除 |
| DELETE | /postgrad/batch | 批量软删除 |
| DELETE | /postgrad/batch/hard | 批量硬删除 |
| POST | /postgrad/import | 导入xlsx |
| PUT | /postgrad/{id}/restore | 恢复 |

### 3.3 考研专业-大学关联接口 (PostgradMajorUniversityController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /postgrad-university/list | 分页查询 |
| DELETE | /postgrad-university/{id} | 软删除 |
| DELETE | /postgrad-university/{id}/hard | 硬删除 |
| DELETE | /postgrad-university/batch | 批量软删除 |
| DELETE | /postgrad-university/batch/hard | 批量硬删除 |
| POST | /postgrad-university/import | 导入xlsx |
| PUT | /postgrad-university/{id}/restore | 恢复 |

---

## 4. Excel导入规范

### xlsx1：专业主表 (t_major)

| 表头名称 | 字段 | DB类型 | 必填 | 说明 |
|---------|------|--------|------|------|
| 专业代码 | major_code | VARCHAR(20) | Y | 唯一 |
| 专业名称 | major_name | VARCHAR(100) | Y | |
| 学科名称 | discipline_name | VARCHAR(100) | | |
| 专业类型 | major_type | VARCHAR(30) | Y | 本科/专科 |
| 学科门类 | major_category | VARCHAR(50) | | 如：工学、理学 |
| 专业类 | parent_category | VARCHAR(50) | | 如：计算机类 |
| 专业标签 | major_tags | VARCHAR(50) | | 直接存储 |
| 授予学位 | degree_awarded | VARCHAR(50) | | |
| 学制 | study_duration | VARCHAR(20) | | 如：四年 |
| 就业率 | employment_rate | NUMERIC(5,2) | | 0-100 |
| 薪资下限 | salary_min | INTEGER | | 元/月 |
| 薪资上限 | salary_max | INTEGER | | 元/月 |
| 专业描述 | description | TEXT | | |

### xlsx2：专业详情表 (t_major_detail)

| 表头名称 | 字段 | DB类型 | 必填 | 说明 |
|---------|------|--------|------|------|
| 专业代码 | (逻辑外键) | - | Y | 必须在t_major中存在 |
| 课程数量 | course_count | INTEGER | | |
| 毕业生规模 | graduate_scale | VARCHAR(20) | | |
| 男生比例 | male_ratio | NUMERIC(5,2) | | 0-100 |
| 女生比例 | female_ratio | NUMERIC(5,2) | | 0-100 |
| 专业描述 | major_description | TEXT | | |
| 培养目标 | training_objective | TEXT | | |
| 培养要求 | training_requirement | TEXT | | |
| 学科要求 | subject_requirement | TEXT | | |
| 就业前景 | career_prospect | TEXT | | |
| 主要课程 | main_courses | TEXT[] | | 逗号分隔→数组 |
| 知识能力 | knowledge_skills | TEXT[] | | 逗号分隔→数组 |

### xlsx3：考研专业表 (t_postgrad_major)

| 表头名称 | 字段 | DB类型 | 必填 | 说明 |
|---------|------|--------|------|------|
| 专业名称 | major_name | VARCHAR(100) | Y | |
| 专业代码 | major_code | VARCHAR(20) | Y | 唯一 |
| 学位类型 | degree_type | VARCHAR(20) | Y | 学术学位/专业学位 |
| 学科门类 | discipline_category | VARCHAR(50) | Y | |
| 热度 | popularity | VARCHAR(10) | | 热门/一般/冷门 |
| 难度 | difficulty | VARCHAR(10) | | 高/中/低 |
| 专业简介 | brief | TEXT | | |
| 专业介绍 | introduction | TEXT | | |
| 考试科目 | exam_subjects | TEXT[] | | 逗号分隔→数组 |
| 报考要求 | admission_requirements | TEXT[] | | 逗号分隔→数组 |
| 跨考难度 | cross_exam_difficulty | VARCHAR(10) | | 较易/中等/较难 |
| 跨考说明 | cross_exam_description | TEXT | | |
| 跨考因素 | cross_exam_factors | TEXT[] | | 逗号分隔→数组 |

### xlsx4：考研专业-大学关联表 (t_postgrad_major_university)

| 表头名称 | 字段 | DB类型 | 必填 | 说明 |
|---------|------|--------|------|------|
| 大学名称 | university_name | VARCHAR(100) | Y | 逻辑外键→t_universities |
| 考研专业代码 | (逻辑外键) | - | Y | 逻辑外键→t_postgrad_major |
| 排序权重 | sort_order | INTEGER | | 默认0 |

---

## 5. 错误处理与事务设计

### 5.1 导入校验流程

```
Excel导入 → 表头校验 → 数据校验 → 外键校验 → 批量插入
                ↓           ↓           ↓           ↓
             不符合规范   类型错误    外键不存在   事务回滚
                ↓           ↓           ↓           ↓
            收集错误信息 → 抛出BusinessException → 返回详细错误
```

### 5.2 校验规则

| 校验类型 | 规则 | 错误示例 |
|---------|------|---------|
| 表头校验 | 必须包含所有必填表头 | "缺少必填表头：专业代码" |
| 唯一性校验 | 唯一字段不能重复 | "第5行：专业代码'080901'已存在" |
| 类型校验 | 数值/日期格式正确 | "第8行：就业率'abc'格式错误" |
| 范围校验 | 枚举值在范围内 | "第12行：学位类型'硕士'不在[学术学位,专业学位]范围内" |
| 外键校验 | 外键必须存在 | "第3行：专业代码'999999'在主表中不存在" |
| 业务校验 | salary_min ≤ salary_max | "第7行：薪资下限大于薪资上限" |

### 5.3 事务策略

```java
@Transactional(rollbackFor = Exception.class)
public ImportResultVO importMajor(MultipartFile file) {
    List<String> errors = new ArrayList<>();

    // 1. 解析Excel
    // 2. 校验每行数据，收集错误
    // 3. 如果有错误，抛出异常触发回滚
    if (!errors.isEmpty()) {
        throw new BusinessException(String.join("; ", errors));
    }
    // 4. 批量插入
    // 5. 返回成功统计
}
```

### 5.4 导入响应格式

成功：
```json
{
  "code": 200,
  "msg": "导入成功，共处理100条数据",
  "data": {
    "total": 100,
    "success": 100,
    "failed": 0,
    "errors": []
  }
}
```

失败：
```json
{
  "code": 400,
  "msg": "导入失败",
  "data": {
    "total": 100,
    "success": 0,
    "failed": 5,
    "errors": [
      "第3行：专业代码'080901'已存在",
      "第5行：学位类型'硕士'不在有效范围内"
    ]
  }
}
```

---

## 6. 枚举定义

### 6.1 专业类型 (major_type)

| 值 | 说明 |
|----|------|
| 本科 | 本科专业 |
| 专科 | 专科专业 |

### 6.2 学位类型 (degree_type)

| 值 | 说明 |
|----|------|
| 学术学位 | 学术型硕士/博士 |
| 专业学位 | 专业型硕士 |

### 6.3 热度 (popularity)

| 值 | 说明 |
|----|------|
| 热门 | 报考人数多 |
| 一般 | 报考人数适中 |
| 冷门 | 报考人数少 |

### 6.4 难度 (difficulty)

| 值 | 说明 |
|----|------|
| 高 | 高难度 |
| 中 | 中等难度 |
| 低 | 低难度 |

### 6.5 跨考难度 (cross_exam_difficulty)

| 值 | 说明 |
|----|------|
| 较易 | 跨考难度低 |
| 中等 | 跨考难度中等 |
| 较难 | 跨考难度高 |

---

## 7. 关键技术点

### 7.1 TEXT[]数组转换

使用自定义EasyExcel Converter处理逗号分隔转数组：

```java
public class StringToArrayConverter implements Converter<String[]> {
    @Override
    public String[] convertToJavaData(ReadCellData<?> cellData, ...) {
        String val = cellData.getStringValue();
        if (val == null || val.trim().isEmpty()) {
            return new String[0];
        }
        // 兼容中英文逗号
        return val.split("[,，]");
    }
}
```

### 7.2 专业与详情的事务一致性

专业主表和详情表是1:1关系，查询和修改时需要保证事务一致性。

### 7.3 逻辑外键处理

导入时通过major_code/university_name查找对应的ID，而非直接传入ID。

---

## 8. 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录/Token过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
