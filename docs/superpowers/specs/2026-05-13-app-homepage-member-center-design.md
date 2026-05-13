# C端首页展示与个人中心设计

## 概述

本设计文档覆盖 C 端（haifeng-app）的首页展示和个人中心功能，包括：
- 任务1：数据库表 `t_member_profile`
- 任务2-1：个人中心 - 用户资料（MemberProfile）
- 任务2-2：个人中心 - 用户信息（Member）
- 任务3：提现功能 + 邀请码后绑定
- 任务4：界面管理 + 模糊搜索

## 设计决策

| 决策项 | 选择 | 理由 |
|-------|------|------|
| member_id 类型 | BIGINT | 与项目统一使用雪花算法保持一致 |
| 省份枚举 | 34个（含港澳台） | 覆盖全部行政区划 |
| 模糊搜索 | 独立接口 | 3 个独立接口，前端调用实现联想 |
| 提现阈值 | 硬编码 50/100 | 简单直接，无需后台配置 |
| 系统设置缓存 | Redis + 1h TTL | 支持多实例，后台修改时主动失效 |
| 邀请码绑定 | 两步确认 | 预览 → 确认，防止误操作 |
| 身份选项 | 枚举 | 高中生/大学生/研究生/其他 |
| school_name 显示 | 条件显示 | 仅大学生/研究生可见，因院校表仅含大学 |

---

## 任务1：数据库设计

### Flyway 迁移文件

**文件名**: `apps_V16__user_profile__tables.sql`

```sql
-- ============================================================
-- 用户资料表 (t_member_profile)
-- 描述：与 t_member 一对一，存储用户的个人资料（可选填）
-- ============================================================

BEGIN;

CREATE TABLE IF NOT EXISTS t_member_profile (
    id                      BIGINT          PRIMARY KEY,
    member_id               BIGINT          NOT NULL UNIQUE,

    -- 个人信息
    real_name               VARCHAR(50),
    email                   VARCHAR(100),
    gender                  VARCHAR(10),
    school_name             VARCHAR(100),
    province                VARCHAR(30),
    city                    VARCHAR(50),
    major                   VARCHAR(100),
    identity                VARCHAR(20),
    grade                   VARCHAR(20),
    education_level         VARCHAR(20),

    -- 统计字段
    favorite_count          INTEGER         DEFAULT 0 NOT NULL,
    view_count              INTEGER         DEFAULT 0 NOT NULL,

    -- 审计字段
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- 索引
CREATE INDEX idx_profile_member_id ON t_member_profile (member_id);
CREATE INDEX idx_profile_identity  ON t_member_profile (identity);
CREATE INDEX idx_profile_province  ON t_member_profile (province);

-- 外键
ALTER TABLE t_member_profile
    ADD CONSTRAINT fk_profile_member
    FOREIGN KEY (member_id) REFERENCES t_member(id);

-- 触发器
CREATE TRIGGER trg_member_profile_updated_at
    BEFORE UPDATE ON t_member_profile
    FOR EACH ROW EXECUTE FUNCTION fn_update_timestamp();

-- 注释
COMMENT ON TABLE  t_member_profile              IS '用户资料表：与 t_member 一对一';
COMMENT ON COLUMN t_member_profile.member_id    IS '关联会员表ID';
COMMENT ON COLUMN t_member_profile.real_name    IS '真实姓名';
COMMENT ON COLUMN t_member_profile.email        IS '邮箱';
COMMENT ON COLUMN t_member_profile.gender       IS '性别（男/女）';
COMMENT ON COLUMN t_member_profile.school_name  IS '学校名称';
COMMENT ON COLUMN t_member_profile.province     IS '省份';
COMMENT ON COLUMN t_member_profile.city         IS '城市';
COMMENT ON COLUMN t_member_profile.major        IS '专业';
COMMENT ON COLUMN t_member_profile.identity     IS '身份（高中生/大学生/研究生/其他）';
COMMENT ON COLUMN t_member_profile.grade        IS '年级';
COMMENT ON COLUMN t_member_profile.education_level IS '学历层次';
COMMENT ON COLUMN t_member_profile.favorite_count  IS '收藏数';
COMMENT ON COLUMN t_member_profile.view_count      IS '浏览数';

COMMIT;
```

### 新增枚举类

#### ProvinceEnum.java

```java
package com.haifeng.common.enums;

public enum ProvinceEnum {
    BEIJING("北京"),
    TIANJIN("天津"),
    HEBEI("河北"),
    SHANXI("山西"),
    NEIMENGGU("内蒙古"),
    LIAONING("辽宁"),
    JILIN("吉林"),
    HEILONGJIANG("黑龙江"),
    SHANGHAI("上海"),
    JIANGSU("江苏"),
    ZHEJIANG("浙江"),
    ANHUI("安徽"),
    FUJIAN("福建"),
    JIANGXI("江西"),
    SHANDONG("山东"),
    HENAN("河南"),
    HUBEI("湖北"),
    HUNAN("湖南"),
    GUANGDONG("广东"),
    GUANGXI("广西"),
    HAINAN("海南"),
    CHONGQING("重庆"),
    SICHUAN("四川"),
    GUIZHOU("贵州"),
    YUNNAN("云南"),
    XIZANG("西藏"),
    SHAANXI("陕西"),
    GANSU("甘肃"),
    QINGHAI("青海"),
    NINGXIA("宁夏"),
    XINJIANG("新疆"),
    HONGKONG("香港"),
    MACAO("澳门"),
    TAIWAN("台湾");

    private final String desc;

    ProvinceEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
```

#### IdentityEnum.java

```java
package com.haifeng.common.enums;

public enum IdentityEnum {
    HIGH_SCHOOL("高中生"),
    COLLEGE("大学生"),
    GRADUATE("研究生"),
    OTHER("其他");

    private final String desc;

    IdentityEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
```

#### GenderEnum.java

```java
package com.haifeng.common.enums;

public enum GenderEnum {
    MALE("男"),
    FEMALE("女");

    private final String desc;

    GenderEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
```

---

## 任务2-1：个人中心 - 用户资料（MemberProfile）

### API 设计

| 方法 | 路径 | 说明 | 权限 |
|-----|------|------|------|
| GET | `/api/v1/app/member/profile` | 获取当前用户资料 | @RequireLogin |
| PUT | `/api/v1/app/member/profile` | 更新用户资料 | @RequireLogin |

### 可编辑字段

| 字段 | 类型 | 说明 | 校验 |
|-----|------|------|------|
| realName | String | 真实姓名 | 最大50字符 |
| email | String | 邮箱 | 邮箱格式 |
| gender | String | 性别 | 枚举：男/女 |
| schoolName | String | 学校名称 | 仅大学生/研究生可填，需存在于 t_universities |
| province | String | 省份 | 枚举：34个省份 |
| city | String | 城市 | 需存在于 t_city |
| major | String | 专业 | 需存在于 t_major |
| identity | String | 身份 | 枚举：高中生/大学生/研究生/其他 |
| grade | String | 年级 | 最大20字符 |
| educationLevel | String | 学历层次 | 最大20字符 |

### 业务规则

1. **首次获取**：若 `t_member_profile` 无记录，自动创建空记录
2. **school_name 条件显示**：
   - 仅当 `identity` = 大学生 或 研究生 时，显示/允许编辑
   - 当 `identity` 改为 高中生/其他 时，`schoolName` 自动置空
3. **字段校验**：`schoolName`、`city`、`major` 需校验存在于对应表

### 文件清单

| 文件 | 路径 |
|-----|------|
| ProfileController.java | haifeng-app/controller/member/ |
| ProfileService.java | haifeng-app/service/member/ |
| ProfileServiceImpl.java | haifeng-app/service/impl/member/ |
| ProfileUpdateDTO.java | haifeng-app/dto/member/ |
| ProfileVO.java | haifeng-app/vo/member/ |
| MemberProfile.java | haifeng-common/entity/user/ (补充字段) |
| MemberProfileMapper.java | haifeng-common/mapper/user/ (新增) |

---

## 任务2-2：个人中心 - 用户信息（Member）

### API 设计

| 方法 | 路径 | 说明 | 权限 |
|-----|------|------|------|
| GET | `/api/v1/app/member/info` | 获取当前用户信息 | @RequireLogin |
| PUT | `/api/v1/app/member/info` | 更新用户信息 | @RequireLogin |
| GET | `/api/v1/app/member/wechat` | 查看微信号明文 | @RequireLogin |
| PUT | `/api/v1/app/member/wechat` | 修改微信号 | @RequireLogin |
| PUT | `/api/v1/app/member/password` | 修改密码 | @RequireLogin |
| PUT | `/api/v1/app/member/avatar` | 修改头像 | @RequireLogin |

### 字段分类

**可编辑字段：**
| 字段 | 接口 | 说明 |
|-----|------|------|
| avatar | PUT /avatar | 头像URL |
| phone | PUT /info | 手机号（校验唯一性） |
| username | PUT /info | 用户名（校验唯一性） |
| password | PUT /password | 密码（需验证旧密码） |
| wechatId | PUT /wechat | 微信号（AES加密+盲索引） |

**只读字段：**
| 字段 | 说明 |
|-----|------|
| inviteCode | 我的邀请码 |
| commissionBalance | 可提现余额 |
| commissionTotalEarned | 累计获得佣金 |
| commissionTotalPaid | 累计已发放佣金 |
| memberType | 会员类型 |
| expireAt | 会员到期时间 |

### 业务规则

1. **微信号查看**：通过 `AESEncryptTypeHandler` 自动解密
2. **微信号修改**：加密存储 + 更新盲索引 `wechatIdIndex`
3. **密码修改**：需验证旧密码，BCrypt 加密新密码
4. **手机号/用户名修改**：需校验唯一性

### 文件清单

| 文件 | 路径 |
|-----|------|
| MemberInfoController.java | haifeng-app/controller/member/ |
| MemberInfoService.java | haifeng-app/service/member/ |
| MemberInfoServiceImpl.java | haifeng-app/service/impl/member/ |
| MemberInfoUpdateDTO.java | haifeng-app/dto/member/ |
| PasswordUpdateDTO.java | haifeng-app/dto/member/ |
| WechatUpdateDTO.java | haifeng-app/dto/member/ |
| MemberInfoVO.java | haifeng-app/vo/member/ |

---

## 任务3：提现功能 + 邀请码绑定

### API 设计

| 方法 | 路径 | 说明 | 权限 |
|-----|------|------|------|
| GET | `/api/v1/app/member/commission` | 获取佣金信息 | @RequireLogin |
| POST | `/api/v1/app/member/withdraw` | 申请提现 | @RequireLogin |
| GET | `/api/v1/app/member/referrer/preview` | 预览邀请码对应用户 | @RequireLogin |
| POST | `/api/v1/app/member/referrer/bind` | 确认绑定邀请码 | @RequireLogin |

### 佣金信息 VO

```java
public class CommissionVO {
    private String inviteCode;              // 我的邀请码
    private BigDecimal commissionBalance;   // 可提现余额
    private BigDecimal commissionTotalEarned; // 累计获得
    private BigDecimal commissionTotalPaid;   // 累计已发放
    private Integer referralCount;          // 邀请人数
    private String referrerInviteCode;      // 我的推荐人邀请码（若有）
}
```

### 提现逻辑

1. **金额限制**：仅允许 50 或 100（硬编码常量）
2. **余额校验**：`commissionBalance >= 提现金额`
3. **创建记录**：
   - 生成 `t_withdraw_record`
   - 状态：`PENDING`
   - 记录：memberId, memberName, phone, wechatId, wechatIdIndex, amount
4. **扣减余额**：`commissionBalance -= 提现金额`

### 邀请码绑定逻辑

**第一步：预览** `GET /referrer/preview?inviteCode=XXX`

校验规则：
1. 邀请码有效（能解析出用户ID）
2. 对应用户存在且未删除
3. 对应用户未被禁用
4. 不能是自己的邀请码
5. 无循环引用（推荐人的 referrerId 不能是当前用户）

返回：
```java
public class ReferrerPreviewVO {
    private String username;        // 推荐人用户名
    private String phone;           // 推荐人手机号（脱敏：138****0000）
}
```

**第二步：确认绑定** `POST /referrer/bind`

校验规则：
1. 当前用户 `referrerId` 为空（只能绑定一次）
2. 再次执行预览的所有校验

操作：
- 设置 `referrerId` = 推荐人ID
- 设置 `referrerUsername` = 推荐人用户名

### 文件清单

| 文件 | 路径 |
|-----|------|
| CommissionController.java | haifeng-app/controller/member/ |
| CommissionService.java | haifeng-app/service/member/ |
| CommissionServiceImpl.java | haifeng-app/service/impl/member/ |
| WithdrawDTO.java | haifeng-app/dto/member/ |
| ReferrerBindDTO.java | haifeng-app/dto/member/ |
| CommissionVO.java | haifeng-app/vo/member/ |
| ReferrerPreviewVO.java | haifeng-app/vo/member/ |

---

## 任务4：界面管理 + 模糊搜索

### 界面管理 API

| 方法 | 路径 | 说明 | 权限 |
|-----|------|------|------|
| GET | `/api/v1/app/home/site-info` | 获取站点信息 | 无需登录 |

### 返回 VO

```java
public class SiteInfoVO {
    private String siteIcp;
    private ContactUrl contactUrl;      // wechat, weibo, zhihu, douyin, bilibili
    private BasicMessage basicMessage;  // address, phone, email, consultationTime
}
```

### 缓存策略

- **Redis Key**: `haifeng:app:site-info`
- **TTL**: 1 小时
- **失效**：后台修改 `system_settings` 时删除缓存

### 模糊搜索 API

| 方法 | 路径 | 查询表 | 权限 |
|-----|------|------|------|
| GET | `/api/v1/app/search/university` | t_universities | 无需登录 |
| GET | `/api/v1/app/search/city` | t_city | 无需登录 |
| GET | `/api/v1/app/search/major` | t_major | 无需登录 |

### 请求参数

| 参数 | 类型 | 说明 |
|-----|------|------|
| keyword | String | 搜索关键词 |
| limit | Integer | 返回数量，默认10，最大20 |

### 返回 VO

```java
public class SearchResultVO {
    private Long id;
    private String name;
}
```

### 查询逻辑

- 使用 `LIKE '%keyword%'` 模糊匹配
- 优先匹配开头（ORDER BY CASE WHEN name LIKE 'keyword%' THEN 0 ELSE 1 END）
- 默认返回 10 条

### 文件清单

| 文件 | 路径 |
|-----|------|
| SiteController.java | haifeng-app/controller/home/ |
| SiteService.java | haifeng-app/service/home/ |
| SiteServiceImpl.java | haifeng-app/service/impl/home/ |
| SiteInfoVO.java | haifeng-app/vo/home/ |
| SearchController.java | haifeng-app/controller/search/ |
| SearchService.java | haifeng-app/service/search/ |
| SearchServiceImpl.java | haifeng-app/service/impl/search/ |
| SearchResultVO.java | haifeng-app/vo/search/ |

---

## 完整文件清单

### haifeng-common 模块

| 类型 | 文件 | 操作 |
|-----|------|------|
| Entity | entity/user/MemberProfile.java | 补充字段：gender, schoolName, favoriteCount, viewCount |
| Mapper | mapper/user/MemberProfileMapper.java | 新增 |
| Enum | enums/ProvinceEnum.java | 新增 |
| Enum | enums/IdentityEnum.java | 新增 |
| Enum | enums/GenderEnum.java | 新增 |

### haifeng-app 模块

| 类型 | 文件 |
|-----|------|
| Controller | controller/member/ProfileController.java |
| Controller | controller/member/MemberInfoController.java |
| Controller | controller/member/CommissionController.java |
| Controller | controller/home/SiteController.java |
| Controller | controller/search/SearchController.java |
| Service | service/member/ProfileService.java |
| Service | service/member/MemberInfoService.java |
| Service | service/member/CommissionService.java |
| Service | service/home/SiteService.java |
| Service | service/search/SearchService.java |
| ServiceImpl | service/impl/member/ProfileServiceImpl.java |
| ServiceImpl | service/impl/member/MemberInfoServiceImpl.java |
| ServiceImpl | service/impl/member/CommissionServiceImpl.java |
| ServiceImpl | service/impl/home/SiteServiceImpl.java |
| ServiceImpl | service/impl/search/SearchServiceImpl.java |
| DTO | dto/member/ProfileUpdateDTO.java |
| DTO | dto/member/MemberInfoUpdateDTO.java |
| DTO | dto/member/PasswordUpdateDTO.java |
| DTO | dto/member/WechatUpdateDTO.java |
| DTO | dto/member/WithdrawDTO.java |
| DTO | dto/member/ReferrerBindDTO.java |
| VO | vo/member/ProfileVO.java |
| VO | vo/member/MemberInfoVO.java |
| VO | vo/member/CommissionVO.java |
| VO | vo/member/ReferrerPreviewVO.java |
| VO | vo/home/SiteInfoVO.java |
| VO | vo/search/SearchResultVO.java |

### Flyway 迁移

| 文件 |
|-----|
| apps_V16__user_profile__tables.sql |

---

## Redis Key 设计

| Key | 说明 | TTL |
|-----|------|-----|
| haifeng:app:site-info | 站点信息缓存 | 1小时 |

---

## 权限矩阵

| 接口 | normal | pro | vip |
|-----|--------|-----|-----|
| GET /home/site-info | O | O | O |
| GET /search/* | O | O | O |
| GET /member/profile | O | O | O |
| PUT /member/profile | O | O | O |
| GET /member/info | O | O | O |
| PUT /member/info | O | O | O |
| GET /member/wechat | O | O | O |
| PUT /member/wechat | O | O | O |
| PUT /member/password | O | O | O |
| PUT /member/avatar | O | O | O |
| GET /member/commission | O | O | O |
| POST /member/withdraw | O | O | O |
| GET /member/referrer/preview | O | O | O |
| POST /member/referrer/bind | O | O | O |

注：O = 允许，个人中心功能对所有已登录用户开放
