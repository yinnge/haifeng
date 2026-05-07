package com.haifeng.admin.dto.university;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CampusGalleryAddDTO {

    @NotNull(message = "院校ID不能为空")
    private Long universityId;

    @NotBlank(message = "图片类型不能为空")
    private String imageType;

    @NotBlank(message = "图片URL不能为空")
    private String imageUrl;

    private Integer sortOrder;
}
