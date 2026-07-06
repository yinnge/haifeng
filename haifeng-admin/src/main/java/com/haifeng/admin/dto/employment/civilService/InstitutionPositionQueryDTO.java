package com.haifeng.admin.dto.employment.civilService;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class InstitutionPositionQueryDTO extends BasePageQueryDTO {
    private String positionName;
    private String supervisingDept;
    private String institution;
    private String province;
    private String examCategory;
    private String positionType;
    private String positionStatus;
}
