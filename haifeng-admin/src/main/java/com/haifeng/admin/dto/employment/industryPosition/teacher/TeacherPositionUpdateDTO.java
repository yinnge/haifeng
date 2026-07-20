package com.haifeng.admin.dto.employment.industryPosition.teacher;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class TeacherPositionUpdateDTO {
    @Size(max = 200, message = "学校名称最长200字符")
    private String schoolName;
    @Size(max = 30, message = "学校类型最长30字符")
    private String schoolType;
    @Size(max = 20, message = "学校性质最长20字符")
    private String schoolNature;
    @Size(max = 200, message = "主管教育部门最长200字符")
    private String supervisingDept;
    @Size(max = 200, message = "岗位名称最长200字符")
    private String positionName;
    @Size(max = 50, message = "学科最长50字符")
    private String subject;
    @Size(max = 30, message = "招聘类型最长30字符")
    private String recruitmentType;
    @Size(max = 30, message = "省份最长30字符")
    private String province;
    @Size(max = 50, message = "城市最长50字符")
    private String city;
    @Size(max = 50, message = "区/县最长50字符")
    private String district;
    @Size(max = 30, message = "学历要求最长30字符")
    private String educationRequirement;
    @Size(max = 30, message = "学位要求最长30字符")
    private String degreeRequirement;
    @Size(max = 500, message = "专业要求最长500字符")
    private String majorRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    @Size(max = 100, message = "教师资格证要求最长100字符")
    private String teacherCertRequirement;
    @Size(max = 50, message = "资格证学科要求最长50字符")
    private String teacherCertSubject;
    @Size(max = 30, message = "普通话等级最长30字符")
    private String putonghuaLevel;
    @Size(max = 200, message = "其他证书要求最长200字符")
    private String otherCertRequirement;
    @Size(max = 50, message = "教学经验要求最长50字符")
    private String workExperience;
    @Size(max = 20, message = "是否要求师范专业最长20字符")
    private String isNormalMajor;
    @Size(max = 50, message = "薪资待遇最长50字符")
    private String salaryRange;
    private String benefits;
    @Size(max = 500, message = "笔试内容最长500字符")
    private String examContent;
    @Size(max = 100, message = "面试形式最长100字符")
    private String interviewForm;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private OffsetDateTime examTime;
    @Size(max = 20, message = "岗位状态最长20字符")
    private String positionStatus;
    @Size(max = 500, message = "报名链接最长500字符")
    private String applyLink;
    @Size(max = 50, message = "联系电话最长50字符")
    private String contactPhone;
    private String remark;
    private String content;
    private Integer sortOrder;
}
