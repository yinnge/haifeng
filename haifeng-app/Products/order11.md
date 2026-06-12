# C 端竞赛证书管理 API 文档（证书列表·详情 / 竞赛列表·详情·关联专业 / 专业↔竞赛双向关联）

## 功能概述

本模块实现 C 端「竞赛证书管理」父模块下的 2 类共 7 个只读接口：

- **职业技能证书**：证书分类列表 + 证书分页列表（支持分类精准筛选 + 证书名称模糊查询）+ 证书详情
- **科研竞赛**：竞赛分页列表 + 竞赛详情（包含 JSONB 详情）+ 竞赛→专业列表；并扩展专业模``块新增「专业→竞赛列表」反向关联

访问权限按子功能分级：列表完全公开；证书详情、竞赛详情需要登录；竞赛↔专业双向关联需要 Pro 及以上会员。所有接口不加 Redis 缓存（实时读库）。

| 子模块 | 功能 | 权限要求 |
|--------|------|----------|
| 证书分类 | 证书分类去重列表（category 下拉来源） | 公开访问 |
| 证书列表 | 证书分页列表（category 精准筛选 + certName 模糊查询） | 公开访问 |
| 证书详情 | 证书完整信息（含考试要求/安排/官网） | 登录用户 |
| 竞赛列表 | 竞赛分页列表 | 公开访问 |
| 竞赛详情 | 竞赛完整信息（JSONB 详情：基本/奖项/规程/流程/奖项展示等） | 登录用户 |
| 竞赛 → 专业 | 按竞赛 id 分页查询关联专业列表 | Pro 及以上 |
| 专业 → 竞赛 | 按专业 id 分页查询关联竞赛列表 | Pro 及以上 |

### 与已有接口的协同

| 已有接口（order9） | 拿到 id 后调用本模块 |
|---|---|---|
| `GET /api/v1/app/major/list`（公开）| `GET /api/v1/app/major/{majorId}/competitions`（Pro，本模块接口 6）|
| 本模块接口 1（证书列表）| `GET /api/v1/app/certificate/{certId}/detail`（登录，本模块接口 2）|
| 本模块接口 3（竞赛列表）| `GET /api/v1/app/competition/{compId}/detail`（登录，本模块接口 4）|
| 本模块接口 3（竞赛列表）| `GET /api/v1/app/competition/{compId}/majors`（Pro，本模块接口 5）|
| 本模块接口 7（证书分类）| 前端下拉筛选器数据源，配合接口 1 的 `category` 参数 |

---

## 通用说明

### 权限说明

| 权限标识 | 说明 |
|----------|------|
| 公开 | 无需登录，无需 Token |
| 登录用户 | 需携带有效 Access Token；由 `@RequireLogin` 切面校验 |
| Pro 及以上 | 需 `member_type ∈ {pro, vip}`；由 `@RequirePro` 切面校验（已隐含登录） |

### 统一响应格式

```json
{
  "code": 200,
  "msg": "success",
  "data": { ... },
  "timestamp": 1717392000000
}
```

### 错误码

| code | 含义 | 触发场景 |
|------|------|----------|
| 200 | 成功 | 正常返回 |
| 400 | 参数错误 | 字段级校验失败（`page < 1` / `size < 10` / `size > 1000`） |
| 401 | 未登录或 Token 过期 | 未带 Token / Token 失效 |
| 403 | 无权限 | 普通用户访问 Pro 接口 |
| 404 | 资源不存在 | 证书/竞赛/专业不存在或已删除 |
| 500 | 服务器内部错误 | 未预期异常 |

### 分页参数（BasePageQueryDTO）

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码，最小 1 |
| size | Integer | 否 | 10 | 每页条数，10–1000 |

### 数据可见性规则

- `t_certificate` 按 `is_deleted = FALSE` 过滤
- `t_competition` 按 `is_deleted = FALSE` 过滤
- `t_competition_detail` 通过新增的 `findActiveByCompetitionId` 方法过滤 `is_deleted = FALSE`（该方法替代了不带软删除过滤的 `findByCompetitionId`）
- `t_competition_major` 表**无** `is_deleted` 字段，直接查询
- `t_major` 按 `status = 1` 过滤存在性校验（专业→竞赛接口）
- 关联查询（接口 5 / 6）主表记录不存在 → 404 业务错误码
- 关联表无数据 → 返回空分页（不抛 404），与现有 `/postgrad-directions` 风格一致
- 列表无匹配 → 返回空分页

### 字段映射约定

- 数据库列 `snake_case` ↔ JSON 字段 `camelCase`（如 `comp_name` → `compName`，`cert_name` → `certName`）
- `TEXT[]` 字段（如 `exam_requirements` / `awards` / `purposes` 等）通过 `JacksonTypeHandler` 双向转换为 `List<String>`
- **JSONB 字段**（竞赛详情的 `basic_info` / `competition_rules` / `process_guide` / `awards_display`）由实体上的 `JacksonTypeHandler` 反序列化为 `Map<String,Object>` / `List<Map<String,String>>`，**直接由前端按字段约定渲染**
- 雪花算法生成的 ID 在 JSON 中以数字返回（注意前端 long 精度）

### JSONB 字段约定

| 字段 | 路径 | JSON 类型 | 说明 |
|------|------|-----------|------|
| `basic_info` | `CompetitionDetailVO.basicInfo` | `Map<String, Object>` | 主办方、年份、官网等键值对 |
| `awards` | `CompetitionDetailVO.awards` | `List<String>` | 奖项名称列表（如「一等奖」「二等奖」）|
| `purposes` | `CompetitionDetailVO.purposes` | `List<String>` | 赛事宗旨列表 |
| `competition_rules` | `CompetitionDetailVO.competitionRules` | `List<Map<String, String>>` | 规则项（`title` + `content`）|
| `scoring_criteria` | `CompetitionDetailVO.scoringCriteria` | `List<String>` | 评分标准条目 |
| `notices` | `CompetitionDetailVO.notices` | `List<String>` | 注意事项条目 |
| `process_guide` | `CompetitionDetailVO.processGuide` | `List<Map<String, String>>` | 流程步骤（`step` + `desc`）|
| `awards_display` | `CompetitionDetailVO.awardsDisplay` | `List<Map<String, String>>` | 奖项展示（`level` + `count`）|
| `background` | `CompetitionDetailVO.background` | `String` | 赛事背景介绍 |

> **注意**：`CompetitionDetailVO.id` 与 `CompetitionDetailVO.competitionId` 在当前实现中**值相同**（都等于竞赛主表 `id`），是 spec 明确要求的字段冗余，前端可任选其一作为跳转主键。

---

## 1. 证书列表

**功能描述**：分页查询证书，支持 `category` 精准筛选 + `certName` 模糊查询。无需登录。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/certificate/list` |
| 权限 | 公开 |
| Content-Type | application/x-www-form-urlencoded（query 参数） |

### 请求参数

| 参数 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|----------|------|
| page | Integer | 否 | — | 页码，默认 1 |
| size | Integer | 否 | — | 每页条数，默认 10 |
| **category** | String | 否 | **精准（=）** | 证书分类（如「计算机」「金融」），空值不参与筛选 |
| **certName** | String | 否 | **模糊（LIKE '%xxx%'）** | 证书名称关键字，空值不参与筛选；匹配 `t_certificate.cert_name` 字段 |

> 多个筛选条件同时存在时按 AND 组合。

### 排序规则

`id ASC`

### 请求示例

```http
GET /api/v1/app/certificate/list?page=1&size=10&category=计算机&certName=软件
```

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1001,
        "certName": "软件设计师",
        "category": "计算机",
        "certLevel": "中级",
        "applicableMajor": "计算机科学与技术、软件工程",
        "registrationTime": "上半年 3 月",
        "examTime": "上半年 5 月",
        "examFee": 100,
        "certIntro": "软件行业专业技术资格证书"
      },
      {
        "id": 1002,
        "certName": "系统集成项目管理工程师",
        "category": "计算机",
        "certLevel": "中级",
        "applicableMajor": "信息管理与信息系统",
        "registrationTime": "下半年 8 月",
        "examTime": "下半年 11 月",
        "examFee": 120,
        "certIntro": "面向 IT 行业项目管理岗位的认证"
      }
    ],
    "total": 2,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1717392000000
}
```

### 响应字段（VO）

**CertificateListVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 证书 ID（用于跳转证书详情） |
| certName | String | 证书名称 |
| category | String | 证书分类 |
| certLevel | String | 证书级别（初级/中级/高级 等）|
| applicableMajor | String | 适用专业 |
| registrationTime | String | 报名时间（自由文本，如「上半年 3 月」）|
| examTime | String | 考试时间（自由文本）|
| examFee | Integer | 报名费用（单位：元）|
| certIntro | String | 证书简介 |

> 详细字段（考试要求、考试安排、官网链接）见接口 2「证书详情」。

### 行为说明

1. `is_deleted = FALSE` 过滤
2. 条件筛选：
   - `category` 有值则 `eq(category, dto.category)`，无值不参与
   - `certName` 有值则 `like('%' + certName + '%')` 匹配 `cert_name` 字段，无值不参与
3. 排序 `id ASC`
4. 无匹配时返回空分页

### 错误响应

| 场景 | code | msg |
|------|------|-----|
| `page < 1` 或 `size` 越界 | 400 | 字段级校验信息 |
| `category` / `certName` 无匹配 | 200 | 返回空分页 |

---

## 2. 证书详情

**功能描述**：根据证书 ID 返回完整证书信息，含考试要求（`TEXT[]`）、考试安排、官网链接。需登录。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/certificate/{certId}/detail` |
| 权限 | 登录用户（`@RequireLogin`） |
| Content-Type | application/x-www-form-urlencoded（query 参数） |

### 请求参数

| 参数 | 位置 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|------|----------|------|
| certId | Path | Long | 是 | **精准（=）** | 证书 ID |

### 请求示例

```http
GET /api/v1/app/certificate/1001/detail
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
```

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1001,
    "certName": "软件设计师",
    "category": "计算机",
    "certLevel": "中级",
    "applicableMajor": "计算机科学与技术、软件工程",
    "registrationTime": "上半年 3 月",
    "examTime": "上半年 5 月",
    "examFee": 100,
    "certIntro": "软件行业专业技术资格证书",
    "examRequirements": ["本科及以上学历", "相关工作经验 1 年以上"],
    "examArrangement": "全国统一考试，上午综合知识、下午案例分析",
    "officialWebsite": "https://www.ruankao.org.cn"
  },
  "timestamp": 1717392000000
}
```

### 响应字段（VO）

**CertificateDetailVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 证书 ID |
| certName | String | 证书名称 |
| category | String | 证书分类 |
| certLevel | String | 证书级别 |
| applicableMajor | String | 适用专业 |
| registrationTime | String | 报名时间 |
| examTime | String | 考试时间 |
| examFee | Integer | 报名费用 |
| certIntro | String | 证书简介 |
| examRequirements | List\<String\> | 考试要求条目（`TEXT[]` 字段，`JacksonTypeHandler` 转换）|
| examArrangement | String | 考试安排详情 |
| officialWebsite | String | 官方网址 |

### 行为说明

1. 先查 `t_certificate`：`id = certId AND is_deleted = FALSE`
2. 不存在或已删除 → 404 "证书不存在"
3. `exam_requirements` 字段为 `TEXT[]` 类型，由 `JacksonTypeHandler` 反序列化为 `List<String>` 返回

### 错误响应

| 场景 | code | msg |
|------|------|-----|
| 未登录 / Token 失效 | 401 | 未登录或 Token 过期 |
| 证书不存在或已删除 | 404 | 证书不存在 |

---

## 3. 竞赛列表

**功能描述**：分页查询竞赛（仅返回未软删除记录）。无需登录。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/competition/list` |
| 权限 | 公开 |
| Content-Type | application/x-www-form-urlencoded（query 参数） |

### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| size | Integer | 否 | 每页条数，默认 10 |

> 本接口无任何业务筛选字段（无模糊/精准查询），仅按 `BasePageQueryDTO` 默认分页。

### 排序规则

`id ASC`

### 请求示例

```http
GET /api/v1/app/competition/list?page=1&size=10
```

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 2001,
        "compName": "蓝桥杯全国软件和信息技术专业人才大赛",
        "compLevel": "国家级",
        "registrationTime": "上半年 3-4 月"
      },
      {
        "id": 2002,
        "compName": "中国互联网+ 大学生创新创业大赛",
        "compLevel": "国家级",
        "registrationTime": "下半年 5-7 月"
      }
    ],
    "total": 2,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1717392000000
}
```

### 响应字段（VO）

**CompetitionListVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 竞赛 ID（用于跳转详情/关联专业） |
| compName | String | 竞赛名称 |
| compLevel | String | 竞赛级别（国家级/省级/校级）|
| registrationTime | String | 报名时间（自由文本）|

### 行为说明

1. `is_deleted = FALSE` 过滤
2. 排序 `id ASC`
3. 无匹配时返回空分页

---

## 4. 竞赛详情

**功能描述**：根据竞赛 ID 返回竞赛完整信息，含详情表（`t_competition_detail`）的 JSONB 字段。需登录。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/competition/{compId}/detail` |
| 权限 | 登录用户（`@RequireLogin`） |
| Content-Type | application/x-www-form-urlencoded（query 参数） |

### 请求参数

| 参数 | 位置 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|------|----------|------|
| compId | Path | Long | 是 | **精准（=）** | 竞赛 ID |

### 请求示例

```http
GET /api/v1/app/competition/2001/detail
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
```

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 2001,
    "competitionId": 2001,
    "basicInfo": {
      "organizer": "工业和信息化部人才交流中心",
      "year": 2024,
      "officialWebsite": "https://dasai.lanqiao.cn",
      "registrationDeadline": "2024-04-30"
    },
    "awards": ["一等奖", "二等奖", "三等奖", "优秀奖"],
    "background": "蓝桥杯大赛旨在促进软件和信息技术领域专业技术人才的培养，提升高校毕业生的就业竞争力……",
    "purposes": ["促进高校软件类课程教学改革", "培养具有实践能力和创新精神的软件人才"],
    "competitionRules": [
      { "title": "组队要求", "content": "每队 1-3 人，可跨校组队" },
      { "title": "作品提交", "content": "提交完整源代码 + 设计文档" }
    ],
    "scoringCriteria": ["代码规范性 30%", "功能完整性 40%", "答辩表现 30%"],
    "notices": ["需提前在官网注册账号", "参赛作品需为原创"],
    "processGuide": [
      { "step": "1", "desc": "登录官网完成账号注册与实名认证" },
      { "step": "2", "desc": "选择赛项并完成在线报名" },
      { "step": "3", "desc": "参加省赛（线上/线下）" },
      { "step": "4", "desc": "省赛晋级后参加全国总决赛" }
    ],
    "awardsDisplay": [
      { "level": "国一", "count": "10" },
      { "level": "国二", "count": "20" },
      { "level": "国三", "count": "50" }
    ]
  },
  "timestamp": 1717392000000
}
```

### 响应字段（VO）

**CompetitionDetailVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 竞赛 ID（与 `competitionId` 相同） |
| competitionId | Long | 竞赛 ID（同 `id`，冗余字段） |
| basicInfo | Map\<String, Object\> | 主办方/年份/官网等键值对（JSONB 透传）|
| awards | List\<String\> | 奖项列表（TEXT[] 转换）|
| background | String | 赛事背景介绍 |
| purposes | List\<String\> | 赛事宗旨列表 |
| competitionRules | List\<Map\<String, String\>\> | 规则项列表（`title` + `content`，JSONB 透传）|
| scoringCriteria | List\<String\> | 评分标准条目 |
| notices | List\<String\> | 注意事项条目 |
| processGuide | List\<Map\<String, String\>\> | 流程步骤列表（`step` + `desc`，JSONB 透传）|
| awardsDisplay | List\<Map\<String, String\>\> | 奖项展示列表（`level` + `count`，JSONB 透传）|

### 行为说明

1. 先查 `t_competition`：`id = compId AND is_deleted = FALSE`
2. 不存在或已删除 → 404 "竞赛不存在"
3. 通过 `CompetitionDetailMapper.findActiveByCompetitionId(compId)` 查 `t_competition_detail`（**已带 `is_deleted = FALSE` 过滤**）
4. 若 `t_competition_detail` 中无对应记录（赛事只有基本信息、无详情）→ 仍返回 200，VO 中除 `id` / `competitionId` 外其余字段为 `null`（**不抛 404**，与 Need 文档"前端动态展示数据表里的数据"精神一致）
5. JSONB 字段由实体上的 `JacksonTypeHandler` 自动反序列化为对应 Java 类型

### 错误响应

| 场景 | code | msg |
|------|------|-----|
| 未登录 / Token 失效 | 401 | 未登录或 Token 过期 |
| 竞赛不存在或已删除 | 404 | 竞赛不存在 |
| 竞赛存在但详情表无记录 | 200 | 返回 VO 仅含 `id` / `competitionId`，其余字段为 `null` |

---

## 5. 竞赛 → 专业

**功能描述**：按竞赛 ID 分页查询与其关联的专业列表（id + 名称）。需 Pro 及以上。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/competition/{compId}/majors` |
| 权限 | Pro 及以上（`@RequirePro`） |
| Content-Type | application/x-www-form-urlencoded（query 参数） |

### 请求参数

| 参数 | 位置 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|------|----------|------|
| compId | Path | Long | 是 | **精准（=）** | 竞赛 ID |
| page | Query | Integer | 否 | — | 页码，默认 1 |
| size | Query | Integer | 否 | — | 每页条数，默认 10 |

### 排序规则

`cm.id ASC`（关联表主键升序，依赖 `idx_cm_competition` 索引）

### 请求示例

```http
GET /api/v1/app/competition/2001/majors?page=1&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
```

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      { "majorId": 1001, "majorName": "计算机科学与技术" },
      { "majorId": 1002, "majorName": "软件工程" },
      { "majorId": 1003, "majorName": "数据科学与大数据技术" }
    ],
    "total": 3,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1717392000000
}
```

### 响应字段（VO）

**CompetitionMajorBriefVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| majorId | Long | 专业 ID（前端据此跳转本科专业详情） |
| majorName | String | 专业名称 |

### 行为说明

1. 先查 `t_competition`：`id = compId AND is_deleted = FALSE`，不存在 → 404 "竞赛不存在"
2. 联表 `t_competition_major`（按 `competition_id` 过滤，走 `idx_cm_competition` 索引）
3. 直接使用中间表冗余字段 `major_id` / `major_name`，不联查 `t_major`（避免主表删除导致跳转信息查询失败）
4. **无任何关联时**返回空分页（`records: []`），不抛 404

### 错误响应

| 场景 | code | msg |
|------|------|-----|
| 未登录 | 401 | 未登录或 Token 过期 |
| 普通用户（非 Pro/Vip） | 403 | 权限不足（需要专业版及以上） |
| `compId` 在 t_competition 不存在 | 404 | 竞赛不存在 |
| `compId` 存在但无任何关联专业 | 200 | 返回空分页 |

---

## 6. 专业 → 竞赛

**功能描述**：按本科专业 ID 分页查询与其关联的竞赛列表（id + 名称）。需 Pro 及以上。本接口扩展自 `MajorController`（与 `/{majorId}/postgrad-directions` 同处）。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/major/{majorId}/competitions` |
| 权限 | Pro 及以上（`@RequirePro`） |
| Content-Type | application/x-www-form-urlencoded（query 参数） |

### 请求参数

| 参数 | 位置 | 类型 | 必填 | 查询方式 | 说明 |
|------|------|------|------|----------|------|
| majorId | Path | Long | 是 | **精准（=）** | 本科专业 ID |
| page | Query | Integer | 否 | — | 页码，默认 1 |
| size | Query | Integer | 否 | — | 每页条数，默认 10 |

### 排序规则

`cm.id ASC`（关联表主键升序，依赖 `idx_cm_major` 索引）

### 请求示例

```http
GET /api/v1/app/major/1001/competitions?page=1&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....
```

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      { "competitionId": 2001, "competitionName": "蓝桥杯全国软件和信息技术专业人才大赛" },
      { "competitionId": 2002, "competitionName": "中国互联网+ 大学生创新创业大赛" },
      { "competitionId": 2003, "competitionName": "ACM 国际大学生程序设计竞赛" }
    ],
    "total": 3,
    "size": 10,
    "current": 1,
    "pages": 1
  },
  "timestamp": 1717392000000
}
```

### 响应字段（VO）

**CompetitionBriefVO**

| 字段 | 类型 | 说明 |
|------|------|------|
| competitionId | Long | 竞赛 ID（前端据此跳转竞赛详情） |
| competitionName | String | 竞赛名称 |

### 行为说明

1. 先查 `t_major`：`id = majorId AND status = 1`，不存在或已下架 → 404 "专业不存在"
2. 联表 `t_competition_major`（按 `major_id` 过滤，走 `idx_cm_major` 索引）
3. 直接使用中间表冗余字段 `competition_id` / `competition_name`，不联查 `t_competition`（避免主表删除导致跳转信息查询失败）
4. **无任何关联时**返回空分页（`records: []`），不抛 404

### 错误响应

| 场景 | code | msg |
|------|------|-----|
| 未登录 | 401 | 未登录或 Token 过期 |
| 普通用户（非 Pro/Vip） | 403 | 权限不足（需要专业版及以上） |
| `majorId` 在 t_major 不存在或已下架 | 404 | 专业不存在 |
| `majorId` 存在但无任何关联竞赛 | 200 | 返回空分页 |

---

## 7. 证书分类列表

**功能描述**：返回 `t_certificate` 表所有不重复的 `category` 值（已过滤 `is_deleted = FALSE` 且 `category` 非空），按字典序升序排列。前端用于下拉筛选器数据源。无需登录。

### 接口信息

| 项 | 值 |
|----|----|
| URL | `GET /api/v1/app/certificate/categories` |
| 权限 | 公开 |
| Content-Type | application/x-www-form-urlencoded（query 参数） |

### 请求参数

无。

### 请求示例

```http
GET /api/v1/app/certificate/categories
```

### 响应示例

```json
{
  "code": 200,
  "msg": "success",
  "data": ["IT类", "工程类", "语言类", "财会类"],
  "timestamp": 1717392000000
}
```

### 响应字段

| 字段 | 类型 | 说明 |
|------|------|------|
| data | List\<String\> | 去重后的证书分类列表，按字典序升序 |

### 行为说明

1. `is_deleted = FALSE` 过滤 + `category IS NOT NULL` 过滤
2. `SELECT DISTINCT category ... ORDER BY category`
3. 走已有索引 `idx_cert_category`
4. 无任何分类数据时返回空数组 `[]`

### 错误响应

| 场景 | code | msg |
|------|------|-----|
| 正常无数据 | 200 | 返回空数组 `[]` |

---

## 关联表与索引说明

| 表 | 关键字段 | 索引 | 使用接口 |
|---|---|---|---|
| `t_certificate` | `category` | `idx_cert_category` | 接口 1、接口 7 |
| `t_certificate` | `cert_name` | `idx_cert_name_search`（btree pattern_ops）| 接口 1 |
| `t_competition` | `comp_level` | `idx_comp_level` | 接口 3 |
| `t_competition_detail` | `competition_id` | `uk_competition_detail_competition_id` UNIQUE | 接口 4 |
| `t_competition_detail` | `awards` | `idx_comp_detail_awards`（GIN）| 接口 4 |
| `t_competition_detail` | `basic_info` | `idx_comp_detail_basic_info`（GIN）| 接口 4 |
| `t_competition_major` | `competition_id` | `idx_cm_competition` | 接口 5 |
| `t_competition_major` | `major_id` | `idx_cm_major` | 接口 6 |
| `t_competition_major` | `(competition_id, major_id)` | `uk_comp_major` UNIQUE | — |

> 所有索引均已存在于 V9 迁移文件中，无需新建。

---

## 筛选条件总览

| 接口 | 路径参数 | 模糊查询字段 | 精准查询字段 |
|------|----------|---------------|---------------|
| 1. 证书列表 | — | `certName` | `category` |
| 2. 证书详情 | `certId`（path）| — | — |
| 3. 竞赛列表 | — | — | `page`、`size` |
| 4. 竞赛详情 | `compId`（path）| — | — |
| 5. 竞赛 → 专业 | `compId`（path）| — | `page`、`size` |
| 6. 专业 → 竞赛 | `majorId`（path）| — | `page`、`size` |
| 7. 证书分类 | — | — | — |

> 接口 7 无任何筛选参数，直接返回全量去重分类列表。其余接口仅按 `BasePageQueryDTO` 默认分页。

---

## 接口路径速查

```
GET  /api/v1/app/certificate/categories                        [公开]   证书分类列表（去重，下拉筛选数据源）
GET  /api/v1/app/certificate/list                              [公开]   证书列表（category 精准 + certName 模糊）
GET  /api/v1/app/certificate/{certId}/detail                   [登录]   证书详情
GET  /api/v1/app/competition/list                              [公开]   竞赛列表
GET  /api/v1/app/competition/{compId}/detail                   [登录]   竞赛详情（含 JSONB 字段）
GET  /api/v1/app/competition/{compId}/majors                   [Pro]    竞赛 → 专业列表
GET  /api/v1/app/major/{majorId}/competitions                  [Pro]    专业 → 竞赛列表
```

---

## 与 order9（专业管理）模块的对照

| 已有（order9 MajorController）| 新增（本模块 order11）|
|---|---|
| `/list`（公开）| — |
| `/{id}/detail`（登录）| — |
| `/category-stats`（公开）| — |
| `/ranking`（Pro）| — |
| `/{id}/postgrad-directions`（Pro）| **`/{id}/competitions`（Pro，本模块接口 6）**|

> 接口 6 与 `postgrad-directions` 在路由模式、权限要求、Service 实现、Mapper SQL 写法上完全一致，仅关联表不同。
