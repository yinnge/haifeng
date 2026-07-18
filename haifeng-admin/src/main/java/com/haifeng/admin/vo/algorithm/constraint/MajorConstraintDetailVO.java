package com.haifeng.admin.vo.algorithm.constraint;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class MajorConstraintDetailVO {
    private Long id;
    private String majorCode;
    private String majorName;
    private String constraintCode;
    private String constraintName;
    private String remark;
    private Integer version;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
