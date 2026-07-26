package com.haifeng.admin.vo.major;

import lombok.Data;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class MajorPostgradDirectionDetailVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private Long majorId;

    private Long postgradMajorId;

    private String majorName;

    private String postgradMajorName;

    private Integer sortOrder;

    private OffsetDateTime createdAt;
}
