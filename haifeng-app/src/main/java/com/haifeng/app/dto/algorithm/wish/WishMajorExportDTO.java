package com.haifeng.app.dto.algorithm.wish;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改专业导出状态DTO
 */
@Data
public class WishMajorExportDTO {

    @NotNull(message = "导出状态不能为空")
    private Boolean isExported;
}
