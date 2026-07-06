# 行业专项招聘管理端 Implementation Plan

> **For agentic workers:** Each task creates a self-contained set of files per table. Execute tasks in order.

**Goal:** 实现 admin 端对教师招聘、医疗卫生、银行/金融三张行业招聘表的 CRUD + Excel 导入（每表7接口，共21接口）

**Architecture:** 每个表独立一套 Controller → Service → ServiceImpl → DTO → VO → ExcelDTO，遵循 haifeng-admin 已有模式（Announcement/University 参考）

**Tech Stack:** Spring Boot 3.x, MyBatis-Plus, EasyExcel, SnowflakeIdGenerator

---

### Task 1: 创建教师招聘的 DTO 文件

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/employment/industryPosition/teacher/TeacherPositionQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/employment/industryPosition/teacher/TeacherPositionAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/employment/industryPosition/teacher/TeacherPositionUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/employment/industryPosition/teacher/TeacherPositionStatusDTO.java`

- [ ] **Step 1: Create TeacherPositionQueryDTO**

`TeacherPositionQueryDTO.java` — extend BasePageQueryDTO
```java
package com.haifeng.admin.dto.employment.industryPosition.teacher;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherPositionQueryDTO extends BasePageQueryDTO {
    private String schoolName;
    private String positionName;
    private String schoolType;
    private String province;
    private String city;
    private String district;
    private String positionStatus;
}
```

- [ ] **Step 2: Create TeacherPositionAddDTO**

```java
package com.haifeng.admin.dto.employment.industryPosition.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class TeacherPositionAddDTO {
    @NotBlank private String schoolName;
    @NotBlank private String schoolType;
    private String schoolNature;
    private String supervisingDept;
    @NotBlank private String positionName;
    @NotBlank private String subject;
    @NotBlank private String recruitmentType;
    @NotBlank private String province;
    private String city;
    private String district;
    private String educationRequirement;
    private String degreeRequirement;
    private String majorRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String teacherCertRequirement;
    private String teacherCertSubject;
    private String putonghuaLevel;
    private String otherCertRequirement;
    private String workExperience;
    private String isNormalMajor;
    private String salaryRange;
    private String benefits;
    private String examContent;
    private String interviewForm;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private OffsetDateTime examTime;
    private String positionStatus;
    private String applyLink;
    private String contactPhone;
    private String remark;
    private String content;
    private Integer sortOrder;
}
```

- [ ] **Step 3: Create TeacherPositionUpdateDTO**

Same fields as AddDTO (all editable).

```java
package com.haifeng.admin.dto.employment.industryPosition.teacher;

import lombok.Data;

@Data
public class TeacherPositionUpdateDTO {
    private String schoolName;
    private String schoolType;
    private String schoolNature;
    private String supervisingDept;
    private String positionName;
    private String subject;
    private String recruitmentType;
    private String province;
    private String city;
    private String district;
    private String educationRequirement;
    private String degreeRequirement;
    private String majorRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String teacherCertRequirement;
    private String teacherCertSubject;
    private String putonghuaLevel;
    private String otherCertRequirement;
    private String workExperience;
    private String isNormalMajor;
    private String salaryRange;
    private String benefits;
    private String examContent;
    private String interviewForm;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private OffsetDateTime examTime;
    private String positionStatus;
    private String applyLink;
    private String contactPhone;
    private String remark;
    private String content;
    private Integer sortOrder;
}
```

- [ ] **Step 4: Create TeacherPositionStatusDTO**

```java
package com.haifeng.admin.dto.employment.industryPosition.teacher;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TeacherPositionStatusDTO {
    @NotNull(message = "状态不能为空")
    private Integer status;
}
```

---

### Task 2: 创建教师招聘的 VO 文件

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/employment/industryPosition/teacher/TeacherPositionListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/employment/industryPosition/teacher/TeacherPositionDetailVO.java`

- [ ] **Step 1: Create TeacherPositionListVO**

```java
package com.haifeng.admin.vo.employment.industryPosition.teacher;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class TeacherPositionListVO {
    private Long id;
    private String schoolName;
    private String schoolType;
    private String schoolNature;
    private String positionName;
    private String recruitmentType;
    private String province;
    private String city;
    private String district;
    private String positionStatus;
    private OffsetDateTime updatedAt;
}
```

- [ ] **Step 2: Create TeacherPositionDetailVO**

All business fields + audit fields.

```java
package com.haifeng.admin.vo.employment.industryPosition.teacher;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class TeacherPositionDetailVO {
    private Long id;
    private String schoolName;
    private String schoolType;
    private String schoolNature;
    private String supervisingDept;
    private String positionName;
    private String subject;
    private String recruitmentType;
    private String province;
    private String city;
    private String district;
    private String educationRequirement;
    private String degreeRequirement;
    private String majorRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String teacherCertRequirement;
    private String teacherCertSubject;
    private String putonghuaLevel;
    private String otherCertRequirement;
    private String workExperience;
    private String isNormalMajor;
    private String salaryRange;
    private String benefits;
    private String examContent;
    private String interviewForm;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private OffsetDateTime examTime;
    private String positionStatus;
    private String applyLink;
    private String contactPhone;
    private String remark;
    private String content;
    private Integer sortOrder;
    private Boolean isDeleted;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
```

---

### Task 3: 创建教师招聘 Service 接口与实现

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/employment/industryPosition/TeacherPositionService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/employment/industryPosition/TeacherPositionServiceImpl.java`

- [ ] **Step 1: Create TeacherPositionService interface**

```java
package com.haifeng.admin.service.employment.industryPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.industryPosition.teacher.TeacherPositionAddDTO;
import com.haifeng.admin.dto.employment.industryPosition.teacher.TeacherPositionQueryDTO;
import com.haifeng.admin.dto.employment.industryPosition.teacher.TeacherPositionUpdateDTO;
import com.haifeng.admin.vo.employment.industryPosition.teacher.TeacherPositionDetailVO;
import com.haifeng.admin.vo.employment.industryPosition.teacher.TeacherPositionListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TeacherPositionService {
    IPage<TeacherPositionListVO> page(TeacherPositionQueryDTO dto);
    TeacherPositionDetailVO detail(Long id);
    void update(Long id, TeacherPositionUpdateDTO dto);
    void delete(Long id);
    void updateStatus(Long id, Integer status);
    void batchDelete(List<Long> ids);
    void importExcel(MultipartFile file);
    String preValidate(MultipartFile file);
}
```

- [ ] **Step 2: Create TeacherPositionServiceImpl**

```java
package com.haifeng.admin.service.impl.employment.industryPosition;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.employment.industryPosition.teacher.TeacherPositionAddDTO;
import com.haifeng.admin.dto.employment.industryPosition.teacher.TeacherPositionQueryDTO;
import com.haifeng.admin.dto.employment.industryPosition.teacher.TeacherPositionUpdateDTO;
import com.haifeng.admin.service.employment.industryPosition.TeacherPositionService;
import com.haifeng.admin.vo.employment.industryPosition.teacher.TeacherPositionDetailVO;
import com.haifeng.admin.vo.employment.industryPosition.teacher.TeacherPositionListVO;
import com.haifeng.common.entity.employment.industryPosition.TeacherPosition;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.employment.industryPosition.TeacherPositionMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import com.alibaba.excel.EasyExcel;
import com.haifeng.admin.excel.employment.industryPosition.TeacherPositionExcelDTO;
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
public class TeacherPositionServiceImpl implements TeacherPositionService {

    private final TeacherPositionMapper teacherPositionMapper;

    @Override
    public IPage<TeacherPositionListVO> page(TeacherPositionQueryDTO dto) {
        Page<TeacherPosition> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<TeacherPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeacherPosition::getIsDeleted, false);

        if (StringUtils.hasText(dto.getSchoolName())) {
            wrapper.like(TeacherPosition::getSchoolName, dto.getSchoolName());
        }
        if (StringUtils.hasText(dto.getPositionName())) {
            wrapper.like(TeacherPosition::getPositionName, dto.getPositionName());
        }
        if (StringUtils.hasText(dto.getSchoolType())) {
            wrapper.eq(TeacherPosition::getSchoolType, dto.getSchoolType());
        }
        if (StringUtils.hasText(dto.getProvince())) {
            wrapper.eq(TeacherPosition::getProvince, dto.getProvince());
        }
        if (StringUtils.hasText(dto.getCity())) {
            wrapper.eq(TeacherPosition::getCity, dto.getCity());
        }
        if (StringUtils.hasText(dto.getDistrict())) {
            wrapper.eq(TeacherPosition::getDistrict, dto.getDistrict());
        }
        if (StringUtils.hasText(dto.getPositionStatus())) {
            wrapper.eq(TeacherPosition::getPositionStatus, dto.getPositionStatus());
        }

        wrapper.orderByDesc(TeacherPosition::getSortOrder);
        wrapper.orderByDesc(TeacherPosition::getCreatedAt);

        IPage<TeacherPosition> teacherPage = teacherPositionMapper.selectPage(page, wrapper);

        return teacherPage.convert(item -> {
            TeacherPositionListVO vo = new TeacherPositionListVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        });
    }

    @Override
    public TeacherPositionDetailVO detail(Long id) {
        TeacherPosition item = teacherPositionMapper.selectById(id);
        if (item == null || Boolean.TRUE.equals(item.getIsDeleted())) {
            throw new BusinessException(404, "教师招聘岗位不存在");
        }
        TeacherPositionDetailVO vo = new TeacherPositionDetailVO();
        BeanUtils.copyProperties(item, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, TeacherPositionUpdateDTO dto) {
        TeacherPosition item = teacherPositionMapper.selectById(id);
        if (item == null || Boolean.TRUE.equals(item.getIsDeleted())) {
            throw new BusinessException(404, "教师招聘岗位不存在");
        }

        BeanUtils.copyProperties(dto, item);
        item.setUpdatedAt(OffsetDateTime.now());
        teacherPositionMapper.updateById(item);

        log.info("更新教师招聘岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        TeacherPosition item = teacherPositionMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(404, "教师招聘岗位不存在");
        }
        teacherPositionMapper.deleteById(id);
        log.info("物理删除教师招聘岗位成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        TeacherPosition item = teacherPositionMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(404, "教师招聘岗位不存在");
        }
        item.setIsDeleted(status == 0);
        item.setUpdatedAt(OffsetDateTime.now());
        teacherPositionMapper.updateById(item);

        log.info("{}教师招聘岗位成功: id={}", status == 0 ? "禁用" : "启用", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "ID列表不能为空");
        }
        int count = 0;
        for (Long id : ids) {
            TeacherPosition item = teacherPositionMapper.selectById(id);
            if (item != null) {
                teacherPositionMapper.deleteById(id);
                count++;
            }
        }
        log.info("批量物理删除教师招聘岗位成功: 请求数量={}, 实际删除数量={}", ids.size(), count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String preValidate(MultipartFile file) {
        List<TeacherPositionExcelDTO> dataList = readExcel(file);
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            int rowNum = i + 2;
            TeacherPositionExcelDTO dto = dataList.get(i);

            if (!StringUtils.hasText(dto.getSchoolName())) {
                errors.add("第" + rowNum + "行: 学校名称不能为空");
            }
            if (!StringUtils.hasText(dto.getPositionName())) {
                errors.add("第" + rowNum + "行: 岗位名称不能为空");
            }
            // ProvinceEnum 校验
            if (StringUtils.hasText(dto.getProvince()) && !com.haifeng.common.enums.ProvinceEnum.isValid(dto.getProvince())) {
                errors.add("第" + rowNum + "行: 省份[" + dto.getProvince() + "]不在有效范围内");
            }
        }

        return errors.isEmpty() ? null : String.join("; ", errors);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importExcel(MultipartFile file) {
        String validateResult = preValidate(file);
        if (validateResult != null) {
            throw new BusinessException(400, "导入失败: " + validateResult);
        }

        List<TeacherPositionExcelDTO> dataList = readExcel(file);
        OffsetDateTime now = OffsetDateTime.now();
        int count = 0;

        for (TeacherPositionExcelDTO dto : dataList) {
            TeacherPosition item = TeacherPosition.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .schoolName(dto.getSchoolName())
                    .schoolType(dto.getSchoolType())
                    .schoolNature(dto.getSchoolNature())
                    .supervisingDept(dto.getSupervisingDept())
                    .positionName(dto.getPositionName())
                    .subject(dto.getSubject())
                    .recruitmentType(dto.getRecruitmentType())
                    .province(dto.getProvince())
                    .city(dto.getCity())
                    .district(dto.getDistrict())
                    .educationRequirement(dto.getEducationRequirement())
                    .degreeRequirement(dto.getDegreeRequirement())
                    .majorRequirement(dto.getMajorRequirement())
                    .ageLimit(dto.getAgeLimit())
                    .recruitmentCount(dto.getRecruitmentCount() != null ? dto.getRecruitmentCount() : 1)
                    .teacherCertRequirement(dto.getTeacherCertRequirement())
                    .teacherCertSubject(dto.getTeacherCertSubject())
                    .putonghuaLevel(dto.getPutonghuaLevel())
                    .otherCertRequirement(dto.getOtherCertRequirement())
                    .workExperience(dto.getWorkExperience())
                    .isNormalMajor(dto.getIsNormalMajor())
                    .salaryRange(dto.getSalaryRange())
                    .benefits(dto.getBenefits())
                    .examContent(dto.getExamContent())
                    .interviewForm(dto.getInterviewForm())
                    .regStartDate(dto.getRegStartDate())
                    .regEndDate(dto.getRegEndDate())
                    .examTime(dto.getExamTime())
                    .positionStatus(dto.getPositionStatus())
                    .applyLink(dto.getApplyLink())
                    .contactPhone(dto.getContactPhone())
                    .remark(dto.getRemark())
                    .content(dto.getContent())
                    .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                    .isDeleted(false)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            teacherPositionMapper.insert(item);
            count++;
        }

        log.info("导入教师招聘岗位成功: 共{}条", count);
    }

    private List<TeacherPositionExcelDTO> readExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传Excel文件");
        }
        try {
            return EasyExcel.read(file.getInputStream())
                    .head(TeacherPositionExcelDTO.class)
                    .sheet()
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new BusinessException(400, "读取Excel文件失败: " + e.getMessage());
        }
    }
}
```

---

### Task 4: 创建教师招聘 Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/employment/industryPosition/TeacherPositionController.java`

- [ ] **Step 1: Create TeacherPositionController**

```java
package com.haifeng.admin.controller.employment.industryPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.industryPosition.teacher.*;
import com.haifeng.admin.service.employment.industryPosition.TeacherPositionService;
import com.haifeng.admin.vo.employment.industryPosition.teacher.*;
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
@RequestMapping("/api/v1/admin/employment/industry-position/teacher")
@RequiredArgsConstructor
public class TeacherPositionController {

    private final TeacherPositionService teacherPositionService;

    @GetMapping("/list")
    public R<IPage<TeacherPositionListVO>> list(@Valid TeacherPositionQueryDTO dto) {
        return R.ok(teacherPositionService.page(dto));
    }

    @GetMapping("/{id}/detail")
    public R<TeacherPositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(teacherPositionService.detail(id));
    }

    @PutMapping("/{id}/update")
    @OperationLog(module = "行业专项招聘", action = "修改教师招聘岗位")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody TeacherPositionUpdateDTO dto) {
        teacherPositionService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}/delete")
    @OperationLog(module = "行业专项招聘", action = "删除教师招聘岗位")
    public R<Void> delete(@PathVariable Long id) {
        teacherPositionService.delete(id);
        return R.ok();
    }

    @PatchMapping("/{id}/status")
    @OperationLog(module = "行业专项招聘", action = "启用/禁用教师招聘岗位")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody TeacherPositionStatusDTO dto) {
        teacherPositionService.updateStatus(id, dto.getStatus());
        return R.ok();
    }

    @DeleteMapping("/batch-delete")
    @OperationLog(module = "行业专项招聘", action = "批量物理删除教师招聘岗位")
    public R<Void> batchDelete(@Valid @RequestBody List<Long> ids) {
        teacherPositionService.batchDelete(ids);
        return R.ok();
    }

    @PostMapping("/pre-validate")
    public R<String> preValidate(@RequestParam("file") MultipartFile file) {
        String result = teacherPositionService.preValidate(file);
        return result == null ? R.ok("校验通过") : R.ok(result);
    }

    @PostMapping("/import")
    @OperationLog(module = "行业专项招聘", action = "导入教师招聘岗位")
    public R<Void> importExcel(@RequestParam("file") MultipartFile file) {
        teacherPositionService.importExcel(file);
        return R.ok();
    }
}
```

---

### Task 5: 创建教师招聘 ExcelDTO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/employment/industryPosition/TeacherPositionExcelDTO.java`

- [ ] **Step 1: Create TeacherPositionExcelDTO**

```java
package com.haifeng.admin.excel.employment.industryPosition;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class TeacherPositionExcelDTO {
    @ExcelProperty("学校名称") private String schoolName;
    @ExcelProperty("学校类型") private String schoolType;
    @ExcelProperty("学校性质") private String schoolNature;
    @ExcelProperty("主管教育部门") private String supervisingDept;
    @ExcelProperty("岗位名称") private String positionName;
    @ExcelProperty("学科") private String subject;
    @ExcelProperty("招聘类型") private String recruitmentType;
    @ExcelProperty("省份") private String province;
    @ExcelProperty("城市") private String city;
    @ExcelProperty("区/县") private String district;
    @ExcelProperty("学历要求") private String educationRequirement;
    @ExcelProperty("学位要求") private String degreeRequirement;
    @ExcelProperty("专业要求") private String majorRequirement;
    @ExcelProperty("年龄上限") private Integer ageLimit;
    @ExcelProperty("招聘人数") private Integer recruitmentCount;
    @ExcelProperty("教师资格证要求") private String teacherCertRequirement;
    @ExcelProperty("资格证学科要求") private String teacherCertSubject;
    @ExcelProperty("普通话等级要求") private String putonghuaLevel;
    @ExcelProperty("其他证书要求") private String otherCertRequirement;
    @ExcelProperty("教学经验要求") private String workExperience;
    @ExcelProperty("是否要求师范专业") private String isNormalMajor;
    @ExcelProperty("薪资待遇") private String salaryRange;
    @ExcelProperty("福利待遇") private String benefits;
    @ExcelProperty("笔试内容") private String examContent;
    @ExcelProperty("面试形式") private String interviewForm;
    @ExcelProperty("报名开始日期") private OffsetDateTime regStartDate;
    @ExcelProperty("报名截止日期") private OffsetDateTime regEndDate;
    @ExcelProperty("考试时间") private OffsetDateTime examTime;
    @ExcelProperty("状态") private String positionStatus;
    @ExcelProperty("报名链接") private String applyLink;
    @ExcelProperty("联系电话") private String contactPhone;
    @ExcelProperty("备注") private String remark;
    @ExcelProperty("详细说明") private String content;
    @ExcelProperty("排序") private Integer sortOrder;
}
```

---

### Task 6: 创建医疗卫生的 DTO 文件

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/employment/industryPosition/healthcare/HealthcarePositionQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/employment/industryPosition/healthcare/HealthcarePositionAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/employment/industryPosition/healthcare/HealthcarePositionUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/employment/industryPosition/healthcare/HealthcarePositionStatusDTO.java`

- [ ] **Step 1: Create HealthcarePositionQueryDTO**

```java
package com.haifeng.admin.dto.employment.industryPosition.healthcare;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class HealthcarePositionQueryDTO extends BasePageQueryDTO {
    private String institutionName;
    private String positionName;
    private String institutionNature;
    private String department;
    private String province;
    private String city;
    private String district;
    private String positionStatus;
}
```

- [ ] **Step 2: Create HealthcarePositionAddDTO / UpdateDTO / StatusDTO**

Same pattern as TeacherPosition — include all entity business fields.

Use entity field names for reference:
institutionName, institutionType, institutionLevel, institutionNature, positionName, department, positionCategory, recruitmentType, province, city, district, educationRequirement, degreeRequirement, majorRequirement, ageLimit, recruitmentCount, workExperience, licenseRequirement, titleRequirement, internshipRequirement, researchRequirement, salaryRange, benefits, housingSubsidy, regStartDate, regEndDate, examTime, examContent, applyLink, positionStatus, contactPhone, contactPerson, remark, content, sortOrder

---

### Task 7: 创建医疗卫生的 VO 文件

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/employment/industryPosition/healthcare/HealthcarePositionListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/employment/industryPosition/healthcare/HealthcarePositionDetailVO.java`

ListVO fields: id, institutionName, positionName, department, positionCategory, province, city, district, positionStatus, updatedAt

DetailVO: id + all business fields + sortOrder, isDeleted, createdAt, updatedAt

---

### Task 8: 创建医疗卫生 Service 接口与实现

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/employment/industryPosition/HealthcarePositionService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/employment/industryPosition/HealthcarePositionServiceImpl.java`

Same 7-method service pattern as TeacherPosition. Entity → `HealthcarePosition`, Mapper → `HealthcarePositionMapper`.

---

### Task 9: 创建医疗卫生 Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/employment/industryPosition/HealthcarePositionController.java`

Path: `/api/v1/admin/employment/industry-position/healthcare`

Same 8-endpoint pattern as TeacherPosition (list, detail, update, delete, status, batch-delete, pre-validate, import).

---

### Task 10: 创建医疗卫生 ExcelDTO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/employment/industryPosition/HealthcarePositionExcelDTO.java`

All fields with `@ExcelProperty` annotations, matching HealthcarePosition entity fields.

---

### Task 11: 创建银行/金融的 DTO 文件

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/employment/industryPosition/finance/FinancePositionQueryDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/employment/industryPosition/finance/FinancePositionAddDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/employment/industryPosition/finance/FinancePositionUpdateDTO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/dto/employment/industryPosition/finance/FinancePositionStatusDTO.java`

QueryDTO filters: institutionName (LIKE), positionName (LIKE), institutionCategory (EQ), institutionType (EQ), province (EQ), city (EQ), positionStatus (EQ)

Note: FinancePosition has `List<String>` fields (majorPreference, certRequirements) — use the `StringListTypeHandler` in entity. For AddDTO/UpdateDTO use `List<String>`.

---

### Task 12: 创建银行/金融的 VO 文件

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/employment/industryPosition/finance/FinancePositionListVO.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/vo/employment/industryPosition/finance/FinancePositionDetailVO.java`

ListVO: id, institutionName, institutionCategory, positionName, positionCategory, recruitmentType, province, city, positionStatus, updatedAt

---

### Task 13: 创建银行/金融 Service 接口与实现

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/employment/industryPosition/FinancePositionService.java`
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/service/impl/employment/industryPosition/FinancePositionServiceImpl.java`

Same 7-method pattern. Entity → `FinancePosition`, Mapper → `FinancePositionMapper`.

---

### Task 14: 创建银行/金融 Controller

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/controller/employment/industryPosition/FinancePositionController.java`

Path: `/api/v1/admin/employment/industry-position/finance`

**Important:** Finance 的分页列表接口 **不需要登录** — 去掉 `@RequireLogin` 类级别注解，只在需要登录的接口上加 `@RequireLogin`（或者不加，按设计：仅 list 接口不要登录，其他要）。

Controller structure:
```java
@RestController
@RequestMapping("/api/v1/admin/employment/industry-position/finance")
@RequiredArgsConstructor
public class FinancePositionController {

    private final FinancePositionService financePositionService;

    @GetMapping("/list")
    public R<IPage<FinancePositionListVO>> list(@Valid FinancePositionQueryDTO dto) {
        return R.ok(financePositionService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<FinancePositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(financePositionService.detail(id));
    }
    // ... rest same as Teacher but with @RequireLogin per-method
}
```

---

### Task 15: 创建银行/金融 ExcelDTO

**Files:**
- Create: `haifeng-admin/src/main/java/com/haifeng/admin/excel/employment/industryPosition/FinancePositionExcelDTO.java`

Note: `majorPreference` and `certRequirements` are `List<String>` — need `@ExcelProperty(converter = StringArrayConverter.class)`. Import `com.haifeng.admin.excel.university.StringArrayConverter`.

---

### Task 16: 编译验证

- [ ] **Step 1: 编译项目**

Run: `mvn compile -pl haifeng-admin -am -q`
