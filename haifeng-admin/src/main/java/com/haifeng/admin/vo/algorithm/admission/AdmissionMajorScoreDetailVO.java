package com.haifeng.admin.vo.algorithm.admission;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class AdmissionMajorScoreDetailVO {
    private Integer id;
    private Integer groupId;
    private Long majorId;
    private String majorCode;
    private String majorName;
    private String educationLevel;
    private String duration;
    private String tuition;
    private String description;
    private List<Object> history;
    private List<String> constraints;
    private Boolean isDeleted;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
