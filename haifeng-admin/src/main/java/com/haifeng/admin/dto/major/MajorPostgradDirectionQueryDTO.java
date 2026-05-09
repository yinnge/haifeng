package com.haifeng.admin.dto.major;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MajorPostgradDirectionQueryDTO extends BasePageQueryDTO {

    /**
     * 本科专业名称（模糊查询）
     */
    private String majorName;

    /**
     * 考研专业名称（模糊查询）
     */
    private String postgradMajorName;
}
