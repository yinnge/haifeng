package com.haifeng.admin.dto.special;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StrongBaseUnivQueryDTO extends BasePageQueryDTO {
    private String universityName;
    private Boolean isPilot;
    private Short pilotYear;
    private Boolean testBeforeScore;
}
