# 基层服务岗位模块设计文档

## 概述
在 admin 端实现三个基层服务岗位的管理模块：基层服务项目（三支一扶+西部计划）、社区工作者、公益性岗位。
功能包括：分页查询、详情、修改、物理删除、禁用/启用、批量删除、Excel导入/预校验。

## 包结构

```
haifeng-admin/.../controller/employment/grassrootsPosition/
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
  ├── GrassrootsProjectPositionQueryDTO.java
  ├── GrassrootsProjectPositionUpdateDTO.java
  ├── CommunityPositionQueryDTO.java
  ├── CommunityPositionUpdateDTO.java
  ├── PublicWelfarePositionQueryDTO.java
  ├── PublicWelfarePositionUpdateDTO.java
  └── StatusDTO.java

vo/employment/grassrootsPosition/
  ├── GrassrootsProjectPositionListVO.java
  ├── GrassrootsProjectPositionDetailVO.java
  ├── CommunityPositionListVO.java
  ├── CommunityPositionDetailVO.java
  ├── PublicWelfarePositionListVO.java
  └── PublicWelfarePositionDetailVO.java

excel/employment/grassrootsPosition/
  ├── GrassrootsProjectPositionExcelDTO.java
  ├── CommunityPositionExcelDTO.java
  └── PublicWelfarePositionExcelDTO.java
```

## API端点

### 基层服务项目 (GrassrootsProjectPosition)
前缀: `/api/v1/admin/employment/grassroots-position/project`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/list` | 分页查询 | @RequireLogin |
| GET | `/{id}/detail` | 详情 | @RequireLogin |
| PUT | `/{id}/update` | 修改 | @RequireLogin |
| DELETE | `/{id}/delete` | 物理删除 | @RequireLogin |
| PATCH | `/{id}/status` | 禁用/启用 | @RequireLogin |
| DELETE | `/batch-delete` | 批量删除 | @RequireLogin |
| POST | `/pre-validate` | 校验xlsx | @RequireLogin |
| POST | `/import` | 导入xlsx | @RequireLogin |

### 社区工作者 (CommunityPosition)
前缀: `/api/v1/admin/employment/grassroots-position/community`
接口同上

### 公益性岗位 (PublicWelfarePosition)
前缀: `/api/v1/admin/employment/grassroots-position/welfare`
接口同上，但 `/list` 不加 @RequireLogin（公开），其余需要登录

## 查询字段

### 基层服务项目
列表字段: id, projectType, year, positionName, serviceType, organizingDept, serviceUnit, province, city, county, positionStatus
模糊: positionName, organizingDept, serviceUnit
精确: projectType, year, serviceType, province, city, county, positionStatus

### 社区工作者
列表字段: id, communityName, positionName, supervisingDept, positionType, province, city, positionStatus
模糊: positionName, communityName, supervisingDept
精确: positionType, province, city, positionStatus

### 公益性岗位
列表字段: id, developingUnit, employingUnit, positionName, positionCategory, province, city, district, monthlySalary, regStartDate, regEndDate, positionStatus
模糊: positionName, developingUnit, employingUnit
精确: positionCategory, province, city, district, maxServiceYears, positionStatus

## 排序规则
所有分页: `sort_order ASC, updated_at DESC`

## 关键实现约定
- 禁用: 设置 is_deleted = true
- 删除: 物理删除 (deleteById)
- Excel导入: 校验 → 收集错误 → 有错抛异常 → 无错批量插入
- 预校验: 同路径，返回错误信息或null
- ID: SnowflakeIdGenerator.nextId()
- OperationLog注解: 增删改操作加注解
- PublicWelfarePosition.targetGroup: TEXT[] 字段，Excel导入用 StringToArrayConverter

## 已确认的设计决策
- 包名统一用复数 grassrootsPosition
- API路径用 kebab-case: grassroots-position
- 公益性岗位列表公开，其他接口需登录
