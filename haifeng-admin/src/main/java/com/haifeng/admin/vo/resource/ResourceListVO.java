package com.haifeng.admin.vo.resource;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ResourceListVO {

    private Long id;

    private String resourceName;

    private String category;

    private String fileType;

    private Integer viewCount;

    private Integer sortOrder;

    private Boolean isDeleted;

    private OffsetDateTime updatedAt;
}
