# C端首页展示与个人中心 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现 C 端首页展示（站点信息）和个人中心功能（用户资料、用户信息、提现、邀请码绑定、模糊搜索）

**Architecture:** 分层架构，Controller → Service → Mapper。haifeng-common 存放 Entity/Mapper/Enum，haifeng-app 存放业务代码。Redis 缓存站点信息，AES 加密微信号。

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, Redis, PostgreSQL, BCrypt, AES

---

## 文件结构

### haifeng-common 模块（新增/修改）

| 操作 | 文件路径 |
|-----|---------|
| 修改 | `entity/user/MemberProfile.java` - 补充 gender, schoolName, favoriteCount, viewCount |
| 新增 | `mapper/user/MemberProfileMapper.java` |
| 新增 | `enums/ProvinceEnum.java` |
| 新增 | `enums/IdentityEnum.java` |
| 新增 | `enums/GenderEnum.java` |

### haifeng-app 模块（新增）

| 类型 | 文件路径 |
|-----|---------|
| Controller | `controller/home/SiteController.java` |
| Controller | `controller/search/SearchController.java` |
| Controller | `controller/member/ProfileController.java` |
| Controller | `controller/member/MemberInfoController.java` |
| Controller | `controller/member/CommissionController.java` |
| Service | `service/home/SiteService.java` |
| Service | `service/search/SearchService.java` |
| Service | `service/member/ProfileService.java` |
| Service | `service/member/MemberInfoService.java` |
| Service | `service/member/CommissionService.java` |
| ServiceImpl | `service/impl/home/SiteServiceImpl.java` |
| ServiceImpl | `service/impl/search/SearchServiceImpl.java` |
| ServiceImpl | `service/impl/member/ProfileServiceImpl.java` |
| ServiceImpl | `service/impl/member/MemberInfoServiceImpl.java` |
| ServiceImpl | `service/impl/member/CommissionServiceImpl.java` |
| DTO | `dto/member/ProfileUpdateDTO.java` |
| DTO | `dto/member/MemberInfoUpdateDTO.java` |
| DTO | `dto/member/PasswordUpdateDTO.java` |
| DTO | `dto/member/WechatUpdateDTO.java` |
| DTO | `dto/member/WithdrawDTO.java` |
| DTO | `dto/member/ReferrerBindDTO.java` |
| VO | `vo/home/SiteInfoVO.java` |
| VO | `vo/search/SearchResultVO.java` |
| VO | `vo/member/ProfileVO.java` |
| VO | `vo/member/MemberInfoVO.java` |
| VO | `vo/member/CommissionVO.java` |
| VO | `vo/member/ReferrerPreviewVO.java` |

### Flyway 迁移

| 文件路径 |
|---------|
| `haifeng-admin/src/main/resources/db/migration/apps_V16__user_profile__tables.sql` |

---

## Task 1: 创建 Flyway 迁移文件

**Files:**
- Create: `haifeng-admin/src/main/resources/db/migration/apps_V16__user_profile__tables.sql`

- [ ] **Step 1: 创建迁移文件**

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

- [ ] **Step 2: 提交**

```bash
git add haifeng-admin/src/main/resources/db/migration/apps_V16__user_profile__tables.sql
git commit -m "feat(migration): add t_member_profile table"
```

---

## Task 2: 创建枚举类

**Files:**
- Create: `haifeng-common/src/main/java/com/haifeng/common/enums/ProvinceEnum.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/enums/IdentityEnum.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/enums/GenderEnum.java`

- [ ] **Step 1: 创建 ProvinceEnum**

```java
package com.haifeng.common.enums;

import lombok.Getter;

@Getter
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

    public static boolean isValid(String province) {
        if (province == null) return true;
        for (ProvinceEnum p : values()) {
            if (p.desc.equals(province)) {
                return true;
            }
        }
        return false;
    }
}
```

- [ ] **Step 2: 创建 IdentityEnum**

```java
package com.haifeng.common.enums;

import lombok.Getter;

@Getter
public enum IdentityEnum {
    HIGH_SCHOOL("高中生"),
    COLLEGE("大学生"),
    GRADUATE("研究生"),
    OTHER("其他");

    private final String desc;

    IdentityEnum(String desc) {
        this.desc = desc;
    }

    public static boolean isValid(String identity) {
        if (identity == null) return true;
        for (IdentityEnum i : values()) {
            if (i.desc.equals(identity)) {
                return true;
            }
        }
        return false;
    }

    public static boolean canHaveSchool(String identity) {
        return COLLEGE.desc.equals(identity) || GRADUATE.desc.equals(identity);
    }
}
```

- [ ] **Step 3: 创建 GenderEnum**

```java
package com.haifeng.common.enums;

import lombok.Getter;

@Getter
public enum GenderEnum {
    MALE("男"),
    FEMALE("女");

    private final String desc;

    GenderEnum(String desc) {
        this.desc = desc;
    }

    public static boolean isValid(String gender) {
        if (gender == null) return true;
        for (GenderEnum g : values()) {
            if (g.desc.equals(gender)) {
                return true;
            }
        }
        return false;
    }
}
```

- [ ] **Step 4: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/enums/ProvinceEnum.java
git add haifeng-common/src/main/java/com/haifeng/common/enums/IdentityEnum.java
git add haifeng-common/src/main/java/com/haifeng/common/enums/GenderEnum.java
git commit -m "feat(enums): add Province, Identity, Gender enums"
```

---

## Task 3: 完善 MemberProfile Entity 和 Mapper

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/entity/user/MemberProfile.java`
- Create: `haifeng-common/src/main/java/com/haifeng/common/mapper/user/MemberProfileMapper.java`

- [ ] **Step 1: 更新 MemberProfile 实体**

将现有 MemberProfile.java 替换为：

```java
package com.haifeng.common.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_member_profile")
public class MemberProfile {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long memberId;

    private String realName;

    private String email;

    private String gender;

    private String schoolName;

    private String province;

    private String city;

    private String major;

    private String identity;

    private String grade;

    private String educationLevel;

    private Integer favoriteCount;

    private Integer viewCount;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 2: 创建 MemberProfileMapper**

```java
package com.haifeng.common.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.user.MemberProfile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberProfileMapper extends BaseMapper<MemberProfile> {
}
```

- [ ] **Step 3: 提交**

```bash
git add haifeng-common/src/main/java/com/haifeng/common/entity/user/MemberProfile.java
git add haifeng-common/src/main/java/com/haifeng/common/mapper/user/MemberProfileMapper.java
git commit -m "feat(entity): update MemberProfile and add mapper"
```

---

## Task 4: 实现站点信息接口（SiteController）

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/home/SiteInfoVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/home/SiteService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/home/SiteServiceImpl.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/home/SiteController.java`

- [ ] **Step 1: 创建 SiteInfoVO**

```java
package com.haifeng.app.vo.home;

import com.haifeng.common.entity.system.BasicMessage;
import com.haifeng.common.entity.system.ContactUrl;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteInfoVO {

    private String siteIcp;

    private ContactUrl contactUrl;

    private BasicMessage basicMessage;
}
```

- [ ] **Step 2: 创建 SiteService 接口**

```java
package com.haifeng.app.service.home;

import com.haifeng.app.vo.home.SiteInfoVO;

public interface SiteService {

    SiteInfoVO getSiteInfo();
}
```

- [ ] **Step 3: 创建 SiteServiceImpl**

```java
package com.haifeng.app.service.impl.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.service.home.SiteService;
import com.haifeng.app.vo.home.SiteInfoVO;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.entity.system.SystemSettings;
import com.haifeng.common.mapper.system.SystemSettingsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService {

    private static final String CACHE_KEY = "haifeng:app:site-info";
    private static final long CACHE_TTL_HOURS = 1;

    private final SystemSettingsMapper settingsMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public SiteInfoVO getSiteInfo() {
        // 先查缓存
        SiteInfoVO cached = (SiteInfoVO) redisTemplate.opsForValue().get(CACHE_KEY);
        if (cached != null) {
            return cached;
        }

        // 查数据库
        SystemSettings settings = settingsMapper.selectOne(
                new LambdaQueryWrapper<SystemSettings>().last("LIMIT 1"));

        if (settings == null) {
            return SiteInfoVO.builder().build();
        }

        SiteInfoVO vo = SiteInfoVO.builder()
                .siteIcp(settings.getSiteIcp())
                .contactUrl(settings.getContactUrl())
                .basicMessage(settings.getBasicMessage())
                .build();

        // 写入缓存
        redisTemplate.opsForValue().set(CACHE_KEY, vo, CACHE_TTL_HOURS, TimeUnit.HOURS);

        return vo;
    }
}
```

- [ ] **Step 4: 创建 SiteController**

```java
package com.haifeng.app.controller.home;

import com.haifeng.app.service.home.SiteService;
import com.haifeng.app.vo.home.SiteInfoVO;
import com.haifeng.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/app/home")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;

    /**
     * 获取站点信息（公开接口，无需登录）
     */
    @GetMapping("/site-info")
    public R<SiteInfoVO> getSiteInfo() {
        return R.ok(siteService.getSiteInfo());
    }
}
```

- [ ] **Step 5: 提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/vo/home/SiteInfoVO.java
git add haifeng-app/src/main/java/com/haifeng/app/service/home/SiteService.java
git add haifeng-app/src/main/java/com/haifeng/app/service/impl/home/SiteServiceImpl.java
git add haifeng-app/src/main/java/com/haifeng/app/controller/home/SiteController.java
git commit -m "feat(app): add site info endpoint with Redis cache"
```

---

## Task 5: 实现模糊搜索接口（SearchController）

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/search/SearchResultVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/search/SearchService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/search/SearchServiceImpl.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/search/SearchController.java`

- [ ] **Step 1: 创建 SearchResultVO**

```java
package com.haifeng.app.vo.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultVO {

    private Long id;

    private String name;
}
```

- [ ] **Step 2: 创建 SearchService 接口**

```java
package com.haifeng.app.service.search;

import com.haifeng.app.vo.search.SearchResultVO;

import java.util.List;

public interface SearchService {

    List<SearchResultVO> searchUniversity(String keyword, Integer limit);

    List<SearchResultVO> searchCity(String keyword, Integer limit);

    List<SearchResultVO> searchMajor(String keyword, Integer limit);
}
```

- [ ] **Step 3: 创建 SearchServiceImpl**

```java
package com.haifeng.app.service.impl.search;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.service.search.SearchService;
import com.haifeng.app.vo.search.SearchResultVO;
import com.haifeng.common.entity.city.City;
import com.haifeng.common.entity.major.Major;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.mapper.city.CityMapper;
import com.haifeng.common.mapper.major.MajorMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 20;

    private final UniversityMapper universityMapper;
    private final CityMapper cityMapper;
    private final MajorMapper majorMapper;

    @Override
    public List<SearchResultVO> searchUniversity(String keyword, Integer limit) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }

        int actualLimit = normalizeLimit(limit);

        LambdaQueryWrapper<University> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(University::getName, keyword)
                .orderByAsc(University::getName)
                .last("LIMIT " + actualLimit);

        List<University> list = universityMapper.selectList(wrapper);

        return list.stream()
                .map(u -> SearchResultVO.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<SearchResultVO> searchCity(String keyword, Integer limit) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }

        int actualLimit = normalizeLimit(limit);

        LambdaQueryWrapper<City> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(City::getCityName, keyword)
                .orderByAsc(City::getCityName)
                .last("LIMIT " + actualLimit);

        List<City> list = cityMapper.selectList(wrapper);

        return list.stream()
                .map(c -> SearchResultVO.builder()
                        .id(c.getId())
                        .name(c.getCityName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<SearchResultVO> searchMajor(String keyword, Integer limit) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }

        int actualLimit = normalizeLimit(limit);

        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Major::getMajorName, keyword)
                .orderByAsc(Major::getMajorName)
                .last("LIMIT " + actualLimit);

        List<Major> list = majorMapper.selectList(wrapper);

        return list.stream()
                .map(m -> SearchResultVO.builder()
                        .id(m.getId())
                        .name(m.getMajorName())
                        .build())
                .collect(Collectors.toList());
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
```

- [ ] **Step 4: 创建 SearchController**

```java
package com.haifeng.app.controller.search;

import com.haifeng.app.service.search.SearchService;
import com.haifeng.app.vo.search.SearchResultVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/app/search")
@RequiredArgsConstructor
@RequireLogin
public class SearchController {

    private final SearchService searchService;

    /**
     * 搜索大学（用于个人资料填写）
     */
    @GetMapping("/university")
    public R<List<SearchResultVO>> searchUniversity(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer limit) {
        return R.ok(searchService.searchUniversity(keyword, limit));
    }

    /**
     * 搜索城市（用于个人资料填写）
     */
    @GetMapping("/city")
    public R<List<SearchResultVO>> searchCity(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer limit) {
        return R.ok(searchService.searchCity(keyword, limit));
    }

    /**
     * 搜索专业（用于个人资料填写）
     */
    @GetMapping("/major")
    public R<List<SearchResultVO>> searchMajor(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer limit) {
        return R.ok(searchService.searchMajor(keyword, limit));
    }
}
```

- [ ] **Step 5: 提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/vo/search/SearchResultVO.java
git add haifeng-app/src/main/java/com/haifeng/app/service/search/SearchService.java
git add haifeng-app/src/main/java/com/haifeng/app/service/impl/search/SearchServiceImpl.java
git add haifeng-app/src/main/java/com/haifeng/app/controller/search/SearchController.java
git commit -m "feat(app): add fuzzy search endpoints for profile"
```

---

## Task 6: 实现用户资料接口（ProfileController）

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/member/ProfileUpdateDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/member/ProfileVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/member/ProfileService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/member/ProfileServiceImpl.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/member/ProfileController.java`

- [ ] **Step 1: 创建 ProfileUpdateDTO**

```java
package com.haifeng.app.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateDTO {

    @Size(max = 50, message = "真实姓名最多50个字符")
    private String realName;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱最多100个字符")
    private String email;

    private String gender;

    @Size(max = 100, message = "学校名称最多100个字符")
    private String schoolName;

    @Size(max = 30, message = "省份最多30个字符")
    private String province;

    @Size(max = 50, message = "城市最多50个字符")
    private String city;

    @Size(max = 100, message = "专业最多100个字符")
    private String major;

    private String identity;

    @Size(max = 20, message = "年级最多20个字符")
    private String grade;

    @Size(max = 20, message = "学历层次最多20个字符")
    private String educationLevel;
}
```

- [ ] **Step 2: 创建 ProfileVO**

```java
package com.haifeng.app.vo.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileVO {

    private String realName;

    private String email;

    private String gender;

    private String schoolName;

    private String province;

    private String city;

    private String major;

    private String identity;

    private String grade;

    private String educationLevel;

    private Integer favoriteCount;

    private Integer viewCount;

    /**
     * 是否可以填写学校（仅大学生/研究生可填）
     */
    private Boolean canEditSchool;
}
```

- [ ] **Step 3: 创建 ProfileService 接口**

```java
package com.haifeng.app.service.member;

import com.haifeng.app.dto.member.ProfileUpdateDTO;
import com.haifeng.app.vo.member.ProfileVO;

public interface ProfileService {

    ProfileVO getProfile();

    void updateProfile(ProfileUpdateDTO dto);
}
```

- [ ] **Step 4: 创建 ProfileServiceImpl**

```java
package com.haifeng.app.service.impl.member;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.dto.member.ProfileUpdateDTO;
import com.haifeng.app.service.member.ProfileService;
import com.haifeng.app.vo.member.ProfileVO;
import com.haifeng.common.entity.city.City;
import com.haifeng.common.entity.major.Major;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.entity.user.MemberProfile;
import com.haifeng.common.enums.GenderEnum;
import com.haifeng.common.enums.IdentityEnum;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.city.CityMapper;
import com.haifeng.common.mapper.major.MajorMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.mapper.user.MemberProfileMapper;
import com.haifeng.common.util.SecurityUtil;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final MemberProfileMapper profileMapper;
    private final UniversityMapper universityMapper;
    private final CityMapper cityMapper;
    private final MajorMapper majorMapper;

    @Override
    public ProfileVO getProfile() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        MemberProfile profile = profileMapper.selectOne(
                new LambdaQueryWrapper<MemberProfile>()
                        .eq(MemberProfile::getMemberId, memberId));

        // 若不存在则自动创建空记录
        if (profile == null) {
            profile = createEmptyProfile(memberId);
        }

        boolean canEditSchool = IdentityEnum.canHaveSchool(profile.getIdentity());

        return ProfileVO.builder()
                .realName(profile.getRealName())
                .email(profile.getEmail())
                .gender(profile.getGender())
                .schoolName(canEditSchool ? profile.getSchoolName() : null)
                .province(profile.getProvince())
                .city(profile.getCity())
                .major(profile.getMajor())
                .identity(profile.getIdentity())
                .grade(profile.getGrade())
                .educationLevel(profile.getEducationLevel())
                .favoriteCount(profile.getFavoriteCount())
                .viewCount(profile.getViewCount())
                .canEditSchool(canEditSchool)
                .build();
    }

    @Override
    @Transactional
    public void updateProfile(ProfileUpdateDTO dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        MemberProfile profile = profileMapper.selectOne(
                new LambdaQueryWrapper<MemberProfile>()
                        .eq(MemberProfile::getMemberId, memberId));

        if (profile == null) {
            profile = createEmptyProfile(memberId);
        }

        // 校验枚举值
        if (dto.getGender() != null && !GenderEnum.isValid(dto.getGender())) {
            throw new BusinessException(400, "性别值无效");
        }
        if (dto.getIdentity() != null && !IdentityEnum.isValid(dto.getIdentity())) {
            throw new BusinessException(400, "身份值无效");
        }
        if (dto.getProvince() != null && !ProvinceEnum.isValid(dto.getProvince())) {
            throw new BusinessException(400, "省份值无效");
        }

        // 校验关联数据存在性
        if (StringUtils.hasText(dto.getCity())) {
            Long count = cityMapper.selectCount(
                    new LambdaQueryWrapper<City>().eq(City::getCityName, dto.getCity()));
            if (count == 0) {
                throw new BusinessException(400, "城市不存在");
            }
        }
        if (StringUtils.hasText(dto.getMajor())) {
            Long count = majorMapper.selectCount(
                    new LambdaQueryWrapper<Major>().eq(Major::getMajorName, dto.getMajor()));
            if (count == 0) {
                throw new BusinessException(400, "专业不存在");
            }
        }

        // 确定最终的identity值
        String finalIdentity = dto.getIdentity() != null ? dto.getIdentity() : profile.getIdentity();
        boolean canEditSchool = IdentityEnum.canHaveSchool(finalIdentity);

        // 校验学校（仅大学生/研究生可填）
        if (StringUtils.hasText(dto.getSchoolName())) {
            if (!canEditSchool) {
                throw new BusinessException(400, "当前身份不支持填写学校");
            }
            Long count = universityMapper.selectCount(
                    new LambdaQueryWrapper<University>().eq(University::getName, dto.getSchoolName()));
            if (count == 0) {
                throw new BusinessException(400, "学校不存在");
            }
        }

        // 更新字段（非null才更新）
        if (dto.getRealName() != null) profile.setRealName(dto.getRealName());
        if (dto.getEmail() != null) profile.setEmail(dto.getEmail());
        if (dto.getGender() != null) profile.setGender(dto.getGender());
        if (dto.getProvince() != null) profile.setProvince(dto.getProvince());
        if (dto.getCity() != null) profile.setCity(dto.getCity());
        if (dto.getMajor() != null) profile.setMajor(dto.getMajor());
        if (dto.getIdentity() != null) profile.setIdentity(dto.getIdentity());
        if (dto.getGrade() != null) profile.setGrade(dto.getGrade());
        if (dto.getEducationLevel() != null) profile.setEducationLevel(dto.getEducationLevel());

        // 学校处理
        if (canEditSchool && dto.getSchoolName() != null) {
            profile.setSchoolName(dto.getSchoolName());
        } else if (!canEditSchool) {
            // 身份改为高中生/其他时，清空学校
            profile.setSchoolName(null);
        }

        profile.setUpdatedAt(OffsetDateTime.now());
        profileMapper.updateById(profile);

        log.info("更新用户资料成功: memberId={}", memberId);
    }

    private MemberProfile createEmptyProfile(Long memberId) {
        MemberProfile profile = MemberProfile.builder()
                .id(SnowflakeIdGenerator.nextId())
                .memberId(memberId)
                .favoriteCount(0)
                .viewCount(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        profileMapper.insert(profile);
        return profile;
    }
}
```

- [ ] **Step 5: 创建 ProfileController**

```java
package com.haifeng.app.controller.member;

import com.haifeng.app.dto.member.ProfileUpdateDTO;
import com.haifeng.app.service.member.ProfileService;
import com.haifeng.app.vo.member.ProfileVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/app/member/profile")
@RequiredArgsConstructor
@RequireLogin
public class ProfileController {

    private final ProfileService profileService;

    /**
     * 获取当前用户资料
     */
    @GetMapping
    public R<ProfileVO> getProfile() {
        return R.ok(profileService.getProfile());
    }

    /**
     * 更新用户资料
     */
    @PutMapping
    public R<Void> updateProfile(@Valid @RequestBody ProfileUpdateDTO dto) {
        profileService.updateProfile(dto);
        return R.ok();
    }
}
```

- [ ] **Step 6: 提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/dto/member/ProfileUpdateDTO.java
git add haifeng-app/src/main/java/com/haifeng/app/vo/member/ProfileVO.java
git add haifeng-app/src/main/java/com/haifeng/app/service/member/ProfileService.java
git add haifeng-app/src/main/java/com/haifeng/app/service/impl/member/ProfileServiceImpl.java
git add haifeng-app/src/main/java/com/haifeng/app/controller/member/ProfileController.java
git commit -m "feat(app): add profile management endpoints"
```

---

## Task 7: 实现用户信息接口（MemberInfoController）

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/member/MemberInfoUpdateDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/member/PasswordUpdateDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/member/WechatUpdateDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/member/MemberInfoVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/member/MemberInfoService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/member/MemberInfoServiceImpl.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/member/MemberInfoController.java`

- [ ] **Step 1: 创建 DTOs**

MemberInfoUpdateDTO.java:
```java
package com.haifeng.app.dto.member;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MemberInfoUpdateDTO {

    @Size(min = 2, max = 50, message = "用户名长度2-50个字符")
    private String username;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Size(max = 500, message = "头像URL最多500个字符")
    private String avatar;
}
```

PasswordUpdateDTO.java:
```java
package com.haifeng.app.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordUpdateDTO {

    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度6-20个字符")
    private String newPassword;
}
```

WechatUpdateDTO.java:
```java
package com.haifeng.app.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WechatUpdateDTO {

    @NotBlank(message = "微信号不能为空")
    @Size(max = 50, message = "微信号最多50个字符")
    private String wechatId;
}
```

- [ ] **Step 2: 创建 MemberInfoVO**

```java
package com.haifeng.app.vo.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberInfoVO {

    // 可编辑字段
    private String username;

    private String phone;

    private String avatar;

    private Boolean hasWechat;

    // 只读字段
    private String inviteCode;

    private BigDecimal commissionBalance;

    private BigDecimal commissionTotalEarned;

    private BigDecimal commissionTotalPaid;

    private String memberType;

    private OffsetDateTime expireAt;
}
```

- [ ] **Step 3: 创建 MemberInfoService 接口**

```java
package com.haifeng.app.service.member;

import com.haifeng.app.dto.member.MemberInfoUpdateDTO;
import com.haifeng.app.dto.member.PasswordUpdateDTO;
import com.haifeng.app.dto.member.WechatUpdateDTO;
import com.haifeng.app.vo.member.MemberInfoVO;

public interface MemberInfoService {

    MemberInfoVO getInfo();

    void updateInfo(MemberInfoUpdateDTO dto);

    String getWechat();

    void updateWechat(WechatUpdateDTO dto);

    void updatePassword(PasswordUpdateDTO dto);

    void updateAvatar(String avatar);
}
```

- [ ] **Step 4: 创建 MemberInfoServiceImpl**

```java
package com.haifeng.app.service.impl.member;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.dto.member.MemberInfoUpdateDTO;
import com.haifeng.app.dto.member.PasswordUpdateDTO;
import com.haifeng.app.dto.member.WechatUpdateDTO;
import com.haifeng.app.service.member.MemberInfoService;
import com.haifeng.app.vo.member.MemberInfoVO;
import com.haifeng.common.config.SecurityProperties;
import com.haifeng.common.entity.user.Member;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.util.CryptoUtil;
import com.haifeng.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberInfoServiceImpl implements MemberInfoService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;

    @Override
    public MemberInfoVO getInfo() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberMapper.selectById(memberId);

        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        return MemberInfoVO.builder()
                .username(member.getUsername())
                .phone(member.getPhone())
                .avatar(member.getAvatar())
                .hasWechat(StringUtils.hasText(member.getWechatId()))
                .inviteCode(member.getInviteCode())
                .commissionBalance(member.getCommissionBalance())
                .commissionTotalEarned(member.getCommissionTotalEarned())
                .commissionTotalPaid(member.getCommissionTotalPaid())
                .memberType(member.getEffectiveMemberType())
                .expireAt(member.getExpireAt())
                .build();
    }

    @Override
    @Transactional
    public void updateInfo(MemberInfoUpdateDTO dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberMapper.selectById(memberId);

        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        // 校验用户名唯一性
        if (StringUtils.hasText(dto.getUsername()) && !dto.getUsername().equals(member.getUsername())) {
            Long count = memberMapper.selectCount(
                    new LambdaQueryWrapper<Member>()
                            .eq(Member::getUsername, dto.getUsername())
                            .eq(Member::getDeleted, false)
                            .ne(Member::getId, memberId));
            if (count > 0) {
                throw new BusinessException(400, "用户名已存在");
            }
            member.setUsername(dto.getUsername());
        }

        // 校验手机号唯一性
        if (StringUtils.hasText(dto.getPhone()) && !dto.getPhone().equals(member.getPhone())) {
            Long count = memberMapper.selectCount(
                    new LambdaQueryWrapper<Member>()
                            .eq(Member::getPhone, dto.getPhone())
                            .eq(Member::getDeleted, false)
                            .ne(Member::getId, memberId));
            if (count > 0) {
                throw new BusinessException(400, "手机号已存在");
            }
            member.setPhone(dto.getPhone());
        }

        if (dto.getAvatar() != null) {
            member.setAvatar(dto.getAvatar());
        }

        member.setUpdatedAt(OffsetDateTime.now());
        memberMapper.updateById(member);

        log.info("更新用户信息成功: memberId={}", memberId);
    }

    @Override
    public String getWechat() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberMapper.selectById(memberId);

        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        // 微信号已通过 TypeHandler 自动解密
        return member.getWechatId();
    }

    @Override
    @Transactional
    public void updateWechat(WechatUpdateDTO dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberMapper.selectById(memberId);

        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        // 更新微信号（TypeHandler 自动加密）
        member.setWechatId(dto.getWechatId());
        // 更新盲索引
        String blindIndex = CryptoUtil.blindIndex(dto.getWechatId(), securityProperties.getHashSalt());
        member.setWechatIdIndex(blindIndex);
        member.setUpdatedAt(OffsetDateTime.now());
        memberMapper.updateById(member);

        log.info("更新微信号成功: memberId={}", memberId);
    }

    @Override
    @Transactional
    public void updatePassword(PasswordUpdateDTO dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberMapper.selectById(memberId);

        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(dto.getOldPassword(), member.getPassword())) {
            throw new BusinessException(400, "旧密码错误");
        }

        // 更新密码
        member.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        member.setUpdatedAt(OffsetDateTime.now());
        memberMapper.updateById(member);

        log.info("修改密码成功: memberId={}", memberId);
    }

    @Override
    @Transactional
    public void updateAvatar(String avatar) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberMapper.selectById(memberId);

        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        member.setAvatar(avatar);
        member.setUpdatedAt(OffsetDateTime.now());
        memberMapper.updateById(member);

        log.info("更新头像成功: memberId={}", memberId);
    }
}
```

- [ ] **Step 5: 创建 MemberInfoController**

```java
package com.haifeng.app.controller.member;

import com.haifeng.app.dto.member.MemberInfoUpdateDTO;
import com.haifeng.app.dto.member.PasswordUpdateDTO;
import com.haifeng.app.dto.member.WechatUpdateDTO;
import com.haifeng.app.service.member.MemberInfoService;
import com.haifeng.app.vo.member.MemberInfoVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/app/member")
@RequiredArgsConstructor
@RequireLogin
public class MemberInfoController {

    private final MemberInfoService memberInfoService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public R<MemberInfoVO> getInfo() {
        return R.ok(memberInfoService.getInfo());
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public R<Void> updateInfo(@Valid @RequestBody MemberInfoUpdateDTO dto) {
        memberInfoService.updateInfo(dto);
        return R.ok();
    }

    /**
     * 查看微信号明文
     */
    @GetMapping("/wechat")
    public R<String> getWechat() {
        return R.ok(memberInfoService.getWechat());
    }

    /**
     * 修改微信号
     */
    @PutMapping("/wechat")
    public R<Void> updateWechat(@Valid @RequestBody WechatUpdateDTO dto) {
        memberInfoService.updateWechat(dto);
        return R.ok();
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public R<Void> updatePassword(@Valid @RequestBody PasswordUpdateDTO dto) {
        memberInfoService.updatePassword(dto);
        return R.ok();
    }

    /**
     * 修改头像
     */
    @PutMapping("/avatar")
    public R<Void> updateAvatar(@RequestBody @NotBlank String avatar) {
        memberInfoService.updateAvatar(avatar);
        return R.ok();
    }
}
```

- [ ] **Step 6: 提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/dto/member/MemberInfoUpdateDTO.java
git add haifeng-app/src/main/java/com/haifeng/app/dto/member/PasswordUpdateDTO.java
git add haifeng-app/src/main/java/com/haifeng/app/dto/member/WechatUpdateDTO.java
git add haifeng-app/src/main/java/com/haifeng/app/vo/member/MemberInfoVO.java
git add haifeng-app/src/main/java/com/haifeng/app/service/member/MemberInfoService.java
git add haifeng-app/src/main/java/com/haifeng/app/service/impl/member/MemberInfoServiceImpl.java
git add haifeng-app/src/main/java/com/haifeng/app/controller/member/MemberInfoController.java
git commit -m "feat(app): add member info management endpoints"
```

---

## Task 8: 实现佣金与提现接口（CommissionController）

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/member/WithdrawDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/member/ReferrerBindDTO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/member/CommissionVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/member/ReferrerPreviewVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/member/CommissionService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/member/CommissionServiceImpl.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/member/CommissionController.java`

- [ ] **Step 1: 创建 DTOs 和 VOs**

WithdrawDTO.java:
```java
package com.haifeng.app.dto.member;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawDTO {

    @NotNull(message = "提现金额不能为空")
    private BigDecimal amount;
}
```

ReferrerBindDTO.java:
```java
package com.haifeng.app.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReferrerBindDTO {

    @NotBlank(message = "邀请码不能为空")
    @Size(min = 8, max = 8, message = "邀请码长度必须为8位")
    private String inviteCode;
}
```

CommissionVO.java:
```java
package com.haifeng.app.vo.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionVO {

    private String inviteCode;

    private BigDecimal commissionBalance;

    private BigDecimal commissionTotalEarned;

    private BigDecimal commissionTotalPaid;

    private Integer referralCount;

    private String referrerInviteCode;
}
```

ReferrerPreviewVO.java:
```java
package com.haifeng.app.vo.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferrerPreviewVO {

    private String username;

    private String phone;
}
```

- [ ] **Step 2: 创建 CommissionService 接口**

```java
package com.haifeng.app.service.member;

import com.haifeng.app.dto.member.ReferrerBindDTO;
import com.haifeng.app.dto.member.WithdrawDTO;
import com.haifeng.app.vo.member.CommissionVO;
import com.haifeng.app.vo.member.ReferrerPreviewVO;

public interface CommissionService {

    CommissionVO getCommission();

    Long withdraw(WithdrawDTO dto);

    ReferrerPreviewVO previewReferrer(String inviteCode);

    void bindReferrer(ReferrerBindDTO dto);
}
```

- [ ] **Step 3: 创建 CommissionServiceImpl**

```java
package com.haifeng.app.service.impl.member;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.dto.member.ReferrerBindDTO;
import com.haifeng.app.dto.member.WithdrawDTO;
import com.haifeng.app.service.member.CommissionService;
import com.haifeng.app.vo.member.CommissionVO;
import com.haifeng.app.vo.member.ReferrerPreviewVO;
import com.haifeng.common.entity.user.Member;
import com.haifeng.common.entity.user.WithdrawRecord;
import com.haifeng.common.enums.WithdrawStatus;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.mapper.user.WithdrawRecordMapper;
import com.haifeng.common.util.DesensitizeUtil;
import com.haifeng.common.util.InviteCodeGenerator;
import com.haifeng.common.util.SecurityUtil;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommissionServiceImpl implements CommissionService {

    // 允许的提现金额
    private static final List<BigDecimal> ALLOWED_AMOUNTS = Arrays.asList(
            new BigDecimal("50.00"),
            new BigDecimal("100.00")
    );

    private final MemberMapper memberMapper;
    private final WithdrawRecordMapper withdrawRecordMapper;

    @Override
    public CommissionVO getCommission() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberMapper.selectById(memberId);

        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        // 查询邀请人数
        Long referralCount = memberMapper.selectCount(
                new LambdaQueryWrapper<Member>()
                        .eq(Member::getReferrerId, memberId)
                        .eq(Member::getDeleted, false));

        // 查询推荐人的邀请码
        String referrerInviteCode = null;
        if (member.getReferrerId() != null) {
            Member referrer = memberMapper.selectById(member.getReferrerId());
            if (referrer != null) {
                referrerInviteCode = referrer.getInviteCode();
            }
        }

        return CommissionVO.builder()
                .inviteCode(member.getInviteCode())
                .commissionBalance(member.getCommissionBalance())
                .commissionTotalEarned(member.getCommissionTotalEarned())
                .commissionTotalPaid(member.getCommissionTotalPaid())
                .referralCount(referralCount.intValue())
                .referrerInviteCode(referrerInviteCode)
                .build();
    }

    @Override
    @Transactional
    public Long withdraw(WithdrawDTO dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberMapper.selectById(memberId);

        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        // 校验提现金额
        BigDecimal amount = dto.getAmount();
        if (!ALLOWED_AMOUNTS.contains(amount)) {
            throw new BusinessException(400, "提现金额只能是50或100");
        }

        // 校验余额
        if (member.getCommissionBalance().compareTo(amount) < 0) {
            throw new BusinessException(400, "余额不足");
        }

        // 扣减余额
        member.setCommissionBalance(member.getCommissionBalance().subtract(amount));
        member.setUpdatedAt(OffsetDateTime.now());
        memberMapper.updateById(member);

        // 创建提现记录
        Long recordId = SnowflakeIdGenerator.nextId();
        WithdrawRecord record = WithdrawRecord.builder()
                .id(recordId)
                .memberId(memberId)
                .memberName(member.getUsername())
                .phone(member.getPhone())
                .wechatId(member.getWechatId())
                .wechatIdIndex(member.getWechatIdIndex())
                .amount(amount)
                .status(WithdrawStatus.PENDING)
                .deleted(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        withdrawRecordMapper.insert(record);

        log.info("提现申请成功: memberId={}, amount={}, recordId={}", memberId, amount, recordId);

        return recordId;
    }

    @Override
    public ReferrerPreviewVO previewReferrer(String inviteCode) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        // 解析邀请码
        long referrerId = InviteCodeGenerator.decode(inviteCode);
        if (referrerId == -1) {
            throw new BusinessException(400, "邀请码无效");
        }

        // 不能是自己的邀请码
        if (referrerId == memberId) {
            throw new BusinessException(400, "不能填写自己的邀请码");
        }

        // 查询推荐人
        Member referrer = memberMapper.selectById(referrerId);
        if (referrer == null || referrer.getDeleted()) {
            throw new BusinessException(400, "邀请码对应的用户不存在");
        }

        // 检查推荐人是否被禁用
        if (!referrer.isActive()) {
            throw new BusinessException(400, "邀请码对应的用户已被禁用");
        }

        // 检查循环引用
        if (referrer.getReferrerId() != null && referrer.getReferrerId().equals(memberId)) {
            throw new BusinessException(400, "不能互相绑定邀请码");
        }

        return ReferrerPreviewVO.builder()
                .username(referrer.getUsername())
                .phone(DesensitizeUtil.desensitizePhone(referrer.getPhone()))
                .build();
    }

    @Override
    @Transactional
    public void bindReferrer(ReferrerBindDTO dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberMapper.selectById(memberId);

        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        // 检查是否已绑定
        if (member.getReferrerId() != null) {
            throw new BusinessException(400, "已绑定邀请码，不可修改");
        }

        // 解析邀请码
        long referrerId = InviteCodeGenerator.decode(dto.getInviteCode());
        if (referrerId == -1) {
            throw new BusinessException(400, "邀请码无效");
        }

        // 不能是自己的邀请码
        if (referrerId == memberId) {
            throw new BusinessException(400, "不能填写自己的邀请码");
        }

        // 查询推荐人
        Member referrer = memberMapper.selectById(referrerId);
        if (referrer == null || referrer.getDeleted()) {
            throw new BusinessException(400, "邀请码对应的用户不存在");
        }

        // 检查推荐人是否被禁用
        if (!referrer.isActive()) {
            throw new BusinessException(400, "邀请码对应的用户已被禁用");
        }

        // 检查循环引用
        if (referrer.getReferrerId() != null && referrer.getReferrerId().equals(memberId)) {
            throw new BusinessException(400, "不能互相绑定邀请码");
        }

        // 绑定
        member.setReferrerId(referrerId);
        member.setReferrerUsername(referrer.getUsername());
        member.setUpdatedAt(OffsetDateTime.now());
        memberMapper.updateById(member);

        log.info("邀请码绑定成功: memberId={}, referrerId={}", memberId, referrerId);
    }
}
```

- [ ] **Step 4: 创建 CommissionController**

```java
package com.haifeng.app.controller.member;

import com.haifeng.app.dto.member.ReferrerBindDTO;
import com.haifeng.app.dto.member.WithdrawDTO;
import com.haifeng.app.service.member.CommissionService;
import com.haifeng.app.vo.member.CommissionVO;
import com.haifeng.app.vo.member.ReferrerPreviewVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/app/member")
@RequiredArgsConstructor
@RequireLogin
public class CommissionController {

    private final CommissionService commissionService;

    /**
     * 获取佣金信息
     */
    @GetMapping("/commission")
    public R<CommissionVO> getCommission() {
        return R.ok(commissionService.getCommission());
    }

    /**
     * 申请提现
     */
    @PostMapping("/withdraw")
    public R<Long> withdraw(@Valid @RequestBody WithdrawDTO dto) {
        return R.ok(commissionService.withdraw(dto));
    }

    /**
     * 预览邀请码对应用户
     */
    @GetMapping("/referrer/preview")
    public R<ReferrerPreviewVO> previewReferrer(@RequestParam String inviteCode) {
        return R.ok(commissionService.previewReferrer(inviteCode));
    }

    /**
     * 确认绑定邀请码
     */
    @PostMapping("/referrer/bind")
    public R<Void> bindReferrer(@Valid @RequestBody ReferrerBindDTO dto) {
        commissionService.bindReferrer(dto);
        return R.ok();
    }
}
```

- [ ] **Step 5: 提交**

```bash
git add haifeng-app/src/main/java/com/haifeng/app/dto/member/WithdrawDTO.java
git add haifeng-app/src/main/java/com/haifeng/app/dto/member/ReferrerBindDTO.java
git add haifeng-app/src/main/java/com/haifeng/app/vo/member/CommissionVO.java
git add haifeng-app/src/main/java/com/haifeng/app/vo/member/ReferrerPreviewVO.java
git add haifeng-app/src/main/java/com/haifeng/app/service/member/CommissionService.java
git add haifeng-app/src/main/java/com/haifeng/app/service/impl/member/CommissionServiceImpl.java
git add haifeng-app/src/main/java/com/haifeng/app/controller/member/CommissionController.java
git commit -m "feat(app): add commission and referrer binding endpoints"
```

---

## Task 9: 更新设计规格并最终提交

**Files:**
- Modify: `docs/superpowers/specs/2026-05-13-app-homepage-member-center-design.md`

- [ ] **Step 1: 提交设计规格更新**

```bash
git add docs/superpowers/specs/2026-05-13-app-homepage-member-center-design.md
git commit -m "docs: update design spec with login requirement for search APIs"
```

- [ ] **Step 2: 验证项目编译**

运行:
```bash
cd haifeng-app && mvn compile -q
```
预期: BUILD SUCCESS

- [ ] **Step 3: 完成最终提交**

```bash
git log --oneline -10
```

验证所有任务已完成。

---

## 任务检查清单

| 任务 | 说明 | 状态 |
|-----|------|------|
| Task 1 | Flyway 迁移文件 | - [ ] |
| Task 2 | 枚举类 | - [ ] |
| Task 3 | MemberProfile Entity + Mapper | - [ ] |
| Task 4 | 站点信息接口 | - [ ] |
| Task 5 | 模糊搜索接口 | - [ ] |
| Task 6 | 用户资料接口 | - [ ] |
| Task 7 | 用户信息接口 | - [ ] |
| Task 8 | 佣金与提现接口 | - [ ] |
| Task 9 | 验证与提交 | - [ ] |
