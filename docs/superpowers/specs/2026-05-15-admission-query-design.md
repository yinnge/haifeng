# App 端专业组查询接口设计

> 日期：2026-05-15
> 模块：haifeng-app / algorithm / admission
> 状态：待实施

## 1. 概述

### 1.1 功能目标

为用户提供专业组和专业明细的分页查询功能，以及院校、城市、专业的详情查询接口。

### 1.2 核心需求

1. **专业组分页查询**：根据用户档案的 batch 查询，返回专业组列表 + 近5年历史数据
2. **专业明细分页查询**：根据 groupId 查询专业明细 + 近5年历史数据
3. **三个详情接口**：根据名字查城市/专业/院校详情
4. **权限限制**：普通用户查20条，前10条正常显示，后10条模糊化
5. **选科筛选**：可选的 SQL 层面筛选 + Java 层面标记

## 2. 接口设计

### 2.1 专业组分页查询

```
GET /api/v1/app/admission/group/page

Query Params:
- batch: String (必填) - 本科批/专科批/提前批
- subjectFilter: Boolean (可选，默认false) - 是否启用选科筛选
- page: Integer (默认1)
- size: Integer (固定20)

Response: IPage<AdmissionGroupPageVO>
```

### 2.2 专业明细分页查询

```
GET /api/v1/app/admission/major/page

Query Params:
- groupId: Integer (必填)
- page: Integer (默认1)
- size: Integer (默认20)

Response: IPage<AdmissionMajorPageVO>
```

### 2.3 三个详情接口

```
GET /api/v1/app/university/brief?name=xxx
GET /api/v1/app/major/brief?name=xxx
GET /api/v1/app/city/brief?name=xxx
```

所有接口需要 `@RequireLogin` 注解。

## 3. 数据流与 SQL 策略

### 3.1 专业组查询流程（2条SQL）

```
┌─────────────────────────────────────────────────────────────┐
│ 1. 获取用户档案（从SecurityUtil拿memberId，查MemberGaokao）    │
│    → 拿到: province, batch, subjectType, secondSubjectType,  │
│           thirdSubjectType                                   │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│ SQL1: 分页查专业组（20条）                                    │
│                                                             │
│ SELECT * FROM t_admission_group                             │
│ WHERE province = #{province}                                │
│   AND batch = #{batch}                                      │
│   AND is_deleted = FALSE                                    │
│   [AND 选科筛选条件 -- 如果 subjectFilter=true]              │
│ ORDER BY min_rank ASC NULLS LAST                           │
│ LIMIT 20 OFFSET #{offset}                                   │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│ SQL2: IN批量查历史数据                                       │
│                                                             │
│ SELECT * FROM t_admission_group                             │
│ WHERE (university_id, group_code) IN (                      │
│     (1001, '01'), (1001, '02'), (1002, '01')...            │
│ )                                                           │
│   AND year >= #{currentYear - 4}                            │
│   AND is_deleted = FALSE                                    │
│ ORDER BY university_id, group_code, year DESC               │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│ Java内存拼接:                                                │
│ - 按 university_id + group_code 分组                        │
│ - 每组取最近5年数据                                          │
│ - 判断选科是否符合，设置 subjectMatch 字段                    │
│ - 判断会员类型，后10条设置 masked=true                        │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 选科筛选 SQL（PostgreSQL 数组运算符）

```sql
AND (
  -- 不限：永远通过
  requirement_type = '不限'
  OR subjects = '{}'
  OR subjects IS NULL

  -- 2选1/3选1：有交集即可（&& 运算符）
  OR (requirement_type IN ('2选1', '3选1')
      AND subjects && #{userSubjects}::text[])

  -- 必选1/2/3：用户选科必须包含专业组要求（@> 运算符）
  OR (requirement_type IN ('必选1', '必选2', '必选3')
      AND #{userSubjects}::text[] @> subjects)
)
```

### 3.3 专业明细查询流程（2条SQL）

```
SQL1: 分页查当前组的专业明细
SQL2: IN批量查历史数据（按 major_code 关联其他年份的同专业）
```

## 4. VO 结构

### 4.1 专业组分页 VO

```java
@Data
@Builder
public class AdmissionGroupPageVO {
    private Integer id;
    private Boolean masked;              // true=模糊化数据，只有id有效

    // === 以下字段 masked=true 时为 null ===
    private String universityName;
    private String cityName;
    private String enrollmentCode;
    private String groupCode;
    private String groupName;
    private List<String> subjects;       // 选科要求
    private String requirementType;      // 不限/2选1/3选1/必选1/必选2/必选3
    private String description;
    private Integer majorCount;
    private Integer categoryCount;
    private List<String> constraints;    // 约束条件

    // === 选科匹配标记 ===
    private Boolean subjectMatch;        // true=符合选科要求
    private String subjectMatchReason;   // 不符合时的原因

    // === 近5年历史数据 ===
    private List<YearScoreVO> historyScores;
}
```

### 4.2 年份分数 VO

```java
@Data
@Builder
public class YearScoreVO {
    private Short year;
    private Integer minScore;
    private Integer minRank;
    private BigDecimal avgScore;
    private Integer avgRank;
    private Integer maxScore;
    private Integer maxRank;
    private Integer admissionCount;
}
```

### 4.3 专业明细分页 VO

```java
@Data
@Builder
public class AdmissionMajorPageVO {
    private Integer id;
    private String majorCode;
    private String majorName;
    private String educationLevel;
    private String duration;
    private String tuition;
    private String description;
    private List<String> constraints;

    // === 近5年历史数据 ===
    private List<YearScoreVO> historyScores;
}
```

### 4.4 详情 VO

```java
// 院校简要信息
@Data
@Builder
public class UniversityBriefVO {
    private String name;
    private String provinceName;
    private String cityName;
    private String region;
    private String category;
    private String educationLevel;
    private String nature;
    private BigDecimal recommendationRate;
    private String department;
    private List<String> tags;
    private String imageUrl;
}

// 专业简要信息
@Data
@Builder
public class MajorBriefVO {
    private String majorCode;
    private String majorName;
    private String disciplineName;
    private String majorType;
    private String majorCategory;
    private String parentCategory;
    private String majorTags;
    private String degreeAwarded;
    private String description;
}

// 城市简要信息
@Data
@Builder
public class CityBriefVO {
    private String cityName;
    private String province;
    private String region;
    private String cityIntro;
    private Integer collegeCount;
}
```

## 5. 权限与模糊化逻辑

### 5.1 会员类型

| 类型 | 值 | 权限 |
|------|-----|------|
| 普通用户 | normal | 前10条正常，后10条模糊 |
| Pro会员 | pro | 全部正常 |
| VIP会员 | vip | 全部正常 |

### 5.2 模糊化处理

```java
// 普通用户：前10条正常，后10条模糊
boolean shouldMask = !isPremium && i >= 10;

if (shouldMask) {
    // 只返回 id + masked=true
    voList.add(AdmissionGroupPageVO.builder()
            .id(group.getId())
            .masked(true)
            .build());
}
```

## 6. 选科匹配服务

### 6.1 位置

`haifeng-common/src/main/java/com/haifeng/common/service/algorithm/matcher/SubjectMatcher.java`

### 6.2 实现

```java
@Component
public class SubjectMatcher {

    public SubjectMatchResult match(MemberGaokao gaokao, AdmissionGroup group) {
        List<String> userSubjects = Arrays.asList(
            gaokao.getSubjectType(),
            gaokao.getSecondSubjectType(),
            gaokao.getThirdSubjectType()
        );

        List<String> groupSubjects = group.getSubjects();
        String reqType = group.getRequirementType();

        // 不限 → 永远符合
        if ("不限".equals(reqType) || groupSubjects == null || groupSubjects.isEmpty()) {
            return SubjectMatchResult.ok();
        }

        // 计算交集数量
        long matchCount = groupSubjects.stream()
                .filter(userSubjects::contains)
                .count();

        switch (reqType) {
            case "2选1":
            case "3选1":
                if (matchCount >= 1) return SubjectMatchResult.ok();
                return SubjectMatchResult.fail("需从 " + String.join("/", groupSubjects) + " 中选考至少1门");

            case "必选1":
                if (matchCount >= 1) return SubjectMatchResult.ok();
                return SubjectMatchResult.fail("必须选考 " + groupSubjects.get(0));

            case "必选2":
                if (matchCount >= 2) return SubjectMatchResult.ok();
                return SubjectMatchResult.fail("必须同时选考 " + String.join(" 和 ", groupSubjects));

            case "必选3":
                if (matchCount >= 3) return SubjectMatchResult.ok();
                return SubjectMatchResult.fail("必须同时选考 " + String.join("、", groupSubjects));

            default:
                return SubjectMatchResult.ok();
        }
    }
}
```

## 7. 包结构

### 7.1 haifeng-common（新增）

```
haifeng-common/
└── service/algorithm/matcher/
    ├── SubjectMatcher.java
    └── SubjectMatchResult.java
```

### 7.2 haifeng-app（新增）

```
haifeng-app/
├── controller/algorithm/admission/
│   └── AdmissionQueryController.java
├── controller/university/
│   └── UniversityBriefController.java
├── controller/major/
│   └── MajorBriefController.java
├── controller/city/
│   └── CityBriefController.java
├── service/algorithm/admission/
│   └── AdmissionQueryService.java
├── service/impl/algorithm/admission/
│   └── AdmissionQueryServiceImpl.java
├── dto/algorithm/admission/
│   ├── AdmissionGroupQueryDTO.java
│   └── AdmissionMajorQueryDTO.java
└── vo/algorithm/admission/
    ├── AdmissionGroupPageVO.java
    ├── AdmissionMajorPageVO.java
    └── YearScoreVO.java
```

### 7.3 haifeng-common/mapper（新增方法）

```java
// AdmissionGroupMapper
List<AdmissionGroup> selectPageByCondition(...);
List<AdmissionGroup> selectHistoryByKeys(...);

// AdmissionMajorScoreMapper
List<AdmissionMajorScore> selectHistoryByMajorCodes(...);
```

## 8. 验证方式

1. 启动 app 服务
2. 使用已有用户登录，确保有高考档案
3. 调用 `GET /api/v1/app/admission/group/page?batch=本科批` 验证：
   - 返回20条数据
   - 普通用户后10条 masked=true
   - 每条有 historyScores 近5年数据
   - 有 subjectMatch 和 subjectMatchReason 字段
4. 调用 `GET /api/v1/app/admission/major/page?groupId=xxx` 验证专业明细
5. 调用三个详情接口验证数据返回
