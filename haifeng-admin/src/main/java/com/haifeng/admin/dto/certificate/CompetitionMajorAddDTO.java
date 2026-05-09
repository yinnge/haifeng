package com.haifeng.admin.dto.certificate;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompetitionMajorAddDTO {

    @NotBlank(message = "竞赛名称不能为空")
    private String competitionName;

    @NotBlank(message = "专业名称不能为空")
    private String majorName;
}
