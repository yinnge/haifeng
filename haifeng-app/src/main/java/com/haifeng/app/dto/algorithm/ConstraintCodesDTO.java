package com.haifeng.app.dto.algorithm;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 约束代码列表请求DTO
 */
@Data
public class ConstraintCodesDTO {

    @NotEmpty(message = "约束代码列表不能为空")
    @Size(max = 100, message = "约束代码数量不能超过100个")
    private List<@Size(max = 50, message = "约束代码长度不能超过50") String> codes;
}
