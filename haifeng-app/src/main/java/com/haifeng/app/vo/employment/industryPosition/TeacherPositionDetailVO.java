package com.haifeng.app.vo.employment.industryPosition;

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
public class TeacherPositionDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

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
}
