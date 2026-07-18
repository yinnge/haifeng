package com.haifeng.admin.dto.university;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CampusGalleryUpdateDTO {

    @NotBlank(message = "图片类型不能为空")
    private String imageType;

    @NotBlank(message = "图片URL不能为空")
    private String imageUrl;

    private Integer sortOrder;

    private Short status;
}
