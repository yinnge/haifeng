package com.haifeng.admin.vo.university;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CampusGalleryDetailVO {

    private Long id;

    private Long universityId;

    private String universityName;

    private String imageType;

    private String imageUrl;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
