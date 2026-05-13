# C端首页与个人中心 API 文档

## 功能概述

本模块实现 C 端用户的首页展示和个人中心功能，包含以下子模块：

| 模块 | 功能 | 权限要求 |
|------|------|----------|
| 首页管理 | 站点信息展示（ICP、联系方式、社交媒体） | 公开访问 |
| 模糊搜索 | 大学/城市/专业联想搜索（用于资料填写） | 需登录 |
| 用户资料 | 个人资料的查看与编辑 | 需登录 |
| 用户信息 | 账号信息、密码、微信号、头像管理 | 需登录 |
| 佣金管理 | 佣金查询、提现申请 | 需登录 |
| 邀请码绑定 | 后期绑定推荐人邀请码 | 需登录 |

---

## 权限说明

| 权限标识 | 说明 |
|----------|------|
| 公开 | 无需登录，任何人可访问 |
| @RequireLogin | 需要携带有效的 Access Token |

**Token 传递方式：**
```
Authorization: Bearer <access_token>
```

---

## 一、首页管理

### 1.1 获取站点信息

获取网站基础信息，用于页面底部展示。

**接口信息**

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/home/site-info` |
| 权限 | 公开 |
| 缓存 | Redis 缓存 1 小时 |

**请求示例**

```http
GET /api/v1/app/home/site-info HTTP/1.1
Host: api.haifeng.com
```

**响应示例**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "siteIcp": "京ICP备2024000001号-1",
    "contactUrl": {
      "wechat": "https://example.com/qrcode/wechat.png",
      "weibo": "https://weibo.com/haifeng",
      "zhihu": "https://zhihu.com/org/haifeng",
      "douyin": "https://douyin.com/haifeng",
      "bilibili": "https://space.bilibili.com/123456"
    },
    "basicMessage": {
      "address": "北京市海淀区中关村大街1号",
      "phone": "400-123-4567",
      "email": "contact@haifeng.com",
      "consultationTime": "周一至周五 9:00-18:00"
    }
  },
  "timestamp": 1715580000000
}
```

---

## 二、模糊搜索

> 用于个人资料填写时的联想输入，所有接口需登录后访问。

### 2.1 搜索大学

根据关键词模糊搜索大学名称。

**接口信息**

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/search/university` |
| 权限 | @RequireLogin |
| 模糊匹配字段 | `t_universities.name` |

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 是 | 搜索关键词 |
| limit | Integer | 否 | 返回数量，默认 10，最大 20 |

**请求示例**

```http
GET /api/v1/app/search/university?keyword=北京&limit=5 HTTP/1.1
Host: api.haifeng.com
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例**

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    { "id": 1001, "name": "北京大学" },
    { "id": 1002, "name": "北京理工大学" },
    { "id": 1003, "name": "北京师范大学" },
    { "id": 1004, "name": "北京航空航天大学" },
    { "id": 1005, "name": "北京交通大学" }
  ],
  "timestamp": 1715580000000
}
```

### 2.2 搜索城市

根据关键词模糊搜索城市名称。

**接口信息**

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/search/city` |
| 权限 | @RequireLogin |
| 模糊匹配字段 | `t_city.city_name` |

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 是 | 搜索关键词 |
| limit | Integer | 否 | 返回数量，默认 10，最大 20 |

**请求示例**

```http
GET /api/v1/app/search/city?keyword=上海&limit=5 HTTP/1.1
Host: api.haifeng.com
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例**

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    { "id": 2001, "name": "上海" }
  ],
  "timestamp": 1715580000000
}
```

### 2.3 搜索专业

根据关键词模糊搜索专业名称。

**接口信息**

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/search/major` |
| 权限 | @RequireLogin |
| 模糊匹配字段 | `t_major.major_name` |

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 是 | 搜索关键词 |
| limit | Integer | 否 | 返回数量，默认 10，最大 20 |

**请求示例**

```http
GET /api/v1/app/search/major?keyword=计算机&limit=5 HTTP/1.1
Host: api.haifeng.com
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例**

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    { "id": 3001, "name": "计算机科学与技术" },
    { "id": 3002, "name": "计算机应用技术" },
    { "id": 3003, "name": "计算机网络技术" }
  ],
  "timestamp": 1715580000000
}
```

---

## 三、用户资料（MemberProfile）

> 用户个人资料信息，与 t_member 一对一关联。

### 3.1 获取用户资料

获取当前登录用户的个人资料。

**接口信息**

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/member/profile` |
| 权限 | @RequireLogin |

**请求示例**

```http
GET /api/v1/app/member/profile HTTP/1.1
Host: api.haifeng.com
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "realName": "张三",
    "email": "zhangsan@example.com",
    "gender": "男",
    "schoolName": "北京大学",
    "province": "北京",
    "city": "北京",
    "major": "计算机科学与技术",
    "identity": "大学生",
    "grade": "大三",
    "educationLevel": "本科",
    "favoriteCount": 15,
    "viewCount": 128,
    "canEditSchool": true
  },
  "timestamp": 1715580000000
}
```

**字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| realName | String | 真实姓名 |
| email | String | 邮箱 |
| gender | String | 性别，可选值：`男`、`女` |
| schoolName | String | 学校名称（仅大学生/研究生显示） |
| province | String | 省份，34个省份/地区 |
| city | String | 城市 |
| major | String | 专业 |
| identity | String | 身份，可选值：`高中生`、`大学生`、`研究生`、`其他` |
| grade | String | 年级，如：`高一`、`大三`、`研一` |
| educationLevel | String | 学历层次 |
| favoriteCount | Integer | 收藏数 |
| viewCount | Integer | 浏览数 |
| canEditSchool | Boolean | 是否可编辑学校（身份为大学生/研究生时为 true） |

### 3.2 更新用户资料

更新当前登录用户的个人资料。

**接口信息**

| 项目 | 值 |
|------|-----|
| URL | `PUT /api/v1/app/member/profile` |
| 权限 | @RequireLogin |
| Content-Type | application/json |

**请求参数**

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| realName | String | 否 | 最大 50 字符 | 真实姓名 |
| email | String | 否 | 邮箱格式，最大 100 字符 | 邮箱 |
| gender | String | 否 | 枚举：`男`/`女` | 性别 |
| schoolName | String | 否 | 最大 100 字符，必须存在于大学表 | 学校名称 |
| province | String | 否 | 枚举：34 个省份 | 省份 |
| city | String | 否 | 最大 50 字符，必须存在于城市表 | 城市 |
| major | String | 否 | 最大 100 字符，必须存在于专业表 | 专业 |
| identity | String | 否 | 枚举：`高中生`/`大学生`/`研究生`/`其他` | 身份 |
| grade | String | 否 | 最大 20 字符 | 年级 |
| educationLevel | String | 否 | 最大 20 字符 | 学历层次 |

**请求示例**

```http
PUT /api/v1/app/member/profile HTTP/1.1
Host: api.haifeng.com
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "realName": "张三",
  "email": "zhangsan@example.com",
  "gender": "男",
  "identity": "大学生",
  "schoolName": "北京大学",
  "province": "北京",
  "city": "北京",
  "major": "计算机科学与技术",
  "grade": "大三",
  "educationLevel": "本科"
}
```

**响应示例**

```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715580000000
}
```

**业务规则**

1. **schoolName 条件编辑**：
   - 仅当 `identity` 为 `大学生` 或 `研究生` 时允许填写
   - 当 `identity` 改为 `高中生` 或 `其他` 时，`schoolName` 自动清空

2. **关联数据校验**：
   - `schoolName` 必须存在于 `t_universities` 表
   - `city` 必须存在于 `t_city` 表
   - `major` 必须存在于 `t_major` 表

**错误码**

| code | msg |
|------|-----|
| 400 | 性别值无效 |
| 400 | 身份值无效 |
| 400 | 省份值无效 |
| 400 | 城市不存在 |
| 400 | 专业不存在 |
| 400 | 学校不存在 |
| 400 | 当前身份不支持填写学校 |

---

## 四、用户信息（Member）

> 用户账号基础信息管理。

### 4.1 获取用户信息

获取当前登录用户的账号信息。

**接口信息**

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/member/info` |
| 权限 | @RequireLogin |

**请求示例**

```http
GET /api/v1/app/member/info HTTP/1.1
Host: api.haifeng.com
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "username": "zhangsan",
    "phone": "13800138000",
    "avatar": "https://example.com/avatar/123.jpg",
    "hasWechat": true,
    "inviteCode": "ABCD1234",
    "commissionBalance": 150.00,
    "commissionTotalEarned": 300.00,
    "commissionTotalPaid": 150.00,
    "memberType": "vip",
    "expireAt": "2025-12-31T23:59:59+08:00"
  },
  "timestamp": 1715580000000
}
```

**字段说明**

| 字段 | 类型 | 可编辑 | 说明 |
|------|------|--------|------|
| username | String | 是 | 用户名 |
| phone | String | 是 | 手机号 |
| avatar | String | 是 | 头像 URL |
| hasWechat | Boolean | - | 是否已绑定微信号 |
| inviteCode | String | 否 | 我的邀请码（8位） |
| commissionBalance | BigDecimal | 否 | 可提现余额 |
| commissionTotalEarned | BigDecimal | 否 | 累计获得佣金 |
| commissionTotalPaid | BigDecimal | 否 | 累计已发放佣金 |
| memberType | String | 否 | 会员类型：`normal`/`pro`/`vip` |
| expireAt | DateTime | 否 | 会员到期时间（normal 用户为 null） |

### 4.2 更新用户信息

更新用户名、手机号、头像。

**接口信息**

| 项目 | 值 |
|------|-----|
| URL | `PUT /api/v1/app/member/info` |
| 权限 | @RequireLogin |
| Content-Type | application/json |

**请求参数**

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| username | String | 否 | 2-50 字符 | 用户名 |
| phone | String | 否 | 手机号格式 `1[3-9]\d{9}` | 手机号 |
| avatar | String | 否 | 最大 500 字符 | 头像 URL |

**请求示例**

```http
PUT /api/v1/app/member/info HTTP/1.1
Host: api.haifeng.com
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "username": "zhangsan_new",
  "phone": "13900139000"
}
```

**响应示例**

```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715580000000
}
```

**错误码**

| code | msg |
|------|-----|
| 400 | 用户名已存在 |
| 400 | 手机号已存在 |

### 4.3 查看微信号明文

查看当前用户绑定的微信号（解密后）。

**接口信息**

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/member/wechat` |
| 权限 | @RequireLogin |

**请求示例**

```http
GET /api/v1/app/member/wechat HTTP/1.1
Host: api.haifeng.com
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例**

```json
{
  "code": 200,
  "msg": "success",
  "data": "wx_zhangsan_123",
  "timestamp": 1715580000000
}
```

### 4.4 修改微信号

更新用户微信号（AES 加密存储）。

**接口信息**

| 项目 | 值 |
|------|-----|
| URL | `PUT /api/v1/app/member/wechat` |
| 权限 | @RequireLogin |
| Content-Type | application/json |

**请求参数**

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| wechatId | String | 是 | 最大 50 字符 | 微信号 |

**请求示例**

```http
PUT /api/v1/app/member/wechat HTTP/1.1
Host: api.haifeng.com
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "wechatId": "wx_zhangsan_new"
}
```

**响应示例**

```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715580000000
}
```

### 4.5 修改密码

修改登录密码，需验证旧密码。

**接口信息**

| 项目 | 值 |
|------|-----|
| URL | `PUT /api/v1/app/member/password` |
| 权限 | @RequireLogin |
| Content-Type | application/json |

**请求参数**

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| oldPassword | String | 是 | 非空 | 旧密码 |
| newPassword | String | 是 | 6-20 字符 | 新密码 |

**请求示例**

```http
PUT /api/v1/app/member/password HTTP/1.1
Host: api.haifeng.com
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "oldPassword": "old123456",
  "newPassword": "new654321"
}
```

**响应示例**

```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715580000000
}
```

**错误码**

| code | msg |
|------|-----|
| 400 | 旧密码错误 |

### 4.6 修改头像

单独更新头像 URL。

**接口信息**

| 项目 | 值 |
|------|-----|
| URL | `PUT /api/v1/app/member/avatar` |
| 权限 | @RequireLogin |
| Content-Type | application/json |

**请求示例**

```http
PUT /api/v1/app/member/avatar HTTP/1.1
Host: api.haifeng.com
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "avatar": "https://example.com/avatar/new.jpg"
}
```

**响应示例**

```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715580000000
}
```

---

## 五、佣金管理

### 5.1 获取佣金信息

获取当前用户的佣金相关信息。

**接口信息**

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/member/commission` |
| 权限 | @RequireLogin |

**请求示例**

```http
GET /api/v1/app/member/commission HTTP/1.1
Host: api.haifeng.com
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "inviteCode": "ABCD1234",
    "commissionBalance": 150.00,
    "commissionTotalEarned": 300.00,
    "commissionTotalPaid": 150.00,
    "referralCount": 5,
    "referrerInviteCode": "EFGH5678"
  },
  "timestamp": 1715580000000
}
```

**字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| inviteCode | String | 我的邀请码（分享给他人） |
| commissionBalance | BigDecimal | 可提现余额 |
| commissionTotalEarned | BigDecimal | 累计获得佣金 |
| commissionTotalPaid | BigDecimal | 累计已发放佣金 |
| referralCount | Integer | 邀请人数（我邀请的用户数量） |
| referrerInviteCode | String | 我的推荐人邀请码（若无则为 null） |

### 5.2 申请提现

申请佣金提现，金额只能是 50 或 100。

**接口信息**

| 项目 | 值 |
|------|-----|
| URL | `POST /api/v1/app/member/withdraw` |
| 权限 | @RequireLogin |
| Content-Type | application/json |

**请求参数**

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| amount | BigDecimal | 是 | 只能是 `50.00` 或 `100.00` | 提现金额 |

**请求示例**

```http
POST /api/v1/app/member/withdraw HTTP/1.1
Host: api.haifeng.com
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "amount": 50.00
}
```

**响应示例**

```json
{
  "code": 200,
  "msg": "success",
  "data": 1790123456789012345,
  "timestamp": 1715580000000
}
```

> `data` 返回提现记录 ID（雪花算法生成）

**业务规则**

1. 提现金额只能是 **50** 或 **100**
2. `commissionBalance` 必须 >= 提现金额
3. 提现成功后，`commissionBalance` 自动扣减
4. 生成提现记录，状态为 `PENDING`

**错误码**

| code | msg |
|------|-----|
| 400 | 提现金额只能是50或100 |
| 400 | 余额不足 |

---

## 六、邀请码绑定

> 用于注册时未填写邀请码的用户，后期绑定推荐人。

### 6.1 预览邀请码

输入邀请码后，预览对应的推荐人信息。

**接口信息**

| 项目 | 值 |
|------|-----|
| URL | `GET /api/v1/app/member/referrer/preview` |
| 权限 | @RequireLogin |

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| inviteCode | String | 是 | 邀请码（8位） |

**请求示例**

```http
GET /api/v1/app/member/referrer/preview?inviteCode=EFGH5678 HTTP/1.1
Host: api.haifeng.com
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "username": "lisi",
    "phone": "138****8888"
  },
  "timestamp": 1715580000000
}
```

> `phone` 字段已脱敏处理

**校验规则**

1. 邀请码必须有效（能解析出用户 ID）
2. 对应用户必须存在且未删除
3. 对应用户未被禁用
4. 不能是自己的邀请码
5. 不能形成循环引用（推荐人的推荐人不能是自己）

**错误码**

| code | msg |
|------|-----|
| 400 | 邀请码无效 |
| 400 | 不能填写自己的邀请码 |
| 400 | 邀请码对应的用户不存在 |
| 400 | 邀请码对应的用户已被禁用 |
| 400 | 不能互相绑定邀请码 |

### 6.2 确认绑定邀请码

确认绑定推荐人，绑定后不可修改。

**接口信息**

| 项目 | 值 |
|------|-----|
| URL | `POST /api/v1/app/member/referrer/bind` |
| 权限 | @RequireLogin |
| Content-Type | application/json |

**请求参数**

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|------|----------|------|
| inviteCode | String | 是 | 8 位字符 | 邀请码 |

**请求示例**

```http
POST /api/v1/app/member/referrer/bind HTTP/1.1
Host: api.haifeng.com
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "inviteCode": "EFGH5678"
}
```

**响应示例**

```json
{
  "code": 200,
  "msg": "success",
  "data": null,
  "timestamp": 1715580000000
}
```

**业务规则**

1. 只有未绑定推荐人的用户才能绑定
2. 绑定后 **不可修改**
3. 执行与预览相同的校验规则

**错误码**

| code | msg |
|------|-----|
| 400 | 已绑定邀请码，不可修改 |
| 400 | 邀请码无效 |
| 400 | 不能填写自己的邀请码 |
| 400 | 邀请码对应的用户不存在 |
| 400 | 邀请码对应的用户已被禁用 |
| 400 | 不能互相绑定邀请码 |

---

## 附录

### A. 省份枚举值

共 34 个省份/地区：

```
北京、天津、河北、山西、内蒙古、辽宁、吉林、黑龙江、上海、江苏、
浙江、安徽、福建、江西、山东、河南、湖北、湖南、广东、广西、
海南、重庆、四川、贵州、云南、西藏、陕西、甘肃、青海、宁夏、
新疆、香港、澳门、台湾
```

### B. 身份枚举值

| 值 | 说明 |
|-----|------|
| 高中生 | 高中在读学生 |
| 大学生 | 本科在读学生 |
| 研究生 | 硕士/博士在读学生 |
| 其他 | 其他身份 |

### C. 性别枚举值

| 值 | 说明 |
|-----|------|
| 男 | 男性 |
| 女 | 女性 |

### D. 会员类型

| 值 | 说明 |
|-----|------|
| normal | 普通用户 |
| pro | Pro 会员 |
| vip | VIP 会员 |

### E. 通用错误码

| code | msg | 说明 |
|------|-----|------|
| 200 | success | 成功 |
| 400 | * | 参数错误/业务校验失败 |
| 401 | 未登录或Token已过期 | 需重新登录 |
| 404 | 用户不存在 | 用户已删除或不存在 |
| 500 | 服务器内部错误 | 系统异常 |
