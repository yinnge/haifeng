package com.haifeng.app.vo.employment.jobIndex;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobIndexListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String categoryLabel;

    private String positionName;

    private String organizationName;

    private String city;

    private String educationRequirement;

    private String recruitmentType;

    private String salaryText;

    private String positionStatus;
}
