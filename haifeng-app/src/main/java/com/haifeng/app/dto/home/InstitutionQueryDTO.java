package com.haifeng.app.dto.home;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 培训机构列表查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InstitutionQueryDTO extends BasePageQueryDTO {

    /** 名称模糊查询 */
    @Size(max = 100, message = "名称最长100个字符")
    private String name;

    /** 分类精确筛选（4 个写死值之一），不传=全部分类 */
    @Size(max = 32, message = "分类最长32个字符")
    private String category;

    /** 省份精确筛选，不传=全部省份 */
    @Size(max = 30, message = "省份最长30个字符")
    private String province;
}
