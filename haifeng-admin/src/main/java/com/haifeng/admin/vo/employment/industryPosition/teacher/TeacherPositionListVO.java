package com.haifeng.admin.vo.employment.industryPosition.teacher;

import lombok.Data;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class TeacherPositionListVO {
    @JsonSerialize(using = ToStringSerializer.class)
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
