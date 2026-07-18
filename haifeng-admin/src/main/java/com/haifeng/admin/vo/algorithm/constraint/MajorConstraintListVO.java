package com.haifeng.admin.vo.algorithm.constraint;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class MajorConstraintListVO {
    private Long id;
    private String majorCode;
    private String majorName;
    private String constraintCode;
    private String constraintName;
    private Boolean isDeleted;
    private Integer version;
    private OffsetDateTime updatedAt;
}
