package com.haifeng.app.dto.special;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StrongBaseScoreQueryDTO extends BasePageQueryDTO {
    private Short year;
    private String province;
    private String subjectType;
    private String entryScoreType;
    private String universityName;
    private String majorName;
    private String majorCode;
}
