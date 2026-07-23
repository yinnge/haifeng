package com.haifeng.admin.vo.employment.industryPosition.teacher;

import lombok.Data;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class TeacherPositionDetailVO {
    @JsonSerialize(using = ToStringSerializer.class)
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
