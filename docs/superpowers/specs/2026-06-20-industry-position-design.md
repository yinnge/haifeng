# 行业专项招聘模块设计文档

## 概述
实现行业专项招聘的三个子模块（教师招聘/医疗卫生/银行金融）的 C 端查询接口。

## 包结构

### haifeng-common

```
entity/employment/industryPosition/
├── TeacherPosition.java         ← t_teacher_position
├── HealthcarePosition.java      ← t_healthcare_position
└── FinancePosition.java         ← t_finance_position

mapper/employment/industryPosition/
├── TeacherPositionMapper.java
├── HealthcarePositionMapper.java
└── FinancePositionMapper.java
```

### haifeng-app

```
controller/employment/industryPosition/
├── TeacherPositionController.java
├── HealthcarePositionController.java
└── FinancePositionController.java

service/employment/industryPosition/
├── TeacherPositionService.java
├── HealthcarePositionService.java
└── FinancePositionService.java

service/impl/employment/industryPosition/
├── TeacherPositionServiceImpl.java
├── HealthcarePositionServiceImpl.java
└── FinancePositionServiceImpl.java

dto/employment/industryPosition/
├── TeacherPositionSearchDTO.java
├── HealthcarePositionSearchDTO.java
└── FinancePositionSearchDTO.java

vo/employment/industryPosition/
├── TeacherPositionListVO.java
├── TeacherPositionDetailVO.java
├── HealthcarePositionListVO.java
├── HealthcarePositionDetailVO.java
├── FinancePositionListVO.java
└── FinancePositionDetailVO.java
```

## API 设计

### 教师招聘 (TeacherPosition)

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/v1/app/employment/teacher/list` | ❌ | 分页列表 |
| GET | `/api/v1/app/employment/teacher/{id}/detail` | ✅ @RequireLogin | 详情 |

**列表查询字段：**
- 模糊：school_name, position_name, major_requirement
- 精准：school_type, school_nature, subject, education_requirement, province, city, district, position_status

**列表返回字段：** id, school_name, school_type, school_nature, position_name, subject, recruitment_type, province, city, district, work_experience, position_status

**详情返回字段：** id, school_name, school_type, school_nature, supervising_dept, position_name, subject, recruitment_type, province, city, district, education_requirement, degree_requirement, major_requirement, age_limit, recruitment_count, teacher_cert_requirement, teacher_cert_subject, putonghua_level, other_cert_requirement, work_experience, is_normal_major, salary_range, benefits, exam_content, interview_form, reg_start_date, reg_end_date, exam_time, position_status, apply_link, contact_phone, remark, content

### 医疗卫生 (HealthcarePosition)

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/v1/app/employment/healthcare/list` | ❌ | 分页列表 |
| GET | `/api/v1/app/employment/healthcare/{id}/detail` | ✅ @RequireLogin | 详情 |

**列表查询字段：**
- 模糊：institution_name, position_name, major_requirement
- 精准：institution_type, institution_level, institution_nature, department, position_category, province, city, district, education_requirement, position_status

**列表返回字段：** id, institution_name, institution_level, position_name, department, position_category, province, city, district, education_requirement, major_requirement, work_experience, position_status

**详情返回字段：** id, institution_name, institution_type, institution_level, institution_nature, position_name, department, position_category, recruitment_type, province, city, district, education_requirement, degree_requirement, major_requirement, age_limit, recruitment_count, work_experience, license_requirement, title_requirement, internship_requirement, research_requirement, salary_range, benefits, housing_subsidy, reg_start_date, reg_end_date, exam_time, exam_content, apply_link, position_status, contact_phone, contact_person, remark, content

### 银行/金融 (FinancePosition)

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/v1/app/employment/finance/list` | ❌ | 分页列表 |
| GET | `/api/v1/app/employment/finance/{id}/detail` | ✅ @RequireLogin | 详情 |

**列表查询字段：**
- 模糊：institution_name, position_name, major_requirement
- 精准：institution_category, institution_type, branch_name, position_category, recruitment_type, province, city, education_requirement, position_status

**列表返回字段：** id, institution_name, institution_category, position_name, position_category, recruitment_type, province, city, education_requirement, degree_requirement, major_requirement, age_limit, work_experience, salary_min, salary_max, reg_start_date, reg_end_date

**详情返回字段：** id, institution_name, institution_category, institution_type, institution_logo, branch_name, position_name, position_category, recruitment_type, province, city, work_location, is_remote, education_requirement, degree_requirement, major_requirement, major_preference(List<String> JSONB), age_limit, work_experience, recruitment_count, cert_requirements(List<String> JSONB), language_requirement, computer_requirement, other_requirement, salary_min, salary_max, salary_text, benefits, exam_content, exam_time, interview_rounds, reg_start_date, reg_end_date, apply_link, position_status, contact_info, remark, content

## 数据流

```
Controller (DTO @Valid)
  → Service (LambdaQueryWrapper<Entity> 拼装条件)
    → Mapper.selectPage(page, wrapper)
      → page.convert(entity -> ListVO.builder()...build())
    → Mapper.selectById(id)
      → 转 DetailVO / 查不到抛 BusinessException(NOT_FOUND)
```

## 关键约束

1. 所有列表接口无需登录（DTO 无 token 校验）
2. 所有详情接口需 `@RequireLogin`
3. 软删除过滤：`wrapper.eq(Entity::getIsDeleted, false)`
4. 模糊查询（keyword LIKE %v%）与精准查询是 AND 关系
5. 三个子模块互不依赖，各自独立
6. 主键使用雪花算法（`IdType.ASSIGN_ID`）
7. FinancePosition 的 `majorPreference` 和 `certRequirements` 使用 JacksonTypeHandler 映射 JSONB
