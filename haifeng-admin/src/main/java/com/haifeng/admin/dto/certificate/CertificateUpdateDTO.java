package com.haifeng.admin.dto.certificate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

@Data
public class CertificateUpdateDTO {

    @NotNull(message = "证书ID不能为空")
    private Long id;

    @NotBlank(message = "证书名称不能为空")
    @Size(max = 150, message = "证书名称最长150字符")
    private String certName;

    @Size(max = 50, message = "分类最长50字符")
    private String category;

    @Size(max = 50, message = "等级最长50字符")
    private String certLevel;

    @Size(max = 200, message = "适用专业最长200字符")
    private String applicableMajor;

    @Size(max = 100, message = "报名时间最长100字符")
    private String registrationTime;

    @Size(max = 100, message = "考试时间最长100字符")
    private String examTime;

    @Min(value = 0, message = "考试费用不能为负数")
    private Integer examFee;

    private String certIntro;

    private List<String> examRequirements;

    private String examArrangement;

    @Size(max = 500, message = "官网链接最长500字符")
    private String officialWebsite;
}
