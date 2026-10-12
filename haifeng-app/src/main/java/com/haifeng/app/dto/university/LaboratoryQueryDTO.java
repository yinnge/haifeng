package com.haifeng.app.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * C 端实验室列表分页查询 DTO
 * universityId 在 path 上，本 DTO 承载分页参数（page/size）+ 可选 labType 精确筛选
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LaboratoryQueryDTO extends BasePageQueryDTO {

    /** 实验室类型精确筛选（可选，空/null = 不过滤） */
    private String labType;
}
