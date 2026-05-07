package com.haifeng.admin.dto.major;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 专业列表查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MajorQueryDTO extends BasePageQueryDTO {

    /**
     * 专业代码
     */
    private String majorCode;

    /**
     * 专业名称
     */
    private String majorName;

    /**
     * 学科名称
     */
    private String disciplineName;

    /**
     * 专业类型
     */
    private String majorType;

    /**
     * 学科门类
     */
    private String majorCategory;

    /**
     * 状态
     */
    private Short status;
}
