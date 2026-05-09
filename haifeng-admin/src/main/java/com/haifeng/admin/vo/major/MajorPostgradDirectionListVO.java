package com.haifeng.admin.vo.major;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class MajorPostgradDirectionListVO {

    private Long id;

    private String majorName;

    private String postgradMajorName;

    private OffsetDateTime createdAt;
}
