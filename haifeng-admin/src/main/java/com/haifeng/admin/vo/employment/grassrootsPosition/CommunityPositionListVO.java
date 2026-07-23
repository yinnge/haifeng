package com.haifeng.admin.vo.employment.grassrootsPosition;

import lombok.Data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class CommunityPositionListVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String communityName;
    private String positionName;
    private String supervisingDept;
    private String positionType;
    private String province;
    private String city;
    private String positionStatus;
}
