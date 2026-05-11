package com.haifeng.admin.vo.company;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EnterpriseListVO {
    private Long id;
    private String cityName;
    private String enterpriseName;
    private String enterpriseNature;
    private String enterpriseType;
    private String recruitmentStatus;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
}
