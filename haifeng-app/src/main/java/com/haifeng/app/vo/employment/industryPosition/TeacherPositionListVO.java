package com.haifeng.app.vo.employment.industryPosition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherPositionListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String schoolName;

    private String schoolType;

    private String schoolNature;

    private String positionName;

    private String subject;

    private String recruitmentType;

    private String province;

    private String city;

    private String district;

    private String workExperience;

    private String positionStatus;
}
