# 统一岗位搜索 API 文档

## 功能概述

基于 `t_job_index` 表实现全站岗位的搜索/筛选/详情入口，聚合公务员、事业编、军队文职、企业招聘、选调生、教师、医疗卫生、金融银行、基层服务、社区工作者、公益岗等各类岗位。

## 权限说明

| 权限标识 | 说明 |
|----------|------|
| 公开 | 无需登录，任何人可访问 |
| @RequireLogin | 需要携带有效的 Access Token |

**Token 传递方式：**
```
Authorization: Bearer <access_token>
```

## 模块结构

```
模块：employment
子模块：jobIndex
前缀：/api/v1/app/employment/job
```

---

## 一、岗位列表（分页查询）

公开接口，无需登录，支持模糊搜索 + 多维度精确筛选。

### 1.1 接口信息

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/employment/job/list` |
| 权限 | 公开 |
| 分页 | 是（继承 BasePageQueryDTO） |
| 排序 | 按 `publish_date DESC NULLS LAST` |

### 1.2 请求参数

| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | - | 页码，默认 1 |
| size | Integer | 否 | - | 每页条数，默认 10，可选值：10/20/30/50/100 |
| keyword | String | 否 | 模糊 | 同时匹配 `position_name` 和 `organization_name` |
| province | String | 否 | 精确 | 省份 |
| city | String | 否 | 精确 | 城市 |
| educationRequirement | String | 否 | 精确 | 学历要求 |
| recruitmentType | String | 否 | 精确 | 招聘类型（国考/省考/校招/社招/春招/秋招等） |
| salaryMin | Integer | 否 | 范围 | 用户期望最低薪资，条件：`salary_max >= salaryMin` |
| salaryMax | Integer | 否 | 范围 | 用户期望最高薪资，条件：`salary_min <= salaryMax` |
| publishDateStart | String | 否 | 范围 | 发布日期起始，格式：`yyyy-MM-dd` |
| publishDateEnd | String | 否 | 范围 | 发布日期截止，格式：`yyyy-MM-dd` |
| regDeadlineStart | String | 否 | 范围 | 报名截止日期起始，格式：`yyyy-MM-dd` |
| regDeadlineEnd | String | 否 | 范围 | 报名截止日期截止，格式：`yyyy-MM-dd` |
| positionStatus | String | 否 | 精确 | 岗位状态（招聘中/已结束/即将开始） |

### 1.3 查询逻辑说明

1. **模糊查询**：`keyword` 同时匹配 `position_name` 或 `organization_name`（OR 关系）
2. **精确查询**：`province`、`city`、`educationRequirement`、`recruitmentType`、`positionStatus` 各自精确匹配
3. **范围查询**：
   - 薪资：使用范围重叠逻辑，`salary_max >= salaryMin` AND `salary_min <= salaryMax`
   - 日期：`publish_date` 和 `reg_deadline` 各自支持起止范围，仅传一侧时只生效一侧
4. **所有条件之间为 AND 关系**

### 1.4 返回字段（JobIndexListVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 ID |
| categoryLabel | String | 前端展示标签（公务员/事业编/选调生/教师/...） |
| positionName | String | 岗位名称 |
| organizationName | String | 招录单位/企业名称 |
| city | String | 城市 |
| educationRequirement | String | 学历要求 |
| recruitmentType | String | 招聘类型 |
| salaryText | String | 薪资文本展示 |
| positionStatus | String | 岗位状态 |

### 1.5 请求示例

```http
GET /api/v1/app/employment/job/list?page=1&size=10&keyword=技术&province=广东&educationRequirement=本科&positionStatus=招聘中 HTTP/1.1
Host: api.haifeng.com
```

### 1.6 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 100001,
        "categoryLabel": "公务员",
        "positionName": "信息技术岗",
        "organizationName": "广东省某单位",
        "city": "广州",
        "educationRequirement": "本科",
        "recruitmentType": "省考",
        "salaryText": "8k-12k",
        "positionStatus": "招聘中"
      },
      {
        "id": 100002,
        "categoryLabel": "企业招聘",
        "positionName": "Java开发工程师",
        "organizationName": "某科技有限公司",
        "city": "深圳",
        "educationRequirement": "本科",
        "recruitmentType": "校招",
        "salaryText": "15k-25k",
        "positionStatus": "招聘中"
      }
    ],
    "total": 42,
    "page": 1,
    "size": 10
  },
  "timestamp": 1715580000000
}
```

---

## 二、岗位详情

需登录后访问，根据列表返回的 ID 获取岗位完整信息。

### 2.1 接口信息

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/employment/job/{id}/detail` |
| 权限 | @RequireLogin |

### 2.2 路径参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 岗位索引 ID（t_job_index 主键） |

### 2.3 返回字段（JobIndexDetailVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 ID |
| sourceType | String | 来源类型（civil/institution/military/enterprise/...） |
| sourceId | Long | 来源表主键 ID |
| categoryLabel | String | 前端展示标签 |
| positionName | String | 岗位名称 |
| organizationName | String | 招录单位/企业名称 |
| organizationLogo | String | 单位 Logo 地址 |
| province | String | 省份 |
| city | String | 城市 |
| educationRequirement | String | 学历要求 |
| recruitmentCount | Integer | 招录/招聘人数 |
| recruitmentType | String | 招聘类型 |
| salaryMin | Integer | 最低月薪（单位：k） |
| salaryMax | Integer | 最高月薪（单位：k） |
| salaryText | String | 薪资文本展示 |
| positionStatus | String | 岗位状态（招聘中/已结束/即将开始） |
| publishDate | String | 发布日期，ISO-8601 格式 |
| regDeadline | String | 报名截止日期，ISO-8601 格式 |
| isHot | Boolean | 是否热门 |
| viewCount | Integer | 浏览量 |
| applyCount | Integer | 报名/申请人数 |

### 2.4 请求示例

```http
GET /api/v1/app/employment/job/100001/detail HTTP/1.1
Host: api.haifeng.com
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 2.5 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 100001,
    "sourceType": "civil",
    "sourceId": 5001,
    "categoryLabel": "公务员",
    "positionName": "信息技术岗",
    "organizationName": "广东省某单位",
    "organizationLogo": "https://example.com/logo/123.png",
    "province": "广东",
    "city": "广州",
    "educationRequirement": "本科",
    "recruitmentCount": 3,
    "recruitmentType": "省考",
    "salaryMin": 8,
    "salaryMax": 12,
    "salaryText": "8k-12k",
    "positionStatus": "招聘中",
    "publishDate": "2026-06-15T00:00:00+08:00",
    "regDeadline": "2026-07-15T23:59:59+08:00",
    "isHot": true,
    "viewCount": 1523,
    "applyCount": 89
  },
  "timestamp": 1715580000000
}
```

### 2.6 错误码

| code | msg |
|------|-----|
| 404 | 岗位不存在 |
| 401 | 未登录或Token已过期 |

---

## 三、数据库表结构（参考）

### t_job_index 统一岗位索引表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGSERIAL | 主键（雪花算法） |
| source_type | VARCHAR(30) | 来源类型：civil/institution/military/enterprise/selection/teacher/healthcare/finance/grassroots/community/public_welfare |
| source_id | INTEGER | 来源表主键 ID |
| category_label | VARCHAR(50) | 前端展示标签 |
| position_name | VARCHAR(200) | 岗位名称 |
| organization_name | VARCHAR(200) | 招录单位名称 |
| organization_logo | VARCHAR(500) | 单位 Logo |
| province | VARCHAR(50) | 省份 |
| city | VARCHAR(50) | 城市 |
| education_requirement | VARCHAR(50) | 学历要求 |
| recruitment_count | INTEGER | 招录人数 |
| recruitment_type | VARCHAR(30) | 招聘类型 |
| salary_min | INTEGER | 最低月薪（k） |
| salary_max | INTEGER | 最高月薪（k） |
| salary_text | VARCHAR(50) | 薪资文本 |
| publish_date | TIMESTAMPTZ | 发布日期 |
| reg_deadline | TIMESTAMPTZ | 报名截止日期 |
| is_hot | BOOLEAN | 是否热门 |
| view_count | INTEGER | 浏览量 |
| apply_count | INTEGER | 报名人数 |
| position_status | VARCHAR(20) | 岗位状态（招聘中/已结束/即将开始） |
| is_deleted | BOOLEAN | 软删除标志 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

---

## 四、source_type 枚举值

| 值 | 说明 |
|-----|------|
| civil | 公务员 |
| institution | 事业编 |
| military | 军队文职 |
| enterprise | 企业招聘 |
| selection | 选调生 |
| teacher | 教师招聘 |
| healthcare | 医疗卫生 |
| finance | 金融银行 |
| grassroots | 基层服务 |
| community | 社区工作者 |
| public_welfare | 公益岗 |

---

## 五、通用错误码

| code | msg | 说明 |
|------|-----|------|
| 200 | success | 成功 |
| 400 | * | 参数错误/业务校验失败 |
| 401 | 未登录或Token已过期 | 需重新登录 |
| 404 | 岗位不存在 | 资源不存在 |
| 500 | 服务器内部错误 | 系统异常 |
