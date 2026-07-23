package com.haifeng.admin.vo.university;

import lombok.Data;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class CampusGalleryListVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private Long universityId;

    private String universityName;

    private String imageType;

    private String imageUrl;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createdAt;
}
