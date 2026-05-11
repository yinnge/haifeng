# 算法约束模块设计文档

## 概述

算法约束模块用于管理高考志愿填报系统中的约束规则，包括：
- 约束条件字典：定义各类约束规则（如色盲不能报医学类）
- 专业约束关联：建立专业与约束的多对多关系
- 安全系数等级：定义志愿填报的安全等级（冲/稳/保/垫）

## 模块结构

### 包结构

```
haifeng-admin/
├── controller/algorithm/constraint/
│   ├── ConstraintDictController.java
│   ├── MajorConstraintController.java
│   └── SafetyLevelController.java
├── service/algorithm/constraint/
│   ├── ConstraintDictService.java
│   ├── MajorConstraintService.java
│   └── SafetyLevelService.java
├── service/impl/algorithm/constraint/
│   ├── ConstraintDictServiceImpl.java
│   ├── MajorConstraintServiceImpl.java
│   └── SafetyLevelServiceImpl.java
├── dto/algorithm/constraint/
│   ├── ConstraintDictAddDTO.java
│   ├── ConstraintDictQueryDTO.java
│   ├── MajorConstraintAddDTO.java
│   ├── MajorConstraintQueryDTO.java
│   ├── SafetyLevelAddDTO.java
│   └── BatchDeleteDTO.java
├── vo/algorithm/constraint/
│   ├── ConstraintDictListVO.java
│   ├── ConstraintDictDetailVO.java
│   ├── MajorConstraintListVO.java
│   ├── MajorConstraintDetailVO.java
│   ├── SafetyLevelListVO.java
│   └── SafetyLevelDetailVO.java
└── excel/algorithm/constraint/
    └── MajorConstraintImportDTO.java

haifeng-common/
├── entity/algorithm/
│   ├── ConstraintDict.java
│   ├── MajorConstraint.java
│   └── SafetyLevelDict.java
└── mapper/algorithm/
    ├── ConstraintDictMapper.java
    ├── MajorConstraintMapper.java
    └── SafetyLevelDictMapper.java
```

## 数据库设计

### Flyway迁移文件

文件：`V13__algorithm_constraint.sql`

#### 1. 约束条件字典表 (t_constraint_dict)

| 字段 | 类型 | 说明 |
|------|------|------|
| code | VARCHAR(50) | 主键，约束代码 |
| name | VARCHAR(100) | 约束名称（唯一） |
| category | VARCHAR(30) | 约束大类 |
| description | TEXT | 详细说明 |
| severity | VARCHAR(10) | HARD/SOFT |
| check_field | VARCHAR(50) | 对应t_member_gaokao字段 |
| check_operator | VARCHAR(20) | 判断运算符 |
| check_value | VARCHAR(100) | 判断值 |
| extra_field | VARCHAR(50) | 附加条件字段 |
| extra_operator | VARCHAR(20) | 附加条件运算符 |
| extra_value | VARCHAR(100) | 附加条件值 |
| sort_order | INTEGER | 排序 |
| is_active | BOOLEAN | 是否启用 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

#### 2. 专业约束关联表 (t_major_constraint)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键（雪花算法） |
| major_code | VARCHAR(20) | 专业代码 |
| major_name | VARCHAR(50) | 专业名称（冗余） |
| constraint_code | VARCHAR(50) | 约束代码 |
| constraint_name | VARCHAR(50) | 约束名称（冗余） |
| remark | VARCHAR(200) | 备注 |
| created_at | TIMESTAMPTZ | 创建时间 |

唯一约束：(major_code, constraint_code)

#### 3. 安全系数等级字典 (t_safety_level_dict)

| 字段 | 类型 | 说明 |
|------|------|------|
| level | SMALLINT | 主键，等级编号1-5 |
| code | VARCHAR(20) | 代码（唯一） |
| name | VARCHAR(30) | 中文名称 |
| name_short | VARCHAR(10) | 简称 |
| min_coefficient | NUMERIC(3,2) | 系数下界 |
| max_coefficient | NUMERIC(3,2) | 系数上界 |
| color | VARCHAR(20) | 前端颜色 |
| confidence | VARCHAR(20) | 置信度 |
| confidence_reason | VARCHAR(150) | 置信度说明 |
| description | TEXT | 说明 |

## API设计

### 1. 约束字典 API

路径前缀：`/api/v1/admin/algorithm/constraint/dict`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /page | 分页列表 |
| GET | /{code} | 详情 |
| POST | / | 新增 |
| PUT | /{code} | 修改 |
| PUT | /{code}/toggle | 禁用/启用 |
| DELETE | /{code} | 删除（硬删除） |
| DELETE | /batch | 批量删除 |

列表展示字段：code, category, severity, check_field, is_active

### 2. 专业约束关联 API

路径前缀：`/api/v1/admin/algorithm/constraint/major`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /page | 分页列表（支持精准查询） |
| GET | /{id} | 详情 |
| POST | / | 新增 |
| DELETE | /{id} | 删除（硬删除） |
| DELETE | /batch | 批量删除 |
| POST | /import | Excel导入 |

列表展示字段：major_code, major_name, constraint_code, constraint_name
精准查询字段：major_code, major_name, constraint_code, constraint_name

### 3. 安全系数 API

路径前缀：`/api/v1/admin/algorithm/constraint/safety-level`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /page | 分页列表 |
| GET | /{level} | 详情 |
| POST | / | 新增 |
| PUT | /{level} | 修改 |
| DELETE | /{level} | 删除（硬删除） |
| DELETE | /batch | 批量删除 |

列表展示字段：level, code, name, name_short, min_coefficient, max_coefficient, confidence

## 业务规则

### check_field 白名单

约束字典的check_field只能是以下高考档案字段：

```java
public static final Set<String> VALID_CHECK_FIELDS = Set.of(
    // 选科信息
    "subject_type", "second_subject_type", "third_subject_type",
    // 各科成绩
    "score_chinese", "score_math", "score_english",
    "score_subject_1", "score_subject_2", "score_subject_3",
    // 视觉条件
    "is_color_blind", "is_color_weak", "vision_left", "vision_right", "has_smell_disorder",
    // 身体指标
    "height_cm", "weight_kg", "is_left_handed", "has_tattoo", "has_scar", "has_stutter",
    // 身份条件
    "is_fresh_graduate", "political_status", "household_type", "is_poverty_county",
    // 外语
    "foreign_language"
);
```

### check_operator 枚举

```java
public enum CheckOperator {
    EQ,       // 等于
    NEQ,      // 不等于
    LT,       // 小于
    LTE,      // 小于等于
    GT,       // 大于
    GTE,      // 大于等于
    IS_TRUE,  // 为真
    IS_FALSE, // 为假
    IN,       // 在列表中
    NOT_IN    // 不在列表中
}
```

### Excel导入规则

**Excel列**：专业名称, 约束名称, 备注

**导入流程**：
1. 校验Excel表头
2. 遍历每行：
   - 校验专业名称存在于t_major（通过major_name查找major_code）
   - 校验约束名称存在于t_constraint_dict（通过name查找code）
   - 检查Excel内(major_name, constraint_name)是否重复
3. 检查数据库(major_code, constraint_code)唯一约束
4. 事务批量插入

**错误处理**：
- 遇到错误收集到List，最后一次性抛出
- 格式："第X行: 专业名称[XXX]不存在"
- 任何错误都回滚整个导入

## 实现要点

1. **ID策略**：
   - 约束字典：code作为主键（VARCHAR）
   - 专业约束关联：雪花算法
   - 安全系数：level作为主键（SMALLINT）

2. **删除方式**：全部硬删除

3. **复用模式**：Excel导入复用`ScoreRankServiceImpl`的实现模式

4. **注解**：Controller写操作加`@OperationLog`注解

5. **权限**：仅管理员可访问（`@RequireLogin`）
