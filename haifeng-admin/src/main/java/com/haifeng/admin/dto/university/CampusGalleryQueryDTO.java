package com.haifeng.admin.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CampusGalleryQueryDTO extends BasePageQueryDTO {

    private String universityName;

    private String imageType;

    private Integer status;
}
