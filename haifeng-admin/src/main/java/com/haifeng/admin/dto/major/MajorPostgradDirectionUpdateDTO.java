package com.haifeng.admin.dto.major;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MajorPostgradDirectionUpdateDTO {

    @NotNull(message = "本科专业ID不能为空")
    private Long majorId;

    @NotNull(message = "考研专业ID不能为空")
    private Long postgradMajorId;

    /**
     * 排序权重
     */
    private Integer sortOrder;
}
