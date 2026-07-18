package com.haifeng.admin.dto.employment.grassrootsPosition;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommunityPositionQueryDTO extends BasePageQueryDTO {
    @Size(max = 50)
    private String positionName;

    @Size(max = 50)
    private String communityName;

    @Size(max = 50)
    private String supervisingDept;

    @Size(max = 50)
    private String positionType;
    @Size(max = 30)
    private String province;
    @Size(max = 50)
    private String city;
    @Size(max = 20)
    private String positionStatus;
}
