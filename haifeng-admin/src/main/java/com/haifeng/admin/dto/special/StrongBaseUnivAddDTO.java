package com.haifeng.admin.dto.special;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StrongBaseUnivAddDTO {
    @NotNull(message = "大学ID不能为空")
    private Long universityId;

    @NotBlank(message = "大学名称不能为空")
    @Size(max = 50, message = "大学名称长度不能超过50")
    private String universityName;

    private Boolean isPilot;
    private Short pilotYear;

    @Size(max = 500, message = "官方页面URL长度不能超过500")
    private String officialUrl;

    @Size(max = 500, message = "报名入口URL长度不能超过500")
    private String signupUrl;

    private Boolean testBeforeScore;

    @Size(max = 20, message = "默认入围比例长度不能超过20")
    private String defaultEntryRatio;

    @Size(max = 500, message = "默认录取公式长度不能超过500")
    private String defaultAdmissionFormula;

    @Size(max = 50, message = "可用专业数量不能超过50")
    private String[] availableMajors;

    @Size(max = 2000, message = "特殊说明长度不能超过2000")
    private String specialNotes;
}
