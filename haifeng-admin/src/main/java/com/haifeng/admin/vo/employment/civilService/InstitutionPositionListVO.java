package com.haifeng.admin.vo.employment.civilService;

import lombok.Data;

@Data
public class InstitutionPositionListVO {
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
