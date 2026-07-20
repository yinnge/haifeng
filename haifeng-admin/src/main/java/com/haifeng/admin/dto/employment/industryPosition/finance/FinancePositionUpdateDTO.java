package com.haifeng.admin.dto.employment.industryPosition.finance;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class FinancePositionUpdateDTO {
    @Size(max = 200, message = "机构名称最长200字符")
    private String institutionName;
    @Size(max = 30, message = "机构大类最长30字符")
    private String institutionCategory;
    @Size(max = 50, message = "机构细分类型最长50字符")
    private String institutionType;
    @Size(max = 500, message = "机构Logo最长500字符")
    private String institutionLogo;
    @Size(max = 200, message = "分支机构名称最长200字符")
    private String branchName;
    @Size(max = 200, message = "岗位名称最长200字符")
    private String positionName;
    @Size(max = 50, message = "岗位类别最长50字符")
    private String positionCategory;
    @Size(max = 30, message = "招聘类型最长30字符")
    private String recruitmentType;
    @Size(max = 30, message = "省份最长30字符")
    private String province;
    @Size(max = 50, message = "城市最长50字符")
    private String city;
    @Size(max = 200, message = "详细工作地点最长200字符")
    private String workLocation;
    private Boolean isRemote;
    @Size(max = 30, message = "学历要求最长30字符")
    private String educationRequirement;
    @Size(max = 30, message = "学位要求最长30字符")
    private String degreeRequirement;
    @Size(max = 500, message = "专业要求最长500字符")
    private String majorRequirement;
    private List<String> majorPreference;
    private Integer ageLimit;
    @Size(max = 50, message = "工作经验要求最长50字符")
    private String workExperience;
    private Integer recruitmentCount;
    private List<String> certRequirements;
    @Size(max = 100, message = "语言要求最长100字符")
    private String languageRequirement;
    @Size(max = 100, message = "计算机要求最长100字符")
    private String computerRequirement;
    private String otherRequirement;
    private Integer salaryMin;
    private Integer salaryMax;
    @Size(max = 100, message = "薪资文本说明最长100字符")
    private String salaryText;
    private String benefits;
    @Size(max = 500, message = "考试内容最长500字符")
    private String examContent;
    private OffsetDateTime examTime;
    @Size(max = 100, message = "面试轮次说明最长100字符")
    private String interviewRounds;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    @Size(max = 500, message = "网申链接最长500字符")
    private String applyLink;
    @Size(max = 20, message = "岗位状态最长20字符")
    private String positionStatus;
    @Size(max = 200, message = "联系方式最长200字符")
    private String contactInfo;
    private String remark;
    private String content;
    private Integer sortOrder;
}
