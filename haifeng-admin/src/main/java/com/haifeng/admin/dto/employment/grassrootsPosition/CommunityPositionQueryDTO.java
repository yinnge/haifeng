package com.haifeng.admin.dto.employment.grassrootsPosition;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommunityPositionQueryDTO extends BasePageQueryDTO {
    private String positionName;
    private String communityName;
    private String supervisingDept;
    private String positionType;
    private String province;
    private String city;
    private String positionStatus;
}
