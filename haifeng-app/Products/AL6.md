# 录取查询模块接口文档

## 概述

本模块实现用户端专业组录取数据查询，包含专业组分页查询和专业明细分页查询两个只读接口。系统根据用户高考档案自动匹配省份，计算安全系数和选科匹配度，并按会员类型进行数据遮罩控制。

**端口：** 8080（用户端）

**基础路径：** `/api/v1/app/admission`

**认证要求：** `@RequireLogin`（需登录，JWT Bearer Token）

---

## 接口清单

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/group/page` | 分页查询专业组 |
| GET | `/major/page` | 分页查询专业明细 |

---

## 1. 分页查询专业组

### 1.1 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/admission/group/page` |
| 方法 | GET |
| 权限 | 登录用户（`@RequireLogin`） |

### 1.2 请求头

```
Authorization: Bearer {accessToken}
```

### 1.3 请求参数（Query String）

| 参数名 | 类型 | 必填 | 默认值 | 校验规则 | 说明 |
|--------|------|------|--------|----------|------|
| batch | String | 是 | - | @NotBlank, @Size(max=50) | **精准查询**，录取批次（如：本科批、提前批、专科批） |
| subjectFilter | Boolean | 否 | false | - | 是否按用户选科筛选。true=只返回选科匹配的专业组 |
| page | Integer | 否 | 1 | @Min(1) | 页码，从1开始 |
| size | Integer | 否 | 10 | @Min(10), @Max(100) | 每页条数，可选：10, 20, 30, 50, 100 |

**请求示例：**
```
GET /api/v1/app/admission/group/page?batch=本科批&subjectFilter=true&page=1&size=10
Authorization: Bearer {accessToken}
```

### 1.4 查询逻辑说明

- **省份：** 从用户高考档案（`MemberGaokao.gaokaoProvince`）自动获取，无需传参
- **年份：** 自动取 `gaokaoYear - 1`（报志愿时尚未出分，展示上一年数据）；若该年无数据，自动降级为 `gaokaoYear - 2`（单级 fallback）
- **batch：** 精准匹配 `t_admission_group.batch` 字段
- **subjectFilter=true** 时：根据用户选科（subjectType / secondSubjectType / thirdSubjectType）筛选，匹配规则：
  - `不限` 或无选科要求 → 通过
  - `2选1` / `3选1` → 用户选科与专业组要求有交集即通过
  - `必选1` / `必选2` / `必选3` → 用户选科包含专业组全部要求科目

### 1.5 会员权限与遮罩

| 会员类型 | 遮罩规则 |
|----------|----------|
| normal | 每页前10条返回完整数据，第11条起仅返回 `{id, masked:true}` |
| pro | 所有条数均返回完整数据 |
| vip | 所有条数均返回完整数据 |

> 遮罩按当前页内索引计算（index ≥ 10 即遮罩），非跨页累计。

### 1.6 响应示例（成功 - Pro/VIP 用户）

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "masked": false,
        "safetyLevel": 0.72,
        "levelShort": "保",
        "safetyDescription": "录取概率较高，除非出现大小年极端波动，基本能录取。建议至少填3-5个此档位的志愿作为安全保障。",
        "universityName": "北京大学",
        "cityName": "北京",
        "enrollmentCode": "1001",
        "groupCode": "01",
        "groupName": "不限选考科目组",
        "subjects": ["物理", "化学"],
        "requirementType": "必选2",
        "description": "包含计算机类、电子信息类等专业",
        "majorCount": 5,
        "categoryCount": 3,
        "constraints": ["色盲不可报考"],
        "subjectMatch": true,
        "subjectMatchReason": null,
        "historyScores": [
          {
            "year": 2024,
            "minScore": 680,
            "minRank": 1200,
            "avgScore": 685.50,
            "avgRank": 950,
            "maxScore": 690,
            "maxRank": 800,
            "admissionCount": 30
          }
        ]
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1,
    "pages": 5
  },
  "timestamp": 1714300000000
}
```

### 1.7 响应示例（成功 - Normal 用户，第11条起遮罩）

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "masked": false,
        "safetyLevel": 0.72,
        "levelShort": "保",
        "safetyDescription": "录取概率较高...",
        "universityName": "北京大学",
        "cityName": "北京",
        "enrollmentCode": "1001",
        "groupCode": "01",
        "groupName": "不限选考科目组",
        "subjects": ["物理", "化学"],
        "requirementType": "必选2",
        "description": "包含计算机类、电子信息类等专业",
        "majorCount": 5,
        "categoryCount": 3,
        "constraints": ["色盲不可报考"],
        "subjectMatch": true,
        "subjectMatchReason": null,
        "historyScores": [...]
      }
    ],
    "total": 50,
    "size": 20,
    "current": 1,
    "pages": 3
  },
  "timestamp": 1714300000000
}
```

> 当 size=20 且用户为 normal 时，records[0]~records[9] 为完整数据，records[10]~records[19] 仅含 `{id, masked:true}`。

### 1.8 响应字段说明（AdmissionGroupPageVO）

| 字段 | 类型 | 说明 | 备注 |
|------|------|------|------|
| id | Integer | 专业组ID | |
| masked | Boolean | 是否遮罩 | true时其他字段均为null |
| safetyLevel | BigDecimal | 安全系数 0.00~1.00 | 取该组所有专业中最高值 |
| levelShort | String | 安全等级简称 | 搏/冲/稳/保/垫/禁 |
| safetyDescription | String | 安全说明 | |
| universityName | String | 院校名称 | |
| cityName | String | 城市名称 | |
| enrollmentCode | String | 招生代码 | |
| groupCode | String | 专业组代码 | |
| groupName | String | 专业组名称 | |
| subjects | List\<String\> | 选科要求科目 | 如 ["物理","化学"] |
| requirementType | String | 选科要求类型 | 不限/2选1/3选1/必选1/必选2/必选3 |
| description | String | 专业组说明 | |
| majorCount | Integer | 包含专业数量 | |
| categoryCount | Integer | 包含专业类数量 | |
| constraints | List\<String\> | 约束条件 | 如 ["色盲不可报考"] |
| subjectMatch | Boolean | 用户选科是否匹配 | |
| subjectMatchReason | String | 不匹配原因 | 匹配时为null |
| historyScores | List\<YearScoreVO\> | 近5年历史录取数据 | 最多5条 |

### 1.9 响应示例（失败 - 未填写高考档案）

```json
{
  "code": 1010,
  "msg": "用户高考档案不存在，请先填写档案",
  "data": null,
  "timestamp": 1714300000000
}
```

### 1.10 响应示例（失败 - 参数校验错误）

```json
{
  "code": 400,
  "msg": "页码最小为1",
  "data": null,
  "timestamp": 1714300000000
}
```

---

## 2. 分页查询专业明细

### 2.1 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/admission/major/page` |
| 方法 | GET |
| 权限 | 登录用户（`@RequireLogin`） |

### 2.2 请求头

```
Authorization: Bearer {accessToken}
```

### 2.3 请求参数（Query String）

| 参数名 | 类型 | 必填 | 默认值 | 校验规则 | 说明 |
|--------|------|------|--------|----------|------|
| groupId | Integer | 是 | - | @NotNull, @Min(1) | **精准查询**，专业组ID |
| page | Integer | 否 | 1 | @Min(1) | 页码 |
| size | Integer | 否 | 10 | @Min(10), @Max(100) | 每页条数 |

**请求示例：**
```
GET /api/v1/app/admission/major/page?groupId=1&page=1&size=10
Authorization: Bearer {accessToken}
```

### 2.4 查询逻辑说明

- 按 `groupId` 精准查询专业组下的所有专业明细
- 需校验专业组省份与用户高考省份一致，否则返回 1011（防越权）
- 排序：按 `majorCode` 升序
- 无遮罩逻辑，所有会员类型均返回完整数据
- 需要用户高考档案存在（用于安全系数计算），否则返回 1010 错误

### 2.5 响应示例（成功）

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 101,
        "safetyLevel": 0.65,
        "levelShort": "稳",
        "safetyDescription": "录取概率中等偏上，属于"正常发挥"就能录取的范围。建议作为志愿表的核心区域，多填几个此档位的志愿。",
        "majorCode": "080901",
        "majorName": "计算机科学与技术",
        "educationLevel": "本科",
        "duration": "4年",
        "tuition": "5000元/年",
        "description": "培养计算机领域高级人才",
        "constraints": ["色盲不可报考"],
        "historyScores": [
          {
            "year": 2024,
            "minScore": 675,
            "minRank": 1500,
            "avgScore": 680.00,
            "avgRank": 1200,
            "maxScore": 685,
            "maxRank": 1000,
            "admissionCount": 15
          }
        ]
      }
    ],
    "total": 5,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1714300000000
}
```

### 2.6 响应字段说明（AdmissionMajorPageVO）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Integer | 专业明细ID |
| safetyLevel | BigDecimal | 安全系数 0.00~1.00 |
| levelShort | String | 安全等级简称：搏/冲/稳/保/垫/禁 |
| safetyDescription | String | 安全说明 |
| majorCode | String | 专业代码 |
| majorName | String | 专业名称 |
| educationLevel | String | 学历层次（如：本科、专科） |
| duration | String | 学制（如：4年、5年） |
| tuition | String | 学费信息 |
| description | String | 专业说明 |
| constraints | List\<String\> | 约束条件 |
| historyScores | List\<YearScoreVO\> | 近5年历史录取数据 |

### 2.7 响应示例（失败 - 专业组不存在）

```json
{
  "code": 1011,
  "msg": "专业组不存在",
  "data": null,
  "timestamp": 1714300000000
}
```

---

## 3. YearScoreVO 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| year | Short | 年份 |
| minScore | Integer | 最低分 |
| minRank | Integer | 最低位次 |
| avgScore | BigDecimal | 平均分 |
| avgRank | Integer | 平均位次 |
| maxScore | Integer | 最高分 |
| maxRank | Integer | 最高位次 |
| admissionCount | Integer | 录取人数 |

---

## 4. 分页响应结构（MyBatis-Plus Page）

| 字段 | 类型 | 说明 |
|------|------|------|
| records | List | 数据列表 |
| total | Long | 总记录数 |
| size | Long | 每页条数 |
| current | Long | 当前页码 |
| pages | Long | 总页数 |

---

## 5. 枚举值说明

### 5.1 会员类型（member_type）

| 值 | 说明 | 本模块权限 |
|----|------|-----------|
| normal | 普通用户 | 专业组列表前10条完整，之后遮罩 |
| pro | Pro会员 | 全部完整 |
| vip | VIP会员 | 全部完整 |

### 5.2 选科要求类型（requirement_type）

数据库 CHECK 约束：`requirement_type IN ('不限', '2选1', '3选1', '必选1', '必选2', '必选3')`

| 值 | 说明 | 匹配规则 |
|----|------|---------|
| 不限 | 不限选考科目 | 全部通过 |
| 2选1 | 2科中选1科 | 用户选科与要求有交集即通过 |
| 3选1 | 3科中选1科 | 用户选科与要求有交集即通过 |
| 必选1 | 必选1科 | 用户选科必须包含该科目 |
| 必选2 | 必选2科 | 用户选科必须包含全部2科 |
| 必选3 | 必选3科 | 用户选科必须包含全部3科 |

### 5.3 安全等级（levelShort）

数据来源：`t_safety_level_dict`（V13 初始化）

| 简称 | code | 名称 | 安全系数范围 | 颜色 | 说明 |
|------|------|------|-------------|------|------|
| 搏 | REACH_HIGH | 大胆冲刺 | 0.00~0.30 | #FF4D4F | 录取概率极低，属于"彩票"志愿 |
| 冲 | REACH | 可以冲击 | 0.30~0.50 | #FFA940 | 有一定录取可能，但风险较大 |
| 稳 | MATCH | 较为稳妥 | 0.50~0.70 | #FADB14 | 录取概率中等偏上 |
| 保 | SAFE | 比较安全 | 0.70~0.85 | #52C41A | 录取概率较高 |
| 垫 | FLOOR | 高度保底 | 0.85~1.00 | #1890FF | 录取概率极高 |
| 禁 | - | 不可报考 | 0.00（精确） | #999999 | 存在硬性报考限制（约束触发） |

> 「禁」不在字典表中，由代码在约束匹配失败时直接生成（`SafetyCalcResult.blocked()`），safetyLevel 固定为 0。

### 5.4 录取批次（batch）常见值

| 值 | 说明 |
|----|------|
| 本科批 | 本科批次 |
| 提前批 | 提前批次 |
| 专科批 | 专科批次 |

> batch 为自由文本字段（VARCHAR(50)），实际值以数据库数据为准。

---

## 6. 错误码说明

| code | 说明 | 触发场景 |
|------|------|---------|
| 200 | 成功 | 正常请求 |
| 400 | 参数错误 | page<1, size<10或>100, batch为空或超50字, groupId为空或<1 |
| 401 | 未登录或Token过期 | 未携带/无效JWT |
| 1010 | 用户高考档案不存在 | 用户未填写MemberGaokao档案（两个接口均可能触发） |
| 1011 | 专业组不存在 | groupId不存在、已删除、或省份与用户高考省份不匹配（major/page接口触发） |
| 500 | 服务器内部错误 | 系统异常 |

---

## 7. 统一响应格式

```json
{
  "code": 200,
  "msg": "success",
  "data": {},
  "timestamp": 1714300000000
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码，200成功，其他见错误码说明 |
| msg | String | 状态描述 |
| data | Object | 响应数据，可为null |
| timestamp | Long | 服务器时间戳（毫秒） |

---

## 8. 数据库表结构（简表）

### 8.1 t_admission_group（专业组录取表）

来源：`V11__create_admission_group.sql`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | SERIAL (PK) | 主键（自增） |
| university_id | BIGINT | 院校ID |
| university_name | VARCHAR(50) | 院校名称 |
| city_name | VARCHAR(50) | 城市名称 |
| year | SMALLINT | 招生年份 |
| province | VARCHAR(20) | 招生省份 |
| batch | VARCHAR(50) | 录取批次 |
| enrollment_code | VARCHAR(30) | 招生代码 |
| group_code | VARCHAR(30) | 专业组代码 |
| group_name | VARCHAR(100) | 专业组名称 |
| subjects | TEXT[] | 选科科目数组 |
| requirement_type | VARCHAR(10) | 选科要求类型（CHECK约束） |
| description | TEXT | 专业组说明 |
| constraints | TEXT[] | 约束条件数组 |
| major_count | INTEGER | 包含专业数量 |
| category_count | INTEGER | 包含专业类数量 |
| admission_count | INTEGER | 录取人数 |
| min_score | INTEGER | 最低分 |
| min_rank | INTEGER | 最低位次 |
| max_score | INTEGER | 最高分 |
| max_rank | INTEGER | 最高位次 |
| avg_score | NUMERIC(6,2) | 平均分 |
| avg_rank | INTEGER | 平均位次 |
| is_deleted | BOOLEAN | 是否删除 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

**唯一约束：** `uk_admission_group (university_id, year, province, batch, group_code)`

### 8.2 t_admission_major_score（专业录取明细表）

来源：`V11__create_admission_group.sql`

| 字段 | 类型 | 说明 |
|------|------|------|
| id | SERIAL (PK) | 主键（自增） |
| group_id | INTEGER (FK) | 专业组ID（关联 t_admission_group.id，ON DELETE CASCADE） |
| major_id | BIGINT | 专业ID（可为空） |
| major_code | VARCHAR(20) | 专业代码 |
| major_name | VARCHAR(100) | 专业名称 |
| education_level | VARCHAR(20) | 学历层次 |
| duration | VARCHAR(20) | 学制 |
| tuition | VARCHAR(50) | 学费信息 |
| description | TEXT | 专业说明 |
| admission_count | INTEGER | 录取人数 |
| min_score | INTEGER | 最低分 |
| min_rank | INTEGER | 最低位次 |
| max_score | INTEGER | 最高分 |
| max_rank | INTEGER | 最高位次 |
| avg_score | NUMERIC(6,2) | 平均分 |
| avg_rank | INTEGER | 平均位次 |
| constraints | TEXT[] | 约束条件数组 |
| is_deleted | BOOLEAN | 是否删除 |
| created_at | TIMESTAMPTZ | 创建时间 |
| updated_at | TIMESTAMPTZ | 更新时间 |

**唯一约束：** `uk_group_major (group_id, major_code)`

---

## 9. 文件清单

| 类型 | 文件路径 |
|------|---------|
| Controller | `haifeng-app/.../controller/algorithm/admission/AdmissionQueryController.java` |
| DTO | `haifeng-app/.../dto/algorithm/admission/AdmissionGroupQueryDTO.java` |
| DTO | `haifeng-app/.../dto/algorithm/admission/AdmissionMajorQueryDTO.java` |
| 基类DTO | `haifeng-common/.../dto/common/BasePageQueryDTO.java` |
| VO | `haifeng-app/.../vo/algorithm/admission/AdmissionGroupPageVO.java` |
| VO | `haifeng-app/.../vo/algorithm/admission/AdmissionMajorPageVO.java` |
| VO | `haifeng-app/.../vo/algorithm/admission/YearScoreVO.java` |
| Service | `haifeng-app/.../service/impl/algorithm/admission/AdmissionQueryServiceImpl.java` |
| Entity | `haifeng-common/.../entity/algorithm/AdmissionGroup.java` |
| Entity | `haifeng-common/.../entity/algorithm/AdmissionMajorScore.java` |
| Mapper | `haifeng-common/.../mapper/algorithm/AdmissionGroupMapper.java` |
| Mapper | `haifeng-common/.../mapper/algorithm/AdmissionMajorScoreMapper.java` |
| Flyway | `haifeng-admin/.../db/migration/V11__create_admission_group.sql` |
