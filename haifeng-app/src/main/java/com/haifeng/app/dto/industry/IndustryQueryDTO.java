package com.haifeng.app.dto.industry;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * C 端行业列表查询 DTO
 * category 精准匹配
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IndustryQueryDTO extends BasePageQueryDTO {

    /** 行业分类精准匹配 */
    private String category;
}
