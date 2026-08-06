package com.haifeng.admin.dto.certificate;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompetitionMajorAddDTO {

    @NotNull(message = "竞赛不能为空")
    private Long competitionId;

    @NotNull(message = "专业不能为空")
    private Long majorId;
}
