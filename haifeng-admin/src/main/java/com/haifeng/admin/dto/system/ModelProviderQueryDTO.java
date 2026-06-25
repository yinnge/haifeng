package com.haifeng.admin.dto.system;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ModelProviderQueryDTO extends BasePageQueryDTO {

    /**
     * 供应商名称（模糊查询）
     */
    @Size(max = 50, message = "供应商名称长度不能超过50")
    private String providerName;

    /**
     * 模型名称（模糊查询）
     */
    @Size(max = 100, message = "模型名称长度不能超过100")
    private String modelName;

    /**
     * 状态：0-禁用，1-启用
     */
    @Min(value = 0, message = "状态值最小为0")
    @Max(value = 1, message = "状态值最大为1")
    private Integer status;
}
