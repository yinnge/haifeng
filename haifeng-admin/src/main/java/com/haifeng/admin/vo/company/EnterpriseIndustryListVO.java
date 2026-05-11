package com.haifeng.admin.vo.company;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EnterpriseIndustryListVO {
    private Long id;
    private Long enterpriseId;
    private String enterpriseName;
    private Long industryId;
    private String industryName;
    private Boolean isPrimary;
    private Short sortOrder;
    private LocalDateTime createdAt;
}
