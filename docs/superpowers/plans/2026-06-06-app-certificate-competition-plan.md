# 竞赛证书管理 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 C 端 App 提供"竞赛证书管理"父模块下的 6 个查询接口：证书列表/详情（任务1）、竞赛列表/详情/关联专业（任务2）、专业→关联竞赛（任务3 扩展 MajorController）。

**Architecture:** 沿用现有 `major` / `industry` 模块的「Controller → Service → ServiceImpl → Mapper」分层。两个新独立 Controller（CertificateController、CompetitionController）+ 在 MajorController 增加 1 个方法。JSONB 字段由实体的 `JacksonTypeHandler` 直接反序列化到 VO，Service 层不做额外 JSON 解析。`CompetitionMajorMapper` 新增 2 个分页联表方法，`CompetitionDetailMapper` 新增 1 个带软删除过滤的方法。

**Tech Stack:** Spring Boot 3.x、Java 17、MyBatis Plus 3.5.x、MySQL/PostgreSQL、JUnit 5 + Mockito、Project Lombok。

**用户约束:**
- 不要使用 `git` 命令提交，最后修改由用户统一提交。计划中所有 `git commit` 步骤改为「标记本任务完成，等待最终统一提交」。
- 项目根目录：`D:\0code\haifeng\backend\haifeng`
- `haifeng-app` 模块工作目录：`D:\0code\haifeng\backend\haifeng\haifeng-app`
- `haifeng-common` 模块工作目录：`D:\0code\haifeng\backend\haifeng\haifeng-common`

---

## 文件结构总览

### haifeng-common（修改 1 个 + 新增 1 个方法）
- 修改：`src/main/java/com/haifeng/common/mapper/certificate/CompetitionMajorMapper.java` —— 新增 `selectMajorsByCompetitionId`、`selectCompetitionsByMajorId`
- 修改：`src/main/java/com/haifeng/common/mapper/certificate/CompetitionDetailMapper.java` —— 新增 `findActiveByCompetitionId`

### haifeng-app（新增）
```
src/main/java/com/haifeng/app/
├── controller/
│   ├── certificate/CertificateController.java
│   └── competition/CompetitionController.java
├── service/
│   ├── certificate/CertificateService.java
│   └── competition/CompetitionService.java
├── service/impl/
│   ├── certificate/CertificateServiceImpl.java
│   └── competition/CompetitionServiceImpl.java
├── dto/
│   └── certificate/CertificateListQueryDTO.java
└── vo/
    ├── certificate/CertificateListVO.java
    ├── certificate/CertificateDetailVO.java
    ├── competition/CompetitionListVO.java
    ├── competition/CompetitionDetailVO.java
    ├── competition/CompetitionMajorBriefVO.java
    └── major/CompetitionBriefVO.java
```

### haifeng-app（修改 3 个）
- `controller/major/MajorController.java` —— 新增 1 个方法
- `service/major/MajorService.java` —— 新增 1 个接口方法
- `service/impl/major/MajorServiceImpl.java` —— 注入 `CompetitionMajorMapper`，新增 1 个方法

### haifeng-app（新增测试）
```
src/test/java/com/haifeng/app/service/
├── certificate/CertificateServiceImplTest.java
└── competition/CompetitionServiceImplTest.java
```

修改测试：
- `src/test/java/com/haifeng/app/service/major/MajorServiceImplTest.java` —— 追加 `competitions_*` 用例

---

## 任务依赖图

```
T1  CompetitionDetailMapper.findActiveByCompetitionId
  ↓
T2  CompetitionMajorMapper.selectMajorsByCompetitionId
T3  CompetitionMajorMapper.selectCompetitionsByMajorId
  ↓
T4  Certificate VO + DTO
T5  Competition VO
T6  MajorCompetitionBriefVO
  ↓
T7  CertificateService + Impl + Test
T8  CompetitionService + Impl + Test
T9  MajorService 扩展 + Test
  ↓
T10 CertificateController
T11 CompetitionController
T12 MajorController 扩展
  ↓
T13 编译 + 全量测试
```

---

## Task 1: 新增 CompetitionDetailMapper.findActiveByCompetitionId

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/mapper/certificate/CompetitionDetailMapper.java`

- [ ] **Step 1: 添加新方法**

打开 `CompetitionDetailMapper.java`，在 `findByCompetitionId` 方法之后新增：

```java
/**
 * 根据 competitionId 查询未软删除的竞赛详情
 * Service 层任务2接口2 专用
 */
@Select("SELECT * FROM t_competition_detail " +
        "WHERE competition_id = #{competitionId} AND is_deleted = FALSE")
CompetitionDetail findActiveByCompetitionId(@Param("competitionId") Long competitionId);
```

- [ ] **Step 2: 编译 common 模块验证**

```bash
mvn -pl haifeng-common -am compile -q
```

预期：`BUILD SUCCESS`，无编译错误。

- [ ] **Step 3: 标记本任务完成（不提交）**

任务完成。等待最终统一提交。

---

## Task 2: 新增 CompetitionMajorMapper.selectMajorsByCompetitionId

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/mapper/certificate/CompetitionMajorMapper.java`

- [ ] **Step 1: 添加新方法**

打开 `CompetitionMajorMapper.java`，新增以下 imports（在已有 `org.apache.ibatis.annotations.*` 旁加 `Page`、`IPage`）：

```java
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
```

在类内部新增：

```java
/**
 * 任务2接口3：分页查询某竞赛关联的专业（id + name）
 * 走 idx_cm_competition 索引
 */
@Select("SELECT cm.major_id AS majorId, cm.major_name AS majorName " +
        "FROM t_competition_major cm " +
        "WHERE cm.competition_id = #{competitionId} " +
        "ORDER BY cm.id ASC")
IPage<Map<String, Object>> selectMajorsByCompetitionId(
        Page<?> page,
        @Param("competitionId") Long competitionId);
```

- [ ] **Step 2: 验证编译**

```bash
mvn -pl haifeng-common -am compile -q
```

预期：`BUILD SUCCESS`。

- [ ] **Step 3: 标记本任务完成**

任务完成。等待最终统一提交。

---

## Task 3: 新增 CompetitionMajorMapper.selectCompetitionsByMajorId

**Files:**
- Modify: `haifeng-common/src/main/java/com/haifeng/common/mapper/certificate/CompetitionMajorMapper.java`

- [ ] **Step 1: 添加新方法**

紧接 Task 2 的 `selectMajorsByCompetitionId` 之后新增：

```java
/**
 * 任务3：分页查询某专业关联的竞赛（id + name）
 * 走 idx_cm_major 索引
 */
@Select("SELECT cm.competition_id AS competitionId, cm.competition_name AS competitionName " +
        "FROM t_competition_major cm " +
        "WHERE cm.major_id = #{majorId} " +
        "ORDER BY cm.id ASC")
IPage<Map<String, Object>> selectCompetitionsByMajorId(
        Page<?> page,
        @Param("majorId") Long majorId);
```

- [ ] **Step 2: 验证编译**

```bash
mvn -pl haifeng-common -am compile -q
```

预期：`BUILD SUCCESS`。

- [ ] **Step 3: 标记本任务完成**

任务完成。等待最终统一提交。

---

## Task 4: 创建 Certificate 模块的 VO 与 DTO

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/certificate/CertificateListVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/certificate/CertificateDetailVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/dto/certificate/CertificateListQueryDTO.java`

- [ ] **Step 1: 创建 CertificateListVO**

路径：`haifeng-app/src/main/java/com/haifeng/app/vo/certificate/CertificateListVO.java`

```java
package com.haifeng.app.vo.certificate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端证书列表 VO（spec 任务1接口1） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String certName;
    private String category;
    private String certLevel;
    private String applicableMajor;
    private String registrationTime;
    private String examTime;
    private Integer examFee;
    private String certIntro;
}
```

- [ ] **Step 2: 创建 CertificateDetailVO**

路径：`haifeng-app/src/main/java/com/haifeng/app/vo/certificate/CertificateDetailVO.java`

```java
package com.haifeng.app.vo.certificate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/** C 端证书详情 VO（spec 任务1接口2） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String certName;
    private String category;
    private String certLevel;
    private String applicableMajor;
    private String registrationTime;
    private String examTime;
    private Integer examFee;
    private String certIntro;
    private List<String> examRequirements;
    private String examArrangement;
    private String officialWebsite;
}
```

- [ ] **Step 3: 创建 CertificateListQueryDTO**

路径：`haifeng-app/src/main/java/com/haifeng/app/dto/certificate/CertificateListQueryDTO.java`

```java
package com.haifeng.app.dto.certificate;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** C 端证书列表查询 DTO（spec 任务1接口1） */
@Data
@EqualsAndHashCode(callSuper = true)
public class CertificateListQueryDTO extends BasePageQueryDTO {

    /** 精准查询 */
    private String category;
}
```

- [ ] **Step 4: 验证编译**

```bash
mvn -pl haifeng-app -am compile -q
```

预期：`BUILD SUCCESS`。

- [ ] **Step 5: 标记本任务完成**

任务完成。等待最终统一提交。

---

## Task 5: 创建 Competition 模块的 VO

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/competition/CompetitionListVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/competition/CompetitionDetailVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/competition/CompetitionMajorBriefVO.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/vo/major/CompetitionBriefVO.java`

- [ ] **Step 1: 创建 CompetitionListVO**

路径：`haifeng-app/src/main/java/com/haifeng/app/vo/competition/CompetitionListVO.java`

```java
package com.haifeng.app.vo.competition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端竞赛列表 VO（spec 任务2接口1） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String compName;
    private String compLevel;
    private String registrationTime;
}
```

- [ ] **Step 2: 创建 CompetitionDetailVO**

路径：`haifeng-app/src/main/java/com/haifeng/app/vo/competition/CompetitionDetailVO.java`

```java
package com.haifeng.app.vo.competition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/** C 端竞赛详情 VO（spec 任务2接口2） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long competitionId;

    private Map<String, Object> basicInfo;
    private List<String> awards;
    private String background;
    private List<String> purposes;
    private List<Map<String, String>> competitionRules;
    private List<String> scoringCriteria;
    private List<String> notices;
    private List<Map<String, String>> processGuide;
    private List<Map<String, String>> awardsDisplay;
}
```

- [ ] **Step 3: 创建 CompetitionMajorBriefVO**

路径：`haifeng-app/src/main/java/com/haifeng/app/vo/competition/CompetitionMajorBriefVO.java`

```java
package com.haifeng.app.vo.competition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端竞赛→专业简明 VO（spec 任务2接口3） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionMajorBriefVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long majorId;
    private String majorName;
}
```

- [ ] **Step 4: 创建 CompetitionBriefVO**

路径：`haifeng-app/src/main/java/com/haifeng/app/vo/major/CompetitionBriefVO.java`

```java
package com.haifeng.app.vo.major;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端专业→竞赛简明 VO（spec 任务3） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionBriefVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long competitionId;
    private String competitionName;
}
```

- [ ] **Step 5: 验证编译**

```bash
mvn -pl haifeng-app -am compile -q
```

预期：`BUILD SUCCESS`。

- [ ] **Step 6: 标记本任务完成**

任务完成。等待最终统一提交。

---

## Task 6: 编写 CertificateService 与测试（TDD 起点）

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/certificate/CertificateService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/certificate/CertificateServiceImpl.java`
- Create: `haifeng-app/src/test/java/com/haifeng/app/service/certificate/CertificateServiceImplTest.java`

- [ ] **Step 1: 编写 Service 接口（先于实现）**

路径：`haifeng-app/src/main/java/com/haifeng/app/service/certificate/CertificateService.java`

```java
package com.haifeng.app.service.certificate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.certificate.CertificateListQueryDTO;
import com.haifeng.app.vo.certificate.CertificateDetailVO;
import com.haifeng.app.vo.certificate.CertificateListVO;

public interface CertificateService {

    /** 任务1接口1：证书分页列表（公开，支持 category 精准过滤） */
    IPage<CertificateListVO> page(CertificateListQueryDTO dto);

    /** 任务1接口2：证书详情（登录） */
    CertificateDetailVO detail(Long certId);
}
```

- [ ] **Step 2: 编写测试类（红）**

路径：`haifeng-app/src/test/java/com/haifeng/app/service/certificate/CertificateServiceImplTest.java`

```java
package com.haifeng.app.service.certificate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.certificate.CertificateListQueryDTO;
import com.haifeng.app.service.impl.certificate.CertificateServiceImpl;
import com.haifeng.app.vo.certificate.CertificateDetailVO;
import com.haifeng.app.vo.certificate.CertificateListVO;
import com.haifeng.common.entity.certificate.Certificate;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.certificate.CertificateMapper;
import com.haifeng.common.response.ResultCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificateServiceImplTest {

    @Mock
    private CertificateMapper certificateMapper;

    @InjectMocks
    private CertificateServiceImpl service;

    @Test
    void page_WithCategory_ShouldFilter() {
        CertificateListQueryDTO dto = new CertificateListQueryDTO();
        dto.setPage(1);
        dto.setSize(10);
        dto.setCategory("计算机";

        Page<Certificate> page = new Page<>(1, 10);
        Certificate entity = Certificate.builder()
                .id(1L).certName("软件设计师").category("计算机")
                .certLevel("中级").isDeleted(false)
                .build();
        IPage<Certificate> entityPage = page.setRecords(List.of(entity));
        when(certificateMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(entityPage);

        IPage<CertificateListVO> result = service.page(dto);

        assertEquals(1, result.getTotal());
        assertEquals("软件设计师", result.getRecords().get(0).getCertName());
        assertEquals("计算机", result.getRecords().get(0).getCategory());

        ArgumentCaptor<LambdaQueryWrapper<Certificate>> captor =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(certificateMapper).selectPage(any(Page.class), captor.capture());
        // 不强校验 SQL，验证 wrapper 已被构造即可
        assertNotNull(captor.getValue());
    }

    @Test
    void page_WithoutCategory_ShouldReturnAll() {
        CertificateListQueryDTO dto = new CertificateListQueryDTO();
        dto.setPage(1);
        dto.setSize(10);

        when(certificateMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<Certificate>(1, 10));

        IPage<CertificateListVO> result = service.page(dto);
        assertNotNull(result);
        assertEquals(0, result.getTotal());
    }

    @Test
    void detail_NotFound_ShouldThrow() {
        when(certificateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.detail(999L));
        assertEquals(ResultCode.NOT_FOUND.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("证书"));
    }

    @Test
    void detail_Found_ShouldReturnFullVO() {
        Certificate entity = Certificate.builder()
                .id(1L).certName("软件设计师").category("计算机").certLevel("中级")
                .applicableMajor("计算机科学与技术")
                .registrationTime("上半年3月").examTime("上半年5月")
                .examFee(100).certIntro("软件行业证书")
                .examRequirements(List.of("本科及以上", "相关工作经验"))
                .examArrangement("全国统考")
                .officialWebsite("https://example.com")
                .isDeleted(false)
                .build();
        when(certificateMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(entity);

        CertificateDetailVO vo = service.detail(1L);

        assertEquals("软件设计师", vo.getCertName());
        assertEquals(2, vo.getExamRequirements().size());
        assertEquals("全国统考", vo.getExamArrangement());
        assertEquals("https://example.com", vo.getOfficialWebsite());
    }
}
```

- [ ] **Step 3: 验证测试失败（红）**

```bash
mvn -pl haifeng-app -am test -Dtest=CertificateServiceImplTest -q
```

预期：编译失败，错误信息包含 `CertificateServiceImpl cannot be resolved`。

- [ ] **Step 4: 编写 ServiceImpl（绿）**

路径：`haifeng-app/src/main/java/com/haifeng/app/service/impl/certificate/CertificateServiceImpl.java`

```java
package com.haifeng.app.service.impl.certificate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.certificate.CertificateListQueryDTO;
import com.haifeng.app.service.certificate.CertificateService;
import com.haifeng.app.vo.certificate.CertificateDetailVO;
import com.haifeng.app.vo.certificate.CertificateListVO;
import com.haifeng.common.entity.certificate.Certificate;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.certificate.CertificateMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {

    private final CertificateMapper certificateMapper;

    @Override
    public IPage<CertificateListVO> page(CertificateListQueryDTO dto) {
        Page<Certificate> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<Certificate>()
                .eq(Certificate::getIsDeleted, false)
                .eq(StringUtils.hasText(dto.getCategory()),
                        Certificate::getCategory, dto.getCategory())
                .orderByAsc(Certificate::getId);

        IPage<Certificate> entityPage = certificateMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    @Override
    public CertificateDetailVO detail(Long certId) {
        Certificate cert = certificateMapper.selectOne(
                new LambdaQueryWrapper<Certificate>()
                        .eq(Certificate::getId, certId)
                        .eq(Certificate::getIsDeleted, false));
        if (cert == null) {
            log.debug("证书不存在或已删除, certId={}", certId);
            throw new BusinessException(ResultCode.NOT_FOUND, "证书不存在");
        }

        return CertificateDetailVO.builder()
                .id(cert.getId())
                .certName(cert.getCertName())
                .category(cert.getCategory())
                .certLevel(cert.getCertLevel())
                .applicableMajor(cert.getApplicableMajor())
                .registrationTime(cert.getRegistrationTime())
                .examTime(cert.getExamTime())
                .examFee(cert.getExamFee())
                .certIntro(cert.getCertIntro())
                .examRequirements(cert.getExamRequirements())
                .examArrangement(cert.getExamArrangement())
                .officialWebsite(cert.getOfficialWebsite())
                .build();
    }

    private CertificateListVO toListVO(Certificate e) {
        return CertificateListVO.builder()
                .id(e.getId())
                .certName(e.getCertName())
                .category(e.getCategory())
                .certLevel(e.getCertLevel())
                .applicableMajor(e.getApplicableMajor())
                .registrationTime(e.getRegistrationTime())
                .examTime(e.getExamTime())
                .examFee(e.getExamFee())
                .certIntro(e.getCertIntro())
                .build();
    }
}
```

- [ ] **Step 5: 验证测试通过（绿）**

```bash
mvn -pl haifeng-app -am test -Dtest=CertificateServiceImplTest -q
```

预期：`Tests run: 4, Failures: 0, Errors: 0, Skipped: 0`

- [ ] **Step 6: 标记本任务完成**

任务完成。等待最终统一提交。

---

## Task 7: 编写 CompetitionService 与测试

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/competition/CompetitionService.java`
- Create: `haifeng-app/src/main/java/com/haifeng/app/service/impl/competition/CompetitionServiceImpl.java`
- Create: `haifeng-app/src/test/java/com/haifeng/app/service/competition/CompetitionServiceImplTest.java`

- [ ] **Step 1: 编写 Service 接口**

路径：`haifeng-app/src/main/java/com/haifeng/app/service/competition/CompetitionService.java`

```java
package com.haifeng.app.service.competition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.vo.competition.CompetitionDetailVO;
import com.haifeng.app.vo.competition.CompetitionListVO;
import com.haifeng.app.vo.competition.CompetitionMajorBriefVO;
import com.haifeng.common.dto.common.BasePageQueryDTO;

public interface CompetitionService {

    /** 任务2接口1：竞赛分页列表（公开） */
    IPage<CompetitionListVO> page(BasePageQueryDTO dto);

    /** 任务2接口2：竞赛详情（登录） */
    CompetitionDetailVO detail(Long compId);

    /** 任务2接口3：分页查询某竞赛关联的专业（Pro） */
    IPage<CompetitionMajorBriefVO> majors(Long compId, BasePageQueryDTO dto);
}
```

- [ ] **Step 2: 编写测试类（红）**

路径：`haifeng-app/src/test/java/com/haifeng/app/service/competition/CompetitionServiceImplTest.java`

```java
package com.haifeng.app.service.competition;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.service.impl.competition.CompetitionServiceImpl;
import com.haifeng.app.vo.competition.CompetitionDetailVO;
import com.haifeng.app.vo.competition.CompetitionListVO;
import com.haifeng.app.vo.competition.CompetitionMajorBriefVO;
import com.haifeng.common.dto.common.BasePageQueryDTO;
import com.haifeng.common.entity.certificate.Competition;
import com.haifeng.common.entity.certificate.CompetitionDetail;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.certificate.CompetitionDetailMapper;
import com.haifeng.common.mapper.certificate.CompetitionMajorMapper;
import com.haifeng.common.mapper.certificate.CompetitionMapper;
import com.haifeng.common.response.ResultCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetitionServiceImplTest {

    @Mock
    private CompetitionMapper competitionMapper;

    @Mock
    private CompetitionDetailMapper competitionDetailMapper;

    @Mock
    private CompetitionMajorMapper competitionMajorMapper;

    @InjectMocks
    private CompetitionServiceImpl service;

    @Test
    void page_ShouldReturnPagedList() {
        BasePageQueryDTO dto = new BasePageQueryDTO();
        dto.setPage(1);
        dto.setSize(10);

        Competition entity = Competition.builder()
                .id(1L).compName("蓝桥杯").compLevel("国家级").registrationTime("上半年")
                .isDeleted(false)
                .build();
        IPage<Competition> entityPage = new Page<Competition>(1, 10).setRecords(List.of(entity));
        when(competitionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(entityPage);

        IPage<CompetitionListVO> result = service.page(dto);
        assertEquals(1, result.getTotal());
        assertEquals("蓝桥杯", result.getRecords().get(0).getCompName());
    }

    @Test
    void detail_CompetitionNotFound_ShouldThrow() {
        when(competitionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.detail(999L));
        assertEquals(ResultCode.NOT_FOUND.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("竞赛"));
    }

    @Test
    void detail_DetailNull_ShouldReturnVOWithBaseFields() {
        Competition competition = Competition.builder()
                .id(1L).compName("蓝桥杯").isDeleted(false)
                .build();
        when(competitionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(competition);
        when(competitionDetailMapper.findActiveByCompetitionId(1L)).thenReturn(null);

        CompetitionDetailVO vo = service.detail(1L);
        assertEquals(1L, vo.getId());
        assertEquals(1L, vo.getCompetitionId());
        assertNull(vo.getBasicInfo());
        assertNull(vo.getAwards());
    }

    @Test
    void detail_Found_ShouldReturnFullVO() {
        Competition competition = Competition.builder()
                .id(1L).compName("蓝桥杯").isDeleted(false)
                .build();
        CompetitionDetail detail = CompetitionDetail.builder()
                .id(10L).competitionId(1L)
                .basicInfo(Map.of("organizer", "工信部", "year", 2024))
                .awards(List.of("一等奖", "二等奖"))
                .background("背景介绍")
                .purposes(List.of("促进教学", "培养人才"))
                .competitionRules(List.of(Map.of("title", "组队", "content", "3人一组")))
                .scoringCriteria(List.of("代码40%", "答辩60%"))
                .notices(List.of("需提前注册"))
                .processGuide(List.of(Map.of("step", "1", "desc", "报名")))
                .awardsDisplay(List.of(Map.of("level", "国一", "count", "10")))
                .isDeleted(false)
                .build();
        when(competitionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(competition);
        when(competitionDetailMapper.findActiveByCompetitionId(1L)).thenReturn(detail);

        CompetitionDetailVO vo = service.detail(1L);
        assertEquals("工信部", vo.getBasicInfo().get("organizer"));
        assertEquals(2, vo.getAwards().size());
        assertEquals("背景介绍", vo.getBackground());
        assertEquals(1, vo.getCompetitionRules().size());
    }

    @Test
    void majors_CompetitionNotFound_ShouldThrow() {
        when(competitionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.majors(999L, new BasePageQueryDTO()));
        assertEquals(ResultCode.NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    void majors_NoRelation_ShouldReturnEmptyPage() {
        Competition competition = Competition.builder()
                .id(1L).compName("蓝桥杯").isDeleted(false)
                .build();
        when(competitionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(competition);
        when(competitionMajorMapper.selectMajorsByCompetitionId(any(Page.class), eq(1L)))
                .thenReturn(new Page<Map<String, Object>>(1, 10));

        BasePageQueryDTO dto = new BasePageQueryDTO();
        dto.setPage(1);
        dto.setSize(10);
        IPage<CompetitionMajorBriefVO> result = service.majors(1L, dto);
        assertNotNull(result);
        assertEquals(0, result.getTotal());
    }

    @Test
    void majors_Found_ShouldReturnPagedVO() {
        Competition competition = Competition.builder()
                .id(1L).compName("蓝桥杯").isDeleted(false)
                .build();
        Map<String, Object> row = Map.of("majorId", 100L, "majorName", "计算机科学");
        IPage<Map<String, Object>> mapPage = new Page<Map<String, Object>>(1, 10)
                .setRecords(List.of(row));
        when(competitionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(competition);
        when(competitionMajorMapper.selectMajorsByCompetitionId(any(Page.class), eq(1L)))
                .thenReturn(mapPage);

        BasePageQueryDTO dto = new BasePageQueryDTO();
        dto.setPage(1);
        dto.setSize(10);
        IPage<CompetitionMajorBriefVO> result = service.majors(1L, dto);
        assertEquals(1, result.getTotal());
        assertEquals(100L, result.getRecords().get(0).getMajorId());
        assertEquals("计算机科学", result.getRecords().get(0).getMajorName());
    }
}
```

- [ ] **Step 3: 验证测试失败（红）**

```bash
mvn -pl haifeng-app -am test -Dtest=CompetitionServiceImplTest -q
```

预期：编译失败，错误信息包含 `CompetitionServiceImpl cannot be resolved`。

- [ ] **Step 4: 编写 ServiceImpl（绿）**

路径：`haifeng-app/src/main/java/com/haifeng/app/service/impl/competition/CompetitionServiceImpl.java`

```java
package com.haifeng.app.service.impl.competition;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.service.competition.CompetitionService;
import com.haifeng.app.vo.competition.CompetitionDetailVO;
import com.haifeng.app.vo.competition.CompetitionListVO;
import com.haifeng.app.vo.competition.CompetitionMajorBriefVO;
import com.haifeng.common.dto.common.BasePageQueryDTO;
import com.haifeng.common.entity.certificate.Competition;
import com.haifeng.common.entity.certificate.CompetitionDetail;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.certificate.CompetitionDetailMapper;
import com.haifeng.common.mapper.certificate.CompetitionMajorMapper;
import com.haifeng.common.mapper.certificate.CompetitionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompetitionServiceImpl implements CompetitionService {

    private final CompetitionMapper competitionMapper;
    private final CompetitionDetailMapper competitionDetailMapper;
    private final CompetitionMajorMapper competitionMajorMapper;

    @Override
    public IPage<CompetitionListVO> page(BasePageQueryDTO dto) {
        Page<Competition> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<Competition>()
                .eq(Competition::getIsDeleted, false)
                .orderByAsc(Competition::getId);

        IPage<Competition> entityPage = competitionMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    @Override
    public CompetitionDetailVO detail(Long compId) {
        Competition competition = competitionMapper.selectOne(
                new LambdaQueryWrapper<Competition>()
                        .eq(Competition::getId, compId)
                        .eq(Competition::getIsDeleted, false));
        if (competition == null) {
            log.debug("竞赛不存在或已删除, compId={}", compId);
            throw new BusinessException(ResultCode.NOT_FOUND, "竞赛不存在");
        }

        CompetitionDetail detail = competitionDetailMapper.findActiveByCompetitionId(compId);

        CompetitionDetailVO.CompetitionDetailVOBuilder builder = CompetitionDetailVO.builder()
                .id(competition.getId())
                .competitionId(competition.getId());

        if (detail != null) {
            builder.basicInfo(detail.getBasicInfo())
                    .awards(detail.getAwards())
                    .background(detail.getBackground())
                    .purposes(detail.getPurposes())
                    .competitionRules(detail.getCompetitionRules())
                    .scoringCriteria(detail.getScoringCriteria())
                    .notices(detail.getNotices())
                    .processGuide(detail.getProcessGuide())
                    .awardsDisplay(detail.getAwardsDisplay());
        }
        return builder.build();
    }

    @Override
    public IPage<CompetitionMajorBriefVO> majors(Long compId, BasePageQueryDTO dto) {
        Competition competition = competitionMapper.selectOne(
                new LambdaQueryWrapper<Competition>()
                        .eq(Competition::getId, compId)
                        .eq(Competition::getIsDeleted, false));
        if (competition == null) {
            log.debug("竞赛不存在或已删除, compId={}", compId);
            throw new BusinessException(ResultCode.NOT_FOUND, "竞赛不存在");
        }

        Page<Map<String, Object>> page = new Page<>(dto.getPage(), dto.getSize());
        IPage<Map<String, Object>> mapPage =
                competitionMajorMapper.selectMajorsByCompetitionId(page, compId);
        return mapPage.convert(row -> CompetitionMajorBriefVO.builder()
                .majorId(row.get("majorId") != null
                        ? ((Number) row.get("majorId")).longValue() : null)
                .majorName(row.get("majorName") != null
                        ? String.valueOf(row.get("majorName")) : null)
                .build());
    }

    private CompetitionListVO toListVO(Competition e) {
        return CompetitionListVO.builder()
                .id(e.getId())
                .compName(e.getCompName())
                .compLevel(e.getCompLevel())
                .registrationTime(e.getRegistrationTime())
                .build();
    }
}
```

- [ ] **Step 5: 验证测试通过（绿）**

```bash
mvn -pl haifeng-app -am test -Dtest=CompetitionServiceImplTest -q
```

预期：`Tests run: 7, Failures: 0, Errors: 0, Skipped: 0`

- [ ] **Step 6: 标记本任务完成**

任务完成。等待最终统一提交。

---

## Task 8: 扩展 MajorService 与测试

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/major/MajorService.java`
- Modify: `haifeng-app/src/main/java/com/haifeng/app/service/impl/major/MajorServiceImpl.java`
- Modify: `haifeng-app/src/test/java/com/haifeng/app/service/major/MajorServiceImplTest.java`

- [ ] **Step 1: 修改 MajorService 接口**

在 `MajorService.java` 中新增 import 和方法：

```java
import com.haifeng.app.vo.major.CompetitionBriefVO;
```

接口内追加：

```java
    /** 任务3接口1：专业 → 关联竞赛列表（Pro 及以上） */
    IPage<CompetitionBriefVO> competitions(Long majorId, BasePageQueryDTO dto);
```

- [ ] **Step 2: 修改 MajorServiceImpl 实现**

打开 `MajorServiceImpl.java`，在 `import` 区域新增：

```java
import com.haifeng.app.vo.major.CompetitionBriefVO;
import com.haifeng.common.mapper.certificate.CompetitionMajorMapper;
```

把 `private final` 字段区改为（追加 mapper 字段）：

```java
    private final MajorMapper majorMapper;
    private final MajorDetailMapper majorDetailMapper;
    private final MajorPostgradDirectionMapper majorPostgradDirectionMapper;
    private final CompetitionMajorMapper competitionMajorMapper;
```

在类的最后（`toListVO` 私有方法之前）追加：

```java
    @Override
    public IPage<CompetitionBriefVO> competitions(Long majorId, BasePageQueryDTO dto) {
        // 1. 校验专业存在且上架
        Major major = majorMapper.selectOne(
                new LambdaQueryWrapper<Major>()
                        .eq(Major::getId, majorId)
                        .eq(Major::getStatus, STATUS_PUBLISHED));
        if (major == null) {
            log.debug("专业不存在或已下架, majorId={}", majorId);
            throw new BusinessException(ResultCode.NOT_FOUND, "专业不存在");
        }
        // 2. 联表分页
        Page<Map<String, Object>> page = new Page<>(dto.getPage(), dto.getSize());
        IPage<Map<String, Object>> mapPage =
                competitionMajorMapper.selectCompetitionsByMajorId(page, majorId);
        return mapPage.convert(row -> CompetitionBriefVO.builder()
                .competitionId(row.get("competitionId") != null
                        ? ((Number) row.get("competitionId")).longValue() : null)
                .competitionName(row.get("competitionName") != null
                        ? String.valueOf(row.get("competitionName")) : null)
                .build());
    }
```

- [ ] **Step 3: 追加测试用例到 MajorServiceImplTest**

打开 `MajorServiceImplTest.java`，在文件头部 import 区追加：

```java
import com.haifeng.app.vo.major.CompetitionBriefVO;
import com.haifeng.common.entity.certificate.CompetitionMajor;
```

（具体 import 顺序按文件现有风格；不存在则新增，存在则跳过。）

在 `MajorServiceImpl` 字段区追加：

```java
    @Mock
    private CompetitionMajorMapper competitionMajorMapper;
```

（`CompetitionMajor` 实体 import 不需要在测试中使用，可省略；只需要 mapper。）

在测试类末尾追加以下两个测试方法：

```java
    @Test
    void competitions_MajorNotFound_ShouldThrow() {
        when(majorMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> majorService.competitions(999L, new BasePageQueryDTO()));
        assertEquals(ResultCode.NOT_FOUND.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("专业"));
    }

    @Test
    void competitions_Found_ShouldReturnPagedVO() {
        Major major = Major.builder().id(1L).status(STATUS_PUBLISHED).build();
        when(majorMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(major);

        Map<String, Object> row = Map.of("competitionId", 200L, "competitionName", "蓝桥杯");
        IPage<Map<String, Object>> mapPage = new Page<Map<String, Object>>(1, 10)
                .setRecords(List.of(row));
        when(competitionMajorMapper.selectCompetitionsByMajorId(any(Page.class), eq(1L)))
                .thenReturn(mapPage);

        BasePageQueryDTO dto = new BasePageQueryDTO();
        dto.setPage(1);
        dto.setSize(10);
        IPage<CompetitionBriefVO> result = majorService.competitions(1L, dto);
        assertEquals(1, result.getTotal());
        assertEquals(200L, result.getRecords().get(0).getCompetitionId());
        assertEquals("蓝桥杯", result.getRecords().get(0).getCompetitionName());
    }
```

- [ ] **Step 4: 检查测试文件中 STATUS_PUBLISHED 常量是否可见**

如果 `MajorServiceImplTest` 没有 `STATUS_PUBLISHED` 常量，使用 `1` 替代（`status` 字段类型为 `Short`，传 `(short) 1`）。

- [ ] **Step 5: 验证编译并跑全部 major 测试**

```bash
mvn -pl haifeng-app -am test -Dtest=MajorServiceImplTest -q
```

预期：原有用例 + 新增 2 个用例全部通过。

- [ ] **Step 6: 标记本任务完成**

任务完成。等待最终统一提交。

---

## Task 9: 创建 CertificateController

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/certificate/CertificateController.java`

- [ ] **Step 1: 创建 Controller**

```java
package com.haifeng.app.controller.certificate;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.certificate.CertificateListQueryDTO;
import com.haifeng.app.service.certificate.CertificateService;
import com.haifeng.app.vo.certificate.CertificateDetailVO;
import com.haifeng.app.vo.certificate.CertificateListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端证书管理（spec 任务1）
 * 接口1 公开，接口2 需登录
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/certificate")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    /** 任务1接口1：证书列表（公开，支持 category 精准过滤） */
    @GetMapping("/list")
    public R<IPage<CertificateListVO>> list(@Valid CertificateListQueryDTO dto) {
        return R.ok(certificateService.page(dto));
    }

    /** 任务1接口2：证书详情（登录） */
    @RequireLogin
    @GetMapping("/{certId}/detail")
    public R<CertificateDetailVO> detail(@PathVariable Long certId) {
        return R.ok(certificateService.detail(certId));
    }
}
```

- [ ] **Step 2: 验证编译**

```bash
mvn -pl haifeng-app -am compile -q
```

预期：`BUILD SUCCESS`。

- [ ] **Step 3: 标记本任务完成**

任务完成。等待最终统一提交。

---

## Task 10: 创建 CompetitionController

**Files:**
- Create: `haifeng-app/src/main/java/com/haifeng/app/controller/competition/CompetitionController.java`

- [ ] **Step 1: 创建 Controller**

```java
package com.haifeng.app.controller.competition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.service.competition.CompetitionService;
import com.haifeng.app.vo.competition.CompetitionDetailVO;
import com.haifeng.app.vo.competition.CompetitionListVO;
import com.haifeng.app.vo.competition.CompetitionMajorBriefVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.annotation.RequirePro;
import com.haifeng.common.dto.common.BasePageQueryDTO;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端竞赛管理（spec 任务2）
 * 接口1 公开，接口2 需登录，接口3 需 Pro
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/competition")
@RequiredArgsConstructor
public class CompetitionController {

    private final CompetitionService competitionService;

    /** 任务2接口1：竞赛列表（公开） */
    @GetMapping("/list")
    public R<IPage<CompetitionListVO>> list(@Valid BasePageQueryDTO dto) {
        return R.ok(competitionService.page(dto));
    }

    /** 任务2接口2：竞赛详情（登录） */
    @RequireLogin
    @GetMapping("/{compId}/detail")
    public R<CompetitionDetailVO> detail(@PathVariable Long compId) {
        return R.ok(competitionService.detail(compId));
    }

    /** 任务2接口3：分页查询某竞赛关联的专业（Pro） */
    @RequirePro
    @GetMapping("/{compId}/majors")
    public R<IPage<CompetitionMajorBriefVO>> majors(
            @PathVariable Long compId,
            @Valid BasePageQueryDTO dto) {
        return R.ok(competitionService.majors(compId, dto));
    }
}
```

- [ ] **Step 2: 验证编译**

```bash
mvn -pl haifeng-app -am compile -q
```

预期：`BUILD SUCCESS`。

- [ ] **Step 3: 标记本任务完成**

任务完成。等待最终统一提交。

---

## Task 11: 扩展 MajorController

**Files:**
- Modify: `haifeng-app/src/main/java/com/haifeng/app/controller/major/MajorController.java`

- [ ] **Step 1: 添加 import**

在 `import com.haifeng.app.vo.major.MajorListVO;` 后追加：

```java
import com.haifeng.app.vo.major.CompetitionBriefVO;
```

- [ ] **Step 2: 添加新接口方法**

在 `postgradDirections` 方法之后追加：

```java
    /** 任务3接口1：专业 → 关联竞赛列表（Pro 及以上） */
    @RequirePro
    @GetMapping("/{majorId}/competitions")
    public R<IPage<CompetitionBriefVO>> competitions(
            @PathVariable Long majorId,
            @Valid BasePageQueryDTO dto) {
        return R.ok(majorService.competitions(majorId, dto));
    }
```

- [ ] **Step 3: 验证编译**

```bash
mvn -pl haifeng-app -am compile -q
```

预期：`BUILD SUCCESS`。

- [ ] **Step 4: 标记本任务完成**

任务完成。等待最终统一提交。

---

## Task 12: 全量编译 + 跑全部测试

- [ ] **Step 1: 编译 haifeng-app 全部模块**

```bash
mvn -pl haifeng-app -am clean compile -q
```

预期：`BUILD SUCCESS`。

- [ ] **Step 2: 运行新增的全部 Service 单元测试**

```bash
mvn -pl haifeng-app -am test -Dtest='CertificateServiceImplTest,CompetitionServiceImplTest,MajorServiceImplTest' -q
```

预期：所有用例通过。`MajorServiceImplTest` 中原有 + 新增 `competitions_*` 用例均通过。

- [ ] **Step 3: 运行全模块测试（如时间允许）**

```bash
mvn -pl haifeng-app -am test -q
```

预期：所有测试通过，无回归。

- [ ] **Step 4: 标记实施完成**

所有任务完成。等待用户统一提交。

---

## 自审记录（实施前自审）

### 1. Spec 覆盖

- 任务1接口1（证书列表，公开，category 过滤） → Task 4（DTO + VO） + Task 6（Service） + Task 9（Controller）
- 任务1接口2（证书详情，登录） → Task 4（VO） + Task 6（Service） + Task 9（Controller，@RequireLogin）
- 任务2接口1（竞赛列表，公开） → Task 5（VO） + Task 7（Service） + Task 10（Controller）
- 任务2接口2（竞赛详情，登录） → Task 1（Mapper） + Task 5（VO） + Task 7（Service） + Task 10（Controller，@RequireLogin）
- 任务2接口3（竞赛→专业，Pro） → Task 2（Mapper） + Task 5（VO） + Task 7（Service） + Task 10（Controller，@RequirePro）
- 任务3接口1（专业→竞赛，Pro） → Task 3（Mapper） + Task 5（CompetitionBriefVO） + Task 8（MajorService 扩展） + Task 11（MajorController 扩展）
- 软删除过滤 → Task 1、Task 6、Task 7 全部使用 `is_deleted = FALSE`
- JSONB 反序列化 → 实体已有 `JacksonTypeHandler`，VO 字段类型与实体一致，无需额外处理
- 错误处理 → Task 6（证书详情）、Task 7（竞赛详情 / 竞赛不存在）、Task 8（专业不存在）全部抛 `BusinessException(NOT_FOUND)`
- 测试覆盖 → Task 6（4 个用例）、Task 7（7 个用例）、Task 8（追加 2 个用例）

### 2. 占位符扫描

无 TBD / TODO / "fill in later"。

### 3. 类型一致性

- `CertificateDetailVO.examRequirements` 类型 `List<String>` ↔ 实体 `Certificate.examRequirements` 类型 `List<String>` ✓
- `CompetitionDetailVO.basicInfo` 类型 `Map<String, Object>` ↔ 实体 `CompetitionDetail.basicInfo` 类型 `Map<String, Object>` ✓
- `CompetitionDetailVO.competitionRules` 类型 `List<Map<String, String>>` ↔ 实体 `CompetitionDetail.competitionRules` 类型 `List<Map<String, String>>` ✓
- `CompetitionDetailVO.processGuide` / `awardsDisplay` 同上 ✓
- Mapper 方法签名（`Page<?>` + `@Param`）→ Service 接收（`Page<Map<String, Object>>` + `compId`）一致 ✓
- 任务8 中 `MajorServiceImpl.competitions` 方法签名 → `MajorService` 接口签名 → `MajorController` 调用一致 ✓

无发现类型不一致问题。
