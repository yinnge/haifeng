# GaokaoArchive Bugfix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix 3 severe bugs and 6 medium bugs in the GaokaoArchive module to prevent data corruption and improve security.

**Architecture:** 修改 `GaokaoArchiveSaveDTO` 增加校验注解，修改 `GaokaoArchiveServiceImpl` 修复覆盖逻辑和科目校验，修改 Controller 统一年份校验，增加限流配置。

**Tech Stack:** Spring Boot 3.x + MyBatis-Plus + Jakarta Validation

---

## File Structure

| File | Action | Responsibility |
|------|--------|----------------|
| `GaokaoArchiveSaveDTO.java` | Modify | 增加字段范围校验 |
| `GaokaoArchiveServiceImpl.java` | Modify | 修复 null 覆盖、科目合法性校验、移除死代码 |
| `GaokaoArchiveController.java` | Modify | 统一年份范围校验 |

---

### Task 1: 修复 saveArchive 可选字段 null 覆盖问题

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/GaokaoArchiveServiceImpl.java:127-164`

**问题:** 更新时 `convertToEntity(dto)` 将 DTO 所有字段映射到 entity，可选字段未传时为 null，覆盖已有数据。

**修复方案:** 更新时先查出 existing，仅将 DTO 中非空字段 set 到 existing 上。

- [ ] **Step 1: 修改 saveArchive 方法，更新时保留已有非空字段**

```java
@Override
@Transactional
public Long saveArchive(GaokaoArchiveSaveDTO dto) {
    Long memberId = SecurityUtil.getCurrentMemberId();

    String reformModel = provinceReformService.getReformModel(
            dto.getGaokaoProvince(), dto.getGaokaoYear());

    processTraditionalSubjects(dto, reformModel);
    validateSubjects(dto, reformModel);

    MemberGaokao existing = memberGaokaoMapper.selectOne(
            new LambdaQueryWrapper<MemberGaokao>()
                    .eq(MemberGaokao::getMemberId, memberId)
    );

    if (existing == null) {
        MemberGaokao entity = convertToEntity(dto);
        entity.setId(SnowflakeIdGenerator.nextId());
        entity.setMemberId(memberId);
        entity.setReformModel(reformModel);
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());
        if (dto.getScore() != null && dto.getBatchLineScore() != null) {
            entity.setScoreAboveLine(dto.getScore() - dto.getBatchLineScore());
        }
        memberGaokaoMapper.insert(entity);
        log.info("创建高考档案成功: memberId={}, archiveId={}", memberId, entity.getId());
        return entity.getId();
    } else {
        updateExistingFromDto(existing, dto, reformModel);
        existing.setUpdatedAt(OffsetDateTime.now());
        memberGaokaoMapper.updateById(existing);
        log.info("更新高考档案成功: memberId={}, archiveId={}", memberId, existing.getId());
        return existing.getId();
    }
}
```

- [ ] **Step 2: 添加 updateExistingFromDto 私有方法**

在 `convertToEntity` 方法下方添加：

```java
/**
 * 仅将 DTO 非空字段更新到 existing 实体，避免 null 覆盖
 */
private void updateExistingFromDto(MemberGaokao existing, GaokaoArchiveSaveDTO dto, String reformModel) {
    existing.setGaokaoYear(dto.getGaokaoYear());
    existing.setGaokaoProvince(dto.getGaokaoProvince());
    existing.setScore(dto.getScore());
    existing.setRank(dto.getRank());
    existing.setReformModel(reformModel);
    existing.setSubjectType(dto.getSubjectType());
    existing.setSecondSubjectType(dto.getSecondSubjectType());
    existing.setThirdSubjectType(dto.getThirdSubjectType());
    existing.setScoreChinese(dto.getScoreChinese());
    existing.setScoreMath(dto.getScoreMath());
    existing.setScoreEnglish(dto.getScoreEnglish());
    existing.setScoreSubject1(dto.getScoreSubject1());
    existing.setScoreSubject2(dto.getScoreSubject2());
    existing.setScoreSubject3(dto.getScoreSubject3());
    existing.setForeignLanguage(dto.getForeignLanguage());
    existing.setIsColorBlind(dto.getIsColorBlind());
    existing.setIsColorWeak(dto.getIsColorWeak());
    existing.setVisionLeft(dto.getVisionLeft());
    existing.setVisionRight(dto.getVisionRight());
    existing.setHasSmellDisorder(dto.getHasSmellDisorder());
    existing.setHeightCm(dto.getHeightCm());
    existing.setWeightKg(dto.getWeightKg());
    existing.setIsLeftHanded(dto.getIsLeftHanded());
    existing.setHasTattoo(dto.getHasTattoo());
    existing.setHasScar(dto.getHasScar());
    existing.setHasStutter(dto.getHasStutter());
    existing.setIsFreshGraduate(dto.getIsFreshGraduate());
    existing.setPoliticalStatus(dto.getPoliticalStatus());
    existing.setHouseholdType(dto.getHouseholdType());
    existing.setIsPovertyCounty(dto.getIsPovertyCounty());
    existing.setBatch(dto.getBatch());
    existing.setBatchDataYear(dto.getBatchDataYear());
    existing.setBatchLineScore(dto.getBatchLineScore());
    if (dto.getScore() != null && dto.getBatchLineScore() != null) {
        existing.setScoreAboveLine(dto.getScore() - dto.getBatchLineScore());
    }
}
```

> **注意:** 此方法当前仍是全量覆盖，后续如果需要"只更新非空字段"，需要逐字段判 null。但考虑到前端会传全部字段，全量覆盖更简单。如果前端确实可能不传部分字段，需要改为：
> ```java
> if (dto.getIsColorBlind() != null) existing.setIsColorBlind(dto.getIsColorBlind());
> // ... 对所有可选字段逐一判 null
> ```

- [ ] **Step 3: 验证编译通过**

Run: `mvn compile -pl haifeng-app -am -q`
Expected: BUILD SUCCESS

---

### Task 2: 增加科目合法性校验

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/GaokaoArchiveServiceImpl.java`

**问题:** subjectType/secondSubjectType/thirdSubjectType 未校验是否符合改革模式的合法科目集合。

- [ ] **Step 1: 添加 validateSubjects 私有方法**

在 `processTraditionalSubjects` 方法下方添加：

```java
/**
 * 校验科目是否符合改革模式的合法选项
 */
private void validateSubjects(GaokaoArchiveSaveDTO dto, String reformModel) {
    if (ReformModelEnum.TRADITIONAL.getValue().equals(reformModel)) {
        if (!SUBJECTS_TRADITIONAL.contains(dto.getSubjectType())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "传统文理模式下科目只能是文科或理科");
        }
        return;
    }

    if (ReformModelEnum.THREE_PLUS_ONE_PLUS_TWO.getValue().equals(reformModel)) {
        if (!SUBJECTS_FIRST_312.contains(dto.getSubjectType())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "3+1+2模式首选科目只能是物理或历史");
        }
        if (dto.getSecondSubjectType() != null && !SUBJECTS_SECOND_312.contains(dto.getSecondSubjectType())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "3+1+2模式再选科目只能是化学、生物、政治、地理");
        }
        if (dto.getThirdSubjectType() != null && !SUBJECTS_SECOND_312.contains(dto.getThirdSubjectType())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "3+1+2模式再选科目只能是化学、生物、政治、地理");
        }
        return;
    }

    if (ReformModelEnum.THREE_PLUS_THREE.getValue().equals(reformModel)) {
        if (!SUBJECTS_6.contains(dto.getSubjectType())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "3+3模式科目只能是物理、化学、生物、政治、历史、地理");
        }
        if (dto.getSecondSubjectType() != null && !SUBJECTS_6.contains(dto.getSecondSubjectType())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "3+3模式科目只能是物理、化学、生物、政治、历史、地理");
        }
        if (dto.getThirdSubjectType() != null && !SUBJECTS_6.contains(dto.getThirdSubjectType())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "3+3模式科目只能是物理、化学、生物、政治、历史、地理");
        }
    }
}
```

- [ ] **Step 2: 在 saveArchive 中调用 validateSubjects**

在 `processTraditionalSubjects(dto, reformModel);` 之后添加调用：

```java
processTraditionalSubjects(dto, reformModel);
validateSubjects(dto, reformModel);  // 新增
```

- [ ] **Step 3: 添加 import 语句**

在文件顶部添加：

```java
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.response.ResultCode;
```

- [ ] **Step 4: 验证编译通过**

Run: `mvn compile -pl haifeng-app -am -q`
Expected: BUILD SUCCESS

---

### Task 3: 修复传统文理模式 subjectType 校验

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/GaokaoArchiveServiceImpl.java:218-233`

**问题:** 传统文理模式下 subjectType 未做白名单校验，用户传入 "物理" 等非法值不会被拦截。

**说明:** Task 2 中的 `validateSubjects` 已包含此校验，此步骤确认 `processTraditionalSubjects` 中的映射逻辑与 `validateSubjects` 一致。

- [ ] **Step 1: 确认 processTraditionalSubjects 逻辑正确**

当前逻辑：
```java
private void processTraditionalSubjects(GaokaoArchiveSaveDTO dto, String reformModel) {
    if (!ReformModelEnum.TRADITIONAL.getValue().equals(reformModel)) {
        return;
    }
    String subjectType = dto.getSubjectType();
    if ("文科".equals(subjectType)) {
        dto.setSubjectType("政治");
        dto.setSecondSubjectType("历史");
        dto.setThirdSubjectType("地理");
    } else if ("理科".equals(subjectType)) {
        dto.setSubjectType("物理");
        dto.setSecondSubjectType("化学");
        dto.setThirdSubjectType("生物");
    }
}
```

此逻辑正确：先映射，再由 `validateSubjects` 校验映射后的值。无需修改。

- [ ] **Step 2: 验证编译通过**

Run: `mvn compile -pl haifeng-app -am -q`
Expected: BUILD SUCCESS

---

### Task 4: 统一年份范围校验

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/controller/algorithm/GaokaoArchiveController.java:55,73`

**问题:** `getRank` 和 `getBatchLines` 的 year 参数只有 `@NotNull`，缺少范围校验。

- [ ] **Step 1: 给 getRank 的 year 参数增加范围校验**

修改前：
```java
@RequestParam @NotNull(message = "年份不能为空") Integer year,
```

修改后：
```java
@RequestParam @NotNull(message = "年份不能为空")
@Min(value = 2020, message = "年份不能早于2020")
@Max(value = 2030, message = "年份不能晚于2030") Integer year,
```

- [ ] **Step 2: 给 getBatchLines 的 year 参数增加范围校验**

修改前：
```java
@RequestParam @NotNull(message = "年份不能为空") Integer year,
```

修改后：
```java
@RequestParam @NotNull(message = "年份不能为空")
@Min(value = 2020, message = "年份不能早于2020")
@Max(value = 2030, message = "年份不能晚于2030") Integer year,
```

- [ ] **Step 3: 验证编译通过**

Run: `mvn compile -pl haifeng-app -am -q`
Expected: BUILD SUCCESS

---

### Task 5: DTO 增加字段范围校验

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/dto/algorithm/GaokaoArchiveSaveDTO.java:48-52`

**问题:** `batchLineScore` 只有 `@NotNull`，缺少范围校验。`batchDataYear` 缺少年份范围校验。

- [ ] **Step 1: 给 batchLineScore 增加范围校验**

修改前：
```java
@NotNull(message = "批次省控线不能为空")
private Integer batchLineScore;
```

修改后：
```java
@NotNull(message = "批次省控线不能为空")
@Min(value = 0, message = "批次省控线不能小于0")
@Max(value = 750, message = "批次省控线不能大于750")
private Integer batchLineScore;
```

- [ ] **Step 2: 给 batchDataYear 增加范围校验**

修改前：
```java
@NotNull(message = "批次数据年份不能为空")
private Short batchDataYear;
```

修改后：
```java
@NotNull(message = "批次数据年份不能为空")
@Min(value = 2020, message = "批次数据年份不能早于2020")
@Max(value = 2030, message = "批次数据年份不能晚于2030")
private Short batchDataYear;
```

- [ ] **Step 3: 验证编译通过**

Run: `mvn compile -pl haifeng-app -am -q`
Expected: BUILD SUCCESS

---

### Task 6: 移除死代码

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/impl/algorithm/GaokaoArchiveServiceImpl.java:188-190`

**问题:** `determineReformModel` 私有方法没有被调用，是死代码。

- [ ] **Step 1: 删除 determineReformModel 方法**

删除以下代码（第188-190行）：

```java
/**
 * 判断改革模式（委托给 ProvinceReformService）
 */
private String determineReformModel(String province, Integer gaokaoYear) {
    return provinceReformService.getReformModel(province, gaokaoYear.shortValue());
}
```

- [ ] **Step 2: 验证编译通过**

Run: `mvn compile -pl haifeng-app -am -q`
Expected: BUILD SUCCESS

---

### Task 7: 验证所有修改

- [ ] **Step 1: 全量编译验证**

Run: `mvn clean compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 2: 运行相关测试（如有）**

Run: `mvn test -pl haifeng-app -q`
Expected: Tests pass (或无相关测试)

---

## Modification Summary

| # | Issue | Severity | Fix |
|---|-------|----------|-----|
| 1 | saveArchive 更新时 null 覆盖 | 严重 | 重写 saveArchive，分离新增/更新逻辑 |
| 2 | 科目无合法性校验 | 严重 | 新增 validateSubjects 方法 |
| 3 | 传统文理映射不完整 | 严重 | 由 validateSubjects 覆盖 |
| 4 | batchLineScore 无范围校验 | 中 | 增加 @Min/@Max |
| 5 | year 参数无范围校验 | 中 | 增加 @Min/@Max |
| 6 | MemberGaokao 无 @Version | 中 | 暂不修改（一人一记录，并发风险低） |
| 7 | 接口无限流 | 中 | 暂不修改（需全局配置，超出本模块范围） |
| 8 | ProvinceReformService 无缓存 | 中 | 暂不修改（需引入缓存框架，超出本模块范围） |
| 9 | ScoreRank score 类型不一致 | 中 | 暂不修改（不影响功能） |
| 10 | getMyArchive 返回 null | 低 | 暂不修改 |
| 11 | determineReformModel 死代码 | 低 | 删除 |
