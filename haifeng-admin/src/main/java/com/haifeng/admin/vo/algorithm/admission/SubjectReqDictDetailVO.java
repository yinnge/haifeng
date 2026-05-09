package com.haifeng.admin.vo.algorithm.admission;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class SubjectReqDictDetailVO {
    private Integer id;
    private String code;
    private String displayName;
    private Short requirementLevel;
    private List<String> subjects;
    private String requirementType;
    private Integer sortOrder;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
