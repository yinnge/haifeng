package com.haifeng.admin.vo.employment.industryPosition.teacher;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class TeacherPositionListVO {
    private Long id;
    private String schoolName;
    private String schoolType;
    private String schoolNature;
    private String positionName;
    private String recruitmentType;
    private String province;
    private String city;
    private String district;
    private String positionStatus;
    private OffsetDateTime updatedAt;
}
