package com.haifeng.admin.vo.algorithm.constraint;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class ConstraintDictDetailVO {
    private String code;
    private String name;
    private String category;
    private String description;
    private String severity;
    private String checkField;
    private String checkOperator;
    private String checkValue;
    private String extraField;
    private String extraOperator;
    private String extraValue;
    private Integer sortOrder;
    private Boolean isActive;
    private Integer version;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
