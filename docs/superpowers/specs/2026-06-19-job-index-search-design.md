# 统一岗位搜索（Job Index Search）设计文档

## 概述
为海峰未来规划院 App 端实现岗位统一搜索功能，基于 `t_job_index` 表提供全站岗位的搜索/筛选/详情入口。

## 分层结构

```
haifeng-common/
├── entity/employment/jobIndex/JobIndex.java
└── mapper/employment/jobIndex/JobIndexMapper.java

haifeng-app/
├── dto/employment/jobIndex/JobSearchDTO.java
├── vo/employment/jobIndex/
│   ├── JobIndexListVO.java
│   └── JobIndexDetailVO.java
├── service/employment/jobIndex/
│   └── JobIndexService.java
├── service/impl/employment/jobIndex/
│   └── JobIndexServiceImpl.java
└── controller/employment/jobIndex/
    └── JobIndexController.java
```

遵循规范：
- 路径前缀：`/api/v1/app/`
- 模块：`employment`
- 子模块：`jobIndex`

## 接口设计

### 接口 1：分页查询（公开，无需登录）

```
GET /api/v1/app/employment/job/list
```

#### 请求参数（JobSearchDTO extends BasePageQueryDTO）

| 参数 | 类型 | 查询方式 | 说明 |
|------|------|----------|------|
| page | Integer | - | 页码，默认 1 |
| size | Integer | - | 每页条数，默认 10 |
| keyword | String | 模糊 | 同时匹配 position_name 和 organization_name |
| province | String | 精确 | 省份 |
| city | String | 精确 | 城市 |
| educationRequirement | String | 精确 | 学历要求 |
| recruitmentType | String | 精确 | 招聘类型 |
| salaryMin | Integer | 范围 | 用户期望最低薪资，条件：job.salary_max >= salaryMin |
| salaryMax | Integer | 范围 | 用户期望最高薪资，条件：job.salary_min <= salaryMax |
| publishDateStart | LocalDate | 范围 | 发布日期起始 |
| publishDateEnd | LocalDate | 范围 | 发布日期截止 |
| regDeadlineStart | LocalDate | 范围 | 报名截止日期起始 |
| regDeadlineEnd | LocalDate | 范围 | 报名截止日期截止 |
| positionStatus | String | 精确 | 岗位状态 |

#### 返回字段（JobIndexListVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| categoryLabel | String | 前端展示标签 |
| positionName | String | 岗位名称 |
| organizationName | String | 招录单位名称 |
| city | String | 城市 |
| educationRequirement | String | 学历要求 |
| recruitmentType | String | 招聘类型 |
| salaryText | String | 薪资文本展示 |
| positionStatus | String | 岗位状态 |

#### 查询逻辑

```sql
SELECT id, category_label, position_name, organization_name, city,
       education_requirement, recruitment_type, salary_text, position_status
FROM t_job_index
WHERE is_deleted = FALSE
  -- 模糊：keyword 匹配 position_name 或 organization_name
  AND (position_name LIKE '%keyword%' OR organization_name LIKE '%keyword%')
  -- 精确匹配（仅当参数非空时生效）
  AND province = #{province}
  AND city = #{city}
  AND education_requirement = #{educationRequirement}
  AND recruitment_type = #{recruitmentType}
  AND position_status = #{positionStatus}
  -- 薪资范围重叠
  AND salary_max >= #{salaryMin}
  AND salary_min <= #{salaryMax}
  -- 日期范围（仅当参数非空时生效）
  AND publish_date >= #{publishDateStart}
  AND publish_date <= #{publishDateEnd}
  AND reg_deadline >= #{regDeadlineStart}
  AND reg_deadline <= #{regDeadlineEnd}
ORDER BY publish_date DESC NULLS LAST
```

### 接口 2：岗位详情（需登录 @RequireLogin）

```
GET /api/v1/app/employment/job/{id}/detail
```

#### 返回字段（JobIndexDetailVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| sourceType | String | 来源类型 |
| sourceId | Long | 来源表主键 ID |
| categoryLabel | String | 前端展示标签 |
| positionName | String | 岗位名称 |
| organizationName | String | 招录单位名称 |
| organizationLogo | String | 单位 Logo 地址 |
| province | String | 省份 |
| city | String | 城市 |
| educationRequirement | String | 学历要求 |
| recruitmentCount | Integer | 招录人数 |
| recruitmentType | String | 招聘类型 |
| salaryMin | Integer | 最低月薪（k） |
| salaryMax | Integer | 最高月薪（k） |
| salaryText | String | 薪资文本展示 |
| positionStatus | String | 岗位状态 |
| publishDate | OffsetDateTime | 发布日期 |
| regDeadline | OffsetDateTime | 报名截止日期 |
| isHot | Boolean | 是否热门 |
| viewCount | Integer | 浏览量 |
| applyCount | Integer | 报名人数 |

## Entity 设计

### JobIndex（haifeng-common）

```java
@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "t_job_index", autoResultMap = true)
public class JobIndex implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)  // SERIAL 自增
    private Long id;

    private String sourceType;
    private Long sourceId;
    private String categoryLabel;
    private String positionName;
    private String organizationName;
    private String organizationLogo;
    private String province;
    private String city;
    private String educationRequirement;
    private Integer recruitmentCount;
    private String recruitmentType;
    private Integer salaryMin;
    private Integer salaryMax;
    private String salaryText;
    private OffsetDateTime publishDate;
    private OffsetDateTime regDeadline;
    private Boolean isHot;
    private Integer viewCount;
    private Integer applyCount;
    private String positionStatus;
    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

## Mapper 设计

### JobIndexMapper（haifeng-common）

```java
@Mapper
public interface JobIndexMapper extends BaseMapper<JobIndex> {
    // 使用 MyBatis-Plus 内置方法，无需额外定义
}
```

## 关键逻辑说明

1. **薪资范围查询**：使用范围重叠逻辑（A 与 B 有交集），确保不会因微小差异漏掉合适岗位
2. **模糊查询**：keyword 同时匹配 position_name 和 organization_name，用 OR 连接后用 AND 与其他条件组合
3. **日期范围**：publish_date 和 reg_deadline 支持起止范围查询，只传 start 或只传 end 时仅生效一侧
4. **排序**：按发布日期倒序排列（NULLS LAST）
5. **分页**：使用 MyBatis-Plus Page 对象，集成 BasePageQueryDTO
6. **实体主键**：t_job_index 使用 SERIAL 自增，所以用 IdType.AUTO
