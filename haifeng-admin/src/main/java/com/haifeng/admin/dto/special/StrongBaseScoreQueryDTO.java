package com.haifeng.admin.dto.special;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StrongBaseScoreQueryDTO extends BasePageQueryDTO {
    private String universityName;
    private Short year;
    private String province;
    private String subjectType;
}
