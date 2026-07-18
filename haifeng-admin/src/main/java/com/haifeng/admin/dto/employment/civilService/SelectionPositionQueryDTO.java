package com.haifeng.admin.dto.employment.civilService;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SelectionPositionQueryDTO extends BasePageQueryDTO {
    @Size(max = 50)
    private String positionName;

    @Size(max = 50)
    private String targetUnit;

    @Size(max = 50)
    private String organizingDept;

    @Size(max = 50)
    private String selectionType;

    @Size(max = 10)
    private String year;

    @Size(max = 30)
    private String province;

    @Size(max = 30)
    private String politicalStatus;

    @Size(max = 20)
    private String positionStatus;
}
