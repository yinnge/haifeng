package com.haifeng.admin.dto.home;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class InstitutionQueryDTO extends BasePageQueryDTO {

    /**
     * 机构名称模糊查询
     */
    @Size(max = 50, message = "名称关键字最长50字符")
    private String name;

    /**
     * 分类精确筛选，空值=全部分类
     */
    @Size(max = 32, message = "分类最长32字符")
    private String category;

    /**
     * 省份精确筛选，空值=全部省份
     */
    @Size(max = 30, message = "省份最长30字符")
    private String province;

    /**
     * 状态筛选: 0-下架 1-上架
     */
    @Min(value = 0, message = "状态值最小为0")
    @Max(value = 1, message = "状态值最大为1")
    private Short status;

    /**
     * 管理账号精确筛选
     */
    private Long userId;
}
