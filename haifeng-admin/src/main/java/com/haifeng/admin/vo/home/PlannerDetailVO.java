package com.haifeng.admin.vo.home;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class PlannerDetailVO {
    private Long id;
    private String name;
    private String position;
    private String region;
    private String avatar;
    private String specialty;
    private String douyinName;
    private String douyinUrl;
    private String personalDescription;
    private String experienceJob;
    private List<String> achievements;
    private List<String> expertiseAreas;
    private Integer sortOrder;
    private Short status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
