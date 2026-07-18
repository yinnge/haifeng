# 高考算法全局配置 & AI厂商余额查询 API 文档

## 概述

本文档包含三部分：

| 模块 | 说明 | 表 |
|------|------|-----|
| 省份算法配置 (ProvinceConfig) | 各省独立的算法参数 | `t_province_config` |
| 高考算法全局参数 (GaokaoConfig) | 全局共用的算法参数（单例） | `gaokao_config` |
| AI厂商余额查询 (AiBalance) | 查询 DeepSeek 厂商当前账户余额 | `t_model_provider` |

### 基础路径

- 省份算法配置：`/api/v1/admin/algorithm/config/province-config`
- 高考算法全局参数：`/api/v1/admin/algorithm/config/gaokao-config`

### 认证要求

所有接口需要 JWT Token，并匹配对应的 `@RequireAdminModule` 权限。

### 统一响应格式

```json
{
  "code": 200,
  "msg": "success",
  "data": {},
  "timestamp": 1234567890
}
```

---

## 一、省份算法配置 (ProvinceConfig)

**基础路径：** `/api/v1/admin/algorithm/config/province-config`

**权限：** `@RequireAdminModule("algo_prov_config")`

**功能说明：**

- 数据来源：由数据库迁移脚本从 `t_province_reform` 自动同步，不支持手动新增
- 不支持删除操作
- 仅提供分页查询、详情查看、参数修改

### 1.1 分页查询

```
GET /api/v1/admin/algorithm/config/province-config/page
```

**请求参数 (Query String)：**

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| page | Integer | 否 | 1 | 页码，最小1 |
| size | Integer | 否 | 10 | 每页条数，可选 10/20/30/50/100 |

**排序规则：** `province ASC`

**响应示例：**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "province": "北京市",
        "densityK": 0.150,
        "lineSteepness": 2.80,
        "rankSteepness": 2.40
      }
    ],
    "total": 31,
    "size": 10,
    "current": 1,
    "pages": 4
  },
  "timestamp": 1234567890
}
```

**列表字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| province | String | 省份名称（主键） |
| densityK | Decimal(4,3) | 同分密度惩罚系数 |
| lineSteepness | Decimal(4,2) | 线差 Sigmoid 陡度 |
| rankSteepness | Decimal(4,2) | 位次 Sigmoid 陡度 |

---

### 1.2 详情

```
GET /api/v1/admin/algorithm/config/province-config/{province}
```

**路径参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| province | String | 是 | 省份名称（URL 编码） |

**响应示例：**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "province": "北京市",
    "densityK": 0.150,
    "lineSteepness": 2.80,
    "rankSteepness": 2.40,
    "createdAt": "2026-07-01T00:00:00+08:00"
  },
  "timestamp": 1234567890
}
```

**详情字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| province | String | 省份名称 |
| densityK | Decimal(4,3) | 同分密度惩罚系数，默认 0.150 |
| lineSteepness | Decimal(4,2) | 线差 Sigmoid 陡度，默认 2.80 |
| rankSteepness | Decimal(4,2) | 位次 Sigmoid 陡度，默认 2.40 |
| createdAt | Timestamptz | 创建时间 |

---

### 1.3 修改

```
PUT /api/v1/admin/algorithm/config/province-config/{province}
```

**路径参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| province | String | 是 | 省份名称（URL 编码） |

**请求体 (JSON)：**

```json
{
  "densityK": 0.180,
  "lineSteepness": 3.00,
  "rankSteepness": 2.60
}
```

**请求字段说明：**

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|:----:|----------|------|
| densityK | Decimal(4,3) | ✓ | [0.000, 1.000] | 同分密度惩罚系数 |
| lineSteepness | Decimal(4,2) | ✓ | [0.00, 10.00] | 线差 Sigmoid 陡度 |
| rankSteepness | Decimal(4,2) | ✓ | [0.00, 10.00] | 位次 Sigmoid 陡度 |

**操作日志：** `@OperationLog(module = "省份算法配置", action = "修改省份算法参数")`

**错误响应：**

| code | msg | 说明 |
|------|-----|------|
| 404 | 省份配置不存在 | 该省份尚未初始化配置 |
| 400 | 同分密度惩罚系数不能小于0 | 参数校验不通过 |

---

## 二、高考算法全局参数 (GaokaoConfig)

**基础路径：** `/api/v1/admin/algorithm/config/gaokao-config`

**权限：** `@RequireAdminModule("algo_gaokao_config")`

**功能说明：**

- 单例表：数据库仅有一行记录，id 固定为 1
- 不提供分页列表、新增、删除操作
- 仅提供查看当前配置和修改配置

### 2.1 查看当前配置

```
GET /api/v1/admin/algorithm/config/gaokao-config/current
```

**响应示例：**

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "defaultDensityK": 0.150,
    "defaultLineSteepness": 2.80,
    "defaultRankSteepness": 2.40,
    "newGaokaoLineWeight": 0.42,
    "newGaokaoRankWeight": 0.50,
    "oldGaokaoLineWeight": 0.62,
    "oldGaokaoRankWeight": 0.30,
    "weightSoftGroup": 0.6,
    "weightSoftBoth": 0.3,
    "yearWeights": [1.00, 0.80, 0.60, 0.40, 0.20],
    "createdAt": "2026-07-01T00:00:00+08:00"
  },
  "timestamp": 1234567890
}
```

**响应字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| defaultDensityK | Decimal(4,3) | 默认同分密度惩罚系数（省份未配置时回退使用） |
| defaultLineSteepness | Decimal(4,2) | 默认线差 Sigmoid 陡度 |
| defaultRankSteepness | Decimal(4,2) | 默认位次 Sigmoid 陡度 |
| newGaokaoLineWeight | Decimal(4,2) | 新高考省份"线差"权重 |
| newGaokaoRankWeight | Decimal(4,2) | 新高考省份"位次"权重 |
| oldGaokaoLineWeight | Decimal(4,2) | 旧高考省份"线差"权重 |
| oldGaokaoRankWeight | Decimal(4,2) | 旧高考省份"位次"权重 |
| weightSoftGroup | Decimal(3,1) | 仅专业组命中软约束时的权重折扣 |
| weightSoftBoth | Decimal(3,1) | 专业组与专业同时命中软约束时的权重折扣 |
| yearWeights | Decimal(3,2)[] | 近5年历史录取数据衰减权重数组（下标0对应距今1年） |
| createdAt | Timestamptz | 创建时间 |

---

### 2.2 修改配置

```
PUT /api/v1/admin/algorithm/config/gaokao-config/current
```

**请求体 (JSON)：**

```json
{
  "defaultDensityK": 0.180,
  "defaultLineSteepness": 3.00,
  "defaultRankSteepness": 2.50,
  "newGaokaoLineWeight": 0.45,
  "newGaokaoRankWeight": 0.48,
  "oldGaokaoLineWeight": 0.60,
  "oldGaokaoRankWeight": 0.32,
  "weightSoftGroup": 0.5,
  "weightSoftBoth": 0.3,
  "yearWeights": [1.00, 0.80, 0.60, 0.40, 0.20]
}
```

**请求字段说明：**

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|------|------|:----:|----------|------|
| defaultDensityK | Decimal(4,3) | ✓ | [0.000, 1.000] | 默认同分密度惩罚系数 |
| defaultLineSteepness | Decimal(4,2) | ✓ | [0.00, 10.00] | 默认线差 Sigmoid 陡度 |
| defaultRankSteepness | Decimal(4,2) | ✓ | [0.00, 10.00] | 默认位次 Sigmoid 陡度 |
| newGaokaoLineWeight | Decimal(4,2) | ✓ | [0.00, 1.00] | 新高考线差权重 |
| newGaokaoRankWeight | Decimal(4,2) | ✓ | [0.00, 1.00] | 新高考位次权重 |
| oldGaokaoLineWeight | Decimal(4,2) | ✓ | [0.00, 1.00] | 旧高考线差权重 |
| oldGaokaoRankWeight | Decimal(4,2) | ✓ | [0.00, 1.00] | 旧高考位次权重 |
| weightSoftGroup | Decimal(3,1) | ✓ | [0.0, 1.0] | 专业组软约束折扣 |
| weightSoftBoth | Decimal(3,1) | ✓ | [0.0, 1.0] | 专业组+专业软约束折扣 |
| yearWeights | Decimal(3,2)[] | ✓ | 数组长度无限制 | 年份衰减权重数组 |

**操作日志：** `@OperationLog(module = "高考算法全局配置", action = "修改全局参数")`

**错误响应：**

| code | msg | 说明 |
|------|-----|------|
| 404 | 高考算法全局配置不存在 | 数据库未初始化（正常情况下有 V18 迁移脚本保证） |
| 400 | 新高考线差权重不能大于1 | 参数校验不通过 |

---

## 三、AI厂商余额查询 (AiBalance)

**基础路径：** `/api/v1/admin/system/model-providers`

**权限：** `@RequireAdminModule("system_provider")`

**功能说明：**

- 查询 `type='ai' AND provider_name='deepseek'` 的厂商配置余额
- 按 `apiKey` 去重，同一 key 关联的多个模型合并为一条记录
- Redis 缓存（key: `haifeng:ai:balance:{apiKey}`，TTL 5 分钟）
- `refresh=true` 跳过缓存直接调 DeepSeek API
- DeepSeek API 调用失败时优雅降级：`isAvailable=false`，余额字段为 null，不抛异常
- 无 DeepSeek 配置时返回空列表

### 3.1 查询 DeepSeek 厂商余额

```
GET /api/v1/admin/system/model-providers/balance
```

**请求参数 (Query String)：**

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| refresh | Boolean | 否 | false | true 时跳过缓存直接调 API |

**响应示例：**

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "providerName": "deepseek",
      "models": ["deepseek-chat", "deepseek-reasoner"],
      "isAvailable": true,
      "currency": "CNY",
      "totalBalance": 110.00,
      "grantedBalance": 10.00,
      "toppedUpBalance": 100.00
    }
  ],
  "timestamp": 1234567890
}
```

**响应字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| providerName | String | 厂商名称，固定 "deepseek" |
| models | String[] | 该 API Key 关联的模型列表 |
| isAvailable | Boolean | DeepSeek API 是否可用 |
| currency | String | 币种，如 CNY |
| totalBalance | BigDecimal | 总余额 |
| grantedBalance | BigDecimal | 赠送余额 |
| toppedUpBalance | BigDecimal | 充值余额 |

**异常场景：**

| 场景 | 行为 |
|------|------|
| DeepSeek API 超时/失败 | `isAvailable=false`，余额字段 null，不抛异常 |
| 无 DeepSeek 配置 | 返回空列表 `[]` |
| Redis 缓存命中 | 直接返回缓存，不调 API |

---

## 四、错误码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误 / 校验不通过 |
| 401 | 未登录 / Token 过期 |
| 403 | 无权限（未匹配 @RequireAdminModule） |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 五、操作日志

| Controller | module | action |
|-----------|--------|--------|
| ProvinceConfigController | 省份算法配置 | 修改省份算法参数 |
| GaokaoConfigController | 高考算法全局配置 | 修改全局参数 |
| ModelProviderController | 系统管理 | 查询AI厂商余额 |

---

## 六、文件结构

```
haifeng-common/src/main/java/com/haifeng/common/
├── entity/algorithm/
│   ├── ProvinceConfig.java          (已有)
│   └── GaokaoConfig.java            (已有)
└── mapper/algorithm/
    ├── ProvinceConfigMapper.java    (已有)
    └── GaokaoConfigMapper.java      (已有)

haifeng-admin/src/main/java/com/haifeng/admin/
├── controller/algorithm/config/
│   ├── ProvinceConfigController.java
│   └── GaokaoConfigController.java
├── controller/system/
│   └── ModelProviderController.java      (已有，新增 /balance 端点)
├── service/algorithm/config/
│   ├── ProvinceConfigService.java
│   └── GaokaoConfigService.java
├── service/system/
│   └── AiBalanceService.java
├── service/impl/algorithm/config/
│   ├── ProvinceConfigServiceImpl.java
│   └── GaokaoConfigServiceImpl.java
├── service/impl/system/
│   └── AiBalanceServiceImpl.java
├── dto/algorithm/config/
│   ├── ProvinceConfigQueryDTO.java
│   ├── ProvinceConfigUpdateDTO.java
│   └── GaokaoConfigUpdateDTO.java
└── vo/algorithm/config/
    ├── ProvinceConfigListVO.java
    ├── ProvinceConfigDetailVO.java
    └── GaokaoConfigDetailVO.java
```
