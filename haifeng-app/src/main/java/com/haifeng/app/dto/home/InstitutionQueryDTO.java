package com.haifeng.app.dto.home;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 培训机构列表查询 DTO（C 端目前无业务筛选字段，保留 class 便于后续扩展）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InstitutionQueryDTO extends BasePageQueryDTO {
}
