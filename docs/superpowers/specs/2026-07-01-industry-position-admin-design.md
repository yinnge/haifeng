# 行业专项招聘 — 管理端实现设计

## 来源需求
`Need/16行业专项招聘.md`

## 一句话描述
实现 admin 端对教师招聘、医疗卫生、银行/金融三张行业招聘表的完整 CRUD + Excel 导入功能。

## 涉及表
| 表名 | 实体类 | 模块子包 |
|------|--------|---------|
| t_teacher_position | TeacherPosition | industryPosition |
| t_healthcare_position | HealthcarePosition | industryPosition |
| t_finance_position | FinancePosition | industryPosition |

## 接口清单（每表7个，共21个）

| # | 接口 | HTTP | 路径 | @OperationLog | @RequireLogin |
|---|------|------|------|:------------:|:------------:|
| 1 | 分页查询 | GET | /list | ✗ | teacher/healthcare ✓, finance ✗ |
| 2 | 详情 | GET | /{id}/detail | ✗ | ✓ |
| 3 | 修改 | PUT | /{id}/update | ✓ | ✓ |
| 4 | 物理删除 | DELETE | /{id}/delete | ✓ | ✓ |
| 5 | 启用/禁用 | PATCH | /{id}/status | ✓ | ✓ |
| 6 | 批量物理删除 | DELETE | /batch-delete | ✓ | ✓ |
| 7 | 导入 xlsx | POST | /import | ✓ | ✓ |

> 额外接口：`POST /pre-validate` — 导入前校验 Excel 格式

## 包结构

```
haifeng-admin/src/main/java/com/haifeng/admin/
├── controller/employment/industryPosition/
│   ├── TeacherPositionController.java        @ /api/v1/admin/employment/industry-position/teacher
│   ├── HealthcarePositionController.java     @ /api/v1/admin/employment/industry-position/healthcare
│   └── FinancePositionController.java        @ /api/v1/admin/employment/industry-position/finance
├── service/employment/industryPosition/
│   ├── TeacherPositionService.java
│   ├── HealthcarePositionService.java
│   └── FinancePositionService.java
├── service/impl/employment/industryPosition/
│   ├── TeacherPositionServiceImpl.java
│   ├── HealthcarePositionServiceImpl.java
│   └── FinancePositionServiceImpl.java
├── dto/employment/industryPosition/
│   ├── teacher/
│   │   ├── TeacherPositionQueryDTO.java       (extend BasePageQueryDTO)
│   │   ├── TeacherPositionAddDTO.java
│   │   ├── TeacherPositionUpdateDTO.java
│   │   └── TeacherPositionStatusDTO.java
│   ├── healthcare/
│   │   ├── HealthcarePositionQueryDTO.java
│   │   ├── HealthcarePositionAddDTO.java
│   │   ├── HealthcarePositionUpdateDTO.java
│   │   └── HealthcarePositionStatusDTO.java
│   └── finance/
│       ├── FinancePositionQueryDTO.java
│       ├── FinancePositionAddDTO.java
│       ├── FinancePositionUpdateDTO.java
│       └── FinancePositionStatusDTO.java
├── vo/employment/industryPosition/
│   ├── teacher/
│   │   ├── TeacherPositionListVO.java
│   │   └── TeacherPositionDetailVO.java
│   ├── healthcare/
│   │   ├── HealthcarePositionListVO.java
│   │   └── HealthcarePositionDetailVO.java
│   └── finance/
│       ├── FinancePositionListVO.java
│       └── FinancePositionDetailVO.java
└── excel/employment/industryPosition/
    ├── TeacherPositionExcelDTO.java
    ├── HealthcarePositionExcelDTO.java
    └── FinancePositionExcelDTO.java
```

## 列表查询规则

### 每页条数选项
10, 20, 30, 50, 100

### 排序
```java
wrapper.orderByDesc(Entity::getSortOrder);
wrapper.orderByDesc(Entity::getCreatedAt);
```

### 筛选规则（AND 关系）

**TeacherPosition：**
| 参数 | 操作 | 类型 |
|------|------|------|
| schoolName | LIKE | 模糊 |
| positionName | LIKE | 模糊 |
| schoolType | EQ | 精准 |
| province | EQ | 精准 |
| city | EQ | 精准 |
| district | EQ | 精准 |
| positionStatus | EQ | 精准 |

**HealthcarePosition：**
| 参数 | 操作 | 类型 |
|------|------|------|
| institutionName | LIKE | 模糊 |
| positionName | LIKE | 模糊 |
| institutionNature | EQ | 精准 |
| department | EQ | 精准 |
| province | EQ | 精准 |
| city | EQ | 精准 |
| district | EQ | 精准 |
| positionStatus | EQ | 精准 |

**FinancePosition：**
| 参数 | 操作 | 类型 |
|------|------|------|
| institutionName | LIKE | 模糊 |
| positionName | LIKE | 模糊 |
| institutionCategory | EQ | 精准 |
| institutionType | EQ | 精准 |
| province | EQ | 精准 |
| city | EQ | 精准 |
| positionStatus | EQ | 精准 |

## 禁用/启用设计
- `PATCH /{id}/status` 请求体 `{ "status": 1 }`（启用）或 `{ "status": 0 }`（禁用）
- 禁用 → `is_deleted = true`；启用 → `is_deleted = false`
- 已禁用的记录不出现在分页列表中
- 启用操作还需恢复 `updated_at` 时间戳

## Excel 导入设计

### 校验接口 POST /pre-validate
1. 接收 `MultipartFile`
2. 用 EasyExcel 同步读取为 ExcelDTO
3. 逐行校验：必填非空、枚举值合法性、数值范围、日期格式
4. 返回错误列表（行号+错误描述）或空列表（校验通过）

### 导入接口 POST /import
1. 同解析+校验
2. 有错误 → 抛 BusinessException(400, 汇总错误信息)
3. 无错误 → `@Transactional` 批量插入（SnowflakeIdGenerator.nextId()）
4. 全部成功或全部回滚

### 类型转换
- TEXT[] 字段（FinancePosition）：`@ExcelProperty(converter = StringArrayConverter.class)`
- 日期/数值：EasyExcel 自动转换

## 注意事项
- 所有实体/mapper已在 haifeng-common 中存在，无需创建
- 省份校验复用 ProvinceEnum.isValid()
- 删除/修改时先检查记录存在性，不存在抛404
- 操作日志使用 `@OperationLog(module = "行业专项招聘", action = "xxx")`
- 不涉及 git 操作，最后统一提交
