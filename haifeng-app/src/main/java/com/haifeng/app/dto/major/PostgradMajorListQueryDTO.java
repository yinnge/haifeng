package com.haifeng.app.dto.major;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** C 端考研专业列表查询 DTO（spec 任务2接口1） */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostgradMajorListQueryDTO extends BasePageQueryDTO {

    /** 模糊查询（LIKE %name%） */
    private String name;

    /** 模糊查询（LIKE %code%） */
    private String code;

    /** 精准查询 */
    private String degreeType;

    /** 精准查询 */
    private String disciplineCategory;

    /** 精准查询 */
    private String popularity;

    /** 精准查询 */
    private String difficulty;
}
