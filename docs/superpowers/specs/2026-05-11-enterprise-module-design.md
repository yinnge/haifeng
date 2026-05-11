# 企业模块设计文档

## 概述

实现后台管理端的企业管理模块，包含企业列表管理和企业-行业关联管理两个子模块。

## 需求来源

- 需求文档：`Need/15企业模块.md`
- 数据表设计：`db/migration/table/DataTable15.md`

## 设计决策

### 已确认的变更

1. **删除 `idx_ent_industry` 索引** - 企业和行业的关系完全通过 `t_enterprise_industry` 关联表管理
2. **删除 `city_id` 字段** - 只保留 `city_name`，前端跳转城市详情通过城市名查询即可，简化导入逻辑
3. **xlsx导入方式** - 企业+岗位使用单文件多Sheet方式（Sheet0=企业，Sheet1=岗位）
4. **TEXT[]数组字段** - 使用逗号分隔字符串，系统自动转换
5. **详情页Tabs** - 两个Tab（基本信息 + 岗位列表）

## 数据库设计

### 表结构

#### t_enterprise（企业表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | SERIAL | 主键（导入时用雪花ID） |
| city_name | VARCHAR(50) | 城市名称 |
| enterprise_name | VARCHAR(200) | 企业名称（唯一） |
| enterprise_nature | VARCHAR(30) | 企业性质（央企/国企/民企/外企/合资） |
| enterprise_type | VARCHAR(50) | 企业类型 |
| logo_url | VARCHAR(500) | Logo地址 |
| official_website | VARCHAR(500) | 官网 |
| region | VARCHAR(100) | 总部地区 |
| enterprise_scale | VARCHAR(50) | 企业规模 |
| main_business | VARCHAR(500) | 主营业务 |
| enterprise_intro | TEXT | 企业简介 |
| recruitment_status | VARCHAR(20) | 招聘状态（默认"招聘中"） |
| is_deleted | BOOLEAN | 软删除标记 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

#### t_enterprise_position（企业岗位表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | SERIAL | 主键 |
| enterprise_id | INTEGER | 企业ID（逻辑外键） |
| position_name | VARCHAR(200) | 岗位名称 |
| recruitment_type | VARCHAR(30) | 招聘类型（校招/社招/实习） |
| position_requirement | TEXT | 岗位要求 |
| position_tags | TEXT[] | 岗位标签 |
| province | VARCHAR(30) | 省份 |
| city | VARCHAR(50) | 城市 |
| work_location | VARCHAR(200) | 工作地点 |
| education_requirement | VARCHAR(30) | 学历要求 |
| major_requirement | VARCHAR(500) | 专业要求 |
| work_experience | VARCHAR(50) | 工作经验 |
| salary_min | INTEGER | 最低薪资（k/月） |
| salary_max | INTEGER | 最高薪资（k/月） |
| apply_link | VARCHAR(500) | 申请链接 |
| deadline | TIMESTAMPTZ | 截止日期 |
| position_status | VARCHAR(20) | 岗位状态 |
| is_deleted | BOOLEAN | 软删除标记 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

#### t_enterprise_industry（企业-行业关联表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | SERIAL | 主键 |
| enterprise_id | INTEGER | 企业ID |
| enterprise_name | VARCHAR(200) | 企业名称（冗余） |
| industry_id | INTEGER | 行业ID |
| industry_name | VARCHAR(100) | 行业名称（冗余） |
| is_primary | BOOLEAN | 是否主行业 |
| sort_order | SMALLINT | 排序 |
| created_at | TIMESTAMPTZ | 创建时间 |

### 索引设计

**t_enterprise:**
- `idx_ent_city_nature_type` - (city_name, enterprise_nature, enterprise_type)
- `idx_ent_nature` - (enterprise_nature)
- `idx_ent_name_pattern` - (enterprise_name varchar_pattern_ops) 模糊搜索
- `idx_ent_region` - (region)

**t_enterprise_position:**
- `idx_ep_enterprise` - (enterprise_id)
- `idx_ep_position_name` - (position_name varchar_pattern_ops)
- `idx_ep_recruitment_type` - (recruitment_type)
- `idx_ep_location` - (province, city)
- `idx_ep_education` - (education_requirement)
- `idx_ep_deadline` - (deadline)
- `idx_ep_tags` - GIN索引
- `idx_ep_salary` - (salary_min, salary_max)

**t_enterprise_industry:**
- `idx_ei_enterprise` - (enterprise_id)
- `idx_ei_industry` - (industry_id)
- `uk_ent_ind` - 唯一约束 (enterprise_id, industry_id)

## 包结构

```
haifeng-admin/
├── controller/company/
│   ├── EnterpriseController.java
│   └── EnterpriseIndustryController.java
├── service/company/
│   ├── EnterpriseService.java
│   └── EnterpriseIndustryService.java
├── service/impl/company/
│   ├── EnterpriseServiceImpl.java
│   └── EnterpriseIndustryServiceImpl.java
├── dto/company/
│   ├── EnterpriseQueryDTO.java
│   ├── EnterpriseAddDTO.java
│   ├── EnterpriseUpdateDTO.java
│   ├── EnterpriseStatusDTO.java
│   ├── EnterpriseBatchDeleteDTO.java
│   ├── EnterpriseIndustryQueryDTO.java
│   └── EnterpriseIndustryBatchDeleteDTO.java
├── vo/company/
│   ├── EnterpriseListVO.java
│   ├── EnterpriseDetailVO.java
│   ├── EnterprisePositionVO.java
│   ├── EnterpriseIndustryListVO.java
│   └── EnterpriseIndustryDetailVO.java
└── excel/company/
    ├── EnterpriseExcelDTO.java
    ├── EnterprisePositionExcelDTO.java
    ├── EnterpriseIndustryExcelDTO.java
    └── StringToArrayConverter.java

haifeng-common/
├── entity/company/
│   ├── Enterprise.java
│   ├── EnterprisePosition.java
│   └── EnterpriseIndustry.java
└── mapper/company/
    ├── EnterpriseMapper.java
    ├── EnterprisePositionMapper.java
    └── EnterpriseIndustryMapper.java
```

## API设计

### EnterpriseController

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/admin/company/enterprise/list` | 分页列表 |
| GET | `/api/v1/admin/company/enterprise/{id}` | 详情（含岗位列表） |
| POST | `/api/v1/admin/company/enterprise` | 新增 |
| PUT | `/api/v1/admin/company/enterprise/{id}` | 修改 |
| PUT | `/api/v1/admin/company/enterprise/{id}/status` | 禁用/启用 |
| DELETE | `/api/v1/admin/company/enterprise/{id}` | 硬删除 |
| DELETE | `/api/v1/admin/company/enterprise/batch` | 批量硬删除 |
| POST | `/api/v1/admin/company/enterprise/import` | xlsx导入 |

**列表查询参数：**
- cityName - 模糊查询
- enterpriseName - 模糊查询
- enterpriseNature - 精准查询（央企/国企/民企/外企/合资）
- enterpriseType - 模糊查询
- recruitmentStatus - 精准查询
- isDeleted - 精准查询

### EnterpriseIndustryController

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/admin/company/enterprise-industry/list` | 分页列表 |
| GET | `/api/v1/admin/company/enterprise-industry/{id}` | 详情 |
| DELETE | `/api/v1/admin/company/enterprise-industry/{id}` | 硬删除 |
| DELETE | `/api/v1/admin/company/enterprise-industry/batch` | 批量硬删除 |
| POST | `/api/v1/admin/company/enterprise-industry/import` | xlsx导入 |

**列表查询参数：**
- enterpriseName - 模糊查询
- industryName - 模糊查询

## xlsx导入设计

### 企业+岗位导入（单文件多Sheet）

**Sheet0 - 企业主表：**
| 城市名称 | 企业名称 | 企业性质 | 企业类型 | Logo地址 | 官网 | 总部地区 | 企业规模 | 主营业务 | 企业简介 | 招聘状态 |
|----------|----------|----------|----------|----------|------|----------|----------|----------|----------|----------|

**Sheet1 - 企业岗位：**
| 企业名称 | 岗位名称 | 招聘类型 | 岗位要求 | 岗位标签 | 省份 | 城市 | 工作地点 | 学历要求 | 专业要求 | 工作经验 | 最低薪资 | 最高薪资 | 申请链接 | 截止日期 | 岗位状态 |
|----------|----------|----------|----------|----------|------|------|----------|----------|----------|----------|----------|----------|----------|----------|----------|

**导入流程：**
1. 读取Sheet0企业数据
2. 校验enterprise_name不重复（文件内 + 数据库）
3. 读取Sheet1岗位数据
4. 校验岗位的enterprise_name在Sheet0中存在
5. 生成雪花ID，建立企业名->ID映射
6. 批量插入企业表
7. 批量插入岗位表
8. 错误处理：收集所有错误 → 事务回滚

### 企业-行业关联导入

**Sheet0 - 关联表：**
| 企业名称 | 行业名称 |
|----------|----------|

**导入流程：**
1. 读取数据
2. 校验enterprise_name存在于t_enterprise
3. 校验industry_name存在于t_industry
4. 校验(enterprise_name, industry_name)不重复
5. 查找对应的enterprise_id和industry_id
6. 批量插入
7. 错误处理：收集所有错误 → 事务回滚

## 错误处理

### xlsx导入错误格式

```
第3行：企业名称不能为空
第5行：企业名称'腾讯科技'在文件中重复
第7行：企业名称'华为技术'已存在于数据库
Sheet1第4行：企业名称'未知企业'在Sheet0中不存在
第6行：企业性质必须是：央企/国企/民企/外企/合资
第2行：企业名称'xxx'不存在
第3行：行业名称'yyy'不存在
第5行：企业'xxx'-行业'yyy'关联已存在
```

### 事务保证

- 所有导入方法添加 `@Transactional(rollbackFor = Exception.class)`
- 任何错误都回滚，不会有部分数据入库

## 实现任务

1. 创建 V15__enterprise__tables.sql（修改后的版本，去掉city_id和idx_ent_industry）
2. 创建 Entity 类（Enterprise, EnterprisePosition, EnterpriseIndustry）
3. 创建 Mapper 接口
4. 创建 DTO 和 VO 类
5. 创建 Excel DTO 和 Converter
6. 实现 EnterpriseService 和 EnterpriseServiceImpl
7. 实现 EnterpriseIndustryService 和 EnterpriseIndustryServiceImpl
8. 创建 Controller 并添加 @OperationLog 注解
