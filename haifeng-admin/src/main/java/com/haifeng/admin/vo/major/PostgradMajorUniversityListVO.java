package com.haifeng.admin.vo.major;

import lombok.Data;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class PostgradMajorUniversityListVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String universityName;

    private String postgradMajorName;

    private Integer sortOrder;

    private Integer status;

    private OffsetDateTime createdAt;
}
