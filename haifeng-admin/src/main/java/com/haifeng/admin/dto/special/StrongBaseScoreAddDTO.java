package com.haifeng.admin.dto.special;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class StrongBaseScoreAddDTO {
    @NotNull(message = "大学ID不能为空")
    private Long universityId;

    @NotBlank(message = "大学名称不能为空")
    @Size(max = 50, message = "大学名称长度不能超过50")
    private String universityName;

    @NotNull(message = "年份不能为空")
    private Short year;

    @NotBlank(message = "省份不能为空")
    @Size(max = 20, message = "省份长度不能超过20")
    private String province;

    @NotBlank(message = "科类不能为空")
    @Size(max = 20, message = "科类长度不能超过20")
    private String subjectType;

    @NotBlank(message = "专业名称不能为空")
    @Size(max = 100, message = "专业名称长度不能超过100")
    private String majorName;

    @Size(max = 20, message = "专业代码长度不能超过20")
    private String majorCode;

    private BigDecimal entryScore;

    @Size(max = 30, message = "入围分数类型长度不能超过30")
    private String entryScoreType;

    @Size(max = 500, message = "入围计算公式长度不能超过500")
    private String entryFormula;

    @Size(max = 20, message = "入围比例长度不能超过20")
    private String entryRatio;

    private BigDecimal admissionScore;

    @Size(max = 500, message = "录取公式长度不能超过500")
    private String admissionFormula;

    private Integer planCount;
    private Integer admissionCount;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
