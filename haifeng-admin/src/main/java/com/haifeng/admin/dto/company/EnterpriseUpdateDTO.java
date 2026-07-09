package com.haifeng.admin.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EnterpriseUpdateDTO {
    private String cityName;
    @NotBlank(message = "企业名称不能为空")
    private String enterpriseName;
    @NotBlank(message = "企业性质不能为空")
    @Pattern(regexp = "央企|国企|民企|外企|合资", message = "企业性质必须是：央企、国企、民企、外企、合资")
    private String enterpriseNature;
    private String enterpriseType;
    private String logoUrl;
    private String officialWebsite;
    private String region;
    private String enterpriseScale;
    private String mainBusiness;
    private String enterpriseIntro;
    private String recruitmentStatus;
}
