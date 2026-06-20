package com.haifeng.app.vo.employment.civilService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionPositionDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String positionName;
    private String supervisingDept;
    private String institution;
    private String workLocation;
    private String province;
    private String examCategory;
    private String positionType;
    private String subCategory;
    private String educationRequirement;
    private String degreeRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    private String salaryRange;
    private String regDeadline;
    private String[] majorRequirements;
    private String specialPosition;
    private String otherRequirement;
    private String otherRequirementDesc;
    private String remarkType;
    private String remarkDesc;
    private String consultationPhone;
    private String supervisionPhone;
    private String positionStatus;
    private String positionTag;
    private String tagText;
}
