package com.haifeng.admin.dto.certificate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import lombok.Data;

@Data
public class CompetitionAddDTO {

    @NotBlank(message = "竞赛名称不能为空")
    @Size(max = 200, message = "竞赛名称最长200字符")
    private String compName;

    @Size(max = 50, message = "竞赛级别最长50字符")
    private String compLevel;

    @Size(max = 100, message = "报名时间最长100字符")
    private String registrationTime;

    @Valid
    private CompetitionDetailDTO detail;
}
