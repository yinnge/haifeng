package com.haifeng.admin.dto.industry;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class IndustryQueryDTO extends BasePageQueryDTO {

    /**
     * 行业名称（模糊查询）
     */
    private String industryName;

    /**
     * 行业分类（模糊查询）
     */
    private String category;

    /**
     * 人才趋势（模糊查询）
     */
    private String talentTrend;

    /**
     * 删除状态
     */
    private Boolean isDeleted;
}
