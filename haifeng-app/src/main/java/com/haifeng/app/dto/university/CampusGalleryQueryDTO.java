package com.haifeng.app.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * C 端校园图册分页查询 DTO
 * universityId 在 path 上，imageType 可选精准匹配
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CampusGalleryQueryDTO extends BasePageQueryDTO {

    private String imageType;
}
