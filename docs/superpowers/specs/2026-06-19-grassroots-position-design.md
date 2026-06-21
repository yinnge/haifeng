# 基层服务岗位模块设计文档

## 概述
实现基层服务岗位的三个子模块（基层服务项目岗位/社区工作者/公益性岗位）的 C 端查询接口。

## 包结构

### haifeng-common

```
entity/employment/grassrootsPosition/
├── GrassrootsProjectPosition.java    ← t_grassroots_project_position
├── CommunityPosition.java            ← t_community_position
└── PublicWelfarePosition.java        ← t_public_welfare_position

mapper/employment/grassrootsPosition/
├── GrassrootsProjectPositionMapper.java
├── CommunityPositionMapper.java
└── PublicWelfarePositionMapper.java
```

### haifeng-app

```
controller/employment/grassrootsPosition/
├── GrassrootsProjectPositionController.java
├── CommunityPositionController.java
└── PublicWelfarePositionController.java

service/employment/grassrootsPosition/
├── GrassrootsProjectPositionService.java
├── CommunityPositionService.java
└── PublicWelfarePositionService.java

service/impl/employment/grassrootsPosition/
├── GrassrootsProjectPositionServiceImpl.java
├── CommunityPositionServiceImpl.java
└── PublicWelfarePositionServiceImpl.java

dto/employment/grassrootsPosition/
├── GrassrootsProjectPositionSearchDTO.java
├── CommunityPositionSearchDTO.java
└── PublicWelfarePositionSearchDTO.java

vo/employment/grassrootsPosition/
├── GrassrootsProjectPositionListVO.java
├── GrassrootsProjectPositionDetailVO.java
├── CommunityPositionListVO.java
├── CommunityPositionDetailVO.java
├── PublicWelfarePositionListVO.java
└── PublicWelfarePositionDetailVO.java
```

## API 设计

### 基层服务项目岗位 (GrassrootsProjectPosition)

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/v1/app/employment/grassroots/project/list` | ❌ | 分页列表 |
| GET | `/api/v1/app/employment/grassroots/project/{id}/detail` | ✅ @RequireLogin | 详情 |

**列表查询字段：**
- 模糊：position_name, organizing_dept, service_unit
- 精准：project_type, year, service_type, province, city, county, education_requirement, major_requirement, grad_year_requirement, political_status, position_status
- 范围：age_limit (ageLimitMin/ageLimitMax)

**列表返回字段：** id, project_type, year, position_name, service_type, organizing_dept, service_unit, province, city, county, township, service_period, education_requirement, major_requirement, age_limit, recruitment_count, political_status

**详情返回字段：** 表全部字段（按需求文档所列）

### 社区工作者岗位 (CommunityPosition)

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/v1/app/employment/grassroots/community/list` | ❌ | 分页列表 |
| GET | `/api/v1/app/employment/grassroots/community/{id}/detail` | ✅ @RequireLogin | 详情 |

**列表查询字段：**
- 模糊：position_name, street_office, community_name, supervising_dept
- 精准：position_type, employment_type, province, city, education_requirement, major_requirement, political_status, work_experience, position_status
- 范围：age_limit (ageLimitMin/ageLimitMax)

**列表返回字段：** id, community_name, district, position_name, education_requirement, major_requirement, position_type, province, city, age_limit, recruitment_count, work_experience

### 公益性岗位 (PublicWelfarePosition)

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/v1/app/employment/grassroots/welfare/list` | ❌ | 分页列表 |
| GET | `/api/v1/app/employment/grassroots/welfare/{id}/detail` | ✅ @RequireLogin | 详情 |

**列表查询字段：**
- 模糊：position_name, developing_unit, employing_unit
- 精准：position_category, province, city, district, education_requirement, household_requirement, max_service_years, position_status, target_group
- 范围：age_range (ageRangeMin/ageRangeMax)

**列表返回字段：** id, developing_unit, employing_unit, position_name, position_category, province, city, district, education_requirement, recruitment_count, monthly_salary, contract_period, max_service_years, reg_start_date, reg_end_date

## 数据流

```
Controller (DTO @Valid)
  → Service (LambdaQueryWrapper<Entity> 拼装条件)
    → Mapper.selectPage(page, wrapper)
      → page.convert(entity -> ListVO.builder()...build())
    → Mapper.selectById(id)
      → 转 DetailVO
```

## 关键约束
1. 所有列表接口无需登录（DTO无token校验）
2. 所有详情接口需 @RequireLogin
3. 软删除过滤：wrapper.eq(Entity::getIsDeleted, false)
4. 模糊查询 + 精准查询 是 AND 关系
5. 三个模块互不依赖，各自独立
