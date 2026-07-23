package com.haifeng.admin.vo.company;

import lombok.Data;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class EnterpriseListVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String cityName;
    private String enterpriseName;
    private String enterpriseNature;
    private String enterpriseType;
    private String recruitmentStatus;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
}
