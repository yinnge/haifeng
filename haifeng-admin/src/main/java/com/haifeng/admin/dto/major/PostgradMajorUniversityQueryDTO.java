package com.haifeng.admin.dto.major;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 考研专业-大学关联查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostgradMajorUniversityQueryDTO extends BasePageQueryDTO {

    /**
     * 大学名称
     */
    private String universityName;

    /**
     * 考研专业名称
     */
    private String postgradMajorName;

    /**
     * 状态
     */
    private Short status;
}
