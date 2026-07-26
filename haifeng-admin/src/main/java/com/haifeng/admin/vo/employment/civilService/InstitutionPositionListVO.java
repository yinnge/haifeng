package com.haifeng.admin.vo.employment.civilService;

import lombok.Data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class InstitutionPositionListVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String positionName;
    private String supervisingDept;
    private String institution;
    private String province;
    private String examCategory;
    private String positionType;
    private String subCategory;
    private String salaryRange;
    private String positionStatus;
}
