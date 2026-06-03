package com.haifeng.app.dto.algorithm;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 校验专业组约束请求DTO
 */
@Data
public class CheckGroupDTO {

    @NotNull(message = "专业组ID不能为空")
    @Positive(message = "专业组ID必须大于0")
    private Integer groupId;
}
