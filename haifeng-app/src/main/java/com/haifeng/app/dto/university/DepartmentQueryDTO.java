package com.haifeng.app.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * C 端院系列表分页查询 DTO
 * universityId 在 path 上，本 DTO 仅承载分页参数（page/size）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DepartmentQueryDTO extends BasePageQueryDTO {
}
