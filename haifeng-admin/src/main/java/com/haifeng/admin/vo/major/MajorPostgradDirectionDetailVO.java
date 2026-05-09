package com.haifeng.admin.vo.major;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class MajorPostgradDirectionDetailVO {

    private Long id;

    private Long majorId;

    private Long postgradMajorId;

    private String majorName;

    private String postgradMajorName;

    private Integer sortOrder;

    private OffsetDateTime createdAt;
}
