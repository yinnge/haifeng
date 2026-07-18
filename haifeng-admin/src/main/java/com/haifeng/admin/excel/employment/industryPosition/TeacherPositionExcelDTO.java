package com.haifeng.admin.excel.employment.industryPosition;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.converter.OffsetDateTimeConverter;
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
    @ExcelProperty(value = "报名开始日期", converter = OffsetDateTimeConverter.class) private OffsetDateTime regStartDate;
    @ExcelProperty(value = "报名截止日期", converter = OffsetDateTimeConverter.class) private OffsetDateTime regEndDate;
    @ExcelProperty(value = "考试时间", converter = OffsetDateTimeConverter.class) private OffsetDateTime examTime;
    @ExcelProperty("状态") private String positionStatus;
    @ExcelProperty("报名链接") private String applyLink;
    @ExcelProperty("联系电话") private String contactPhone;
    @ExcelProperty("备注") private String remark;
    @ExcelProperty("详细说明") private String content;
    @ExcelProperty("排序") private Integer sortOrder;
}
