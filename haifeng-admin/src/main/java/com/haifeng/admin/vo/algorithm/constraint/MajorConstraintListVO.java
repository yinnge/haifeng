package com.haifeng.admin.vo.algorithm.constraint;

import lombok.Data;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class MajorConstraintListVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String majorCode;
    private String majorName;
    private String constraintCode;
    private String constraintName;
    private Boolean isDeleted;
    private Integer version;
    private OffsetDateTime updatedAt;
}
