package com.haifeng.admin.dto.employment.civilService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InstitutionPositionUpdateDTO {
    @Size(max = 200, message = "职位名称最多200字符")
    private String positionName;

    @Size(max = 200, message = "主管部门最多200字符")
    private String supervisingDept;

    @Size(max = 200, message = "招聘单位最多200字符")
    private String institution;

    @Size(max = 100, message = "工作地点最多100字符")
    private String workLocation;

    @Size(max = 30, message = "省份最多30字符")
    private String province;

    @Size(max = 50, message = "考试类别最多50字符")
    private String examCategory;

    @Size(max = 50, message = "职位类型最多50字符")
    private String positionType;

    @Size(max = 50, message = "子类别最多50字符")
    private String subCategory;

    @Pattern(regexp = "无要求|大专|本科|硕士|博士", message = "学历要求只能是 无要求/大专/本科/硕士/博士")
    private String educationRequirement;

    @Pattern(regexp = "无要求|学士|硕士|博士", message = "学位要求只能是 无要求/学士/硕士/博士")
    private String degreeRequirement;

    @Min(value = 18, message = "年龄限制不能小于18")
    @Max(value = 65, message = "年龄限制不能大于65")
    private Integer ageLimit;

    @Min(value = 1, message = "招聘人数最少1人")
    private Integer recruitmentCount;

    @Size(max = 50, message = "薪资待遇最多50字符")
    private String salaryRange;

    @Size(max = 30, message = "报名截止日期最多30字符")
    private String regDeadline;

    private String[] majorRequirements;

    @Size(max = 100, message = "特殊岗位标记最多100字符")
    private String specialPosition;

    @Size(max = 500, message = "其他要求最多500字符")
    private String otherRequirement;

    private String otherRequirementDesc;

    @Size(max = 50, message = "备注类型最多50字符")
    private String remarkType;

    private String remarkDesc;

    @Size(max = 50, message = "咨询电话最多50字符")
    private String consultationPhone;

    @Size(max = 50, message = "监督电话最多50字符")
    private String supervisionPhone;

    @Pattern(regexp = "招聘中|已结束", message = "职位状态只能是 招聘中/已结束")
    private String positionStatus;

    @Pattern(regexp = "热门|无|急招", message = "标签只能是 热门/无/急招")
    private String positionTag;

    @Size(max = 50, message = "标签文字最多50字符")
    private String tagText;

    @Min(value = 0, message = "排序值不能为负")
    private Integer sortOrder;
}
