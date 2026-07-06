package com.haifeng.admin.vo.employment.grassrootsPosition;

import lombok.Data;

@Data
public class CommunityPositionListVO {
    private Long id;
    private String communityName;
    private String positionName;
    private String supervisingDept;
    private String positionType;
    private String province;
    private String city;
    private String positionStatus;
}
