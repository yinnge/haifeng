package com.haifeng.admin.vo.resource;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ResourceDetailVO {

    private Long id;

    private String resourceName;

    private String coverUrl;

    private String description;

    private String resourceUrl;

    private String accessCode;

    private String category;

    private String fileType;

    private Integer viewCount;

    private Integer sortOrder;

    private Boolean isDeleted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
