package com.haifeng.admin.dto.certificate;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionMajorQueryDTO extends BasePageQueryDTO {
    private Long competitionId;
    private Long majorId;
    private String competitionName;
    private String majorName;
}
