package com.haifeng.admin.vo.algorithm.constraint;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class ConstraintDictListVO {
    private String code;
    private String category;
    private String severity;
    private String checkField;
    private Boolean isActive;
    private Boolean isDeleted;
    private Integer version;
    private OffsetDateTime updatedAt;
}
