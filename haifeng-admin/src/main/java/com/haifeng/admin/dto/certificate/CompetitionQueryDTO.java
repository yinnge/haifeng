package com.haifeng.admin.dto.certificate;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionQueryDTO extends BasePageQueryDTO {
    private String compName;
    private String compLevel;
    private Boolean isDeleted;
}
