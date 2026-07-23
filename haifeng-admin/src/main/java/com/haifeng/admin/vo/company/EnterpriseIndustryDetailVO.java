package com.haifeng.admin.vo.company;

import lombok.Data;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class EnterpriseIndustryDetailVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private Long enterpriseId;
    private String enterpriseName;
    private Long industryId;
    private String industryName;
    private Boolean isPrimary;
    private Short sortOrder;
    private LocalDateTime createdAt;
}
