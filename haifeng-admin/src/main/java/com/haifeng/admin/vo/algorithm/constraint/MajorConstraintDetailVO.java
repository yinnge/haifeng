package com.haifeng.admin.vo.algorithm.constraint;

import lombok.Data;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class MajorConstraintDetailVO {
    @JsonSerialize(using = ToStringSerializer.class)
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
