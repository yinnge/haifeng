package com.haifeng.admin.dto.major;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 考研专业查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostgradMajorQueryDTO extends BasePageQueryDTO {

    /**
     * 专业名称
     */
    private String majorName;

    /**
     * 专业代码
     */
    private String majorCode;

    /**
     * 学位类型（学术学位/专业学位）
     */
    private String degreeType;

    /**
     * 学科门类
     */
    private String disciplineCategory;

    /**
     * 热门程度（热门/一般/冷门）
     */
    private String popularity;

    /**
     * 难度等级（高/中/低）
     */
    private String difficulty;

    /**
     * 状态
     */
    private Short status;
}
