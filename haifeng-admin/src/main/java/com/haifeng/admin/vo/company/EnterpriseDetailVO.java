package com.haifeng.admin.vo.company;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EnterpriseDetailVO {
    private Long id;
    private String cityName;
    private String enterpriseName;
    private String enterpriseNature;
    private String enterpriseType;
    private String logoUrl;
    private String officialWebsite;
    private String region;
    private String enterpriseScale;
    private String mainBusiness;
    private String enterpriseIntro;
    private String recruitmentStatus;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // Tab2: 岗位列表
    private List<EnterprisePositionVO> positions;
}
