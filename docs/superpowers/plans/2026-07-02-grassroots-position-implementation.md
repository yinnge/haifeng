# 基层服务岗位管理 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement step-by-step.

**Goal:** 在admin端实现三个基层服务岗位管理模块（基层服务项目、社区工作者、公益性岗位），每个模块支持分页查询、详情、修改、删除、禁用、批量删除、Excel导入/校验。

**Architecture:** 三个模块分别独立 Controller/Service/ServiceImpl，共用 StatusDTO，完全复用 `industryPosition` 模块的现有模式。包路径 `grassrootsPosition`，API路径 `grassroots-position/{entity}`。

**Tech Stack:** Spring Boot 3, MyBatis-Plus, EasyExcel, SnowflakeIdGenerator

---

### Task 1: 创建公共DTO和目录结构

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/employment/grassrootsPosition/StatusDTO.java`
- Create directories (via IDE): `grassrootsPosition` under controller/service/dto/vo/excel

- [ ] **Step 1: Create StatusDTO.java**

```java
package com.haifeng.admin.dto.employment.grassrootsPosition;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusDTO {
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值不合法")
    @Max(value = 1, message = "状态值不合法")
    private Integer status;
}
```

- [ ] **Step 2: Create directory structure**

```
haifeng-admin/src/main/java/com/haifeng/admin/
  controller/employment/grassrootsPosition/
  service/employment/grassrootsPosition/
  service/impl/employment/grassrootsPosition/
  dto/employment/grassrootsPosition/
  vo/employment/grassrootsPosition/
  excel/employment/grassrootsPosition/
```

---

### Task 2: 基层服务项目 - DTO、VO、ExcelDTO

**Files:**
- Create: `haifeng-admin/.../dto/employment/grassrootsPosition/GrassrootsProjectPositionQueryDTO.java`
- Create: `haifeng-admin/.../dto/employment/grassrootsPosition/GrassrootsProjectPositionUpdateDTO.java`
- Create: `haifeng-admin/.../vo/employment/grassrootsPosition/GrassrootsProjectPositionListVO.java`
- Create: `haifeng-admin/.../vo/employment/grassrootsPosition/GrassrootsProjectPositionDetailVO.java`
- Create: `haifeng-admin/.../excel/employment/grassrootsPosition/GrassrootsProjectPositionExcelDTO.java`

- [ ] **Step 1: GrassrootsProjectPositionQueryDTO**

```java
package com.haifeng.admin.dto.employment.grassrootsPosition;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GrassrootsProjectPositionQueryDTO extends BasePageQueryDTO {
    private String positionName;
    private String organizingDept;
    private String serviceUnit;
    private String projectType;
    private String year;
    private String serviceType;
    private String province;
    private String city;
    private String county;
    private String positionStatus;
}
```

- [ ] **Step 2: GrassrootsProjectPositionUpdateDTO**

```java
package com.haifeng.admin.dto.employment.grassrootsPosition;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class GrassrootsProjectPositionUpdateDTO {
    private String projectType;
    private String year;
    private String positionName;
    private String serviceType;
    private String organizingDept;
    private String serviceUnit;
    private String province;
    private String city;
    private String county;
    private String township;
    private String servicePeriod;
    private String serviceStartDate;
    private String serviceEndDate;
    private String educationRequirement;
    private String majorRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String gradYearRequirement;
    private String householdRequirement;
    private String politicalStatus;
    private String otherRequirement;
    private String examContent;
    private OffsetDateTime examTime;
    private String interviewForm;
    private String monthlySubsidy;
    private String socialInsurance;
    private String housingInfo;
    private String otherBenefits;
    private String afterServicePolicy;
    private Boolean canTransferToCivil;
    private Boolean canTransferToInstitution;
    private String examBonusPoints;
    private String tuitionCompensation;
    private String postgradBonus;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private String applyLink;
    private String positionStatus;
    private String contactPhone;
    private String remark;
    private String content;
    private Integer sortOrder;
}
```

- [ ] **Step 3: GrassrootsProjectPositionListVO**

```java
package com.haifeng.admin.vo.employment.grassrootsPosition;

import lombok.Data;

@Data
public class GrassrootsProjectPositionListVO {
    private Long id;
    private String projectType;
    private String year;
    private String positionName;
    private String serviceType;
    private String organizingDept;
    private String serviceUnit;
    private String province;
    private String city;
    private String county;
    private String positionStatus;
}
```

- [ ] **Step 4: GrassrootsProjectPositionDetailVO**

```java
package com.haifeng.admin.vo.employment.grassrootsPosition;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class GrassrootsProjectPositionDetailVO {
    private Long id;
    private String projectType;
    private String year;
    private String positionName;
    private String serviceType;
    private String organizingDept;
    private String serviceUnit;
    private String province;
    private String city;
    private String county;
    private String township;
    private String servicePeriod;
    private String serviceStartDate;
    private String serviceEndDate;
    private String educationRequirement;
    private String majorRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String gradYearRequirement;
    private String householdRequirement;
    private String politicalStatus;
    private String otherRequirement;
    private String examContent;
    private OffsetDateTime examTime;
    private String interviewForm;
    private String monthlySubsidy;
    private String socialInsurance;
    private String housingInfo;
    private String otherBenefits;
    private String afterServicePolicy;
    private Boolean canTransferToCivil;
    private Boolean canTransferToInstitution;
    private String examBonusPoints;
    private String tuitionCompensation;
    private String postgradBonus;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private String applyLink;
    private String positionStatus;
    private String contactPhone;
    private String remark;
    private String content;
    private Integer sortOrder;
    private Boolean isDeleted;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 5: GrassrootsProjectPositionExcelDTO**

```java
package com.haifeng.admin.excel.employment.grassrootsPosition;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class GrassrootsProjectPositionExcelDTO {
    @ExcelProperty("项目类型") private String projectType;
    @ExcelProperty("年份") private String year;
    @ExcelProperty("岗位名称") private String positionName;
    @ExcelProperty("服务类型") private String serviceType;
    @ExcelProperty("组织单位") private String organizingDept;
    @ExcelProperty("服务单位") private String serviceUnit;
    @ExcelProperty("省份") private String province;
    @ExcelProperty("城市") private String city;
    @ExcelProperty("区/县") private String county;
    @ExcelProperty("乡镇/街道") private String township;
    @ExcelProperty("服务期限") private String servicePeriod;
    @ExcelProperty("服务开始日期") private String serviceStartDate;
    @ExcelProperty("服务结束日期") private String serviceEndDate;
    @ExcelProperty("学历要求") private String educationRequirement;
    @ExcelProperty("专业要求") private String majorRequirement;
    @ExcelProperty("年龄上限") private Integer ageLimit;
    @ExcelProperty("招募人数") private Integer recruitmentCount;
    @ExcelProperty("毕业年份要求") private String gradYearRequirement;
    @ExcelProperty("户籍要求") private String householdRequirement;
    @ExcelProperty("政治面貌") private String politicalStatus;
    @ExcelProperty("其他要求") private String otherRequirement;
    @ExcelProperty("笔试内容") private String examContent;
    @ExcelProperty("考试时间") private OffsetDateTime examTime;
    @ExcelProperty("面试形式") private String interviewForm;
    @ExcelProperty("月补贴标准") private String monthlySubsidy;
    @ExcelProperty("社保缴纳") private String socialInsurance;
    @ExcelProperty("住房安排") private String housingInfo;
    @ExcelProperty("其他待遇") private String otherBenefits;
    @ExcelProperty("期满政策") private String afterServicePolicy;
    @ExcelProperty("可定向考公") private Boolean canTransferToCivil;
    @ExcelProperty("可转事业编") private Boolean canTransferToInstitution;
    @ExcelProperty("考试加分") private String examBonusPoints;
    @ExcelProperty("学费补偿") private String tuitionCompensation;
    @ExcelProperty("考研加分") private String postgradBonus;
    @ExcelProperty("报名开始") private OffsetDateTime regStartDate;
    @ExcelProperty("报名截止") private OffsetDateTime regEndDate;
    @ExcelProperty("报名链接") private String applyLink;
    @ExcelProperty("状态") private String positionStatus;
    @ExcelProperty("联系电话") private String contactPhone;
    @ExcelProperty("备注") private String remark;
    @ExcelProperty("详细说明") private String content;
    @ExcelProperty("排序") private Integer sortOrder;
}
```

---

### Task 3: 基层服务项目 - Service接口 + ServiceImpl

**Files:**
- Create: `haifeng-admin/.../service/employment/grassrootsPosition/GrassrootsProjectPositionService.java`
- Create: `haifeng-admin/.../service/impl/employment/grassrootsPosition/GrassrootsProjectPositionServiceImpl.java`

- [ ] **Step 1: GrassrootsProjectPositionService.java**

```java
package com.haifeng.admin.service.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.grassrootsPosition.GrassrootsProjectPositionQueryDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.GrassrootsProjectPositionUpdateDTO;
import com.haifeng.admin.vo.employment.grassrootsPosition.GrassrootsProjectPositionDetailVO;
import com.haifeng.admin.vo.employment.grassrootsPosition.GrassrootsProjectPositionListVO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface GrassrootsProjectPositionService {
    IPage<GrassrootsProjectPositionListVO> page(GrassrootsProjectPositionQueryDTO dto);
    GrassrootsProjectPositionDetailVO detail(Long id);
    void update(Long id, GrassrootsProjectPositionUpdateDTO dto);
    void delete(Long id);
    void updateStatus(Long id, Integer status);
    void batchDelete(List<Long> ids);
    String preValidate(MultipartFile file);
    void importExcel(MultipartFile file);
}
```

- [ ] **Step 2: GrassrootsProjectPositionServiceImpl.java**

```java
package com.haifeng.admin.service.impl.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.employment.grassrootsPosition.GrassrootsProjectPositionQueryDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.GrassrootsProjectPositionUpdateDTO;
import com.haifeng.admin.excel.employment.grassrootsPosition.GrassrootsProjectPositionExcelDTO;
import com.haifeng.admin.service.employment.grassrootsPosition.GrassrootsProjectPositionService;
import com.haifeng.admin.vo.employment.grassrootsPosition.GrassrootsProjectPositionDetailVO;
import com.haifeng.admin.vo.employment.grassrootsPosition.GrassrootsProjectPositionListVO;
import com.haifeng.common.entity.employment.grassrootsPosition.GrassrootsProjectPosition;
import com.haifeng.common.enums.ProvinceEnum;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.grassrootsPosition.GrassrootsProjectPositionMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import com.alibaba.excel.EasyExcel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrassrootsProjectPositionServiceImpl implements GrassrootsProjectPositionService {

    private final GrassrootsProjectPositionMapper grassrootsProjectPositionMapper;

    @Override
    public IPage<GrassrootsProjectPositionListVO> page(GrassrootsProjectPositionQueryDTO dto) {
        Page<GrassrootsProjectPosition> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<GrassrootsProjectPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GrassrootsProjectPosition::getIsDeleted, false);

        if (StringUtils.hasText(dto.getPositionName())) {
            wrapper.like(GrassrootsProjectPosition::getPositionName, dto.getPositionName());
        }
        if (StringUtils.hasText(dto.getOrganizingDept())) {
            wrapper.like(GrassrootsProjectPosition::getOrganizingDept, dto.getOrganizingDept());
        }
        if (StringUtils.hasText(dto.getServiceUnit())) {
            wrapper.like(GrassrootsProjectPosition::getServiceUnit, dto.getServiceUnit());
        }
        if (StringUtils.hasText(dto.getProjectType())) {
            wrapper.eq(GrassrootsProjectPosition::getProjectType, dto.getProjectType());
        }
        if (StringUtils.hasText(dto.getYear())) {
            wrapper.eq(GrassrootsProjectPosition::getYear, dto.getYear());
        }
        if (StringUtils.hasText(dto.getServiceType())) {
            wrapper.eq(GrassrootsProjectPosition::getServiceType, dto.getServiceType());
        }
        if (StringUtils.hasText(dto.getProvince())) {
            wrapper.eq(GrassrootsProjectPosition::getProvince, dto.getProvince());
        }
        if (StringUtils.hasText(dto.getCity())) {
            wrapper.eq(GrassrootsProjectPosition::getCity, dto.getCity());
        }
        if (StringUtils.hasText(dto.getCounty())) {
            wrapper.eq(GrassrootsProjectPosition::getCounty, dto.getCounty());
        }
        if (StringUtils.hasText(dto.getPositionStatus())) {
            wrapper.eq(GrassrootsProjectPosition::getPositionStatus, dto.getPositionStatus());
        }

        wrapper.orderByAsc(GrassrootsProjectPosition::getSortOrder).orderByDesc(GrassrootsProjectPosition::getUpdatedAt);

        IPage<GrassrootsProjectPosition> entityPage = grassrootsProjectPositionMapper.selectPage(page, wrapper);

        return entityPage.convert(entity -> {
            GrassrootsProjectPositionListVO vo = new GrassrootsProjectPositionListVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        });
    }

    @Override
    public GrassrootsProjectPositionDetailVO detail(Long id) {
        GrassrootsProjectPosition entity = grassrootsProjectPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "基层服务项目岗位不存在");
        }
        GrassrootsProjectPositionDetailVO vo = new GrassrootsProjectPositionDetailVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public void update(Long id, GrassrootsProjectPositionUpdateDTO dto) {
        GrassrootsProjectPosition entity = grassrootsProjectPositionMapper.selectById(id);
        if (entity == null || entity.getIsDeleted()) {
            throw new BusinessException(404, "基层服务项目岗位不存在");
        }
        BeanUtils.copyProperties(dto, entity);
        entity.setUpdatedAt(OffsetDateTime.now());
        grassrootsProjectPositionMapper.updateById(entity);
        log.info("更新基层服务项目岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        GrassrootsProjectPosition entity = grassrootsProjectPositionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "基层服务项目岗位不存在");
        }
        grassrootsProjectPositionMapper.deleteById(id);
        log.info("硬删除基层服务项目岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        GrassrootsProjectPosition entity = grassrootsProjectPositionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "基层服务项目岗位不存在");
        }
        entity.setIsDeleted(status == 0);
        entity.setUpdatedAt(OffsetDateTime.now());
        grassrootsProjectPositionMapper.updateById(entity);
        log.info("更新基层服务项目岗位状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        int successCount = 0;
        for (Long id : ids) {
            GrassrootsProjectPosition entity = grassrootsProjectPositionMapper.selectById(id);
            if (entity != null) {
                grassrootsProjectPositionMapper.deleteById(id);
                successCount++;
            }
        }
        log.info("批量删除基层服务项目岗位成功: total={}, success={}", ids.size(), successCount);
    }

    @Override
    public String preValidate(MultipartFile file) {
        List<GrassrootsProjectPositionExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        for (GrassrootsProjectPositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getProjectType())) {
                errors.add("项目类型不能为空");
            }
            if (!StringUtils.hasText(dto.getYear())) {
                errors.add("年份不能为空");
            }
            if (!StringUtils.hasText(dto.getPositionName())) {
                errors.add("岗位名称不能为空");
            }
            if (!StringUtils.hasText(dto.getServiceType())) {
                errors.add("服务类型不能为空");
            }
            if (!StringUtils.hasText(dto.getProvince())) {
                errors.add("省份不能为空");
            } else if (!ProvinceEnum.isValid(dto.getProvince())) {
                errors.add("省份不合法: " + dto.getProvince());
            }
            if (!StringUtils.hasText(dto.getEducationRequirement())) {
                errors.add("学历要求不能为空");
            }
            if (!errors.isEmpty()) {
                errorMsg.append("第").append(row).append("行: ").append(String.join("; ", errors)).append("\n");
            }
        }
        return errorMsg.length() > 0 ? errorMsg.toString() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importExcel(MultipartFile file) {
        List<GrassrootsProjectPositionExcelDTO> list = readExcel(file);
        StringBuilder errorMsg = new StringBuilder();
        int row = 1;
        for (GrassrootsProjectPositionExcelDTO dto : list) {
            row++;
            List<String> errors = new ArrayList<>();
            if (!StringUtils.hasText(dto.getProjectType())) errors.add("项目类型不能为空");
            if (!StringUtils.hasText(dto.getYear())) errors.add("年份不能为空");
            if (!StringUtils.hasText(dto.getPositionName())) errors.add("岗位名称不能为空");
            if (!StringUtils.hasText(dto.getServiceType())) errors.add("服务类型不能为空");
            if (!StringUtils.hasText(dto.getProvince())) errors.add("省份不能为空");
            else if (!ProvinceEnum.isValid(dto.getProvince())) errors.add("省份不合法: " + dto.getProvince());
            if (!StringUtils.hasText(dto.getEducationRequirement())) errors.add("学历要求不能为空");
            if (!errors.isEmpty()) {
                errorMsg.append("第").append(row).append("行: ").append(String.join("; ", errors)).append("\n");
            }
        }
        if (errorMsg.length() > 0) {
            throw new BusinessException(400, errorMsg.toString());
        }

        OffsetDateTime now = OffsetDateTime.now();
        for (GrassrootsProjectPositionExcelDTO dto : list) {
            GrassrootsProjectPosition entity = GrassrootsProjectPosition.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .projectType(dto.getProjectType())
                    .year(dto.getYear())
                    .positionName(dto.getPositionName())
                    .serviceType(dto.getServiceType())
                    .organizingDept(dto.getOrganizingDept())
                    .serviceUnit(dto.getServiceUnit())
                    .province(dto.getProvince())
                    .city(dto.getCity())
                    .county(dto.getCounty())
                    .township(dto.getTownship())
                    .servicePeriod(dto.getServicePeriod())
                    .serviceStartDate(dto.getServiceStartDate())
                    .serviceEndDate(dto.getServiceEndDate())
                    .educationRequirement(dto.getEducationRequirement())
                    .majorRequirement(dto.getMajorRequirement())
                    .ageLimit(dto.getAgeLimit())
                    .recruitmentCount(dto.getRecruitmentCount())
                    .gradYearRequirement(dto.getGradYearRequirement())
                    .householdRequirement(dto.getHouseholdRequirement())
                    .politicalStatus(dto.getPoliticalStatus())
                    .otherRequirement(dto.getOtherRequirement())
                    .examContent(dto.getExamContent())
                    .examTime(dto.getExamTime())
                    .interviewForm(dto.getInterviewForm())
                    .monthlySubsidy(dto.getMonthlySubsidy())
                    .socialInsurance(dto.getSocialInsurance())
                    .housingInfo(dto.getHousingInfo())
                    .otherBenefits(dto.getOtherBenefits())
                    .afterServicePolicy(dto.getAfterServicePolicy())
                    .canTransferToCivil(dto.getCanTransferToCivil())
                    .canTransferToInstitution(dto.getCanTransferToInstitution())
                    .examBonusPoints(dto.getExamBonusPoints())
                    .tuitionCompensation(dto.getTuitionCompensation())
                    .postgradBonus(dto.getPostgradBonus())
                    .regStartDate(dto.getRegStartDate())
                    .regEndDate(dto.getRegEndDate())
                    .applyLink(dto.getApplyLink())
                    .positionStatus(dto.getPositionStatus())
                    .contactPhone(dto.getContactPhone())
                    .remark(dto.getRemark())
                    .content(dto.getContent())
                    .sortOrder(dto.getSortOrder())
                    .isDeleted(false)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            grassrootsProjectPositionMapper.insert(entity);
        }
        log.info("导入基层服务项目岗位成功: count={}", list.size());
    }

    private List<GrassrootsProjectPositionExcelDTO> readExcel(MultipartFile file) {
        try {
            return EasyExcel.read(file.getInputStream())
                    .head(GrassrootsProjectPositionExcelDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel失败", e);
            throw new BusinessException(400, "读取Excel文件失败");
        }
    }
}
```

---

### Task 4: 基层服务项目 - Controller

**Files:**
- Create: `haifeng-admin/.../controller/employment/grassrootsPosition/GrassrootsProjectPositionController.java`

- [ ] **Step 1: GrassrootsProjectPositionController.java**

```java
package com.haifeng.admin.controller.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.grassrootsPosition.GrassrootsProjectPositionQueryDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.GrassrootsProjectPositionUpdateDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.StatusDTO;
import com.haifeng.admin.service.employment.grassrootsPosition.GrassrootsProjectPositionService;
import com.haifeng.admin.vo.employment.grassrootsPosition.GrassrootsProjectPositionDetailVO;
import com.haifeng.admin.vo.employment.grassrootsPosition.GrassrootsProjectPositionListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RequireLogin
@RestController
@RequestMapping("/api/v1/admin/employment/grassroots-position/project")
@RequiredArgsConstructor
public class GrassrootsProjectPositionController {

    private final GrassrootsProjectPositionService grassrootsProjectPositionService;

    @GetMapping("/list")
    public R<IPage<GrassrootsProjectPositionListVO>> list(@Valid GrassrootsProjectPositionQueryDTO dto) {
        return R.ok(grassrootsProjectPositionService.page(dto));
    }

    @GetMapping("/{id}/detail")
    public R<GrassrootsProjectPositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(grassrootsProjectPositionService.detail(id));
    }

    @PutMapping("/{id}/update")
    @OperationLog(module = "基层服务管理", action = "修改基层服务项目岗位")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody GrassrootsProjectPositionUpdateDTO dto) {
        grassrootsProjectPositionService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}/delete")
    @OperationLog(module = "基层服务管理", action = "删除基层服务项目岗位")
    public R<Void> delete(@PathVariable Long id) {
        grassrootsProjectPositionService.delete(id);
        return R.ok();
    }

    @PatchMapping("/{id}/status")
    @OperationLog(module = "基层服务管理", action = "启用/禁用基层服务项目岗位")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        grassrootsProjectPositionService.updateStatus(id, dto.getStatus());
        return R.ok();
    }

    @DeleteMapping("/batch-delete")
    @OperationLog(module = "基层服务管理", action = "批量删除基层服务项目岗位")
    public R<Void> batchDelete(@Valid @RequestBody List<Long> ids) {
        grassrootsProjectPositionService.batchDelete(ids);
        return R.ok();
    }

    @PostMapping("/pre-validate")
    public R<String> preValidate(@RequestParam("file") MultipartFile file) {
        String result = grassrootsProjectPositionService.preValidate(file);
        return result == null ? R.ok("校验通过") : R.ok(result);
    }

    @PostMapping("/import")
    @OperationLog(module = "基层服务管理", action = "导入基层服务项目岗位")
    public R<Void> importExcel(@RequestParam("file") MultipartFile file) {
        grassrootsProjectPositionService.importExcel(file);
        return R.ok();
    }
}
```

---

### Task 5: 社区工作者 - DTO、VO、ExcelDTO

**Files:**
- Create: `haifeng-admin/.../dto/employment/grassrootsPosition/CommunityPositionQueryDTO.java`
- Create: `haifeng-admin/.../dto/employment/grassrootsPosition/CommunityPositionUpdateDTO.java`
- Create: `haifeng-admin/.../vo/employment/grassrootsPosition/CommunityPositionListVO.java`
- Create: `haifeng-admin/.../vo/employment/grassrootsPosition/CommunityPositionDetailVO.java`
- Create: `haifeng-admin/.../excel/employment/grassrootsPosition/CommunityPositionExcelDTO.java`

- [ ] **Step 1: CommunityPositionQueryDTO.java**

```java
package com.haifeng.admin.dto.employment.grassrootsPosition;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommunityPositionQueryDTO extends BasePageQueryDTO {
    private String positionName;
    private String communityName;
    private String supervisingDept;
    private String positionType;
    private String province;
    private String city;
    private String positionStatus;
}
```

- [ ] **Step 2: CommunityPositionUpdateDTO.java**

```java
package com.haifeng.admin.dto.employment.grassrootsPosition;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class CommunityPositionUpdateDTO {
    private String streetOffice;
    private String communityName;
    private String supervisingDept;
    private String district;
    private String positionName;
    private String positionType;
    private String employmentType;
    private String province;
    private String city;
    private String workLocation;
    private String educationRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String majorRequirement;
    private String householdRequirement;
    private String politicalStatus;
    private String workExperience;
    private String socialWorkCert;
    private String communityExperience;
    private String residenceRequirement;
    private String salaryRange;
    private String salaryComposition;
    private String benefits;
    private String examContent;
    private String interviewForm;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private OffsetDateTime examTime;
    private String positionStatus;
    private String applyLink;
    private String applyMethod;
    private String contactPhone;
    private String contactAddress;
    private String remark;
    private String content;
    private Integer sortOrder;
}
```

- [ ] **Step 3: CommunityPositionListVO.java**

```java
package com.haifeng.admin.vo.employment.grassrootsPosition;

import lombok.Data;

@Data
public class CommunityPositionListVO {
    private Long id;
    private String communityName;
    private String positionName;
    private String supervisingDept;
    private String positionType;
    private String province;
    private String city;
    private String positionStatus;
}
```

- [ ] **Step 4: CommunityPositionDetailVO.java**

```java
package com.haifeng.admin.vo.employment.grassrootsPosition;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class CommunityPositionDetailVO {
    private Long id;
    private String streetOffice;
    private String communityName;
    private String supervisingDept;
    private String district;
    private String positionName;
    private String positionType;
    private String employmentType;
    private String province;
    private String city;
    private String workLocation;
    private String educationRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String majorRequirement;
    private String householdRequirement;
    private String politicalStatus;
    private String workExperience;
    private String socialWorkCert;
    private String communityExperience;
    private String residenceRequirement;
    private String salaryRange;
    private String salaryComposition;
    private String benefits;
    private String examContent;
    private String interviewForm;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private OffsetDateTime examTime;
    private String positionStatus;
    private String applyLink;
    private String applyMethod;
    private String contactPhone;
    private String contactAddress;
    private String remark;
    private String content;
    private Integer sortOrder;
    private Boolean isDeleted;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 5: CommunityPositionExcelDTO.java**

```java
package com.haifeng.admin.excel.employment.grassrootsPosition;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class CommunityPositionExcelDTO {
    @ExcelProperty("街道办事处/乡镇") private String streetOffice;
    @ExcelProperty("社区名称") private String communityName;
    @ExcelProperty("主管部门") private String supervisingDept;
    @ExcelProperty("区/县") private String district;
    @ExcelProperty("岗位名称") private String positionName;
    @ExcelProperty("岗位类型") private String positionType;
    @ExcelProperty("用工形式") private String employmentType;
    @ExcelProperty("省份") private String province;
    @ExcelProperty("城市") private String city;
    @ExcelProperty("工作地点") private String workLocation;
    @ExcelProperty("学历要求") private String educationRequirement;
    @ExcelProperty("年龄上限") private Integer ageLimit;
    @ExcelProperty("招聘人数") private Integer recruitmentCount;
    @ExcelProperty("专业要求") private String majorRequirement;
    @ExcelProperty("户籍要求") private String householdRequirement;
    @ExcelProperty("政治面貌") private String politicalStatus;
    @ExcelProperty("工作经验") private String workExperience;
    @ExcelProperty("社工证要求") private String socialWorkCert;
    @ExcelProperty("社区经验要求") private String communityExperience;
    @ExcelProperty("居住地要求") private String residenceRequirement;
    @ExcelProperty("薪资待遇") private String salaryRange;
    @ExcelProperty("薪资构成") private String salaryComposition;
    @ExcelProperty("福利待遇") private String benefits;
    @ExcelProperty("笔试内容") private String examContent;
    @ExcelProperty("面试形式") private String interviewForm;
    @ExcelProperty("报名开始") private OffsetDateTime regStartDate;
    @ExcelProperty("报名截止") private OffsetDateTime regEndDate;
    @ExcelProperty("考试时间") private OffsetDateTime examTime;
    @ExcelProperty("状态") private String positionStatus;
    @ExcelProperty("报名链接") private String applyLink;
    @ExcelProperty("报名方式") private String applyMethod;
    @ExcelProperty("联系电话") private String contactPhone;
    @ExcelProperty("报名地址") private String contactAddress;
    @ExcelProperty("备注") private String remark;
    @ExcelProperty("详细说明") private String content;
    @ExcelProperty("排序") private Integer sortOrder;
}
```

---

### Task 6: 社区工作者 - Service 接口 + ServiceImpl

**Files:**
- Create: `haifeng-admin/.../service/employment/grassrootsPosition/CommunityPositionService.java`
- Create: `haifeng-admin/.../service/impl/employment/grassrootsPosition/CommunityPositionServiceImpl.java`

- [ ] **Step 1: CommunityPositionService.java**

```java
package com.haifeng.admin.service.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.grassrootsPosition.CommunityPositionQueryDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.CommunityPositionUpdateDTO;
import com.haifeng.admin.vo.employment.grassrootsPosition.CommunityPositionDetailVO;
import com.haifeng.admin.vo.employment.grassrootsPosition.CommunityPositionListVO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface CommunityPositionService {
    IPage<CommunityPositionListVO> page(CommunityPositionQueryDTO dto);
    CommunityPositionDetailVO detail(Long id);
    void update(Long id, CommunityPositionUpdateDTO dto);
    void delete(Long id);
    void updateStatus(Long id, Integer status);
    void batchDelete(List<Long> ids);
    String preValidate(MultipartFile file);
    void importExcel(MultipartFile file);
}
```

- [ ] **Step 2: CommunityPositionServiceImpl.java**
(Same pattern as GrassrootsProjectPositionServiceImpl, but with CommunityPosition entity/mapper)
Key differences from reference:
- Entity: CommunityPosition
- Mapper: CommunityPositionMapper
- Exception messages: "社区工作者岗位不存在"
- Query fields: positionName(like), communityName(like), supervisingDept(like), positionType(eq), province(eq), city(eq), positionStatus(eq)
- Sort: sortOrder ASC, updatedAt DESC
- Validate: positionName, streetOffice, province required
- Excel builder fields: all fields from CommunityPositionExcelDTO

---

### Task 7: 社区工作者 - Controller

**Files:**
- Create: `haifeng-admin/.../controller/employment/grassrootsPosition/CommunityPositionController.java`

```java
package com.haifeng.admin.controller.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.grassrootsPosition.CommunityPositionQueryDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.CommunityPositionUpdateDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.StatusDTO;
import com.haifeng.admin.service.employment.grassrootsPosition.CommunityPositionService;
import com.haifeng.admin.vo.employment.grassrootsPosition.CommunityPositionDetailVO;
import com.haifeng.admin.vo.employment.grassrootsPosition.CommunityPositionListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RequireLogin
@RestController
@RequestMapping("/api/v1/admin/employment/grassroots-position/community")
@RequiredArgsConstructor
public class CommunityPositionController {

    private final CommunityPositionService communityPositionService;

    @GetMapping("/list")
    public R<IPage<CommunityPositionListVO>> list(@Valid CommunityPositionQueryDTO dto) {
        return R.ok(communityPositionService.page(dto));
    }

    @GetMapping("/{id}/detail")
    public R<CommunityPositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(communityPositionService.detail(id));
    }

    @PutMapping("/{id}/update")
    @OperationLog(module = "基层服务管理", action = "修改社区工作者岗位")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody CommunityPositionUpdateDTO dto) {
        communityPositionService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}/delete")
    @OperationLog(module = "基层服务管理", action = "删除社区工作者岗位")
    public R<Void> delete(@PathVariable Long id) {
        communityPositionService.delete(id);
        return R.ok();
    }

    @PatchMapping("/{id}/status")
    @OperationLog(module = "基层服务管理", action = "启用/禁用社区工作者岗位")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        communityPositionService.updateStatus(id, dto.getStatus());
        return R.ok();
    }

    @DeleteMapping("/batch-delete")
    @OperationLog(module = "基层服务管理", action = "批量删除社区工作者岗位")
    public R<Void> batchDelete(@Valid @RequestBody List<Long> ids) {
        communityPositionService.batchDelete(ids);
        return R.ok();
    }

    @PostMapping("/pre-validate")
    public R<String> preValidate(@RequestParam("file") MultipartFile file) {
        String result = communityPositionService.preValidate(file);
        return result == null ? R.ok("校验通过") : R.ok(result);
    }

    @PostMapping("/import")
    @OperationLog(module = "基层服务管理", action = "导入社区工作者岗位")
    public R<Void> importExcel(@RequestParam("file") MultipartFile file) {
        communityPositionService.importExcel(file);
        return R.ok();
    }
}
```

---

### Task 8: 公益性岗位 - DTO、VO、ExcelDTO

**Files:**
- Create: `haifeng-admin/.../dto/employment/grassrootsPosition/PublicWelfarePositionQueryDTO.java`
- Create: `haifeng-admin/.../dto/employment/grassrootsPosition/PublicWelfarePositionUpdateDTO.java`
- Create: `haifeng-admin/.../vo/employment/grassrootsPosition/PublicWelfarePositionListVO.java`
- Create: `haifeng-admin/.../vo/employment/grassrootsPosition/PublicWelfarePositionDetailVO.java`
- Create: `haifeng-admin/.../excel/employment/grassrootsPosition/PublicWelfarePositionExcelDTO.java`

- [ ] **Step 1: PublicWelfarePositionQueryDTO.java**

```java
package com.haifeng.admin.dto.employment.grassrootsPosition;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PublicWelfarePositionQueryDTO extends BasePageQueryDTO {
    private String positionName;
    private String developingUnit;
    private String employingUnit;
    private String positionCategory;
    private String province;
    private String city;
    private String district;
    private Integer maxServiceYears;
    private String positionStatus;
}
```

- [ ] **Step 2: PublicWelfarePositionUpdateDTO.java**

```java
package com.haifeng.admin.dto.employment.grassrootsPosition;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class PublicWelfarePositionUpdateDTO {
    private String developingUnit;
    private String employingUnit;
    private String positionName;
    private String positionCategory;
    private String workContent;
    private String province;
    private String city;
    private String district;
    private String workLocation;
    private List<String> targetGroup;
    private String educationRequirement;
    private String ageRange;
    private String healthRequirement;
    private Integer recruitmentCount;
    private String householdRequirement;
    private Boolean employmentDifficultyCert;
    private String otherRequirement;
    private String contractPeriod;
    private Boolean isRenewable;
    private Integer maxServiceYears;
    private String monthlySalary;
    private String salarySource;
    private String subsidyStandard;
    private String socialInsuranceInfo;
    private String otherBenefits;
    private String workSchedule;
    private Boolean isShiftWork;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private String applyMethod;
    private String applyAddress;
    private String requiredDocuments;
    private String positionStatus;
    private String contactPhone;
    private String contactPerson;
    private String remark;
    private String content;
    private Integer sortOrder;
}
```

- [ ] **Step 3: PublicWelfarePositionListVO.java**

```java
package com.haifeng.admin.vo.employment.grassrootsPosition;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class PublicWelfarePositionListVO {
    private Long id;
    private String developingUnit;
    private String employingUnit;
    private String positionName;
    private String positionCategory;
    private String province;
    private String city;
    private String district;
    private String monthlySalary;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private String positionStatus;
}
```

- [ ] **Step 4: PublicWelfarePositionDetailVO.java**

```java
package com.haifeng.admin.vo.employment.grassrootsPosition;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class PublicWelfarePositionDetailVO {
    private Long id;
    private String developingUnit;
    private String employingUnit;
    private String positionName;
    private String positionCategory;
    private String workContent;
    private String province;
    private String city;
    private String district;
    private String workLocation;
    private String[] targetGroup;
    private String educationRequirement;
    private String ageRange;
    private String healthRequirement;
    private Integer recruitmentCount;
    private String householdRequirement;
    private Boolean employmentDifficultyCert;
    private String otherRequirement;
    private String contractPeriod;
    private Boolean isRenewable;
    private Integer maxServiceYears;
    private String monthlySalary;
    private String salarySource;
    private String subsidyStandard;
    private String socialInsuranceInfo;
    private String otherBenefits;
    private String workSchedule;
    private Boolean isShiftWork;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private String applyMethod;
    private String applyAddress;
    private String requiredDocuments;
    private String positionStatus;
    private String contactPhone;
    private String contactPerson;
    private String remark;
    private String content;
    private Integer sortOrder;
    private Boolean isDeleted;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 5: PublicWelfarePositionExcelDTO.java**

```java
package com.haifeng.admin.excel.employment.grassrootsPosition;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.converter.StringToArrayConverter;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class PublicWelfarePositionExcelDTO {
    @ExcelProperty("开发单位") private String developingUnit;
    @ExcelProperty("用工单位") private String employingUnit;
    @ExcelProperty("岗位名称") private String positionName;
    @ExcelProperty("岗位类别") private String positionCategory;
    @ExcelProperty("工作内容") private String workContent;
    @ExcelProperty("省份") private String province;
    @ExcelProperty("城市") private String city;
    @ExcelProperty("区/县") private String district;
    @ExcelProperty("工作地点") private String workLocation;
    @ExcelProperty(value = "面向人群", converter = StringToArrayConverter.class) private String[] targetGroup;
    @ExcelProperty("学历要求") private String educationRequirement;
    @ExcelProperty("年龄范围") private String ageRange;
    @ExcelProperty("身体条件") private String healthRequirement;
    @ExcelProperty("招聘人数") private Integer recruitmentCount;
    @ExcelProperty("户籍要求") private String householdRequirement;
    @ExcelProperty("困难认定") private Boolean employmentDifficultyCert;
    @ExcelProperty("其他要求") private String otherRequirement;
    @ExcelProperty("合同期限") private String contractPeriod;
    @ExcelProperty("可续签") private Boolean isRenewable;
    @ExcelProperty("最长服务年限") private Integer maxServiceYears;
    @ExcelProperty("月工资") private String monthlySalary;
    @ExcelProperty("工资来源") private String salarySource;
    @ExcelProperty("补贴标准") private String subsidyStandard;
    @ExcelProperty("社保缴纳") private String socialInsuranceInfo;
    @ExcelProperty("其他福利") private String otherBenefits;
    @ExcelProperty("工作时间") private String workSchedule;
    @ExcelProperty("是否倒班") private Boolean isShiftWork;
    @ExcelProperty("报名开始") private OffsetDateTime regStartDate;
    @ExcelProperty("报名截止") private OffsetDateTime regEndDate;
    @ExcelProperty("报名方式") private String applyMethod;
    @ExcelProperty("报名地址") private String applyAddress;
    @ExcelProperty("所需材料") private String requiredDocuments;
    @ExcelProperty("状态") private String positionStatus;
    @ExcelProperty("联系电话") private String contactPhone;
    @ExcelProperty("联系人") private String contactPerson;
    @ExcelProperty("备注") private String remark;
    @ExcelProperty("详细说明") private String content;
    @ExcelProperty("排序") private Integer sortOrder;
}
```

---

### Task 9: 公益性岗位 - Service 接口 + ServiceImpl

**Files:**
- Create: `haifeng-admin/.../service/employment/grassrootsPosition/PublicWelfarePositionService.java`
- Create: `haifeng-admin/.../service/impl/employment/grassrootsPosition/PublicWelfarePositionServiceImpl.java`

- [ ] **Step 1: PublicWelfarePositionService.java**

Same pattern as GrassrootsProjectPositionService but with PublicWelfarePosition types.

- [ ] **Step 2: PublicWelfarePositionServiceImpl.java**

Key differences from reference:
- Entity: PublicWelfarePosition
- Mapper: PublicWelfarePositionMapper
- Exception messages: "公益性岗位不存在"
- Query fields: positionName(like), developingUnit(like), employingUnit(like), positionCategory(eq), province(eq), city(eq), district(eq), maxServiceYears(eq), positionStatus(eq)
- Sort: sortOrder ASC, updatedAt DESC
- Validate: positionName, developingUnit, province, positionCategory required
- Excel builder: all fields from PublicWelfarePositionExcelDTO, including targetGroup via StringToArrayConverter

---

### Task 10: 公益性岗位 - Controller（/list 公开）

**Files:**
- Create: `haifeng-admin/.../controller/employment/grassrootsPosition/PublicWelfarePositionController.java`

Key difference: `/list` does NOT have `@RequireLogin`, other endpoints do.

```java
package com.haifeng.admin.controller.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.grassrootsPosition.PublicWelfarePositionQueryDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.PublicWelfarePositionUpdateDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.StatusDTO;
import com.haifeng.admin.service.employment.grassrootsPosition.PublicWelfarePositionService;
import com.haifeng.admin.vo.employment.grassrootsPosition.PublicWelfarePositionDetailVO;
import com.haifeng.admin.vo.employment.grassrootsPosition.PublicWelfarePositionListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/employment/grassroots-position/welfare")
@RequiredArgsConstructor
public class PublicWelfarePositionController {

    private final PublicWelfarePositionService publicWelfarePositionService;

    @GetMapping("/list")
    public R<IPage<PublicWelfarePositionListVO>> list(@Valid PublicWelfarePositionQueryDTO dto) {
        return R.ok(publicWelfarePositionService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<PublicWelfarePositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(publicWelfarePositionService.detail(id));
    }

    @RequireLogin
    @PutMapping("/{id}/update")
    @OperationLog(module = "基层服务管理", action = "修改公益性岗位")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody PublicWelfarePositionUpdateDTO dto) {
        publicWelfarePositionService.update(id, dto);
        return R.ok();
    }

    @RequireLogin
    @DeleteMapping("/{id}/delete")
    @OperationLog(module = "基层服务管理", action = "删除公益性岗位")
    public R<Void> delete(@PathVariable Long id) {
        publicWelfarePositionService.delete(id);
        return R.ok();
    }

    @RequireLogin
    @PatchMapping("/{id}/status")
    @OperationLog(module = "基层服务管理", action = "启用/禁用公益性岗位")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        publicWelfarePositionService.updateStatus(id, dto.getStatus());
        return R.ok();
    }

    @RequireLogin
    @DeleteMapping("/batch-delete")
    @OperationLog(module = "基层服务管理", action = "批量删除公益性岗位")
    public R<Void> batchDelete(@Valid @RequestBody List<Long> ids) {
        publicWelfarePositionService.batchDelete(ids);
        return R.ok();
    }

    @RequireLogin
    @PostMapping("/pre-validate")
    public R<String> preValidate(@RequestParam("file") MultipartFile file) {
        String result = publicWelfarePositionService.preValidate(file);
        return result == null ? R.ok("校验通过") : R.ok(result);
    }

    @RequireLogin
    @PostMapping("/import")
    @OperationLog(module = "基层服务管理", action = "导入公益性岗位")
    public R<Void> importExcel(@RequestParam("file") MultipartFile file) {
        publicWelfarePositionService.importExcel(file);
        return R.ok();
    }
}
```

---

### Task 11: 验证编译

- [ ] **Step 1: Maven compile check**

```bash
cd D:\0code\haifeng\backend\haifeng
mvn compile -q
```
Expected: BUILD SUCCESS
