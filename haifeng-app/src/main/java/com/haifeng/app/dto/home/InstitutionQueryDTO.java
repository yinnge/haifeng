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
    @Size(max = 100, message = "名称最长100个字符")
    private String name;   // 名称模糊查询
}
