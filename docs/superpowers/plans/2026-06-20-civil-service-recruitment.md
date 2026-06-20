# 体制内招录模块 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 app 端实现体制内招录 4 张表（公务员/事业编/部队文职/选调生）的分页列表（无需登录）和详情（需登录）接口。

**Architecture:** 遵循现有 `employment/jobIndex` 模式。common 层放 Entity+Mapper，app 层放 DTO+VO+Service+Controller。每张表独立文件，共 28 个新文件。

**Tech Stack:** Spring Boot 3.x + MyBatis-Plus + Lombok + Hutool

---

## File Structure

```
haifeng-common/src/main/java/com/haifeng/common/
  entity/employment/civilService/
    CivilPosition.java
    InstitutionPosition.java
    MilitaryPosition.java
    SelectionPosition.java
  mapper/employment/civilService/
    CivilPositionMapper.java
    InstitutionPositionMapper.java
    MilitaryPositionMapper.java
    SelectionPositionMapper.java

haifeng-app/src/main/java/com/haifeng/app/
  dto/employment/civilService/
    CivilPositionSearchDTO.java
    InstitutionPositionSearchDTO.java
    MilitaryPositionSearchDTO.java
    SelectionPositionSearchDTO.java
  vo/employment/civilService/
    CivilPositionListVO.java
    CivilPositionDetailVO.java
    InstitutionPositionListVO.java
    InstitutionPositionDetailVO.java
    MilitaryPositionListVO.java
    MilitaryPositionDetailVO.java
    SelectionPositionListVO.java
    SelectionPositionDetailVO.java
  service/employment/civilService/
    CivilPositionService.java
    InstitutionPositionService.java
    MilitaryPositionService.java
    SelectionPositionService.java
  service/impl/employment/civilService/
    CivilPositionServiceImpl.java
    InstitutionPositionServiceImpl.java
    MilitaryPositionServiceImpl.java
    SelectionPositionServiceImpl.java
  controller/employment/civilService/
    CivilPositionController.java
    InstitutionPositionController.java
    MilitaryPositionController.java
    SelectionPositionController.java
```

---

### Task 1: Common Entities

**Files:**
- Create: `haifeng-common/.../entity/employment/civilService/CivilPosition.java`
- Create: `haifeng-common/.../entity/employment/civilService/InstitutionPosition.java`
- Create: `haifeng-common/.../entity/employment/civilService/MilitaryPosition.java`
- Create: `haifeng-common/.../entity/employment/civilService/SelectionPosition.java`

- [ ] **Step 1: Create CivilPosition entity**

```java
package com.haifeng.common.entity.employment.civilService;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_civil_position", autoResultMap = true)
public class CivilPosition implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String positionName;
    private String examType;
    private String recruitingDept;
    private String deptCode;
    private String positionCode;
    private String affiliatedBureau;
    private String majorRequirement;
    private String minEducation;
    private String degreeRequirement;
    private String politicalStatus;
    private String workExperience;
    private String grassrootsExperience;
    private String examCategory;
    private String interviewRatio;
    private Integer recruitmentCount;
    private Boolean hasProfessionalTest;
    private String workLocation;
    private String workLocationDetail;
    private String householdRequirement;
    private String householdLocation;
    private String positionIntro;
    private String remark;
    private String officialWebsite;
    private String contactPhone;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private String regStatus;
    private Integer applicantCount;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 2: Create InstitutionPosition entity**

```java
package com.haifeng.common.entity.employment.civilService;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_institution_position", autoResultMap = true)
public class InstitutionPosition implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String positionName;
    private String supervisingDept;
    private String institution;
    private String workLocation;
    private String province;
    private String examCategory;
    private String positionType;
    private String subCategory;
    private String educationRequirement;
    private String degreeRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String salaryRange;
    private String regDeadline;
    private String[] majorRequirements;
    private String specialPosition;
    private String otherRequirement;
    private String otherRequirementDesc;
    private String remarkType;
    private String remarkDesc;
    private String consultationPhone;
    private String supervisionPhone;
    private String positionStatus;
    private String positionTag;
    private String tagText;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 3: Create MilitaryPosition entity**

```java
package com.haifeng.common.entity.employment.civilService;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_military_position", autoResultMap = true)
public class MilitaryPosition implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String positionName;
    private String employerUnit;
    private String department;
    private String positionType;
    private String workLocation;
    private String salaryRange;
    private String majorRequirement;
    private String educationRequirement;
    private String regDeadline;
    private String positionStatus;
    private String positionDescription;
    private String[] responsibilities;
    private String[] qualifications;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 4: Create SelectionPosition entity**

```java
package com.haifeng.common.entity.employment.civilService;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_selection_position", autoResultMap = true)
public class SelectionPosition implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String positionName;
    private String selectionType;
    private String year;
    private String province;
    private String organizingDept;
    private String targetUnit;
    private String workLocation;
    private String trainingDirection;
    private String grassrootsServiceYears;
    private String trainingPlan;
    private String educationRequirement;
    private String degreeRequirement;
    private String majorRequirement;
    private String[] majorCategories;
    private String universityRequirement;
    private String[] targetUniversities;
    private String politicalStatus;
    private String studentCadreRequirement;
    private String awardsRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String examSubjects;
    private String interviewForm;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private OffsetDateTime examTime;
    private String applyLink;
    private String positionStatus;
    private String remark;
    private String contactPhone;
    private String officialLink;
    private String content;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
```

---

### Task 2: Common Mappers

**Files:**
- Create: `haifeng-common/.../mapper/employment/civilService/CivilPositionMapper.java`
- Create: `haifeng-common/.../mapper/employment/civilService/InstitutionPositionMapper.java`
- Create: `haifeng-common/.../mapper/employment/civilService/MilitaryPositionMapper.java`
- Create: `haifeng-common/.../mapper/employment/civilService/SelectionPositionMapper.java`

- [ ] **Step 1: Create CivilPositionMapper**

```java
package com.haifeng.common.mapper.employment.civilService;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.employment.civilService.CivilPosition;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CivilPositionMapper extends BaseMapper<CivilPosition> {

}
```

- [ ] **Step 2: Create InstitutionPositionMapper**

```java
package com.haifeng.common.mapper.employment.civilService;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.employment.civilService.InstitutionPosition;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InstitutionPositionMapper extends BaseMapper<InstitutionPosition> {

}
```

- [ ] **Step 3: Create MilitaryPositionMapper**

```java
package com.haifeng.common.mapper.employment.civilService;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.employment.civilService.MilitaryPosition;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MilitaryPositionMapper extends BaseMapper<MilitaryPosition> {

}
```

- [ ] **Step 4: Create SelectionPositionMapper**

```java
package com.haifeng.common.mapper.employment.civilService;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.employment.civilService.SelectionPosition;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SelectionPositionMapper extends BaseMapper<SelectionPosition> {

}
```

---

### Task 3: App DTOs

**Files:**
- Create: `haifeng-app/.../dto/employment/civilService/CivilPositionSearchDTO.java`
- Create: `haifeng-app/.../dto/employment/civilService/InstitutionPositionSearchDTO.java`
- Create: `haifeng-app/.../dto/employment/civilService/MilitaryPositionSearchDTO.java`
- Create: `haifeng-app/.../dto/employment/civilService/SelectionPositionSearchDTO.java`

- [ ] **Step 1: Create CivilPositionSearchDTO**

```java
package com.haifeng.app.dto.employment.civilService;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CivilPositionSearchDTO extends BasePageQueryDTO {

    private String keyword;

    private String examType;
    private String positionCode;
    private String deptCode;
    private String minEducation;
    private String degreeRequirement;
    private String politicalStatus;
    private String examCategory;
}
```

- [ ] **Step 2: Create InstitutionPositionSearchDTO**

```java
package com.haifeng.app.dto.employment.civilService;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class InstitutionPositionSearchDTO extends BasePageQueryDTO {

    private String keyword;

    private String province;
    private String examCategory;
    private String positionType;
    private String educationRequirement;
    private String degreeRequirement;
    private String positionStatus;
    private String specialPosition;
}
```

- [ ] **Step 3: Create MilitaryPositionSearchDTO**

```java
package com.haifeng.app.dto.employment.civilService;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MilitaryPositionSearchDTO extends BasePageQueryDTO {

    private String keyword;

    private String positionType;
    private String educationRequirement;
    private String positionStatus;
    private String workLocation;
}
```

- [ ] **Step 4: Create SelectionPositionSearchDTO**

```java
package com.haifeng.app.dto.employment.civilService;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SelectionPositionSearchDTO extends BasePageQueryDTO {

    private String keyword;

    private String selectionType;
    private String year;
    private String province;
    private String educationRequirement;
    private String degreeRequirement;
    private String politicalStatus;
    private Integer ageLimit;
    private String positionStatus;
}
```

---

### Task 4: App ListVOs

**Files:**
- Create: `haifeng-app/.../vo/employment/civilService/CivilPositionListVO.java`
- Create: `haifeng-app/.../vo/employment/civilService/InstitutionPositionListVO.java`
- Create: `haifeng-app/.../vo/employment/civilService/MilitaryPositionListVO.java`
- Create: `haifeng-app/.../vo/employment/civilService/SelectionPositionListVO.java`

- [ ] **Step 1: Create CivilPositionListVO**

```java
package com.haifeng.app.vo.employment.civilService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CivilPositionListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String positionName;
    private String examType;
    private String recruitingDept;
    private String majorRequirement;
    private String minEducation;
    private String degreeRequirement;
    private String politicalStatus;
    private String examCategory;
    private String workLocation;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private String regStatus;
    private Integer applicantCount;
}
```

- [ ] **Step 2: Create InstitutionPositionListVO**

```java
package com.haifeng.app.vo.employment.civilService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionPositionListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String positionName;
    private String supervisingDept;
    private String institution;
    private String workLocation;
    private String province;
    private String examCategory;
    private String positionType;
    private String educationRequirement;
    private String degreeRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String salaryRange;
    private String regDeadline;
    private String specialPosition;
    private String positionStatus;
}
```

- [ ] **Step 3: Create MilitaryPositionListVO**

```java
package com.haifeng.app.vo.employment.civilService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilitaryPositionListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String positionName;
    private String employerUnit;
    private String department;
    private String positionType;
    private String workLocation;
    private String salaryRange;
    private String majorRequirement;
    private String educationRequirement;
    private String regDeadline;
    private String positionStatus;
}
```

- [ ] **Step 4: Create SelectionPositionListVO**

```java
package com.haifeng.app.vo.employment.civilService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectionPositionListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String positionName;
    private String selectionType;
    private String year;
    private String province;
    private String organizingDept;
    private String targetUnit;
    private String workLocation;
    private String trainingDirection;
    private String educationRequirement;
    private String degreeRequirement;
    private String majorRequirement;
    private String universityRequirement;
    private String politicalStatus;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private String positionStatus;
}
```

---

### Task 5: App DetailVOs

**Files:**
- Create: `haifeng-app/.../vo/employment/civilService/CivilPositionDetailVO.java`
- Create: `haifeng-app/.../vo/employment/civilService/InstitutionPositionDetailVO.java`
- Create: `haifeng-app/.../vo/employment/civilService/MilitaryPositionDetailVO.java`
- Create: `haifeng-app/.../vo/employment/civilService/SelectionPositionDetailVO.java`

- [ ] **Step 1: Create CivilPositionDetailVO**

```java
package com.haifeng.app.vo.employment.civilService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CivilPositionDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String positionName;
    private String examType;
    private String recruitingDept;
    private String deptCode;
    private String positionCode;
    private String affiliatedBureau;
    private String majorRequirement;
    private String minEducation;
    private String degreeRequirement;
    private String politicalStatus;
    private String workExperience;
    private String grassrootsExperience;
    private String examCategory;
    private String interviewRatio;
    private Integer recruitmentCount;
    private Boolean hasProfessionalTest;
    private String workLocation;
    private String workLocationDetail;
    private String householdRequirement;
    private String householdLocation;
    private String positionIntro;
    private String remark;
    private String officialWebsite;
    private String contactPhone;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private String regStatus;
    private Integer applicantCount;
}
```

- [ ] **Step 2: Create InstitutionPositionDetailVO**

```java
package com.haifeng.app.vo.employment.civilService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionPositionDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String positionName;
    private String supervisingDept;
    private String institution;
    private String workLocation;
    private String province;
    private String examCategory;
    private String positionType;
    private String subCategory;
    private String educationRequirement;
    private String degreeRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String salaryRange;
    private String regDeadline;
    private String[] majorRequirements;
    private String specialPosition;
    private String otherRequirement;
    private String otherRequirementDesc;
    private String remarkType;
    private String remarkDesc;
    private String consultationPhone;
    private String supervisionPhone;
    private String positionStatus;
    private String positionTag;
    private String tagText;
}
```

- [ ] **Step 3: Create MilitaryPositionDetailVO**

```java
package com.haifeng.app.vo.employment.civilService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilitaryPositionDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String positionName;
    private String employerUnit;
    private String department;
    private String positionType;
    private String workLocation;
    private String salaryRange;
    private String majorRequirement;
    private String educationRequirement;
    private String regDeadline;
    private String positionStatus;
    private String positionDescription;
    private String[] responsibilities;
    private String[] qualifications;
}
```

- [ ] **Step 4: Create SelectionPositionDetailVO**

```java
package com.haifeng.app.vo.employment.civilService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectionPositionDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String positionName;
    private String selectionType;
    private String year;
    private String province;
    private String organizingDept;
    private String targetUnit;
    private String workLocation;
    private String trainingDirection;
    private String grassrootsServiceYears;
    private String trainingPlan;
    private String educationRequirement;
    private String degreeRequirement;
    private String majorRequirement;
    private String[] majorCategories;
    private String universityRequirement;
    private String[] targetUniversities;
    private String politicalStatus;
    private String studentCadreRequirement;
    private String awardsRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String examSubjects;
    private String interviewForm;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private OffsetDateTime examTime;
    private String applyLink;
    private String positionStatus;
    private String remark;
    private String contactPhone;
    private String officialLink;
    private String content;
}
```

---

### Task 6: App Service Interfaces

**Files:**
- Create: `haifeng-app/.../service/employment/civilService/CivilPositionService.java`
- Create: `haifeng-app/.../service/employment/civilService/InstitutionPositionService.java`
- Create: `haifeng-app/.../service/employment/civilService/MilitaryPositionService.java`
- Create: `haifeng-app/.../service/employment/civilService/SelectionPositionService.java`

- [ ] **Step 1: Create CivilPositionService**

```java
package com.haifeng.app.service.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.civilService.CivilPositionSearchDTO;
import com.haifeng.app.vo.employment.civilService.CivilPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.CivilPositionListVO;

public interface CivilPositionService {

    IPage<CivilPositionListVO> page(CivilPositionSearchDTO dto);

    CivilPositionDetailVO detail(Long id);
}
```

- [ ] **Step 2: Create InstitutionPositionService**

```java
package com.haifeng.app.service.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.civilService.InstitutionPositionSearchDTO;
import com.haifeng.app.vo.employment.civilService.InstitutionPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.InstitutionPositionListVO;

public interface InstitutionPositionService {

    IPage<InstitutionPositionListVO> page(InstitutionPositionSearchDTO dto);

    InstitutionPositionDetailVO detail(Long id);
}
```

- [ ] **Step 3: Create MilitaryPositionService**

```java
package com.haifeng.app.service.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.civilService.MilitaryPositionSearchDTO;
import com.haifeng.app.vo.employment.civilService.MilitaryPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.MilitaryPositionListVO;

public interface MilitaryPositionService {

    IPage<MilitaryPositionListVO> page(MilitaryPositionSearchDTO dto);

    MilitaryPositionDetailVO detail(Long id);
}
```

- [ ] **Step 4: Create SelectionPositionService**

```java
package com.haifeng.app.service.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.civilService.SelectionPositionSearchDTO;
import com.haifeng.app.vo.employment.civilService.SelectionPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.SelectionPositionListVO;

public interface SelectionPositionService {

    IPage<SelectionPositionListVO> page(SelectionPositionSearchDTO dto);

    SelectionPositionDetailVO detail(Long id);
}
```

---

### Task 7: App Service Implementations

**Files:**
- Create: `haifeng-app/.../service/impl/employment/civilService/CivilPositionServiceImpl.java`
- Create: `haifeng-app/.../service/impl/employment/civilService/InstitutionPositionServiceImpl.java`
- Create: `haifeng-app/.../service/impl/employment/civilService/MilitaryPositionServiceImpl.java`
- Create: `haifeng-app/.../service/impl/employment/civilService/SelectionPositionServiceImpl.java`

- [ ] **Step 1: Create CivilPositionServiceImpl**

```java
package com.haifeng.app.service.impl.employment.civilService;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.civilService.CivilPositionSearchDTO;
import com.haifeng.app.service.employment.civilService.CivilPositionService;
import com.haifeng.app.vo.employment.civilService.CivilPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.CivilPositionListVO;
import com.haifeng.common.entity.employment.civilService.CivilPosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.civilService.CivilPositionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CivilPositionServiceImpl implements CivilPositionService {

    private final CivilPositionMapper civilPositionMapper;

    @Override
    public IPage<CivilPositionListVO> page(CivilPositionSearchDTO dto) {
        LambdaQueryWrapper<CivilPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CivilPosition::getIsDeleted, false);

        if (StrUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w
                    .like(CivilPosition::getPositionName, dto.getKeyword())
                    .or()
                    .like(CivilPosition::getRecruitingDept, dto.getKeyword())
                    .or()
                    .like(CivilPosition::getMajorRequirement, dto.getKeyword())
                    .or()
                    .like(CivilPosition::getWorkLocation, dto.getKeyword())
            );
        }

        wrapper.eq(StrUtil.isNotBlank(dto.getExamType()), CivilPosition::getExamType, dto.getExamType());
        wrapper.eq(StrUtil.isNotBlank(dto.getPositionCode()), CivilPosition::getPositionCode, dto.getPositionCode());
        wrapper.eq(StrUtil.isNotBlank(dto.getDeptCode()), CivilPosition::getDeptCode, dto.getDeptCode());
        wrapper.eq(StrUtil.isNotBlank(dto.getMinEducation()), CivilPosition::getMinEducation, dto.getMinEducation());
        wrapper.eq(StrUtil.isNotBlank(dto.getDegreeRequirement()), CivilPosition::getDegreeRequirement, dto.getDegreeRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getPoliticalStatus()), CivilPosition::getPoliticalStatus, dto.getPoliticalStatus());
        wrapper.eq(StrUtil.isNotBlank(dto.getExamCategory()), CivilPosition::getExamCategory, dto.getExamCategory());

        wrapper.orderByDesc(CivilPosition::getCreatedAt);

        Page<CivilPosition> page = new Page<>(dto.getPage(), dto.getSize());
        civilPositionMapper.selectPage(page, wrapper);

        return page.convert(item -> CivilPositionListVO.builder()
                .id(item.getId())
                .positionName(item.getPositionName())
                .examType(item.getExamType())
                .recruitingDept(item.getRecruitingDept())
                .majorRequirement(item.getMajorRequirement())
                .minEducation(item.getMinEducation())
                .degreeRequirement(item.getDegreeRequirement())
                .politicalStatus(item.getPoliticalStatus())
                .examCategory(item.getExamCategory())
                .workLocation(item.getWorkLocation())
                .regStartDate(item.getRegStartDate())
                .regEndDate(item.getRegEndDate())
                .regStatus(item.getRegStatus())
                .applicantCount(item.getApplicantCount())
                .build());
    }

    @Override
    public CivilPositionDetailVO detail(Long id) {
        CivilPosition item = civilPositionMapper.selectById(id);
        if (item == null || Boolean.TRUE.equals(item.getIsDeleted())) {
            log.warn("公务员职位不存在，id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return CivilPositionDetailVO.builder()
                .id(item.getId())
                .positionName(item.getPositionName())
                .examType(item.getExamType())
                .recruitingDept(item.getRecruitingDept())
                .deptCode(item.getDeptCode())
                .positionCode(item.getPositionCode())
                .affiliatedBureau(item.getAffiliatedBureau())
                .majorRequirement(item.getMajorRequirement())
                .minEducation(item.getMinEducation())
                .degreeRequirement(item.getDegreeRequirement())
                .politicalStatus(item.getPoliticalStatus())
                .workExperience(item.getWorkExperience())
                .grassrootsExperience(item.getGrassrootsExperience())
                .examCategory(item.getExamCategory())
                .interviewRatio(item.getInterviewRatio())
                .recruitmentCount(item.getRecruitmentCount())
                .hasProfessionalTest(item.getHasProfessionalTest())
                .workLocation(item.getWorkLocation())
                .workLocationDetail(item.getWorkLocationDetail())
                .householdRequirement(item.getHouseholdRequirement())
                .householdLocation(item.getHouseholdLocation())
                .positionIntro(item.getPositionIntro())
                .remark(item.getRemark())
                .officialWebsite(item.getOfficialWebsite())
                .contactPhone(item.getContactPhone())
                .regStartDate(item.getRegStartDate())
                .regEndDate(item.getRegEndDate())
                .regStatus(item.getRegStatus())
                .applicantCount(item.getApplicantCount())
                .build();
    }
}
```

- [ ] **Step 2: Create InstitutionPositionServiceImpl**

```java
package com.haifeng.app.service.impl.employment.civilService;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.civilService.InstitutionPositionSearchDTO;
import com.haifeng.app.service.employment.civilService.InstitutionPositionService;
import com.haifeng.app.vo.employment.civilService.InstitutionPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.InstitutionPositionListVO;
import com.haifeng.common.entity.employment.civilService.InstitutionPosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.civilService.InstitutionPositionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstitutionPositionServiceImpl implements InstitutionPositionService {

    private final InstitutionPositionMapper institutionPositionMapper;

    @Override
    public IPage<InstitutionPositionListVO> page(InstitutionPositionSearchDTO dto) {
        LambdaQueryWrapper<InstitutionPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InstitutionPosition::getIsDeleted, false);

        if (StrUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w
                    .like(InstitutionPosition::getPositionName, dto.getKeyword())
                    .or()
                    .like(InstitutionPosition::getSupervisingDept, dto.getKeyword())
                    .or()
                    .like(InstitutionPosition::getInstitution, dto.getKeyword())
                    .or()
                    .like(InstitutionPosition::getWorkLocation, dto.getKeyword())
            );
        }

        wrapper.eq(StrUtil.isNotBlank(dto.getProvince()), InstitutionPosition::getProvince, dto.getProvince());
        wrapper.eq(StrUtil.isNotBlank(dto.getExamCategory()), InstitutionPosition::getExamCategory, dto.getExamCategory());
        wrapper.eq(StrUtil.isNotBlank(dto.getPositionType()), InstitutionPosition::getPositionType, dto.getPositionType());
        wrapper.eq(StrUtil.isNotBlank(dto.getEducationRequirement()), InstitutionPosition::getEducationRequirement, dto.getEducationRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getDegreeRequirement()), InstitutionPosition::getDegreeRequirement, dto.getDegreeRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getPositionStatus()), InstitutionPosition::getPositionStatus, dto.getPositionStatus());
        wrapper.eq(StrUtil.isNotBlank(dto.getSpecialPosition()), InstitutionPosition::getSpecialPosition, dto.getSpecialPosition());

        wrapper.orderByDesc(InstitutionPosition::getCreatedAt);

        Page<InstitutionPosition> page = new Page<>(dto.getPage(), dto.getSize());
        institutionPositionMapper.selectPage(page, wrapper);

        return page.convert(item -> InstitutionPositionListVO.builder()
                .id(item.getId())
                .positionName(item.getPositionName())
                .supervisingDept(item.getSupervisingDept())
                .institution(item.getInstitution())
                .workLocation(item.getWorkLocation())
                .province(item.getProvince())
                .examCategory(item.getExamCategory())
                .positionType(item.getPositionType())
                .educationRequirement(item.getEducationRequirement())
                .degreeRequirement(item.getDegreeRequirement())
                .ageLimit(item.getAgeLimit())
                .recruitmentCount(item.getRecruitmentCount())
                .salaryRange(item.getSalaryRange())
                .regDeadline(item.getRegDeadline())
                .specialPosition(item.getSpecialPosition())
                .positionStatus(item.getPositionStatus())
                .build());
    }

    @Override
    public InstitutionPositionDetailVO detail(Long id) {
        InstitutionPosition item = institutionPositionMapper.selectById(id);
        if (item == null || Boolean.TRUE.equals(item.getIsDeleted())) {
            log.warn("事业编职位不存在，id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return InstitutionPositionDetailVO.builder()
                .id(item.getId())
                .positionName(item.getPositionName())
                .supervisingDept(item.getSupervisingDept())
                .institution(item.getInstitution())
                .workLocation(item.getWorkLocation())
                .province(item.getProvince())
                .examCategory(item.getExamCategory())
                .positionType(item.getPositionType())
                .subCategory(item.getSubCategory())
                .educationRequirement(item.getEducationRequirement())
                .degreeRequirement(item.getDegreeRequirement())
                .ageLimit(item.getAgeLimit())
                .recruitmentCount(item.getRecruitmentCount())
                .salaryRange(item.getSalaryRange())
                .regDeadline(item.getRegDeadline())
                .majorRequirements(item.getMajorRequirements())
                .specialPosition(item.getSpecialPosition())
                .otherRequirement(item.getOtherRequirement())
                .otherRequirementDesc(item.getOtherRequirementDesc())
                .remarkType(item.getRemarkType())
                .remarkDesc(item.getRemarkDesc())
                .consultationPhone(item.getConsultationPhone())
                .supervisionPhone(item.getSupervisionPhone())
                .positionStatus(item.getPositionStatus())
                .positionTag(item.getPositionTag())
                .tagText(item.getTagText())
                .build();
    }
}
```

- [ ] **Step 3: Create MilitaryPositionServiceImpl**

```java
package com.haifeng.app.service.impl.employment.civilService;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.civilService.MilitaryPositionSearchDTO;
import com.haifeng.app.service.employment.civilService.MilitaryPositionService;
import com.haifeng.app.vo.employment.civilService.MilitaryPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.MilitaryPositionListVO;
import com.haifeng.common.entity.employment.civilService.MilitaryPosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.civilService.MilitaryPositionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MilitaryPositionServiceImpl implements MilitaryPositionService {

    private final MilitaryPositionMapper militaryPositionMapper;

    @Override
    public IPage<MilitaryPositionListVO> page(MilitaryPositionSearchDTO dto) {
        LambdaQueryWrapper<MilitaryPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MilitaryPosition::getIsDeleted, false);

        if (StrUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w
                    .like(MilitaryPosition::getPositionName, dto.getKeyword())
                    .or()
                    .like(MilitaryPosition::getEmployerUnit, dto.getKeyword())
                    .or()
                    .like(MilitaryPosition::getDepartment, dto.getKeyword())
                    .or()
                    .like(MilitaryPosition::getMajorRequirement, dto.getKeyword())
            );
        }

        wrapper.eq(StrUtil.isNotBlank(dto.getPositionType()), MilitaryPosition::getPositionType, dto.getPositionType());
        wrapper.eq(StrUtil.isNotBlank(dto.getEducationRequirement()), MilitaryPosition::getEducationRequirement, dto.getEducationRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getPositionStatus()), MilitaryPosition::getPositionStatus, dto.getPositionStatus());
        wrapper.eq(StrUtil.isNotBlank(dto.getWorkLocation()), MilitaryPosition::getWorkLocation, dto.getWorkLocation());

        wrapper.orderByDesc(MilitaryPosition::getCreatedAt);

        Page<MilitaryPosition> page = new Page<>(dto.getPage(), dto.getSize());
        militaryPositionMapper.selectPage(page, wrapper);

        return page.convert(item -> MilitaryPositionListVO.builder()
                .id(item.getId())
                .positionName(item.getPositionName())
                .employerUnit(item.getEmployerUnit())
                .department(item.getDepartment())
                .positionType(item.getPositionType())
                .workLocation(item.getWorkLocation())
                .salaryRange(item.getSalaryRange())
                .majorRequirement(item.getMajorRequirement())
                .educationRequirement(item.getEducationRequirement())
                .regDeadline(item.getRegDeadline())
                .positionStatus(item.getPositionStatus())
                .build());
    }

    @Override
    public MilitaryPositionDetailVO detail(Long id) {
        MilitaryPosition item = militaryPositionMapper.selectById(id);
        if (item == null || Boolean.TRUE.equals(item.getIsDeleted())) {
            log.warn("部队文职岗位不存在，id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return MilitaryPositionDetailVO.builder()
                .id(item.getId())
                .positionName(item.getPositionName())
                .employerUnit(item.getEmployerUnit())
                .department(item.getDepartment())
                .positionType(item.getPositionType())
                .workLocation(item.getWorkLocation())
                .salaryRange(item.getSalaryRange())
                .majorRequirement(item.getMajorRequirement())
                .educationRequirement(item.getEducationRequirement())
                .regDeadline(item.getRegDeadline())
                .positionStatus(item.getPositionStatus())
                .positionDescription(item.getPositionDescription())
                .responsibilities(item.getResponsibilities())
                .qualifications(item.getQualifications())
                .build();
    }
}
```

- [ ] **Step 4: Create SelectionPositionServiceImpl**

```java
package com.haifeng.app.service.impl.employment.civilService;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.employment.civilService.SelectionPositionSearchDTO;
import com.haifeng.app.service.employment.civilService.SelectionPositionService;
import com.haifeng.app.vo.employment.civilService.SelectionPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.SelectionPositionListVO;
import com.haifeng.common.entity.employment.civilService.SelectionPosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.civilService.SelectionPositionMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SelectionPositionServiceImpl implements SelectionPositionService {

    private final SelectionPositionMapper selectionPositionMapper;

    @Override
    public IPage<SelectionPositionListVO> page(SelectionPositionSearchDTO dto) {
        LambdaQueryWrapper<SelectionPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SelectionPosition::getIsDeleted, false);

        if (StrUtil.isNotBlank(dto.getKeyword())) {
            wrapper.and(w -> w
                    .like(SelectionPosition::getPositionName, dto.getKeyword())
                    .or()
                    .like(SelectionPosition::getTargetUnit, dto.getKeyword())
                    .or()
                    .like(SelectionPosition::getWorkLocation, dto.getKeyword())
                    .or()
                    .like(SelectionPosition::getMajorRequirement, dto.getKeyword())
            );
        }

        wrapper.eq(StrUtil.isNotBlank(dto.getSelectionType()), SelectionPosition::getSelectionType, dto.getSelectionType());
        wrapper.eq(StrUtil.isNotBlank(dto.getYear()), SelectionPosition::getYear, dto.getYear());
        wrapper.eq(StrUtil.isNotBlank(dto.getProvince()), SelectionPosition::getProvince, dto.getProvince());
        wrapper.eq(StrUtil.isNotBlank(dto.getEducationRequirement()), SelectionPosition::getEducationRequirement, dto.getEducationRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getDegreeRequirement()), SelectionPosition::getDegreeRequirement, dto.getDegreeRequirement());
        wrapper.eq(StrUtil.isNotBlank(dto.getPoliticalStatus()), SelectionPosition::getPoliticalStatus, dto.getPoliticalStatus());
        if (dto.getAgeLimit() != null) {
            wrapper.eq(SelectionPosition::getAgeLimit, dto.getAgeLimit());
        }
        wrapper.eq(StrUtil.isNotBlank(dto.getPositionStatus()), SelectionPosition::getPositionStatus, dto.getPositionStatus());

        wrapper.orderByDesc(SelectionPosition::getCreatedAt);

        Page<SelectionPosition> page = new Page<>(dto.getPage(), dto.getSize());
        selectionPositionMapper.selectPage(page, wrapper);

        return page.convert(item -> SelectionPositionListVO.builder()
                .id(item.getId())
                .positionName(item.getPositionName())
                .selectionType(item.getSelectionType())
                .year(item.getYear())
                .province(item.getProvince())
                .organizingDept(item.getOrganizingDept())
                .targetUnit(item.getTargetUnit())
                .workLocation(item.getWorkLocation())
                .trainingDirection(item.getTrainingDirection())
                .educationRequirement(item.getEducationRequirement())
                .degreeRequirement(item.getDegreeRequirement())
                .majorRequirement(item.getMajorRequirement())
                .universityRequirement(item.getUniversityRequirement())
                .politicalStatus(item.getPoliticalStatus())
                .ageLimit(item.getAgeLimit())
                .recruitmentCount(item.getRecruitmentCount())
                .regStartDate(item.getRegStartDate())
                .regEndDate(item.getRegEndDate())
                .positionStatus(item.getPositionStatus())
                .build());
    }

    @Override
    public SelectionPositionDetailVO detail(Long id) {
        SelectionPosition item = selectionPositionMapper.selectById(id);
        if (item == null || Boolean.TRUE.equals(item.getIsDeleted())) {
            log.warn("选调生岗位不存在，id={}", id);
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return SelectionPositionDetailVO.builder()
                .id(item.getId())
                .positionName(item.getPositionName())
                .selectionType(item.getSelectionType())
                .year(item.getYear())
                .province(item.getProvince())
                .organizingDept(item.getOrganizingDept())
                .targetUnit(item.getTargetUnit())
                .workLocation(item.getWorkLocation())
                .trainingDirection(item.getTrainingDirection())
                .grassrootsServiceYears(item.getGrassrootsServiceYears())
                .trainingPlan(item.getTrainingPlan())
                .educationRequirement(item.getEducationRequirement())
                .degreeRequirement(item.getDegreeRequirement())
                .majorRequirement(item.getMajorRequirement())
                .majorCategories(item.getMajorCategories())
                .universityRequirement(item.getUniversityRequirement())
                .targetUniversities(item.getTargetUniversities())
                .politicalStatus(item.getPoliticalStatus())
                .studentCadreRequirement(item.getStudentCadreRequirement())
                .awardsRequirement(item.getAwardsRequirement())
                .ageLimit(item.getAgeLimit())
                .recruitmentCount(item.getRecruitmentCount())
                .examSubjects(item.getExamSubjects())
                .interviewForm(item.getInterviewForm())
                .regStartDate(item.getRegStartDate())
                .regEndDate(item.getRegEndDate())
                .examTime(item.getExamTime())
                .applyLink(item.getApplyLink())
                .positionStatus(item.getPositionStatus())
                .remark(item.getRemark())
                .contactPhone(item.getContactPhone())
                .officialLink(item.getOfficialLink())
                .content(item.getContent())
                .build();
    }
}
```

---

### Task 8: App Controllers

**Files:**
- Create: `haifeng-app/.../controller/employment/civilService/CivilPositionController.java`
- Create: `haifeng-app/.../controller/employment/civilService/InstitutionPositionController.java`
- Create: `haifeng-app/.../controller/employment/civilService/MilitaryPositionController.java`
- Create: `haifeng-app/.../controller/employment/civilService/SelectionPositionController.java`

- [ ] **Step 1: Create CivilPositionController**

```java
package com.haifeng.app.controller.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.civilService.CivilPositionSearchDTO;
import com.haifeng.app.service.employment.civilService.CivilPositionService;
import com.haifeng.app.vo.employment.civilService.CivilPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.CivilPositionListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/app/employment/civil-service/position")
@RequiredArgsConstructor
public class CivilPositionController {

    private final CivilPositionService civilPositionService;

    @GetMapping("/list")
    public R<IPage<CivilPositionListVO>> list(@Valid CivilPositionSearchDTO dto) {
        return R.ok(civilPositionService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<CivilPositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(civilPositionService.detail(id));
    }
}
```

- [ ] **Step 2: Create InstitutionPositionController**

```java
package com.haifeng.app.controller.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.civilService.InstitutionPositionSearchDTO;
import com.haifeng.app.service.employment.civilService.InstitutionPositionService;
import com.haifeng.app.vo.employment.civilService.InstitutionPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.InstitutionPositionListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/app/employment/civil-service/institution")
@RequiredArgsConstructor
public class InstitutionPositionController {

    private final InstitutionPositionService institutionPositionService;

    @GetMapping("/list")
    public R<IPage<InstitutionPositionListVO>> list(@Valid InstitutionPositionSearchDTO dto) {
        return R.ok(institutionPositionService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<InstitutionPositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(institutionPositionService.detail(id));
    }
}
```

- [ ] **Step 3: Create MilitaryPositionController**

```java
package com.haifeng.app.controller.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.civilService.MilitaryPositionSearchDTO;
import com.haifeng.app.service.employment.civilService.MilitaryPositionService;
import com.haifeng.app.vo.employment.civilService.MilitaryPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.MilitaryPositionListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/app/employment/civil-service/military")
@RequiredArgsConstructor
public class MilitaryPositionController {

    private final MilitaryPositionService militaryPositionService;

    @GetMapping("/list")
    public R<IPage<MilitaryPositionListVO>> list(@Valid MilitaryPositionSearchDTO dto) {
        return R.ok(militaryPositionService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<MilitaryPositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(militaryPositionService.detail(id));
    }
}
```

- [ ] **Step 4: Create SelectionPositionController**

```java
package com.haifeng.app.controller.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.civilService.SelectionPositionSearchDTO;
import com.haifeng.app.service.employment.civilService.SelectionPositionService;
import com.haifeng.app.vo.employment.civilService.SelectionPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.SelectionPositionListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/app/employment/civil-service/selection")
@RequiredArgsConstructor
public class SelectionPositionController {

    private final SelectionPositionService selectionPositionService;

    @GetMapping("/list")
    public R<IPage<SelectionPositionListVO>> list(@Valid SelectionPositionSearchDTO dto) {
        return R.ok(selectionPositionService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<SelectionPositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(selectionPositionService.detail(id));
    }
}
```

---

### Task 9: Build Verification

- [ ] **Step 1: Compile haifeng-common**

Run: `mvn compile -pl haifeng-common -am`
Expected: BUILD SUCCESS

- [ ] **Step 2: Compile full project**

Run: `mvn compile -pl haifeng-app -am`
Expected: BUILD SUCCESS
